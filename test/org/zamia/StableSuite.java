package org.zamia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zamia.analysis.SATest;
import org.zamia.instgraph.IGTest;
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
 * Some tests do not clean ZDB on exit. To work around, set env. 
 *   var ZAMIA_LOCKING=disabled
 *   
 * To allocate enautgh VM memory: -Xmx1000m -Xms800m -Xss8m
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
 *    testVestsChXX and testHapra - example files not found)
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
		IGSynthTest.class
})
public class StableSuite {
}
