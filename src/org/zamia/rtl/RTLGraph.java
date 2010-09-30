/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.rtl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.zamia.ComponentStub;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.SignalDeclaration;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.vhdl.ast.OperationMath.MathOp;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeRecord;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLGraph extends RTLModule {

	class PortModuleMapping implements Serializable {
		public RTLPortModule module;

		public RTLPort port;

		public PortModuleMapping(RTLPortModule module_, RTLPort port_) {
			port = port_;
			module = module_;
		}

		public PortModuleMapping() {
		}
	}

	private HashMap<RTLPort, PortModuleMapping> pgm;

	private HashMapArray<String, RTLModule> subs;

	private HashMapArray<String, RTLSignal> signals; /*
								 * of String -> Signal; may contain same signals
								 * with different ids
								 */

	// these ids will be used to create names for
	// "unnamed" signals (signals that have not been
	// given a name by the user, that is)
	private int uniqueSignalCnt, uniqueModuleCnt, uniqueId;

	private String className;

	private Architecture arch;

	public final static boolean enableSanityChecks = false;

	public RTLGraph(String instanceName_, String className_, ASTObject src_, Architecture arch_) {
		super(null, instanceName_, src_);
		className = className_;
		arch = arch_;
		clear();
	}

	public void add(RTLModule module_) throws ZamiaException {

		String id = module_.getInstanceName();
		if (!(module_ instanceof RTLPortModule)) {
			String uid = getUniqueId(id);
			if (id != uid) {
				id = uid;
				module_.setInstanceName(id);
			}
		}

		subs.put(id, module_);
		module_.setParent(this);
	}

	public void add(RTLSignal signal_) throws ZamiaException {

		String id = signal_.getId();
		String uid = getUniqueId(id);
		if (id != uid) {
			id = uid;
			signal_.setId(id);
		}

		if (id != null)
			signals.put(signal_.getId(), signal_);
	}

	public void remove(RTLSignal s_) throws ZamiaException {
		signals.remove(s_.getId());
	}

	public void remove(RTLPort p) throws ZamiaException {
		super.remove(p);

		PortModuleMapping mapping = (PortModuleMapping) pgm.get(p);
		if (mapping == null)
			return; // apparently not a primary port
		RTLPortModule pg = mapping.module;
		pgm.remove(p);
		remove(pg, true);
		RTLSignal s = pg.getInternalPort().getSignal();
		if (s != null)
			s.setPort(null);
	}

	public void remove(RTLModule module_) throws ZamiaException {
		remove(module_, true);
	}

	public void remove(RTLModule module_, boolean removeConnections_) throws ZamiaException {

		// if this is a port gate we also remove the corresponding
		// primary port here
		if (module_ instanceof RTLPortModule) {
			RTLPortModule pg = (RTLPortModule) module_;
			RTLPort p = pg.getExternalPort();
			remove(p);
		}

		if (removeConnections_) {
			for (int i = 0; i < module_.getNumPorts(); i++) {
				RTLPort port = module_.getPort(i);
				RTLSignal s = port.getSignal();
				if (s != null)
					s.removePortConn(port);
			}
		}

		// remove module itself

		subs.remove(module_.getInstanceName());
	}

	@Override
	public String getClassName() {
		return className;
	}

	public RTLSignal findSignal(String id_) {
		RTLSignal sig = signals.get(id_);
		if (sig != null)
			return sig.getCurrent();
		return null;
	}

	public RTLModule findSub(String id_) {
		return subs.get(id_);
	}

	public boolean contains(RTLModule g_) {
		return subs.containsValue(g_);
	}

	public boolean contains(RTLSignal s_) {
		return signals.containsValue(s_);
	}

	/**
	 * Connect two given signals a_ and b_. If possible, that is when at least
	 * one of the signals is not a port and is not part of a named bus (we'll
	 * call this signal the victim and the other one the resulting signal from
	 * now on), this is done by adding all connections and ids from the victim
	 * signal to the resulting signal and deleting the victim signal.
	 * 
	 * If both signals cannot be deleted, the connection is constructed by
	 * placing a buf module between the signals. Because of this it is very
	 * important that a_ is the driving signal and b_ is the receiving signal
	 * when calling this function. A bidirectional connection between the two
	 * signals will not be implemented in this case!
	 * 
	 * @param drtiving_
	 *            driving signal
	 * @param receiving_
	 *            receiving signal
	 * @param src_
	 *            Source code reference for error reporting
	 * @return the resulting signal
	 * @throws ZamiaException
	 */

	public RTLSignal sigJoin(RTLSignal driving_, RTLSignal receiving, ASTObject src_) throws ZamiaException {

		ZILType drivingT = driving_.getType().simplify();
		ZILType receivingT = receiving.getType().simplify();

		if (!drivingT.isCompatible(receivingT))
			throw new ZamiaException("join type mismatch " + drivingT + " <-> " + receivingT + ", signals: " + driving_ + ", " + receiving, src_);

		while (driving_.getReplacedBy() != null)
			driving_ = driving_.getReplacedBy();
		while (receiving.getReplacedBy() != null)
			receiving = receiving.getReplacedBy();

		if (enableSanityChecks) {
			if (!contains(driving_)) {
				System.out.println("ERROR: signal a=" + driving_ + " to be joined is not part of this netlist!!");
			}
			if (!contains(receiving)) {
				System.out.println("ERROR: signal b=" + receiving + " to be joined is not part of this netlist!!");
			}
		}

		if (driving_ == receiving)
			return driving_;

		// can we replace at least one of the two signals?
		boolean aRepl = (driving_.getPort() == null);
		boolean bRepl = (receiving.getPort() == null);

		if (!aRepl && !bRepl) {

			// ok, two port-signals => in this case we have to generate buf
			// modules

			RTLSignal result = placeOperationLogic(LogicOp.BUF, driving_, null, src_);
			return sigJoin(result, receiving, src_);

		} else {
			// at most one of them is a port signal
			// decide which one to replace and which one to keep

			// a simple improvement: 
			// if both signals are replaceable, try to preserve
			// user-provided signals ids (not generated us_* ids)
			if (aRepl && bRepl) {
				aRepl = driving_.getId().startsWith("us_");
			}

			RTLSignal res, victim;
			if (aRepl) {
				res = receiving;
				victim = driving_;
			} else {
				res = driving_;
				victim = receiving;
			}

			while (victim.getNumConns() > 0) {
				RTLPort p = victim.getConn(0);
				p.setSignal(res);
			}

			remove(victim);

			int n = victim.getNumIds();
			for (int i = 0; i < n; i++) {
				String id = victim.getId(i);
				signals.put(id, res);
				res.addId(id);
			}

			ZILValue iv = victim.getInitialValue();
			if (iv != null)
				res.setInitialValue(iv);

			int m = res.getNumConns();
			for (int j = 0; j < m; j++) {
				RTLPort conn = res.getConn(j);

				if (conn.getSignal() != res)
					System.out.println("Signal '" + res + "' claims to be connected to " + conn.getId() + " but in fact the port is connected to " + conn.getSignal());

			}
			ASTObject src = victim.getSource();
			if (src instanceof SignalDeclaration)
				res.setSource(src);

			victim.setReplacedBy(res);

			return res;
		}
	}

	public RTLSignal placeOperationMath(MathOp op_, RTLSignal a_, RTLSignal b_, ZILType outType_, ASTObject src_) throws ZamiaException {

		RTLOperationMath module = new RTLOperationMath(op_, a_.getType(), outType_, this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(a_.getCurrent());
		if (b_ != null) {
			RTLPort pb = module.getB();
			pb.setSignal(b_.getCurrent());
		}

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal placeOperationLogic(LogicOp op_, RTLSignal a_, RTLSignal b_, ASTObject src_) throws ZamiaException {

		RTLOperationLogic module = new RTLOperationLogic(op_, a_.getType(), this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(a_.getCurrent());
		if (b_ != null) {
			RTLPort pb = module.getB();
			pb.setSignal(b_.getCurrent());
		}

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal placeComparator(CompareOp op_, RTLSignal a_, RTLSignal b_, ASTObject src_) throws ZamiaException {

		RTLComparator module = new RTLComparator(op_, a_.getType(), this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(a_.getCurrent());

		RTLPort pb = module.getB();
		pb.setSignal(b_.getCurrent());

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public RTLArrayAggregate placeArrayAggregate(ZILTypeArray resType_, ASTObject src_) throws ZamiaException {

		RTLArrayAggregate module = new RTLArrayAggregate(resType_, this, null, src_);
		add(module);

		return module;
	}

	public RTLRecordAggregate placeRecordAggregate(ZILTypeRecord resType_, ASTObject src_) throws ZamiaException {

		RTLRecordAggregate module = new RTLRecordAggregate(resType_, this, null, src_);
		add(module);

		return module;
	}

	public RTLOperationConcat placeConcat(RTLSignal a_, RTLSignal b_, RTLSignal res_, ASTObject src_) throws ZamiaException {

		RTLOperationConcat module = new RTLOperationConcat(a_.getType(), b_.getType(), res_.getType(), this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(a_);
		RTLPort pb = module.getB();
		pb.setSignal(b_);
		RTLPort pz = module.getZ();
		pz.setSignal(res_);

		return module;
	}

	public RTLTypeCast placeOperationTypeCast(RTLSignal a_, RTLSignal result_, ASTObject src_) throws ZamiaException {

		RTLTypeCast module = new RTLTypeCast(a_.getType(), result_.getType(), this, null, src_);
		add(module);

		RTLPort p = module.getA();
		p.setSignal(a_);
		p = module.getZ();
		p.setSignal(result_);

		return module;
	}

	public RTLSignal placeBitSel(RTLSignal s_, int offset_, int width_, ZILType resType_, ASTObject src_) throws ZamiaException {
		RTLBitSel module = new RTLBitSel(s_.getType(), offset_, width_, resType_, this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(s_.getCurrent());

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public void placeArraySel(RTLSignal s_, RTLSignal sel_, RTLSignal result_, ZILType subType_, ASTObject src_) throws ZamiaException {
		RTLArraySel module = new RTLArraySel(s_.getType(), sel_.getType(), subType_, this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(s_.getCurrent());

		RTLPort ps = module.getS();
		ps.setSignal(sel_.getCurrent());

		RTLPort pz = module.getZ();
		pz.setSignal(result_);
	}

	public RTLSignal placeRecordSel(RTLSignal s_, String id_, ZILType subType_, ASTObject src_) throws ZamiaException {
		RTLRecordSel module = new RTLRecordSel(s_.getType(), subType_, this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(s_.getCurrent());

		RTLPort pz = module.addOutput(id_, src_);
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal placeArrayRangeSel(RTLSignal s_, int left_, int right_, boolean ascending_, ZILType resType_, ASTObject src_) throws ZamiaException {
		RTLArrayRangeSel module = new RTLArrayRangeSel(s_.getType(), left_, right_, ascending_, resType_, this, null, src_);
		add(module);

		RTLPort pa = module.getA();
		pa.setSignal(s_.getCurrent());

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal placeRegister(RTLSignal asyncD_, RTLSignal asyncE_, RTLSignal syncD_, RTLSignal syncE_, RTLSignal clk_, ASTObject src_) throws ZamiaException {

		ZILType t;
		if (asyncD_ != null)
			t = asyncD_.getType();
		else
			t = syncD_.getType();

		RTLRegister reg = new RTLRegister(this, t, null, src_);
		add(reg);

		RTLPort pz = reg.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);
		pz.setSignal(res);

		if (asyncD_ != null) {
			RTLPort p = reg.getASyncData();
			p.setSignal(asyncD_);

			if (asyncE_ != null) {
				p = reg.getASyncEnable();
				p.setSignal(asyncE_);
			}
		}

		if (syncD_ != null) {
			RTLPort p = reg.getSyncData();
			p.setSignal(syncD_);

			if (syncE_ != null) {
				p = reg.getSyncEnable();
				p.setSignal(syncE_);
			}

			p = reg.getClk();
			p.setSignal(clk_);
		}

		return res;
	}

	public RTLSignal placeLiteral(ZILValue v_, ASTObject src_) throws ZamiaException {

		RTLLiteral module = new RTLLiteral(v_, this, null, src_);

		add(module);

		RTLPort pz = module.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), src_);

		pz.setSignal(res);
		return res;
	}

	public RTLSignal placeOne(ZILType t_, ASTObject src_) throws ZamiaException {
		return placeLiteral(ZILValue.generateValue(ZILValue.BIT_1, t_.getEnableType(), null, src_), src_);
	}

	public RTLSignal placeZero(ZILType t_, ASTObject src_) throws ZamiaException {
		return placeLiteral(ZILValue.generateValue(ZILValue.BIT_0, t_.getEnableType(), null, src_), src_);
	}

	private RTLPortModule createPortModule(RTLPort p_, ASTObject src_) throws ZamiaException {
		RTLPortModule pg = null;
		RTLPort internalPort;

		switch (p_.getDirection()) {
		case IN:
			pg = new RTLInputPort(this, p_, src_);
			internalPort = pg.getInternalPort();
			break;
		case OUT:
			pg = new RTLOutputPort(this, p_, src_);
			internalPort = pg.getInternalPort();
			break;
		case BUFFER:
		case INOUT:
			pg = new RTLInoutPort(this, p_, src_);
			internalPort = pg.getInternalPort();
			break;
		default:
			throw new ZamiaException("RTL Graph: error, unknown dir " + p_.getDirection(), src_);
		}
		pgm.put(p_, new PortModuleMapping(pg, internalPort));
		RTLSignal s = new RTLSignal(this, p_.getId(), internalPort, p_.getType(), src_);
		internalPort.setSignal(s);
		add(s);
		add(pg);
		return pg;
	}

	public RTLPortModule getPortModule(RTLPort p_) {
		return pgm.get(p_).module;
	}

	@Override
	public RTLPort createPort(String id_, ZILType type_, PortDir direction_, ASTObject src_) throws ZamiaException {

		if (ports.containsKey(id_)) {
			throw new ZamiaException("Port " + id_ + " was already defined.", src_);
		}

		RTLPort port = new RTLPort(this, id_, type_, direction_);
		createPortModule(port, src_);
		add(port);
		return port;
	}

	public RTLSignal createSignal(String id_, ZILType type_, ASTObject src_) throws ZamiaException {

		// FIXME: remove sanity check
		if (type_.isOpen()) {
			throw new ZamiaException("Tried to create open typed signal.", src_);
		}

		RTLSignal signal = new RTLSignal(this, id_, null, type_, src_);

		add(signal);

		return signal;
	}

	public RTLSignal createUnnamedSignal(ZILType type_, ASTObject src_) throws ZamiaException {
		String id = getUnnamedSignalId();
		return createSignal(id, type_, src_);
	}

	public RTLSignalAE createUnnamedSignalAE(ZILType type_, ASTObject src_) throws ZamiaException {
		return new RTLSignalAE(createUnnamedSignal(type_, src_), createUnnamedSignal(type_.getEnableType(), src_));
	}

	public String getUnnamedSignalId() {
		String id = "us_" + uniqueSignalCnt++;
		while (signals.containsKey(id)) {
			id = "us_" + uniqueSignalCnt++;
		}
		return id;
	}

	public String getUniqueSignalId(String signalname_) {
		String ret = signalname_ + "_" + uniqueSignalCnt++;
		while (findSignal(ret) != null) {
			ret = signalname_ + "_" + uniqueSignalCnt++;
		}
		return ret;
	}

	public String getUniqueModuleId(String classname_) {
		String ret = classname_ + "_" + uniqueModuleCnt++;
		while (findSub(ret) != null) {
			ret = classname_ + "_" + uniqueModuleCnt++;
		}
		return ret;
	}

	public String getUniqueId(String id_) {
		String id = id_;
		while (signals.containsKey(id)) {
			id = id_ + "_" + uniqueId++;
		}
		while (subs.containsKey(id)) {
			id = id_ + "_" + uniqueId++;
		}
		return id;
	}

	public void clear() {
		super.clear();
		signals = new HashMapArray<String, RTLSignal>(10);
		subs = new HashMapArray<String, RTLModule>(1);
		pgm = new HashMap<RTLPort, PortModuleMapping>(10);
		uniqueSignalCnt = 0;
		uniqueModuleCnt = 0;
		uniqueId = 0;
	}

	public int getNumSubs() {
		return subs.size();
	}

	public int getNumSignals() {
		return signals.size();
	}

	public RTLSignal getSignal(int idx_) {
		return signals.get(idx_);
	}

	public int consistencyCheck() {

		int num_errs = 0;
		int n = getNumSignals();
		logger.info("Checking " + n + " signals for counter consistency...");
		for (int i = 0; i < n; i++) {

			RTLSignal s = getSignal(i);

			int m = s.getNumConns();
			int nDrivers = 0;
			int nReaders = 0;
			for (int j = 0; j < m; j++) {
				RTLPort conn = s.getConn(j);

				if (conn.getSignal() != s) {
					num_errs++;
					logger.error("ERROR: Signal '" + s + "' claims to be connected to " + conn.getId() + " but in fact the port is connected to " + conn.getSignal());
				}

				if (conn.getDirection() == PortDir.IN)
					nReaders++;
				else
					nDrivers++;

				RTLModule g = conn.getModule();
				RTLModule lookedUpRTLModule = findSub(g.getInstanceName());
				if (lookedUpRTLModule != g) {
					num_errs++;
					logger.error("ERROR:Signal claims to be connected to " + conn + " which is a port of " + g + " which is not part of this netlist!");
				}
			}
			//			if (nReaders != s.getNumReaders()) {
			//				num_errs++;
			//				err.println("Signal '" + s.getInstanceName()
			//						+ "' claims to have " + s.getNumReaders()
			//						+ " readers but in fact has " + nReaders);
			//			}
			//			if (nDrivers != s.getNumDrivers()) {
			//				num_errs++;
			//				err.println("Signal '" + s.getInstanceName()
			//						+ "' claims to have " + s.getNumDrivers()
			//						+ " drivers but in fact has " + nDrivers);
			//			}
			if (s.getCurrent() != s) {
				num_errs++;
				logger.error("ERROR: Signal '" + s.getId() + "' is outdated!");
			}
		}

		logger.info("Checking " + n + " signals for sourceless and dangling signals...");
		for (int i = 0; i < n; i++) {

			RTLSignal s = getSignal(i);
			if (s.getNumConns() < 1) {
				num_errs++;
				logger.error("ERROR: Signal '" + s.getId() + "' has no connections!");
				continue;
			}
			if (s.getNumDrivers() < 1) {
				num_errs++;
				logger.error("ERROR:Signal '" + s.getId() + "' has no drivers!");
			}
			if (s.getNumReaders() < 1) {
				num_errs++;
				logger.error("ERROR:Signal '" + s.getId() + "' is dangling!");
			}
		}

		n = getNumSubs();
		logger.info("Checking " + n + " subs for unconnected ports...");
		for (int i = 0; i < n; i++) {

			RTLModule m = getSub(i);
			int np = m.getNumPorts();
			for (int j = 0; j < np; j++) {
				RTLPort p = m.getPort(j);
				RTLSignal s = p.getSignal();
				if (s == null) {
					if (m.isPortMandatory(p)) {
						num_errs++;
						logger.error("ERROR: Port '" + p + "' is not connected!");
					}
				} else {
					if (!s.containsConn(p)) {
						num_errs++;
						logger.error("ERROR: Port '" + p + "' claims to be connected to " + s + " but the signal knows nothing about it!");
					}
				}
			}
		}
		n = getNumSubs();
		logger.info("Checking " + n + " subs for inconsistencies...");
		for (int i = 0; i < n; i++) {

			RTLModule g = getSub(i);

			if (g.getParent() != this) {
				logger.error("ERROR: Sub " + g + " is not part of this RTL graph!!");
			}

			if (!(g instanceof RTLGraph))
				continue;

			((RTLGraph) g).consistencyCheck();
		}

		n = getNumSignals();
		logger.info("Checking " + n + " signals for sub consistency...");
		for (int i = 0; i < n; i++) {

			RTLSignal s = getSignal(i);

			int m = s.getNumConns();
			for (int j = 0; j < m; j++) {
				RTLPort conn = s.getConn(j);

				RTLModule sub = conn.getModule();
				if (!subs.containsValue(sub)) {
					logger.error("ERROR: Sub " + sub + " connected to signal but not part of this netlist!");
				}

			}
		}
		return num_errs;
	}

	public RTLModule getSub(int i) {
		return subs.get(i);
	}

	//	public NetList dissolve() throws ZamiaException {
	//		throw new ZamiaException ("Internal error: unimplemented method called.");
	//	}

	public ArrayList<RTLModule> getReceivers(RTLModule module_) {

		ArrayList<RTLModule> receivers = new ArrayList<RTLModule>();

		for (Iterator<RTLPort> i = module_.getOutputPorts().iterator(); i.hasNext();) {
			RTLPort p = i.next();

			RTLSignal s = p.getSignal();
			if (s == null)
				continue;
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.OUT) {
					RTLModule r = dp.getModule();
					if (r != module_)
						receivers.add(r);
				}
			}
		}
		return receivers;
	}

	public ArrayList<RTLModule> getReceivers(RTLSignal signal_) {

		ArrayList<RTLModule> receivers = new ArrayList<RTLModule>();

		int n = signal_.getNumConns();
		for (int j = 0; j < n; j++) {
			RTLPort dp = signal_.getConn(j);
			if (dp.getDirection() != PortDir.OUT)
				receivers.add(dp.getModule());
		}
		return receivers;
	}

	public HashSet<RTLModule> getReceivers(RTLPort port_) {

		HashSet<RTLModule> receivers = new HashSet<RTLModule>();

		RTLSignal s = port_.getSignal();
		if (s != null) {
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.OUT)
					receivers.add(dp.getModule());
			}
		}
		return receivers;
	}

	public ArrayList<RTLModule> getDrivers(RTLModule module_) {

		ArrayList<RTLModule> drivers = new ArrayList<RTLModule>();

		for (Iterator<RTLPort> i = module_.getInputPorts().iterator(); i.hasNext();) {
			RTLPort p = i.next();

			RTLSignal s = p.getSignal();
			if (s == null)
				continue;
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.IN) {
					RTLModule r = dp.getModule();
					if (r != module_)
						drivers.add(r);
				}
			}
		}
		return drivers;
	}

	public int levelize(HashMap<RTLModule, Integer> moduleDepth, HashMap<Integer, ArrayList<RTLModule>> depthModule) {
		return levelize(moduleDepth, depthModule, null);
	}

	/**
	 * calculates the depth of each module and stores it into a HashMap. Also
	 * generates the reverse mapping of levels to lists of modules
	 * 
	 * @param moduleDepth_
	 *            RTLModule -> Integer mapping
	 * @param depthModule_
	 *            Integer -> ArrayList of RTLModules mapping
	 * @param visibleModules_
	 * @return maximum depth of graph
	 */
	public int levelize(HashMap<RTLModule, Integer> moduleDepth_, HashMap<Integer, ArrayList<RTLModule>> depthModule_, HashSet<RTLModule> visibleModules_) {

		// step 1: compute number of connected inputs per module
		// push those modules which do not have connected inputs
		// on the stack (inputmodules / literals)

		HashMap<RTLModule, Integer> numConnectedInputs = new HashMap<RTLModule, Integer>();
		LinkedList<RTLModule> queue = new LinkedList<RTLModule>();
		HashSet<RTLModule> todo = new HashSet<RTLModule>();
		// keep track of how often we have reached each module
		// and what the maximum logic depth was
		// when # reached == # connected inputs we know it's
		// logic depth and push it on the stack
		HashMap<RTLModule, Integer> reached = new HashMap<RTLModule, Integer>();

		for (int i = 0; i < getNumSubs(); i++) {
			RTLModule module = getSub(i);
			if (visibleModules_ != null && !visibleModules_.contains(module))
				continue;
			// System.out.println ("LEVELIZE Sub #"+i+" : "+module);

			int numCI = 0;
			ArrayList<RTLPort> inputs = module.getInputPorts();
			int numI = inputs.size();
			for (int j = 0; j < numI; j++) {
				RTLPort port = inputs.get(j);
				RTLSignal s = port.getSignal();
				if ((s != null) && (s.getNumDrivers() > 0)) {

					// a little hack to break feedback loops caused by registers
					if (numI > 1) {
						RTLModule driver = s.getDriver(0).getModule();
						if (!(driver instanceof RTLRegister)) {
							numCI++;
						}
					} else
						numCI++;
				}
			}
			numConnectedInputs.put(module, numCI);
			reached.put(module, 0);
			todo.add(module);
			if (numCI == 0) {
				queue.add(module);

				if (!(module instanceof RTLPortModule) && !(module instanceof RTLLiteral))
					moduleDepth_.put(module, 1);
				else
					moduleDepth_.put(module, 0);
			}
		}

		// step 2: recursively levelize remaining modules

		int maxDepth = 0;
		//		RTLModule end_of_longest_path = null;
		boolean placedFF = false;

		int numTodo = todo.size();
		int count = 0;
		int pold = 0;

		while (true) {

			int pc = 0;
			if (numTodo > 0)
				pc = count * 100 / numTodo;

			if (pc > pold) {
				pold = pc;
				//				System.out.print("\rLevelize: " + pc + "% done.");
			}
			count++;

			while (!queue.isEmpty()) {
				RTLModule module = queue.poll();
				if (!todo.contains(module)) {
					continue;
				}
				if ((module instanceof RTLRegister) && !placedFF) {
					continue;
				}

				int depth = ((Integer) moduleDepth_.get(module)).intValue();
				todo.remove(module);

				for (Iterator<RTLModule> irc = getReceivers(module).iterator(); irc.hasNext();) {
					RTLModule receiver = irc.next();
					if (visibleModules_ != null && !visibleModules_.contains(receiver))
						continue;
					if ((receiver != module) && (todo.contains(receiver))) {

						// calc depth of this receiver

						Integer i = (Integer) moduleDepth_.get(receiver);
						if (i == null) {
							moduleDepth_.put(receiver, depth + 1);
							if (depth >= maxDepth) {
								maxDepth = depth + 1;
								//								end_of_longest_path = receiver;
							}
						} else {
							int r_depth = i.intValue();
							if (depth >= r_depth) {
								moduleDepth_.put(receiver, depth + 1);
								if (depth >= maxDepth) {
									maxDepth = depth + 1;
									//									end_of_longest_path = receiver;
								}
							}
						}

						// how often have we reached this receiver?
						int numReached = reached.get(receiver);
						numReached++;
						reached.put(receiver, numReached);

						// how many connected inputs has this receiver?
						int numCI = numConnectedInputs.get(receiver);

						if (numCI == numReached)
							queue.add(receiver);
					}
				}

			}
			if (!todo.isEmpty()) {
				// there seem to be some feedbacks in this
				// graph, so we need to push modules in
				// (we want to levelize all modules)

				// have we placed the flipflops yet?
				if (placedFF) {
					// yes => push in those modules with the highest
					// number of alreadyconnected inputs

					int mostOftenReached = 0;

					for (Iterator<RTLModule> i = todo.iterator(); i.hasNext();) {
						RTLModule module = i.next();
						int numReached = reached.get(module);
						if (numReached > mostOftenReached)
							mostOftenReached = numReached;
					}

					for (Iterator<RTLModule> i = todo.iterator(); i.hasNext();) {
						RTLModule module = i.next();

						int numReached = reached.get(module);
						if (numReached == mostOftenReached) {

							queue.add(module);
							moduleDepth_.put(module, maxDepth);
						}
					}
				} else {
					// ah - perfect time to push in the flipflops
					// for well-formed circuits that should solve all
					// feedback problems

					for (Iterator<RTLModule> i = todo.iterator(); i.hasNext();) {
						RTLModule module = i.next();
						if (module instanceof RTLRegister) {
							queue.add(module);
							moduleDepth_.put(module, maxDepth);
						}
					}
					placedFF = true;
				}
			} else
				break;
		}

		//		if (end_of_longest_path != null)
		//			System.out.println("==> End of longest path:"
		//					+ end_of_longest_path.getInstanceName());

		// generates depthModule from moduleDepth
		// moduleDepth: module --> depth
		// depthModule: depth (integer) --> list of modules (arrayList)
		ArrayList<RTLModule> list = null;
		int n = getNumSubs();
		for (int i = 0; i < n; i++) {
			RTLModule module = getSub(i);
			Object depth = moduleDepth_.get(module);
			if (depth == null)
				continue;

			if (depthModule_.containsKey(depth)) {
				list = (ArrayList<RTLModule>) depthModule_.get(depth);
			} else {
				list = new ArrayList<RTLModule>();
			}
			list.add(module);
			depthModule_.put((Integer) depth, list);
		}

		return maxDepth;
	}

	public void cleanup() {

		int n = getNumSignals();
		for (int i = 0; i < n; i++) {
			RTLSignal s = getSignal(i);

			RTLSignal current = s.getCurrent();
			if (current != s) {
				System.out.println("Old signal detected: " + s);
			}

		}
	}

	public Architecture getArch() {
		return arch;
	}

	public void dump(PrintStream out) {

		for (int i = 0; i < getNumSubs(); i++) {
			RTLModule module = getSub(i);

			out.println();
			out.println("SUB #" + i + ": " + module);
			out.println();

			int nPorts = module.getNumPorts();
			for (int j = 0; j < nPorts; j++) {
				RTLPort p = module.getPort(j);
				out.println("  Port " + p + " connected to:");

				RTLSignal s = p.getSignal();
				if (s == null) {
					out.println("    open");
					out.println();
					continue;
				}

				int nConns = s.getNumConns();
				for (int k = 0; k < nConns; k++) {
					out.println("    " + s.getConn(k));
				}
				out.println();
			}
		}
	}

	public void printStats() {
		logger.info("\nStats of RTL Graph " + this + ":");
		logger.info("%8d Ports\n", getNumPorts());
		logger.info("%8d Modules\n", getNumSubs());
		logger.info("%8d Signals\n", getNumSignals());
	}

	@Override
	public boolean equals(RTLModule aModule) {
		return false;
	}

	public RTLSignal findSignal(PathName aSignalPathName, int aIdx) throws ZamiaException {
		
		int n = aSignalPathName.getNumSegments();
		if (aIdx == n-1) {
			RTLSignal s = signals.get(aSignalPathName.getSegment(aIdx));
			
			if (s==null) {
				throw new ZamiaException ("Couldn't find signal "+aSignalPathName);
			}
			
			return s;
		}

		RTLModule sub = subs.get(aSignalPathName.getSegment(aIdx));
		
		if (sub == null || !(sub instanceof RTLGraph)) {
			throw new ZamiaException ("Couldn't find signal "+aSignalPathName);
		}

		RTLGraph graph = (RTLGraph) sub;
		
		return graph.findSignal(aSignalPathName, aIdx+1);
	}

	/**
	 * replace stubs by actual rtl graphs
	 * 
	 * @param aZprj
	 * @throws ZamiaException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	
	public void link(ZamiaProject aZPrj) throws ZamiaException, IOException, ClassNotFoundException {
		
		throw new ZamiaException ("Sorry, RTL code is disabled for now.");
		
//		int i = 0;
//		while (i<getNumSubs()) {
//			
//			RTLModule module = getSub(i);
//			
//			if (!(module instanceof RTLComponent)) {
//				i++;
//				continue;
//			}
//			
//			RTLComponent component = (RTLComponent) module;
//
//			// FIXME: pass generics
//			
//			ComponentStub stub = component.getStub();
//			DUUID duuid = stub.getDUUID();
//			
//			RTLGraph subGraph = aZPrj.elaborate(duuid.getLibId(), duuid.getId(), duuid.getArchId());
//			
//			subGraph.link(aZPrj);
//			
//			// duplicate connections
//			
//			int nPorts = component.getNumPorts();
//			for (int iPort = 0; iPort<nPorts; iPort++) {
//				RTLPort port = component.getPort(iPort);
//				RTLPort newPort = subGraph.findPort(port.getId());
//				
//				if (newPort == null) {
//					throw new ZamiaException ("RTLGraph: link() : Internal error: couldn't find port "+port);
//				}
//				
//				RTLPortModule pm = subGraph.getPortModule(newPort);
//				
//				newPort = pm.getExternalPort();
//				
//				RTLSignal s = port.getSignal();
//				if (s != null) {
//					s.removePortConn(port);
//					newPort.setSignal(s);
//				}
//			}
//			
//			remove(component);
//			subGraph.setInstanceName(component.getInstanceName());
//			
//			add(subGraph);
//			
//		}
		
	}

}
