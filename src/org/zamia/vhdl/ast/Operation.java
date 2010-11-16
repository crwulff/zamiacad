/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class Operation extends VHDLNode {

	public Operation(VHDLNode aParent, long aLocation) {
		super(aParent, aLocation);
	}

	public final ArrayList<IGOperation> computeIG(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode,
			ErrorReport aReport) throws ZamiaException {

		if (aCache == null) {
			return computeIGP(aTypeHint, aContainer, aEE, null, aErrorMode, aReport);
		}

		ArrayList<IGOperation> res = aCache.getIGOperation(this, aTypeHint);
		if (res != null)
			return res;

		res = computeIGP(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		aCache.setIGOperation(this, aTypeHint, res);
		return res;
	}

	protected abstract ArrayList<IGOperation> computeIGP(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode,
			ErrorReport aReport) throws ZamiaException;

	public abstract String toVHDL();

	// to be overridden
	public void dump(PrintStream out, int i) {
		printSpaces(out, i);
		out.println(this);
	}

	/*
	 *  convenience functions
	 */

	/**
	 * Convenience function - if we want exactly one result and know it's type,
	 * this function will filter the results from the general computeIG() method
	 * by using the typeHint and checking for type assignment compatibility.
	 * 
	 * @param aTypeHint
	 * @param aContainer
	 * @param aEE
	 * @param aCache
	 *            TODO
	 * @param aErrorMode
	 * @param aReport
	 * @return
	 * @throws ZamiaException
	 */
	public final IGOperation computeIGOperation(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode,
			ErrorReport aReport) throws ZamiaException {

		ErrorReport report = aReport;
		if (report == null) {
			report = new ErrorReport();
		}

		ArrayList<IGOperation> res = computeIG(aTypeHint, aContainer, aEE, aCache, aErrorMode, report);

		int n = res != null ? res.size() : 0;
		for (int i = 0; i < n; i++) {

			IGOperation op = res.get(i);

			IGType t = op.getType();
			if (aTypeHint == null || t.isAssignmentCompatible(aTypeHint, getLocation())) {
				return op;
			} else {
				if (QUICK_ERROR_REPORTING) {
					report.append("Type mismatch in expression.", getLocation());
				} else {
					report.append("Type mismatch in expression.\n  left : " + t + "\n  right: " + aTypeHint, getLocation());
				}
			}
		}

		if (n == 0) {
			reportError("Expression expected here.", aErrorMode, report);
		} else {
			reportError("Type mismatch.", aErrorMode, report);
		}
		return null;
	}

	public final IGType computeType(IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport)
			throws ZamiaException {

		IGOperation op = computeIGOperation(aTypeHint, aContainer, aEE, aCache, aErrorMode, aReport);
		if (op == null) {
			return null;
		}
		return op.getType();
	}

	protected IGOperationInvokeSubprogram generateOperatorInvocation(String aOpId, Operation aA, Operation aB, IGType aTypeHint, IGContainer aContainer, IGElaborationEnv aEE,
			IGOperationCache aCache, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		AssociationList al = new AssociationList(this, getLineCol());
		AssociationElement ae = new AssociationElement(this, getLineCol());
		ae.setActualPart(aA);
		al.add(ae);
		if (aB != null) {
			ae = new AssociationElement(this, getLineCol());
			ae.setActualPart(aB);
			al.add(ae);
		}

		ErrorReport report = aReport;
		if (report == null) {
			report = new ErrorReport();
		}

		IGResolveResult rr = aContainer.resolve(aOpId);
		IGOperationInvokeSubprogram inv = null;
		int n = rr.getNumResults();
		for (int i = 0; i < n; i++) {

			IGItem item = rr.getResult(i);
			if (!(item instanceof IGSubProgram)) {
				continue;
			}

			IGSubProgram sp = (IGSubProgram) item;

			IGType rt = sp.getReturnType();
			if (rt == null) {
				//report.append("SubProgram is a procedure, not a function.", getLocation());
				continue;
			}
			if (aTypeHint != null && !rt.isAssignmentCompatible(aTypeHint, getLocation())) {
				//if (QUICK_ERROR_REPORTING) {
				//	report.append("Wrong return type", getLocation());
				//} else {
				//	report.append("Wrong return type: " + sp + " from " + sp.computeSourceLocation() + "\n  expected=" + aTypeHint + "\n  actual=" + rt, getLocation());
				//}
				continue;
			}

			IGOperationInvokeSubprogram invocation = sp.generateInvocation(al, aContainer, aEE, aCache, getLocation(), report);
			if (invocation != null) {
				if (inv != null) {
					int s1 = inv.getScore();
					int s2 = invocation.getScore();
					//						if (s1 == s2) {
					//							reportError("Operator " + opId + " is ambigous here:\n" + sp + " from " + sp.computeSourceLocation() + " matches as well as\n" + inv.getSub()
					//									+ " from " + inv.getSub().computeSourceLocation(), this, aErrorMode, report);
					//							return null;
					//						} else 
					if (s2 > s1) {
						inv = invocation;
					}
				} else {
					inv = invocation;
				}
			}
		}

		if (inv == null) {

			n = report.getNumEntries();
			if (n > 0) {
				String msg = report.toString();
				reportError(msg, this, aErrorMode, report);
			} else {
				reportError("Operator " + aOpId + " couldn't be resolved here", this, aErrorMode, report);
			}
			return null;
		}
		return inv;
	}

}