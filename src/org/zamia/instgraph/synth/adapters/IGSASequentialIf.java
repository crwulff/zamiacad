/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 23, 2010
 */
package org.zamia.instgraph.synth.adapters;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationUnary;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.synth.IGBinding;
import org.zamia.instgraph.synth.IGBindingNode;
import org.zamia.instgraph.synth.IGBindingNodeCond;
import org.zamia.instgraph.synth.IGBindings;
import org.zamia.instgraph.synth.IGClock;
import org.zamia.instgraph.synth.IGObjectRemapping;
import org.zamia.instgraph.synth.IGStmtSynthAdapter;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMIf;
import org.zamia.instgraph.synth.model.IGSMIfClock;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.util.HashSetArray;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSASequentialIf extends IGStmtSynthAdapter {


	@Override
	public void preprocess(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSMSequenceOfStatements aPreprocessedSOS, String aReturnVarName, IGClock aClock, IGSynth aSynth)
			throws ZamiaException {
		IGSequentialIf ifstmt = (IGSequentialIf) aStmt;

		IGSequenceOfStatements thenSOS = ifstmt.getThenSOS();
		IGSequenceOfStatements elseSOS = ifstmt.getElseSOS();
		IGOperation cond = ifstmt.getCond();

		SourceLocation location = ifstmt.computeSourceLocation();
		
		boolean isClock = false;

		IGClock clock = aSynth.findClock(cond);
		if (clock != null) {

			if (aClock != null && aClock != clock)
				throw new ZamiaException("Multiple clocks detected,", location);

			if (elseSOS != null) {
				throw new ZamiaException ("No else branch allowed in clock if statements.", location);
			}
			
			IGSMSequenceOfStatements ts = new IGSMSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth);
			aSynth.getSynthAdapter(thenSOS).preprocess(thenSOS, aOR, ts, aReturnVarName, clock, aSynth);

			IGSMIfClock ic = new IGSMIfClock(clock, ts, ifstmt.getId(), location, aSynth);
			aPreprocessedSOS.add(ic);

			return;

		}

		IGSMExprNode icond = aSynth.getSynthAdapter(cond).preprocess(cond, aOR, aPreprocessedSOS, aSynth);

		IGSMSequenceOfStatements ts = new IGSMSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth);
		aSynth.getSynthAdapter(thenSOS).preprocess(thenSOS, aOR, ts, aReturnVarName, aClock, aSynth);

		IGSMSequenceOfStatements es;

		if (elseSOS != null) {
			es = new IGSMSequenceOfStatements(null, elseSOS.computeSourceLocation(), aSynth);
			aSynth.getSynthAdapter(elseSOS).preprocess(elseSOS, aOR, es, aReturnVarName, aClock, aSynth);
		} else {
			es = new IGSMSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth);
		}

		IGSMIf se = new IGSMIf(icond, ts, es, ifstmt.getId(), ifstmt.computeSourceLocation(), aSynth);

		aPreprocessedSOS.add(se);


	}

	@Override
	public void inlineSubprograms(IGSequentialStatement aStmt, IGObjectRemapping aOR, IGSequenceOfStatements aInlinedSOS, String aReturnVarName, IGSynth aSynth)
			throws ZamiaException {

		IGSequentialIf ifstmt = (IGSequentialIf) aStmt;

		IGSequenceOfStatements thenSOS = ifstmt.getThenSOS();
		IGSequenceOfStatements elseSOS = ifstmt.getElseSOS();
		IGOperation cond = ifstmt.getCond();

		IGSequenceOfStatements ts = new IGSequenceOfStatements(null, thenSOS.computeSourceLocation(), aSynth.getZDB());
		aSynth.getSynthAdapter(thenSOS).inlineSubprograms(thenSOS, aOR, ts, aReturnVarName, aSynth);

		IGOperation icond = aSynth.getSynthAdapter(cond).inlineSubprograms(cond, aOR, aInlinedSOS, aSynth);

		IGSequentialIf se = new IGSequentialIf(icond, ts, ifstmt.getId(), ifstmt.computeSourceLocation(), aSynth.getZDB());

		aInlinedSOS.add(se);

		if (elseSOS != null) {
			IGSequenceOfStatements es = new IGSequenceOfStatements(null, elseSOS.computeSourceLocation(), aSynth.getZDB());
			aSynth.getSynthAdapter(elseSOS).inlineSubprograms(elseSOS, aOR, es, aReturnVarName, aSynth);
			se.setElse(es);
		}

	}

	@Override
	public IGBindings resolveVariables(IGSequentialStatement aStmt, IGSequenceOfStatements aInlinedSOS, IGBindings aBindings, IGSequenceOfStatements aResolvedSOS, IGClock aClock,
			IGObjectRemapping aOR, IGSynth aSynth) throws ZamiaException {

		IGSequentialIf ifstmt = (IGSequentialIf) aStmt;
		SourceLocation location = ifstmt.computeSourceLocation();

		// is this a clock specification?

		IGOperation cond = ifstmt.getCond();

		boolean isClock = false;

		IGClock clock = aSynth.findClock(cond);
		if (clock != null) {

			if (aClock != null && aClock != clock)
				throw new ZamiaException("Multiple clocks detected,", location);

			isClock = true;

		} else {

			clock = aClock;

			cond = aSynth.getSynthAdapter(cond).resolveVariables(cond, aBindings, aResolvedSOS, aClock, aOR, aSynth);

		}

		// vrms for then and else branch

		IGBindings vbT;
		IGBindings vbE = null;

		IGSequenceOfStatements thenStmt = ifstmt.getThenSOS();
		IGSequenceOfStatements elseStmt = ifstmt.getElseSOS();

		// resulting if statement will have an else branch in any case

		IGSequenceOfStatements sosT = new IGSequenceOfStatements(null, location, aSynth.getZDB());
		IGSequenceOfStatements sosE = new IGSequenceOfStatements(null, location, aSynth.getZDB());

		// now resolve variables separately

		vbT = aSynth.getSynthAdapter(thenStmt).resolveVariables(thenStmt, aInlinedSOS, aBindings, sosT, clock, aOR, aSynth);
		if (elseStmt != null) {

			if (isClock) {
				throw new ZamiaException("No else branch allowed in clock-specifying if", location);
			}

			vbE = aSynth.getSynthAdapter(elseStmt).resolveVariables(elseStmt, aInlinedSOS, aBindings, sosE, clock, aOR, aSynth);
		}

		// now, the tricky part: merge vbT and vbE
		// first, compute set of all variables we have replaced in any of the two branches
		HashSetArray<IGObject> vars = computeDrivenSignals(vbT, vbE);

		// now, for each driven variable  
		// produce resulting new bindings

		IGBindings retBindings = new IGBindings(aBindings);

		int num = vars.size();
		for (int i = 0; i < num; i++) {
			IGObject var = vars.get(i);

			IGBinding thenBinding = vbT.getBinding(var);
			IGBinding elseBinding = vbE != null ? vbE.getBinding(var) : null;

			if (isClock) {

				if (thenBinding != null) {

					IGClock thenClk = thenBinding.getClock();
					if (thenClk != null) {
						if (clock != null && clock != thenClk) {
							throw new ZamiaException("Multiple clocks detected.", location);
						}
					}
					
					IGBindingNode node = thenBinding.getASyncBinding();
					if (node != null) {
						throw new ZamiaException ("IGSASequentialIf: Internal error: async value generated in sync block", location);
					}

					node = thenBinding.getSyncBinding();
					if (node != null) {
						retBindings.bind(var, node, clock);
					}

				}

			} else {

				if (elseBinding != null) {

					IGBindingNode node = elseBinding.getASyncBinding();
					if (node != null) {
						node = new IGBindingNodeCond(new IGOperationUnary(cond, UnaryOp.NOT, cond.getType(), location, aSynth.getZDB()), node);
						retBindings.bind(var, node, null);
					}

					node = elseBinding.getSyncBinding();
					if (node != null) {
						node = new IGBindingNodeCond(new IGOperationUnary(cond, UnaryOp.NOT, cond.getType(), location, aSynth.getZDB()), node);
						retBindings.bind(var, node, elseBinding.getClock());
					}

				}

				if (thenBinding != null) {

					IGBindingNode node = thenBinding.getASyncBinding();
					if (node != null) {
						node = new IGBindingNodeCond(cond, node);
						retBindings.bind(var, node, null);
					}

					node = thenBinding.getSyncBinding();
					if (node != null) {
						node = new IGBindingNodeCond(cond, node);
						retBindings.bind(var, node, thenBinding.getClock());
					}

				}

			}
		}

		IGSequentialIf si = new IGSequentialIf(cond, sosT, ifstmt.getId(), location, aSynth.getZDB());
		si.setElse(sosE);

		aResolvedSOS.add(si);

		return retBindings;
	}

	private HashSetArray<IGObject> computeDrivenSignals(IGBindings aBindingsA, IGBindings aBindingsB) {
		HashSetArray<IGObject> vars = new HashSetArray<IGObject>();
		int n = aBindingsA.getNumBindings();
		for (int i = 0; i < n; i++) {
			vars.add(aBindingsA.getBinding(i).getObject());
		}
		if (aBindingsB != null) {
			n = aBindingsB.getNumBindings();
			for (int i = 0; i < n; i++) {
				vars.add(aBindingsB.getBinding(i).getObject());
			}
		}
		return vars;
	}

	@Override
	public IGBindings computeBindings(IGSequentialStatement aStmt, IGSequenceOfStatements aResolvedSOS, IGBindings aLastBindings, IGClock clk_, IGSynth aSynth)
			throws ZamiaException {

		IGSequentialIf ifstmt = (IGSequentialIf) aStmt;
		SourceLocation location = ifstmt.computeSourceLocation();

		IGOperation fCond = ifstmt.getCond();
		IGSequenceOfStatements fThenStmt = ifstmt.getThenSOS();
		IGSequenceOfStatements fElseStmt = ifstmt.getElseSOS();

		logger.debug("IGSASequentialIf: computeBindings() con=" + fCond + " fThenStmt=" + fThenStmt + " fElseStmt=" + fElseStmt + " clock=" + clk_);

		// is this a clock specification?

		IGClock clock = aSynth.findClock(fCond);
		if (clock != null) {
			if (clk_ != null && clk_ != clock) {
				throw new ZamiaException("Clock was already specified in this scope (Sequential if).", location);
			}

			// inlining has made sure the else branch is empty so we only care about the then-branch here
			return aSynth.getSynthAdapter(fThenStmt).computeBindings(fThenStmt, aResolvedSOS, aLastBindings, clock, aSynth);
		}

		// no clock specification => compute bindings
		// for both then and else branch

		IGBindings thenBindings = aSynth.getSynthAdapter(fThenStmt).computeBindings(fThenStmt, aResolvedSOS, aLastBindings, clk_, aSynth);
		IGBindings elseBindings = aSynth.getSynthAdapter(fElseStmt).computeBindings(fElseStmt, aResolvedSOS, aLastBindings, clk_, aSynth);

		IGOperation cond = ifstmt.getCond();

		// compute set of all driven Signals

		HashSetArray<IGObject> driven = computeDrivenSignals(thenBindings, elseBindings);

		// now produce resulting new bindings for all
		// signals and variables in this set

		IGBindings retBindings = new IGBindings();

		int num = driven.size();
		for (int i = 0; i < num; i++) {
			IGObject signal = driven.get(i);

			IGBinding thenBinding = thenBindings.getBinding(signal);
			IGBinding elseBinding = elseBindings.getBinding(signal);

			if (elseBinding != null) {

				IGBindingNode node = elseBinding.getASyncBinding();
				if (node != null) {
					node = new IGBindingNodeCond(new IGOperationUnary(cond, UnaryOp.NOT, cond.getType(), location, aSynth.getZDB()), node);
					retBindings.bind(signal, node, null);
				}

				node = elseBinding.getSyncBinding();
				if (node != null) {
					node = new IGBindingNodeCond(new IGOperationUnary(cond, UnaryOp.NOT, cond.getType(), location, aSynth.getZDB()), node);
					retBindings.bind(signal, node, elseBinding.getClock());
				}

			}

			if (thenBinding != null) {

				IGBindingNode node = thenBinding.getASyncBinding();
				if (node != null) {
					node = new IGBindingNodeCond(cond, node);
					retBindings.bind(signal, node, null);
				}

				node = thenBinding.getSyncBinding();
				if (node != null) {
					node = new IGBindingNodeCond(cond, node);
					retBindings.bind(signal, node, thenBinding.getClock());
				}

			}
		}

		return retBindings;
	}


}
