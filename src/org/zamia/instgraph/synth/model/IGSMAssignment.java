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
import org.zamia.instgraph.synth.IGBindingNodePhi;
import org.zamia.instgraph.synth.IGBindingNodeValue;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSMAssignment extends IGSMStatement {

	private IGSMTarget fTarget;

	private IGSMExprNode fValue;

	public IGSMAssignment(IGSMExprNode aValue, IGSMTarget aTarget, String aLabel, SourceLocation aLocation, IGSynth aSynth) {
		super(aLabel, aLocation, aSynth);
		fTarget = aTarget;
		fValue = aValue;
	}

	@Override
	public void dump(int aIndent) {
		logger.debug(aIndent, "%s <- %s", fTarget, fValue);
	}

	@Override
	public IGBindings computeBindings(IGBindings aBindingsBefore, IGSynth aSynth) throws ZamiaException {

		ArrayList<IGSMConditionalTarget> condTargets = new ArrayList<IGSMConditionalTarget>();

		fTarget.computeTargets(condTargets);

		// FIXME: handle variables

		IGBindings newBindings = new IGBindings();

		for (IGSMConditionalTarget condTarget : condTargets) {

			RTLSignal target = condTarget.getTarget();

			IGSMExprNode cond = condTarget.getCond();

			IGBindingNode node = new IGBindingNodeValue(fValue, fLocation);

			if (cond != null) {
				node = new IGBindingNodePhi(cond, node, null, fLocation);
			}

			IGBinding binding = new IGBinding(target, node);
			newBindings.setBinding(target, binding);
		}

		return newBindings;

	}

}
