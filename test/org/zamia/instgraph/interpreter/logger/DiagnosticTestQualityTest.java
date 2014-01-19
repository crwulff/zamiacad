package org.zamia.instgraph.interpreter.logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;

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
		new AlgoritmicChecker("some path", 6, 6) {
			
			boolean isCovered(int test, int statment) {
				return test == statment;
			}
			
			@Override
			void checkOut(String out) {
				String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 1.00  L = 1.00  H = 1.00  We = 0.00  We2 = 0.00\n" +
						"\n" +
						"(some path) TestWhatif.txt:\n" +
						"         <F|P> Test0 Test1 Test2 Test3 Test4 Test5\n" +
						"    0:  0 <|>      x                                 <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n" +
						"    1:  0 <|>            x                           <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n" +
						"    2:  0 <|>                  x                     <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n" +
						"    3:  0 <|>                        x               <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n" +
						"    4:  0 <|>                              x         <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n" +
						"    5:  0 <|>                                    x   <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 1  Ham) 10\n";

				System.out.println("out = " + out);
				System.out.println("expected = " + correct);
				assertThat(out, equalTo(correct));
				
			}
		};

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

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 2.67  L = 1.00  H = 4.33  We = 0.56  We2 = 0.75\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>      x     x                           <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 5  Ham) 15\n" +
				"    1:  0 <|>            x     x                 x   <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 3  L) 1  H) 5  Ham) 15\n" +
				"    2:  0 <|>            x     x     x               <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 3  L) 1  H) 5  Ham) 17\n" +
				"    3:  0 <|>                        x               <0|1>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 2  Ham) 19\n" +
				"    4:  0 <|>      x     x     x           x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 4  L) 1  H) 5  Ham) 17\n" +
				"    5:  0 <|>      x                       x     x   <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 4  Ham) 19\n";

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

		new AlgoritmicChecker("some path", 6, 6) {
			
			boolean isCovered(int test, int statment) {
				return test != statment;
			}
			
			@Override
			void checkOut(String out) {
				String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 1.00  L = 1.00  H = 6.00  We = 0.00  We2 = 0.00\n" +
						"\n" +
						"(some path) TestWhatif.txt:\n" +
						"         <F|P> Test0 Test1 Test2 Test3 Test4 Test5\n" +
						"    0:  0 <|>            x     x     x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n" +
						"    1:  0 <|>      x           x     x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n" +
						"    2:  0 <|>      x     x           x     x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n" +
						"    3:  0 <|>      x     x     x           x     x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n" +
						"    4:  0 <|>      x     x     x     x           x   <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n" +
						"    5:  0 <|>      x     x     x     x     x         <0|5>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 6  Ham) 10\n";

				System.out.println("out = " + out);
				System.out.println("expected = " + correct);
				assertThat(out, equalTo(correct));
				
			}
		};

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

		String correct = "\n###########  Assignments  ###########    TotalStmts = 6   W = 3.00  L = 1.40  H = 4.60  We = 2.80  We2 = 1.67\n" +
				"\n" +
				"(some path) TestWhatif.txt:\n" +
				"         <F|P> Test1 Test2 Test3 Test4 Test5 Test6\n" +
				"    0:  0 <|>      x     x     x     x     x     x   <0|6>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 5  L) 2  H) 5  Ham) 11\n" +
				"    1:  0 <|>      x     x     x     x     x     x   <0|6>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 5  L) 2  H) 5  Ham) 11\n" +
				"    3:  0 <|>            x                 x         <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 5  Ham) 13\n" +
				"    4:  0 <|>                  x     x     x         <0|3>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 1  L) 1  H) 4  Ham) 14\n" +
				"    5:  0 <|>      x     x                           <0|2>   1) 0.000  2) 0.000  3) NaN  4) NaN  5) NaN  W) 2  L) 1  H) 4  Ham) 15\n";

		assertThat(out.toString(), equalTo(correct));
	}

	/**
	 * User must implement isCovered(test, statement) to build the matrix using
	 * this class. He can optionally implement checkOut() to test the result.
	 * */
	static abstract class AlgoritmicChecker {

		static String format(double a) {
			//return String.format("%.3f", a).replaceAll(",", ".").replace(".000", "");
			return a + "";
		}

		IGHitCountLogger logger;
		List<IGHitCountLogger> loggers;
		IGHitCountLogger allAssignmentsLogger;
		String title;
		
		AlgoritmicChecker(String title, int tests, int statements) {
			this.title = title;
			loggers = new ArrayList<IGHitCountLogger>(tests);
			SourceFile file = new SourceFile(new File("TestWhatif.txt"), "TestWhatif.txt");
			file.setLocalPath(title);
			file.setNumLines(tests);

			// build the diagnostic table
			for (int test = 0; test != tests; test++) {
				logger = new IGHitCountLogger("Test" + test);
				for (int statement = 0; statement != statements; statement++)
					if (isCovered(test, statement))
						logger.logHit(new SourceLocation(file, statement, 0), 10);

				if (logger.getNumItems() == 0) // DiagnosticTestQuality will
												// fail if empty logger is
												// supplied
					logger.getOrCreateLogger(file);
				loggers.add(logger);
			}

			allAssignmentsLogger = new IGHitCountLogger("All Assignments");
			for (int s = 0; s != statements; s++)
				allAssignmentsLogger.logHit(new SourceLocation(file, s, 0), 10);

			Report report = Report.createReport(loggers,
					allAssignmentsLogger.getNumItems(), "Assignments");

			// check result
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			report.print(new PrintStream(out));

			checkOut(out.toString());
		}

		/**Implement this method. Return 'yes' iff given test excitates given statement*/
		abstract boolean isCovered(int test, int statement);

		abstract void checkOut(String out);// {
		// assertThat(out.toString(), equalTo(correct));
		// }
	}

	abstract static class AlgorithmicPrinter extends AlgoritmicChecker {

		AlgorithmicPrinter(String title, int tests, int statements) {
			super(title, tests, statements);
		}

		@Override
		void checkOut(String out) {
			
			DiagnosticTestQuality testQuality = DiagnosticTestQuality
					.createFrom(loggers, allAssignmentsLogger);

			// Refine the response a bit adding the test quality and trimming empty tails
			String response = out
					.toString()
					.replaceFirst("\n", "")
					.replaceFirst( // Inject the TQ as the second line of the report 
							"\n",
							"\nHamming="
									+ format(testQuality.getSumNormRaimund())
									+ ", UniqMaxim="
									+ format(testQuality.getUniquenessMaksim())
									+ ", ValMarko="
									+ format(testQuality.ValMarko)
									+ ", Kostin="
									+ testQuality.kostinResolution + ", WHD="
									+ testQuality.WHD_avg);

			// Remove the scores at the tail of the statement lines. Here, every line
			// corresponds to a statement. It starts with coverage bitmap and 
			// ends with scores. We remove the tail so that instead of
			// 0: 0 <|> x x x x x <0|5> 1) 0.000 2) 0.000 3) NaN 4) NaN 5) NaN W) 11 L) 11 H) 11 Ham) 0
			//  we'll have simply
			// 0: 0 <|> x x x x x

			String EOL = "\n";
			String[] split = response.split(EOL);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < split.length; i++) {

				int stop = split[i].lastIndexOf(">") + 1;
				if (split[i].contains("<F|P>")) // a header line -- keep intact
					stop = 0;
				sb.append(stop == 0 ? split[i] : split[i].substring(0, stop))
						.append("\n");

			}
			response = sb.toString();
			System.out.println(response);
			
			boolean tested = false;
			// Check the response against the reference file
			URL path = getClass().getResource("DiagnosticTestQualityTest.txt");
			try (BufferedReader br = new BufferedReader(new FileReader(new File(path.toURI())))) {
	            String line;
	            while ( (line = br.readLine()) != null) {
	            	String line1  = line; assertThat(line1, startsWith ("###########  Assignments  ###########"));
	            	String line2 = br.readLine(); assertThat(line2, startsWith("Hamming="));
	            	String titleLine = br.readLine();
	            	if (titleLine.equals("(" + title + ") TestWhatif.txt:")) { // title matches - this is our section
	            		if (tested) throw new RuntimeException("Double golden reference for '" + title + "'");
	            		split = response.split(EOL);
	            		assertThat(split[0], is(line1));
	            		assertThat(split[1], is(line2));
	            		assertThat(split[2], is(titleLine));
	            		for (int i = 3 ; i != split.length ; i++) {
	            			line = br.readLine();
	            			assertThat(split[i], is(line));
	            		}
	            		assertThat(br.readLine().length(), is(0)); // there must be an empty line that separates the sections
	            		tested = true;
	            	} 
	            	else while (br.readLine().length() != 0); // skip the section
	            	
	            }
	            
			} 
			catch (Exception e) { throw new AssertionError("Failed to check '" + title + "'", e);}
				//Files.readAllBytes(Paths.get(path.toURI()));
			
			if (!tested) throw new RuntimeException("There is no golden reference for test set '"+title+"'");
		}
	}
	
	/** Allows to enter the matrix as text. */
	static void AsciiPrinter(String title, final String... ascii) {
		new AlgorithmicPrinter(title, ascii[0].length(), ascii.length) {

			boolean isCovered(int test, int statement) {
				return ascii[statement].charAt(test) == '1';
			}

		};

	}

	// abstract class AsciiChecker extends AlgoritmicChecker {
	//
	// AsciiChecker(String title, final String ... ascii) {
	// super(title, ascii[0].length(), ascii.length);
	// };
	//
	// boolean isCovered(int test, int statement) {
	// return ascii[statement].charAt(test) == '1';
	// }
	// }

	// valtih: I see that Kostin is the best but once it saturates, it stops
	// contributing
	public static void main(String[] args) {
	}
		
	@Test
	public void AlgorithmicMatrixTest() {

		new AlgorithmicPrinter("gray-ones", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test == 10) || (statement > test);
			}

		};

		new AlgorithmicPrinter("gray-zeroes", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test != 10) && (statement > test);
			}

		};

		new AlgorithmicPrinter("green - one", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return (statement < 6) ? true : false;
				return (statement > test);
			}

		};

		new AlgorithmicPrinter("green - zero", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return (statement < 5) ? true : false;
				return (statement > test);
			}

		};

		new AlgorithmicPrinter("red - one", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return (statement < 5) ? false : true;
				return (statement > test);
			}

		};

		new AlgorithmicPrinter("red - zero", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return (statement < 6) ? false : true;
				return (statement > test);
			}

		};

		new AlgorithmicPrinter("blue - zeros", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test != 10) && (statement == test);
			}

		};

		new AlgorithmicPrinter("blue - ones", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test == 10) || (statement == test);
			}

		};

		new AlgorithmicPrinter("orange - 01", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return statement == 1;
				return statement != test;
			}

		};

		new AlgorithmicPrinter("orange - 011111", 11, 11) {

			boolean isCovered(int test, int statement) {
				if (test == 10)
					return statement > 0 && statement < 6;
				return statement != test;
			}

		};

		new AlgorithmicPrinter("orange - all ones", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test == 10) || (statement != test);
			}

		};

		new AlgorithmicPrinter("useless test 1", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test == 0);
			}

		};

		new AlgorithmicPrinter("useless test 2", 11, 11) {

			boolean isCovered(int test, int statement) {
				return (test < 5);
			}

		};

		new AlgorithmicPrinter("all ones", 11, 11) {
			boolean isCovered(int test, int statement) {
				return true;
			}
		};
		
		
		
		/*
		 * 1 1 1
		 * 1 1 0
		 * 1 0 1
		 * 1 0 0  
		 * 0 1 1
		 * 0 1 0
		 * 0 0 1
		 * 0 0 0
		 * */				
		AsciiPrinter("best", "111", "110", "101", "100", "011", "010", "001", "000");
		
		AsciiPrinter("half the best", "11", "11", "10", "10", "01", "01", "00", "00");
		
		//AsciiMatrix("00000000", "00", "00", "00", "00", "00", "00", "00", "00");
		AsciiPrinter("00000001", "00", "00", "00", "00", "00", "00", "00", "01");
		AsciiPrinter("00000011", "00", "00", "00", "00", "00", "00", "01", "01");
		AsciiPrinter("00000111", "00", "00", "00", "00", "00", "01", "01", "01");
		AsciiPrinter("00001111", "00", "00", "00", "00", "01", "01", "01", "01");
		AsciiPrinter("00011111", "00", "00", "00", "01", "01", "01", "01", "01");
		AsciiPrinter("00111111", "00", "00", "01", "01", "01", "01", "01", "01");
		AsciiPrinter("01111111", "00", "01", "01", "01", "01", "01", "01", "01");
		AsciiPrinter("11111111", "01", "01", "01", "01", "01", "01", "01", "01");
		AsciiPrinter("01010101", "01", "10", "01", "10", "01", "10", "01", "10");
		AsciiPrinter("00110011", "00", "10", "01", "11", "00", "10", "01", "11");

	}

}
