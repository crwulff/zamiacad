/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.rtlng.RTLPort.PortDir;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLSignal extends RTLItem {

	private final boolean enableSanityChecks = false;

	// signal id handling is a bit tricky due to signal join:
	private String fIDs[]; // all ids of this signal

	private int fNumIds;

	private RTLPort fPort; // if this signal represents a (primary) port

	private final RTLType fType;

	protected final RTLModule fModule;

	private RTLSignal fReplacedBy = null; // this signal was a victim of a

	// sigjoin

	private RTLPort fConns[];

	private int fNumConns;

	private RTLValue fInitialValue;

	private boolean fIsAnonymous;

	public RTLSignal(boolean aIsAnonymous, RTLValue aInitialValue, RTLModule aModule, String aId, RTLPort aPort /*
																																						* may be null
																																						* if this is
																																						* not a port
																																						* signal
																																						*/, RTLType aType, SourceLocation aLocation, ZDB aZDB) {

		super(aLocation, aZDB);

		fInitialValue = aInitialValue;
		fIsAnonymous = aIsAnonymous;
		fType = aType;
		fIDs = new String[1];
		fNumIds = 1;
		fIDs[0] = aId;

		fPort = aPort;
		fModule = aModule;

		fConns = new RTLPort[2];
		fNumConns = 0;
	}

	public RTLPort getPort() {
		return fPort;
	}

	public void setPort(RTLPort aPort) {
		fPort = aPort;
	}

	public String getId() {
		return fIDs[0];
	}

	public void addId(String aId) {
		int idx = findIdIdx(aId);

		if (idx >= 0) {
			return;
		}

		if (fNumIds >= fIDs.length) {
			String a[] = new String[fNumIds + 5];
			for (int i = 0; i < fNumIds; i++) {
				a[i] = fIDs[i];
			}
			fIDs = a;
		}

		fIDs[fNumIds] = aId;
		fNumIds++;

	}

	public int getNumIds() {
		return fNumIds;
	}

	public String getId(int aIdx) {
		return fIDs[aIdx];
	}

	public int findIdIdx(String aId) {
		for (int i = 0; i < fNumIds; i++) {
			if (fIDs[i] == aId)
				return i;
		}
		return -1;
	}

	public RTLType getType() {
		return fType;
	}

	public String toString() {

		return "RTLSignal (id=" + getId() + ")" + "@" + Integer.toHexString(hashCode());
	}

	// signal may have been replaced by sigjoin
	public RTLSignal getCurrent() {
		if (fReplacedBy == null)
			return this;
		return fReplacedBy.getCurrent();
	}

	public RTLSignal getReplacedBy() {
		return fReplacedBy;
	}

	public void setReplacedBy(RTLSignal aSignal) {
		fReplacedBy = aSignal;
	}

	public int getNumConns() {
		return fNumConns;
	}

	public RTLPort getConn(int aIdx) {
		return fConns[aIdx];
	}

	public int findPortConnIdx(RTLPort aPort) {
		for (int i = 0; i < fNumConns; i++) {
			if (fConns[i] == aPort)
				return i;
		}
		return -1;
	}

	public void removePortConn(RTLPort aPort) {

		int idx = findPortConnIdx(aPort);

		if (idx < 0) {
			logger.error("Internal error: tried to remove port " + aPort + " from signal " + this + " but that port is not connected to this signal.");
			return;
		}

		if (idx < fNumConns - 1) {
			fConns[idx] = fConns[fNumConns - 1];
		}
		fNumConns--;

		if (enableSanityChecks)
			sanityCheck();
	}

	private void sanityCheck() {
		for (int i = 0; i < fNumConns; i++)
			if (fConns[i] == null)
				logger.error("ERROR: null connection!");
	}

	public void addPortConn(RTLPort aPort) {
		int idx = findPortConnIdx(aPort);

		if (idx >= 0) {
			logger.error("Internal error: tried to add port " + aPort + " to signal " + this + " but that port is already connected to this signal.");
			return;
		}

		if (fNumConns >= fConns.length) {
			RTLPort c[] = new RTLPort[fNumConns + 5];
			for (int i = 0; i < fNumConns; i++) {
				c[i] = fConns[i];
			}
			fConns = c;
		}

		fConns[fNumConns] = aPort;
		fNumConns++;
		if (enableSanityChecks)
			sanityCheck();
	}

	public RTLValue getInitialValue() {
		return fInitialValue;
	}

	public void setId(String aId) {
		fIDs[0] = aId;
	}

	public int getNumDrivers() {
		int nDrivers = 0;
		for (int i = 0; i < fNumConns; i++) {
			RTLPort port = fConns[i];

			if (port.getDirection() != PortDir.IN) {
				nDrivers++;
			}
		}
		return nDrivers;
	}

	public int getNumReaders() {
		int nReaders = 0;
		for (int i = 0; i < fNumConns; i++) {
			RTLPort port = fConns[i];
			if (port.getDirection() != PortDir.OUT) {
				nReaders++;
			}
		}
		return nReaders;
	}

	public boolean containsConn(RTLPort aPort) {
		for (int i = 0; i < fNumConns; i++) {
			if (fConns[i] == aPort)
				return true;
		}
		return false;
	}

	public RTLModule getRTLGraph() {
		return fModule;
	}

	public RTLPort getDriver(int aIdx) {
		int iDriver = 0;
		for (int i = 0; i < fNumConns; i++) {
			RTLPort port = fConns[i];
			if (port.getDirection() != PortDir.IN) {
				if (iDriver == aIdx)
					return port;
				iDriver++;
			}
		}
		return null;
	}

	public ArrayList<RTLPort> getDrivers() {
		ArrayList<RTLPort> res = new ArrayList<RTLPort>(fNumConns);
		for (int i = 0; i < fNumConns; i++) {
			RTLPort port = fConns[i];
			if (port.getDirection() != PortDir.IN) {
				res.add(port);
			}
		}
		return res;
	}

	public RTLPort getReader(int aIdx) {
		int iReader = 0;
		for (int i = 0; i < fNumConns; i++) {
			RTLPort port = fConns[i];
			if (port.getDirection() != PortDir.OUT) {
				if (iReader == aIdx)
					return port;
				iReader++;
			}
		}
		return null;
	}

	public void setInitialValue (RTLValue aValue) {
		fInitialValue = aValue;
	}
	
	public void setAnonymous(boolean aIsAnonymous) {
		fIsAnonymous = aIsAnonymous;
	}
	
	public boolean isAnonymous() {
		return fIsAnonymous;
	}

}
