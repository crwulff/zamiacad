/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 28, 2009
 */
package org.zamia.plugin.views.sim;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zamia.ExceptionLogger;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class WaveformPaintJobScheduler implements Runnable {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Lock fLock;

	private Condition fCond;

	private WaveformPaintJob fCurrent, fNext;

	private boolean fRunning = true;

	private SimulatorView fSimView;

	public WaveformPaintJobScheduler(SimulatorView aSimView) {
		fLock = new ReentrantLock();
		fCond = fLock.newCondition();
		fSimView = aSimView;
	}

	public void schedule(WaveformPaintJob aJob) {

		fLock.lock();
		try {

			if (fCurrent != null) {
				fCurrent.cancel();
			}

			fNext = aJob;

			fCond.signalAll();

		} finally {
			fLock.unlock();
		}
	}

	public void cancel() {
		fLock.lock();
		try {

			fRunning = false;

			if (fCurrent != null) {
				fCurrent.cancel();
			}

			fCond.signalAll();

		} finally {
			fLock.unlock();
		}
	}

	public void cancelCurrentJob() {
		fLock.lock();
		try {

			if (fCurrent != null) {
				fCurrent.cancel();
			}

		} finally {
			fLock.unlock();
		}
	}


	@Override
	public void run() {

		while (fRunning) {
			fLock.lock();
			try {

				while (fNext == null && fRunning) {
					fCond.await();
				}

				if (fNext != null) {
					
					fCurrent = fNext;
					fNext = null;
					
					fCurrent.schedule();
					
					fLock.unlock();
					
					fSimView.setRedDotState(1);
					
					fCurrent.join();

					fSimView.setRedDotState(0);

					fLock.lock();
				}

			} catch (InterruptedException e) {
				el.logException(e);
				fRunning = false;
			} finally {
				fLock.unlock();
			}
		}
	}

}
