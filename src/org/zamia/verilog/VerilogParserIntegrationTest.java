/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia.verilog;

import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.ZamiaLogger;
import org.zamia.test.Builder;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class VerilogParserIntegrationTest extends TestCase {
	
	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out);
	}

	public void testOR1200() throws Exception {

		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("test/verilog/or1200-rel1", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}
	
	@Override
	protected void tearDown() {
		fBuilder.shutdown();
	}

}
