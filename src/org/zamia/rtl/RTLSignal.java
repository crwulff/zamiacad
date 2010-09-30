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

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author guenter bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLSignal implements Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final boolean enableSanityChecks = false;

	// signal id handling is a bit tricky due to signal join:
	private String ids[]; // all ids of this signal

	private int numIds;

	private RTLPort port; // if this signal represents a (primary) port

	private ZILType type;

	private ASTObject src; // intermediate layer object this signal was generated from

	protected RTLGraph rtlg;

	private RTLSignal replacedBy = null; // this signal was a victim of a

	// sigjoin

	private RTLPort conns[];

	private int numConns;

	private ZILValue initialValue;

	public RTLSignal(RTLGraph rtlg_, String id_, RTLPort port_ /*
																 * may be null
																 * if this is
																 * not a port
																 * signal
																 */, ZILType type_, ASTObject src_) {

		ids = new String[1];
		numIds = 1;
		ids[0] = id_;

		port = port_;
		rtlg = rtlg_;
		src = src_;
		setType(type_);

		conns = new RTLPort[2];
		numConns = 0;
	}

	public RTLPort getPort() {
		return port;
	}

	public void setPort(RTLPort port_) {
		port = port_;
	}

	public String getId() {
		return ids[0];
	}

	public void addId(String id_) {
		int idx = findIdIdx(id_);

		if (idx >= 0) {
			return;
		}

		if (numIds >= ids.length) {
			String a[] = new String[numIds + 5];
			for (int i = 0; i < numIds; i++) {
				a[i] = ids[i];
			}
			ids = a;
		}

		ids[numIds] = id_;
		numIds++;

	}

	public int getNumIds() {
		return numIds;
	}

	public String getId(int i) {
		return ids[i];
	}

	public int findIdIdx(String id_) {
		for (int i = 0; i < numIds; i++) {
			if (ids[i] == id_)
				return i;
		}
		return -1;
	}

	public void setType(ZILType type_) {
		type = type_;
	}

	public ZILType getType() {
		return type;
	}

	public String toString() {

		return "RTLSignal (id=" + getId() + ")" + "@" + Integer.toHexString(hashCode());
	}

	// signal may have been replaced by sigjoin
	public RTLSignal getCurrent() {
		if (replacedBy == null)
			return this;
		return replacedBy.getCurrent();
	}

	public ASTObject getSource() {
		return src;
	}

	public RTLSignal getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(RTLSignal s_) {
		replacedBy = s_;
	}

	public int getNumConns() {
		return numConns;
	}

	public RTLPort getConn(int i) {
		return conns[i];
	}

	public int findPortConnIdx(RTLPort port_) {
		for (int i = 0; i < numConns; i++) {
			if (conns[i] == port_)
				return i;
		}
		return -1;
	}

	public void removePortConn(RTLPort port_) {

		int idx = findPortConnIdx(port_);

		if (idx < 0) {
			logger.error("Internal error: tried to remove port " + port_ + " from signal " + this + " but that port is not connected to this signal.");
			return;
		}

		if (idx < numConns - 1) {
			conns[idx] = conns[numConns - 1];
		}
		numConns--;

		if (enableSanityChecks)
			sanityCheck();
	}

	private void sanityCheck() {
		for (int i = 0; i < numConns; i++)
			if (conns[i] == null)
				logger.error("ERROR: null connection!");
	}

	public void addPortConn(RTLPort port_) {
		int idx = findPortConnIdx(port_);

		if (idx >= 0) {
			logger.error("Internal error: tried to add port " + port_ + " to signal " + this + " but that port is already connected to this signal.");
			return;
		}

		if (numConns >= conns.length) {
			RTLPort c[] = new RTLPort[numConns + 5];
			for (int i = 0; i < numConns; i++) {
				c[i] = conns[i];
			}
			conns = c;
		}

		conns[numConns] = port_;
		numConns++;
		if (enableSanityChecks)
			sanityCheck();
	}

	public ZILValue getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(ZILValue iv_) {
		initialValue = iv_;
	}

	public void setId(String id_) {
		ids[0] = id_;
	}

	public int getNumDrivers() {
		int nDrivers = 0;
		for (int i = 0; i < numConns; i++) {
			RTLPort port = conns[i];

			if (port.getDirection() != PortDir.IN) {
				nDrivers++;
			}
		}
		return nDrivers;
	}

	public int getNumReaders() {
		int nReaders = 0;
		for (int i = 0; i < numConns; i++) {
			RTLPort port = conns[i];
			if (port.getDirection() != PortDir.OUT) {
				nReaders++;
			}
		}
		return nReaders;
	}

	public boolean containsConn(RTLPort p) {
		for (int i = 0; i < numConns; i++) {
			if (conns[i] == p)
				return true;
		}
		return false;
	}

	public RTLGraph getRTLGraph() {
		return rtlg;
	}

	public RTLPort getDriver(int idx_) {
		int iDriver = 0;
		for (int i = 0; i < numConns; i++) {
			RTLPort port = conns[i];
			if (port.getDirection() != PortDir.IN) {
				if (iDriver == idx_)
					return port;
				iDriver++;
			}
		}
		return null;
	}

	public ArrayList<RTLPort> getDrivers() {
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(numConns);
		for (int i = 0; i < numConns; i++) {
			RTLPort port = conns[i];
			if (port.getDirection() != PortDir.IN) {
				res.add(port);
			}
		}
		return res;
	}

	public RTLPort getReader(int idx_) {
		int iReader = 0;
		for (int i = 0; i < numConns; i++) {
			RTLPort port = conns[i];
			if (port.getDirection() != PortDir.OUT) {
				if (iReader == idx_)
					return port;
				iReader++;
			}
		}
		return null;
	}

	public void setSource(ASTObject src2) {
		src = src2;
	}

	public PathName getPath() throws ZamiaException {

		return rtlg.getPath().append(getId(0));
	}

}
