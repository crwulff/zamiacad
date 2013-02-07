package org.zamia.instgraph.interpreter.logger;

import org.junit.After;
import org.junit.Test;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Anton Chepurov
 */
public class DiagnosticTestQualityTest {

	DiagnosticTestQuality testQuality;

	@After
	public void printHamming() {
		System.out.println(testQuality);
	}

	@Test
	public void testLength3() {

		/*
		* ************************
		*  1
		*    1 1
		*    1
		*    1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(4);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile("Test3.txt");
		file.setLocalPath("some path");
		file.setNumLines(4);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);

		testQuality = DiagnosticTestQuality.createFrom(loggers, allAssignmentsLogger);

		assertNotNull(testQuality);

		assertThat(testQuality.getNumTests(file), is(3));
		assertThat(testQuality.getNumTests(), is(3));

		assertThat(testQuality.getSum(file, 0), is(6));
		assertThat(testQuality.getSum(file, 1), is(6));
		assertThat(testQuality.getSum(file, 2), is(4));
		assertThat(testQuality.getMin(file, 0), is(2));
		assertThat(testQuality.getMin(file, 1), is(2));
		assertThat(testQuality.getMin(file, 2), is(2));
		assertThat(testQuality.getAverage(file, 0), is((double) 6 / 2));
		assertThat(testQuality.getAverage(file, 1), is((double) 6 / 2));
		assertThat(testQuality.getAverage(file, 2), is((double) 4 / 2));
		assertThat(testQuality.getUniquenessJaan(), is(10.5));
		assertThat(testQuality.getUniquenessMaksim(), is((double) 1 + ((double) 2 / 3)));
		assertThat(testQuality.getSum(), is(16));
		assertThat(testQuality.getSumNormRaimund(), is((double) 8 / 3));
	}

	@Test
	public void testLength4() {

		/*
		* ************************
		*  1
		*    1 1 1
		*    1
		*    1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(4);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile("Test4.txt");
		file.setLocalPath("some path");
		file.setNumLines(4);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);

		testQuality = DiagnosticTestQuality.createFrom(loggers, allAssignmentsLogger);

		assertNotNull(testQuality);

		assertThat(testQuality.getNumTests(), is(4));
		assertThat(testQuality.getNumTests(file), is(4));

		assertThat(testQuality.getSum(file, 0), is(8));
		assertThat(testQuality.getSum(file, 1), is(8));
		assertThat(testQuality.getSum(file, 2), is(4));
		assertThat(testQuality.getSum(file, 3), is(4));
		assertThat(testQuality.getMin(file, 0), is(2));
		assertThat(testQuality.getMin(file, 1), is(2));
		assertThat(testQuality.getMin(file, 2), is(0));
		assertThat(testQuality.getMin(file, 3), is(0));
		assertThat(testQuality.getAverage(file, 0), is((double) 8 / 3));
		assertThat(testQuality.getAverage(file, 1), is((double) 8 / 3));
		assertThat(testQuality.getAverage(file, 2), is((double) 4 / 3));
		assertThat(testQuality.getAverage(file, 3), is((double) 4 / 3));
		assertThat(testQuality.getSum(), is(24));
		assertThat(testQuality.getSumNormRaimund(), is((double) 2));
	}

	@Test
	public void testLength5() {

		/*
		* ************************
		*  1
		*    1 1 1 1
		*    1
		*    1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(4);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile("Test5.txt");
		file.setLocalPath("some path");
		file.setNumLines(4);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test5");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);

		testQuality = DiagnosticTestQuality.createFrom(loggers, allAssignmentsLogger);

		assertNotNull(testQuality);

		assertThat(testQuality.getNumTests(file), is(5));
		assertThat(testQuality.getNumTests(), is(5));

		assertThat(testQuality.getSum(file, 0), is(10));
		assertThat(testQuality.getSum(file, 1), is(10));
		assertThat(testQuality.getSum(file, 2), is(4));
		assertThat(testQuality.getSum(file, 3), is(4));
		assertThat(testQuality.getSum(file, 4), is(4));
		assertThat(testQuality.getMin(file, 0), is(2));
		assertThat(testQuality.getMin(file, 1), is(2));
		assertThat(testQuality.getMin(file, 2), is(0));
		assertThat(testQuality.getMin(file, 3), is(0));
		assertThat(testQuality.getMin(file, 4), is(0));
		assertThat(testQuality.getAverage(file, 0), is((double) 10 / 4));
		assertThat(testQuality.getAverage(file, 1), is((double) 10 / 4));
		assertThat(testQuality.getAverage(file, 2), is((double) 4 / 4));
		assertThat(testQuality.getAverage(file, 3), is((double) 4 / 4));
		assertThat(testQuality.getAverage(file, 4), is((double) 4 / 4));
		assertThat(testQuality.getUniquenessJaan(), is(16.25));
		assertThat(testQuality.getUniquenessMaksim(), is(1.4));
		assertThat(testQuality.getSum(), is(32));
		assertThat(testQuality.getSumNormRaimund(), is(1.6));
	}

	@Test
	public void multipleFilesAndCorrectDeviation() {

		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(8);
		IGHitCountLogger logger;

		/*
		* ************************
		*  1
		*    1 1 1
		*    1
		*    1
		* *************************
		* */
		SourceFile file = new SourceFile("Test4.txt");
		file.setLocalPath("some path");
		file.setNumLines(4);

		/*
		* ************************
		*      1
		*    1 1 1
		*    1
		*
		*    1   1
		*  1   1
		* *************************
		* */
		SourceFile file2 = new SourceFile("Test44.txt");
		file2.setLocalPath("some path44");
		file2.setNumLines(6);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file2, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file2, 1, 0), 10);
		logger.logHit(new SourceLocation(file2, 2, 0), 10);
		logger.logHit(new SourceLocation(file2, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file2, 0, 0), 10);
		logger.logHit(new SourceLocation(file2, 1, 0), 10);
		logger.logHit(new SourceLocation(file2, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file2, 1, 0), 10);
		logger.logHit(new SourceLocation(file2, 4, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 4, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file2, 5, 0), 10);

		testQuality = DiagnosticTestQuality.createFrom(loggers, allAssignmentsLogger);

		assertNotNull(testQuality);

		assertThat(testQuality.getNumTests(), is(4));
		assertThat(testQuality.getNumTests(file), is(4));
		assertThat(testQuality.getNumTests(file2), is(4));

		assertThat(testQuality.getSum(file, 0), is(8));
		assertThat(testQuality.getSum(file, 1), is(8));
		assertThat(testQuality.getSum(file, 2), is(4));
		assertThat(testQuality.getSum(file, 3), is(4));
		assertThat(testQuality.getMin(file, 0), is(2));
		assertThat(testQuality.getMin(file, 1), is(2));
		assertThat(testQuality.getMin(file, 2), is(0));
		assertThat(testQuality.getMin(file, 3), is(0));
		assertThat(testQuality.getAverage(file, 0), is((double) 8 / 3));
		assertThat(testQuality.getAverage(file, 1), is((double) 8 / 3));
		assertThat(testQuality.getAverage(file, 2), is((double) 4 / 3));
		assertThat(testQuality.getAverage(file, 3), is((double) 4 / 3));
		assertThat(testQuality.getSum(), is(58));
		assertThat(testQuality.getSumNormRaimund(), is((double) 29 / 6));
		assertThat(testQuality.getDeviation(), is(1.7716909687891083));
		assertThat(testQuality.getCoverage(), is(90.00));
	}

	@Test
	public void whatifIdeal() {

		/*
		* ************************
		*  1
		*    1
		*      1
		*        1
		*          1
		*            1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(6);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile(new File("TestWhatif.txt"), "TestWhatif.txt");
		file.setLocalPath("some path");
		file.setNumLines(6);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test5");
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test6");
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 4, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 5, 0), 10);

		Report report = Report.createReport(loggers, allAssignmentsLogger.getNumItems(), "Assignments");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		report.print(new PrintStream(out));

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 1.00  L = 1.00  H = 1.00\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>      x                                 <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n" +
				"    1:  0 <|>            x                           <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n" +
				"    2:  0 <|>                  x                     <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n" +
				"    3:  0 <|>                        x               <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n" +
				"    4:  0 <|>                              x         <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n" +
				"    5:  0 <|>                                    x   <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1\n";

		assertThat(out.toString(), equalTo(correct));
	}
	@Test
	public void whatifMedium() {

		/*
		* ************************
		*  1 1
		*    1 1     1
		*    1 1 1
		*        1
		*  1 1 1   1 1
		*  1       1 1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(6);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile(new File("TestWhatif.txt"), "TestWhatif.txt");
		file.setLocalPath("some path");
		file.setNumLines(6);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test5");
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test6");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 4, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 5, 0), 10);

		Report report = Report.createReport(loggers, allAssignmentsLogger.getNumItems(), "Assignments");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		report.print(new PrintStream(out));

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 2.67  L = 1.00  H = 4.33\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>      x     x                           <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 5\n" +
				"    1:  0 <|>            x     x                 x   <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 3  L) 1  H) 5\n" +
				"    2:  0 <|>            x     x     x               <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 3  L) 1  H) 5\n" +
				"    3:  0 <|>                        x               <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 2\n" +
				"    4:  0 <|>      x     x     x           x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 4  L) 1  H) 5\n" +
				"    5:  0 <|>      x                       x     x   <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 4\n";

		assertThat(out.toString(), equalTo(correct));
	}
	@Test
	public void whatifWorst() {

		/*
		* ************************
		*    1 1 1 1 1
		*  1   1 1 1 1
		*  1 1   1 1 1
		*  1 1 1   1 1
		*  1 1 1 1   1
		*  1 1 1 1 1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(6);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile(new File("TestWhatif.txt"), "TestWhatif.txt");
		file.setLocalPath("some path");
		file.setNumLines(6);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test5");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test6");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 2, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 4, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 5, 0), 10);

		Report report = Report.createReport(loggers, allAssignmentsLogger.getNumItems(), "Assignments");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		report.print(new PrintStream(out));

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 1.00  L = 1.00  H = 6.00\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>            x     x     x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n" +
				"    1:  0 <|>      x           x     x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n" +
				"    2:  0 <|>      x     x           x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n" +
				"    3:  0 <|>      x     x     x           x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n" +
				"    4:  0 <|>      x     x     x     x           x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n" +
				"    5:  0 <|>      x     x     x     x     x         <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6\n";

		assertThat(out.toString(), equalTo(correct));
	}
	@Test
	public void whatifIdenticalCandidates() {

		/*
		* ************************
		*  1 1 1 1 1 1
		*  1 1 1 1 1 1
		*
		*    1     1
		*      1 1 1
		*  1 1
		* *************************
		* */
		List<IGHitCountLogger> loggers = new ArrayList<IGHitCountLogger>(6);
		IGHitCountLogger logger;
		SourceFile file = new SourceFile(new File("TestWhatif.txt"), "TestWhatif.txt");
		file.setLocalPath("some path");
		file.setNumLines(6);

		logger = new IGHitCountLogger("Test1");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test2");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 5, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test3");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test4");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test5");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		logger.logHit(new SourceLocation(file, 3, 0), 10);
		logger.logHit(new SourceLocation(file, 4, 0), 10);
		loggers.add(logger);
		logger = new IGHitCountLogger("Test6");
		logger.logHit(new SourceLocation(file, 0, 0), 10);
		logger.logHit(new SourceLocation(file, 1, 0), 10);
		loggers.add(logger);

		IGHitCountLogger allAssignmentsLogger = new IGHitCountLogger("All Assignments");
		allAssignmentsLogger.logHit(new SourceLocation(file, 0, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 1, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 2, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 3, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 4, 0), 10);
		allAssignmentsLogger.logHit(new SourceLocation(file, 5, 0), 10);

		Report report = Report.createReport(loggers, allAssignmentsLogger.getNumItems(), "Assignments");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		report.print(new PrintStream(out));

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 2.50  L = 1.17  H = 3.83\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>      x     x     x     x     x     x   <0|6>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 5  L) 2  H) 5\n" +
				"    1:  0 <|>      x     x     x     x     x     x   <0|6>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 5  L) 2  H) 5\n" +
				"    3:  0 <|>            x                 x         <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 5\n" +
				"    4:  0 <|>                  x     x     x         <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 4\n" +
				"    5:  0 <|>      x     x                           <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 4\n";

		assertThat(out.toString(), equalTo(correct));
	}

}
