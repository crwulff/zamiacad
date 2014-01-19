package org.zamia.instgraph.interpreter.logger;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Node;

import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.interpreter.logger.IGCodeExecutionLogger.CodeItem;
import org.zamia.instgraph.interpreter.logger.IGHitCountLogger.Hit;
import org.zamia.instgraph.interpreter.logger.IGLogicalExpressionLogger.Expression;

/**
 * @author Anton Chepurov
 */
public class Report {

	private static final boolean OVER_TOTAL = false; // default = FALSE
	private static final boolean DROP_ALWAYS_COVERED = false; // default = FALSE

	String fTitle;

	Header fHeader;

	public double fWhatif = -1;
	double fWhatifHi = -1;
	double fWhatifLo = -1;
	double fWhatifEmph = -1;
	double fWhatifEmph2 = -1;
	int fLengthWhatif = 1, fLengthWhatifHi = 1, fLengthWhatifLo = 1, fLengthHamming = 1;
	int fNumTotalStmts = -1;

	private TreeMap<SourceFile, FileReport> fFileReports = new TreeMap<SourceFile, FileReport>(LOCATION_COMPARATOR);

	private static final ZamiaLogger logger = ZamiaLogger.getInstance();

	private HashMap<SourceFile, HashMap<Criterion, Report>> fFilteredReports;

	// used on creation only
	private int fLoggerId = 0;

	Report() {
		this(null);
	}

	Report(String aTitle) {
		fTitle = aTitle;
	}


	public static Report createReport(List<? extends IGCodeExecutionLogger> aLoggers, int aNumTotalStmts, String aTitle) {

		return createReport(Collections.<IGHitCountLogger>emptyList(), aLoggers, aNumTotalStmts, aTitle);
	}

	public static Report createReport(List<? extends IGCodeExecutionLogger> aFailedLoggers,
									  List<? extends IGCodeExecutionLogger> aPassedLoggers,
									  String aTitle) {
		return createReport(aFailedLoggers, aPassedLoggers, -1, aTitle);
	}

	public static Report createReport(List<? extends IGCodeExecutionLogger> aFailedLoggers,
									  List<? extends IGCodeExecutionLogger> aPassedLoggers,
									  int aNumTotalStmts, String aTitle) {

		Report report = new Report(aTitle);

		report.fHeader = generateHeader(aFailedLoggers, aPassedLoggers);

		report.processLoggers(aFailedLoggers);

		report.processLoggers(aPassedLoggers);

		report.initStat();

		report.fNumTotalStmts = aNumTotalStmts;

		report.computeWHATIF();

		return report;
	}

	public final static Comparator<SourceFile> LOCATION_COMPARATOR = new Comparator<SourceFile>() {
		@Override
		public int compare(SourceFile o1, SourceFile o2) {
			return o1.getFileName().compareToIgnoreCase(o2.getFileName());
		}
	};

	private final static Comparator<CodeItem> ITEM_COMPARATOR = new Comparator<CodeItem>() {
		@Override
		public int compare(CodeItem o1, CodeItem o2) {

			int result = o1.compareTo(o2);
			if (result != 0) {
				return result;
			}

			if (o1 instanceof Expression && o2 instanceof Expression) {

				Expression first = (Expression) o1;
				Expression second = (Expression) o2;

				return first.getLogicValue().compareTo(second.getLogicValue());
			}

			return result;
		}
	};

	private void processLoggers(List<? extends IGCodeExecutionLogger> aLoggers) {

		for (IGCodeExecutionLogger logger : aLoggers) {

			for (Map.Entry<SourceFile, IGCodeExecutionLogger> entry : logger.fLoggersByFile.entrySet()) {
				SourceFile file = entry.getKey();
				IGCodeExecutionLogger fileLogger = entry.getValue();

				if (file.getFile() == null) {
					continue;
				}

				FileReport fileReport = getFileReport(file);

				for (CodeItem item : fileLogger.fItems)

					fileReport.addItem(item);

				if (fileReport.lines.isEmpty()) {

					fFileReports.remove(file);
				}
			}

			fLoggerId++;
		}
	}

