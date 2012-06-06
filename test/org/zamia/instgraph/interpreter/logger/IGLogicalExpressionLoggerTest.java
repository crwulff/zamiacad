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
public class IGLogicalExpressionLoggerTest {


	@Test
	public void testSubtract() throws Exception {

		IGLogicalExpressionLogger logger = new IGLogicalExpressionLogger("");

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
		logger.logExpr(sl1, true, true);
		logger.logExpr(sl2, true, true);
		logger.logExpr(sl3, true, true);
		logger.logExpr(sl4, true, true);
		logger.logExpr(sl5, true, true);

		IGLogicalExpressionLogger subtr = new IGLogicalExpressionLogger("");

		subtr.logExpr(sl2, false, false);
		subtr.logExpr(sl3, false, true);
		subtr.logExpr(sl4, true, true);
		subtr.logExpr(sl5, true, false);


		IGLogicalExpressionLogger result = (IGLogicalExpressionLogger) logger.subtractAll(subtr);

		assertNotNull(result);

		assertTrue(result.hasLoggerForFile(sf1));
		assertTrue(result.hasLoggerForFile(sf2));
		assertTrue(result.hasLoggerForFile(sf3));
		assertTrue(result.hasLoggerForFile(sf4));
		assertTrue(result.hasLoggerForFile(sf5));

		IGLogicalExpressionLogger.Expression expr1 = new IGLogicalExpressionLogger.Expression(sl1, true, true);
		IGLogicalExpressionLogger.Expression expr2 = new IGLogicalExpressionLogger.Expression(sl2, false, false);
		IGLogicalExpressionLogger.Expression expr3 = new IGLogicalExpressionLogger.Expression(sl3, true, true);
		IGLogicalExpressionLogger.Expression expr4 = new IGLogicalExpressionLogger.Expression(sl4, false, false);
		IGLogicalExpressionLogger.Expression expr5 = new IGLogicalExpressionLogger.Expression(sl5, false, true);
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf1)).fItems.contains(expr1));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf2)).fItems.contains(expr2));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf3)).fItems.contains(expr3));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf4)).fItems.contains(expr4));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf5)).fItems.contains(expr5));
		IGLogicalExpressionLogger.Expression e2 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf2)).getItem(expr2);
		assertThat(e2.fHasTrueOccurred, is(true));
		assertThat(e2.fHasFalseOccurred, is(true));
		IGLogicalExpressionLogger.Expression e3 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf3)).getItem(expr3);
		assertThat(e3.fHasTrueOccurred, is(true));
		assertThat(e3.fHasFalseOccurred, is(false));
		IGLogicalExpressionLogger.Expression e4 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf4)).getItem(expr4);
		assertThat(e4.fHasTrueOccurred, is(false));
		assertThat(e4.fHasFalseOccurred, is(false));
		IGLogicalExpressionLogger.Expression e5 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf5)).getItem(expr5);
		assertThat(e5.fHasTrueOccurred, is(false));
		assertThat(e5.fHasFalseOccurred, is(true));
	}

	@Test
	public void testMerge() throws Exception {

		IGLogicalExpressionLogger logger = new IGLogicalExpressionLogger("");

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
		logger.logExpr(sl1, true, true);
		logger.logExpr(sl2, false, true);
		logger.logExpr(sl3, true, false);
//		logger.logExpr(sl4, true, true);
//		logger.logExpr(sl5, true, true);

		IGLogicalExpressionLogger added = new IGLogicalExpressionLogger("");
		added.logExpr(sl2, false, false);
		added.logExpr(sl3, false, true);
		added.logExpr(sl4, true, true);
		added.logExpr(sl5, true, false);


		IGLogicalExpressionLogger result = (IGLogicalExpressionLogger) IGLogicalExpressionLogger.mergeAll(logger, added);

		assertNotNull(result);

		assertTrue(result.hasLoggerForFile(sf1));
		assertTrue(result.hasLoggerForFile(sf2));
		assertTrue(result.hasLoggerForFile(sf3));
		assertTrue(result.hasLoggerForFile(sf4));
		assertTrue(result.hasLoggerForFile(sf5));

		IGLogicalExpressionLogger.Expression expr1 = new IGLogicalExpressionLogger.Expression(sl1, true, true);
		IGLogicalExpressionLogger.Expression expr2 = new IGLogicalExpressionLogger.Expression(sl2, false, true);
		IGLogicalExpressionLogger.Expression expr3 = new IGLogicalExpressionLogger.Expression(sl3, true, true);
		IGLogicalExpressionLogger.Expression expr4 = new IGLogicalExpressionLogger.Expression(sl4, true, true);
		IGLogicalExpressionLogger.Expression expr5 = new IGLogicalExpressionLogger.Expression(sl5, true, false);
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf1)).fItems.contains(expr1));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf2)).fItems.contains(expr2));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf3)).fItems.contains(expr3));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf4)).fItems.contains(expr4));
		assertTrue(((IGLogicalExpressionLogger) result.getLogger(sf5)).fItems.contains(expr5));
		IGLogicalExpressionLogger.Expression e1 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf1)).getItem(expr1);
		assertThat(e1.fHasTrueOccurred, is(true));
		assertThat(e1.fHasFalseOccurred, is(true));
		IGLogicalExpressionLogger.Expression e2 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf2)).getItem(expr2);
		assertThat(e2.fHasTrueOccurred, is(false));
		assertThat(e2.fHasFalseOccurred, is(true));
		IGLogicalExpressionLogger.Expression e3 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf3)).getItem(expr3);
		assertThat(e3.fHasTrueOccurred, is(true));
		assertThat(e3.fHasFalseOccurred, is(true));
		IGLogicalExpressionLogger.Expression e4 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf4)).getItem(expr4);
		assertThat(e4.fHasTrueOccurred, is(true));
		assertThat(e4.fHasFalseOccurred, is(true));
		IGLogicalExpressionLogger.Expression e5 = (IGLogicalExpressionLogger.Expression) (result.getLogger(sf5)).getItem(expr5);
		assertThat(e5.fHasTrueOccurred, is(true));
		assertThat(e5.fHasFalseOccurred, is(false));
	}
}
