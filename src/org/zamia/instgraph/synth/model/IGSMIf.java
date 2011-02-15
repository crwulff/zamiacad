/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.synth.IGBinding;
import org.zamia.instgraph.synth.IGBindingNode;
import org.zamia.instgraph.synth.IGBindingNodePhi;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtl.RTLSignal;
import org.zamia.util.HashSetArray;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMIf extends IGSMStatement {

	private IGSMExprNode fCond;

	private IGSMSequenceOfStatements fThenStmt;

	private IGSMSequenceOfStatements fElseStmt;

	public IGSMIf(IGSMExprNode aCond, IGSMSequenceOfStatements aThenStmt, IGSMSequenceOfStatements aElseStmt, String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super(aLabel, aLocation, aSynth);
		fCond = aCond;
		fThenStmt = aThenStmt;
		fElseStmt = aElseStmt;
	}

	@Override
	public void dump(int aIndent) {

		logger.debug(aIndent, "if %s then", fCond);
		fThenStmt.dump(aIndent + 2);
		logger.debug(aIndent, "else");
		fElseStmt.dump(aIndent + 2);
		logger.debug(aIndent, "end if");

	}

	private HashSetArray<RTLSignal> computeDrivenSignals(IGBindings aBindingsA, IGBindings aBindingsB) {
		HashSetArray<RTLSignal> signals = new HashSetArray<RTLSignal>();

		int n = aBindingsA.getNumBindings();
		for (int i = 0; i < n; i++) {
			IGBinding binding = aBindingsA.getBinding(i);
			signals.add(binding.getTarget());
		}

		if (aBindingsB != null) {
			n = aBindingsB.getNumBindings();
			for (int i = 0; i < n; i++) {
				IGBinding binding = aBindingsB.getBinding(i);
				signals.add(binding.getTarget());
			}
		}

		return signals;
	}

	@Override
	public IGBindings computeBindings(IGBindings aBindingsBefore, IGSynth aSynth) throws ZamiaException {

		logger.debug("IGSMIf: computeBindings() cond=" + fCond + " fThenStmt=" + fThenStmt + " fElseStmt=" + fElseStmt);

		// compute bindings for both then and else branch

		IGBindings thenBindings = fThenStmt.computeBindings(aBindingsBefore, aSynth);
		IGBindings elseBindings = fElseStmt.computeBindings(aBindingsBefore, aSynth);

		// compute set of all driven Signals

		HashSetArray<RTLSignal> driven = computeDrivenSignals(thenBindings, elseBindings);

		// now produce resulting new bindings for all
		// signals and variables in this set

		IGBindings newBindings = new IGBindings();

		int num = driven.size();
		for (int i = 0; i < num; i++) {
			RTLSignal signal = driven.get(i);

			IGBinding thenBinding = thenBindings.getBinding(signal);
			IGBinding elseBinding = elseBindings.getBinding(signal);

			IGBindingNode thenNode = thenBinding != null ? thenBinding.getBinding() : null;
			IGBindingNode elseNode = elseBinding != null ? elseBinding.getBinding() : null;

			IGBindingNode node = new IGBindingNodePhi(fCond, thenNode, elseNode, fLocation);
			IGBinding binding = new IGBinding(signal, node);
			newBindings.setBinding(signal, binding);
		}

		return newBindings;

	}

}
