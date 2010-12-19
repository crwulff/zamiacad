/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.rtlng;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.rtlng.RTLPort.PortDir;
import org.zamia.rtlng.RTLValue.BitValue;
import org.zamia.rtlng.nodes.RTLNBinaryOp;
import org.zamia.rtlng.nodes.RTLNBinaryOp.BinaryOp;
import org.zamia.rtlng.nodes.RTLNLiteral;
import org.zamia.rtlng.nodes.RTLNRegister;
import org.zamia.rtlng.nodes.RTLNUnaryOp;
import org.zamia.rtlng.nodes.RTLNUnaryOp.UnaryOp;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLModule extends RTLNode {

	public final static boolean enableSanityChecks = false;

	private final String fSignature;

	private final String fUID;

	private HashMapArray<String, RTLNode> fNodes;

	private HashMapArray<String, RTLSignal> fSignals; /*
														* of String -> Signal; may contain same signals
														* with different ids
														*/

	// these ids will be used to create names for
	// "unnamed" signals (signals that have not been
	// given a name by the user, that is)
	private int fUniqueSignalCnt, fUniqueModuleCnt, fUniqueId;

	public RTLModule(String aSignature, String aUID, SourceLocation aLocation, ZDB aZDB) {
		super(null, null, aLocation, aZDB);
		fSignature = aSignature;
		fUID = aUID;
		clear();
	}

	public void add(RTLNode aNode) throws ZamiaException {

		String id = aNode.getInstanceName();
		String uid = getUniqueId(id);
		if (id != uid) {
			id = uid;
			aNode.setInstanceName(id);
		}

		fNodes.put(id, aNode);
		aNode.setParent(this);
	}

	public void add(RTLSignal aSignal) throws ZamiaException {

		String id = aSignal.getId();
		String uid = getUniqueId(id);
		if (id != uid) {
			id = uid;
			aSignal.setId(id);
		}

		if (id != null)
			fSignals.put(aSignal.getId(), aSignal);
	}

	public void remove(RTLSignal aSignal) throws ZamiaException {
		fSignals.remove(aSignal.getId());
	}

	public void remove(RTLNode aNode) throws ZamiaException {
		remove(aNode, true);
	}

	public void remove(RTLNode aNode, boolean aRemoveConnections) throws ZamiaException {

		if (aRemoveConnections) {
			for (int i = 0; i < aNode.getNumPorts(); i++) {
				RTLPort port = aNode.getPort(i);
				RTLSignal s = port.getSignal();
				if (s != null)
					s.removePortConn(port);
			}
		}

		// remove module itself

		fNodes.remove(aNode.getInstanceName());
	}

	public RTLSignal findSignal(String aId) {
		RTLSignal sig = fSignals.get(aId);
		if (sig != null)
			return sig.getCurrent();
		return null;
	}

	public RTLNode findNode(String aId) {
		return fNodes.get(aId);
	}

	public boolean contains(RTLNode aNode) {
		return fNodes.containsValue(aNode);
	}

	public boolean contains(RTLSignal aSignal) {
		return fSignals.containsValue(aSignal);
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
	 * @param aDriving
	 *            driving signal
	 * @param aReceiving
	 *            receiving signal
	 * @param aLocation
	 *            Source code reference for error reporting
	 * @return the resulting signal
	 * @throws ZamiaException
	 */

	public RTLSignal sigJoin(RTLSignal aDriving, RTLSignal aReceiving, SourceLocation aLocation) throws ZamiaException {

		RTLType drivingT = aDriving.getType();
		RTLType receivingT = aReceiving.getType();

		if (!drivingT.isCompatible(receivingT))
			throw new ZamiaException("join type mismatch " + drivingT + " <-> " + receivingT + ", signals: " + aDriving + ", " + aReceiving, aLocation);

		while (aDriving.getReplacedBy() != null)
			aDriving = aDriving.getReplacedBy();
		while (aReceiving.getReplacedBy() != null)
			aReceiving = aReceiving.getReplacedBy();

		if (enableSanityChecks) {
			if (!contains(aDriving)) {
				System.out.println("ERROR: signal a=" + aDriving + " to be joined is not part of this netlist!!");
			}
			if (!contains(aReceiving)) {
				System.out.println("ERROR: signal b=" + aReceiving + " to be joined is not part of this netlist!!");
			}
		}

		if (aDriving == aReceiving)
			return aDriving;

		// can we replace at least one of the two signals?
		boolean aRepl = (aDriving.getPort() == null);
		boolean bRepl = (aReceiving.getPort() == null);

		if (!aRepl && !bRepl) {

			// ok, two port-signals => in this case we have to generate buf
			// modules

			RTLSignal result = createComponentUnary(UnaryOp.BUF, aDriving, aLocation);
			return sigJoin(result, aReceiving, aLocation);

		} else {
			// at most one of them is a port signal
			// decide which one to replace and which one to keep
			// a simple improvement: 
			// if both signals are replaceable, try to preserve
			// user-provided signals ids (not generated us_* ids)
			if (aRepl && bRepl) {
				aRepl = aDriving.getId().startsWith("us_");
			}

			RTLSignal res, victim;
			if (aRepl) {
				res = aReceiving;
				victim = aDriving;
			} else {
				res = aDriving;
				victim = aReceiving;
			}

			while (victim.getNumConns() > 0) {
				RTLPort p = victim.getConn(0);
				p.setSignal(res);
			}

			remove(victim);

			int n = victim.getNumIds();
			for (int i = 0; i < n; i++) {
				String id = victim.getId(i);
				fSignals.put(id, res);
				res.addId(id);
			}

			RTLValue iv = victim.getInitialValue();
			if (iv != null)
				res.setInitialValue(iv);

			int m = res.getNumConns();
			for (int j = 0; j < m; j++) {
				RTLPort conn = res.getConn(j);

				if (conn.getSignal() != res)
					System.out.println("Signal '" + res + "' claims to be connected to " + conn.getId() + " but in fact the port is connected to " + conn.getSignal());

			}

			if (!victim.isAnonymous()) {
				res.setAnonymous(false);
				res.setSourceLocation(victim.computeSourceLocation());
			}

			victim.setReplacedBy(res);

			return res;
		}
	}

	public RTLSignal createComponentUnary(UnaryOp aOp, RTLSignal aA, SourceLocation aLocation) throws ZamiaException {

		RTLNUnaryOp node = new RTLNUnaryOp(aOp, aA.getType(), this, aLocation, getZDB());
		add(node);

		RTLPort pa = node.getA();
		pa.setSignal(aA.getCurrent());

		RTLPort pz = node.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), aLocation);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal createComponentBinary(BinaryOp aOp, RTLSignal aA, RTLSignal aB, SourceLocation aLocation) throws ZamiaException {

		RTLNBinaryOp node = new RTLNBinaryOp(aOp, aA.getType(), this, aLocation, getZDB());
		add(node);

		RTLPort pa = node.getA();
		pa.setSignal(aA.getCurrent());

		RTLPort pb = node.getB();
		pb.setSignal(aB.getCurrent());

		RTLPort pz = node.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), aLocation);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal createLiteral(RTLValue aValue, SourceLocation aLocation) throws ZamiaException {

		RTLNLiteral node = new RTLNLiteral(aValue, this, aLocation, getZDB());
		add(node);

		RTLPort pz = node.getZ();
		RTLSignal res = createUnnamedSignal(pz.getType(), aLocation);

		pz.setSignal(res);

		return res;
	}

	public RTLSignal createOne(RTLType aType, SourceLocation aLocation) throws ZamiaException {

		switch (aType.getCat()) {
		case BIT:

			RTLValueBuilder builder = new RTLValueBuilder(aType, aLocation, getZDB());

			builder.setBit(BitValue.BV_1);
			return createLiteral(builder.buildValue(), aLocation);

		default:
			// FIXME: implement arrays, records
			throw new ZamiaException("Sorry, not implemented yet.", aLocation);
		}
	}

	public RTLNRegister createRegister(RTLType aType, SourceLocation aLocation) throws ZamiaException {

		RTLNRegister node = new RTLNRegister(aType, this, aLocation, getZDB());
		add(node);

		return node;
	}

	@Override
	public RTLPort createPort(String aId, RTLType aType, PortDir aDir, SourceLocation aLocation) throws ZamiaException {

		if (fPorts.containsKey(aId)) {
			throw new ZamiaException("RTLModule: Internal error: Port " + aId + " was already defined", aLocation);
		}

		RTLPort port = new RTLPort(this, aId, aType, aDir, aLocation, getZDB());

		RTLSignal s = new RTLSignal(false, port.getInitialValue(), this, port.getId(), port, port.getType(), aLocation, getZDB());
		port.setSignal(s);
		add(s);
		add(port);

		return port;
	}

	public RTLSignal createSignal(String aId, RTLType aType, SourceLocation aLocation) throws ZamiaException {

		RTLSignal signal = new RTLSignal(aId == null, null, this, aId, null, aType, aLocation, getZDB());

		add(signal);

		return signal;
	}

	public RTLSignal createUnnamedSignal(RTLType aType, SourceLocation aLocation) throws ZamiaException {
		String id = getUnnamedSignalId();
		return createSignal(id, aType, aLocation);
	}

	public RTLSignalAE createUnnamedSignalAE(RTLType aType, SourceLocation aLocation) throws ZamiaException {
		return new RTLSignalAE(createUnnamedSignal(aType, aLocation), createUnnamedSignal(aType.computeEnableType(), aLocation), aLocation, getZDB());
	}

	public String getUnnamedSignalId() {
		String id = "us_" + fUniqueSignalCnt++;
		while (fSignals.containsKey(id)) {
			id = "us_" + fUniqueSignalCnt++;
		}
		return id;
	}

	public String getUniqueSignalId(String aSignalName) {
		String ret = aSignalName + "_" + fUniqueSignalCnt++;
		while (findSignal(ret) != null) {
			ret = aSignalName + "_" + fUniqueSignalCnt++;
		}
		return ret;
	}

	public String getUniqueModuleId(String aCallName) {
		String ret = aCallName + "_" + fUniqueModuleCnt++;
		while (findNode(ret) != null) {
			ret = aCallName + "_" + fUniqueModuleCnt++;
		}
		return ret;
	}

	public String getUniqueId(String aId) {
		String id = aId;
		while (fSignals.containsKey(id)) {
			id = aId + "_" + fUniqueId++;
		}
		while (fNodes.containsKey(id)) {
			id = aId + "_" + fUniqueId++;
		}
		return id;
	}

	@Override
	public void clear() {
		super.clear();
		fSignals = new HashMapArray<String, RTLSignal>(10);
		fNodes = new HashMapArray<String, RTLNode>(1);
		fUniqueSignalCnt = 0;
		fUniqueModuleCnt = 0;
		fUniqueId = 0;
	}

	public int getNumNodes() {
		return fNodes.size();
	}

	public int getNumSignals() {
		return fSignals.size();
	}

	public RTLSignal getSignal(int aIdx) {
		return fSignals.get(aIdx);
	}

	public RTLNode getNode(int aIdx) {
		return fNodes.get(aIdx);
	}

	//	public NetList dissolve() throws ZamiaException {
	//		throw new ZamiaException ("Internal error: unimplemented method called.");
	//	}

	public ArrayList<RTLNode> getReceivers(RTLNode aNode) {

		ArrayList<RTLNode> receivers = new ArrayList<RTLNode>();

		for (Iterator<RTLPort> i = aNode.getOutputPorts().iterator(); i.hasNext();) {
			RTLPort p = i.next();

			RTLSignal s = p.getSignal();
			if (s == null)
				continue;
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.OUT) {
					RTLNode r = dp.getNode();
					if (r != aNode)
						receivers.add(r);
				}
			}
		}
		return receivers;
	}

	public ArrayList<RTLNode> getReceivers(RTLSignal aSignal) {

		ArrayList<RTLNode> receivers = new ArrayList<RTLNode>();

		int n = aSignal.getNumConns();
		for (int j = 0; j < n; j++) {
			RTLPort dp = aSignal.getConn(j);
			if (dp.getDirection() != PortDir.OUT)
				receivers.add(dp.getNode());
		}
		return receivers;
	}

	public HashSet<RTLNode> getReceivers(RTLPort aPort) {

		HashSet<RTLNode> receivers = new HashSet<RTLNode>();

		RTLSignal s = aPort.getSignal();
		if (s != null) {
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.OUT)
					receivers.add(dp.getNode());
			}
		}
		return receivers;
	}

	public ArrayList<RTLNode> getDrivers(RTLNode aNode) {

		ArrayList<RTLNode> drivers = new ArrayList<RTLNode>();

		for (Iterator<RTLPort> i = aNode.getInputPorts().iterator(); i.hasNext();) {
			RTLPort p = i.next();

			RTLSignal s = p.getSignal();
			if (s == null)
				continue;
			int n = s.getNumConns();
			for (int j = 0; j < n; j++) {
				RTLPort dp = s.getConn(j);
				if (dp.getDirection() != PortDir.IN) {
					RTLNode r = dp.getNode();
					if (r != aNode)
						drivers.add(r);
				}
			}
		}
		return drivers;
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

	public void dump(PrintStream aPS) {

		for (int i = 0; i < getNumNodes(); i++) {
			RTLNode module = getNode(i);

			aPS.println();
			aPS.println("NODE #" + i + ": " + module);
			aPS.println();

			int nPorts = module.getNumPorts();
			for (int j = 0; j < nPorts; j++) {
				RTLPort p = module.getPort(j);
				aPS.println("  Port " + p + " connected to:");

				RTLSignal s = p.getSignal();
				if (s == null) {
					aPS.println("    open");
					aPS.println();
					continue;
				}

				int nConns = s.getNumConns();
				for (int k = 0; k < nConns; k++) {
					aPS.println("    " + s.getConn(k));
				}
				aPS.println();
			}
		}
	}

	public void printStats() {
		logger.info("\nStats of RTL Graph " + this + ":");
		logger.info("%8d Ports\n", getNumPorts());
		logger.info("%8d Modules\n", getNumNodes());
		logger.info("%8d Signals\n", getNumSignals());
	}

	public RTLSignal findSignal(PathName aSignalPathName, int aIdx) throws ZamiaException {

		int n = aSignalPathName.getNumSegments();
		if (aIdx == n - 1) {
			RTLSignal s = fSignals.get(aSignalPathName.getSegment(aIdx));

			if (s == null) {
				throw new ZamiaException("Couldn't find signal " + aSignalPathName);
			}

			return s;
		}

		RTLNode node = fNodes.get(aSignalPathName.getSegment(aIdx));

		if (node == null || !(node instanceof RTLModule)) {
			throw new ZamiaException("Couldn't find signal " + aSignalPathName);
		}

		RTLModule graph = (RTLModule) node;

		return graph.findSignal(aSignalPathName, aIdx + 1);
	}

	@Override
	public String getClassName() {
		return fUID;
	}

	public String getSignature() {
		return fSignature;
	}

}
