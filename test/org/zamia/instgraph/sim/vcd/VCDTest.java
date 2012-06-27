/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.sim.vcd;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;
import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;

import java.io.File;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Guenter Bartsch
 */
public class VCDTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private ZamiaProject fZPrj;

	private ERManager fERM;

	private DMManager fDUM;

	private VCDImport fImport;

	private void setupTest(String aTestDir, int aNumNodes, String aToplevel, String aVCDFile, PathName aPrefix) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aTestDir + "/BuildPath.txt");

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("VCD Test Tmp Project", aTestDir, sf, null);
		fZPrj.clean();
		fERM = fZPrj.getERM();
		fDUM = fZPrj.getDUM();

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = DMUID.parse(aToplevel);

		duuid = fDUM.getArchDUUID(duuid);

		assertNotNull(duuid);

		int n = fERM.getNumErrors();
		logger.error("VCDTest: Build finished. Found %d errors.", n);

		for (int i = 0; i < n; i++) {
			ZamiaException em = fERM.getError(i);
			logger.error("VCDTest: error %6d/%6d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		n = fZPrj.getIGM().countNodes(duuid);
		logger.info("VCDTest: elaborated model for %s has %d unique modules.", duuid, n);
		assertEquals(aNumNodes, n);

		/*
		 * load sim data from VCD file
		 */

		Toplevel tl = new Toplevel(duuid, null);

		fImport = new VCDImport();

		ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));

		fImport.open(tlp, new File(aVCDFile), aPrefix, fZPrj);
	}

	@Test
	public void testPlasmaModelsim() throws Exception {
		setupTest("examples/plasma", 19, "TBENCH", "examples/vcd/four-state_tbench_ModelSim.vcd", new PathName("TBENCH"));

		assertNotNull(fImport);

		BigInteger endTime = fImport.getEndTime();
		assertEquals(10000000000l, endTime.longValue());
		BigInteger startTime = fImport.getStartTime();
		assertEquals(0, startTime.longValue());

		List<PathName> names = fImport.findSignalNamesRegexp("*", 1024);
		int n = names.size();
		assertEquals(200, n);
		for (int i = 0; i < n; i++) {

			PathName path = names.get(i);

			logger.info("VCDTest: Signal #%4d/%4d: %s", i + 1, n, path);

			IGISimCursor cursor = fImport.createCursor();

			boolean b = cursor.gotoTransition(path, startTime);
			assertTrue(b);

			IGStaticValue v = cursor.getCurrentValue();
			assertNotNull(v);

			//logger.info("Got Value: %s", v);

			BigInteger t = cursor.getCurrentTime();
			assertNotNull(t);

			t = cursor.gotoNextTransition(endTime);
			assertNotNull(t);
			assertTrue(t.compareTo(startTime) >= 0);
		}
	}

	@Test
	public void testCounter() throws Exception {
		setupTest("examples/gcounter", 19, "COUNTER_TB", "examples/vcd/gcounter_tb.vcd", null);

		assertNotNull(fImport);

		BigInteger endTime = fImport.getEndTime();
		assertEquals(250000000, endTime.longValue());
		BigInteger startTime = fImport.getStartTime();
		assertEquals(0, startTime.longValue());

		List<PathName> names = fImport.findSignalNamesRegexp("*", 1024);
		int n = names.size();
		assertEquals(79, n);
		for (int i = 0; i < n; i++) {

			PathName path = names.get(i);

			logger.info("VCDTest: Signal #%4d/%4d: %s", i + 1, n, path);

			IGISimCursor cursor = fImport.createCursor();

			boolean b = cursor.gotoTransition(path, startTime);
			assertTrue(b);

			IGStaticValue v = cursor.getCurrentValue();
			assertNotNull(v);

			BigInteger t = cursor.getCurrentTime();
			assertNotNull(t);

			t = cursor.gotoNextTransition(endTime);
			assertNotNull(t);
			assertTrue(t.compareTo(startTime) >= 0);
		}
	}

	@Test
	public void testPlasma() throws Exception {
		setupTest("examples/plasma", 11, "CPU_TB", "examples/vcd/plasma_tb.vcd", null);

		assertNotNull(fImport);

		BigInteger endTime = fImport.getEndTime();
		assertEquals(10000000000l, endTime.longValue());
		BigInteger startTime = fImport.getStartTime();
		assertEquals(0, startTime.longValue());

		List<PathName> names = fImport.findSignalNamesRegexp("*", 1024);
		int n = names.size();
		assertEquals(200, n);
		for (int i = 0; i < n; i++) {

			PathName path = names.get(i);

			logger.info("VCDTest: Signal #%4d/%4d: %s", i + 1, n, path);

			IGISimCursor cursor = fImport.createCursor();

			boolean b = cursor.gotoTransition(path, startTime);
			assertTrue(b);

			IGStaticValue v = cursor.getCurrentValue();
			assertNotNull(v);

			//logger.info("Got Value: %s", v);

			BigInteger t = cursor.getCurrentTime();
			assertNotNull(t);

			t = cursor.gotoNextTransition(endTime);
			assertNotNull(t);
			assertTrue(t.compareTo(startTime) >= 0);
		}
	}

	@Test
	public void testIdConversion() throws Exception {
		String id = VCDData.convertSegmentId("bar(1)");
		assertEquals("BAR#1", id);
		id = VCDData.convertSegmentId("FOO(1)");
		assertEquals("FOO#1", id);
		id = VCDData.convertSegmentId("verylongid(78)");
		assertEquals("VERYLONGID#78", id);
	}

	@After
	public void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}
}
