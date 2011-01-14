/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 9, 2011
 */
package org.zamia.instgraph.synth.model;

import jdd.bdd.BDD;
import jdd.bdd.NodeTable;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class IGSMExprNodeBDD extends IGSMExprNode {

	private static final IGSMExprEngine ee = IGSMExprEngine.getInstance();

	private int fResVar;

	public IGSMExprNodeBDD(BinOp aOp, IGSMExprNode aA, IGSMExprNode aB, SourceLocation aLocation, IGSynth aSynth) throws ZamiaException {
		super(aA.getType(), aLocation, aSynth);

		BDD bdd = ee.getBDD();

		int a = -1, b = -1;

		if (aA instanceof IGSMExprNodeBDD) {
			IGSMExprNodeBDD node = (IGSMExprNodeBDD) aA;
			a = node.getResVar();
		} else {

			RTLValue sv = aA.getStaticValue();

			if (sv != null) {
				switch (sv.getBit()) {
				case BV_0:
					a = bdd.getZero();
					break;
				case BV_1:
					a = bdd.getOne();
					break;
				}
			}
			if (a < 0) {
				a = ee.mapToBDDVar(aA);
			}
		}

		if (aB instanceof IGSMExprNodeBDD) {
			IGSMExprNodeBDD node = (IGSMExprNodeBDD) aB;
			b = node.getResVar();
		} else {

			RTLValue sv = aB.getStaticValue();

			if (sv != null) {
				switch (sv.getBit()) {
				case BV_0:
					b = bdd.getZero();
					break;
				case BV_1:
					b = bdd.getOne();
					break;
				}
			}
			if (b < 0) {
				b = ee.mapToBDDVar(aB);
			}
		}

		switch (aOp) {
		case AND:
			fResVar = bdd.and(a, b);
			break;
		case OR:
			fResVar = bdd.or(a, b);
			break;
		case NAND:
			fResVar = bdd.nand(a, b);
			break;
		case NOR:
			fResVar = bdd.nor(a, b);
			break;
		case XOR:
			fResVar = bdd.xor(a, b);
			break;
		case XNOR:
			fResVar = bdd.not(bdd.xor(a, b));
			break;
		default:
			throw new ZamiaException("Internal error: unsupported BDD operation");
		}
	}

	public IGSMExprNodeBDD(boolean aB, RTLType aType, SourceLocation aLocation, IGSynth aSynth) {
		super(aType, aLocation, aSynth);

		BDD bdd = ee.getBDD();

		fResVar = aB ? bdd.getOne() : bdd.getZero();

	}

	public IGSMExprNodeBDD(UnaryOp aOp, IGSMExprNode aA, SourceLocation aLocation, IGSynth aSynth) throws ZamiaException {
		super(aA.getType(), aLocation, aSynth);

		BDD bdd = ee.getBDD();

		int a = -1;

		if (aA instanceof IGSMExprNodeBDD) {
			IGSMExprNodeBDD node = (IGSMExprNodeBDD) aA;
			a = node.getResVar();
		} else {

			RTLValue sv = aA.getStaticValue();

			if (sv != null) {
				switch (sv.getBit()) {
				case BV_0:
					a = bdd.getZero();
					break;
				case BV_1:
					a = bdd.getOne();
					break;
				}
			}
			if (a < 0) {
				a = ee.mapToBDDVar(aA);
			}
		}

		switch (aOp) {
		case NOT:
			fResVar = bdd.not(a);
			break;
		default:
			throw new ZamiaException("Internal error: unsupported BDD operation");
		}
	}

	@Override
	public RTLValue getStaticValue() {
		BDD bdd = ee.getBDD();

		if (fResVar == bdd.getOne()) {
			return fSynth.getBitValue(BitValue.BV_1);
		} else if (fResVar == bdd.getZero()) {
			return fSynth.getBitValue(BitValue.BV_0);
		}
		return null;
	}

	private int getResVar() {
		return fResVar;
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();

		BDD bdd = ee.getBDD();

		printSet(buf, fResVar, bdd.numberOfVariables(), bdd);

		return buf.toString();
	}

	private void printSet(StringBuilder buf, int bdd, int max, NodeTable nt) {
		if (bdd < 2) {
			buf.append(bdd == 0 ? "FALSE" : "TRUE");
		} else {

			//			if(BDDPrinter.set_chars == null || BDDPrinter.set_chars.length < max)
			//				BDDPrinter.set_chars = Allocator.allocateCharArray(max);
			//			BDDPrinter.set_chars_len = max;
			//			BDDPrinter.nt = nt;
			//			BDDPrinter.nn = nn;

			char cube[] = new char[max];

			printSet_rec(buf, bdd, 0, max, nt, cube);
		}
	}

	private static void printSet_rec(StringBuilder buf, int bdd, int level, int max, NodeTable nt, char cube[]) {

		if (level == max) {

			boolean first = true;

			for (int i = 0; i < max; i++) {
				char c = cube[i];

				switch (c) {
				case '0':
					if (first) {
						first = false;
					} else {
						buf.append('∧');
					}
					IGSMExprNode expr = ee.levelToExpr(i);
					buf.append("(¬"+expr+")");
					break;
				case '1':
					if (first) {
						first = false;
					} else {
						buf.append('∧');
					}
					expr = ee.levelToExpr(i);
					buf.append("("+expr+")");
					break;
				default:
				}

				//buf.append(c);
			}
			buf.append(" ∨ ");
			return;
		}

		int var = nt.getVar(bdd);
		if (var > level || bdd == 1) {
			cube[level] = '-';
			printSet_rec(buf, bdd, level + 1, max, nt, cube);
			return;
		}

		int low = nt.getLow(bdd);
		int high = nt.getHigh(bdd);

		if (low != 0) {
			cube[level] = '0';
			printSet_rec(buf, low, level + 1, max, nt, cube);
		}

		if (high != 0) {
			cube[level] = '1';
			printSet_rec(buf, high, level + 1, max, nt, cube);
		}
	}

	public void printCubes() {
		ee.getBDD().printSet(fResVar);
	}

}
