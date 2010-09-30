/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.test;

import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject.VHDLLanguageSupport;


/**
 * @author Guenter Bartsch
 */
public class DG2ETest extends TestCase {

	PrintStream out = System.out;

	PrintStream err = System.err;

	private Builder fBuilder;

	public void setupTest(VHDLLanguageSupport aLanguageSupport) {
		ZamiaLogger.setup(Level.DEBUG);
		fBuilder = new Builder(out, aLanguageSupport);
	}

	public void testVHDL2008Parser() throws Exception {

		setupTest(VHDLLanguageSupport.VHDL2008);

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("test/dg2e", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	public void testVHDL2002Parser() throws Exception {

		setupTest(VHDLLanguageSupport.VHDL2002);

		fBuilder.clean();
		int n;
		if ((n = fBuilder.compileDir("test/dg2e", "WORK")) > 0) {
			System.out.println("Total: " + n + " errors.");
			fail("Errors detected.");
		}
	}

	@Override
	protected void tearDown() {
		fBuilder.shutdown();
	}

	//	public void testOneFile() throws Exception {
	//		setupTest();
	//
	//		b.clean();
	//		int n;
	//		if ( (n=b.compileFile("/home/guenter/projects/workspace/zamia/test/dg2e/ch_04/fg_04_06.vhd")) > 0) {
	//			System.out.println ("Total: "+n+" errors.");
	//			
	//			fail("Errors detected.");		
	//		}
	//	}

}
