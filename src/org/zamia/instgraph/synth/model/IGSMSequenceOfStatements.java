/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.synth.IGBinding;
import org.zamia.instgraph.synth.IGBindingNode;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMSequenceOfStatements extends IGSMStatement {

	private final ArrayList<IGSMStatement> fStmts = new ArrayList<IGSMStatement>();

	public IGSMSequenceOfStatements(String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super(aLabel, aLocation, aSynth);
	}

	private int getNumStatements() {
		return fStmts.size();
	}

	private IGSMStatement getStatement(int aIdx) {
		return fStmts.get(aIdx);
	}

	@Override
	public void dump(int aIndent) {
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSMStatement stmt = getStatement(i);
			stmt.dump(aIndent + 2);
		}
	}

	public void add(IGSMStatement aStatement) {
		fStmts.add(aStatement);
	}

	@Override
	public IGBindings computeBindings(IGBindings aBindingsBefore, IGSynth aSynth) throws ZamiaException {

		IGBindings newBindings = new IGBindings();

		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSMStatement stmt = getStatement(i);

			IGBindings stmtBindings = stmt.computeBindings(newBindings, aSynth);

			int m = stmtBindings.getNumBindings();
			for (int j = 0; j < m; j++) {

				IGBinding binding = stmtBindings.getBinding(j);

				RTLSignal target = binding.getTarget();

				IGBinding prevBinding = newBindings.getBinding(target);

				if (prevBinding != null) {

					// replace omega-values in new binding by previous binding

					IGBindingNode node = binding.getBinding().replaceOmega(prevBinding.getBinding());

					binding = new IGBinding(target, node);

				}

				newBindings.setBinding(target, binding);
			}
		}

		return newBindings;
	}


}