	FileReport getFileReport(SourceFile aFile) {

		if (!fFileReports.containsKey(aFile)) {
			fFileReports.put(aFile, new FileReport(aFile));
		}
		return fFileReports.get(aFile);
	}

	void computeWHATIF() {

		if (fNumTotalStmts == -1) {
			return;
		}

		fWhatif = 0;
		fWhatifHi = 0;
		fWhatifLo = 0;
		fWhatifEmph = 0;

		ArrayList<FileReport.ItemLine> stmts = new ArrayList<FileReport.ItemLine>(fNumTotalStmts);
		for (FileReport fileReport : fFileReports.values()) {
			for (FileReport.ItemLine itemLine : fileReport.lines.values()) {
				if (DROP_ALWAYS_COVERED) {
					if (itemLine.isAlwaysCovered()) {
						continue;
					}
				}
				stmts.add(itemLine);
			}
		}

		int numTests = fHeader.passedCount;

		int maxWhatif = 0, maxWhatifHi = 0, maxWhatifLo = 0, maxHamming = 0;
		for (FileReport.ItemLine stmt : stmts) {

			HashSet<Integer> failingTests = new HashSet<Integer>();
			for (int test = 0; test < numTests; test++) {
				if (stmt.passed[test]) {
					failingTests.add(test);
				}
			}

			int tf = failingTests.size();
			int tp = numTests - tf;

			int whatif = 1, whatifHi = 1, whatifLo = 1;
			for (FileReport.ItemLine candidateStmt : stmts) {

				if (candidateStmt == stmt) {
					continue;
				}
				// compute potential fails and passes for the candidateStmt
				HashSet<Integer> potFails = new HashSet<Integer>();
				HashSet<Integer> potPasses = new HashSet<Integer>();
				for (int test = 0; test < numTests; test++) {
					if (candidateStmt.passed[test]) {
						if (failingTests.contains(test)) {
							potFails.add(test);
						} else {
							potPasses.add(test);
						}
					}
				}

				if (potFails.isEmpty()) {
					continue;
				}

				whatifHi++;

				double score = computeStat1(potPasses.size(), tp, potFails.size(), tf);
				if (isSuspect(score)) {
					whatif++;
				}

				// count all identical candidates
				if (potPasses.isEmpty() && potFails.containsAll(failingTests)) {
					whatifLo++;
				}
			}

			stmt.stat.whatif = whatif;
			stmt.stat.whatifHi = whatifHi;
			stmt.stat.whatifLo = whatifLo;
			fWhatif += whatif;
			fWhatifHi += whatifHi;
			fWhatifLo += whatifLo;
			maxWhatif = whatif > maxWhatif ? whatif : maxWhatif;
			maxWhatifHi = whatifHi > maxWhatifHi ? whatifHi : maxWhatifHi;
			maxWhatifLo = whatifLo > maxWhatifLo ? whatifLo : maxWhatifLo;

			int hamming = 0;
			for (FileReport.ItemLine candidateStmt : stmts) {

				if (candidateStmt == stmt) {
					continue;
				}

				hamming += computeHammingDistance(stmt, candidateStmt);
			}
			stmt.stat.hamming = hamming;
			maxHamming = hamming > maxHamming ? hamming : maxHamming;
		}

		if (OVER_TOTAL) {
			fWhatif /= (double) fNumTotalStmts;
			fWhatifHi /= (double) fNumTotalStmts;
			fWhatifLo /= (double) fNumTotalStmts;
		} else {
			fWhatif /= (double) stmts.size();
			fWhatifHi /= (double) stmts.size();
			fWhatifLo /= (double) stmts.size();
		}

		fLengthWhatif = String.valueOf(maxWhatif).length();
		fLengthWhatifHi = String.valueOf(maxWhatifHi).length();
		fLengthWhatifLo = String.valueOf(maxWhatifLo).length();
		fLengthHamming = String.valueOf(maxHamming).length();

		for (FileReport.ItemLine stmt : stmts) {
			fWhatifEmph += Math.pow((double) stmt.stat.whatif - fWhatif, 2);
		}
		if (OVER_TOTAL) {
			fWhatifEmph2 = Math.sqrt(fWhatifEmph / (double) fNumTotalStmts);
			fWhatifEmph /= (double) fNumTotalStmts;
		} else {
			fWhatifEmph2 = Math.sqrt(fWhatifEmph / (double) stmts.size());
			fWhatifEmph /= (double) stmts.size();
		}
	}

