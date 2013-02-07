package org.zamia.instgraph.interpreter.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.util.XMLUtils;

import static org.zamia.util.FileUtils.closeSilently;

/**
 * @author Anton Chepurov
 */
public class ReportUtils {

	private static final ExceptionLogger el = ExceptionLogger.getInstance();
	private static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static Report readFromFile(File aFile) {

		Reader reader = new Reader(aFile);

		return reader.read();
	}

	public static void write2XMLFile(Report aReport, File aFile, Node aRootNode) {

		Report2XML writer = new Report2XML(aReport, aFile, aRootNode);

		writer.write();
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
						report.fNumTotalStmts = parseTotalStmts(line);
						String[] parts = line.split("[WLH] =");
						report.fWhatif = parseDouble(parts[1].trim());
						report.fWhatifLo = parseDouble(parts[2].trim());
						report.fWhatifHi = parseDouble(parts[3].trim());
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

				if (report.hasMarkers()) {
					report.initStat();
					report.computeWHATIF();
				}

				return report;

			} catch (IOException e) {
				return null;
			} finally {
				closeSilently(reader);
			}
		}

		private int parseTotalStmts(String line) {
			return Integer.parseInt(line.substring(line.indexOf("TotalStmts = ") + 13, line.indexOf("W =")).trim());
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

			} else {

				int failedCnt = Integer.parseInt(line.substring(line.indexOf("<") + 1, line.indexOf("|")));
				int passedCnt = Integer.parseInt(line.substring(line.indexOf("|") + 1, line.indexOf(">")));

				String[] parts = line.substring(line.indexOf(">") + 1).trim().split("[\\dWLH]\\)");

				double[] rates = new double[]{
						parseDouble(parts[1]),
						parseDouble(parts[2].trim()),
						parseDouble(parts[3].trim()),
						parseDouble(parts[4].trim()),
						parseDouble(parts[5].trim()),
						parseDouble(parts[6].trim()),
						parseDouble(parts[7].trim()),
						parseDouble(parts[8].trim())
				};

				itemLine.initStat(passedCnt, failedCnt, rates);

				if (report.fLengthWhatif == 1) {
					report.fLengthWhatif = line.indexOf("L)") - line.indexOf("W)") - 5;
					report.fLengthWhatifLo = line.indexOf("H)") - line.indexOf("L)") - 5;
					report.fLengthWhatifHi = line.length() - line.indexOf("H)") - 3;
				}
			}
		}

		private double parseDouble(String part) {
			try {
				if (part.trim().equals("NaN")) {
					return Double.NaN;
				}
				return NumberFormat.getInstance(Locale.US).parse(part.trim()).doubleValue();
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
			parts[0] = parts[0].trim();
			parts[1] = parts[1].trim();
			String[] failed = parts[0].isEmpty() ? new String[0] :  parts[0].split("\\s+");
			String[] passed = parts[1].isEmpty() ? new String[0] :  parts[1].split("\\s+");

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

	private static class Report2XML {

		private Report fReport;
		private File fFile;
		private Document fXml;
		private Node fRootNode;

		public Report2XML(Report aReport, File aFile, Node aRootNode) {
			fReport = aReport;
			fFile = aFile;
			fXml = aRootNode.getOwnerDocument();
			fRootNode = aRootNode;
		}

		public void write() {

			try {

				dumpReport2XML();

				XMLUtils.xml2file(fXml, fFile);

			} catch (ZamiaException e) {
				logger.info("### ERROR ### when dumping Report to XML: " + e.getMessage());
				el.logException(e);
			}
		}

		private void dumpReport2XML() throws ZamiaException {

			if (XMLUtils.hasNode("result", fRootNode)) {
				return;
			}

			Element resultNode = XMLUtils.createNodeIn(fRootNode, "result", fXml);

			String task = XMLUtils.getAttribute("name", fRootNode);

			if (task.equals("debug")) {

				for (SourceFile sourceFile : fReport.getFiles()) {

					Element fileNode = XMLUtils.createNodeIn(resultNode, "file", fXml);
					fileNode.setAttribute("path", sourceFile.getLocalPath());

					for (Report.FileReport.ItemLine itemLine : fReport.getLines(sourceFile)) {

						Element line = XMLUtils.createNodeIn(fileNode, "line", fXml);
						line.setTextContent(String.valueOf(itemLine.getLine()));
						String color = itemLine.getColor();
						if (color != null) {
							line.setAttribute("color", color);
						}
					}
				}
			}
		}
	}
}
