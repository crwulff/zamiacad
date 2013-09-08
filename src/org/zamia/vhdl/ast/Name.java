/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 26.06.2004
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAlias;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.zdb.ZDB;

/**
 * Represents names (including extensions such as indices, ranges, suffixes...)
 * and function calls
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Name extends VHDLNode {

	private String fId;

	private ArrayList<NameExtension> fExtensions;

	public Name(String id_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		fId = id_;
	}

	// convenience
	public void addId(String id_, long location_) {
		NameExtensionSuffix ext = new NameExtensionSuffix(new Suffix(id_, location_), this, location_);
		add(ext);
	}

	public String getId() {
		return fId;
	}

	public void setId(String id) {
		this.fId = id;
	}

	@Override
	public int getNumChildren() {
		if (fExtensions == null)
			return 0;
		return fExtensions.size();
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (fExtensions == null)
			return null;
		return fExtensions.get(idx_);
	}

	public Name cloneName() {
		Name nn = new Name(fId, null, getLineCol());

		int n = getNumExtensions();
		for (int i = 0; i < n; i++) {
			NameExtension ext = getExtension(i);
			nn.add(ext);
		}

		return nn;
	}

	/*
	 * extensions
	 */

	public void add(NameExtension extension_) {
		if (fExtensions == null)
			fExtensions = new ArrayList<NameExtension>(1);
		fExtensions.add(extension_);
		extension_.setParent(this);
	}

	public int getNumExtensions() {
		if (fExtensions == null)
			return 0;
		return fExtensions.size();
	}

	public NameExtension getExtension(int idx_) {
		return fExtensions.get(idx_);
	}

	public boolean isSimpleName() {
		return getNumExtensions() == 0;
	}

	/************************************************************
	 * 
	 * 
	 * IG world starts here
	 * 
	 * @param aCache
	 *            TODO
	 * 
	 * 
	 ************************************************************/

	public final IGResolveResult computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {
		if (aCache == null) {
			return computeIGP(aTypeHint, aContainer, aEE, null, aErrorMode, aReport);
		}

		IGResolveResult result = aCache.getIGResolveResult(this, aTypeHint);
		if (result != null)
			return result;

		result = computeIGP(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (result == null)
			return null;
		aCache.setIGResolveResult(this, aTypeHint, result);
		return result;
	}

	private IGResolveResult createOperations(IGResolveResult aRR, ZDB aZDB) {

		IGResolveResult res = new IGResolveResult();

		for (IGItem item : aRR) {

			if (item instanceof IGObject) {
				item = new IGOperationObject((IGObject) item, getLocation(), aZDB);
				
			} else if (item instanceof IGOperationAlias) {
				
				// Here, item is a declaration-specified alias and, by wrapping it, we produce a reference to the declaration.
				// Without wrapping, any occurrence of alias in an expression, e.g. t <= add(alias, value), will tell that it 
				// is located in the declaration area. And, IG search will return the parent expression even when user cursor 
				// is right over the alias.
				// I'm not aware if similar problems occur with other kinds of operations. 
				// A small todo: the alias declaration seems redundant after we refer its fOp
				// as declaration rather than alias itself
				IGOperationAlias alias = (IGOperationAlias) item;
				item = new IGOperationAlias(alias.getOperand(0), alias.getType(), ((IGOperationAlias) item).getId(), getLocation(), aZDB);
			}
			
			res.addItem(item);
		}

		return res;
	}

	private IGResolveResult computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		String id = getId();

		/*
		 * Step 1/2 : resolve id, create hierarchical list of all potential matches
		 */

		IGResolveResult rr = aContainer.resolve(id);
		if (rr.getNumResults() == 0) {

			// is this actually a string literal?

			if (id.charAt(0) == '"' && getNumExtensions() == 0 && aTypeHint != null && aTypeHint.isArray()) {

				int l = id.length();
				String image = id.substring(1, l - 1);

				IGType type = aTypeHint.fitToLength(image.length() - 1, aContainer, getLocation(), aEE, aErrorMode, aReport);

				rr.addItem(new IGOperationLiteral.STR(image, type, getLocation()));

				return rr;
			}

			reportError("Couldn't resolve " + getId(), this, aErrorMode, aReport);
			return rr;
		}

		rr = createOperations(rr, aEE.getZDB());

		/*
		 * Step 2/2 : filter/modify the match tree through the extensions
		 */

		ErrorReport report = aReport != null ? aReport : new ErrorReport();

		rr = applyExtensions(rr, aContainer, aEE, aCache, report);

		return rr;
	}

	private IGResolveResult applyExtensions(IGResolveResult aRR, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ErrorReport aReport) throws ZamiaException {

		int nExt = getNumExtensions();
		if (nExt == 0) {
			return aRR;
		}

		ArrayList<IGItem> items = new ArrayList<IGItem>(aRR.getNumResults());
		for (IGItem item : aRR) {
			items.add(item);
		}

		SourceLocation prevLocation = getLocation();

		for (int iExt = 0; iExt < nExt; iExt++) {

			NameExtension ext = getExtension(iExt);

			ArrayList<IGItem> newItems = new ArrayList<IGItem>(items.size());
			for (IGItem item : items) {
				ext.computeIG(item, prevLocation, aContainer, aEE, aCache, newItems, aReport);
				prevLocation = ext.getLocation();
			}

			items = newItems;
		}

		return new IGResolveResult(items);
	}

	// convenience

	public IGType computeIGAsType(IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGResolveResult result = computeIG(null, aContainer, aEE, aCache, aErrorMode, aReport);

		for (IGItem item : result) {

			if (item instanceof IGType) {
				return (IGType) item;
			}
		}

		reportError("Type expected here.", this, aErrorMode, aReport);
		return null;
	}

	public IGOperation computeIGAsOperation(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGResolveResult result = computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);

		ErrorReport report = aReport;
		if (aReport == null) {
			report = new ErrorReport();
		}

		IGOperation fallbackOp = null;

		int n = result.getNumResults();
		for (int i = 0; i < n; i++) {
			IGItem item = result.getResult(i);

			if (item instanceof IGOperation) {
				IGOperation op = (IGOperation) item;

				if (aTypeHint != null) {
					IGType t = op.getType();
					if (t != null) {
						if (t.isAssignmentCompatible(aTypeHint, getLocation())) {
							return op;
						}
					} else {
						report.append("Operation " + op + " does not have a type.", getLocation());
					}
				} else {
					return op;
				}
				fallbackOp = op;
				;

			} else if (item instanceof IGSubProgram) {

				IGSubProgram sub = (IGSubProgram) item;

				AssociationList al = new AssociationList(this, getLineCol());

				IGOperationInvokeSubprogram invocation = sub.generateInvocation(al, aContainer, aEE, aCache, getLocation(), aReport);
				if (invocation != null) {
					return invocation;
				}

			} else if (item instanceof IGType) {

				IGType type = (IGType) item;

				IGOperation r = type.getRange();
				if (r == null) {
					report.append("Type " + type + " not allowed in an expression.", getLocation());
				}

				return r;
			} else if (item instanceof IGObject) {
				return new IGOperationObject((IGObject) item, getLocation(), aEE.getZDB());
			}

		}

		if (fallbackOp != null) {
			return fallbackOp;
		}
		reportError("Expression expected here.", this, aErrorMode, aReport);
		return null;
	}

	public IGDUUID computeIGAsDesignUnit(IGContainer aContainer, IGElaborationEnv aEE, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ErrorReport report = aReport;
		if (aReport == null) {
			report = new ErrorReport();
		}

		IGResolveResult result = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		for (IGItem item : result) {

			if (item instanceof IGDUUID) {
				return (IGDUUID) item;
			}
		}

		reportError("Design unit expected here.", this, aErrorMode, report);
		return null;
	}

	public IGSubProgram computeIGAsResF(IGContainer aContainer, IGElaborationEnv aEE, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ErrorReport report = aReport;
		if (aReport == null) {
			report = new ErrorReport();
		}

		IGResolveResult result = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		for (IGItem item : result) {

			if (item instanceof IGSubProgram) {

				IGSubProgram sub = (IGSubProgram) item;

				IGContainer formalScope = sub.getContainer();

				ArrayList<IGObject> interfaces = formalScope.getInterfaceList();
				if (interfaces.size() != 1) {
					continue;
				}

				if (!sub.isFunction()) {
					continue;
				}

				return sub;
			}
		}

		reportError("Resolution function expected here.", this, aErrorMode, report);
		return null;
	}

	public IGOperationInvokeSubprogram computeIGAsProcedure(IGContainer aContainer, IGElaborationEnv aEE, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ErrorReport report = aReport;
		if (aReport == null) {
			report = new ErrorReport();
		}

		IGResolveResult result = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		for (IGItem item : result) {

			if (item instanceof IGSubProgram) {

				IGSubProgram sub = (IGSubProgram) item;

				IGContainer formalScope = sub.getContainer();

				ArrayList<IGObject> interfaces = formalScope.getInterfaceList();
				if (interfaces.size() > 0) {
					report.append("Num interfaces >0", getLocation());
					continue;
				}

				if (sub.isFunction()) {
					report.append("Function, not procedure: ", getLocation());
					continue;
				}

				return new IGOperationInvokeSubprogram(new IGMappings(), sub, getLocation(), aEE.getZDB());

			} else if (item instanceof IGOperationInvokeSubprogram) {
				IGOperationInvokeSubprogram inv = (IGOperationInvokeSubprogram) item;

				IGSubProgram sub = inv.getSub();

				if (sub.isFunction()) {
					report.append("Function, not procedure: " + sub, getLocation());
					continue;
				}

				return inv;

			} else {
				report.append("Not a procedure: " + item, getLocation());
			}
		}

		reportError("Procedure expected here.", this, aErrorMode, report);
		return null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(fId);

		for (int i = 0; i < getNumExtensions(); i++) {
			buf.append(getExtension(i));
		}
		return buf.toString();
	}

	public String toVHDL() {
		return toString();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (aId.equals(fId)) {
			aResult.add(new ReferenceSite(this, aRefType));
		}

		int n = getNumExtensions();

		for (int i = 0; i < n; i++) {

			NameExtension ext = getExtension(i);

			ext.findReferences(aId, aCat, RefType.Read, aDepth + 1, aZPrj, aContainer, aCache, aResult, aTODO);

		}
	}
}