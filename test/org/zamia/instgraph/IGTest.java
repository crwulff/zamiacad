/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph;

import org.junit.Ignore;
import org.junit.Test;
import org.zamia.BasicTest;
import org.zamia.BuildPath;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGStmt;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * @author Guenter Bartsch
 */


public class IGTest extends BasicTest {

	private IGInterpreterCode fCode;


	// FIXME: disabled for now (PSL support is incomplete)
	//	public void testPSL6() throws Exception {
	//
	//		if (!enablePSL6Test) {
	//			fail("Test disabled");
	//			return;
	//		}
	//
	//		runTest("test/semantic/psl6Test", 1);
	//	}

	@Test
	public void testLoop() throws Exception {
		runTest("examples/semantic/loops", 1);
	}
	
	@Test
	public void testInstGenerics() throws Exception {

		runTest("examples/semantic/instGenericTest", "BuildPath.txt", 9, new ErrorChecker() {
			public void handle() {
				int nErr = fZPrj.getERM().getNumErrors();
				assertEquals("Got wrong number of errors", 1, nErr);
				assertContains(0, "PLANNED ERROR: INTENTIONALLY WRONG EXPECTED,2, IS SPECIFIED INSTEAD OF CORRECT 1: 1 MUST EQUAL 2");
			}
		}); 

				
	}

	@Test
	public void testAVSAES() throws Exception {

		runTest("examples/avs_aes", 125);
	}

	@Test
	public void testSubProgram7() throws Exception {

		runTest("examples/semantic/subProgramTest7", 1);
	}

	@Test
	public void testSubProgram8() throws Exception {

		runTest("examples/semantic/subProgramTest8", 46);
	}
	
	@Test
	public void testExpr() throws Exception {

		runTest("examples/semantic/exprTest", 1);
	}

	@Test
	public void testAlias() throws Exception {

		runTest("examples/semantic/aliasTest", 1);
	}

	@Test
	public void testSubProgram6() throws Exception {

		runTest("examples/semantic/subProgramTest6", 1);
	}

	@Test
	public void testArray() throws Exception {

		runTest("examples/semantic/arrayTest", 1);
	}

	@Test
	public void testArray2() throws Exception {

		runTest("examples/semantic/array2Test", 1);
	}

	@Test
	public void testSubProgram2() throws Exception {

		runTest("examples/semantic/subProgramTest2", 1);
	}

	@Test
	public void testResolver() throws Exception {

		runTest("examples/semantic/resolverTest", 1);
	}

	@Test
	public void testGenerate2() throws Exception {

		runTest("examples/semantic/generate2Test", 69);
	}

	@Test
	public void testVarDeclaration() throws Exception {

		runTest("examples/semantic/varDeclarationTest", 1);
	}

	@Test
	public void testPSL5() throws Exception {

		runTest("examples/semantic/psl5Test", 1);
	}

	@Test
	public void testInstPort() throws Exception {

		runTest("examples/semantic/inst(port)Test", "BuildPath.txt", 19, new ErrorChecker() {
			
			public void handle() {
				int nErr = fZPrj.getERM().getNumErrors();
				assertEquals("Got wrong number of errors", 15, nErr);
				
				assertContains(0, "Design unit expected here.");
				assertContains(1, "Couldn't resolve TR2");
				assertContains(2, " Couldn't resolve PORTTR1");
				assertContains(3, "Too many positional parameters.");
				assertContains(4, "Illegal mix of named and positional parameters");
				assertContains(5, " Type mismatch in expression.");
				assertContains(6, " Failed to compute actual item in named mapping: 1, formal 1/1 was: PORTA");
				assertContains(7, "Direction mismatch in positional mapping formal PORTB of mode OUT to actual PORTA of mode IN");
				assertContains(8, "Direction mismatch in named mapping formal PORTB of mode OUT to actual PORTA of mode IN");
//				assertContains(9, "Couldn't resolve SELF_INSTANCE1");
//				assertContains(9, "Couldn't resolve SELF_INSTANCE1");
//				assertContains(11, "EntityInstantiation: Couldn't find 'SELF_INSTANCE(ABC)'");
				assertContains(9, "Couldn't resolve UNEXISTING");
				assertContains(10, "EntityInstantiation: Couldn't find 'UNIMPLEMENTED_ENTITY'");
				assertContains(11, "EntityInstantiation: Couldn't find 'TOP(UNEXISTING)'");
				//assertContains(12, "Architecture not found for WORK.UNIMPLEMENTED_ENTITY");
				assertContains(12, "Couldn't resolve UNEXISTING_COMPONENT");
				assertContains(13, "Design unit expected here.");
			}
			
		});
	}

