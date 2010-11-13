/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.sim.ref;

import org.apache.log4j.Level;
import org.junit.Test;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;

import java.io.File;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Guenter Bartsch
 */
public class IGRefSimTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	private final static BigInteger NANO_FACTOR = new BigInteger("1000000");

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("Sim Test Tmp Project", aBasePath, sf);
		fZPrj.clean();
	}

	private DMUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes, int aNanos) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = getUID(fZPrj);

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

		sim.run(new BigInteger("" + aNanos).multiply(NANO_FACTOR));

	}

	private void runTest(String aTestDir, int aNumNodes, int aNanos) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes, aNanos);
	}

	@Test
	public void testRisingEdge() throws Exception {
		runTest("examples/refsim/params2", 1, 152);
	}
	
	@Test
	public void testParams1() throws Exception {
		runTest("examples/refsim/params1", 1, 100);
	}
	
	@Test
	public void testExpr1() throws Exception {

		runTest("examples/refsim/expr1", 1, 100);

	}
	
	@Test
	public void testHexLiteral() throws Exception {

		runTest("examples/refsim/hexLiteral", 1, 100);

	}
	
	@Test
	public void testGCounter() throws Exception {

		runTest("examples/gcounter", 19, 152);
	}

	@Test
	public void testFCounter() throws Exception {

		runTest("examples/refsim/fcounter", 1, 152);

	}

	// FIXME: disabled for now as sources are missing
	//	public void testPartialSignalAssignment() throws Exception {
	//
	//		if (!enablePartialSATest) {
	//			fail("Test disabled");
	//			return;
	//		}
	//
	//		runTest("test/refsim/partialSignalAssignmentTest", 1, 5);
	//	}

	@Test
	public void testPlasmaAlu() throws Exception {

		runTest("examples/plasma", "/BuildPathAlu.txt", 2, 29750); // 119 * 250
	}

	@Test
	public void testZ48() throws Exception {

		runTest("examples/pg99", "BuildPath_z48_tb.txt", 11, 10000);
	}
	@Test
	public void testPlasma() throws Exception {

		runTest("examples/plasma", "BuildPathTbench.txt", 19, 0);
	}
	@Test
	public void testEntityInst() throws Exception {

		runTest("examples/refsim/entityInst", 2, 152);
	}
}
