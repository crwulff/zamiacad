/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.vhdl.ast.DMUID;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Guenter Bartsch
 */
public class IGTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("IG Test Tmp Project", aBasePath, sf, null);
		fZPrj.clean();
	}

	private DMUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = getUID(fZPrj);

		int n = fZPrj.getERM().getNumErrors();
		logger.error("IGTest: Build finished. Found %d errors.", n);

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("IGTest: error %6d/%6d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		n = fZPrj.getIGM().countNodes(duuid);
		logger.info("IGTest: elaborated model for %s has %d unique modules.", duuid, n);
		assertEquals(aNumNodes, n);
	}

	private void runTest(String aTestDir, int aNumNodes) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes);
	}

	@After
	public void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}

	// disabled for now (supposed to generate errors)
	//	public void testInst() throws Exception {
	//
	//		if (!enableInstTest) {
	//			fail("Test disabled");
	//			return;
	//		}
	//
	//		runTest("test/semantic/instTest", 2);
	//	}

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
	public void testGenerics1() throws Exception {

		runTest("examples/semantic/genericsTest1", 2);
	}

	@Test
	public void testAVSAES() throws Exception {

		runTest("examples/avs_aes", 125);
	}

	@Test
	public void testAggregate() throws Exception {

		runTest("examples/semantic/aggregateTest", 1);
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
	public void testAlias1() throws Exception {

		runTest("examples/semantic/alias1Test", 1);
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
	public void testSubProgram2() throws Exception {

		runTest("examples/semantic/subProgramTest2", 1);
	}

	@Test
	public void testConcat7() throws Exception {

		runTest("examples/semantic/concat7Test", 1);
	}

	@Test
	public void testConcat2() throws Exception {

		runTest("examples/semantic/concat2Test", 1);
	}

	@Test
	public void testLiteral5() throws Exception {

		runTest("examples/semantic/literal5Test", 1);
	}

	@Test
	public void testResolver() throws Exception {

		runTest("examples/semantic/resolverTest", 1);
	}

	@Test
	public void testLiteral4() throws Exception {

		runTest("examples/semantic/literal4Test", 1);
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
	public void testConcat6() throws Exception {

		runTest("examples/semantic/concat6Test", 1);
	}

	@Test
	public void testPSL5() throws Exception {

		runTest("examples/semantic/psl5Test", 1);
	}

	@Test
	public void testType3() throws Exception {

		runTest("examples/semantic/type3Test", 1);
	}

	@Test
	public void testInst3() throws Exception {

		runTest("examples/semantic/inst3Test", 2);
	}

	@Test
	public void testPSL4() throws Exception {

		runTest("examples/semantic/psl4Test", 2);
	}

	@Test
	public void testType2() throws Exception {

		runTest("examples/semantic/type2Test", 1);
	}

	@Test
	public void testSubProgram5() throws Exception {

		runTest("examples/semantic/subProgramTest5", 1);
	}

	@Test
	public void testConcat5() throws Exception {

		runTest("examples/semantic/concat5Test", 1);
	}

	@Test
	public void testArray2() throws Exception {

		runTest("examples/semantic/array2Test", 1);
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
	public void testLiteral3() throws Exception {

		runTest("examples/semantic/literal3Test", 1);
	}

	@Test
	public void testPSL2() throws Exception {

		runTest("examples/semantic/psl2Test", 1);
	}

	@Test
	public void testAttribute4() throws Exception {

		runTest("examples/semantic/attribute4Test", 1);
	}

	@Test
	public void testConst3() throws Exception {

		runTest("examples/semantic/const3Test", 1);
	}

	@Test
	public void testPSL() throws Exception {

		runTest("examples/semantic/pslTest", 1);
	}

	@Test
	public void testAttr3() throws Exception {

		runTest("examples/semantic/attribute3Test", 1);
	}

	@Test
	public void testInst2() throws Exception {

		runTest("examples/semantic/inst2Test", 2);
	}

	@Test
	public void testAttr2() throws Exception {

		runTest("examples/semantic/attribute2Test", 1);
	}

	@Test
	public void testExpr2() throws Exception {

		runTest("examples/semantic/expr2Test", 1);
	}

	@Test
	public void testSubProgram3() throws Exception {

		runTest("examples/semantic/subProgramTest3", 1);
	}

	@Test
	public void testLiteral2() throws Exception {

		runTest("examples/semantic/literal2Test", 1);
	}

	@Test
	public void testTypeConversion2() throws Exception {

		runTest("examples/semantic/typeConversion2Test", 1);
	}

	@Test
	public void testConst2() throws Exception {

		runTest("examples/semantic/const2Test", 4);
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
	public void testAggregate3() throws Exception {

		runTest("examples/semantic/aggregate3Test", 2);
	}

	@Test
	public void testConcat3() throws Exception {

		runTest("examples/semantic/concat3Test", 1);
	}

	@Test
	public void testAttributes() throws Exception {

		runTest("examples/semantic/attributeTest", 1);
	}

	// at the moment it is not clear whether this
	// test is supposed to work or not
	// ghdl doesn't like this one either:
	// aggTest.vhdl:20:32: type "rt2" not allowed in an expression
	//	public void testAggregate2() throws Exception {
	//
	//		if (!enableAggregate2Test) {
	//			fail("Test disabled");
	//			return;
	//		}
	//
	//		runTest("test/semantic/aggregate2Test", 1);
	//	}
	@Test
	public void testConcat4() throws Exception {

		runTest("examples/semantic/concat4Test", 1);
	}

	@Test
	public void testTypeConversion() throws Exception {

		runTest("examples/semantic/typeConversionTest", 1);
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
	public void testType() throws Exception {

		runTest("examples/semantic/typeTest", 1);
	}

	@Test
	public void testITC99() throws Exception {

		runTest("examples/itc99", 1);
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

		runTest("examples/plasma", 14);
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
	public void testLeonDevices() throws Exception {

		runTest("examples/leonExtern", "BuildPathDevices.txt", 1);
	}

	@Test
	public void testLeonExtern() throws Exception {

		runTest("examples/leonExtern", 1933);
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
