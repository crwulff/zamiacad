/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 28, 2007
 */

package org.zamia.rtl;

import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILValue;
import org.zamia.zil.interpreter.ZILInterpreter;


/**
 * simple optimizations on the RTL Level, ie
 * 
 * constant propagation arithemtic / logic simplifications
 * 
 * @author guenter bartsch
 * 
 */

public class Optimizer {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public static final boolean dump = false;

	public static void optimize(RTLGraph rtlg_) throws ZamiaException {

		boolean changed;

		HashMap<RTLSignal, ZILValue> constantSignals = new HashMap<RTLSignal, ZILValue>();

		do {
			changed = false;

			changed = changed | findConstantSignals(rtlg_, constantSignals);
			changed = changed | removeLatches(rtlg_, constantSignals);

			changed = changed | removeLiterals(rtlg_, constantSignals);

			changed = changed | replaceConstantTargetArraySels(rtlg_, constantSignals);
			changed = changed | replaceConstantArraySels(rtlg_, constantSignals);
			changed = changed | replaceCompleteTargetArraySels(rtlg_, constantSignals);
			changed = changed | replaceCompleteTargetRecordSels(rtlg_, constantSignals);
			changed = changed | replaceConstantConds(rtlg_, constantSignals);
			changed = changed | replaceConstantMuxs(rtlg_, constantSignals);
			changed = changed | replaceConstantEnableMuxs(rtlg_, constantSignals);
			changed = changed | replaceConstantMathOps(rtlg_, constantSignals);

			changed = changed | simplifyORLogic(rtlg_, constantSignals);
			changed = changed | simplifyNOTLogic(rtlg_, constantSignals);

			changed = changed | removeConstantCEs(rtlg_, constantSignals);

			changed = changed | replaceConstantRecordSels(rtlg_, constantSignals);

			changed = changed | removeDangling(rtlg_, constantSignals);
			changed = changed | mergeTargetArrayCSels(rtlg_, constantSignals);
			changed = changed | mergeTargetRecordCSels(rtlg_, constantSignals);
			changed = changed | mergeArrayCSels(rtlg_, constantSignals);
			changed = changed | mergeRecordSels(rtlg_, constantSignals);
			changed = changed | mergeEquivalentModules(rtlg_, constantSignals);

			rtlg_.consistencyCheck();

		} while (changed);

	}

	private static RTLModule getDriver(RTLPort port_) throws ZamiaException {

		RTLSignal signal = port_.getSignal();
		if (signal == null)
			return null;

		int nDrivers = signal.getNumDrivers();
		if (nDrivers != 1)
			return null;

		return signal.getDriver(0).getModule();
	}

