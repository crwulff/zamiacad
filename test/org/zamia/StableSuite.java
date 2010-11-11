package org.zamia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zamia.analysis.SATest;
import org.zamia.instgraph.IGTest;
import org.zamia.instgraph.sim.ref.IGRefSimTest;
import org.zamia.rtl.sim.vcd.VCDTest;
import org.zamia.verilog.VerilogParserIntegrationTest;
import org.zamia.verilog.VerilogParserTest;
import org.zamia.vhdl.VHDLIndexerTest;
import org.zamia.zdb.ZDBTest;

/**
 * @author Anton Chepurov
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
		VerilogParserIntegrationTest.class
})
public class StableSuite {
}