	private static int computeHammingDistance(FileReport.ItemLine first, FileReport.ItemLine second) {
		int dist = 0;
		for (int test = 0; test < first.passed.length; test++) {
			if (first.passed[test] ^ second.passed[test])
				dist++;
		}
		return dist;
	}

	void initStat() {

		for (FileReport fileReport : fFileReports.values()) {

			for (FileReport.ItemLine itemLine : fileReport.lines.values()) {

				itemLine.initStat();
			}
		}
	}

	private static Header generateHeader(List<? extends IGCodeExecutionLogger> aFailedLoggers, List<? extends IGCodeExecutionLogger> aPassedLoggers) {

		IGCodeExecutionLogger anyLogger = !aFailedLoggers.isEmpty() ? aFailedLoggers.get(0) : aPassedLoggers.get(0);

		String headerOffset = "";
		if (anyLogger instanceof IGLogicalExpressionLogger) {
			headerOffset = "            ";
		} else if (anyLogger instanceof IGHitCountLogger) {
			headerOffset = "         ";
		}

		int maxIdLength = 0;
		maxIdLength = computeMaxId(aFailedLoggers, maxIdLength);
		maxIdLength = computeMaxId(aPassedLoggers, maxIdLength);

		int colOffset = maxIdLength + 1;
		String format = "%" + colOffset + "s";

		StringBuilder b = new StringBuilder(headerOffset);
		for (IGCodeExecutionLogger logger : aFailedLoggers) {
			b.append(String.format(format, logger.fId));
		}
		b.append("<F|P>");
		for (IGCodeExecutionLogger logger : aPassedLoggers) {
			b.append(String.format(format, logger.fId));
		}

		return new Header(b.toString(), colOffset, aFailedLoggers.size(), aPassedLoggers.size());
	}

	private static int computeMaxId(List<? extends IGCodeExecutionLogger> aLoggers, int aMaxIdLength) {
		for (IGCodeExecutionLogger logger : aLoggers) {
			int idLength = logger.fId.length();
			if (idLength > aMaxIdLength) {
				aMaxIdLength = idLength;
			}
		}
		return aMaxIdLength;
	}

	public boolean hasFile(SourceFile aSourceFile) {
		return fFileReports.containsKey(aSourceFile);
	}

	public Collection<FileReport.ItemLine> getLines(SourceFile aSourceFile) {

		FileReport fileReport = fFileReports.get(aSourceFile);

		if (fileReport == null)
			return Collections.emptyList();

		return fileReport.lines.values();
	}

	public boolean hasMarkers() {
		return fHeader != null;
	}

	public Set<SourceFile> getFiles() {
		return fFileReports.keySet();
	}

	static class Header {

		String header;

		int colOffset;

		int failedCount;
		int passedCount;

		public Header(String header, int colOffset, int failedCount, int passedCount) {
			this.header = header;
			this.colOffset = colOffset;
			this.failedCount = failedCount;
			this.passedCount = passedCount;
		}

		@Override
		public String toString() {
			return header;
		}
	}

	public void printStat() {
		printStat(null);
	}

	public void printStat(PrintStream out) {
		withHits = false;
		print(out);
		withHits = true;
	}

	private static boolean withHits = true;

	public void print() {
		print(null);
	}

	public void print(PrintStream out) {

		print(out, "");
		print(out, "###########  %s  ###########    TotalStmts = %d   W = %.2f  L = %.2f  H = %.2f  We = %.2f  We2 = %.2f", fTitle, fNumTotalStmts, fWhatif, fWhatifLo, fWhatifHi, fWhatifEmph, fWhatifEmph2);
		print(out, "");

		for (FileReport fileReport : fFileReports.values()) {

			print(out, "%s:", fileReport.file);

			if (withHits && hasMarkers()) {
				print(out, "%s", fHeader);
			}

			fileReport.print(out);

		}
	}

	private static void print(PrintStream out, String line, Object... args) {

		if (out == null) {
			logger.info(line, args);
		} else {
			out.printf(Locale.US, line + "\n", args);
		}
	}

