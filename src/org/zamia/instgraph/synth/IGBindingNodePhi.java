/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 27, 2010
 */
package org.zamia.instgraph.synth;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.model.IGSMExprEngine;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGBindingNodePhi extends IGBindingNode {

	private final IGSMExprNode fCond;

	private final IGBindingNode fThenNode, fElseNode;

	public IGBindingNodePhi(IGSMExprNode aCond, IGBindingNode aThenNode, IGBindingNode aElseNode, SourceLocation aLocation) {
		super(aLocation);
		fCond = aCond;
		fThenNode = aThenNode;
		fElseNode = aElseNode;
	}

	public IGSMExprNode getCond() {
		return fCond;
	}

	@Override
	public String toString() {
		return "Φ(" + fCond + "," + fThenNode + "," + fElseNode + ")";
	}

	@Override
	public void dump(int aI) {
		logger.debug(aI, "IGBindingPhi cond=%s", fCond);
		logger.debug(aI + 1, "then=");
		if (fThenNode != null) {
			fThenNode.dump(aI + 2);
		} else {
			logger.debug(aI + 2, "Omega");
		}
		logger.debug(aI + 1, "else=");
		if (fElseNode != null) {
			fElseNode.dump(aI + 2);
		} else {
			logger.debug(aI + 2, "Omega");
		}
	}

	@Override
	public IGBindingNode replaceOmega(IGBindingNode aNode) throws ZamiaException {

		IGBindingNode thenNode = fThenNode;
		IGBindingNode elseNode = fElseNode;

		if (thenNode == null) {
			thenNode = aNode;
		} else {
			thenNode = thenNode.replaceOmega(aNode);
		}

		if (elseNode == null) {
			elseNode = aNode;
		} else {
			elseNode = elseNode.replaceOmega(aNode);
		}

		return new IGBindingNodePhi(fCond, thenNode, elseNode, fLocation);
	}

	@Override
	public IGSMExprNode computeCombinedEnable(IGSynth aSynth) throws ZamiaException {

		IGSMExprEngine ee = IGSMExprEngine.getInstance();

		IGSMExprNode a = fThenNode != null ? ee.binary(BinOp.AND, fCond, fThenNode.computeCombinedEnable(aSynth), fLocation) : ee.literal(aSynth.getBitValue(BitValue.BV_0),
				aSynth, fLocation);
		IGSMExprNode b = fElseNode != null ? ee.binary(BinOp.AND, ee.unary(UnaryOp.NOT, fCond, fLocation), fElseNode.computeCombinedEnable(aSynth), fLocation) : ee.literal(
				aSynth.getBitValue(BitValue.BV_0), aSynth, fLocation);

		return ee.binary(BinOp.OR, a, b, fLocation);
	}

	@Override
	public RTLSignal synthesizeASyncData(IGSMExprNode aAE, RTLSignal aClk, IGSynth aSynth) throws ZamiaException {

		if (fElseNode == null) {
			return fThenNode.synthesizeASyncData(aAE, aClk, aSynth);
		} else if (fThenNode == null) {
			return fElseNode.synthesizeASyncData(aAE, aClk, aSynth);
		}

		IGSMExprNode s = fCond.replaceClockEdge(aClk, aSynth.getBitValue(BitValue.BV_0), aSynth);

		logger.debug("IGBindingsNodePhi: Select signal for %s is %s", this, s);

		IGSMExprEngine ee = aSynth.getEE();

		s = ee.restrict(s, aAE, aSynth, fLocation);

		logger.debug("IGBindingsNodePhi: Restricted select signal for %s is %s", this, s);

		RTLValue sv = s.getStaticValue();

		if (sv == null) {

			return aSynth.placeMUX(s.synthesize(aSynth), fThenNode.synthesizeASyncData(aAE, aClk, aSynth), fElseNode.synthesizeASyncData(aAE, aClk, aSynth), fLocation);

		}

		if (sv.getBit() == BitValue.BV_0) {
			return fElseNode.synthesizeASyncData(aAE, aClk, aSynth);
		}
		return fThenNode.synthesizeASyncData(aAE, aClk, aSynth);
	}

	@Override
	public RTLSignal synthesizeSyncData(IGSMExprNode aAEn, RTLSignal aClk, IGSynth aSynth) throws ZamiaException {

		if (fElseNode == null) {
			return fThenNode.synthesizeSyncData(aAEn, aClk, aSynth);
		} else if (fThenNode == null) {
			return fElseNode.synthesizeSyncData(aAEn, aClk, aSynth);
		}

		IGSMExprNode s = fCond.replaceClockEdge(aClk, aSynth.getBitValue(BitValue.BV_1), aSynth);

		logger.debug("IGBindingsNodePhi: Select signal for %s is %s", this, s);

		IGSMExprEngine ee = aSynth.getEE();

		s = ee.restrict(s, aAEn, aSynth, fLocation);

		logger.debug("IGBindingsNodePhi: Restricted select signal for %s is %s", this, s);

		RTLValue sv = s.getStaticValue();

		if (sv == null) {

			return aSynth.placeMUX(s.synthesize(aSynth), fThenNode.synthesizeSyncData(aAEn, aClk, aSynth), fElseNode.synthesizeSyncData(aAEn, aClk, aSynth), fLocation);

		}

		if (sv.getBit() == BitValue.BV_0) {
			return fElseNode.synthesizeSyncData(aAEn, aClk, aSynth);
		}
		return fThenNode.synthesizeSyncData(aAEn, aClk, aSynth);
	}

}
