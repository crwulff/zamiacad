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
public class HammingDistanceTest {

	HammingDistance hammingDistance;

	@After
	public void printHamming() {
		System.out.println(hammingDistance);
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

		hammingDistance = HammingDistance.createFrom(loggers, 4);

		assertNotNull(hammingDistance);

		assertThat(hammingDistance.getNumTests(file), is(3));
		assertThat(hammingDistance.getNumTests(), is(3));

		assertThat(hammingDistance.getSum(file, 0), is(6));
		assertThat(hammingDistance.getSum(file, 1), is(6));
		assertThat(hammingDistance.getSum(file, 2), is(4));
		assertThat(hammingDistance.getMin(file, 0), is(2));
		assertThat(hammingDistance.getMin(file, 1), is(2));
		assertThat(hammingDistance.getMin(file, 2), is(2));
		assertThat(hammingDistance.getAverage(file, 0), is((double) 6 / 2));
		assertThat(hammingDistance.getAverage(file, 1), is((double) 6 / 2));
		assertThat(hammingDistance.getAverage(file, 2), is((double) 4 / 2));
		assertThat(hammingDistance.getUniquenessJaan(), is(10.5));
		assertThat(hammingDistance.getUniquenessMaksim(), is((double) 1 + ((double) 2 / 3)));
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

		hammingDistance = HammingDistance.createFrom(loggers, 4);

		assertNotNull(hammingDistance);

		assertThat(hammingDistance.getNumTests(file), is(4));

		assertThat(hammingDistance.getSum(file, 0), is(8));
		assertThat(hammingDistance.getSum(file, 1), is(8));
		assertThat(hammingDistance.getSum(file, 2), is(4));
		assertThat(hammingDistance.getSum(file, 3), is(4));
		assertThat(hammingDistance.getMin(file, 0), is(2));
		assertThat(hammingDistance.getMin(file, 1), is(2));
		assertThat(hammingDistance.getMin(file, 2), is(0));
		assertThat(hammingDistance.getMin(file, 3), is(0));
		assertThat(hammingDistance.getAverage(file, 0), is((double) 8 / 3));
		assertThat(hammingDistance.getAverage(file, 1), is((double) 8 / 3));
		assertThat(hammingDistance.getAverage(file, 2), is((double) 4 / 3));
		assertThat(hammingDistance.getAverage(file, 3), is((double) 4 / 3));
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

		hammingDistance = HammingDistance.createFrom(loggers, 4);

		assertNotNull(hammingDistance);

		assertThat(hammingDistance.getNumTests(file), is(5));
		assertThat(hammingDistance.getNumTests(), is(5));

		assertThat(hammingDistance.getSum(file, 0), is(10));
		assertThat(hammingDistance.getSum(file, 1), is(10));
		assertThat(hammingDistance.getSum(file, 2), is(4));
		assertThat(hammingDistance.getSum(file, 3), is(4));
		assertThat(hammingDistance.getSum(file, 4), is(4));
		assertThat(hammingDistance.getMin(file, 0), is(2));
		assertThat(hammingDistance.getMin(file, 1), is(2));
		assertThat(hammingDistance.getMin(file, 2), is(0));
		assertThat(hammingDistance.getMin(file, 3), is(0));
		assertThat(hammingDistance.getMin(file, 4), is(0));
		assertThat(hammingDistance.getAverage(file, 0), is((double) 10 / 4));
		assertThat(hammingDistance.getAverage(file, 1), is((double) 10 / 4));
		assertThat(hammingDistance.getAverage(file, 2), is((double) 4 / 4));
		assertThat(hammingDistance.getAverage(file, 3), is((double) 4 / 4));
		assertThat(hammingDistance.getAverage(file, 4), is((double) 4 / 4));
		assertThat(hammingDistance.getUniquenessJaan(), is(16.25));
		assertThat(hammingDistance.getUniquenessMaksim(), is(1.4));
	}
}
