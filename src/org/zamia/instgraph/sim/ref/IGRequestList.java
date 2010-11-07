/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.instgraph.sim.ref;

import java.math.BigInteger;
import java.util.ArrayList;

import org.zamia.ZamiaException;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class IGRequestList {

	private IGRequestList fNext;

	private BigInteger fTime;

	private ArrayList<IGSimRequest> fRequests;

	public IGRequestList(BigInteger aTime, IGRequestList aNext) {
		fTime = aTime;
		fNext = aNext;
		fRequests = new ArrayList<IGSimRequest>();
	}

	public IGRequestList getNext() {
		return fNext;
	}

	public BigInteger getTime() {
		return fTime;
	}

	public void addRequest(IGSimRequest aRequest) {
		fRequests.add(aRequest);
	}

	public void execute(IGSimRef aSimulator) throws ZamiaException {
		int n = fRequests.size();
		for (int i = 0; i < n; i++) {
			IGSimRequest req = fRequests.get(i);

			req.execute(aSimulator);
		}
	}

	public void setNext(IGRequestList aWL) {
		fNext = aWL;
	}

	public void cancelAllWakeups(IGSimProcess aProcess) {
		int n = fRequests.size();
		for (int i = 0; i < n; i++) {
			IGSimRequest req = fRequests.get(i);
			if (req.getProcess() == aProcess) {
				req.setCanceled(true);
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("@ ");

		buf.append(fTime).append("(").append(fRequests.size()).append(")").append(": ");

		buf.append(fRequests);

		if (fNext != null) {
			buf.append(" (more in next...)");
		}

		return buf.toString();
	}
}
