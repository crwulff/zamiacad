/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DMUID;

/**
 * @author Guenter Bartsch
 */
public class IncrementalUpdateIGTest extends TestCase {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean enableManikTest = true;

	public final static boolean enablePlasmaTest = true;

	public final static boolean enableLeonTest = true;

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("IG Test Tmp Project", aBasePath, sf, null);
		fZPrj.clean();
	}

	private DMUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, int aNumNodes, HashSetArray<SourceFile> aChanged, int aNumRebuiltNodes) throws Exception {
		setupTest(aTestDir, aTestDir + "/BuildPath.txt");

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
		logger.info("IGTest: elaborated model for %s has %d unique nodes.", duuid, n);
		assertEquals(aNumNodes, n);

		n = builder.build(false, false, aChanged);
		logger.info("IGTest: changed model got %d nodes rebuilt.", n);
		assertEquals(aNumRebuiltNodes, n);

		n = fZPrj.getIGM().countNodes(duuid);
		logger.info("IGTest: changed model for %s has %d unique nodes.", duuid, n);
		assertEquals(aNumNodes, n);

		fZPrj.shutdown();
	}

	public void testPlasma() throws Exception {

		if (!enablePlasmaTest) {
			fail("Test disabled");
			return;
		}

		String baseDir = "examples/plasma";

		SourceFile sf1 = new SourceFile(new File(baseDir + "/" + "mlite_cpu.vhd"));
		SourceFile sf2 = new SourceFile(new File(baseDir + "/" + "uart.vhd"));

		HashSetArray<SourceFile> changed = new HashSetArray<SourceFile>(2);
		changed.add(sf1);
		changed.add(sf2);

		runTest(baseDir, 14, changed, 5);
	}

	public void testManik() throws Exception {

		if (!enableManikTest) {
			fail("Test disabled");
			return;
		}

		String baseDir = "examples/manik";

		HashSetArray<SourceFile> changed = new HashSetArray<SourceFile>(2);
		addSource(baseDir, "manik/regfile.vhd", changed);

		runTest("examples/manik", 3257, changed, 2);
	}

	public void testLeon() throws Exception {

		if (!enableLeonTest) {
			fail("Test disabled");
			return;
		}

		String baseDir = "examples/leonSOC";

		HashSetArray<SourceFile> changed = new HashSetArray<SourceFile>(2);
		addSource(baseDir, "lib/gaisler/uart/ahbuart.vhd", changed);
		addSource(baseDir, "lib/gaisler/uart/apbuart.vhd", changed);
		addSource(baseDir, "lib/gaisler/leon3/mmu.vhd", changed);
		addSource(baseDir, "lib/gaisler/greth/grethm.vhd", changed);

		runTest(baseDir, 1913, changed, 6);
	}

	private void addSource(String aBaseDir, String aLocalPath, HashSetArray<SourceFile> aChanged) {
		SourceFile sf = new SourceFile(new File(aBaseDir + File.separator + aLocalPath));
		sf.setLocalPath(aLocalPath);
		aChanged.add(sf);
	}

	public static void main(String args[]) {
		IncrementalUpdateIGTest igt = new IncrementalUpdateIGTest();

		try {
			//igt.testLeonExtern();
			igt.testPlasma();
			//igt.testJOP();
			//igt.testCounterG();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
