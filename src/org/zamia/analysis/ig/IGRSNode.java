/* 
 * Copyright 2009, 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 27, 2009
 */
package org.zamia.analysis.ig;

import java.io.PrintStream;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode;

/**
 * IGModule, IGProcess, IGSequentialStatement, ... counterpart
 * 
 * @author Guenter Bartsch
 *
 */
public class IGRSNode {

	private final HashMapArray<String, IGRSSignal> fSignals;

	private final HashMapArray<String, IGRSNode> fNodes;

	private IGRSNode fParent;

	private final SourceLocation fLocation;

	private final PathName fPath;

	private HashMapArray<String, IGRSPort> fPorts;

	private final String fId;

	private final IGRSResult fResult;

	public IGRSNode(String aId, IGRSNode aParent, SourceLocation aLocation, PathName aPath, IGRSResult aResult) {
		fId = aId;
		fParent = aParent;
		fLocation = aLocation;
		fPath = aPath;
		fResult = aResult;

		fSignals = new HashMapArray<String, IGRSSignal>();
		fNodes = new HashMapArray<String, IGRSNode>();
		fPorts = new HashMapArray<String, IGRSPort>();
	}

	public void dump(int aI, PrintStream aOut) {

		VHDLNode.printlnIndented(fId + " {", aI, aOut);

		int n = fSignals.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal s = fSignals.get(i);
			s.dump(aI + 2, aOut);
		}

		n = fNodes.size();
		for (int i = 0; i < n; i++) {
			IGRSNode child = fNodes.get(i);
			child.dump(aI + 2, aOut);
		}

		VHDLNode.printlnIndented("}", aI, aOut);
	}

	public int countNodes() {
		int count = 1;
		int n = fNodes.size();
		for (int i = 0; i < n; i++) {
			IGRSNode child = fNodes.get(i);
			count += child.countNodes();
		}
		return count;
	}

	public int countConns() {
		int count = 0;
		int n = fNodes.size();
		for (int i = 0; i < n; i++) {
			IGRSNode child = fNodes.get(i);
			count += child.countConns();
		}
		n = fSignals.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal s = fSignals.get(i);

			count += s.getNumConns();

		}
		return count;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public PathName getPath() {
		return fPath;
	}

	int getNumSubs() {
		return fNodes.size();
	}

	public IGRSNode getSub(int aIdx) {
		return fNodes.get(aIdx);
	}

	public int getNumSignals() {
		return fSignals.size();
	}

	public IGRSSignal getSignal(int aIdx) {
		return fSignals.get(aIdx);
	}

	IGRSSignal getOrCreateSignal(IGObject aObject) throws ZamiaException {

		String id = aObject.getId();
		SourceLocation location = aObject.computeSourceLocation();

		IGRSSignal signal = fSignals.get(id);
		
		if (signal == null) {
			
			IGRSType type = fResult.synthesizeType(aObject.getType());
			
			signal = new IGRSSignal(this, id, type, null, location);
			fSignals.put(id, signal);
		}

		OIDir dir = aObject.getDirection();

		//if (dir != OIDir.NONE) {
		IGRSPort port = fPorts.get(id);

		if (port == null) {
			port = new IGRSPort(this, id, dir);

			port.setSignal(signal);
			signal.setPort(port);
			fPorts.put(id, port);
		}
		//}

		return signal;
	}

	IGRSNode getOrCreateChild(String aName, long aDBID, SourceLocation aLocation, PathName aPath) {

		IGRSNode node = fNodes.get(aName);

		if (node != null) {
			return node;
		}

		node = new IGRSNode(aName, this, aLocation, aPath, fResult);

		fNodes.put(aName, node);
		node.setParent(this);

		return node;
	}

	public IGRSNode getParent() {
		return fParent;
	}


	public int getNumPorts() {
		return fPorts.size();
	}

	public IGRSPort getPort(int aIdx) {
		return fPorts.get(aIdx);
	}

	public String getInstanceName() {
		return fId;
	}

	public IGRSSignal findSignal(String aId) {
		IGRSSignal sig = fSignals.get(aId);
		if (sig != null)
			return sig;
		return null;
	}

	public IGRSNode findNode(String aId) {
		return fNodes.get(aId);
	}

	public void setParent(IGRSNode aParent) {
		fParent = aParent;
	}

	public String toString() {
		return "IGRSNode(id=" + fId + ", path=" + fPath+ ")";
	}

}
