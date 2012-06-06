package org.zamia.analysis.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.logger.IGCodeExecutionLogger;
import org.zamia.instgraph.interpreter.logger.IGHitCountLogger;
import org.zamia.instgraph.interpreter.logger.IGLogicalExpressionLogger;
import org.zamia.instgraph.interpreter.logger.Report;
import org.zamia.instgraph.sim.ref.IGSimRef;
import org.zamia.util.FileUtils;
import org.zamia.util.PathName;

import static org.junit.Assert.assertTrue;
import static org.zamia.util.FileUtils.unzip;

/**
 * @author Anton Chepurov
 */
public class DynamicDebugAlgorithm {

	private static final ZamiaLogger logger = ZamiaLogger.getInstance();
	private static final ExceptionLogger el = ExceptionLogger.getInstance();
	private static final BigInteger MLN = new BigInteger("1000000");

	private ZamiaProject fZPrj;

	private IGSimRef fSim;

	public static void main(String[] args) throws Exception {

		DynamicDebugAlgorithm debugEngine = new DynamicDebugAlgorithm();

		if (args != null && args.length == 3) {

			debugEngine.runDebugTests(args[0], args[1], args[2]); // "1case_alu_with_overflow_bug.vhd", "alu.vhd", "4070"

		} else {

			LinkedList<String[]> tests = loadTests();

//			for (String[] test : tests)
			String[] test = tests.get(0);
				debugEngine.runDebugTests(test[0], test[1], test[2]);
		}
	}

	private void runDebugTests(String buggyFile, String correctFile, String time) throws Exception {

		cleanErados();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(buggyFile, correctFile);
		FileUtils.unzip(new File("examples/erados/src/processor/buggy_files.zip"), map);

		rebuild();

//		List<IGHitCountLogger> passedLines = new LinkedList<IGHitCountLogger>();
		List<IGHitCountLogger> passedAssignments = new LinkedList<IGHitCountLogger>();
		List<IGLogicalExpressionLogger> passedConditions = new LinkedList<IGLogicalExpressionLogger>();
		List<IGLogicalExpressionLogger> passedBranches = new LinkedList<IGLogicalExpressionLogger>();

//		List<IGHitCountLogger> failedLines = new LinkedList<IGHitCountLogger>();
		List<IGHitCountLogger> failedAssignments = new LinkedList<IGHitCountLogger>();
		List<IGLogicalExpressionLogger> failedConditions = new LinkedList<IGLogicalExpressionLogger>();
		List<IGLogicalExpressionLogger> failedBranches = new LinkedList<IGLogicalExpressionLogger>();

		// Run Tests
		for (int i = 1; i < 37; i++) {
			if (i == 27 || i == 30 || i == 31 || i == 32 || i == 36)
				continue;

			copy("SOFTWARE/SPARTAN3_STARTERKIT/TEST_PROCESSOR_PROGRAMS/" + i + "/object_code.oc.mif", "SOFTWARE/SPARTAN3_STARTERKIT/TEST_PROCESSOR_PROGRAMS/OBJECT_CODE.OC.MIF");

			fSim = openSim();
			logger.info("[%d] LEDS_LD after start: %s", i, fSim.getValue(new PathName("LEDS_LD")));
			run(time);
			IGStaticValue value = fSim.getValue(new PathName("LEDS_LD"));
			logger.info("[%d] LEDS_LD: %s", i, value);

			String id = String.valueOf(i);
//			IGHitCountLogger lineLog = fSim.collectExecutedLines(id);
			IGHitCountLogger assignmentLog = fSim.collectExecutedAssignments(id);
			IGLogicalExpressionLogger conditionLog = fSim.collectExecutedConditions(id);
			IGLogicalExpressionLogger branchLog = fSim.collectExecutedBranches(id);

			boolean ok = checkSignalValue("LEDS_LD", "11111111");

			if (ok) {
//				passedLines.add(lineLog);
				passedAssignments.add(assignmentLog);
				passedConditions.add(conditionLog);
				passedBranches.add(branchLog);
			} else {
//				failedLines.add(lineLog);
				failedAssignments.add(assignmentLog);
				failedConditions.add(conditionLog);
				failedBranches.add(branchLog);
				logger.info("Sources # %d detected the bug.", i);
			}
		}

//		printReport(passedLines, failedLines, buggyFile + "__LINES.txt", new IGLineHitLogger());
		printReport(passedAssignments, failedAssignments, buggyFile + "__ASSIGNMENTS.txt", "Assignments");
		printReport(passedConditions, failedConditions, buggyFile + "__CONDITIONS.txt", "Conditions");
		printReport(passedBranches, failedBranches, buggyFile + "__BRANCHES.txt", "Branches");

	}

	private <T extends IGCodeExecutionLogger> void printReport(List<T> aPassed, List<T> aFailed, String aFileName, String aReportTitle) {

		logger.info("Passed length: %s", aPassed.size());
		logger.info("Failed length: %s", aFailed.size());

		Report report = Report.createReport(aFailed, aPassed, aReportTitle);

		File reportFile = new File(fZPrj.fBasePath.toString(), aFileName);
		File redFile = new File(fZPrj.fBasePath.toString(), aFileName.replaceFirst("\\.txt", "__RED.txt"));

		try {
			report.print(new PrintStream(reportFile));

			report.getSuspects().print(new PrintStream(redFile));

			logger.info("Report is written to file %s", reportFile.toString());
		} catch (FileNotFoundException e) {
			logger.info("Failed to write report to file %s", reportFile.toString());
		}

	}

	private void cleanErados() throws Exception {
		initProject("examples/erados", "examples/erados/BuildPath.txt");
		copy("SOFTWARE/SPARTAN3_STARTERKIT/TEST_PROCESSOR_PROGRAMS/or/object_code.oc.mif", "SOFTWARE/SPARTAN3_STARTERKIT/TEST_PROCESSOR_PROGRAMS/OBJECT_CODE.OC.MIF");
		unzip(new File("examples/erados/src/processor/correct_files.zip"));
	}

	public void initProject(String aBasePath, String aBuildPath) throws Exception {

		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("Erados Debug Project", aBasePath, sf);
		fZPrj.clean();
	}

	private void copy(String src, String dest) {
		String base = fZPrj.fBasePath.toString();
		boolean ok = FileUtils.copy(new File(base + File.separator + src), new File(base + File.separator + dest));
		if (!ok) {
			logger.info("Failed to copy %s", src);
		}
	}

	private void rebuild() throws ZamiaException, IOException {
		ZamiaProjectBuilder builder = fZPrj.getBuilder();
		builder.build(true, true, null);
	}

	private IGSimRef openSim() throws ZamiaException, IOException {

		IGSimRef sim = new IGSimRef();

		BuildPath bp = fZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);
		ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));

		sim.open(tlp, null, null, fZPrj);

		return sim;
	}

	private void run(String time) throws ZamiaException {

		fSim.run(new BigInteger(time).multiply(MLN));
	}

	private boolean checkSignalValue(String signalName, String expectedValue) {
		IGStaticValue value = fSim.getValue(new PathName(signalName));
		return value.toString().equals(expectedValue);
	}

	private static LinkedList<String[]> loadTests() {

		LinkedList<String[]> res = new LinkedList<String[]>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("examples/erados/tests.txt"));

			String line;
			while ((line = reader.readLine()) != null) {

				String[] parts = line.split("\\t", 3);
				res.add(new String[]{parts[0], parts[1], parts[2]});
			}

		} catch (IOException e) {
			el.logException(e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					/* do nothing */
				}
			}
		}
		return res;
	}
}