	public class FileReport {

		SourceFile file;

		TreeMap<CodeItem, ItemLine> lines = new TreeMap<CodeItem, ItemLine>(ITEM_COMPARATOR);

		public FileReport(SourceFile file) {
			this.file = file;
		}

		public void addItem(CodeItem aItem) {

			if (aItem instanceof Hit) {

				markItem(aItem);

			} else if (aItem instanceof Expression) {

				Expression expr = (Expression) aItem;

				if (expr.fHasTrueOccurred) {

					Expression trueItem = new Expression(expr.fLoc, true, false);

					markItem(trueItem);
				}

				if (expr.fHasFalseOccurred) {

					Expression falseItem = new Expression(expr.fLoc, false, true);

					markItem(falseItem);
				}
			}
		}

		private void markItem(CodeItem aItem) {

			ItemLine itemLine = getItemLine(aItem);

			itemLine.mark();
		}

		Report.FileReport.ItemLine getItemLine(CodeItem aItem) {

			if (!lines.containsKey(aItem)) {
				lines.put(aItem, new Report.FileReport.ItemLine(aItem));
			}

			return lines.get(aItem);
		}

		public void print(PrintStream out) {

			int lastLine = -1, lastCol = -1;
			for (ItemLine itemLine : lines.values()) {

				String line = itemLine.toString(lastLine, lastCol);

				Report.print(out, "%s", line);

				if (lastLine < itemLine.item.fLoc.fLine) {
					lastLine = itemLine.item.fLoc.fLine;
					lastCol = -1;
				}
				if (lastCol < itemLine.item.fLoc.fCol) {
					lastCol = itemLine.item.fLoc.fCol;
				}
			}
		}

		private double getThreshold1() {
			return 0.6;
		}

		private double getThreshold2() {
			return 0.75;
		}

		public class ItemLine {

			CodeItem item;

			boolean[] failed = null;

			boolean[] passed = null;

			Stat stat;

			private ItemLine(CodeItem aItem) {
				item = aItem;
				if (hasMarkers()) {
					failed = new boolean[fHeader.failedCount];
					passed = new boolean[fHeader.passedCount];
				}
			}

			public void mark() {

				int idx = fLoggerId;
				boolean[] dest = failed;
				if (idx > dest.length - 1) {
					idx = idx - dest.length;
					dest = passed;
				}
				dest[idx] = true;
			}

			@Override
			public String toString() {
				return item + " " + stat;
			}

			public String toString(int lastLine, int lastCol) {

				StringBuilder b = new StringBuilder();

				b.append(item.toString(lastLine, lastCol));

				if (withHits && hasMarkers()) {
					printMarkers(b, failed);
					b.append(" <|> ");
					printMarkers(b, passed);
				}

				b.append("  ").append(stat);

				return b.toString();
			}

			private void printMarkers(StringBuilder b, boolean[] marked) {
				for (boolean isMarked : marked) {
					b.append(String.format("%" + fHeader.colOffset + "s", isMarked ? "x" : ""));
				}
			}

			void initStat() {

				int passedCnt = countItems(passed);
				int failedCnt = countItems(failed);
				int totalPassed = passed.length;
				int totalFailed = failed.length;

				stat = new Stat(passedCnt, failedCnt, totalPassed, totalFailed);

			}

			public void initStat(int aPassedCnt, int aFailedCnt, double[] aRates) {

				stat = new Stat(aPassedCnt, aFailedCnt, aRates[0], aRates[1], aRates[2], aRates[3], aRates[4], (int) aRates[5], (int) aRates[6], (int) aRates[7], (int) aRates[8]);
			}

			private int countItems(boolean[] items) {
				int passedCnt = 0;
				for (boolean isPassed : items) {
					if (isPassed) {
						passedCnt++;
					}
				}
				return passedCnt;
			}

			public boolean isAlwaysCovered() {
				return passed.length == stat.passedCnt;
			}

			public boolean isGreen() {
				return !isSuspect();
			}

			public boolean isSuspect() {
				return Report.isSuspect(getV1());
			}

			public boolean isYellow() {
				double v1 = getV1();
				return v1 > 0.5 && v1 < getThreshold1();
			}

