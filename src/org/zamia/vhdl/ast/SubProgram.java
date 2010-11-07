/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Aug 2, 2005
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SubProgram extends BlockDeclarativeItem {

	// class SubProgramTransients extends IntermediateTransients {
	// public InterpreterCode ic;
	// }

	public final static String RETURN_VAR_NAME = "%%RETURN__VALUE%%";

	private Name returnType;

	private SequenceOfStatements code = null;

	private InterfaceList interfaces;

	private ArrayList<BlockDeclarativeItem> declarations = new ArrayList<BlockDeclarativeItem>();

	private boolean pure;

	public SubProgram(String id_, InterfaceList interfaces_, Name returnType_, boolean pure_, VHDLNode parent_, long location_) throws ZamiaException {
		super(id_, parent_, location_);
		pure = pure_;
		returnType = returnType_;
		if (returnType != null) {
			returnType.setParent(this);
		}

		interfaces = new InterfaceList(this, location_);

		if (interfaces_ != null) {
			int n = interfaces_.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				InterfaceDeclaration interf = interfaces_.get(i);
				interfaces.add(interf);
			}
		}
	}

	public void add(BlockDeclarativeItem item_) {
		declarations.add(item_);
		item_.setParent(this);
	}

	// private void checkTypes(OperationCache cache_) throws ZamiaException {
	// if (code == null)
	// return ;
	// code.checkTypes(cache_);
	// }

	public void dump(PrintStream out_) {
		out_.println("SubProgram id=" + id);
	}

	@Override
	public int getNumChildren() {
		return getNumDeclarations() + 3;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return returnType;
		if (idx_ == 1)
			return code;
		if (idx_ == 2)
			return interfaces;
		return getDeclaration(idx_ - 3);
	}

	public SequenceOfStatements getCode() {
		return code;
	}

	public ArrayList<BlockDeclarativeItem> getDeclarations() {
		return declarations;
	}

	public void setDeclarations(ArrayList<BlockDeclarativeItem> declarations2) {
		declarations = declarations2;
		int n = declarations.size();
		for (int i = 0; i < n; i++) {
			declarations.get(i).setParent(this);
		}
	}

	public void setCode(SequenceOfStatements seq_) {
		if (seq_ == null)
			return;
		code = seq_;
		seq_.setParent(this);
	}

	public boolean hasCode() {
		return code != null;
	}

	public int getNumDeclarations() {
		return declarations.size();
	}

	public BlockDeclarativeItem getDeclaration(int i) {
		return declarations.get(i);
	}

	public int getNumInterfaces() {
		return interfaces.getNumInterfaces();
	}

	public InterfaceDeclaration getInterface(int idx_) {
		return interfaces.get(idx_);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(getId() + "(");

		int n = getNumInterfaces();
		for (int i = 0; i < n; i++) {
			InterfaceDeclaration interf = getInterface(i);
			b.append(interf.toString());
			if (i < n - 1) {
				b.append(",");
			}
		}

		b.append(")");
		if (returnType != null) {
			b.append(" RETURNS " + returnType);
		}
		return b.toString();
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		// do we have a declaration that shadows this id ?
		// or do we need to report the declaration ?

		int n = declarations.size();
		for (int i = 0; i < n; i++) {

			BlockDeclarativeItem decl = declarations.get(i);

			String id = decl.getId();

			if (id.equals(id_)) {
				if (depth_ > 0) {
					return;
				} else {
					result_.add(new ReferenceSite(decl, RefType.Declaration));
				}
			}
		}

		if (code != null) {
			code.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}

		if (id_.equals(getId())) {
			result_.add(new ReferenceSite(this, RefType.Declaration));
		}

	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType rt = null;
		if (returnType != null) {
			rt = returnType.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
		}

		IGSubProgram sub = new IGSubProgram(aContainer.store(), rt, getId(), getLocation(), aEE.getZDB());

		IGContainer container = sub.getContainer();

		if (interfaces != null) {
			int n = interfaces.getNumInterfaces();
			for (int i = 0; i < n; i++) {
				try {
					InterfaceDeclaration interf = (InterfaceDeclaration) interfaces.get(i);

					IGContainerItem igi = interf.computeIG(null, container, aEE);

					container.addInterface((IGObject) igi);

				} catch (ZamiaException e) {
					reportError(e);
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}

		/*
		 * look for a header / specification for this subprogram 
		 */

		sub.computeSignatures();

		// header from package header

		IGSubProgram matchingSub = null;
		if (aSpecItems != null) {

			int n = aSpecItems.size();

			for (int i = n - 1; i >= 0; i--) {

				IGContainerItem specItem = aSpecItems.get(i);
				if (!(specItem instanceof IGSubProgram)) {
					continue;
				}

				IGSubProgram sub2 = (IGSubProgram) specItem;

				if (sub2.getBuiltin() != null) {
					continue;
				}

				if (sub.conforms(sub2)) {
					if (matchingSub != null) {
						throw new ZamiaException("SubProgram: More than one matching header declaration found for " + getId(), getLocation());
					}
					matchingSub = sub2;
				}
			}
		}

		// local header ?

		ArrayList<IGContainerItem> localSpecItems = aContainer.findLocalItems(getId());
		IGSubProgram matchingLocalSub = null;
		if (localSpecItems != null) {

			int n = localSpecItems.size();

			for (int i = n - 1; i >= 0; i--) {

				IGContainerItem specItem = localSpecItems.get(i);
				if (!(specItem instanceof IGSubProgram)) {
					continue;
				}

				IGSubProgram sub2 = (IGSubProgram) specItem;

				if (sub2.getBuiltin() != null) {
					continue;
				}

				if (sub.conforms(sub2)) {
					if (matchingLocalSub != null) {
						throw new ZamiaException("SubProgram: More than one matching declaration found for " + getId(), getLocation());
					}
					matchingLocalSub = sub2;
				}
			}
		}

		// if we have found a matching header, we'll add our body to it
		// otherwise we create a new sub.

		if (matchingLocalSub != null) {
			// add decls/code to header instead of creating a new sub.
			sub = matchingLocalSub;

			IGContainer container2 = sub.getContainer();

			container.removeInterfaces();
			int nI = container2.getNumInterfaces();
			for (int i = 0; i < nI; i++) {
				container.addInterface(container2.getInterface(i));
			}

			sub.setContainer(container);

		} else if (matchingSub != null) {

			// add decls/code to header instead of creating a new sub.
			sub = matchingSub;

			IGContainer container2 = sub.getContainer();

			container.removeInterfaces();
			int nI = container2.getNumInterfaces();
			for (int i = 0; i < nI; i++) {
				container.addInterface(container2.getInterface(i));
			}

			sub.setContainer(container);

		} else {
			aContainer.add(sub); // we have to add it right away for recursion
		}

		try {
			if (code != null) {

				IGSequenceOfStatements zSeq = new IGSequenceOfStatements(null, getLocation(), aEE.getZDB());

				// declarations:

				int n = declarations.size();
				for (int i = 0; i < n; i++) {
					try {
						BlockDeclarativeItem decl = (BlockDeclarativeItem) declarations.get(i);
						decl.computeIG(null, container, aEE);
					} catch (ZamiaException e) {
						reportError(e);
					} catch (Throwable t) {
						el.logException(t);
					}
				}

				code.generateIG(zSeq, container, aEE);
				sub.setCode(zSeq);

				sub.storeOrUpdate();

			} else {
				// builtin ?
				IGBuiltin builtin = IGSubProgram.getBuiltin(getId());

				if (builtin != null) {

					sub.setBuiltin(builtin);

				}
			}
		} catch (ZamiaException e) {
			reportError(e);
		} catch (Throwable t) {
			el.logException(t);
		}

		return sub;
	}

	public InterfaceList getInterfaces() {
		return interfaces;
	}

	public void collectIdentifiers(HashSetArray<String> identifiers_, ZamiaProject zprj_) {

		int n = declarations.size();
		for (int i = 0; i < n; i++) {
			identifiers_.add(declarations.get(i).getId());
		}

		super.collectIdentifiers(identifiers_, zprj_);
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {

		StringBuilder buf = new StringBuilder();

		if (returnType != null) {
			buf.append("PROCEDURE " + getId() + interfaces + " ");

		} else {
			if (pure) {
				buf.append("FUNCTION ");
			} else {
				buf.append("IMPURE FUNCTION ");
			}
			buf.append(getId());
			buf.append(interfaces);
			buf.append("RETURN " + returnType);
		}

		if (code != null) {
			buf.append(" IS");
		} else {
			buf.append(";");
		}

		printlnIndented(buf.toString(), indent_, out_);

		if (code != null) {
			printlnIndented("BEGIN", indent_, out_);
			code.dumpVHDL(indent_ + 1, out_);
			printlnIndented("END " + getId() + ";", indent_, out_);
		}
	}
}