	private static boolean mergeTargetRecordCSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetEMux))
				continue;

			RTLTargetEMux emux = (RTLTargetEMux) module;

			// driven by two TargetRecordCSels ?

			RTLPort d1Port = emux.getD1();
			RTLSignal d1Signal = d1Port.getSignal();
			if (d1Signal == null) {
				logger.error("Optimizer: RTLTargetEMux '%s': d1 port not connected.", emux);
				continue;
			}
			RTLPort driver1Port = d1Signal.getDriver(0);
			if (driver1Port == null)
				continue;
			RTLModule driver1 = driver1Port.getModule();
			if (!(driver1 instanceof RTLTargetRecordCSel))
				continue;
			RTLTargetRecordCSel csel1 = (RTLTargetRecordCSel) driver1;

			RTLPort d2Port = emux.getD2();
			RTLSignal d2Signal = d2Port.getSignal();
			if (d2Signal == null) {
				logger.error("Optimizer: RTLTargetEMux '%s': d2 port not connected.", emux);
				continue;
			}
			RTLPort driver2Port = d2Signal.getDriver(0);
			if (driver2Port == null)
				continue;
			RTLModule driver2 = driver2Port.getModule();
			if (!(driver2 instanceof RTLTargetRecordCSel))
				continue;
			RTLTargetRecordCSel csel2 = (RTLTargetRecordCSel) driver2;

			RTLPort qPort = emux.getZ();
			RTLSignal qs = qPort.getSignal();
			RTLPort qePort = emux.getZE();
			RTLSignal qes = qePort.getSignal();

			rtlg_.remove(emux);
			rtlg_.remove(driver2);

			int m = csel2.getNumInputs();
			for (int j = 0; j < m; j++) {

				RTLPort input = csel2.getInput(j);
				String id = csel2.getInputId(j);

				csel1.connectInput(id, input.getSignal(), csel2.getSource());
			}

			csel1.getZ().setSignal(qs);
			csel1.getZE().setSignal(qes);

			changed = true;
		}

		return changed;
	}

	private static boolean removeConstantCEs(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLCE))
				continue;

			RTLCE ce = (RTLCE) module;

			RTLPort e = ce.getE();

			ZILValue v = constantSignals.get(e.getSignal());
			if (v == null)
				continue;

			if (v.isLogicOne()) {

				RTLPort ze = ce.getZE();

				rtlg_.remove(module);

				rtlg_.sigJoin(rtlg_.placeOne(ze.getType(), module.getSource()), ze.getSignal(), module.getSource());

				changed = true;
			}
		}

		return changed;
	}

	private static boolean simplifyNOTLogic(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLOperationLogic))
				continue;

			RTLOperationLogic logic = (RTLOperationLogic) module;

			LogicOp op = logic.getOp();

			if (op == LogicOp.NOT) {

				RTLPort a = logic.getA();

				RTLPort z = logic.getZ();
				RTLSignal zs = z.getSignal();

				RTLModule driver = getDriver(a);
				if (driver instanceof RTLOperationLogic) {
					RTLOperationLogic l = (RTLOperationLogic) driver;

					if (l.getOp() == LogicOp.NOT) {

						RTLSignal s = l.getA().getSignal();
						rtlg_.remove(logic);

						rtlg_.sigJoin(s, zs, logic.getSource());

						changed = true;
					}
				}

			}

		}

		return changed;
	}

	private static boolean simplifyORLogic(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLOperationLogic))
				continue;

			RTLOperationLogic logic = (RTLOperationLogic) module;

			LogicOp op = logic.getOp();

			if (op == LogicOp.OR) {

				RTLPort a = logic.getA();

				RTLSignal as = a.getSignal();

				RTLPort b = logic.getB();

				RTLSignal bs = b.getSignal();

				RTLPort z = logic.getZ();
				RTLSignal zs = z.getSignal();

				RTLSignal sInv = null;

				RTLModule driver = getDriver(a);
				if (driver instanceof RTLOperationLogic) {
					RTLOperationLogic l = (RTLOperationLogic) driver;

					if (l.getOp() == LogicOp.NOT) {

						RTLSignal s = l.getA().getSignal();
						if (s == bs) {
							sInv = s;
						}
					}
				}

				if (sInv == null) {
					driver = getDriver(b);
					if (driver instanceof RTLOperationLogic) {
						RTLOperationLogic l = (RTLOperationLogic) driver;

						if (l.getOp() == LogicOp.NOT) {

							RTLSignal s = l.getA().getSignal();
							if (s == as) {
								sInv = s;
							}
						}
					}
				}

				if (sInv != null) {

					rtlg_.remove(logic);

					rtlg_.sigJoin(rtlg_.placeOne(zs.getType(), logic.getSource()), zs, logic.getSource());

					changed = true;

				}

			}

		}

		return changed;
	}

	private static boolean mergeEquivalentModules(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (module instanceof RTLGraph || module instanceof RTLLiteral || module instanceof RTLPortModule || module instanceof ZILInterpreter)
				continue;

			for (int j = i + 1; j < rtlg_.getNumSubs(); j++) {

				RTLModule module2 = rtlg_.getSub(j);

				if (module.equals(module2)) {

					logger.info("Optimizer: Candidates for merging: %s and %s", module, module2);

					int n = module.getNumPorts();
					for (int k = 0; k < n; k++) {

						RTLPort p1 = module.getPort(k);

						if (p1.getDirection() == PortDir.IN)
							continue;

						RTLPort p2 = module2.getPort(k);

						RTLSignal s1 = p1.getSignal();
						RTLSignal s2 = p2.getSignal();

						if (s2 == null)
							continue;

						if (s1 == null) {

							p1.setSignal(s2);

						} else {

							rtlg_.sigJoin(s1, s2, module2.getSource());

						}
					}

					rtlg_.remove(module2);

					changed = true;
				}
			}
		}

		return changed;
	}

	private static boolean mergeRecordSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		int i = 0;
		while (i < rtlg_.getNumSignals()) {
			RTLSignal s = rtlg_.getSignal(i);

			int m = s.getNumConns();
			RTLRecordSel csel = null;
			for (int j = 0; j < m; j++) {

				RTLPort p = s.getConn(j);
				if (p.getDirection() != PortDir.IN)
					continue;

				RTLModule module = p.getModule();
				if (!(module instanceof RTLRecordSel)) {
					continue;
				}

				if (csel == null) {
					csel = (RTLRecordSel) module;
				} else {

					RTLRecordSel csel2 = (RTLRecordSel) module;

					int nOutputs = csel2.getNumOutputs();
					for (int iOutput = 0; iOutput < nOutputs; iOutput++) {

						RTLPort output = csel2.getOutput(iOutput);
						RTLSignal sOutput = output.getSignal();
						String id = csel2.getOutputId(iOutput);

						rtlg_.remove(csel2);

						csel.connectOutput(id, sOutput, csel2.getSource());
					}

					changed = true;
				}
			}
			i++;
		}

		return changed;
	}

	private static boolean replaceConstantRecordSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetRecordSel))
				continue;

			RTLTargetRecordSel rs = (RTLTargetRecordSel) module;

			// constant enable ?

			RTLPort ePort = rs.getE();
			RTLSignal e = ePort.getSignal();
			ZILValue enable = constantSignals.get(e);
			if (enable == null)
				continue;

			RTLPort dPort = rs.getD();
			RTLSignal dSignal = dPort.getSignal();
			RTLPort zPort = rs.getZ();
			RTLSignal zs = zPort.getSignal();
			RTLPort zePort = rs.getZE();
			RTLSignal zes = zePort.getSignal();

			rtlg_.remove(rs);
			RTLTargetRecordCSel nrs = new RTLTargetRecordCSel(dPort.getType(), zPort.getType(), rtlg_, null, rs.getSource());
			rtlg_.add(nrs);

			nrs.getZ().setSignal(zs);
			nrs.connectInput(rs.getId(), dSignal, rs.getSource());
			nrs.getZE().setSignal(zes);

			changed = true;
		}

		return changed;
	}

	private static boolean mergeTargetArrayCSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetEMux))
				continue;

			RTLTargetEMux emux = (RTLTargetEMux) module;

			// driven by two csels ?

			RTLPort d1Port = emux.getD1();
			RTLSignal d1Signal = d1Port.getSignal();
			if (d1Signal == null) {
				logger.error("Optimizer: RTLTargetEMux '%s': d1 port not connected.", emux);
				continue;
			}
			RTLPort driver1Port = d1Signal.getDriver(0);
			if (driver1Port == null)
				continue;
			RTLModule driver1 = driver1Port.getModule();
			if (!(driver1 instanceof RTLTargetArrayCSel))
				continue;
			RTLTargetArrayCSel csel1 = (RTLTargetArrayCSel) driver1;

			RTLPort d2Port = emux.getD2();
			RTLSignal d2Signal = d2Port.getSignal();
			if (d2Signal == null) {
				logger.error("Optimizer: RTLTargetEMux '%s': d2 port not connected.", emux);
				continue;
			}
			RTLPort driver2Port = d2Signal.getDriver(0);
			if (driver2Port == null)
				continue;
			RTLModule driver2 = driver2Port.getModule();
			if (!(driver2 instanceof RTLTargetArrayCSel))
				continue;
			RTLTargetArrayCSel csel2 = (RTLTargetArrayCSel) driver2;

			RTLPort qPort = emux.getZ();
			RTLSignal qs = qPort.getSignal();
			RTLPort qePort = emux.getZE();
			RTLSignal qes = qePort.getSignal();

			rtlg_.remove(emux);
			rtlg_.remove(driver2);

			int m = csel2.getNumInputs();
			for (int j = 0; j < m; j++) {

				RTLPort input = csel2.getInput(j);
				int offset = csel2.getInputOffset(j);

				csel1.connectInput(offset, input.getSignal(), csel2.getSource());
			}

			csel1.getZ().setSignal(qs);
			csel1.getZE().setSignal(qes);

			changed = true;
		}

		return changed;
	}

	private static boolean mergeArrayCSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSignals(); i++) {
			RTLSignal s = rtlg_.getSignal(i);

			int m = s.getNumConns();
			RTLArrayCSel csel = null;
			for (int j = 0; j < m; j++) {

				RTLPort p = s.getConn(j);
				if (p.getDirection() != PortDir.IN)
					continue;

				RTLModule module = p.getModule();
				if (!(module instanceof RTLArrayCSel)) {
					continue;
				}

				if (csel == null) {
					csel = (RTLArrayCSel) module;
				} else {

					RTLArrayCSel csel2 = (RTLArrayCSel) module;

					int nOutputs = csel2.getNumOutputs();
					for (int iOutput = 0; iOutput < nOutputs; iOutput++) {

						RTLPort output = csel2.getOutput(iOutput);
						RTLSignal sOutput = output.getSignal();
						int off = csel2.getOutputOffset(iOutput);

						csel.connectOutput(off, sOutput, csel2.getSource());
					}

					rtlg_.remove(csel2);

					changed = true;
				}

			}

		}

		return changed;
	}

	private static boolean removeDangling(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) {

		boolean changed = false;

		// small hack for testbenches
		if (rtlg_.getNumPorts() == 0) {
			return false;
		}

		int i = 0;
		while (i < rtlg_.getNumSignals()) {
			RTLSignal s = rtlg_.getSignal(i);

			int nReaders = s.getNumReaders();
			int nDrivers = s.getNumDrivers();
			if (nReaders > 0 && nDrivers > 0) {
				i++;
				continue;
			}

			while (s.getNumConns() > 0) {
				RTLPort port = s.getConn(0);
				port.setSignal(null);
			}

			try {
				rtlg_.remove(s);
			} catch (ZamiaException e) {
				el.logException(e);
			}

			changed = true;
		}

		i = 0;
		while (i < rtlg_.getNumSubs()) {

			RTLModule module = rtlg_.getSub(i);

			if (module instanceof RTLPortModule) {
				i++;
				continue;
			}

			boolean connectedOutputs = false;

			int nPorts = module.getNumPorts();
			for (int j = 0; j < nPorts; j++) {

				RTLPort p = module.getPort(j);

				if (p.getDirection() == PortDir.IN)
					continue;

				if (p.getSignal() != null) {
					connectedOutputs = true;
					break;
				}

			}

			if (!connectedOutputs) {
				try {
					rtlg_.remove(module);
				} catch (ZamiaException e) {
					el.logException(e);
				}
			} else {
				i++;
			}
		}
		return changed;
	}

	private static boolean replaceConstantMuxs(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetEMux))
				continue;

			RTLTargetEMux emux = (RTLTargetEMux) module;

			// driven by two ces ?

			RTLPort e1Port = emux.getE1();
			RTLSignal e1Signal = e1Port.getSignal();
			if (e1Signal == null)
				continue;

			RTLPort driver1Port = e1Signal.getDriver(0);
			if (driver1Port == null)
				continue;
			RTLModule driver1 = driver1Port.getModule();
			if (!(driver1 instanceof RTLCE))
				continue;
			RTLCE ce1 = (RTLCE) driver1;

			RTLPort e2Port = emux.getE2();
			RTLSignal e2Signal = e2Port.getSignal();
			if (e2Signal == null)
				continue;

			RTLPort driver2Port = e2Signal.getDriver(0);
			if (driver2Port == null)
				continue;
			RTLModule driver2 = driver2Port.getModule();
			if (!(driver2 instanceof RTLCE))
				continue;
			RTLCE ce2 = (RTLCE) driver2;

			RTLPort d1Port = emux.getD1();
			RTLSignal d1Signal = d1Port.getSignal();
			if (d1Signal == null)
				continue;

			RTLPort d2Port = emux.getD2();
			RTLSignal d2Signal = d2Port.getSignal();
			if (d2Signal == null)
				continue;

			RTLPort d1ePort = ce1.getE();
			RTLSignal d1es = d1ePort.getSignal();

			RTLPort d2ePort = ce2.getE();
			RTLSignal d2es = d2ePort.getSignal();

			RTLPort qPort = emux.getZ();
			RTLSignal qs = qPort.getSignal();
			RTLPort qePort = emux.getZE();
			RTLSignal qes = qePort.getSignal();

			rtlg_.remove(emux);
			rtlg_.remove(driver1);
			rtlg_.remove(driver2);
			RTLMux cmux = new RTLMux(d1ePort.getType(), rtlg_, null, emux.getSource());
			rtlg_.add(cmux);

			cmux.getD1().setSignal(d1Signal);
			cmux.getD2().setSignal(d2Signal);
			cmux.getS().setSignal(d2es);
			cmux.getZ().setSignal(qs);

			RTLSignal enable = rtlg_.placeOperationLogic(LogicOp.OR, d1es, d2es, emux.getSource());

			RTLCE ce = new RTLCE(qePort.getType(), rtlg_, null, emux.getSource());
			rtlg_.add(ce);

			RTLPort ceEPort = ce.getE();
			ceEPort.setSignal(enable);
			RTLPort ceZEPort = ce.getZE();
			ceZEPort.setSignal(qes);

			changed = true;
		}

		return changed;
	}

	private static boolean replaceConstantEnableMuxs(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetEMux))
				continue;

			RTLTargetEMux emux = (RTLTargetEMux) module;

			// e1 constant ?

			RTLPort e1Port = emux.getE1();
			RTLSignal e1Signal = e1Port.getSignal();

			ZILValue v = constantSignals.get(e1Signal);

			if (v != null && v.isAllOnes()) {

				RTLPort d1Port = emux.getD1();
				RTLSignal d1 = d1Port.getSignal();

				RTLPort zPort = emux.getZ();
				RTLSignal z = zPort.getSignal();

				RTLPort zePort = emux.getZE();
				RTLSignal ze = zePort.getSignal();

				rtlg_.remove(emux);

				rtlg_.sigJoin(d1, z, emux.getSource());

				rtlg_.sigJoin(rtlg_.placeOne(ze.getType(), emux.getSource()), ze, emux.getSource());

				changed = true;

			} else {

				// e2 constant and e1 driven by CE ?

				RTLModule driver = getDriver(e1Port);
				if (driver instanceof RTLCE) {

					RTLCE ce = (RTLCE) driver;

					RTLPort e2Port = emux.getE2();
					RTLSignal e2Signal = e2Port.getSignal();

					v = constantSignals.get(e2Signal);

					if (v != null && v.isAllOnes()) {

						RTLPort d1Port = emux.getD1();
						RTLSignal d1 = d1Port.getSignal();

						RTLPort d2Port = emux.getD2();
						RTLSignal d2 = d2Port.getSignal();

						RTLPort zPort = emux.getZ();
						RTLSignal z = zPort.getSignal();

						RTLPort zePort = emux.getZE();
						RTLSignal ze = zePort.getSignal();

						RTLPort sPort = ce.getE();
						RTLSignal s = sPort.getSignal();

						rtlg_.remove(emux);
						rtlg_.remove(ce);

						RTLMux mux = new RTLMux(z.getType(), rtlg_, null, emux.getSource());
						rtlg_.add(mux);

						mux.getD1().setSignal(d2);
						mux.getD2().setSignal(d1);
						mux.getS().setSignal(s);
						mux.getZ().setSignal(z);

						rtlg_.sigJoin(rtlg_.placeOne(ze.getType(), emux.getSource()), ze, emux.getSource());

						changed = true;
					}

				}

			}

		}

		return changed;
	}

	private static boolean replaceConstantConds(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetCond))
				continue;

			RTLTargetCond cond = (RTLTargetCond) module;

			// constant enable ?

			RTLPort ePort = cond.getE();
			RTLSignal s = ePort.getSignal();
			ZILValue enableS = constantSignals.get(s);
			if (enableS == null)
				continue;

			RTLPort dPort = cond.getD();
			RTLSignal ds = dPort.getSignal();
			RTLPort cPort = cond.getC();
			RTLSignal cs = cPort.getSignal();
			RTLPort zPort = cond.getZ();
			RTLSignal zs = zPort.getSignal();
			RTLPort zePort = cond.getZE();
			RTLSignal zes = zePort.getSignal();

			rtlg_.remove(cond);

			rtlg_.sigJoin(ds, zs, cond.getSource());

			RTLCE ce = new RTLCE(zes.getType(), rtlg_, null, cond.getSource());
			rtlg_.add(ce);
			ce.getE().setSignal(cs);
			ce.getZE().setSignal(zes);

			//			RTLTargetCCond ccond = new RTLTargetCCond(dPort.getType(), rtlg_, null, cond.getSource());
			//			rtlg_.add(ccond);
			//
			//			ccond.getD().setSignal(ds);
			//			ccond.getC().setSignal(cs);
			//			ccond.getZ().setSignal(zs);
			//			ccond.getZE().setSignal(zes);

			changed = true;
		}

		return changed;
	}

	private static boolean replaceConstantMathOps(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLOperationMath))
				continue;

			RTLOperationMath rtlMath = (RTLOperationMath) module;

			// constant inputs ?

			RTLPort aPort = rtlMath.getA();
			RTLSignal sA = aPort.getSignal();
			ZILValue vA = constantSignals.get(sA);
			if (vA == null)
				continue;

			RTLPort bPort = rtlMath.getB();
			RTLSignal sB = bPort.getSignal();
			ZILValue vB = null;
			if (sB != null) {
				vB = constantSignals.get(sB);
				if (vB == null)
					continue;
			}

			// compute result
			
			ZILValue vZ = ZILValue.computeMath(vA, vB, rtlMath.getOp(), rtlMath.getSource());
			
			// remove math op
			
			RTLPort zPort = rtlMath.getZ();
			RTLSignal zs = zPort.getSignal();

			rtlg_.remove(rtlMath);

			// place constant
			
			RTLSignal ls = rtlg_.placeLiteral(vZ, rtlMath.getSource());
			
			rtlg_.sigJoin(ls, zs, rtlMath.getSource());

			changed = true;
		}

		return changed;
	}

	private static boolean replaceConstantTargetArraySels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetArraySel))
				continue;

			RTLTargetArraySel as = (RTLTargetArraySel) module;

			// constant index ?

			RTLPort sPort = as.getS();
			RTLSignal s = sPort.getSignal();
			ZILValue idx = constantSignals.get(s);
			if (idx == null)
				continue;

			RTLPort ePort = as.getE();
			s = ePort.getSignal();
			ZILValue c = constantSignals.get(s);
			if (c == null)
				continue;

			if (dump)
				System.out.println("Got candidate for replacement: " + as + ", index: " + idx);

			RTLPort dPort = as.getD();
			RTLSignal ds = dPort.getSignal();
			RTLPort qPort = as.getZ();
			RTLSignal qs = qPort.getSignal();
			RTLPort qePort = as.getZE();
			RTLSignal qes = qePort.getSignal();

			rtlg_.remove(as);
			RTLTargetArrayCSel cs = new RTLTargetArrayCSel(dPort.getType(), qPort.getType(), rtlg_, null, as.getSource());
			rtlg_.add(cs);

			cs.getZ().setSignal(qs);
			cs.getZE().setSignal(qes);

			cs.addInput(idx.getInt(as.getSource()), as.getSource()).setSignal(ds);

			changed = true;
		}

		return changed;
	}

	private static boolean replaceCompleteTargetRecordSels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetRecordCSel))
				continue;

			RTLTargetRecordCSel as = (RTLTargetRecordCSel) module;

			if (!as.isComplete())
				continue;

			RTLPort zPort = as.getZ();
			RTLSignal z = zPort.getSignal();

			RTLPort zePort = as.getZE();
			RTLSignal ze = zePort.getSignal();

			RTLTargetRecord cs = new RTLTargetRecord(as.getTIn(), zPort.getType(), rtlg_, null, as.getSource());
			rtlg_.add(cs);

			int nInputs = as.getNumInputs();
			for (int j = 0; j < nInputs; j++) {
				RTLPort p = as.getInput(j);
				cs.connectInput(as.getInputId(j), p.getSignal(), as.getSource());
			}

			rtlg_.remove(as);

			cs.getZ().setSignal(z);

			rtlg_.sigJoin(rtlg_.placeOne(ze.getType(), as.getSource()), ze, as.getSource());

			changed = true;
		}

		return changed;
	}

	private static boolean replaceCompleteTargetArraySels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLTargetArrayCSel))
				continue;

			RTLTargetArrayCSel as = (RTLTargetArrayCSel) module;

			if (!as.isComplete())
				continue;

			RTLPort zPort = as.getZ();
			RTLSignal z = zPort.getSignal();

			RTLPort zePort = as.getZE();
			RTLSignal ze = zePort.getSignal();

			RTLTargetArray cs = new RTLTargetArray(as.getTIn(), (ZILTypeArray) zPort.getType(), rtlg_, null, as.getSource());
			rtlg_.add(cs);

			int nInputs = as.getNumInputs();
			for (int j = 0; j < nInputs; j++) {
				RTLPort p = as.getInput(j);
				cs.connectInput(as.getInputOffset(j), p.getSignal(), as.getSource());
			}

			rtlg_.remove(as);

			cs.getZ().setSignal(z);

			rtlg_.sigJoin(rtlg_.placeOne(ze.getType(), as.getSource()), ze, as.getSource());

			changed = true;
		}

		return changed;
	}

	private static boolean replaceConstantArraySels(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean changed = false;

		for (int i = 0; i < rtlg_.getNumSubs(); i++) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLArraySel))
				continue;

			RTLArraySel as = (RTLArraySel) module;

			// constant index ?

			RTLPort sPort = as.getS();
			RTLSignal s = sPort.getSignal();
			ZILValue idx = constantSignals.get(s);
			if (idx == null)
				continue;

			RTLPort aPort = as.getA();
			RTLSignal aSignal = aPort.getSignal();
			RTLPort qPort = as.getZ();
			RTLSignal qs = qPort.getSignal();

			rtlg_.remove(as);
			RTLArrayCSel cs = new RTLArrayCSel(aPort.getType(), sPort.getType(), qPort.getType(), rtlg_, null, as.getSource());
			rtlg_.add(cs);

			cs.getD().setSignal(aSignal);

			cs.addOutput(idx.getInt(as.getSource()), as.getSource()).setSignal(qs);

			changed = true;
		}

		return changed;
	}

	private static boolean removeLiterals(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean foundNew = false;

		logger.info("Optimizer: removingLiterals()");

		int i = 0;
		while (i < rtlg_.getNumSubs()) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLLiteral)) {
				i++;
				continue;
			}

			RTLLiteral literal = (RTLLiteral) module;

			RTLPort z = literal.getZ();
			RTLSignal s = z.getSignal();

			if (s == null || s.getNumReaders() == 0) {
				logger.debug("Optimizer: removing literal " + literal.getInstanceName() + ", s=" + s);

				rtlg_.remove(literal);

				foundNew = true;

			} else {

				// look for literals of same value

				ZILValue v = literal.getValue();

				int j = 0;
				while (j < rtlg_.getNumSubs()) {

					RTLModule module2 = rtlg_.getSub(j);

					if (!(module2 instanceof RTLLiteral) || module2 == module) {
						j++;
						continue;
					}

					RTLLiteral literal2 = (RTLLiteral) module2;

					ZILValue v2 = literal2.getValue();

					if (v.equals(v2)) {

						RTLPort z2 = literal2.getZ();
						RTLSignal s2 = z2.getSignal();

						rtlg_.remove(literal2);

						rtlg_.sigJoin(s, s2, literal2.getSource());

						foundNew = true;

					} else {
						j++;
					}
				}

				i++;
			}
		}

		return foundNew;
	}

	private static boolean removeLatches(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) throws ZamiaException {

		boolean foundNew = false;

		// warning: num subs may change during loop run because of
		// latch removal
		int i = 0;
		while (i < rtlg_.getNumSubs()) {

			RTLModule module = rtlg_.getSub(i);

			if (!(module instanceof RTLRegister)) {
				i++;
				continue;
			}

			RTLRegister reg = (RTLRegister) module;

			if (reg.getSyncData().getSignal() != null) {
				i++;
				continue;
			}

			ZILValue c = constantSignals.get(reg.getASyncEnable().getSignal());

			if (c == null) {
				i++;
				continue;
			}

			if (dump)
				System.out.println("Removing latch: " + reg);
			foundNew = true;

			RTLSignal d = reg.getASyncData().getSignal();
			RTLSignal q = reg.getZ().getSignal();
			rtlg_.remove(reg);

			rtlg_.sigJoin(d, q, null);
		}

		return foundNew;
	}

	private static boolean findConstantSignals(RTLGraph rtlg_, HashMap<RTLSignal, ZILValue> constantSignals) {

		boolean foundNew = false;

		int n = rtlg_.getNumSignals();
		for (int i = 0; i < n; i++) {
			RTLSignal s = rtlg_.getSignal(i);

			int m = s.getNumConns();
			RTLPort driver = null;
			for (int j = 0; j < m; j++) {

				RTLPort p = s.getConn(j);
				if (p.getDirection() == PortDir.IN)
					continue;

				if (driver != null) {
					driver = null;
					break;
				}
				driver = p;
			}

			if (driver != null) {

				RTLModule module = driver.getModule();

				if (module instanceof RTLLiteral) {
					RTLLiteral literal = (RTLLiteral) module;

					if (!constantSignals.containsKey(s)) {
						constantSignals.put(s, literal.getValue());
						foundNew = true;
						if (dump)
							System.out.println("new constant signal found: " + s + " is constant " + literal.getValue());
					}
				}
			}
		}
		return foundNew;
	}

}
