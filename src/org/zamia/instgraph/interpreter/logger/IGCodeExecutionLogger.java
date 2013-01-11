package org.zamia.instgraph.interpreter.logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;

/**
 * @author Anton Chepurov
 */
public abstract class IGCodeExecutionLogger {

	final String fId;

	final Map<SourceFile, IGCodeExecutionLogger> fLoggersByFile;

	final TreeSet<CodeItem> fItems;

	IGCodeExecutionLogger(String aId, boolean aIsComposite) {
		fId = aId == null ? "X" : aId;
		fLoggersByFile = aIsComposite ? new HashMap<SourceFile, IGCodeExecutionLogger>() : null;
		fItems = aIsComposite ? null : new TreeSet<CodeItem>();
	}

	abstract IGCodeExecutionLogger createLogger();

	abstract IGCodeExecutionLogger createLeafLogger();

	public boolean hasLoggerForFile(SourceFile aSourceFile) {
		if (isLeaf()) {
			throw new RuntimeException("Checking for file logger in LEAF logger");
		}
		return fLoggersByFile.containsKey(aSourceFile);
	}

	public IGCodeExecutionLogger getLogger(SourceFile aSourceFile) {
		if (isLeaf()) {
			throw new RuntimeException("Trying to get file logger from LEAF logger");
		}
		return fLoggersByFile.get(aSourceFile);
	}

	boolean isLeaf() {
		return fLoggersByFile == null;
	}

	boolean isComposite() {
		return fLoggersByFile != null;
	}

	private IGCodeExecutionLogger getOrCreateLogger(SourceFile aSourceFile) {

		if (isLeaf()) {
			throw new RuntimeException("Trying to get file logger from LEAF logger");
		}

		IGCodeExecutionLogger logger = fLoggersByFile.get(aSourceFile);
		if (logger == null) {
			logger = createLeafLogger();
			fLoggersByFile.put(aSourceFile, logger);
		}

		return logger;
	}

	void logItemComposite(CodeItem aNewItem) {

		if (aNewItem.fLoc == null) {
			return;
		}

		IGCodeExecutionLogger fileLogger = getOrCreateLogger(aNewItem.fLoc.fSF);

		fileLogger.logItemLeaf(aNewItem);
	}

	void logItemLeaf(CodeItem aNewItem) {

		if (aNewItem == null) {
			return;
		}

		if (fItems.contains(aNewItem)) {

			CodeItem existingItem = getItem(aNewItem);

			existingItem.merge(aNewItem);

		} else {

			fItems.add(aNewItem);
		}
	}

	CodeItem getItem(CodeItem aItemToFind) {
		return fItems.tailSet(aItemToFind).first();
	}

	@SuppressWarnings("UnusedDeclaration")
	public int getNumItems() {
		int sum = 0;
		if (isComposite()) {
			for (IGCodeExecutionLogger fileLogger : fLoggersByFile.values()) {
				sum += fileLogger.fItems.size();
			}
		} else {
			sum = fItems.size();
		}
		return sum;
	}

	public void dropSystemFiles() {
		if (isLeaf()) {
			return;
		}
		LinkedList<SourceFile> systemFiles = new LinkedList<SourceFile>();
		for (SourceFile file : fLoggersByFile.keySet()) {
			if (!file.isLocal()) {
				systemFiles.add(file);
			}
		}
		for (SourceFile sysFile : systemFiles) {
			fLoggersByFile.remove(sysFile);
		}
	}

	public static IGCodeExecutionLogger mergeAll(IGCodeExecutionLogger... aLoggers) throws ZamiaException {

		IGCodeExecutionLogger result = null;

		for (IGCodeExecutionLogger logger : aLoggers) {

			if (result == null) {
				result = logger.createLogger();
			}

			logger.mergeCompositesInto(result);
		}

		return result;
	}

