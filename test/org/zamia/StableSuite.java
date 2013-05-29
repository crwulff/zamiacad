package org.zamia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zamia.analysis.SATest;
import org.zamia.instgraph.IGTest;
import org.zamia.instgraph.interpreter.logger.ReportUtilsTest;
import org.zamia.instgraph.sim.ref.IGRefSimTest;
import org.zamia.instgraph.sim.vcd.VCDTest;
import org.zamia.instgraph.synth.IGSynthTest;
import org.zamia.verilog.VerilogParserIntegrationTest;
import org.zamia.verilog.VerilogParserTest;
import org.zamia.vhdl.VHDLIndexerTest;
import org.zamia.zdb.ZDBTest;

/**
 * @author Anton Chepurov
 * 
 *
 * To allocate enough VM memory: -ea -Xmx3000m -Xms800m -Xss8m -verbose:gc -XX:+PrintGCDetails
 * The problem is that even 3.5 GB is not enough for many Leon tests (OutOfMem is not propagated to the detector). So, they are ignored in FSCacheTest and IGTest
 * 
 * Tests that currently fail:
 *  org.zamia.analysis.SATest
 *    testGCounterASTDeclarationSearch
 *    testGCounterASTCompletion
 *    testGCounterASTReferenceSearch
 *    testPlasmaASTReferenceSearch
 *  org.zamia.instgraph.sim.vcd.VCDTest - testPlasmaModelsim
 *  instgraph.IGSynthTest
 *    testPlasmaAlu
 *    testArrayIdx
 *    testCombProc
 *  org.zamia.instgraph.IGTest 
 *    testVestsChXX 
 *    testHapra - example files not found
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		IGRefSimTest.class,
		FSCacheTest.class,
		BuildPathTest.class,
		DG2ETest.class,
		VHDLIndexerTest.class,
		IGTest.class,
		SATest.class,
		ParserTest.class,
		ZDBTest.class,
		VCDTest.class,
		VerilogParserTest.class,
		VerilogParserIntegrationTest.class,
		IGSynthTest.class,
		ReportUtilsTest.class
})
public class StableSuite {
}