	@Test
	public void testPSL4() throws Exception {

		runTest("examples/semantic/psl4Test", 2);
	}

	@Test
	public void testType() throws Exception {

		runTest("examples/semantic/typeTest", 5);
	}
	
	@Test
	public void testSubProgram5() throws Exception {

		runTest("examples/semantic/subProgramTest5", 1);
	}

	@Test
	public void testPSL3() throws Exception {

		runTest("examples/semantic/psl3Test", 2);
	}

	@Test
	public void testSubProgram4() throws Exception {

		runTest("examples/semantic/subProgramTest4", 1);
	}

	@Test
	public void testPSL2() throws Exception {

		runTest("examples/semantic/psl2Test", 1);
	}

	@Test
	public void testPSL() throws Exception {

		runTest("examples/semantic/pslTest", 1);
	}

	//public static class Attributes extends BasicTest
		@Test
		public void attributeTest() throws Exception {
			runTest("examples/semantic/attributes/attributeTest", 2);
		}
		@Test
		public void attribute2Test() throws Exception {
			runTest("examples/semantic/attributes/attribute2Test", 1);
		}
		@Test
		public void attribute3Test() throws Exception {
			runTest("examples/semantic/attributes/attribute3Test", 1);
		}
		@Test
		public void attribute4Test() throws Exception {
			runTest("examples/semantic/attributes/attribute4Test", 1);
		}

	@Test
	public void testSubProgram3() throws Exception {

		runTest("examples/semantic/subProgramTest3", 1);
	}

	@Test
	public void testLiteral() throws Exception {

		runTest("examples/semantic/literalTest", 1);
	}

	@Test
	public void testGenerate() throws Exception {

		runTest("examples/semantic/generateTest", 3);
	}

	@Test
	public void testExpr2() throws Exception {

		runTest("examples/semantic/expr2Test", 1);
	}


	@Test
	public void testAggregate() throws Exception {

		runTest("examples/semantic/aggregateTest", 5);
	}
	
	@Test
	public void testTypeConversion() throws Exception {

		runTest("examples/semantic/typeConversionTest", 4);
	}

	@Test
	public void testBlock() throws Exception {

		runTest("examples/semantic/blockTest", 2);
	}

	@Test
	public void testPackages() throws Exception {

		runTest("examples/leonpkg", 1);
	}

	@Test
	public void testConcat() throws Exception {

		runTest("examples/semantic/concatTest", 2);
	}

	@Test
	public void testConstComputation() throws Exception {

		runTest("examples/semantic/constTest", 3);
	}

	@Test
	public void testVestsCh01() throws Exception {

		runTest("examples/vests/vhdl-93/ashenden/compliant/ch01", 6);
	}

	@Test
	public void testVestsCh02() throws Exception {

		runTest("examples/vests/vhdl-93/ashenden/compliant/ch02", 1);
	}

	@Test
	public void testVestsCh03() throws Exception {

		runTest("examples/vests/vhdl-93/ashenden/compliant/ch03", 1);
	}

	@Test
	public void testVestsCh04() throws Exception {

		runTest("examples/vests/vhdl-93/ashenden/compliant/ch04", 2);
	}

	@Test
	public void testMM() throws Exception {

		File f = new File("examples/gooofy/BuildPath.txt");
		if (!f.exists()) {
			// ok, this test is optional
			return;
		}

		runTest("examples/gooofy", 384);
	}

	@Test
	public void testOggOnAChip() throws Exception {

		runTest("examples/oggonachip", 358);
	}

	@Test
	public void testITC99() throws Exception {

		runTest("examples/itc99", 110);
	}

	@Test
	public void testDLX() throws Exception {

		runTest("examples/dlx", 157);
	}

	@Test
	public void testDDS() throws Exception {

		runTest("examples/dds_synthesizer", 2);
	}

	@Test
	public void testMDCT() throws Exception {

		runTest("examples/mdct", 66);
	}

