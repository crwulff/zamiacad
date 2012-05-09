package org.zamia.instgraph.sim;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public abstract class IGAbstractProgressMonitor {

	private BigInteger fUnit;

	private BigInteger fStartTime;

	public IGAbstractProgressMonitor(BigInteger aStartTime, BigInteger aTotalTime, int aSteps) {
		fStartTime = aStartTime;
		fUnit = aTotalTime.divide(BigInteger.valueOf(aSteps));
	}

	public void setProgress(BigInteger aCurrentTime) {
		if (aCurrentTime.subtract(fStartTime).compareTo(fUnit) >= 0) {
			fStartTime = aCurrentTime;
			doProgress();
		}
	}

	protected abstract void doProgress();

	public abstract boolean isCanceled();


	private IGAbstractProgressMonitor() {
	}

	public static final IGAbstractProgressMonitor CANCELLED_MONITOR = new IGAbstractProgressMonitor() {
		@Override
		protected void doProgress() {
		}
		@Override
		public boolean isCanceled() {
			return true;
		}
	};
}
