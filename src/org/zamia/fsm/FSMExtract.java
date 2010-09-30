/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.fsm;

import java.util.ArrayList;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.vhdl.ast.Operation;
import org.zamia.vhdl.ast.SequentialCase;
import org.zamia.vhdl.ast.SequentialIf;
import org.zamia.vhdl.ast.SequentialSignalAssignment;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class FSMExtract {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	class FSMFingerprint {
		public SequentialIf syncReset = null;

		public SequentialIf asyncReset = null;

		public SequentialCase myTransition = null;

		public ArrayList<SequentialSignalAssignment> initStmts = new ArrayList<SequentialSignalAssignment>();
	}

	class ConditionalAssignment {
		Operation condition;

		String value;
	}

	class SimplifyResult {
		Operation op;

		boolean isTrue;

		boolean isFalse;

		public SimplifyResult(boolean isFalse_, boolean isTrue_, Operation op_) {
			op = op_;
			isFalse = isFalse_;
			isTrue = isTrue_;
		}
	}

//	public void extractFSMs(Bindings bindings_, HashSet<RTLSignal> handledSignals_, ArrayList<FSM> fsms_, OperationCache cache_) throws ZamiaException {
//		int n = bindings_.getNumBindings();
//		for (int i = 0; i < n; i++) {
//
//			Binding b = bindings_.getBinding(i);
//
//			RTLSignal s = b.getSignal();
//
//			logger.debug("FSMExtract: Checking wheter '%s' qualifies as a state signal:", s);
//
//			if (isStateSignal(s, bindings_, cache_)) {
//
//				FSM fsm = extractFSM(s, bindings_, cache_);
//
//				if (fsm != null) {
//					handledSignals_.add(s);
//
//					getAdditionalOutputs(fsm, s, handledSignals_, bindings_, cache_);
//
//					fsms_.add(fsm);
//				}
//			}
//		}
//	}
//
//	private void getAdditionalOutputs(FSM fsm, RTLSignal state_, HashSet<RTLSignal> handledSignals_, Bindings bindings_, ZILCache cache_) throws ZamiaException {
//		logger.debug("FSMExtract: Additional outputs:");
//
//		int n = bindings_.getNumBindings();
//		for (int i = 0; i < n; i++) {
//			Binding b = bindings_.getBinding(i);
//
//			if (b.getSyncValue() != null)
//				continue;
//
//			logger.debug("FSMExtract: Is this an additional output? " + b.getSignal().getId());
//			// b.dump(System.out, 2);
//
//			ZILTargetOperation to = b.getASyncValue();
//
//			if (dependsOnlyOnStateSignal(to, false, state_, cache_)) {
//				logger.debug("FSMExtract: Have an additional output: " + b.getSignal().getId());
//				b.getASyncValue().dump(System.out, 2);
//
//				int m = fsm.getNumStates();
//				for (int j = 0; j < m; j++) {
//
//					State s = fsm.getState(j);
//
//					EnabledResult value = computeValue(to, state_, s.getId());
//
//					fsm.addOutput(s, b.getSignal(), value.res);
//
//					logger.debug("FSMExtract:   output of state " + s.getId() + " is " + value.res);
//				}
//				handledSignals_.add(b.getSignal());
//				handledSignals_.add(fsm.getNextStateSignal());
//			}
//		}
//	}
//
//	class EnabledResult {
//		OperationLiteral res;
//
//		boolean enabled;
//
//		public EnabledResult(OperationLiteral res_, boolean enabled_) {
//			res = res_;
//			enabled = enabled_;
//		}
//
//	}
//
//	private EnabledResult computeValue(TargetOperation to_, RTLSignal state_, String currentState_) {
//
//		if (to_ instanceof TargetOperationEMux) {
//			TargetOperationEMux emux = (TargetOperationEMux) to_;
//
//			EnabledResult er = computeValue(emux.getValue(), state_, currentState_);
//
//			if (er.enabled)
//				return er;
//
//			return computeValue(emux.getOldValue(), state_, currentState_);
//		} else if (to_ instanceof TargetOperationCond) {
//			TargetOperationCond cond = (TargetOperationCond) to_;
//
//			EnabledResult er = computeValue(cond.getOp(), state_, currentState_);
//
//			er.enabled = er.enabled & computeBoolean(cond.getCond(), state_, currentState_);
//
//			return er;
//		} else if (to_ instanceof TargetOperationOp) {
//
//			return new EnabledResult(computeValue(((TargetOperationOp) to_).getOp(), state_, currentState_), true);
//		}
//
//		logger.debug("FSMExtract: ERROR: unknown target operation " + to_);
//
//		return null;
//	}
//
//	private OperationLiteral computeValue(Operation op_, RTLSignal state_, String currentState_) {
//
//		if (op_ instanceof OperationLiteral) {
//			return (OperationLiteral) op_;
//		}
//
//		logger.debug("FSMExtract: ERR: unknown op " + op_);
//		return null;
//	}
//
//	private boolean computeBoolean(Operation op_, RTLSignal state_, String currentState_) {
//
//		if (op_ instanceof OperationLogic) {
//
//			OperationLogic ol = (OperationLogic) op_;
//
//			boolean b1 = computeBoolean(ol.getOperandA(), state_, currentState_);
//
//			LogicOp op = ol.getOp();
//			if (op == LogicOp.AND) {
//				boolean b2 = computeBoolean(ol.getOperandB(), state_, currentState_);
//
//				if (!b1 || !b2) {
//					return false;
//				} else
//
//					return true;
//			} else {
//				logger.debug("FSMExtract: Error: unknown logic op");
//			}
//		} else if (op_ instanceof OperationNot) {
//			OperationNot ol = (OperationNot) op_;
//
//			boolean b1 = computeBoolean(ol.getOperand(), state_, currentState_);
//			return !b1;
//
//		} else if (op_ instanceof OperationCompare) {
//			OperationCompare oc = (OperationCompare) op_;
//
//			if (oc.getOp() != CompareOp.EQUAL) {
//				logger.debug("FSMExtract: ERROR: only compare op supported here.");
//				return false;
//			}
//
//			Operation a = oc.getOperandA();
//			Operation b = oc.getOperandB();
//
//			String aid = getSimpleId(a);
//			String bid = b.toString();
//
//			if (aid == null || !aid.equals(state_.getId())) {
//				aid = getSimpleId(b);
//				bid = aid.toString();
//			}
//
//			if (aid == null || !aid.equals(state_.getId())) {
//				logger.debug("FSMExtract: ERROR: unsupported compare!");
//				return false;
//			}
//
//			return bid.equals(currentState_);
//		}
//
//		logger.debug("FSMExtract: ERR: unknown op " + op_);
//
//		return false;
//	}
//
//	private boolean dependsOnlyOnStateSignal(ZILTargetOperation to_, boolean haveCond_, RTLSignal state_, ZILCache cache_) throws ZamiaException {
//
//		// logger.debug ("Checking TO: "+to_);
//
//		if (to_ instanceof ZILTargetOperationEMux) {
//			ZILTargetOperationEMux emux = (ZILTargetOperationEMux) to_;
//
//			if (!dependsOnlyOnStateSignal(emux.getValue(), haveCond_, state_, cache_))
//				return false;
//			if (!dependsOnlyOnStateSignal(emux.getOldValue(), true, state_, cache_))
//				return false;
//			return true;
//
//		} else if (to_ instanceof ZILTargetOperationCond) {
//			ZILTargetOperationCond cond = (ZILTargetOperationCond) to_;
//
//			if (!dependsOnlyOnStateSignal(cond.getOp(), true, state_, cache_))
//				return false;
//
//			return dependsOnlyOnStateSignal(cond.getCond(), state_, cache_);
//		} else if (to_ instanceof ZILTargetOperationOp) {
//			if (!haveCond_)
//				return false;
//			if (!isConstant(((ZILTargetOperationOp) to_).getOp()))
//				return false;
//			return true;
//		}
//
//		return false;
//	}
//
//	private boolean dependsOnlyOnStateSignal(Operation cond_, RTLSignal state_, ZILCache cache_) throws ZamiaException {
//
//		// logger.debug ("Checking: "+cond_);
//
//		if (cond_ instanceof OperationCompare) {
//			OperationCompare cmp = (OperationCompare) cond_;
//
//			Operation a = cmp.getOperandA();
//			Operation b = cmp.getOperandB();
//
//			if (!dependsOnlyOnStateSignal(a, state_, cache_))
//				return false;
//
//			if (!dependsOnlyOnStateSignal(b, state_, cache_))
//				return false;
//			return true;
//		} else if (cond_ instanceof OperationName) {
//
//			OperationName name = (OperationName) cond_;
//
//			String str = getSimpleId(name);
//			if (str == null)
//				return false;
//			if (!str.equals(state_.getId())) {
//				if (!(name.computeConstant(state_.getType(), false, null, cache_) != null))
//					return false;
//				else
//					return true;
//			}
//			return true;
//		} else if (cond_ instanceof OperationLiteral) {
//			return true;
//		} else if (cond_ instanceof OperationNot) {
//			OperationNot n = (OperationNot) cond_;
//			return dependsOnlyOnStateSignal(n.getOperand(), state_, cache_);
//		}
//
//		return false;
//	}
//
//	private boolean isConstant(Operation op) {
//
//		if (!(op instanceof OperationLiteral))
//			return false;
//
//		return true;
//	}

//	private FSM extractFSM(RTLSignal s_, Bindings bindings_, ZILCache cache_) throws ZamiaException {
//
//		// FIXME: translate to ZIL world
//		logger.error("FSMExtract.extractFSM(): not translated to ZIL world yet.");
//		return null;
//		
//		RTLSignal state = s_;
//
//		Binding b = bindings_.getBinding(state);
//
//		if (b == null)
//			return null;
//
//		RTLGraph rtlg = state.getRTLGraph();
//
//		logger.debug("FSMExtract: Extracting FSM from " + state);
//
//		// b.dump(System.out, 2);
//
//		ZILTargetOperation sv = b.getSyncValue();
//
//		if (sv == null) {
//			// no reg inferred
//			return null;
//		}
//
//		if (!(sv instanceof ZILTargetOperationOp)) {
//			return null;
//		}
//
//		ZILTargetOperationOp toop = (ZILTargetOperationOp) sv;
//		Operation value = toop.getOp();
//
//		if (value instanceof Name) {
//			Name name = (Name) value;
//
//			if (name.getNumExtensions() > 0)
//				return null;
//
//			RTLSignal nextState = rtlg.findSignal(name.getId());
//
//			logger.debug("FSMExtract: Next state signal identified: " + nextState);
//
//			Binding bn = bindings_.getBinding(nextState);
//
//			// bn.dump(System.out, 2);
//
//			if (bn.getSyncValue() != null)
//				return null;
//
//			ArrayList<ConditionalAssignment> cas = new ArrayList<ConditionalAssignment>();
//			collectConditionalAssignments(bn.getASyncValue(), null, state, nextState, cache_, cas);
//
//			FSM fsm = new FSM("MyFSM");
//			HashMap<String, State> allStates = new HashMap<String, State>();
//
//			fsm.setStateSignal(state);
//			fsm.setNextStateSignal(nextState);
//
//			for (int i = 0; i < cas.size(); i++) {
//				ConditionalAssignment ca = cas.get(i);
//
//				logger.debug(ca.value + "\t : " + ca.condition);
//				if (!allStates.containsKey(ca.value)) {
//					State s = new State(ca.value);
//					allStates.put(ca.value, s);
//					fsm.addState(s);
//				}
//			}
//
//			for (String s : allStates.keySet()) {
//
//				logger.debug("FSMExtract: Transitions from state " + s + ":");
//
//				State f = allStates.get(s);
//
//				for (int i = 0; i < cas.size(); i++) {
//					ConditionalAssignment ca = cas.get(i);
//
//					// FIXME
//					if (ca.condition == null) {
//						logger.debug("FSMExtract: Condition == null - don't know how to handle");
//						return null;
//					}
//
//					SimplifyResult res = simplify(ca.condition, state.getId(), s);
//
//					if (!res.isFalse) {
//						logger.debug("FSMExtract:   to " + ca.value + " if " + res.op.toVHDL());
//
//						State t = allStates.get(ca.value);
//
//						CondTransition ct = f.createCondTransition(t);
//
//						ct.setCondition(res.op);
//
//					}
//				}
//			}
//
//			return fsm;
//		}
//
//		return null;
//	}

//	private SimplifyResult simplify(Operation op_, String stateSignalId_, String state_) {
//
//		if (op_ == null)
//			return new SimplifyResult(false, true, null);
//
//		if (op_ instanceof OperationLogic) {
//
//			OperationLogic ol = (OperationLogic) op_;
//
//			SimplifyResult r1 = simplify(ol.getOperandA(), stateSignalId_, state_);
//
//			LogicOp op = ol.getOp();
//			if (op == LogicOp.AND) {
//				SimplifyResult r2 = simplify(ol.getOperandB(), stateSignalId_, state_);
//
//				if (r1.isFalse || r2.isFalse) {
//					return new SimplifyResult(true, false, null);
//				} else if (r1.isTrue && r2.isTrue) {
//					return new SimplifyResult(false, true, null);
//				} else if (r1.isTrue) {
//					return new SimplifyResult(false, false, r2.op);
//				} else if (r2.isTrue) {
//					return new SimplifyResult(false, false, r1.op);
//				} else {
//					return new SimplifyResult(false, false, new OperationLogic(LogicOp.AND, r1.op, r2.op, null, 0));
//				}
//			} else {
//				logger.debug("FSMExtract: Error: unknown logic op");
//			}
//		} else if (op_ instanceof OperationNot) {
//			OperationNot ol = (OperationNot) op_;
//
//			SimplifyResult r1 = simplify(ol.getOperand(), stateSignalId_, state_);
//			if (r1.isFalse)
//				return new SimplifyResult(false, true, null);
//			else if (r1.isTrue)
//				return new SimplifyResult(true, false, null);
//			else
//				return new SimplifyResult(false, false, new OperationNot(r1.op, null, 0));
//
//		} else if (op_ instanceof OperationCompare) {
//			OperationCompare oc = (OperationCompare) op_;
//
//			if (oc.getOp() != CompareOp.EQUAL) {
//				logger.debug("FSMExtract: ERROR: only compare op supported here.");
//				return new SimplifyResult(false, false, oc);
//			}
//
//			Operation a = oc.getOperandA();
//			Operation b = oc.getOperandB();
//
//			String aid = getSimpleId(a);
//			if (aid == null)
//				return new SimplifyResult(false, false, oc);
//			String bid = getSimpleId(b);
//			if (bid == null)
//				return new SimplifyResult(false, false, oc);
//
//			if (aid.equals(stateSignalId_)) {
//				if (bid.equals(state_))
//					return new SimplifyResult(false, true, null);
//				else
//					return new SimplifyResult(true, false, null);
//			} else if (bid.equals(stateSignalId_)) {
//				if (aid.equals(state_))
//					return new SimplifyResult(false, true, null);
//				else
//					return new SimplifyResult(true, false, null);
//			} else
//				return new SimplifyResult(false, false, oc);
//		}
//
//		logger.debug("FSMExtract: Error: Don't know how to handle: " + op_);
//
//		return null;
//	}

//	private boolean collectConditionalAssignments(TargetOperation to_, Operation cond_, RTLSignal stateSignal_, RTLSignal nextStateSignal_, OperationCache cache_,
//			ArrayList<ConditionalAssignment> cas_) throws ZamiaException {
//
//		logger.debug("FSMExtract: Extracting conditional assignments from " + to_);
//
//		if (to_ instanceof TargetOperationEMux) {
//
//			TargetOperationEMux emux = (TargetOperationEMux) to_;
//
//			if (!collectConditionalAssignments(emux.getOldValue(), cond_, stateSignal_, nextStateSignal_, cache_, cas_))
//				return false;
//			if (!collectConditionalAssignments(emux.getValue(), cond_, stateSignal_, nextStateSignal_, cache_, cas_))
//				return false;
//
//		} else if (to_ instanceof TargetOperationOp) {
//
//			Operation op = ((TargetOperationOp) to_).getOp();
//
//			if (!(op instanceof Name)) {
//				logger.debug("FSMExtract: Error! dont' know how to handle " + op);
//				return false;
//			}
//
//			Name name = (Name) op;
//			if (name.getNumExtensions() > 0) {
//				logger.debug("FSMExtract: Error! Name extensions not handled " + name);
//				return false;
//			}
//
//			String id = name.getId();
//
//			if (id.equals(stateSignal_.getId()) || id.equals(nextStateSignal_.getId()))
//				return true;
//
//			ConditionalAssignment ca = new ConditionalAssignment();
//
//			ca.value = id;
//			ca.condition = cond_;
//			cas_.add(ca);
//
//		} else if (to_ instanceof TargetOperationCond) {
//			TargetOperationCond toc = (TargetOperationCond) to_;
//
//			Operation c = toc.getCond();
//
//			if (cond_ != null) {
//				c = new OperationLogic(LogicOp.AND, cond_, c, null, 0);
//			}
//
//			return collectConditionalAssignments(toc.getOp(), c, stateSignal_, nextStateSignal_, cache_, cas_);
//
//		} else {
//			logger.debug("FSMExtract: Error! don't know how to handle " + to_);
//			return false;
//		}
//		return true;
//	}

	// private State extractState(Operation cond_, RTLSignal stateSignal_) {
	//		
	// if (!(cond_ instanceof OperationCompare)) {
	// return null;
	// }
	//		
	// OperationCompare cmp = (OperationCompare) cond_;
	// if (!(cmp.getOp() == CompareOp.EQUAL)) {
	// return null;
	// }
	//		
	// String a = getSimpleId(cmp.getOperandA());
	// if (a==null)
	// return null;
	// String b = getSimpleId(cmp.getOperandB());
	// if (b==null)
	// return null;
	//		
	// String stateId = stateSignal_.getId();
	//
	// if (a.equals(stateId)) {
	// return new State(b);
	// } else if (b.equals(stateId))
	// return new State(a);
	//		
	// return null;
	// }

//	private String getSimpleId(Operation a) {
//		if (!(a instanceof Name))
//			return null;
//		Name name = (Name) a;
//		if (name.getNumExtensions() > 0)
//			return null;
//		return name.getId();
//	}
//
//	private boolean isStateSignal(RTLSignal s_, Bindings bindings_, OperationCache cache_) throws ZamiaException {
//
//		Binding b = bindings_.getBinding(s_);
//
//		if (b == null)
//			return false;
//
//		logger.debug("FSMExtract: Checking wheter this could be a state signal binding:");
//
//		// b.dump(System.out, 2);
//
//		TargetOperation sv = b.getSyncValue();
//
//		if (sv == null) {
//			// no reg inferred
//			return false;
//		}
//
//		if (checkOnlyCompleteAndConstantValues(sv, s_.getId(), bindings_, cache_, s_.getType())) {
//			logger.debug("FSMExtract:  ==============> STATE SIGNAL CANDIDATE: " + s_);
//			return true;
//		}
//
//		// if (! (sv instanceof TargetOperationOp)) {
//		// return false;
//		// }
//		//		
//		// TargetOperationOp toop = (TargetOperationOp) sv;
//		// Operation value = toop.getOp();
//		//		
//		// if (value instanceof OperationName) {
//		// OperationName name = (OperationName) value;
//		//			
//		// if (name.getNumExtensions()>0)
//		// return false;
//		//			
//		// RTLSignal s = s_.getRTLGraph().findSignal(name.getId());
//		// if (s == null)
//		// return false;
//		// return isStateSignal(s, bindings_, cache_);
//		// } else {
//		//			
//		// if (checkOnlyConstantValues(value, cache_, s_.getType()))
//		// return true;
//		//			
//		//			
//		// }
//
//		return false;
//	}
//
//	private boolean checkOnlyCompleteAndConstantValues(TargetOperation to_, String self_, Bindings bindings_, OperationCache cache_, SigType type_) throws ZamiaException {
//
//		if (to_ instanceof TargetOperationEMux) {
//			TargetOperationEMux emux = (TargetOperationEMux) to_;
//
//			return checkOnlyCompleteAndConstantValues(emux.getOldValue(), self_, bindings_, cache_, type_)
//					&& checkOnlyCompleteAndConstantValues(emux.getValue(), self_, bindings_, cache_, type_);
//		} else if (to_ instanceof TargetOperationCond) {
//			TargetOperationCond cond = (TargetOperationCond) to_;
//
//			return checkOnlyCompleteAndConstantValues(cond.getOp(), self_, bindings_, cache_, type_);
//		} else if (to_ instanceof TargetOperationOp) {
//			TargetOperationOp oop = (TargetOperationOp) to_;
//			return checkOnlyConstantValues(oop.getOp(), self_, bindings_, cache_, type_);
//		}
//		return false;
//	}
//
//	private boolean checkOnlyConstantValues(Operation value, String self_, Bindings bindings_, OperationCache cache_, SigType typeHint_) throws ZamiaException {
//
//		if (value instanceof Name) {
//			Name name = (Name) value;
//
//			Value c = name.getConstant(cache_, typeHint_, null, false);
//
//			if (c != null) {
//				return true;
//			} else {
//
//				VariableBinding vb = name.getVariableBinding(cache_);
//				if (vb != null) {
//
//					logger.debug("FSMExtract: FSM: VB: " + vb);
//
//					return checkOnlyCompleteAndConstantValues(vb.getTO(), self_, bindings_, cache_, vb.getType());
//				} else {
//
//					if (name.getNumExtensions() > 0)
//						return false;
//
//					String id = name.getId();
//
//					if (id.equals(self_))
//						return true; // same state
//
//					Resolver resolver = name.getNameResolver(cache_);
//
//					IResolvableObject obj = resolver.resolveObject(id, cache_);
//
//					if (obj instanceof Value)
//						return true;
//					if (obj instanceof RTLSignal) {
//
//						RTLSignal signal = (RTLSignal) obj;
//
//						Binding b = bindings_.getBinding(signal);
//
//						if (b == null)
//							return false;
//
//						if (b.getSyncValue() != null)
//							return false;
//
//						return checkOnlyCompleteAndConstantValues(b.getASyncValue(), self_, bindings_, cache_, typeHint_);
//					}
//				}
//			}
//
//			// return false;
//
//		}
//
//		return false;
//	}
}
