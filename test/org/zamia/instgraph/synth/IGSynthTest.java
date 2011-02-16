/* 
 * Copyright 2009,2010 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.synth;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLVisualGraphContentProvider;
import org.zamia.rtl.RTLVisualGraphLabelProvider;
import org.zamia.rtl.RTLValue.BitValue;
import org.zamia.rtl.sim.RTLSimulator;
import org.zamia.util.PathName;
import org.zamia.vg.VGGCSVG;
import org.zamia.vg.VGLayout;
import org.zamia.vhdl.ast.DMUID;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Guenter Bartsch
 */
public class IGSynthTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

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

	private RTLModule runSynth(String aTestDir, String aBuildPathName, String aSynthUID) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID dmuid = getUID(fZPrj);

		int n = fZPrj.getERM().getNumErrors();
		logger.error("IGSynthTest: Build finished. Found %d errors.", n);

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("IGSynthTest: error %6d/%6d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		IGManager igm = fZPrj.getIGM();

		n = igm.countNodes(dmuid);
		logger.info("IGSynthTest: elaborated model for %s has %d unique modules.", dmuid, n);

		dmuid = DMUID.parse(aSynthUID);

		Toplevel tl = new Toplevel(dmuid, null);

		IGModule igmodule = igm.findModule(tl);

		IGSynth synth = new IGSynth(fZPrj);

		RTLModule rtlm = synth.synthesize(igmodule);

		n = rtlm.getNumNodes();

		logger.info("IGSynthTest: synthesized RTL graph for %s has %d nodes.", dmuid, n);

		rtlm.dump(System.out);

		RTLVisualGraphContentProvider contentProvider = new RTLVisualGraphContentProvider(rtlm);

		RTLVisualGraphLabelProvider labelProvider = new RTLVisualGraphLabelProvider(rtlm);

		String svgFileName = fZPrj.getDataPath() + File.separator + aSynthUID + ".svg";

		logger.info("IGSynthTest: SVG file name: %s", svgFileName);

		PrintWriter out = null;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(svgFileName)));

			VGGCSVG gc = new VGGCSVG(out);

			VGLayout<RTLNode, RTLPort, RTLSignal> layout = new VGLayout<RTLNode, RTLPort, RTLSignal>(contentProvider, labelProvider, gc);

			layout.paint();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

		// test dynamic mode:

		contentProvider.setDynamicMode(true);

		out = null;

		VGLayout<RTLNode, RTLPort, RTLSignal> layout = null;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(svgFileName)));

			VGGCSVG gc = new VGGCSVG(out);

			layout = new VGLayout<RTLNode, RTLPort, RTLSignal>(contentProvider, labelProvider, gc);

			layout.paint();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

		// expand all initially expandable ports:

		n = layout.getNumExpandablePorts();

		for (int i = 0; i < n; i++) {
			RTLPort p = layout.getExpandablePort(i);

			contentProvider.expandPort(p);

		}

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(svgFileName)));

			VGGCSVG gc = new VGGCSVG(out);

			layout = new VGLayout<RTLNode, RTLPort, RTLSignal>(contentProvider, labelProvider, gc);

			layout.paint();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

		return rtlm;
	}

	private RTLModule runSynth(String aTestDir, String aSynthUID) throws Exception {
		return runSynth(aTestDir, "BuildPath.txt", aSynthUID);
	}

	@Test
	public void testLatch() throws Exception {

		RTLModule rtlm = runSynth("examples/synth/latchTest", "WORK.FOO(RTL)");

		RTLSimulator sim = new RTLSimulator(fZPrj);

		sim.open(rtlm);

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("C1"), "0");
		sim.assign(new PathName("C2"), "0");

		sim.simulate();

		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("C1"), "0");
		sim.assign(new PathName("C2"), "0");

		sim.simulate();

		assertEquals(BitValue.BV_1, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("C1"), "1");
		sim.assign(new PathName("C2"), "1");

		sim.simulate();

		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("C1"), "1");
		sim.assign(new PathName("C2"), "1");

		sim.simulate();

		assertEquals(BitValue.BV_1, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("C1"), "1");
		sim.assign(new PathName("C2"), "1");

		sim.simulate();

		assertEquals(BitValue.BV_1, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("C1"), "1");
		sim.assign(new PathName("C2"), "1");

		sim.simulate();

		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

	}

	@Test
	public void testClock2() throws Exception {

		RTLModule rtlm = runSynth("examples/synth/clock2Test", "WORK.FOO(RTL)");

		RTLSimulator sim = new RTLSimulator(fZPrj);

		sim.open(rtlm);

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("C"), "0");
		sim.assign(new PathName("N"), "0");
		sim.assign(new PathName("R"), "1");

		sim.simulate();

		sim.assign(new PathName("C"), "1");
		sim.simulate();
		sim.assign(new PathName("C"), "0");
		sim.simulate();

		RTLValue vz = sim.getCurrentValue(new PathName("Z"));
		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("R"), "0");
		sim.simulate();
		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("N"), "1");
		sim.simulate();
		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("C"), "1");
		sim.simulate();
		sim.assign(new PathName("C"), "0");
		sim.simulate();

		assertEquals(BitValue.BV_1, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("A"), "0");
		sim.simulate();
		assertEquals(BitValue.BV_1, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("C"), "1");
		sim.simulate();
		sim.assign(new PathName("C"), "0");
		sim.simulate();

		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());

		sim.assign(new PathName("N"), "0");
		sim.assign(new PathName("A"), "1");

		sim.assign(new PathName("C"), "1");
		sim.simulate();
		sim.assign(new PathName("C"), "0");
		sim.simulate();

		assertEquals(BitValue.BV_0, sim.getCurrentValue(new PathName("Z")).getBit());
	}

	@Test
	public void testClock1() throws Exception {

		RTLModule rtlm = runSynth("examples/synth/clock1Test", "WORK.FOO(RTL)");

		RTLSimulator sim = new RTLSimulator(fZPrj);

		sim.open(rtlm);

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		sim.assign(new PathName("CLK"), "1");
		sim.simulate();
		sim.assign(new PathName("CLK"), "0");
		sim.simulate();

		RTLValue vz = sim.getCurrentValue(new PathName("Z"));
		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));
		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("B"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

	}

	@Test
	@Ignore
	public void testArrayIdx() throws Exception {

		RTLModule rtlm = runSynth("examples/synth/arrayIdxTest", "WORK.FOO(RTL)");

		RTLSimulator sim = new RTLSimulator(fZPrj);

		sim.open(rtlm);

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		sim.assign(new PathName("CLK"), "1");
		sim.simulate();
		sim.assign(new PathName("CLK"), "0");
		sim.simulate();

		RTLValue vz = sim.getCurrentValue(new PathName("Z"));
		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));
		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "0");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");
		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("B"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("CLK"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("CLK"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

	}

	@Test
	@Ignore
	public void testCombProc() throws Exception {

		RTLModule rtlm = runSynth("examples/synth/combProcTest", "WORK.FOO(RTL)");

		RTLSimulator sim = new RTLSimulator(fZPrj);

		sim.open(rtlm);

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "0");

		sim.simulate();

		RTLValue vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "0");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("A"), "0");
		sim.assign(new PathName("B"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_1, vz.getBit());

		sim.assign(new PathName("A"), "1");
		sim.assign(new PathName("B"), "1");

		sim.simulate();

		vz = sim.getCurrentValue(new PathName("Z"));

		assertEquals(BitValue.BV_0, vz.getBit());

	}

	@Test
	@Ignore
	public void testPlasmaAlu() throws Exception {

		runSynth("examples/plasma", "WORK.ALU(LOGIC)");
	}

	@After
	public void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}

}
