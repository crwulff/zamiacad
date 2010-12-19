/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGOperationSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.nodes.RTLNBinaryOp.BinaryOp;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSAOperationBinary extends IGOperationSynthAdapter {

	@Override
	public IGOperation inlineSubprograms(IGOperation aOperation, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, IGSynth aSynth) throws ZamiaException {
		IGOperationBinary obin = (IGOperationBinary) aOperation;

		IGOperation a = obin.getA();
		IGOperation b = obin.getB();

		IGOperation a2 = aSynth.getSynthAdapter(a).inlineSubprograms(a, aOR, aInlinedSOS, aSynth);
		IGOperation b2 = aSynth.getSynthAdapter(b).inlineSubprograms(b, aOR, aInlinedSOS, aSynth);

		return new IGOperationBinary(a2, b2, obin.getBinOp(), obin.getType(), obin.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public IGOperation resolveVariables(IGOperation aOperation, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock, IGObjectRemapping aOR, IGSynth aSynth)
			throws ZamiaException {
		IGOperationBinary obin = (IGOperationBinary) aOperation;

		IGOperation a = obin.getA();
		IGOperation b = obin.getB();

		IGOperation a2 = aSynth.getSynthAdapter(a).resolveVariables(a, aBindings, aResolvedSOS, aClock, aOR, aSynth);
		IGOperation b2 = aSynth.getSynthAdapter(b).resolveVariables(b, aBindings, aResolvedSOS, aClock, aOR, aSynth);

		return new IGOperationBinary(a2, b2, obin.getBinOp(), obin.getType(), obin.computeSourceLocation(), aSynth.getZDB());
	}

	@Override
	public RTLSignal synthesizeValue(IGOperation aOperation, IGSynth aSynth) throws ZamiaException {
		IGOperationBinary obin = (IGOperationBinary) aOperation;

		IGOperation a = obin.getA();
		IGOperation b = obin.getB();

		RTLSignal sa = aSynth.getSynthAdapter(a).synthesizeValue(a, aSynth);
		RTLSignal sb = aSynth.getSynthAdapter(b).synthesizeValue(b, aSynth);

		RTLModule module = aSynth.getRTLModule();

		BinaryOp op = null;

		switch (obin.getBinOp()) {
		case AND:
			op = BinaryOp.AND;
			break;
		case OR:
			op = BinaryOp.OR;
			break;
		case XOR:
			op = BinaryOp.XOR;
			break;
		case NAND:
			op = BinaryOp.NAND;
			break;
		case NOR:
			op = BinaryOp.NOR;
			break;
		case XNOR:
			op = BinaryOp.XNOR;
			break;
		case ADD:
			op = BinaryOp.ADD;
			break;
		case SUB:
			op = BinaryOp.SUB;
			break;
		case MUL:
			op = BinaryOp.MUL;
			break;
		case DIV:
			op = BinaryOp.DIV;
			break;
		case POWER:
			op = BinaryOp.POWER;
			break;
		case MOD:
			op = BinaryOp.MOD;
			break;
		case CONCAT:
			op = BinaryOp.CONCAT;
			break;
		case EQUAL:
			op = BinaryOp.EQUAL;
			break;
		case GREATER:
			op = BinaryOp.GREATER;
			break;
		case GREATEREQ:
			op = BinaryOp.GREATEREQ;
			break;
		case LESS:
			op = BinaryOp.LESS;
			break;
		case LESSEQ:
			op = BinaryOp.LESSEQ;
			break;
		case NEQUAL:
			op = BinaryOp.NEQUAL;
			break;
		case MAX:
			op = BinaryOp.MAX;
			break;
		case MIN:
			op = BinaryOp.MIN;
			break;
		case ROL:
			op = BinaryOp.ROL;
			break;
		case ROR:
			op = BinaryOp.ROR;
			break;
		case SLA:
			op = BinaryOp.SLA;
			break;
		case SRA:
			op = BinaryOp.SRA;
			break;
		case SLL:
			op = BinaryOp.SLL;
			break;
		case SRL:
			op = BinaryOp.SRL;
			break;
		case REM:
			op = BinaryOp.REM;
			break;
		}

		return module.createComponentBinary(op, sa, sb, obin.computeSourceLocation());
	}

}
