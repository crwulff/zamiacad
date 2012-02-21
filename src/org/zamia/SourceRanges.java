package org.zamia;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class SourceRanges {

	private final Map<SourceFile, SourceRanges> fRangesByFile;

	private final Map<Integer, Integer> fCountByLine;

	private int fMaxCount = 0;

	/**
	 * Public ranges factory
	 *
	 * @return composite ranges capable of mapping ranges by file
	 */
	public static SourceRanges createRanges() {
		return new SourceRanges(new HashMap<SourceFile, SourceRanges>(), null);
	}

	/**
	 * Private ranges factory
	 *
	 * @return leaf ranges capable of mapping counts by lines
	 */
	private static SourceRanges createRangesInternal() {
		return new SourceRanges(null, new TreeMap<Integer, Integer>());
	}

	private SourceRanges(Map<SourceFile, SourceRanges> aRangesByFile, Map<Integer, Integer> aCountByLine) {
		fRangesByFile = aRangesByFile;
		fCountByLine = aCountByLine;
	}

	public void add(SourceLocation aSourceLocation, int aCount) {

		if (isLeaf()) {
			throw new RuntimeException("Trying to add file to LEAF SourceRanges");
		}

		SourceLocation loc = aSourceLocation;

		if (loc == null) {
			return;
		}

		SourceRanges sourceRanges = fRangesByFile.get(loc.fSF);
		if (sourceRanges == null) {
			sourceRanges = createRangesInternal();
			fRangesByFile.put(loc.fSF, sourceRanges);
		}

		sourceRanges.add(loc.fLine - 1, aCount); // -1 is an optimization for further queries (no need to +1 each time)
	}

	private void add(int aLine, Integer aCount) {

		Integer count = fCountByLine.get(aLine);
		if (count == null) {
			count = 0;
		}

		int total = count + aCount;

		fCountByLine.put(aLine, total);

		if (total > fMaxCount) {
			fMaxCount = total;
		}
	}

	public boolean hasFile(SourceFile aSourceFile) {
		if (isLeaf()) {
			throw new RuntimeException("Trying to poll for file from LEAF SourceRanges");
		}
		return fRangesByFile.containsKey(aSourceFile);
	}

	public SourceRanges getSourceRanges(SourceFile aSourceFile) {
		if (isLeaf()) {
			throw new RuntimeException("Trying to poll for file from LEAF SourceRanges");
		}
		return fRangesByFile.get(aSourceFile);
	}

	public boolean hasLine(int aLine) {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for line from COMPOSITE SourceRanges");
		}
		return fCountByLine.containsKey(aLine);
	}

	public int getCount(int aLine) {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for count from COMPOSITE SourceRanges");
		}
		return fCountByLine.containsKey(aLine) ? fCountByLine.get(aLine) : 0;
	}

	private boolean isLeaf() {
		return fRangesByFile == null;
	}

	private boolean isComposite() {
		return fCountByLine == null;
	}

	public int getMaxCount() {
		if (isComposite()) {
			throw new RuntimeException("Trying to poll for max count from COMPOSITE SourceRanges");
		}
		return fMaxCount;
	}

	public SourceRanges subtract(SourceRanges... aOthers) {
		SourceRanges result = this;
		for (SourceRanges other : aOthers) {
			result = result.subtractP(other);
		}
		return result;
	}

	private SourceRanges subtractP(SourceRanges aOthers) {

		SourceRanges result;

		if (isComposite() ^ aOthers.isComposite()) {
			return this; // do nothing
		}

		if (isComposite()) {

			result = createRanges();

			for (Map.Entry<SourceFile, SourceRanges> entry : fRangesByFile.entrySet()) {
				SourceFile file = entry.getKey();
				SourceRanges ranges = entry.getValue();

				SourceRanges resultInternal = ranges;

				if (aOthers.hasFile(file)) {

					resultInternal = ranges.subtract(aOthers.getSourceRanges(file));

					if (resultInternal.isEmpty()) {
						continue;
					}
				}

				result.fRangesByFile.put(file, resultInternal);
			}
		} else {

			result = createRangesInternal();

			for (Map.Entry<Integer, Integer> entry : fCountByLine.entrySet()) {
				Integer line = entry.getKey();
				Integer count = entry.getValue();

				if (aOthers.hasLine(line)) {
					continue;
				}

				result.fCountByLine.put(line, count);
			}
		}

		return result;
	}

	private boolean isEmpty() {
		return isComposite() ? fRangesByFile.isEmpty() : fCountByLine.isEmpty();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("SourceRanges");
		sb.append("{fRangesByFile=").append(fRangesByFile);
		sb.append(", fCountByLine=").append(fCountByLine);
		sb.append('}');
		return sb.toString();
	}
}