	@Test
	public void testHapra() throws Exception {

		runTest("examples/hapra", 18);
	}

	@Test
	public void testCounterG() throws Exception {

		runTest("examples/gcounter", 19);
	}

	@Test
	public void testB04() throws Exception {

		runTest("examples/b04", 2);
	}

	@Test
	public void testGCD() throws Exception {

		runTest("examples/gcd", 2);
	}

	@Test
	public void testPlasma() throws Exception {

		runTest("examples/plasma", 44);
	}

	@Test
	public void testAdd4() throws Exception {

		runTest("examples/add4", 13);
	}
	@Test
	public void testPG99Z48() throws Exception {

		runTest("examples/pg99", "BuildPath_z48_tb.txt", 11);
	}

	@Test
	public void testPG99() throws Exception {

		runTest("examples/pg99", 1866);
	}

	@Test
	public void testJOP() throws Exception {

		runTest("examples/jop", 288);
	}

	@Test
	@Ignore
	public void testBP() throws Exception {

		runTest("examples/semantic/bpTest", 1933);
	}

	@Test
	public void testManik() throws Exception {

		runTest("examples/manik", 3257);
	}

	@Test
	public void testLeon() throws Exception {

		runTest("examples/leonSOC", 1933);
	}

	@Test
	@Ignore
	public void testLeonDevices() throws Exception {

		runTest("examples/leonExtern", "BuildPathDevices.txt", 1);
	}

	@Test
	@Ignore
	public void testLeonExtern() throws Exception {

		runTest("examples/leonExtern", 1933);
	}

	@Test
	public void correctSourcesInGeneratedCodeOfConditionalSignalAssignment() throws Exception {

		runTest("examples/semantic/conditionalSignalAssignment", 1);

		generateCodeForFirstStmt();

		checkRequirementsCriticalForCoverage();
	}

	private void checkRequirementsCriticalForCoverage() {

		assertThat(fCode.size(), is(115));

		fooStmtsWithin(0, 51).shouldComeFromLines(11, 12); // last 'JUMP NC 56' should be located on IF-condition's line

		fooStmtsWithin(52, 55).shouldComeFromLines(10); // last 'JUMP 115' should be located on THEN's line

		fooStmtsWithin(56, 107).shouldComeFromLines(14, 15, 16); // last 'JUMP NC 112' => on IF-condition's line

		fooStmtsWithin(108).shouldComeFromLines(10);
		fooStmtsWithin(110, 111).shouldComeFromLines(13); // 'JUMP 115' => on THEN's line

		fooStmtsWithin(112).shouldComeFromLines(10);
		fooStmtsWithin(114).shouldComeFromLines(17);

	}

	private class Matcher {

		LinkedList<IGStmt> fooStmts;

		private Matcher() {
			fooStmts = new LinkedList<IGStmt>();
		}

		void shouldComeFromLines(Integer... lines) {

			for (IGStmt fooStmt : fooStmts)

				assertThat("'" + fooStmt + "'s line should be " + Arrays.toString(lines),
						Arrays.<Integer>asList(lines), hasItem(fooStmt.getLine()));
		}
	}

	private Matcher fooStmtsWithin(int PC) {
		return fooStmtsWithin(PC, PC);
	}

	private Matcher fooStmtsWithin(int startPC, int endPC) {

		Matcher matcher = new Matcher();

		for (int pc = startPC; pc <= endPC; pc++) {
			IGStmt stmt = fCode.get(pc);

			File file = stmt.computeSourceLocation().fSF.getFile();
			if (file != null && file.getName().equals("foo.vhdl")) {
				matcher.fooStmts.add(stmt);
			}
		}

		return matcher;
	}

	private void generateCodeForFirstStmt() throws ZamiaException {

		BuildPath bp = fZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		IGModule module = fZPrj.getIGM().findModule(tl);

		IGConcurrentStatement stmt = module.getStructure().getStatement(0);

		assertThat(stmt, instanceOf(IGProcess.class));

		IGProcess proc = (IGProcess) stmt;

		IGSequentialStatement seq = proc.getSequenceOfStatements().getStatement(0);

		fCode = new IGInterpreterCode("", null);
		seq.generateCode(fCode);
	}

	public static void main (String args[]) {
		IGTest igt = new IGTest();
		try {
			igt.testLeonDevices();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
