/* 
 * Copyright 2009 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.sim.ref;

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
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DUUID;


/**
 * @author Guenter Bartsch
 */
public class IGRefSimTest extends TestCase {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean enableFCounterTest = true;

	public final static boolean enableCOUNTERGTest = true;

	public final static boolean enablePartialSATest = true;

	public final static boolean enablePLASMAALUTest = true;

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("Sim Test Tmp Project", aBasePath, sf);
		fZPrj.clean();
	}

	private DUUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, int aNumNodes, String... aSimRuns) throws Exception {
		runTest(aTestDir, "/BuildPath.txt", aNumNodes, aSimRuns);
	}
	private void runTest(String aTestDir, String aBuildPath, int aNumNodes, String... aSimRuns) throws Exception {
		setupTest(aTestDir, aTestDir + aBuildPath);

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
		logger.info("IGTest: elaborated model for %s has %d modules.", duuid, n);
		assertEquals(aNumNodes, n);

		IGSimRef refsim = new IGSimRef();

		ToplevelPath tlp = new ToplevelPath(new Toplevel(duuid, null), new PathName(""));

		refsim.open(tlp, null, null, fZPrj);

		for (String simRun : aSimRuns) {
			refsim.run(new BigInteger(simRun));
		}
	}

	public void testFCounter() throws Exception {

		if (!enableFCounterTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/refsim/fcounter", 1, "152000000");
		
	}

	public void testCounterG() throws Exception {

		if (!enableCOUNTERGTest) {
			fail("Test disabled");
			return;
		}

		runTest("test/gcounter", 19, "152000000");
	}

	public void testPartialSignalAssignment() throws Exception {

		if (!enablePartialSATest) {
			fail("Test disabled");
			return;
		}

		runTest("test/refsim/partialSignalAssignmentTest", 1, "5000000");
	}


	private void testPlasmaAlu() throws Exception {
		if (!enablePLASMAALUTest) {
			fail("Test disabled");
			return;
		}

		runTest("examples/plasma", "/BuildPathAlu.txt", 2, "29750000000"); // 119 * 250 * 1000000

	}

	public static void main(String args[]) {
		IGRefSimTest igt = new IGRefSimTest();

		try {
//			igt.testFCounter();

			igt.testCounterG();
//			igt.testPlasmaAlu();
//			igt.testPartialSignalAssignment();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