			public boolean isOrange() {
				double v1 = getV1();
				return v1 >= getThreshold1() && v1 < getThreshold2();
			}

			public boolean isRed() {
				return getV1() >= getThreshold2();
			}

			public int getLine() {
				return item.fLoc.fLine;
			}

			public int getCol() {
				return item.fLoc.fCol;
			}

			public double getV1() {
				return stat.v1;
			}

			public String getMarkerMessage() {

				if (!isSuspect()) {
					return "";
				}

				String codeItem = item instanceof Hit ? "assignment" : "condition";

				return String.format("This %s (%d:%d) looks suspicious (s=%.3f)", codeItem, getLine(), getCol(), stat.v1);
			}

			public boolean satisfies(Criterion aCriterion) {
				return aCriterion.isFulfilledBy(this);
			}

			public String getColor() {
				if (isRed()) {
					return "red";
				} else if (isOrange()) {
					return "orange";
				} else if (isYellow()) {
					return "yellow";
				} else {
					return "green";
				}
			}

			class Stat {

				final int passedCnt, failedCnt;

				final double v1, v2, v3, v4, v5;

				int whatif, whatifHi, whatifLo, hamming;

				private Stat(int aPassedCnt, int aFailedCnt, int aTotalPassed, int aTotalFailed) {

					this(
							aPassedCnt, aFailedCnt,
							computeStat1(aPassedCnt, aTotalPassed, aFailedCnt, aTotalFailed),
							computeStat2(aPassedCnt, aTotalPassed, aFailedCnt, aTotalFailed),
							computeStat3(aPassedCnt, aTotalPassed, aFailedCnt, aTotalFailed),
							computeStat4(aPassedCnt, aTotalPassed, aFailedCnt, aTotalFailed),
							computeStat5(aPassedCnt, aTotalPassed, aFailedCnt, aTotalFailed),
							-1, -1, -1, -1
					);
				}

				private Stat(int aPassedCnt, int aFailedCnt, double v1, double v2, double v3, double v4, double v5, int whatif, int whatifLo, int whatifHi, int hamming) {

					this.passedCnt = aPassedCnt;
					this.failedCnt = aFailedCnt;

					this.v1 = v1;
					this.v2 = v2;
					this.v3 = v3;
					this.v4 = v4;
					this.v5 = v5;

					this.whatif = whatif;
					this.whatifLo = whatifLo;
					this.whatifHi = whatifHi;
					this.hamming = hamming;
				}

				@Override
				public String toString() {

					StringBuilder b = new StringBuilder();
					b.append(String.format("%3s|%-5s", "<" + failedCnt, passedCnt + ">"));
					b.append(String.format(Locale.US, "1) %.3f  ", v1));
					b.append(String.format(Locale.US, "2) %.3f  ", v2));
					b.append(String.format(Locale.US, "3) %.3f  ", v3));
					b.append(String.format(Locale.US, "4) %.3f  ", v4));
					b.append(String.format(Locale.US, "5) %.3f  ", v5));
					b.append(String.format("W) %" + fLengthWhatif + "d  ", whatif));
					b.append(String.format("L) %" + fLengthWhatifLo + "d  ", whatifLo));
					b.append(String.format("H) %" + fLengthWhatifHi + "d  ", whatifHi));
					b.append(String.format("Ham) %" + fLengthHamming + "d", hamming));

					return b.toString();
				}
			}
		}
	}

	public static Report readFromFile(File aFile) {

		return ReportUtils.readFromFile(aFile);
	}

	public void write2XMLFile(File aXmlFile, Node aRootNode) {

		ReportUtils.write2XMLFile(this, aXmlFile, aRootNode);
	}

	private static abstract class Criterion {
		private final String fTitle;

		protected Criterion(String aTitle) {
			fTitle = aTitle;
		}

		abstract boolean isFulfilledBy(FileReport.ItemLine itemLine);

		@Override
		public String toString() {
			return fTitle;
		}
	}

