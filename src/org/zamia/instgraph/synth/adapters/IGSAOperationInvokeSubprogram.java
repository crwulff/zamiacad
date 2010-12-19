/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGMapping;
import org.zamia.instgraph.IGMappings;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationUnary;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationInvokeSubprogram extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {

		IGOperationInvokeSubprogram inv = (IGOperationInvokeSubprogram) aOperation;

		SourceLocation location = inv.computeSourceLocation();

		IGSubProgram sp = inv.getSub();

		// is this an operator on std types ?

		if (isStdOp(sp)) {

			IGContainer container = sp.getContainer();

			IGType t = sp.getReturnType();

			int ni = container.getNumInterfaces();
			int n = inv.getNumMappings();

			if (n != ni) {
				throw new ZamiaException("Not synthesizable.", location);
			}

			IGOperation opA = null;
			IGOperation opB = null;

			String ops = sp.getId();

			IGOperation op = null;

			switch (ni) {
			case 2:
				opA = inv.getMapping(0).getActual();
				opA = aSynth.getSynthAdapter(opA).inlineSubprograms(opA, aOR, aInlinedSOS, aSynth);

				opB = inv.getMapping(1).getActual();
				opB = aSynth.getSynthAdapter(opB).inlineSubprograms(opB, aOR, aInlinedSOS, aSynth);

				BinOp bop = null;
				if (ops.equals("\"+\"")) {
					bop = BinOp.ADD;
				} else if (ops.equals("\"AND\"")) {
					bop = BinOp.AND;
				} else if (ops.equals("\"/\"")) {
					bop = BinOp.DIV;
				} else if (ops.equals("\"=\"")) {
					bop = BinOp.EQUAL;
				} else if (ops.equals("\">\"")) {
					bop = BinOp.GREATER;
				} else if (ops.equals("\">=\"")) {
					bop = BinOp.GREATEREQ;
				} else if (ops.equals("\"<\"")) {
					bop = BinOp.LESS;
				} else if (ops.equals("\"<=\"")) {
					bop = BinOp.LESSEQ;
				} else if (ops.equals("\"=\"")) {
					bop = BinOp.MOD;
				} else if (ops.equals("\"MOD\"")) {
					bop = BinOp.MUL;
				} else if (ops.equals("\"NAND\"")) {
					bop = BinOp.NAND;
				} else if (ops.equals("\"/=\"")) {
					bop = BinOp.NEQUAL;
				} else if (ops.equals("\"NOR\"")) {
					bop = BinOp.NOR;
				} else if (ops.equals("\"OR\"")) {
					bop = BinOp.OR;
				} else if (ops.equals("\"**\"")) {
					bop = BinOp.POWER;
				} else if (ops.equals("\"REM\"")) {
					bop = BinOp.REM;
				} else if (ops.equals("\"ROL\"")) {
					bop = BinOp.ROL;
				} else if (ops.equals("\"ROR\"")) {
					bop = BinOp.ROR;
				} else if (ops.equals("\"SLA\"")) {
					bop = BinOp.SLA;
				} else if (ops.equals("\"SLL\"")) {
					bop = BinOp.SLL;
				} else if (ops.equals("\"SRA\"")) {
					bop = BinOp.SRA;
				} else if (ops.equals("\"SRL\"")) {
					bop = BinOp.SRL;
				} else if (ops.equals("\"-\"")) {
					bop = BinOp.SUB;
				} else if (ops.equals("\"XNOR\"")) {
					bop = BinOp.XNOR;
				} else if (ops.equals("\"XOR\"")) {
					bop = BinOp.XOR;
				} else if (ops.equals("\"&\"")) {
					bop = BinOp.CONCAT;
				}
				if (bop == null) {
					throw new ZamiaException("Not synthesizable.", location);
				}

				op = new IGOperationBinary(opA, opB, bop, t, location, aSynth.getZDB());
				break;
			case 1:
				opA = inv.getMapping(0).getActual();
				opA = aSynth.getSynthAdapter(opA).inlineSubprograms(opA, aOR, aInlinedSOS, aSynth);

				UnaryOp uop = null;
				if (ops.equals("\"ABS\"")) {
					uop = UnaryOp.ABS;
				} else if (ops.equals("\"NOT\"")) {
					uop = UnaryOp.NOT;
				} else if (ops.equals("\"-\"")) {
					uop = UnaryOp.NEG;
				} else if (ops.equals("\"+\"")) {
					uop = UnaryOp.BUF;
				}
				if (uop == null) {
					throw new ZamiaException("Not synthesizable.", location);
				}

				op = new IGOperationUnary(opA, uop, t, location, aSynth.getZDB());
				break;
			default:
				throw new ZamiaException("Not synthesizable.", location);
			}

			return op;
		} else {

			// FIXME: implement inlining (macro-expansion) of subprograms
			IGMappings mappings2 = new IGMappings();

			int n = inv.getNumMappings();
			for (int i = 0; i < n; i++) {

				IGMapping mapping = inv.getMapping(i);

				IGOperation formal = mapping.getFormal();
				IGOperation actual = mapping.getActual();

				IGOperation formal2 = aSynth.getSynthAdapter(formal).inlineSubprograms(formal, aOR, aInlinedSOS, aSynth);
				IGOperation actual2 = aSynth.getSynthAdapter(actual).inlineSubprograms(actual, aOR, aInlinedSOS, aSynth);

				IGMapping mapping2 = new IGMapping(formal2, actual2, mapping.computeSourceLocation(), aSynth.getZDB());

				mappings2.addMapping(mapping2, 0);
			}

			//			IGOperationInvokeSubprogram inv2 = new IGOperationInvokeSubprogram(mappings2, sp, inv.computeSourceLocation(), aSynth.getZDB());
			//			return inv2;
		}
		throw new ZamiaException("Sorry, not implemented yet.");

	}

	private boolean isStdOp(IGSubProgram aSp) {

		SourceLocation source = aSp.computeSourceLocation();

		String uri = source.fSF.getURI();
		if (uri == null) {
			return false;
		}

		String id = aSp.getId();
		if (id.charAt(0) == '"') {
			return true;
		}

		return false;
	}

}
