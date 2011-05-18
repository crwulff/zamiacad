/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.zamia.ErrorReport;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.util.HashSetArray;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;
import org.zamia.zdb.ZDBIIDSaver;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public abstract class IGItem implements Serializable, ZDBIIDSaver {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private String fZPrjID;

	protected long fDBID;

	private long fSFDBID;

	private int fLine;

	private int fCol;

	private transient ZDB fZDB;

	private transient SourceLocation fLocation;

	public IGItem(SourceLocation aLocation, ZDB aZDB) {

		if (aZDB != null) {
			fZDB = aZDB;
			ZamiaProject zprj = (ZamiaProject) aZDB.getOwner();
			fZPrjID = zprj.getId();
			if (aLocation != null) {
				fSFDBID = getOrCreateSFHID(aLocation.fSF);
				fLine = aLocation.fLine;
				fCol = aLocation.fCol;
				fLocation = aLocation;
			}
		}
	}

	public long save(IGItem aItem) {
		if (aItem == null)
			return 0;
		long dbid = aItem.getDBID();
		if (dbid == 0) {
			dbid = getZDB().store(aItem);
		}
		return dbid;
	}

	public long storeOrUpdate() {
		if (fDBID == 0) {
			save(this);
		} else {
			getZDB().update(fDBID, this);
		}
		return fDBID;
	}

	public long store() {
		return save(this);
	}

	public void setDBID(long aId) {
		if (fDBID != 0) {
			logger.warn("IGItem: warning: re-storing %s DBID: %d => %d", this, fDBID, aId);
		}
		fDBID = aId;
	}

	public void setZDB(ZDB aZDB) {
		fZDB = aZDB;
	}

	public ZDB getZDB() {

		if (fZDB == null) {
			ZamiaProject zprj = ZamiaProject.lookupProject(fZPrjID);
			if (zprj != null) {
				fZDB = zprj.getZDB();
			}
		}

		return fZDB;
	}

	public ZamiaProject getZPrj() {
		ZDB db = getZDB();
		return (ZamiaProject) db.getOwner();
	}

	public IGManager getIGM() {
		return getZPrj().getIGM();
	}

	public long getDBID() {
		return fDBID;
	}

	public long getSFDBID() {
		return fSFDBID;
	}

	public int getLine() {
		return fLine;
	}

	public int getCol() {
		return fCol;
	}

	private long getOrCreateSFHID(SourceFile aSF) {

		String path = aSF.getAbsolutePath();

		long id = fZDB.getIdx("SFIdx", path);

		if (id != 0) {
			return id;
		}

		id = fZDB.store(aSF);
		fZDB.putIdx("SFIdx", path, id);

		return id;
	}

	private SourceFile getSF(long aSfhid) {
		return (SourceFile) getZDB().load(aSfhid);
	}

	public SourceLocation computeSourceLocation() {

		if (fLocation != null) {
			return fLocation;
		}

		long sfDBID = getSFDBID();

		if (sfDBID == 0) {
			return null;
		}

		SourceFile sf = getSF(sfDBID);

		fLocation = new SourceLocation(sf, getLine(), getCol());

		return fLocation;
	}

	protected void addItemAccess(IGItem aItem, AccessType aAccessType, int aDepth, IGItem aFilterItem, AccessType aFilterType, HashSetArray<IGItemAccess> aAccessedItems) {

		if (aItem == null) {
			return;
		}

		if (aFilterItem != null && aFilterItem.getDBID() != aItem.getDBID())
			return;

		if (aFilterType != null && aAccessType != aFilterType)
			return;

		SourceLocation location = computeSourceLocation();
		if (location == null)
			return;

		aAccessedItems.add(new IGItemAccess(aItem, aAccessType, aDepth, computeSourceLocation()));
	}

	public abstract int getNumChildren();

	public abstract IGItem getChild(int aIdx);

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
		reportError(aMsg, computeSourceLocation(), aErrorMode, aReport);
	}

	protected void reportError(String aMsg, VHDLNode aObj, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		reportError(aMsg, aObj.getLocation(), aErrorMode, aReport);
	}

	public void dump(int aMaxDepth) {

		HashSet<Long> done = new HashSet<Long>();

		ZStack<Long> todo = new ZStack<Long>();

		todo.push(fDBID);

		while (!todo.isEmpty()) {

			long dbid = todo.pop();
			if (done.contains(dbid)) {
				continue;
			}
			done.add(dbid);

			Object obj = fZDB.load(dbid);

			System.out.println("*** Dumping ZDB Object id " + dbid + ":" + obj + " ***\n");

			dump(obj, todo, 1, aMaxDepth);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private static void dump(Object o, ZStack<Long> todo, int depth, int aMaxDepth) {
		if (o == null) {
			printIndented(depth, "NULL");
			return;
		}

		if (depth > aMaxDepth) {
			printIndented(depth, "...");
			return;
		}

		if (o instanceof Object[]) {
			printIndented(depth, "[");
			for (Object obj : (Object[]) o) {
				dump(obj, todo, depth + 1, aMaxDepth);
			}
			printIndented(depth, "]");
		} else if (o instanceof HashMap) {
			printIndented(depth, "HashMap[");
			HashMap hm = (HashMap) o;
			for (Iterator it = hm.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = hm.get(key);
				dump(value, todo, depth + 1, aMaxDepth);
				dump(key, todo, depth + 1, aMaxDepth);
			}
			printIndented(depth, "]");
		} else if (o instanceof Array) {
			printIndented(depth, "[");
			Array a = (Array) o;
			int m = Array.getLength(a);
			for (int j = 0; j < m; j++) {
				Object o2 = Array.get(a, j);
				dump(o2, todo, depth + 1, aMaxDepth);
			}
			printIndented(depth, "]");
		} else if (o instanceof ArrayList) {
			printIndented(depth, "[");
			ArrayList al = (ArrayList) o;
			int m = al.size();
			for (int j = 0; j < m; j++) {
				Object o2 = al.get(j);
				dump(o2, todo, depth + 1, aMaxDepth);
			}
			printIndented(depth, "]");
		} else if (o instanceof HashSetArray) {
			HashSetArray hma = (HashSetArray) o;
			printIndented(depth, "HashSetArray [");

			int m = hma.size();
			for (int j = 0; j < m; j++) {
				Object o2 = hma.get(j);
				dump(o2, todo, depth + 1, aMaxDepth);
			}
			printIndented(depth, "]");
		} else {

			Class cls = o.getClass();

			while (!Object.class.equals(cls)) {
				Field[] fields = cls.getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);

					int modifiers = field.getModifiers();

					if (Modifier.isStatic(modifiers))
						continue;
					else if (Modifier.isFinal(modifiers))
						continue;
					else if (Modifier.isTransient(modifiers))
						continue;

					Object obj;
					try {
						obj = field.get(o);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}

					if (isAPrimitiveType(field.getType())) {
						printIndented(depth, field.getName() + ": " + obj);

						if (field.getName().contains("DBID")) {
							if (obj instanceof Long) {
								long dbid = ((Long) obj).longValue();
								todo.push(dbid);
							}
						}

					} else {
						printIndented(depth, field.getName() + " {");
						dump(obj, todo, depth + 1, aMaxDepth);
						printIndented(depth, "}");
					}
				}

				cls = cls.getSuperclass();
			}

		}
	}

	private static void printIndented(int aDepth, String aStr) {
		for (int i = 0; i < aDepth; i++) {
			System.out.print("  ");
		}
		System.out.println(aStr);
	}

	@SuppressWarnings("rawtypes")
	private static boolean isAPrimitiveType(Class clazz) {
		if (clazz == java.lang.Boolean.TYPE)
			return true;

		if (clazz == java.lang.Character.TYPE)
			return true;

		if (clazz == java.lang.Byte.TYPE)
			return true;

		if (clazz == java.lang.Short.TYPE)
			return true;

		if (clazz == java.lang.Integer.TYPE)
			return true;

		if (clazz == java.lang.Long.TYPE)
			return true;

		if (clazz == java.lang.Float.TYPE)
			return true;

		if (clazz == java.lang.Double.TYPE)
			return true;

		if (clazz == java.lang.Void.TYPE)
			return true;

		return false;
	}

}