	private static final Criterion SUSPECTS_FILTER = new Criterion("SUSPECTS") {
		@Override
		public boolean isFulfilledBy(FileReport.ItemLine itemLine) {
			return itemLine.isSuspect();
		}
	};
	private static final Criterion RED_FILTER = new Criterion("RED") {
		@Override
		public boolean isFulfilledBy(FileReport.ItemLine itemLine) {
			return itemLine.isRed();
		}
	};
	private static final Criterion ORANGE_FILTER = new Criterion("ORANGE") {
		@Override
		public boolean isFulfilledBy(FileReport.ItemLine itemLine) {
			return itemLine.isOrange();
		}
	};
	private static final Criterion YELLOW_FILTER = new Criterion("YELLOW") {
		@Override
		public boolean isFulfilledBy(FileReport.ItemLine itemLine) {
			return itemLine.isYellow();
		}
	};
	private static final Criterion GREEN_FILTER = new Criterion("GREEN") {
		@Override
		public boolean isFulfilledBy(FileReport.ItemLine itemLine) {
			return itemLine.isGreen();
		}
	};

	public Report getSuspects() {
		return getFiltered(null, SUSPECTS_FILTER);
	}

	public Report getRedSuspects(SourceFile aFile) {
		return getFiltered(aFile, RED_FILTER);
	}

	public Report getOrangeSuspects(SourceFile aFile) {
		return getFiltered(aFile, ORANGE_FILTER);
	}

	public Report getYellowSuspects(SourceFile aFile) {
		return getFiltered(aFile, YELLOW_FILTER);
	}

	public Report getGreen(SourceFile aFile) {
		return getFiltered(aFile, GREEN_FILTER);
	}

	private Report getFiltered(SourceFile aFile, Criterion aCriterion) {
		if (fFilteredReports == null || !fFilteredReports.containsKey(aFile)) {
			filterItems(aFile);
		}
		return fFilteredReports.get(aFile).get(aCriterion);
	}

	private void filterItems(SourceFile aFile) {

		if (fFilteredReports == null) {
			fFilteredReports = new HashMap<SourceFile, HashMap<Criterion, Report>>();
		}

		if (!fFilteredReports.containsKey(aFile)) {
			fFilteredReports.put(aFile, new HashMap<Criterion, Report>());
		}

		HashMap<Criterion, Report> reportsByCriterion = fFilteredReports.get(aFile);

		Criterion[] criteria = new Criterion[]{SUSPECTS_FILTER, RED_FILTER, ORANGE_FILTER, YELLOW_FILTER, GREEN_FILTER};

		for (Criterion criterion : criteria) {
			Report result = new Report(fTitle + ": " + criterion.fTitle);
			result.fHeader = fHeader;

			reportsByCriterion.put(criterion, result);
		}

		for (FileReport fileReport : fFileReports.values()) {

			if (aFile != null && aFile.compareTo(fileReport.file) != 0) {
				continue;
			}

			for (FileReport.ItemLine itemLine : fileReport.lines.values()) {

				for (Criterion criterion : criteria) {

					if (itemLine.satisfies(criterion)) {

						Report result = reportsByCriterion.get(criterion);

						FileReport resultFileReport = result.getFileReport(fileReport.file);

						resultFileReport.lines.put(itemLine.item, itemLine);
					}
				}

			}

			for (Criterion criterion : criteria) {

				Report result = reportsByCriterion.get(criterion);

				FileReport resultFileReport = result.getFileReport(fileReport.file);

				if (resultFileReport.lines.isEmpty()) {
					result.fFileReports.remove(resultFileReport.file);
				}
			}
		}
	}

	@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
	private static double computeStat1(double p, double tp, double f, double tf) {

		if (tf == 0) {
			return 0;
		} else if (tp == 0) {
			return 1;
		}

		return (f / tf) / (p / tp + f / tf);
	}

	@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
	private static double computeStat2(double p, double tp, double f, double tf) {

		return f / (p + tf);
	}

	@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
	private static double computeStat3(double p, double tp, double f, double tf) {

		return f / Math.sqrt(tf * (p + f));
	}

	@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
	private static double computeStat4(double p, double tp, double f, double tf) {

		return Math.abs((f / tf) - (p / tp));
	}

	@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
	private static double computeStat5(double p, double tp, double f, double tf) {

		return f / tf;
	}

	private static boolean isSuspect(double aScore) {
		return aScore > 0.5;
	}
}
