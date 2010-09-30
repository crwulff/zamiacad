/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.rtl;

import java.io.Serializable;
import java.util.ArrayList;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public abstract class RTLModule implements Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	protected String instanceName;

	protected RTLGraph parent;

	private ASTObject src;

	protected HashMapArray<String, RTLPort> ports;

	// test output on/ off
	// private boolean dump;

	public RTLModule(RTLGraph parent_, String instanceName_, ASTObject src_) {
		parent = parent_;
		src = src_;
		if (instanceName_ != null)
			instanceName = instanceName_;
		else if (parent_ != null)
			instanceName = parent_.getUniqueModuleId(getClassName());

		clear();
	}

	public void clear() {
		ports = new HashMapArray<String, RTLPort>(3);
	}

	public abstract String getClassName();

	public void setInstanceName(String s) {
		instanceName = s;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void remove(RTLPort p) throws ZamiaException {
		ports.remove(p.getId());
	}

	public int getNumPorts() {
		return ports.size();
	}

	public RTLPort getPort(int idx_) {
		return ports.get(idx_);
	}

	public void setParent(RTLGraph parent_) {
		parent = parent_;
	}

	public RTLPort createPort(String id_, ZILType type_, PortDir direction_, ASTObject src_) throws ZamiaException {

		RTLPort port = new RTLPort(this, id_, type_, direction_);

		add(port);

		return port;
	}

	protected void add(RTLPort port) {
		ports.put(port.getId(), port);
	}

	public ASTObject getSource() {
		return src;
	}

	public void setSource(ASTObject src_) {
		src = src_;
	}

	public int getNumInputs() {
		int n = 0;
		for (int i = 0; i < ports.size(); i++) {
			if (ports.get(i).getDirection() == PortDir.IN)
				n++;
		}
		return n;
	}

	public int getNumOutputs() {
		int n = 0;
		for (int i = 0; i < ports.size(); i++) {
			if (ports.get(i).getDirection() == PortDir.OUT)
				n++;
		}
		return n;
	}

	public RTLGraph getParent() {
		return parent;
	}

	public ArrayList<RTLPort> getOutputPorts() {
		int n = ports.size();
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(n);

		for (int i = 0; i < n; i++) {
			RTLPort port = ports.get(i);
			if (port.getDirection() == PortDir.OUT)
				res.add(port);
		}

		return res;
	}

	public ArrayList<RTLPort> getInputPorts() {
		int n = ports.size();
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(n);

		for (int i = 0; i < n; i++) {
			RTLPort port = ports.get(i);
			if (port.getDirection() == PortDir.IN)
				res.add(port);
		}

		return res;
	}

	public PathName getPath() {
		if (parent != null)
			return parent.getPath().append(getInstanceName());
		else
			return new PathName(""+PathName.separator);
	}

	public ArrayList<RTLModule> getSuccessors() {

		ArrayList<RTLModule> res = new ArrayList<RTLModule>();

		int n = ports.size();
		for (int i = 0; i < n; i++) {
			RTLPort p = ports.get(i);

			if (p.getDirection() != PortDir.IN) {

				RTLSignal s = p.getSignal();

				if (s == null)
					continue;

				int m = s.getNumConns();
				for (int j = 0; j < m; j++) {

					RTLPort p2 = s.getConn(j);

					if (p2 == p)
						continue;

					RTLModule module = p2.getModule();

					res.add(module);
				}
			}
		}
		return res;
	}

	public ArrayList<RTLModule> getPredecessors() {

		ArrayList<RTLModule> res = new ArrayList<RTLModule>();

		int n = ports.size();
		for (int i = 0; i < n; i++) {
			RTLPort p = ports.get(i);

			if (p.getDirection() == PortDir.IN) {

				RTLSignal s = p.getSignal();

				if (s == null)
					continue;

				int m = s.getNumConns();
				for (int j = 0; j < m; j++) {

					RTLPort p2 = s.getConn(j);

					if (p2 == p)
						continue;

					RTLModule module = p2.getModule();

					res.add(module);
				}
			}
		}
		return res;
	}

	public RTLPort findPort(String id_) {
		return ports.get(id_);
	}

	public String toString() {
		return getPath().toString();
	}

	public boolean isPortMandatory(RTLPort port_) {
		return true;
	}

	// needs to be overwritten in any subclass
	// that contains additional state (e.g. RTLGraph, Port modules, constant selectors...)
	@SuppressWarnings("unchecked")
	public boolean equals(RTLModule module2_) {

		Class c1 = getClass();
		Class c2 = module2_.getClass();

		if (!c1.equals(c2))
			return false;

		int n = getNumPorts();
		int n2 = module2_.getNumPorts();
		if (n != n2)
			return false;

		for (int k = 0; k < n; k++) {

			RTLPort p1 = getPort(k);

			RTLPort p2 = module2_.getPort(k);

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
