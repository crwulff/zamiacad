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
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGDUUID;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGOperationPhi;
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.zdb.ZDB;


/**
 * Represents names (including extensions such as indices, ranges, suffixes...)
 * and function calls
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class Name extends ASTObject {

	private String fId;

	private ArrayList<NameExtension> fExtensions;

	public Name(String id_, ASTObject parent_, long location_) {
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
	public ASTObject getChild(int idx_) {
		if (fExtensions == null)
			return null;
		return fExtensions.get(idx_);
	}

	public Name cloneName() {
		Name nn = new Name(fId, null, location);

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

	public final ArrayList<IGItem> computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {
		if (aCache == null) {
			return computeIGP(aTypeHint, aContainer, aEE, null, aErrorMode, aReport);
		}

		ArrayList<IGItem> items = aCache.getIGItems(this, aTypeHint);
		if (items != null)
			return items;

		items = computeIGP(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (items == null)
			return null;
		aCache.setIGItems(this, aTypeHint, items);
		return items;
	}

	private IGResolveResult createOperations(IGResolveResult aRR, ZDB aZDB) {

		IGResolveResult parent = aRR.getParent();
		if (parent != null) {
			parent = createOperations(parent, aZDB);
		}

		int n = aRR.getNumResults();
		ArrayList<IGItem> res = new ArrayList<IGItem>(n);
		for (int i = 0; i < n; i++) {
			IGItem item = aRR.getResult(i);

			if (item instanceof IGObject) {
				item = new IGOperationObject((IGObject) item, getLocation(), aZDB);
			}
			res.add(item);
		}

		return new IGResolveResult(parent, res);
	}

	private ArrayList<IGItem> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		ArrayList<IGItem> res = new ArrayList<IGItem>();

		String id = getId();

		/*
		 * Step 1/3 : resolve id, create hierarchical list of all potential matches
		 */

		IGResolveResult rr = aContainer.resolve(id);
		if (rr == null) {
			
			// is this actually a string literal?
			
			if (id.charAt(0) == '"' && getNumExtensions() == 0 && aTypeHint != null && aTypeHint.isArray()) {

				IGInterpreterRuntimeEnv env = aEE.getInterpreterEnv();
				ZDB zdb = aEE.getZDB();
				
				IGType it = aTypeHint.getIndexType();

				int l = id.length();
				
				String image = id.substring(1, l - 1);

				IGOperation ascending = it.getAscending(aContainer, getLocation());
				IGOperation left = it.getLeft(getLocation());
				IGOperation length = it.getDiscreteValue(image.length() - 1, getLocation(), aErrorMode, aReport);
				if (length == null) {
					return null;
				}
				IGOperation right;
				if (ascending instanceof IGStaticValue) {

					IGStaticValue sAscending = (IGStaticValue) ascending;
					if (sAscending.isTrue()) {
						right = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
					} else {
						right = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);
					}
					
				} else {
					IGOperation rightAsc = new IGOperationBinary(left, length, BinOp.ADD, it, getLocation(), zdb).optimize(env);
					IGOperation rightDesc = new IGOperationBinary(left, length, BinOp.SUB, it, getLocation(), zdb).optimize(env);

					right = new IGOperationPhi(ascending, rightAsc, rightDesc, it, getLocation(), zdb).optimize(env);
				}

				IGRange range = new IGRange(left, right, ascending, getLocation(), zdb);

				IGType type = aTypeHint.createSubtype(range, aEE.getInterpreterEnv(), getLocation());

				res.add(new IGOperationLiteral(image, type, getLocation()));

				return res;
			}

			reportError("Couldn't resolve " + getId(), this, aErrorMode, aReport);
			return res;
		}

		rr = createOperations(rr, aEE.getZDB());

		/*
		 * Step 2/3 : filter/modify the match tree through the extensions
		 */

		ErrorReport report = aReport != null ? aReport : new ErrorReport();

		rr = applyExtensions(rr, aContainer, aEE, aCache, report);

		/*
		 * Step 3/3 : create linear list
		 */

		while (rr != null) {
			int n = rr.getNumResults();
			for (int i = n - 1; i >= 0; i--) {

				IGItem match = rr.getResult(i);
				if (match == null) {
					continue;
				}
				res.add(match);
			}
			rr = rr.getParent();
		}

		return res;
	}

	private IGResolveResult applyExtensions(IGResolveResult aRR, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ErrorReport aReport) throws ZamiaException {

		int nExt = getNumExtensions();
		if (nExt == 0) {
			return aRR;
		}

		IGResolveResult parent = aRR.getParent();
		if (parent != null) {
			parent = applyExtensions(parent, aContainer, aEE, aCache, aReport);
		}

		int m = aRR.getNumResults();
		ArrayList<IGItem> items = new ArrayList<IGItem>(m);
		for (int j = 0; j < m; j++) {
			items.add(aRR.getResult(j));
		}

		SourceLocation prevLocation = getLocation();
		
		for (int iExt = 0; iExt < nExt; iExt++) {

			NameExtension ext = getExtension(iExt);

			m = items.size();
			ArrayList<IGItem> newItems = new ArrayList<IGItem>(m);
			for (int j = 0; j < m; j++) {
				ext.computeIG(items.get(j), prevLocation, aContainer, aEE, aCache, newItems, aReport);
				prevLocation = ext.getLocation();
			}

			items = newItems;
		}

		return new IGResolveResult(parent, items);
	}

	// convenience

	public IGType computeIGAsType(IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ArrayList<IGItem> items = computeIG(null, aContainer, aEE, aCache, aErrorMode, aReport);

		int n = items.size();
		for (int i = 0; i < n; i++) {

			IGItem item = items.get(i);

			if (item instanceof IGType) {
				return (IGType) item;
			}
		}

		reportError("Type expected here.", this, aErrorMode, aReport);
		return null;
	}

	public IGOperation computeIGAsOperation(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		ArrayList<IGItem> items = computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);

		ErrorReport report = aReport;
		if (aReport == null) {
			report = new ErrorReport();
		}

		IGOperation fallbackOp = null;

		int n = items.size();
		for (int i = 0; i < n; i++) {
			IGItem item = items.get(i);

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

		ArrayList<IGItem> items = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		int n = items.size();
		for (int i = 0; i < n; i++) {

			IGItem item = items.get(i);

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

		ArrayList<IGItem> items = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		int n = items.size();
		for (int i = 0; i < n; i++) {

			IGItem item = items.get(i);

			if (item instanceof IGSubProgram) {

				IGSubProgram sub = (IGSubProgram) item;

				IGContainer formalScope = sub.getContainer();

				ArrayList<IGObject> interfaces = formalScope.getInterfaces();
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

		ArrayList<IGItem> items = computeIG(null, aContainer, aEE, new IGOperationCache(), aErrorMode, report);

		int n = items.size();
		for (int i = 0; i < n; i++) {

			IGItem item = items.get(i);

			if (item instanceof IGSubProgram) {

				IGSubProgram sub = (IGSubProgram) item;

				IGContainer formalScope = sub.getContainer();

				ArrayList<IGObject> interfaces = formalScope.getInterfaces();
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