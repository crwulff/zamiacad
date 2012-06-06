package org.zamia.instgraph.interpreter.logger;

import java.util.TreeMap;

import org.zamia.SourceLocation;

/**
 * @author Anton Chepurov
 */
public class IGHitCountLogger extends IGCodeExecutionLogger {

	private TreeMap<Integer, Integer> fTotalHitsByLine;

	private int fMaxCount = 0;

	private boolean fIsDirty;

	public IGHitCountLogger(String fId) {
		super(fId, true);
	}

	private IGHitCountLogger() {
		super(null, false);
	}

	@Override
	protected IGHitCountLogger createLogger() {
		return new IGHitCountLogger(null);
	}

	@Override
	protected IGHitCountLogger createLeafLogger() {
		return new IGHitCountLogger();
	}

	public void logHit(SourceLocation aSourceLocation, int aCount) {

		logItemComposite(new Hit(aSourceLocation, aCount));
	}

	@Override
	void logItemLeaf(CodeItem aNewItem) {

		if (aNewItem == null) {
			return;
		}

		super.logItemLeaf(aNewItem);

		int newCount = ((Hit) getItem(aNewItem)).fCount;

		if (newCount > fMaxCount) {
			fMaxCount = newCount;
		}
	}

	@Override
	protected void mergeLeaves(IGCodeExecutionLogger aOtherLogger) {

		super.mergeLeaves(aOtherLogger);

		fIsDirty = true;
	}

	public boolean hasLine(int aLine) {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for line from COMPOSITE logger");
		}
		computeTotal();
		return fTotalHitsByLine.containsKey(aLine);
	}

	private void computeTotal() {
		if (fTotalHitsByLine == null || fIsDirty) {
			fTotalHitsByLine = new TreeMap<Integer, Integer>();
			for (CodeItem item : fItems) {
				Hit hit = (Hit) item;
				Integer curCount = fTotalHitsByLine.containsKey(hit.fLoc.fLine) ? fTotalHitsByLine.get(hit.fLoc.fLine) : 0;
				fTotalHitsByLine.put(hit.fLoc.fLine, curCount + hit.fCount);
			}
			fIsDirty = false;
		}
	}

	public int getCount(int aLine) {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for count from COMPOSITE logger");
		}
		computeTotal();
		return fTotalHitsByLine.containsKey(aLine) ? fTotalHitsByLine.get(aLine) : 0;
	}

	public int getMaxCount() {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for max count from COMPOSITE logger");
		}
		return fMaxCount;
	}

	static class Hit extends CodeItem {

		private int fCount;

		public Hit(SourceLocation aLocation, int aCount) {
			super(aLocation);
			fCount = aCount;
		}

		@Override
		public void merge(CodeItem aOther) {

			if (!(aOther instanceof Hit)) {
				return;
			}

			Hit other = (Hit) aOther;

			fCount += other.fCount;
		}

		@Override
		public CodeItem subtract(CodeItem aOther) {
			// just return null so as to skip this item
			return null;
		}
	}
}
