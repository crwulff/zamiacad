package org.zamia;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

/**
 * @author Anton Chepurov
 */
public class SourceRanges {

	private final Map<SourceFile, SourceRanges> fRangesByFile;

	private final Map<Integer, Integer> fCountByLine;

	/**
	 * Public ranges factory
	 * @return composite ranges capable of mapping ranges by file
	 */
	public static SourceRanges createRanges() {
		return new SourceRanges(new HashMap<SourceFile, SourceRanges>(), null);
	}
	/**
	 * Private ranges factory
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

		if (fRangesByFile == null) {
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

		sourceRanges.add(loc.fLine, aCount);
	}

	public void add(int aLine, Integer aCount) {

		if (fCountByLine == null) {
			throw new RuntimeException("Trying to add line to COMPOSITE SourceRanges");
		}

		Integer count = fCountByLine.get(aLine);
		if (count == null) {
			count = 0;
		}
		fCountByLine.put(aLine, count + aCount);
	}

	public boolean hasFile(SourceFile aSourceFile) {
		return fRangesByFile.containsKey(aSourceFile);
	}

	public SourceRanges getSourceRanges(SourceFile aSourceFile) {
		return fRangesByFile.get(aSourceFile);
	}

	public boolean hasLine(int aLine) {
		if (fCountByLine == null) {
			throw new RuntimeException("Trying to poll for line from COMPOSITE SourceRanges");
		}
		return fCountByLine.containsKey(aLine);
	}

	public int getCount(int aLine) {
		if (fCountByLine == null) {
			throw new RuntimeException("Trying to poll for count from COMPOSITE SourceRanges");
		}
		return fCountByLine.containsKey(aLine) ? fCountByLine.get(aLine) : 0;
	}
}
