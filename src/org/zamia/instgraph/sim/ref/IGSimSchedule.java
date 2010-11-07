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

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class IGSimSchedule {

	private IGRequestList fFirst;

	public IGSimSchedule() {
		fFirst = null;
	}

	public boolean isEmpty() {
		return fFirst == null;
	}

	public IGRequestList getFirst() {
		return fFirst;
	}

	public void removeFirst() {
		fFirst = fFirst.getNext();
	}

	public void schedule(BigInteger aT, IGSimRequest aRequest) {

		IGRequestList cur = fFirst;

		if (cur == null) {
			cur = new IGRequestList(aT, null);

			fFirst = cur;
		} else {

			IGRequestList next = cur.getNext();

			while (next != null && next.getTime().compareTo(aT) <= 0) {
				cur = next;
				next = cur.getNext();
			}

			BigInteger t = cur.getTime();

			if (t.compareTo(aT) < 0) {

				IGRequestList rl = new IGRequestList(aT, null);

				rl.setNext(cur.getNext());
				cur.setNext(rl);

				cur = rl;

			} else if (t.compareTo(aT) > 0) {

				fFirst = new IGRequestList(aT, null);

				fFirst.setNext(cur);

				cur = fFirst;

			}
		}
		cur.addRequest(aRequest);
	}

	public void cancelAllWakeups(IGSimProcess aProcess) {

		IGRequestList l = fFirst;
		while (l != null) {
			
			l.cancelAllWakeups(aProcess);
			
			l = l.getNext();
		}
	}

	@Override
	public String toString() {
		return isEmpty() ? "Empty " + IGSimSchedule.class.getSimpleName() : fFirst.toString();
	}
}
