/* 
 * Copyright 2009 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.ZamiaProject.VHDLLanguageSupport;
import org.zamia.vhdl.ast.DUUID;

/**
 * @author Guenter Bartsch
 */
public class IGTest extends TestCase {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean enablePSL6Test = true;

	public final static boolean enableLiteral5Test = true;

	public final static boolean enableResolverTest = true;

	public final static boolean enableLiteral4Test = true;

	public final static boolean enableGenerate2Test = true;

	public final static boolean enableVarDeclarationTest = true;

	public final static boolean enableConcat6Test = true;

	public final static boolean enablePSL5Test = true;

	public final static boolean enableType3Test = true;

	public final static boolean enableInst3Test = true;

	public final static boolean enablePSL4Test = true;

	public final static boolean enableType2Test = true;

	public final static boolean enableSubProgram5Test = true;

	public final static boolean enableConcat5Test = true;

	public final static boolean enableArray2Test = true;

	public final static boolean enablePSL3Test = true;

	public final static boolean enableSubProgram4Test = true;

	public final static boolean enableLiteral3Test = true;

	public final static boolean enableBPTest = true;

	public final static boolean enablePSL2Test = true;

	public final static boolean enableAttribute4Test = true;

	public final static boolean enableConst3Test = true;

	public final static boolean enablePSLTest = true;

	public final static boolean enableAttr3Test = true;

	public final static boolean enableInst2Test = true;

	public final static boolean enableAttr2Test = true;

	public final static boolean enableExpr2Test = true;

	public final static boolean enableSubProgram3Test = true;

	public final static boolean enableInstTest = true;

	public final static boolean enableLiteral2Test = true;

	public final static boolean enableTypeConversion2Test = true;

	public final static boolean enableConst2Test = true;

	public final static boolean enableLiteralTest = true;

	public final static boolean enableGenerateTest = true;

	public final static boolean enableAggregate3Test = true;

	public final static boolean enableConcat4Test = true;

	public final static boolean enableBlockTest = true;

	public final static boolean enablePG99Test = true;

	public final static boolean enableOggOnAChipTest = true;

	public final static boolean enableArrayTest = true;

	public final static boolean enableAVSAESTest = true;

	public final static boolean enableITC99Test = true;

	public final static boolean enableJOPTest = true;

	public final static boolean enableDLXTest = true;

	public final static boolean enableConcat3Test = true;

	public final static boolean enableExprTest = true;

	public final static boolean enableVestsTest = true;

	public final static boolean enableTypeTest = true;

	public final static boolean enableAttributeTest = true;

	public final static boolean enableDDSTest = true;

	public final static boolean enableMDCTTest = true;

	public final static boolean enableManikTest = true;

	public final static boolean enableHapraTest = true;

	public final static boolean enableAggregateTest = true;

	public final static boolean enableAggregate2Test = true;

	public final static boolean enableConcat2Test = true;

	public final static boolean enableSubProgram2Test = true;

	public final static boolean enableConstComputationTest = true;

	public final static boolean enableTypeConversionTest = true;

	public final static boolean enableCOUNTERGTest = true;

	public final static boolean enableB04Test = true;

	public final static boolean enableGCDTest = true;

	public final static boolean enableConcatTest = true;

	public final static boolean enablePlasmaTest = true;

	public final static boolean enableADD4Test = true;

	public final static boolean enablePackagesTest = true;

	public final static boolean enableMMTest = true;

	public final static boolean enableLeonTest = true;

