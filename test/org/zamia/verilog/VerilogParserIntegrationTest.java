/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia.verilog;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zamia.Builder;
import org.zamia.ZamiaLogger;

import java.io.PrintStream;

import static org.junit.Assert.fail;

/**
 * @author Guenter Bartsch
 */
public class VerilogParserIntegrationTest {

	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	@Before
	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out);
	}

	@Test
	public void testOR1200() throws Exception {

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("examples/verilog/or1200-rel1", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	@After
	public void tearDown() {
		fBuilder.shutdown();
	}

}
