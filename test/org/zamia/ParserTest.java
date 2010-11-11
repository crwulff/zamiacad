/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.fail;

/**
 * @author Guenter Bartsch
 */
public class ParserTest {

	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out);
	}

	@Test
	public void testWhenConcatenation() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("examples/whenamp.vhdl")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	@Test
	public void testCharLiterals() throws Exception {
		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileFile("examples/conversions.vhd")) > 0) {
			System.out.println("Total: " + n + " errors.");

			fail("Errors detected.");
		}
	}

	@Test
	public void testG2() throws Exception {

		setupTest();

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("examples/g2", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	@Test
	public void testExamples() throws Exception {

		setupTest();

		fBuilder.clean();

		String[] dirs = {
				"examples/leonFPU",
				"examples/n68",
				"examples/suskaIIIc"
		};
		int n;
		for (String dir : dirs) {
			if ((n = fBuilder.compileDir(dir, "WORK")) > 0) {
				System.out.println("Total: " + n + " errors.");
				fail("Errors detected.");
			}
		}
	}

	@After
	public void tearDown() {
		fBuilder.shutdown();
	}

}
