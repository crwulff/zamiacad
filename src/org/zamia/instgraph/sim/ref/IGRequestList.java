/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.instgraph.sim.ref;

import org.zamia.ZamiaException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Guenter Bartsch
 */
public class IGRequestList {

	private IGRequestList fNext;

	private BigInteger fTime;

	private ArrayList<IGSignalChangeRequest> fSignalRequests;

	private ArrayList<IGWakeupRequest> fWakeupRequests;

	public IGRequestList(BigInteger aTime, IGRequestList aNext) {
		fTime = aTime;
		fNext = aNext;
		fSignalRequests = new ArrayList<IGSignalChangeRequest>();
		fWakeupRequests = new ArrayList<IGWakeupRequest>();
	}

	public IGRequestList getNext() {
		return fNext;
	}

	public BigInteger getTime() {
		return fTime;
	}

	public void addRequest(IGSimRequest aRequest) {
		if (aRequest instanceof IGSignalChangeRequest) {
			fSignalRequests.add(((IGSignalChangeRequest) aRequest));
		}
		if (aRequest instanceof IGWakeupRequest) {
			fWakeupRequests.add((IGWakeupRequest) aRequest);
		}
	}

	public void executeSignals(IGSimRef aSimulator) throws ZamiaException {
		for (IGSignalChangeRequest req : fSignalRequests) {
			req.execute(aSimulator);
		}
	}

	public void executeWakeups(IGSimRef aSimulator) throws ZamiaException {
		for (IGWakeupRequest req : fWakeupRequests) {
			req.execute(aSimulator);
		}
	}

	public void setNext(IGRequestList aWL) {
		fNext = aWL;
	}

	public void cancelAllWakeups(IGSimProcess aProcess) {
		for (IGSimRequest req : fWakeupRequests) {
			if (req.getProcess() == aProcess) {
				req.setCanceled(true);
			}
		}
		for (IGSimRequest req : fSignalRequests) {
			if (req.getProcess() == aProcess) {
				req.setCanceled(true);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("@ ");

		buf.append(fTime).append("(").append(fSignalRequests.size() + fWakeupRequests.size()).append(")").append(": ");

		buf.append(fSignalRequests);
		buf.append(fWakeupRequests);

		if (fNext != null) {
			buf.append(" (more in next...)");
		}

		return buf.toString();
	}

	public List<IGSignalChangeRequest> getSignalChanges() {
		return fSignalRequests;
	}
}
