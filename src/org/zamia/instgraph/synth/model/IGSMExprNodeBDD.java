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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdd.bdd.BDD;
import jdd.bdd.NodeTable;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLType;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLValue.BitValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class IGSMExprNodeBDD extends IGSMExprNode {

	private HashSet<Integer> fInputVars = new HashSet<Integer>();

	private int fResVar;

	public IGSMExprNodeBDD(BinOp aOp, IGSMExprNode aA, IGSMExprNode aB, SourceLocation aLocation, IGSynth aSynth) throws ZamiaException {
		super(aA.getType(), aLocation, aSynth);

		BDD bdd = ee.getBDD();

		int a = -1, b = -1;

		if (aA instanceof IGSMExprNodeBDD) {
			IGSMExprNodeBDD node = (IGSMExprNodeBDD) aA;
			a = node.getResVar();

			HashSet<Integer> inputs = node.getInputVars();

			for (Integer v : inputs) {
				fInputVars.add(v);
			}

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
				fInputVars.add(a);
			}
		}

		if (aB instanceof IGSMExprNodeBDD) {
			IGSMExprNodeBDD node = (IGSMExprNodeBDD) aB;
			b = node.getResVar();

			HashSet<Integer> inputs = node.getInputVars();
			for (Integer v : inputs) {
				fInputVars.add(v);
			}

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
				fInputVars.add(b);
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

	// restrict
	public IGSMExprNodeBDD(IGSMExprNodeBDD aBDD, IGSMExprNodeBDD aCareBDD, SourceLocation aLocation, IGSynth aSynth) {
		super(aBDD.getType(), aLocation, aSynth);

		BDD bdd = ee.getBDD();

		HashSet<Integer> inputs = aBDD.getInputVars();

		for (Integer v : inputs) {
			fInputVars.add(v);
		}

		fResVar = bdd.simplify(aCareBDD.getResVar(), aBDD.getResVar());
	}

	private HashSet<Integer> getInputVars() {
		return fInputVars;
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

			HashSet<Integer> inputs = node.getInputVars();
			for (Integer v : inputs) {
				fInputVars.add(v);
			}

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
				fInputVars.add(a);
			}
		}

		switch (aOp) {
		case NOT:
			fResVar = bdd.not(a);
			break;
		case BUF:
			fResVar = a;
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

		printSet(buf, fResVar);

		return buf.toString();
	}

	private void printSet(StringBuilder buf, int bdd) {

		BDD nt = ee.getBDD();
		int max = nt.numberOfVariables();

		if (bdd < 2) {
			buf.append(bdd == 0 ? "FALSE" : "TRUE");
		} else {

			//			if(BDDPrinter.set_chars == null || BDDPrinter.set_chars.length < max)
			//				BDDPrinter.set_chars = Allocator.allocateCharArray(max);
			//			BDDPrinter.set_chars_len = max;
			//			BDDPrinter.nt = nt;
			//			BDDPrinter.nn = nn;

			char cube[] = new char[max];

			ArrayList<String> cubes = new ArrayList<String>();

			computeCubes(cubes, bdd, 0, max, nt, cube);

			int n = cubes.size();
			for (int i = 0; i < n; i++) {
				String cb = cubes.get(i);

				if (i > 0) {
					buf.append(" ∨ ");
				}

				buf.append("(");

				boolean first = true;
				for (int j = 0; j < max; j++) {
					char c = cb.charAt(j);

					switch (c) {
					case '0':
						if (first) {
							first = false;
						} else {
							buf.append('∧');
						}
						IGSMExprNode expr = ee.levelToExpr(j);
						buf.append("(¬" + expr + ")");
						break;
					case '1':
						if (first) {
							first = false;
						} else {
							buf.append('∧');
						}
						expr = ee.levelToExpr(j);
						buf.append("(" + expr + ")");
						break;
					default:
					}
				}

				buf.append(")");
			}
		}
	}

	private void computeCubes(List<String> buf, int bdd, int level, int max, NodeTable nt, char cube[]) {

		if (level == max) {

			buf.add(new String(cube));
			return;
		}

		int var = nt.getVar(bdd);
		if (var > level || bdd == 1) {
			cube[level] = '-';
			computeCubes(buf, bdd, level + 1, max, nt, cube);
			return;
		}

		int low = nt.getLow(bdd);
		int high = nt.getHigh(bdd);

		if (low != 0) {
			cube[level] = '0';
			computeCubes(buf, low, level + 1, max, nt, cube);
		}

		if (high != 0) {
			cube[level] = '1';
			computeCubes(buf, high, level + 1, max, nt, cube);
		}
	}

	public void printCubes() {
		ee.getBDD().printSet(fResVar);
	}

	@Override
	public IGSMExprNode replaceClockEdge(RTLSignal aClockSignal, RTLValue aValue, IGSynth aSynth) throws ZamiaException {

		BDD nt = ee.getBDD();
		int max = nt.numberOfVariables();

		int bdd = fResVar;

		if (bdd < 2) {

			return this;

		} else {

			// FIXME: this is horribly inefficient

			IGSMExprNode res = null;

			SourceLocation l = getLocation();

			char cube[] = new char[max];

			ArrayList<String> cubes = new ArrayList<String>();

			computeCubes(cubes, bdd, 0, max, nt, cube);

			int n = cubes.size();
			for (int i = 0; i < n; i++) {
				String cb = cubes.get(i);

				IGSMExprNode nc = null;

				for (int j = 0; j < max; j++) {
					char c = cb.charAt(j);

					switch (c) {
					case '0':
						IGSMExprNode expr = ee.levelToExpr(j).replaceClockEdge(aClockSignal, aValue, aSynth);

						IGSMExprNode e = ee.unary(UnaryOp.NOT, expr, l);
						if (nc == null) {
							nc = ee.convertToBDD(e, aSynth, l);
						} else {
							nc = ee.binary(BinOp.AND, nc, e, l);
						}
						break;
					case '1':

						expr = ee.levelToExpr(j).replaceClockEdge(aClockSignal, aValue, aSynth);

						if (nc == null) {
							nc = ee.convertToBDD(expr, aSynth, l);
						} else {
							nc = ee.binary(BinOp.AND, nc, expr, l);
						}
						break;
					default:
					}
				}

				if (res != null) {
					res = ee.binary(BinOp.OR, res, nc, l);
				} else {
					res = nc;
				}
			}

			return res;
		}
	}

	@Override
	public void findClockEdges(Set<IGSMExprNodeClockEdge> aClockEdges) throws ZamiaException {

		for (Integer v : fInputVars) {
			IGSMExprNode node = ee.mapToExpr(v);
			node.findClockEdges(aClockEdges);
		}

	}

	public void dumpBDD() {

		BDD nt = ee.getBDD();

		nt.printCubes(fResVar);
		nt.printSet(fResVar);

		ArrayList<String> cubes = new ArrayList<String>();
		int max = nt.numberOfVariables();
		char cube[] = new char[max];

		computeCubes(cubes, fResVar, 0, max, nt, cube);

		int n = cubes.size();
		for (int i = 0; i < n; i++) {
			System.out.println("CUBE: " + cubes.get(i));
		}

		for (int i = 0; i < max; i++) {
			System.out.printf("VAR %03d : %s\n", i, ee.levelToExpr(i));
		}

	}

	@Override
	public RTLSignal synthesize(IGSynth aSynth) throws ZamiaException {
		BDD nt = ee.getBDD();
		int max = nt.numberOfVariables();

		int bdd = fResVar;

		if (bdd < 2) {

			return aSynth.placeLiteral(getStaticValue(), fLocation);

		} else {

			RTLSignal res = null;

			SourceLocation l = getLocation();

			char cube[] = new char[max];

			ArrayList<String> cubes = new ArrayList<String>();

			computeCubes(cubes, bdd, 0, max, nt, cube);

			int n = cubes.size();
			for (int i = 0; i < n; i++) {
				String cb = cubes.get(i);

				RTLSignal sc = null;

				for (int j = 0; j < max; j++) {
					char c = cb.charAt(j);

					switch (c) {
					case '0':
						IGSMExprNode expr = ee.levelToExpr(j);

						RTLSignal sexpr = expr.synthesize(aSynth);
						
						RTLSignal se = aSynth.placeUnary(UnaryOp.NOT, sexpr, l);
						if (sc == null) {
							sc = se;
						} else {
							
							sc = aSynth.placeBinary(BinOp.AND, sc, se, l);
						}
						break;
						
					case '1':

						expr = ee.levelToExpr(j);

						if (sc == null) {
							sc = expr.synthesize(aSynth);
						} else {
							sc = aSynth.placeBinary(BinOp.AND, sc, expr.synthesize(aSynth), l);
						}
						break;
					default:
					}
				}

				if (res != null) {
					res = aSynth.placeBinary(BinOp.OR, res, sc, l);
				} else {
					res = sc;
				}
			}

			return res;
		}
	}

}