	private void mergeCompositesInto(IGCodeExecutionLogger aResult) {

		for (Map.Entry<SourceFile, IGCodeExecutionLogger> entry : fLoggersByFile.entrySet()) {
			SourceFile thisFile = entry.getKey();
			IGCodeExecutionLogger thisLogger = entry.getValue();

			IGCodeExecutionLogger resultLeaf;

			if (aResult.hasLoggerForFile(thisFile))
				resultLeaf = aResult.getLogger(thisFile);
			else {
				resultLeaf = aResult.createLeafLogger();
				aResult.fLoggersByFile.put(thisFile, resultLeaf);
			}

			resultLeaf.mergeLeaves(thisLogger);
		}
	}

	void mergeLeaves(IGCodeExecutionLogger aOtherLogger) {

		for (CodeItem otherItem : aOtherLogger.fItems) {

			logItemLeaf(otherItem);
		}
	}

	public IGCodeExecutionLogger subtractAll(IGCodeExecutionLogger... aOthers) throws ZamiaException {

		IGCodeExecutionLogger result = this;

		for (IGCodeExecutionLogger other : aOthers) {

			result = result.subtractComposites(other);
		}

		return result;
	}

	private IGCodeExecutionLogger subtractComposites(IGCodeExecutionLogger aOther) throws ZamiaException {

		IGCodeExecutionLogger result = createLogger();

		for (Map.Entry<SourceFile, IGCodeExecutionLogger> entry : fLoggersByFile.entrySet()) {
			SourceFile thisFile = entry.getKey();
			IGCodeExecutionLogger thisLogger = entry.getValue();

			IGCodeExecutionLogger resultLeaf = thisLogger;

			if (aOther.hasLoggerForFile(thisFile)) {

				resultLeaf = thisLogger.subtractLeaves(aOther.getLogger(thisFile));

				if (resultLeaf.isEmpty()) {
					continue;
				}
			}

			result.fLoggersByFile.put(thisFile, resultLeaf);
		}

		return result;
	}

	private IGCodeExecutionLogger subtractLeaves(IGCodeExecutionLogger aOther) {

		IGCodeExecutionLogger result = createLeafLogger();

		for (CodeItem thisItem : fItems) {

			if (aOther.fItems.contains(thisItem)) {

				thisItem = thisItem.subtract(aOther.getItem(thisItem));
			}

			result.logItemLeaf(thisItem);
		}

		return result;
	}

	private boolean isEmpty() {
		return isComposite() ? fLoggersByFile.isEmpty() : fItems.isEmpty();
	}

	public abstract static class CodeItem implements Comparable<CodeItem>, Cloneable {

		public final SourceLocation fLoc;

		public CodeItem(SourceLocation aLocation) {
			fLoc = aLocation;
		}

		@Override
		public String toString() {
			return String.format("%5s:%3s", fLoc.fLine, fLoc.fCol);
		}

		public abstract void merge(CodeItem aOther);

		public abstract CodeItem subtract(CodeItem aOther);

		@Override
		public int compareTo(CodeItem o) {

			int result = fLoc.fSF.getAbsolutePath().compareTo(o.fLoc.fSF.getAbsolutePath());
			if (result != 0) {
				return result;
			}

			result = Integer.valueOf(fLoc.fLine).compareTo(o.fLoc.fLine);
			if (result != 0) {
				return result;
			}

			return Integer.valueOf(fLoc.fCol).compareTo(o.fLoc.fCol);
		}

		public String toString(int lastLine, int lastCol) {
			String line = fLoc.fLine > lastLine ? String.valueOf(fLoc.fLine) : "";
			String col = fLoc.fLine > lastLine || fLoc.fCol > lastCol ? String.valueOf(fLoc.fCol) : "";
			return String.format("%5s:%3s", line, col);
		}

		@SuppressWarnings("RedundantIfStatement")
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			CodeItem codeItem = (CodeItem) o;

			if (!fLoc.equals(codeItem.fLoc)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return fLoc.hashCode();
		}
	}

	@Override
	public String toString() {
		if (isComposite()) {
			StringBuilder sb = new StringBuilder("[").append(fId).append("]");
			sb.append(fLoggersByFile);
			return sb.toString();
		} else {
			return "CodeItems{" + fItems + '}';
		}
	}
}
