/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 24, 2010
 */
package org.zamia.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class CountingLatch {

	private Lock fLock;

	private Condition fCond;

	private int fValue;

	public CountingLatch(int aInitialValue) {
		fValue = aInitialValue;
		fLock = new ReentrantLock();
		fCond = fLock.newCondition();
	}

	public void await() throws InterruptedException {
		fLock.lock();
		while (fValue > 0) {
			fCond.await();
		}
		fLock.unlock();
	}

	public void countDown() {
		fLock.lock();
		if (fValue > 0) {
			fValue--;
		}
		if (fValue <= 0) {
			fCond.signalAll();
		}
		fLock.unlock();
	}

	public void countUp() {
		fLock.lock();
		fValue++;
		fLock.unlock();
	}

	public int getCount() {
		int v = 0;
		fLock.lock();
		v = fValue;
		fLock.unlock();
		return v;
	}
}
