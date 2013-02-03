package org.zamia.instgraph.interpreter.logger;

import java.io.File;

import org.junit.Test;

import org.zamia.cli.Zamia;
import org.zamia.util.FileUtils;
import org.zamia.util.XMLUtils;

import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class ReportUtilsTest {

	private final static boolean NO_ROBSY = !(new File("examples/robsy/BuildPath.txt").exists());

	@Test
	public void testWrite2XMLFile() throws Exception {

		if (NO_ROBSY) {
			return; // ok, this test is optional
		}

		File golden_In = new File("examples/robsy/xml/Assertain_example.xml");
		File goldenOut = new File("examples/robsy/xml/Assertain_example_OUT.xml");
		File copy = new File("examples/robsy/xml/Assertain_example2.xml");
		copy.delete();
		assertTrue(!copy.exists());
		FileUtils.copy(golden_In, copy);

		Zamia zamia = new Zamia(new String[]{"-d", "examples/robsy", "-x", "xml/Assertain_example2.xml"});
		zamia.getZPrj().shutdown();

		assertTrue(XMLUtils.parseXML(copy).isEqualNode(XMLUtils.parseXML(goldenOut)));

		assertTrue(copy.delete());
	}

	@Test
	public void testWrite2XMLFilePlasma() throws Exception {

		File golden_In = new File("examples/plasma_debug/debug.xml");
		File goldenOut = new File("examples/plasma_debug/debug_GOLDEN_OUT.xml");
		File copy = new File("examples/plasma_debug/debug2.xml");
		copy.delete();
		assertTrue(!copy.exists());
		FileUtils.copy(golden_In, copy);

		Zamia zamia = new Zamia(new String[]{"-d", "examples/plasma_debug", "-x", "debug2.xml"});
		zamia.getZPrj().shutdown();

		assertTrue(XMLUtils.parseXML(copy).isEqualNode(XMLUtils.parseXML(goldenOut)));

		assertTrue(copy.delete());
	}
}
