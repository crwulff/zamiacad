/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.test;

import java.io.File;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.ZamiaProject.VHDLLanguageSupport;
import org.zamia.instgraph.sim.ref.IGSimRef;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DUUID;

/**
 * @author Guenter Bartsch
 */
public class RefSimTest extends TestCase {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean enableGCounterTest = true;

	public final static boolean enableZ48Test = true;

	private ZamiaProject fZPrj;
	
	private final static BigInteger NANO_FACTOR = new BigInteger("1000000");

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

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes, int aNanos) throws Exception {
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
		
		IGSimRef sim = new IGSimRef();
		
		Toplevel tl = new Toplevel(duuid, null);
		
		ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));
		
		sim.open(tlp, null, null, fZPrj);
		
		sim.reset();
		
		sim.run(new BigInteger (""+aNanos).multiply(NANO_FACTOR));
		
	}

	private void runTest(String aTestDir, int aNumNodes, int aNanos) throws Exception {
		runTest (aTestDir, "BuildPath.txt", aNumNodes, aNanos);
	}

	@Override
	protected void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}

	public void testZ48() throws Exception {

		if (!enableZ48Test) {
			fail("Test disabled");
			return;
		}

		runTest("examples/pg99", "BuildPath_z48_tb.txt", 11, 10000);
	}

	public void testGCounter() throws Exception {

		if (!enableGCounterTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/gcounter", 19, 1000);
	}


	public static void main(String args[]) {
		RefSimTest rst = new RefSimTest();

		try {
			rst.testGCounter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
