/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2009
 */
package org.zamia.rtl.sim;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class SimSchedule {

	private RequestList fFirst;

	public SimSchedule() {
		fFirst = null;
	}

	public boolean isEmpty() {
		return fFirst == null;
	}

	public RequestList getFirst() {
		return fFirst;
	}

	public void removeFirst() {
		fFirst = fFirst.getNext();
	}

	public void schedule(long aT, SimRequest aRequest) {

		RequestList cur = fFirst;

		if (cur == null) {
			cur = new RequestList(aT, null);

			fFirst = cur;
		} else {

			RequestList next = cur.getNext();

			while (next != null && next.getTime() <= aT) {
				cur = next;
				next = cur.getNext();
			}

			long t = cur.getTime();

			if (t < aT) {

				RequestList rl = new RequestList(aT, null);

				rl.setNext(cur.getNext());
				cur.setNext(rl);

				cur = rl;

			} else if (t > aT) {

				fFirst = new RequestList(aT, null);

				fFirst.setNext(cur);

				cur = fFirst;

			}
		}
		cur.addRequest(aRequest);
	}

}