	public final static boolean enableLeonExternTest = true;

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath, VHDLLanguageSupport aVHDLLanguageSupport) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("IG Test Tmp Project", aBasePath, sf, null, aVHDLLanguageSupport);
		fZPrj.clean();
	}

	private DUUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName, VHDLLanguageSupport.VHDL2008);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DUUID duuid = getUID(fZPrj);

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
		runTest (aTestDir, "BuildPath.txt", aNumNodes);
	}

	@Override
	protected void tearDown() {
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

	public void testPG99() throws Exception {

		if (!enablePG99Test) {
			fail("Test disabled");
			return;
		}

		runTest("examples/pg99", "BuildPath_z48_tb.txt", 11);
	}

	public void testJOP() throws Exception {

		if (!enableJOPTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/jop", 288);
	}

	public void testBP() throws Exception {

		if (!enableBPTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/bpTest", 1913);
	}

	public void testLeonExtern() throws Exception {

		if (!enableLeonExternTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/leonExtern", 1913);
	}

	public void testLiteral5() throws Exception {

		if (!enableLiteral5Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/literal5Test", 1);
	}

	public void testManik() throws Exception {

		if (!enableManikTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/manik", 3257);
	}

	public void testResolver() throws Exception {

		if (!enableResolverTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/resolverTest", 1);
	}

	public void testLiteral4() throws Exception {

		if (!enableLiteral4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/literal4Test", 1);
	}

	public void testGenerate2() throws Exception {

		if (!enableGenerate2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/generate2Test", 69);
	}

	public void testVarDeclaration() throws Exception {

		if (!enableVarDeclarationTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/varDeclarationTest", 1);
	}

	public void testLeon() throws Exception {

		if (!enableLeonTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/leonSOC", 1913);
	}

	public void testConcat6() throws Exception {

		if (!enableConcat6Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concat6Test", 1);
	}

	public void testPSL5() throws Exception {

		if (!enablePSL5Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/psl5Test", 1);
	}

	public void testType3() throws Exception {

		if (!enableType3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/type3Test", 1);
	}

	public void testInst3() throws Exception {

		if (!enableInst3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/inst3Test", 2);
	}

	public void testPSL4() throws Exception {

		if (!enablePSL4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/psl4Test", 2);
	}

	public void testType2() throws Exception {

		if (!enableType2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/type2Test", 1);
	}

	public void testSubProgram5() throws Exception {

		if (!enableSubProgram5Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/subProgramTest5", 1);
	}

	public void testConcat5() throws Exception {

		if (!enableConcat5Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concat5Test", 1);
	}

	public void testArray2() throws Exception {

		if (!enableArray2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/array2Test", 1);
	}

	public void testPSL3() throws Exception {

		if (!enablePSL3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/psl3Test", 2);
	}

	public void testSubProgram4() throws Exception {

		if (!enableSubProgram4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/subProgramTest4", 1);
	}

	public void testLiteral3() throws Exception {

		if (!enableLiteral3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/literal3Test", 1);
	}

	public void testPSL2() throws Exception {

		if (!enablePSL2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/psl2Test", 1);
	}

	public void testAttribute4() throws Exception {

		if (!enableAttribute4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/attribute4Test", 1);
	}

	public void testConst3() throws Exception {

		if (!enableConst3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/const3Test", 1);
	}

	public void testPSL() throws Exception {

		if (!enablePSLTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/pslTest", 1);
	}

	public void testAttr3() throws Exception {

		if (!enableAttr3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/attribute3Test", 1);
	}

	public void testInst2() throws Exception {

		if (!enableInst2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/inst2Test", 2);
	}

	public void testAttr2() throws Exception {

		if (!enableAttr2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/attribute2Test", 1);
	}

	public void testExpr2() throws Exception {

		if (!enableExpr2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/expr2Test", 1);
	}

	public void testSubProgram3() throws Exception {

		if (!enableSubProgram3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/subProgramTest3", 1);
	}

	public void testLiteral2() throws Exception {

		if (!enableLiteral2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/literal2Test", 1);
	}

	public void testTypeConversion2() throws Exception {

		if (!enableTypeConversion2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/typeConversion2Test", 1);
	}

	public void testConst2() throws Exception {

		if (!enableConst2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/const2Test", 4);
	}

	public void testLiteral() throws Exception {

		if (!enableLiteralTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/literalTest", 1);
	}

	public void testGenerate() throws Exception {

		if (!enableGenerateTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/generateTest", 3);
	}

	public void testAggregate3() throws Exception {

		if (!enableAggregate3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/aggregate3Test", 2);
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

	public void testConcat4() throws Exception {

		if (!enableConcat4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concat4Test", 1);
	}

	public void testArray() throws Exception {

		if (!enableArrayTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/arrayTest", 1);
	}

	public void testVestsCh01() throws Exception {

		if (!enableVestsTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/vests/vhdl-93/ashenden/compliant/ch01", 6);
	}

	public void testVestsCh02() throws Exception {

		if (!enableVestsTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/vests/vhdl-93/ashenden/compliant/ch02", 1);
	}

	public void testVestsCh03() throws Exception {

		if (!enableVestsTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/vests/vhdl-93/ashenden/compliant/ch03", 1);
	}

	public void testVestsCh04() throws Exception {

		if (!enableVestsTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/vests/vhdl-93/ashenden/compliant/ch04", 2);
	}

	public void testTypeConversion() throws Exception {

		if (!enableTypeConversionTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/typeConversionTest", 1);
	}

	public void testBlock() throws Exception {

		if (!enableBlockTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/blockTest", 2);
	}

	public void testMM() throws Exception {

		File f = new File("examples/gooofy/BuildPath.txt");
		if (!f.exists()) {
			// ok, this test is optional
			return;
		}

		if (!enableMMTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/gooofy", 384);
	}

	public void testOggOnAChip() throws Exception {

		if (!enableOggOnAChipTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/oggonachip", 358);
	}

	public void testType() throws Exception {

		if (!enableTypeTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/typeTest", 1);
	}

	public void testAVSAES() throws Exception {

		if (!enableAVSAESTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/avs_aes", 125);
	}

	public void testITC99() throws Exception {

		if (!enableITC99Test) {
			fail("Test disabled");
			return;
		}

		runTest("examples/itc99", 1);
	}

	public void testDLX() throws Exception {

		if (!enableDLXTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/dlx", 157);
	}

	public void testConcat3() throws Exception {

		if (!enableConcat3Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concat3Test", 1);
	}

	public void testExpr() throws Exception {

		if (!enableExprTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/exprTest", 1);
	}

	public void testAttributes() throws Exception {

		if (!enableAttributeTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/attributeTest", 1);
	}

	public void testDDS() throws Exception {

		if (!enableDDSTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/dds_synthesizer", 2);
	}

	public void testMDCT() throws Exception {

		if (!enableMDCTTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/mdct", 66);
	}

	public void testHapra() throws Exception {

		if (!enableHapraTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/hapra", 18);
	}

	public void testAggregate() throws Exception {

		if (!enableAggregateTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/aggregateTest", 1);
	}

	public void testConcat2() throws Exception {

		if (!enableConcat2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concat2Test", 1);
	}

	public void testSubProgram2() throws Exception {

		if (!enableSubProgram2Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/subProgramTest2", 1);
	}

	public void testConstComputation() throws Exception {

		if (!enableConstComputationTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/constTest", 3);
	}

	public void testPackages() throws Exception {

		if (!enablePackagesTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/leonpkg", 1);
	}

	public void testCounterG() throws Exception {

		if (!enableCOUNTERGTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/gcounter", 19);
	}

	public void testB04() throws Exception {

		if (!enableB04Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/b04", 2);
	}

	public void testGCD() throws Exception {

		if (!enableGCDTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/gcd", 2);
	}

	public void testConcat() throws Exception {

		if (!enableConcatTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/semantic/concatTest", 2);
	}

	public void testPlasma() throws Exception {

		if (!enablePlasmaTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/plasma", 14);
	}

	public void testAdd4() throws Exception {

		if (!enableADD4Test) {
			fail("Test disabled");
			return;
		}

		runTest("test/add4", 13);
	}

	public static void main(String args[]) {
		IGTest igt = new IGTest();

		try {
			//igt.testManik();
			igt.testLeonExtern();
			//igt.testPlasma();
			//igt.testJOP();
			//igt.testCounterG();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
