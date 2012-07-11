/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 4, 2008
 */
package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGResolveResult;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.OIDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class AssociationList extends VHDLNode {

	private ArrayList<AssociationElement> associations;

	public AssociationList(VHDLNode aParent, long aLineCol) {
		super(aParent, aLineCol);
		
		if (aParent != null) {
			setSource(aParent.getSource());
		}
		
		associations = new ArrayList<AssociationElement>(1);
	}

	public void add(AssociationElement ae_) {
		associations.add(ae_);
		ae_.setParent(this, true);
	}
	public AssociationElement add(VHDLNode parent_, long location_) {
		AssociationElement ae_ = new AssociationElement(parent_, location_);
		add(ae_);
		return ae_;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return associations.get(idx_);
	}

	@Override
	public int getNumChildren() {
		return associations.size();
	}

	public int getNumAssociations() {
		return associations.size();
	}

	public AssociationElement getAssociation(int idx_) {
		return associations.get(idx_);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("(");
		int n = associations.size();
		for (int i = 0; i < n; i++) {
			buf.append(associations.get(i));
			if (i < n - 1)
				buf.append(", ");
		}
		buf.append(")");
		return buf.toString();
	}

	public void dumpVHDL(int indent_, PrintStream out_) throws ZamiaException {
		printlnIndented(toString(), indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		int n = associations.size();
		for (int i = 0; i < n; i++) {
			AssociationElement association = associations.get(i);
			association.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	/**
	 * The purpose of this method is to bring together this InterfaceList with
	 * an actual list of parameters (an AssociationList). Since
	 * AssociationElements can be named, i.e. have an formal part which can be a
	 * full-blown Name more than one AssociationElement can apply to one
	 * interface, e.g
	 * 
	 * q(1) => a, q(0) => b
	 * 
	 * the result of this computation is a list of assignments.
	 * 
	 * @param aFormalCache
	 *            TODO
	 * @param aActualCache
	 *            TODO
	 * 
	 */

	public IGMappings map(IGContainer aFormalScope, IGElaborationEnv aFormalEE, IGOperationCache aFormalCache, IGContainer aActualScope, IGElaborationEnv aActualEE,
			IGOperationCache aActualCache, ArrayList<IGObject> interfaces, boolean ignoreDanglingOutputs_, ErrorReport aReport, boolean checkDirs_) throws ZamiaException {
		int pIdx = 0;
		int nInterfaces = interfaces.size();
		int nParams = getNumAssociations();

		IGMappings res = new IGMappings();

		// phase one: positional elements

		while (pIdx < nInterfaces) {
			IGObject interf = interfaces.get(pIdx);

			if (pIdx >= nParams) {
				if (pIdx < nInterfaces) {

					for (int i = pIdx; i < nInterfaces; i++) {

						IGObject intf = interfaces.get(i);
						if (intf.getInitialValue() != null) {
							continue;
						}
						if (ignoreDanglingOutputs_ && intf.getDirection() == OIDir.OUT) {
							continue;
						}

						aReport.append("Too few actual parameters", getLocation());
						res.setFailed(true);
						return res;
					}
				}
				break;
			}

			AssociationElement ae = getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal != null)
				break;

			Operation actual = ae.getActualPart();

			if (actual != null) {

				IGType formalType = interf.getType();

				IGMapping mapping = null;

				ErrorReport report = new ErrorReport();
				IGOperation actualObj = actual.computeIGOperation(formalType, aActualScope, aActualEE, aActualCache, ASTErrorMode.RETURN_NULL, report);
				if (actualObj == null) {
					aReport.append(report);
					return null;
				}

				IGType actualType = actualObj.getType();
				if (actualType == null) {
					aReport.append("Failed to compute type for " + actualObj, actual.getLocation());
					res.setFailed(true);
					pIdx++;
					continue;
				}

				int score = formalType.getAssignmentCompatibilityScore(actualType, ae.getLocation());
				if (score == 0) {
					aReport.append("Type mismatch in interface mapping: \n  formal=" + interf + ", \n  actual=" + actual + ", \n  formalType=" + formalType + ", \n  actualType="
							+ actualType, actual.getLocation());
					res.setFailed(true);
					pIdx++;
					continue;
				}

				if (checkDirs_) {
					OIDir formalDir = interf.getDirection();
					OIDir actualDir = actualObj.getDirection();
					if (!checkDir(formalDir, actualDir)) {
						aReport.append("Direction mismatch in positional mapping formal: " + formal + " formal dir: " + formalDir + " actual: " + actualObj + " actualDir: "
								+ actualDir, actual.getLocation());

						res.setFailed(true);
						pIdx++;
						continue;
					}
				}

				mapping = new IGMapping(new IGOperationObject(interf, interf.computeSourceLocation(), aActualEE.getZDB()), actualObj, actual.getLocation(), aFormalEE.getZDB());

				res.addMapping(mapping, score);

			} else {
				if (interf.getDirection() != OIDir.OUT || !ignoreDanglingOutputs_) {
					aReport.append("Missing actual for interface " + interf, getLocation());
					res.setFailed(true);
					pIdx++;
					continue;
				}
			}
			pIdx++;
		}

		if (pIdx < nParams) {
			AssociationElement ae = getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal == null) {
				aReport.append("Too many positional parameters.", getLocation());
				res.setFailed(true);
				return res;
			}
		}

		// phase two: named associations
		while (pIdx < nParams) {

			AssociationElement ae = getAssociation(pIdx);

			FormalPart formal = ae.getFormalPart();
			if (formal == null) {
				throw new ZamiaException("Illegal mix of named and positional parameters", ae.getLocation());
			}

			Operation actual = ae.getActualPart();

			ErrorReport report = new ErrorReport();
			Name formalName = formal.getName();

			IGResolveResult formalItems = formalName.computeIG(null, aFormalScope, aFormalEE, aFormalCache, ASTErrorMode.RETURN_NULL, report);
			if (formalItems == null || formalItems.isEmpty()) {
				aReport.append(report);
				aReport.append("Failed to compute object for interface name " + formalName, formal.getLocation());
				res.setFailed(true);
				pIdx++;
				continue;
			}

			boolean foundMatch = false;
			int nFormalItems = formalItems.getNumResults();
			
			for (int iFormalItem = 0; iFormalItem < nFormalItems; iFormalItem++) {
				IGItem formalItem = formalItems.getResult(iFormalItem);
				if (!(formalItem instanceof IGOperation)) {
					continue;
				}

				IGOperation formalOp = (IGOperation) formalItem;

				if (aActualEE != null) {

					if (actual != null) { // open ?

						IGType formalType = formalOp.getType();

						report = new ErrorReport();
						IGOperation actualObj = actual.computeIGOperation(formalType, aActualScope, aActualEE, aActualCache, ASTErrorMode.RETURN_NULL, report);
						if (actualObj == null) {
							aReport.append(report);
							aReport.append("Failed to compute actual item in named mapping: " + actual + ", formal "+(iFormalItem+1)+"/"+nFormalItems+" was: " + formal + " (item="+formalItem+")", actual.getLocation());
							continue;
						}

						IGType actualType = actualObj.getType();
						if (actualType == null) {
							aReport.append("Failed to actual compute type for" + actual, actual.getLocation());
							continue;
						}

						int score = formalType.getAssignmentCompatibilityScore(actualType, ae.getLocation());
						if (score == 0) {
							aReport.append("Type mismatch in named mapping: formal=" + formalOp + ", actual=" + actual + ", formalType=" + formalType + ", actualType="
									+ actualType, actual.getLocation());
							continue;
						}

						if (checkDirs_) {
							OIDir formalDir = formalOp.getDirection();
							OIDir actualDir = actualObj.getDirection();
							if (!checkDir(formalDir, actualDir)) {
								aReport.append("Direction mismatch in named mapping formal: " + formal + ", formal dir: " + formalDir + ", actual: " + actual + ", actualDir: "
										+ actualDir, actual.getLocation());
								continue;
							}
						}

						res.addMapping(new IGMapping(formalOp, actualObj, formal.getLocation(), aFormalEE.getZDB()), score);
						foundMatch = true;
						break;
					} else {
						foundMatch = true;
					} 
				} else {
					foundMatch = true;
				}
			}
			
			if (!foundMatch && actual != null) {
				
				SourceLocation location = actual.getLocation();
				
				aReport.append("Failed to map: formal="+formal+", actual="+actual, location);
				res.setFailed(true);
			}

			pIdx++;
		}

		// phase three: leftovers?

		if (pIdx < nParams) {
			aReport.append("Too many parameters.", getLocation());
			res.setFailed(true);
		}

		return res;
	}

	private boolean checkDir(OIDir aFormalDir, OIDir aActualDir) {

		switch (aFormalDir) {
		case IN:
			return aActualDir != OIDir.OUT;
		case OUT:
			return aActualDir != OIDir.IN;
		}

		return true;
	}
}
