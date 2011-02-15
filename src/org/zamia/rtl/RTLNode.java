/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.rtl;

import java.util.ArrayList;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class RTLNode extends RTLItem {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	protected String fInstanceName;

	protected RTLModule fModule;

	protected HashMapArray<String, RTLPort> fPorts;

	public RTLNode(String aInstanceName, RTLModule aModule, SourceLocation aLocation, ZDB aZDB) {

		super(aLocation, aZDB);

		fModule = aModule;

		if (aInstanceName != null)
			fInstanceName = aInstanceName;
		else if (aModule != null)
			fInstanceName = aModule.getUniqueModuleId(getClassName());

		clear();
	}

	public void clear() {
		fPorts = new HashMapArray<String, RTLPort>(3);
	}

	public abstract String getClassName();

	public void setInstanceName(String aInstanceName) {
		fInstanceName = aInstanceName;
	}

	public String getInstanceName() {
		return fInstanceName;
	}

	public void remove(RTLPort aPort) throws ZamiaException {
		fPorts.remove(aPort.getId());
	}

	public int getNumPorts() {
		return fPorts.size();
	}

	public RTLPort getPort(int aIdx) {
		return fPorts.get(aIdx);
	}

	public void setParent(RTLModule aParent) {
		fModule = aParent;
	}

	public RTLPort createPort(String aId, RTLType aType, PortDir aDir, SourceLocation aLocation) throws ZamiaException {

		RTLPort port = new RTLPort(this, aId, aType, aDir, aLocation, getZDB());

		add(port);

		return port;
	}

	protected void add(RTLPort aPort) {
		fPorts.put(aPort.getId(), aPort);
	}

	public int getNumInputs() {
		int n = 0;
		for (int i = 0; i < fPorts.size(); i++) {
			if (fPorts.get(i).getDirection() == PortDir.IN)
				n++;
		}
		return n;
	}

	public int getNumOutputs() {
		int n = 0;
		for (int i = 0; i < fPorts.size(); i++) {
			if (fPorts.get(i).getDirection() == PortDir.OUT)
				n++;
		}
		return n;
	}

	public RTLModule getParent() {
		return fModule;
	}

	public ArrayList<RTLPort> getOutputPorts() {
		int n = fPorts.size();
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(n);

		for (int i = 0; i < n; i++) {
			RTLPort port = fPorts.get(i);
			if (port.getDirection() == PortDir.OUT)
				res.add(port);
		}

		return res;
	}

	public ArrayList<RTLPort> getInputPorts() {
		int n = fPorts.size();
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(n);

		for (int i = 0; i < n; i++) {
			RTLPort port = fPorts.get(i);
			if (port.getDirection() == PortDir.IN)
				res.add(port);
		}

		return res;
	}

	public ArrayList<RTLNode> getSuccessors() {

		ArrayList<RTLNode> res = new ArrayList<RTLNode>();

		int n = fPorts.size();
		for (int i = 0; i < n; i++) {
			RTLPort p = fPorts.get(i);

			if (p.getDirection() != PortDir.IN) {

				RTLSignal s = p.getSignal();

				if (s == null)
					continue;

				int m = s.getNumConns();
				for (int j = 0; j < m; j++) {

					RTLPort p2 = s.getConn(j);

					if (p2 == p)
						continue;

					RTLNode node = p2.getNode();

					res.add(node);
				}
			}
		}
		return res;
	}

	public ArrayList<RTLNode> getPredecessors() {

		ArrayList<RTLNode> res = new ArrayList<RTLNode>();

		int n = fPorts.size();
		for (int i = 0; i < n; i++) {
			RTLPort p = fPorts.get(i);

			if (p.getDirection() == PortDir.IN) {

				RTLSignal s = p.getSignal();

				if (s == null)
					continue;

				int m = s.getNumConns();
				for (int j = 0; j < m; j++) {

					RTLPort p2 = s.getConn(j);

					if (p2 == p)
						continue;

					RTLNode node = p2.getNode();

					res.add(node);
				}
			}
		}
		return res;
	}

	public RTLPort findPort(String aId) {
		return fPorts.get(aId);
	}

	public boolean isPortMandatory(RTLPort aPort) {
		return true;
	}

	// needs to be overwritten in any subclass
	// that contains additional state (e.g. RTLGraph, Port modules, constant selectors...)
	@SuppressWarnings({ "rawtypes" })
	public boolean equals(RTLNode aNode) {

		Class c1 = getClass();
		Class c2 = aNode.getClass();

		if (!c1.equals(c2))
			return false;

		int n = getNumPorts();
		int n2 = aNode.getNumPorts();
		if (n != n2)
			return false;

		for (int k = 0; k < n; k++) {

			RTLPort p1 = getPort(k);

			RTLPort p2 = aNode.getPort(k);

			if (p1.getDirection() != PortDir.OUT) {
				if (p1.getSignal() != p2.getSignal()) {
					return false;
				}
			} else {
				if (!p1.getType().isCompatible(p2.getType())) {
					return false;
				}
			}
		}
		return true;
	}
}
