/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 27, 2006
 * 
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Level;
import org.zamia.ERManager;
import org.zamia.ErrorReport;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaException.ExCat;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTDeclarationSearch;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.util.HashSetArray;


/**
 * 
 * everything in the syntax tree is supposed to be derived from this base class
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class ASTObject implements Serializable {

	public enum ASTErrorMode {
		EXCEPTION, RETURN_NULL
	};

	protected long location; // upper 32 bits: col, lower 32 bits: line

	protected ASTObject parent;

	private static int counter = 0;

	protected int cnt = 0;

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	public static final boolean QUICK_ERROR_REPORTING=true;
	
	public ASTObject(ASTObject aParent, long aLocation) {
		location = aLocation;
		parent = aParent;
		cnt = counter++;
	}

	public ASTObject(long aLocation) {
		this(null, aLocation);
	}

	public SourceLocation getLocation() {

		DesignUnit du = getDesignUnit();

		if (du == null)
			return null;

		return new SourceLocation(du.getSourceFile(), (int) (location & 0xFFFFFFFF), (int) (location >> 32));
	}

	// overwritten in DesignUnit
	public String getLibId() {
		if (parent != null)
			return parent.getLibId();
		return null;
	}

	// overwritten in DesignUnit
	public DesignUnit getDesignUnit() {
		if (parent != null)
			return parent.getDesignUnit();
		return null;
	}

	public void setParent(ASTObject aParent) {
		setParent(aParent, false);
	}

	public void setParent(ASTObject aParent, boolean aForce) {

		if (!aForce && parent != null)
			return;

		parent = aParent;
	}

	public ASTObject getParent() {
		return parent;
	}

	public abstract int getNumChildren();

	public abstract ASTObject getChild(int aIdx);

	// should be overwritten by ios which have declarations
	public DeclarativeItem findDeclaration(String aId, ZamiaProject aZPrj) {
		if (ASTDeclarationSearch.dump) {
			logger.debug("SA: findDeclaration ('%s'), this=%s", aId, toString());
		}
		ASTObject p = getParent();
		if (p != null)
			return p.findDeclaration(aId, aZPrj);
		return null;
	}

	public static void printSpaces(PrintStream aOut, int aN) {
		for (int i = 0; i < aN; i++)
			aOut.print(" ");
	}

	public static void printIndented(String aStr, int aIndent, PrintStream aOut) {
		for (int i = 0; i < aIndent; i++)
			aOut.print(" ");
		aOut.print(aStr);
	}

	public static void printlnIndented(String aStr, int aIndent, PrintStream aOut) {
		printIndented(aStr, aIndent, aOut);
		aOut.println();
	}

	// FIXME: make abstract, implement in all subclasses
	public void dumpVHDL(int aIndent, PrintStream aOut) throws ZamiaException {
		throw new ZamiaException("Don't know how to dump vhdl for " + this);
	}

	@SuppressWarnings("unchecked")
	public static void dump(PrintStream aOut, ASTObject aObj, int aIndent, HashSet<Object> aDumpedObjects) {

		if (aDumpedObjects.contains(aObj))
			return;
		aDumpedObjects.add(aObj);

		Class cls = aObj.getClass();

		while (!Object.class.equals(cls)) {
			Field[] fields = cls.getDeclaredFields();
			int n = fields.length;
			for (int i = 0; i < n; i++) {
				Field field = fields[i];
				field.setAccessible(true);

				String id = field.getName();
				if (id.equals("parent") || id.equals("context") || id.equals("uid") || id.equals("counter") || id.equals("cnt") || id.contains("OP") || id.contains("CAT")
						|| id.contains("log"))
					continue;

				printSpaces(aOut, aIndent);
				aOut.print(field.getName());

				try {
					Object value = field.get(aObj);

					if (value instanceof ASTObject) {
						ASTObject io = (ASTObject) value;
						aOut.println(": " + io.getClass().getName() + " { #" + io.cnt);
						dump(aOut, (ASTObject) value, aIndent + 2, aDumpedObjects);
						printSpaces(aOut, aIndent);
						aOut.print(field.getName());
						aOut.println(" } #" + io.cnt);
					} else if (value instanceof List) {
						List list = (List) value;

						aOut.println(": " + list.getClass().getName() + " [ ");

						int m = list.size();
						for (int j = 0; j < m; j++) {
							Object o = list.get(j);

							printSpaces(aOut, aIndent + 2);
							aOut.println("[" + j + "]");

							if (o instanceof ASTObject) {
								ASTObject io = (ASTObject) o;
								printSpaces(aOut, aIndent + 2);
								aOut.println(": " + io.getClass().getName() + " { #" + io.cnt);
								dump(aOut, io, aIndent + 4, aDumpedObjects);
							} else {
								printSpaces(aOut, aIndent + 2);
								aOut.println(" = " + value);
							}
						}
						printSpaces(aOut, aIndent);
						aOut.print(field.getName());
						aOut.println(" ]");
					} else {
						aOut.println(" = " + value);
					}

				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cls = cls.getSuperclass();
		}

		if (aIndent == 2) {
			aOut.println();
			aOut.println("# objects dumped: " + aDumpedObjects.size());
		}
	}

	@SuppressWarnings("unchecked")
	public static void sanityCheck(PrintStream aOut, ASTObject aObj, HashSet<Object> aCheckedObjects) {

		if (aCheckedObjects.contains(aObj))
			return;
		aCheckedObjects.add(aObj);

		Class cls = aObj.getClass();

		while (!Object.class.equals(cls)) {
			Field[] fields = cls.getDeclaredFields();
			int n = fields.length;
			for (int i = 0; i < n; i++) {
				Field field = fields[i];
				field.setAccessible(true);

				String id = field.getName();

				if (id.equals("parent"))
					continue;

				try {
					Object value = field.get(aObj);

					if (value instanceof OperationLiteral)
						continue;

					if (value instanceof ASTObject) {
						ASTObject io = (ASTObject) value;

						if (io.getParent() != aObj) {
							System.out.println("Error: io " + io + " (cnt " + io.cnt + ") does not point to parent " + aObj + " (cnt=" + aObj.cnt + ")");
						}

						sanityCheck(aOut, io, aCheckedObjects);
					} else if (value instanceof List) {
						List list = (List) value;

						int m = list.size();
						for (int j = 0; j < m; j++) {
							Object o = list.get(j);

							if (o instanceof ASTObject) {
								ASTObject io = (ASTObject) o;

								if (io instanceof OperationLiteral)
									continue;

								if (io.getParent() != aObj) {
									System.out.println("Error: io " + io + " (cnt " + io.cnt + ") does not point to parent " + aObj + " (cnt=" + aObj.cnt + ")");
								}

								sanityCheck(aOut, io, aCheckedObjects);
							}
						}
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cls = cls.getSuperclass();
		}
	}

	public long getLineCol() {
		return location;
	}

	// convenience

	public void dumpObjectTree(ZamiaLogger aZL, Level aLevel) {

		aZL.log(aLevel, "");
		aZL.log(aLevel, "-----------------------------------------------");
		aZL.log(aLevel, "Object tree of IO " + this + ": ");

		ASTObject io = this;
		int count = 0;
		while (count < 20) {

			aZL.log(aLevel, "%3d : %8h : %20s : %s\n", count, io.hashCode(), io.getLocation(), io.getClass());
			aZL.log(aLevel, "    " + io);
			count++;
			io = io.getParent();
			if (io == null)
				break;
		}

		aZL.log(aLevel, "Object tree of IO " + this + " ends here ");
		aZL.log(aLevel, "-----------------------------------------------");
	}

	public abstract void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException;

	public int getCnt() {
		return cnt;
	}

	public static IGItem tryToGenerateIGOperation(IGItem aItem, IGContainer aContainer, SourceLocation aSrc) {

		if (aItem instanceof IGObject) {
			return new IGOperationObject((IGObject) aItem, aSrc, aContainer.getZDB());
		}

		return aItem;
	}

	/**
	 * Used in CompletionProcessor
	 * 
	 * to be overwritten by subclasses if they have declarations
	 * 
	 * @param aIndentifiers
	 * @param aZPrj
	 *            TODO
	 */
	public void collectIdentifiers(HashSetArray<String> aIndentifiers, ZamiaProject aZPrj) {
		if (parent != null)
			parent.collectIdentifiers(aIndentifiers, aZPrj);
	}

	/*
	 * convenience error reporting functions
	 */

	public void reportError(ZamiaException aException) {

		aException.setCat(ExCat.INTERMEDIATE);

		ERManager erm = null;

		ZamiaProject zprj = getZPrj();
		if (zprj != null) {
			erm = zprj.getERM();
		}

		SourceLocation location = aException.getLocation();
		if (location == null) {
			location = getLocation();
			aException = new ZamiaException(aException.getMessage(), location);
		}

		if (erm != null) {
			erm.addError(aException);
		} else {
			logger.error("Error: %s: %s", aException.getLocation(), aException.getMessage());
		}
	}

	public ZamiaProject getZPrj() {
		DesignUnit du = getDesignUnit();
		if (du != null) {
			return du.getZPrj();
		}
		return null;
	}

	public void reportError(String aMessage) {
		reportError(new ZamiaException(ExCat.INTERMEDIATE, true, aMessage, this));
	}

	public void reportWarning(String aMessage) {
		reportError(new ZamiaException(ExCat.INTERMEDIATE, false, aMessage, this));
	}

	public void reportError(String aFormat, Object... aArgs) {

		String msg = String.format(aFormat, aArgs);

		//String msg = formatter.format(aFormat, aArgs);
		//System.out.printf("H");

		reportError(msg);
	}

	public void reportWarning(String aFormat, Object... aArgs) {
		String msg = String.format(aFormat, aArgs);
		reportWarning(msg);
	}

	// utility functions for new postponed error reporting
	protected void reportError(String aMsg, SourceLocation aLocation, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		if (aErrorMode == ASTErrorMode.EXCEPTION) {

			if (aReport != null) {
				aReport.append(aMsg, aLocation);
				aReport.log();
			} else {
				logger.debug("Error report: %s: %s", aLocation, aMsg);
			}

			throw new ZamiaException(aMsg, aLocation);
		}
		aReport.append(aMsg, aLocation);
	}

	protected void reportError(String aMsg, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		reportError(aMsg, getLocation(), aErrorMode, aReport);
	}

	protected void reportError(String aMsg, ASTObject aObj, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		reportError(aMsg, aObj.getLocation(), aErrorMode, aReport);
	}

}
