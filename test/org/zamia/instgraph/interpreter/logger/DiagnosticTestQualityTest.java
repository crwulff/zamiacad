package org.zamia.instgraph.interpreter.logger;

import org.junit.After;
import org.junit.Test;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;

import java.util.ArrayList;
import java.util.List;

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
}
