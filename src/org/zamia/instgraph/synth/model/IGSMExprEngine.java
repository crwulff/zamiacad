/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 9, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.ArrayList;
import java.util.HashMap;

import jdd.bdd.BDD;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLType.TypeCat;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;
import org.zamia.util.HashMapArray;

/**
 * The expression engine - a singleton factory (not abstract since
 * there is only one implementation)
 * 
 * Lets the use create ExprNode trees easily and efficiently, yet will 
 * automatically seperate out all boolean expression parts and 
 * represent them as BDDs. Reason for that is that we need to answer
 * questions of tautology or contradiction efficiently and some
 * logic optimization is nice to have here as well.
 * 
 * All expression node trees produced by this factory are immutable.
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMExprEngine {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private final BDD fBDD;

	private final HashMap<RTLSignal, IGSMExprNode> fSignalMap = new HashMap<RTLSignal, IGSMExprNode>();

	private final HashMapArray<IGSMExprNode, Integer> fVarMap = new HashMapArray<IGSMExprNode, Integer>();

	private final HashMap<Integer, IGSMExprNode> fVarMapRev = new HashMap<Integer, IGSMExprNode>();

	private final ArrayList<IGSMExprNode> fVars = new ArrayList<IGSMExprNode>();

	private IGSMExprEngine() {
		fBDD = new BDD(5000, 5000);
	}

	IGSMExprNode levelToExpr(int aLevel) {
		return fVars.get(aLevel);
	}

	IGSMExprNode mapToExpr(int aBDDVar) {
		return fVarMapRev.get(aBDDVar);
	}

	int mapToBDDVar(IGSMExprNode aNode) {

		int res;

		if (!fVarMap.containsKey(aNode)) {
			res = fBDD.createVar();

			fVarMap.put(aNode, res);
			fVarMapRev.put(res, aNode);
			fVars.add(aNode);
		} else {
			res = fVarMap.get(aNode);
		}

		return res;
	}

	private static IGSMExprEngine fInstance = null;

	public static IGSMExprEngine getInstance() {

		if (fInstance == null) {
			fInstance = new IGSMExprEngine();
		}

		return fInstance;
	}

	private boolean isLogicOp(BinOp aOp) {

		switch (aOp) {
		case AND:
		case NAND:
		case OR:
		case NOR:
		case XOR:
		case XNOR:
			return true;
		}

		return false;
	}

	public IGSMExprNode binary(BinOp aOp, IGSMExprNode aA, IGSMExprNode aB, SourceLocation aLocation) throws ZamiaException {

		RTLType typeA = aA.getType();
		RTLType typeB = aB.getType();

		if (typeA.getCat() == TypeCat.BIT && typeB.getCat() == TypeCat.BIT) {

			if (isLogicOp(aOp)) {

				return new IGSMExprNodeBDD(aOp, aA, aB, aLocation, aA.getSynth());

			}

			// optimize "s='1'" => s, "s='0'" => not s

			if (aOp == BinOp.EQUAL) {

				RTLValue sv = aA.getStaticValue();
				IGSMExprNode op = aB;

				if (sv == null) {
					sv = aB.getStaticValue();
					op = aA;
				}

				if (sv != null) {

					switch (sv.getBit()) {
					case BV_0:
						return unary(UnaryOp.NOT, op, aLocation);

					case BV_1:
						return op;
					}
				}
			}
		}

		return new IGSMExprNodeBinary(aOp, aA, aB, aLocation, aA.getSynth());
	}

	public IGSMExprNode unary(UnaryOp aOp, IGSMExprNode aA, SourceLocation aLocation) throws ZamiaException {

		RTLType typeA = aA.getType();
		if (typeA.getCat() == TypeCat.BIT && (aOp == UnaryOp.NOT || aOp == UnaryOp.BUF)) {

			return new IGSMExprNodeBDD(aOp, aA, aLocation, aA.getSynth());

		}

		return new IGSMExprNodeUnary(aOp, aA, aLocation, aA.getSynth());
	}

	public IGSMExprNode literal(RTLValue aValue, IGSynth aSynth, SourceLocation aLocation) throws ZamiaException {

		RTLType type = aValue.getType();

		if (type.getCat() == TypeCat.BIT) {

			BitValue bit = aValue.getBit();

			switch (bit) {
			case BV_0:
				return new IGSMExprNodeBDD(false, type, aLocation, aSynth);
			case BV_1:
				return new IGSMExprNodeBDD(true, type, aLocation, aSynth);
			}
		}

		return new IGSMExprNodeValue(aValue, aLocation, aSynth);
	}

	public BDD getBDD() {
		return fBDD;
	}

	public IGSMExprNode signal(RTLSignal aS, IGSynth aSynth, SourceLocation aLocation) throws ZamiaException {

		IGSMExprNode node = fSignalMap.get(aS);

		if (node == null) {

			node = new IGSMExprNodeSignal(aS, aLocation, aSynth);
			
			node = convertToBDD(node, aSynth, aLocation);
			
			fSignalMap.put(aS, node);
		}

		return node;
	}

	public IGSMExprNode restrict(IGSMExprNode aNode, IGSMExprNode aCareFun, IGSynth aSynth, SourceLocation aLocation) {

		if (aNode instanceof IGSMExprNodeBDD && aCareFun instanceof IGSMExprNodeBDD) {

			IGSMExprNodeBDD bdd = (IGSMExprNodeBDD) aNode;
			IGSMExprNodeBDD careBDD = (IGSMExprNodeBDD) aCareFun;

			return new IGSMExprNodeBDD(bdd, careBDD, aLocation, aSynth);
		}

		return aNode;
	}

	// helper method that turns single-bit nodes safely into a BDD node
	public IGSMExprNode convertToBDD(IGSMExprNode aNode, IGSynth aSynth, SourceLocation aLocation) throws ZamiaException {

		if (aNode instanceof IGSMExprNodeBDD) {
			return aNode;
		}

		RTLType type = aNode.getType();
		if (type.getCat() != TypeCat.BIT) {
			return aNode;
		}

		return new IGSMExprNodeBDD(UnaryOp.BUF, aNode, aLocation, aSynth);

	}

}
