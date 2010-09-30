/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.rtl.sim;

import java.util.ArrayList;

import org.zamia.ZamiaException;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class RequestList {

	private RequestList fNext;

	private long fTime;

	private ArrayList<SimRequest> fRequests;

	public RequestList(long aTime, RequestList aNext) {
		fTime = aTime;
		fNext = aNext;
		fRequests = new ArrayList<SimRequest>();
	}

	public RequestList getNext() {
		return fNext;
	}

	public long getTime() {
		return fTime;
	}

	public void addRequest(SimRequest aRequest) {
		fRequests.add(aRequest);
	}

	public void execute(Simulator aSimulator) throws ZamiaException {
		int n = fRequests.size();
		for (int i = 0; i<n; i++) {
			SimRequest req = fRequests.get(i);
			
			req.execute(aSimulator);
			
			
		}
	}

	public void setNext(RequestList aWL) {
		fNext = aWL;
	}

}
