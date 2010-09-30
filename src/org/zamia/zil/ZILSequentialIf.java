/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 22, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.interpreter.ZILJumpNCStmt;
import org.zamia.zil.interpreter.ZILJumpStmt;
import org.zamia.zil.interpreter.ZILLabel;
import org.zamia.zil.synthesis.Binding;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableBinding;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILSequentialIf extends ZILSequentialStatement {

	private ZILOperation fCond;

	private ZILSequenceOfStatements fThenStmt;

	private ZILSequenceOfStatements fElseStmt;

	public ZILSequentialIf(ZILOperation aCond, ZILSequenceOfStatements aThenStmt, ZILIContainer aContainer, ASTObject aSrc) {
		super(aContainer, aSrc);
		fCond = aCond;
		fThenStmt = aThenStmt;
	}

	public void setElse(ZILSequenceOfStatements aElseStmt) {
		fElseStmt = aElseStmt;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "if (%s) {", fCond);
		fThenStmt.dump(aIndent + 1);
		if (fElseStmt != null) {
			logger.debug(aIndent, "} else {");
			fElseStmt.dump(aIndent + 1);
		}
		logger.debug(aIndent, "}");
	}

	@Override
	public String toString() {
		return "SequentialIf (cond=" + fCond + ")";
	}

	@Override
	public boolean isSynthesizable() throws ZamiaException {
		if (!(fCond.getClock() != null) && !fCond.isSynthesizable())
			return false;
		if (!fThenStmt.isSynthesizable())
			return false;
		if (fElseStmt != null && !fElseStmt.isSynthesizable())
			return false;

		return true;
	}

	@Override
	public Bindings computeBindings(ZILClock clk_, RTLCache cache_, VariableRemapping vr_) throws ZamiaException {
		logger.debug("ZILSequentialIf: computeBindings() con=" + fCond + " fThenStmt=" + fThenStmt + " fElseStmt=" + fElseStmt + " clock=" + clk_);

		// is this a clock specification?

		ZILClock clk = clk_;

		ZILClock clock = fCond.getClock();
		if (clock != null) {
			if (clk_ != null)
				throw new ZamiaException("Clock was already specified in this scope (Sequential if).", getSrc());

			clk = clock;

			Bindings thenBindings = fThenStmt.computeBindings(clk, cache_, vr_);
			Bindings elseBindings = null;
			if (fElseStmt != null)
				elseBindings = fElseStmt.computeBindings(clk_, cache_, vr_);

			if (elseBindings == null)
				return thenBindings;

			// we have to merge

			HashSetArray<ZILSignal> driven = computeDrivenSignals(thenBindings, elseBindings);

			Bindings retBindings = new Bindings();

			int num = driven.size();
			for (int i = 0; i < num; i++) {
				ZILSignal signal = driven.get(i);

				ZILTargetOperationDestination elseValue = null, thenValue = null;

				/*
				 * async part
				 */

				Binding sb = thenBindings.getBinding(signal);
				if (sb != null)
					thenValue = sb.getASyncValue();
				if (elseBindings != null) {
					Binding eb = elseBindings.getBinding(signal);
					if (eb != null)
						elseValue = eb.getASyncValue();
				}

				if (thenValue != null) {
					retBindings.bind(thenValue);
				} else if (elseValue != null) {
					retBindings.bind(elseValue);
				}

				/*
				 * sync part
				 */

				elseValue = null;
				thenValue = null;

				ZILClock elseClock = null;
				ZILClock thenClock = null;

				if (sb != null) {
					thenValue = sb.getSyncValue();
					thenClock = sb.getClock();
				}
				if (elseBindings != null) {
					Binding eb = elseBindings.getBinding(signal);
					if (eb != null) {
						elseValue = eb.getSyncValue();
						elseClock = eb.getClock();
					}
				}

				if (thenClock != elseClock && elseClock != null && thenClock != null)
					throw new ZamiaException("Colliding clock specifications detected", getSrc());

				clock = thenClock != null ? thenClock : elseClock;

				if (thenValue != null) {
					retBindings.bindClocked(thenValue, clock);
				} else {
					retBindings.bindClocked(elseValue, clock);
				}
			}

			return retBindings;
		}

		// no clock specification => compute bindings
		// for both then and else branch

		logger.debug("ZILSequentialIf: computeBindings() expanding variables on if condition: %s", fCond);

		Bindings thenBindings = fThenStmt.computeBindings(clk, cache_, vr_);
		Bindings elseBindings = null;
		if (fElseStmt != null)
			elseBindings = fElseStmt.computeBindings(clk_, cache_, vr_);

		// compute set of all driven Signals

		HashSetArray<ZILSignal> driven = computeDrivenSignals(thenBindings, elseBindings);

		// now produce resulting new bindings for all
		// signals and variables in this set

		Bindings retBindings = new Bindings();

		int num = driven.size();
		for (int i = 0; i < num; i++) {
			ZILSignal signal = driven.get(i);

			ZILTargetOperation elseValue = null, thenValue = null;

			/*
			 * async part
			 */

			Binding sb = thenBindings.getBinding(signal);
			if (sb != null) {
				ZILTargetOperationDestination tv = sb.getASyncValue();
				if (tv != null) {
					thenValue = tv.getSource();
				}
			}
			if (elseBindings != null) {
				Binding eb = elseBindings.getBinding(signal);
				if (eb != null) {
					ZILTargetOperationDestination ev = eb.getASyncValue();
					if (ev != null) {
						elseValue = ev.getSource();
					}
				}
			}

			if (thenValue != null || elseValue != null) {
				ZILTargetOperation o;
				if (elseValue == null) {
					o = new ZILTargetOperationCond(thenValue, fCond, getContainer(), getSrc());
				} else if (thenValue == null) {
					o = new ZILTargetOperationCond(elseValue, new ZILOperationNot(fCond, getContainer(), getSrc()), getContainer(), getSrc());
				} else {
					o = new ZILTargetOperationCond(thenValue, fCond, getContainer(), getSrc());
					ZILTargetOperation o2 = new ZILTargetOperationCond(elseValue, new ZILOperationNot(fCond, getContainer(), getSrc()), getContainer(), getSrc());

					o = new ZILTargetOperationEMux(o2, o, getContainer(), getSrc());
				}

				ZILTargetOperationDestination tod = new ZILTargetOperationDestination(signal, getContainer(), getSrc());
				tod.setSource(o);

				retBindings.bind(tod);
			}

			/*
			 * sync part
			 */

			elseValue = null;
			thenValue = null;

			ZILClock elseClock = null;
			ZILClock thenClock = null;

			if (sb != null) {
				ZILTargetOperationDestination tv = sb.getSyncValue();
				if (tv != null) {
					thenValue = tv.getSource();
					thenClock = sb.getClock();
				}
			}
			if (elseBindings != null) {
				Binding eb = elseBindings.getBinding(signal);
				if (eb != null) {
					ZILTargetOperationDestination ev = eb.getSyncValue();
					if (ev != null) {
						elseValue = ev.getSource();
						elseClock = eb.getClock();
					}
				}
			}

			if (thenClock != elseClock && elseClock != null && thenClock != null)
				throw new ZamiaException("Colliding clock specifications detected", getSrc());

			clock = thenClock != null ? thenClock : elseClock;

			if (thenValue != null || elseValue != null) {
				ZILTargetOperation o;
				if (elseValue == null) {
					o = new ZILTargetOperationCond(thenValue, fCond, getContainer(), getSrc());
				} else if (thenValue == null) {
					o = new ZILTargetOperationCond(elseValue, new ZILOperationNot(fCond, getContainer(), getSrc()), getContainer(), getSrc());
				} else {
					o = new ZILTargetOperationCond(thenValue, fCond, getContainer(), getSrc());
					ZILTargetOperation o2 = new ZILTargetOperationCond(elseValue, new ZILOperationNot(fCond, getContainer(), getSrc()), getContainer(), getSrc());

					o = new ZILTargetOperationEMux(o2, o, getContainer(), getSrc());
				}

				ZILTargetOperationDestination tod = new ZILTargetOperationDestination(signal, getContainer(), getSrc());
				tod.setSource(o);

				retBindings.bindClocked(tod, clock);
			}
		}

		return retBindings;
	}

	private HashSetArray<ZILSignal> computeDrivenSignals(Bindings thenBindings, Bindings elseBindings) {
		HashSetArray<ZILSignal> driven = new HashSetArray<ZILSignal>();

		int n = thenBindings.getNumBindings();
		for (int i = 0; i < n; i++) {
			Binding binding = thenBindings.getBinding(i);

			ZILIReferable ref = binding.getReferable();
			if (!(ref instanceof ZILSignal)) {
				logger.error("ZILSequentialIf: computeDrivenSignal: non-signal driven: %s", ref);
				continue;
			}

			driven.add((ZILSignal) ref);
		}
		if (elseBindings != null) {
			n = elseBindings.getNumBindings();
			for (int i = 0; i < n; i++) {
				Binding binding = elseBindings.getBinding(i);
				ZILIReferable ref = binding.getReferable();
				if (!(ref instanceof ZILSignal)) {
					logger.error("ZILSequentialIf: computeDrivenSignal: non-signal driven: %s", ref);
					continue;
				}
				driven.add((ZILSignal) ref);
			}
		}
		return driven;
	}

	@Override
	public void generateCode(ZILInterpreterCode aCode, RTLCache aCache, ZILLabel aLoopExitLabel) throws ZamiaException {
		ZILLabel elseLabel = new ZILLabel();
		ZILLabel endLabel = new ZILLabel();

		fCond.generateCode(aCode, aCache);

		if (fElseStmt != null)
			aCode.add(new ZILJumpNCStmt(elseLabel, getSrc()));
		else
			aCode.add(new ZILJumpNCStmt(endLabel, getSrc()));

		// then Statements
		//		Resolver resolver = new Resolver(getParent().getResolver(cache_), this);
		//		cache_.setResolver(this, resolver);
		// NO: fThenStmt generated code could contain exit statement code_.add(new EnterStmt (this));
		fThenStmt.generateCode(aCode, aCache, aLoopExitLabel);
		//code_.add(new ExitStmt (this));

		if (fElseStmt != null) {
			aCode.add(new ZILJumpStmt(endLabel, getSrc()));
			aCode.defineLabel(elseLabel);
			//			resolver = new Resolver(getParent().getResolver(cache_), this);
			//			cache_.setResolver(this, resolver);
			// NO: code could contain exit statement code_.add(new EnterStmt (this));
			fElseStmt.generateCode(aCode, aCache, aLoopExitLabel);
			// code_.add(new ExitStmt (this));
		}

		aCode.defineLabel(endLabel);
		//		code_.add(new ZILNopStmt(this));
	}

	@Override
	protected void inlineSubprograms(VariableRemapping aVR, ZILSequenceOfStatements aSOS, RTLCache aCache, String aReturnVarName) throws ZamiaException {

		ZILSequenceOfStatements ts = new ZILSequenceOfStatements(aSOS.getContainer(), fThenStmt.getSrc());
		fThenStmt.inlineSubprograms(aVR, ts, aCache, aReturnVarName);

		ZILSequentialIf se = new ZILSequentialIf(fCond.inlineSubprograms(aVR, aSOS, aCache), ts, aSOS.getContainer(), getSrc());
		aSOS.add(se);

		if (fElseStmt != null) {
			ZILSequenceOfStatements es = new ZILSequenceOfStatements(aSOS.getContainer(), fElseStmt.getSrc());
			fElseStmt.inlineSubprograms(aVR, es, aCache, aReturnVarName);
			se.setElse(es);
		}
	}

	@Override
	protected Bindings resolveVariables(Bindings aVB, ZILSequenceOfStatements aSOS, ZILClock aClk, RTLCache aCache, VariableRemapping aVR) throws ZamiaException {

		// is this a clock specification?

		ZILOperation cond = fCond;
		ZILClock clk = null;

		boolean isClock = false;

		ZILClock clock = cond.getClock();
		if (clock != null) {

			if (clk != null)
				throw new ZamiaException("Multiple clocks detected,", getSrc());

			clk = clock;

			isClock = true;

		} else {

			cond = fCond.resolveVariables(aVB, aSOS, aCache);

		}

		// vrms for then and else branch

		Bindings vbT;
		Bindings vbE = null;

		// resulting if statement will have an else branch in any case

		ZILSequenceOfStatements sosT = new ZILSequenceOfStatements(null, getSrc());
		ZILSequenceOfStatements sosE = new ZILSequenceOfStatements(null, getSrc());

		// now resolve variables separately

		vbT = fThenStmt.resolveVariables(aVB, sosT, clk, aCache, aVR);
		if (fElseStmt != null) {

			if (clk != null)
				throw new ZamiaException("No else clause allowed in clock-specifying if", getSrc());

			vbE = fElseStmt.resolveVariables(aVB, sosE, clk, aCache, aVR);
		}

		// now, the tricky part: merge vbT and vbE
		// first, compute set of all variables we have replaced in any of the two branches
		HashSetArray<ZILVariable> vars = new HashSetArray<ZILVariable>();
		int n = vbT.getNumVariableBindings();
		for (int i = 0; i < n; i++) {
			vars.add(vbT.getVariableBinding(i).getVar());
		}
		if (vbE != null) {
			n = vbE.getNumVariableBindings();
			for (int i = 0; i < n; i++) {
				vars.add(vbE.getVariableBinding(i).getVar());
			}
		}

		// now, for each driven variable  
		// produce resulting new bindings

		Bindings retBindings = new Bindings(aVB);

		int num = vars.size();
		for (int i = 0; i < num; i++) {
			ZILVariable var = vars.get(i);

			ZILTargetOperation elseValue = null, thenValue = null;

			ZILClock vclk = null;
			VariableBinding vb = vbT.get(var);
			if (vb != null) {
				thenValue = vb.getTO();
				vclk = vb.getClk();
			}
			if (vbE != null) {
				vb = vbE.get(var);
				if (vb != null) {
					elseValue = vb.getTO();
					if (vb.getClk() != null) {

						if (vclk != null && vclk != vb.getClk())
							throw new ZamiaException("Multiple clock signals affecting variable detected.", getSrc());

						vclk = vb.getClk();
					}
				}
			}

			if (isClock) {

				if (thenValue != null) {
					retBindings.bind(var, thenValue, vclk);
				} else if (elseValue != null) {
					retBindings.bind(var, elseValue, vclk);
				}

			} else {
				if (thenValue != null || elseValue != null) {
					ZILTargetOperation o;
					if (elseValue == null) {
						o = new ZILTargetOperationCond(thenValue, cond, aSOS.getContainer(), getSrc());
					} else if (thenValue == null) {
						o = new ZILTargetOperationCond(elseValue, new ZILOperationNot(cond, aSOS.getContainer(), getSrc()), aSOS.getContainer(), getSrc());
					} else {
						o = new ZILTargetOperationCond(thenValue, cond, aSOS.getContainer(), getSrc());
						ZILTargetOperation o2 = new ZILTargetOperationCond(elseValue, new ZILOperationNot(cond, aSOS.getContainer(), getSrc()), aSOS.getContainer(), getSrc());

						o = new ZILTargetOperationEMux(o2, o, aSOS.getContainer(), getSrc());
					}

					retBindings.bind(var, o, vclk);
				}
			}
		}

		ZILSequentialIf si = new ZILSequentialIf(cond, sosT, aSOS.getContainer(), getSrc());
		si.setElse(sosE);

		aSOS.add(si);

		return retBindings;
	}

	@Override
	public void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) {
		fCond.computeReadSignals(aReadSignals);

		fThenStmt.computeReadSignals(aReadSignals);

		if (fElseStmt != null)
			fElseStmt.computeReadSignals(aReadSignals);
	}

}
