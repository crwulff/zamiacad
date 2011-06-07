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

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaLogger;
import org.zamia.vhdl.ast.VHDLNode;

/**
 * IGObject signal counterpart
 * 
 * @author Guenter Bartsch
 *
 */

public class IGRSSignal {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private final boolean enableSanityChecks = false;

	private final String fID;

	private IGRSPort fPort; // if this signal represents a (primary) port

	protected final IGRSNode fNode;

	private IGRSPort fConns[];

	private int fNumConns;

	private final IGRSType fType;

	public IGRSSignal(IGRSNode aNode, String aId, IGRSType aType, IGRSPort aPort /* may be null if this is not a port signal */, SourceLocation aLocation) {

		fID = aId;
		fType = aType;

		fPort = aPort;
		fNode = aNode;

		fConns = new IGRSPort[2];
		fNumConns = 0;
	}

	public IGRSPort getPort() {
		return fPort;
	}

	void setPort(IGRSPort aPort) {
		fPort = aPort;
	}

	public String getId() {
		return fID;
	}

	public String toString() {

		return "IGRSSignal (id=" + getId() + ")" + "@" + Integer.toHexString(hashCode());
	}

	public int getNumConns() {
		return fNumConns;
	}

	public IGRSPort getConn(int aIdx) {
		return fConns[aIdx];
	}

	private int findPortConnIdx(IGRSPort aPort) {
		for (int i = 0; i < fNumConns; i++) {
			if (fConns[i] == aPort)
				return i;
		}
		return -1;
	}

	void removePortConn(IGRSPort aPort) {

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

	void addPortConn(IGRSPort aPort) {

		if (aPort == null) {
			logger.error("Internal error: addPortConn(): aPort==null");
			return;
		}

		int idx = findPortConnIdx(aPort);

		if (idx >= 0) {
			//logger.error("Internal error: tried to add port " + aPort + " to signal " + this + " but that port is already connected to this signal.");
			return;
		}

		if (fNumConns >= fConns.length) {
			IGRSPort c[] = new IGRSPort[fNumConns + 5];
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

	public IGRSNode getNode() {
		return fNode;
	}

	public void dump(int aIndent, PrintStream aOut) {

		VHDLNode.printlnIndented("signal " + fID + " {", aIndent, aOut);

		for (int i = 0; i < fNumConns; i++) {
			IGRSPort port = fConns[i];
			VHDLNode.printlnIndented("connected to: " + port, aIndent + 1, aOut);
		}

		VHDLNode.printlnIndented("}", aIndent, aOut);
	}

	public IGRSType getType() {
		return fType;
	}

}
