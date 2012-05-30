/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.sim.ref;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;

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
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zamia.util.FileUtils.unzip;

/**
 * @author Guenter Bartsch
 */
public class IGRefSimTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	private IGSimRef fSim;

	private final static BigInteger NANO_FACTOR = new BigInteger("1000000");

	private final static boolean NO_ERADOS = !(new File("examples/erados/BuildPath.txt").exists());

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("Sim Test Tmp Project", aBasePath, sf);
		fZPrj.clean();
	}

	private DMUID getUID() {

		BuildPath bp = fZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return fZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes, int aNanos) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = getUID();

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

		fSim = new IGSimRef();

		Toplevel tl = new Toplevel(duuid, null);

		ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));

		fSim.open(tlp, null, null, fZPrj);

		fSim.reset();

		fSim.run(new BigInteger("" + aNanos).multiply(NANO_FACTOR));

	}

	private void runTest(String aTestDir, int aNumNodes, int aNanos) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes, aNanos);
	}

	@Test
	public void testProcretReportAndNow() throws Exception {
		runTest("examples/refsim/procret-report-now", 1, 60);
	}
	
	@Test
	public void testParamsEvent() throws Exception {
		runTest("examples/refsim/params2", 1, 60);
	}

	@Test
	public void testLastValuePure() throws Exception {
		runTest("examples/refsim/lastValue1", 1, 400);
	}

	@Test
	public void testLastValueInsideRisingEdge() throws Exception {
		runTest("examples/refsim/lastValue2", 1, 400);
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

		runTest("examples/plasma", "BuildPathTbench.txt", 19, 10000);
	}
	@Test
	public void testEntityInst() throws Exception {

		runTest("examples/refsim/entityInst", 2, 152);
	}
	@Test
	public void testUnconstrainedInterface() throws Exception {

		runTest("examples/refsim/unconstrainedInterface", 2, 2);
	}

	@Test
	public void testAscending() throws Exception {

		runTest("examples/refsim/ascending", 1, 0);
	}

	@Test
	public void testRead() throws Exception {

		runTest("examples/refsim/textio.read", 1, 0);
	}

	@Test
	public void testFileOpen() throws Exception {

		runTest("examples/refsim/file_open", 1, 160);
	}

	@Test
	public void testFileOpenErrors1() throws Exception {

		// lock file artificially
		File file = new File("examples/refsim/file_open/blocked.txt");
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		FileLock lock = fos.getChannel().lock();


		runTest("examples/refsim/file_open", "BuildPathErrors1.txt", 1, 160);


		lock.release();
		fos.close();
		file.delete();
	}

	@Test (expected = MarkerException.class)
	public void testFileOpenErrors2() throws Exception {

		try {

			runTest("examples/refsim/file_open", "BuildPathErrors2.txt", 1, 0);

		} catch (ZamiaException e) {
			if (e.getMessage().equals("Attempt to access a closed file.")) {
				throw new MarkerException();
			}
		}
	}

	@Test (expected = MarkerException.class)
	public void testFileOpenErrors3() throws Exception {

		try {

			runTest("examples/refsim/file_open", "BuildPathErrors3.txt", 1, 0);

		} catch (ZamiaException e) {
			String msg = e.getMessage();
			if (msg.startsWith("Attempt to read from file \"") && msg.endsWith("\" which is opened only for writing or appending.")) {
				throw new MarkerException();
			}
		}
	}

	@Test (expected = MarkerException.class)
	public void testFileOpenErrors4() throws Exception {

		try {

			runTest("examples/refsim/file_open", "BuildPathErrors4.txt", 1, 0);

		} catch (ZamiaException e) {
			if (e.getMessage().equals("Attempt to access a closed file.")) {
				throw new MarkerException();
			}
		} finally {
			new File("examples/refsim/file_open/blabla_write.txt").delete();
		}
	}

	@Test (expected = MarkerException.class)
	public void testFileOpenErrors5() throws Exception {

		try {

			runTest("examples/refsim/file_open", "BuildPathErrors5.txt", 1, 0);

		} catch (ZamiaException e) {
			String msg = e.getMessage();
			if (msg.startsWith("Attempt to write to or flush file \"") && msg.endsWith("\" which is opened only for reading.")) {
				throw new MarkerException();
			}
		} finally {
			new File("examples/refsim/file_open/blabla_read.txt").delete();
		}
	}

	@Test (expected = MarkerException.class)
	public void testFileOpenErrors6() throws Exception {

		try {

			runTest("examples/refsim/file_open", "BuildPathErrors6.txt", 1, 0);

		} catch (ZamiaException e) {
			if (e.getMessage().equals("Attempt to access a closed file.")) {
				throw new MarkerException();
			}
		}
	}

	@Test
	public void testFileOpenENDF() throws Exception {

		runTest("examples/refsim/file_open", "BuildPathENDF.txt", 1, 0);

		new File("examples/refsim/file_open/blabla_write.txt").delete();
	}

	@Test
	public void testArrayGreater() throws Exception {

		runTest("examples/refsim/arrayGreater", 1, 0);
	}
	@Test
	public void testEradosBug1() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(1);

		runTest("examples/erados", 48, 4070);

		checkSignalValue("LEDS_LD", "00001001");

		cleanErados();
	}
	@Test
	public void testEradosBug2() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(2);

		runTest("examples/erados", 48, 1550);

		checkSignalValue("LEDS_LD", "00000001");

		cleanErados();
	}
	@Test
	public void testEradosBug3() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(3);

		runTest("examples/erados", 48, 13630);

		checkSignalValue("LEDS_LD", "00010111");

		cleanErados();
	}
	@Test
	public void testEradosBug4() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(4);

		runTest("examples/erados", 48, 20050);

		checkSignalValue("LEDS_LD", "11111111");

		cleanErados();
	}
	@Test
	public void testEradosBug5() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(5);

		runTest("examples/erados", 48, 18850);

		checkSignalValue("LEDS_LD", "00011010");

		cleanErados();
	}
	@Test
	public void testEradosBug6() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(6);

		runTest("examples/erados", 48, 4790);

		checkSignalValue("LEDS_LD", "00001011");

		cleanErados();
	}
	@Test
	public void testEradosBug7() throws Exception {

		if (NO_ERADOS) {
			return; // ok, this test is optional
		}

		cleanErados();

		unzipBug(7);

		runTest("examples/erados", 48, 12130);

		checkSignalValue("LEDS_LD", "00010101");

		cleanErados();
	}

	private void checkSignalValue(String signalName, String valueAsString) {

		IGStaticValue value = fSim.getValue(new PathName(signalName));

		assertEquals("Signal " + signalName + " has wrong value.", valueAsString, value.toString());
	}

	private void unzipBug(int bugNr) {

		Map<String, String> buggyFiles = new HashMap<String, String>();
		switch (bugNr) {
			case 1 :
				buggyFiles.put("1case_alu_with_overflow_bug.vhd", "alu.vhd");
				break;
			case 2 :
				buggyFiles.put("2case_alu_with_compare_instruction_bug.vhd", "alu.vhd");
				break;
			case 3 :
				buggyFiles.put("3case_state_machine_with_operand_fetch_bug.vhd", "state_machine.vhd");
				break;
			case 4 :
				buggyFiles.put("4case_interrupt_mod_irq_bug.vhd", "interrupt_mod.vhd");
				break;
			case 5 :
				buggyFiles.put("5case_sfrs_mod_interrupt_mask_bug.vhd", "sfrs_mod.vhd");
				break;
			case 6 :
				buggyFiles.put("6case_jump_target_return_bug.vhd", "jump_target.vhd");
				break;
			case 7 :
				buggyFiles.put("7case_gprs_mod_reg_bug.vhd", "gprs_mod.vhd");
				break;
		}

		if (!buggyFiles.isEmpty()) {
			unzip(new File("examples/erados/src/processor/buggy_files.zip"), buggyFiles);
		}
	}

	private void cleanErados() {
		unzip(new File("examples/erados/src/processor/correct_files.zip"));
	}

	private class MarkerException extends Exception {
	}
}
