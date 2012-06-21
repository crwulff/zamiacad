package org.zamia.instgraph.interpreter.logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import org.zamia.SourceFile;
import org.zamia.SourceLocation;

/**
 * @author Anton Chepurov
 */
public class ReportUtils {

	public static Report readFromFile(File aFile) {

		Reader reader = new Reader(aFile);

		return reader.read();
	}

	private static class Reader {

		private final File fInputFile;

		private SourceFile file;

		private int line, col;

		private Report report;

		private Report.Header header;

		public Reader(File aFile) {
			fInputFile = aFile;
		}

		public Report read() {

			BufferedReader reader = null;
			try {

				reader = new BufferedReader(new FileReader(fInputFile));

				report = new Report();

				String line;
				while ((line = reader.readLine()) != null) {

					if (line.isEmpty()) {
						continue;
					}

					if (line.startsWith("###########  ")) {
						report = new Report(parseTitle(line));
						continue;
					}

					if (line.startsWith("(")) {

						file = parseFile(line);

						continue;
					}

					if (line.contains("<F|P>")) {

						if (header == null) {

							header = parseHeader(line);

							report.fHeader = header;
						}

						continue;
					}

					parseItemLine(line);
				}

				return report;

			} catch (IOException e) {
				return null;
			} finally {
				closeSilently(reader);
			}
		}

		private String parseTitle(String line) {
			return line.substring(13, line.lastIndexOf("###########")).trim();
		}

		private void parseItemLine(String line) {

			IGCodeExecutionLogger.CodeItem codeItem = parseCodeItem(line);

			parseItemLine(line, codeItem);
		}

		private void parseItemLine(String line, IGCodeExecutionLogger.CodeItem codeItem) {

			Report.FileReport fileReport = report.getFileReport(file);

			Report.FileReport.ItemLine itemLine = fileReport.getItemLine(codeItem);

			if (report.hasMarkers()) {

				int markersOffset = codeItem.toString(-1, -1).length();
				int delimIdx = line.indexOf(" <|> ");
				int afterDelim = delimIdx + 5;

				String failedMarkers = line.substring(markersOffset, delimIdx);
				String passedMarkers = line.substring(afterDelim, line.indexOf("<", afterDelim));

				itemLine.failed = parseMarkers(failedMarkers, header.failedCount);

				itemLine.passed = parseMarkers(passedMarkers, header.passedCount);

				itemLine.initStat();

			} else {

				int failedCnt = Integer.parseInt(line.substring(line.indexOf("<") + 1, line.indexOf("|")));
				int passedCnt = Integer.parseInt(line.substring(line.indexOf("|") + 1, line.indexOf(">")));

				String[] parts = line.substring(line.indexOf(">") + 1).trim().split("\\d\\)");

				double[] rates = new double[]{
						parseDouble(parts[1]),
						parseDouble(parts[2].trim()),
						parseDouble(parts[3].trim()),
						parseDouble(parts[4].trim()),
						parseDouble(parts[5].trim())
				};

				itemLine.initStat(passedCnt, failedCnt, rates);
			}
		}

		private double parseDouble(String part) {
			try {
				return NumberFormat.getInstance().parse(part.trim()).doubleValue();
			} catch (ParseException e) {
				return 0;
			}
		}

		private boolean[] parseMarkers(String markersLine, int markersCount) {

			boolean[] markers = new boolean[markersCount];

			for (int i = header.colOffset - 1, j = 0; i < markersLine.length(); i += header.colOffset, j++) {

				if (markersLine.charAt(i) == 'x')
					markers[j] = true;
			}
			return markers;
		}

		private IGCodeExecutionLogger.CodeItem parseCodeItem(String line) {

			int xIdx = line.indexOf("x");
			int ltIdx = line.indexOf("<");
			int markersOffset = xIdx > -1 && xIdx < ltIdx ? xIdx : ltIdx;

			String codeItem = line.substring(0, markersOffset).trim();

			String[] parts = codeItem.split(":");
			parts[0] = parts[0].trim();
			parts[1] = parts[1].trim();

			if (parts[0].length() > 0) {
				this.line = Integer.parseInt(parts[0]);
			}

			boolean isExpression = true;

			boolean hasTrueOccurred = false, hasFalseOccurred = false;

			if (parts[1].contains("T")) {

				hasTrueOccurred = true;

				parts = parts[1].split("\\s+");

				if (parts.length > 1) {
					this.col = Integer.parseInt(parts[0].trim());
				}

			} else if (parts[1].contains("F")) {

				hasFalseOccurred = true;

				parts = parts[1].split("\\s+");

				if (parts.length > 1) {
					this.col = Integer.parseInt(parts[0].trim());
				}

			} else {

				isExpression = false;

				this.col = Integer.parseInt(parts[1]);

			}

			SourceLocation loc = new SourceLocation(this.file, this.line, this.col);

			if (isExpression) {
				return new IGLogicalExpressionLogger.Expression(loc, hasTrueOccurred, hasFalseOccurred);
			} else {
				return new IGHitCountLogger.Hit(loc, -1);
			}
		}

		private Report.Header parseHeader(String line) {

			if (!line.contains("<F|P>")) {
				return null;
			}

			String[] parts = line.split("<F\\|P>");
			String[] failed = parts[0].trim().split("\\s+");
			String[] passed = parts[1].trim().split("\\s+");

			int failedCount = failed.length;
			int passedCount = passed.length;

			int colOffset = 0;
			for (String idx : failed)
				if (idx.length() > colOffset)
					colOffset = idx.length();
			for (String idx : passed)
				if (idx.length() > colOffset)
					colOffset = idx.length();
			colOffset++;


			return new Report.Header(line, colOffset, failedCount, passedCount);
		}

		private SourceFile parseFile(String line) {

			String localPath = line.substring(line.indexOf("(") + 1, line.indexOf(")"));

			return new SourceFile(new File(localPath), localPath);

		}
	}

	private static void closeSilently(Closeable aClosable) {
		if (aClosable != null) {
			try {
				aClosable.close();
			} catch (IOException e) {
				/* do nothing */
			}
		}
	}
}
