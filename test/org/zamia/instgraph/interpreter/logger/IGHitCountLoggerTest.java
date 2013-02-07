package org.zamia.instgraph.interpreter.logger;

import java.io.File;

import org.junit.Test;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class IGHitCountLoggerTest {


	@Test
	public void testSubtract() throws Exception {

		IGHitCountLogger logger = new IGHitCountLogger("");

		SourceFile sf1 = new SourceFile(new File("asdf1"));
		SourceLocation sl1 = new SourceLocation(sf1, 10, 11);
		SourceFile sf2 = new SourceFile(new File("asdf2"));
		SourceLocation sl2 = new SourceLocation(sf2, 1, 2);
		SourceFile sf3 = new SourceFile(new File("asdf3"));
		SourceLocation sl3 = new SourceLocation(sf3, 13, 12);
		SourceFile sf4 = new SourceFile(new File("asdf4"));
		SourceLocation sl4 = new SourceLocation(sf4, 132, 13);
		SourceFile sf5 = new SourceFile(new File("asdf5"));
		SourceLocation sl5 = new SourceLocation(sf5, 198, 14);
		logger.logHit(sl1, 123);
		logger.logHit(sl2, 122);
		logger.logHit(sl3, 121);
		logger.logHit(sl4, 120);
		logger.logHit(sl5, 119);

		IGHitCountLogger subtr = new IGHitCountLogger("");

		subtr.logHit(sl4, 11);
		subtr.logHit(sl5, 11);


		IGHitCountLogger result = (IGHitCountLogger) logger.subtractAll(subtr);

		assertNotNull(result);

		assertTrue(result.hasLoggerForFile(sf1));
		assertTrue(result.hasLoggerForFile(sf2));
		assertTrue(result.hasLoggerForFile(sf3));
		assertTrue(!result.hasLoggerForFile(sf4));
		assertTrue(!result.hasLoggerForFile(sf5));

		assertTrue(((IGHitCountLogger) result.getLogger(sf1)).hasHitsAt(10));
		assertTrue(((IGHitCountLogger) result.getLogger(sf2)).hasHitsAt(1));
		assertTrue(((IGHitCountLogger) result.getLogger(sf3)).hasHitsAt(13));

		assertThat(((IGHitCountLogger) result.getLogger(sf1)).getHitsAt(10), is(123));
		assertThat(((IGHitCountLogger) result.getLogger(sf2)).getHitsAt(1), is(122));
		assertThat(((IGHitCountLogger) result.getLogger(sf3)).getHitsAt(13), is(121));

	}

	@Test
	public void testMerge() throws Exception {

		IGHitCountLogger logger = new IGHitCountLogger("");

		SourceFile sf1 = new SourceFile(new File("asdf1"));
		SourceLocation sl1 = new SourceLocation(sf1, 10, 11);
		SourceFile sf2 = new SourceFile(new File("asdf2"));
		SourceLocation sl2 = new SourceLocation(sf2, 1, 2);
		SourceFile sf3 = new SourceFile(new File("asdf3"));
		SourceLocation sl3 = new SourceLocation(sf3, 13, 12);
		SourceFile sf4 = new SourceFile(new File("asdf4"));
		SourceLocation sl4 = new SourceLocation(sf4, 132, 13);
		SourceFile sf5 = new SourceFile(new File("asdf5"));
		SourceLocation sl5 = new SourceLocation(sf5, 198, 14);
		logger.logHit(sl1, 123);
		logger.logHit(sl2, 122);
		logger.logHit(sl3, 121);
//		logger.logHit(sl4, 120);
//		logger.logHit(sl5, 119);

		IGHitCountLogger added = new IGHitCountLogger("");

		added.logHit(sl4, 11);
		added.logHit(sl5, 12);


		IGHitCountLogger result = (IGHitCountLogger) IGHitCountLogger.mergeAll(logger, added);

		assertNotNull(result);

		assertTrue(result.hasLoggerForFile(sf1));
		assertTrue(result.hasLoggerForFile(sf2));
		assertTrue(result.hasLoggerForFile(sf3));
		assertTrue(result.hasLoggerForFile(sf4));
		assertTrue(result.hasLoggerForFile(sf5));

		assertTrue(((IGHitCountLogger) result.getLogger(sf1)).hasHitsAt(10));
		assertTrue(((IGHitCountLogger) result.getLogger(sf2)).hasHitsAt(1));
		assertTrue(((IGHitCountLogger) result.getLogger(sf3)).hasHitsAt(13));
		assertTrue(((IGHitCountLogger) result.getLogger(sf4)).hasHitsAt(132));
		assertTrue(((IGHitCountLogger) result.getLogger(sf5)).hasHitsAt(198));

		assertThat(((IGHitCountLogger) result.getLogger(sf1)).getHitsAt(10), is(123));
		assertThat(((IGHitCountLogger) result.getLogger(sf2)).getHitsAt(1), is(122));
		assertThat(((IGHitCountLogger) result.getLogger(sf3)).getHitsAt(13), is(121));
		assertThat(((IGHitCountLogger) result.getLogger(sf4)).getHitsAt(132), is(11));
		assertThat(((IGHitCountLogger) result.getLogger(sf5)).getHitsAt(198), is(12));

	}

}
