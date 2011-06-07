/* 
 * Copyright 2006-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.analysis.ig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;
import org.zamia.ERManager;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.analysis.SourceLocation2IG;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.util.Pair;
import org.zamia.vg.VGGCSVG;
import org.zamia.vg.VGLayout;


/**
 * IG-based Static analysis code test cases.
 *
 * @author Guenter Bartsch
 */
public class IGSATest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		// ZamiaLogger.setup(Level.INFO);

		SourceFile sf = new SourceFile(new File(aBuildPath));

		fZPrj = new ZamiaProject("SA Test Tmp Project", aBasePath, sf);
		fZPrj.clean();
		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		ERManager erm = fZPrj.getERM();

		assertEquals(0, erm.getNumErrors());

	}

	private IGRSResult runIGReferenceSearch(SourceFile aSF, String aTLPath, int aLine, int aCol, boolean aUpwards, boolean aDownwards) throws ZamiaException, IOException {
		ToplevelPath tlp = new ToplevelPath(aTLPath);

		SourceLocation location = new SourceLocation(aSF, aLine, aCol);

		Pair<IGItem, ToplevelPath> res = SourceLocation2IG.findNearestItem(location, tlp, fZPrj);

		assertNotNull("Failed to find nearest IG Item", res);

		IGItem item = res.getFirst();
		assertNotNull("Failed to find nearest IG Item", item);
		ToplevelPath path = res.getSecond();

		logger.info("SATest: nearest item: %s, path: %s", item, path);

		if (item != null) {

			IGReferencesSearchNG rs = new IGReferencesSearchNG(fZPrj);

			IGObject object = null;

			if (item instanceof IGObject) {
				object = (IGObject) item;
			} else if (item instanceof IGOperationObject) {
				object = ((IGOperationObject) item).getObject();
			} else if ((item instanceof IGInstantiation) || (item instanceof IGOperationLiteral)) {
				// not supported, ok.
				return null;
			} else {
				fail("Unknown item class: " + item);
			}

			IGRSResult result = rs.search(object, path, true, true);

			/*
			 * visualize result (i.e. generate SVG file)
			 */
			
			IGRSVisualGraphContentProvider contentProvider = new IGRSVisualGraphContentProvider(result);

			IGRSVisualGraphLabelProvider labelProvider = new IGRSVisualGraphLabelProvider(result);

			IGRSVisualGraphSelectionProvider selectionProvider = new IGRSVisualGraphSelectionProvider();

			String svgFileName = fZPrj.getDataPath() + File.separator + tlp.getToplevel().getDUUID().getUID() + ".svg";

			logger.info("IGSynthTest: SVG file name: %s", svgFileName);

			PrintWriter out = null;

			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(svgFileName)));

				VGGCSVG gc = new VGGCSVG(out);

				VGLayout<IGRSNode, IGRSPort, IGRSSignal> layout = new VGLayout<IGRSNode, IGRSPort, IGRSSignal>(contentProvider, labelProvider, gc);

				layout.paint(selectionProvider);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}

			String dotFileName = fZPrj.getDataPath() + File.separator + tlp.getToplevel().getDUUID().getUID() + ".dot";

			logger.info("IGSynthTest: DOT file name: %s", dotFileName);

			out = null;

			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(dotFileName)));

				IGRS2DOT gen = new IGRS2DOT(result);
				gen.convert(out);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}


			return result;

		} else {
			fail("Item not found.");
		}

		return null;
	}

	@Test
	public void testGCounterIGReferenceSearch() throws Exception {
		setupTest("examples/gcounter", "examples/gcounter/BuildPath.txt");

		SourceFile sf = new SourceFile(new File("examples/gcounter/addg.vhdl"));

		IGRSResult result = runIGReferenceSearch(sf, "COUNTER_TB:COUNTER0.ADDG", 22, 35, true, true);
		assertNotNull(result);
		result.dump(0, System.out);
		assertEquals(30, result.countNodes());
		assertEquals(47, result.countConns());

		SourceFile sf2 = new SourceFile(new File("examples/gcounter/ha.vhdl"));
		result = runIGReferenceSearch(sf2, "COUNTER_TB:COUNTER0.ADDG.GEN1#3.VAI.HA2", 14, 3, true, true);
		assertNotNull(result);
		result.dump(0, System.out);
		assertEquals(8, result.countNodes());
		assertEquals(7, result.countConns());

		result = runIGReferenceSearch(sf, "WORK.COUNTER_TB:COUNTER0.ADDG.GEN1#0.VAI", 22, 64, true, true);
		assertNotNull(result);
		result.dump(0, System.out);
		assertEquals(37, result.countNodes());
		assertEquals(69, result.countConns());

		//		DUUID duuid = new DUUID(LUType.Architecture, "WORK", "COUNTER_TB", "RTL");
		//		stressTestIGReferenceSearch(duuid, "COUNTER_TB:");
	}

	//	public void testGCounterIGReferenceSearchIncr() throws Exception {
	//		setupTest("test/gcounter", "test/gcounter/BuildPath.txt");
	//
	//		SourceFile sf = new SourceFile(new File("test/gcounter/va.vhdl"));
	//
	//		ReferenceSearchResult result = runIGReferenceSearch(sf, "WORK.COUNTER_TB:COUNTER0.ADDG.GEN1#0.VAI.HA1", 18, 33, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(6, result.countRefs());
	//
	//		// now, do an incremental rebuild of the design and run the reference search again
	//
	//		HashSetArray<SourceFile> changed = new HashSetArray<SourceFile>(2);
	//		changed.add(sf);
	//
	//		ZamiaProjectBuilder builder = fZPrj.getBuilder();
	//
	//		int n = builder.build(false, false, changed);
	//		assertEquals(2, n);
	//
	//		result = runIGReferenceSearch(sf, "WORK.COUNTER_TB:COUNTER0.ADDG.GEN1#0.VAI.HA1", 18, 33, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(6, result.countRefs());
	//	}
	//
	//	public void testLeonIGReferenceSearch() throws Exception {
	//		setupTest("text/leonExtern", "test/leonExtern/BuildPath.txt");
	//
	//		String homePath = System.getProperty("user.home");
	//
	//		SourceFile sf = new SourceFile(new File(homePath + File.separator + "projects/workspace/zamia/examples/leonSOC/lib/gaisler/greth/grethm.vhd"));
	//
	//		ReferenceSearchResult result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.M100", 84, 22, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(905, result.countRefs());
	//
	//		result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.M100", 84, 17, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(103, result.countRefs());
	//
	//		result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.", 84, 55, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(83, result.countRefs());
	//	}
	//
	//	public void testPlasmaIGReferenceSearch() throws Exception {
	//		setupTest("examples/plasma", "examples/plasma/BuildPath.txt");
	//
	//		SourceFile sf = new SourceFile(new File("examples/plasma/uart.vhd"));
	//
	//		/*
	//		 * 3,7    M1.M2.P1  => S          M1.M2.S
	//		 * 5,9    M1.M2     => S          M1.M2.P1
	//		 * 7,14   M1.M2.P1  => S          M1.M2.
	//		 * 7,14   M1.M2     => S          M1.M2.
	//		*/
	//
	//		runIGItemSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE.UARTOP", 80, 14, "WORK.PLASMA:U3_UART.UART_MODULE.RIN");
	//		runIGItemSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE", 298, 6, "WORK.PLASMA:U3_UART.UART_MODULE.UARTOP");
	//		runIGItemSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE.UARTOP", 316, 13, "WORK.PLASMA:U3_UART.UART_MODULE.");
	//		runIGItemSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE", 316, 13, "WORK.PLASMA:U3_UART.UART_MODULE.");
	//		runIGItemSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE.", 316, 13, "WORK.PLASMA:U3_UART.UART_MODULE.");
	//
	//		ReferenceSearchResult result = runIGReferenceSearch(sf, "WORK.PLASMA:U3_UART.UART_MODULE.UARTOP.BRATE", 80, 14, true, true);
	//		assertNotNull(result);
	//		result.dump(0, System.out);
	//		assertEquals(3, result.countRefs());
	//	}
	//
	//	private void runIGItemSearch(SourceFile aSF, String aTLPath, int aLine, int aCol, String aExpectedPath) throws ZamiaException, IOException {
	//
	//		ToplevelPath tlp = new ToplevelPath(aTLPath);
	//
	//		SourceLocation location = new SourceLocation(aSF, aLine, aCol);
	//
	//		Pair<IGItem, ToplevelPath> res = SourceLocation2IG.findNearestItem(location, tlp, fZPrj);
	//
	//		assertNotNull("Failed to find nearest IG Item", res);
	//
	//		IGItem item = res.getFirst();
	//		assertNotNull("Failed to find nearest IG Item", item);
	//
	//		ToplevelPath path = res.getSecond();
	//
	//		assertEquals(aExpectedPath, path.toString());
	//	}
	//
	//	private void stressTestIGReferenceSearch(DUUID aDUUID, String aPath) throws Exception {
	//		// do a real stress test - global search on all names
	//
	//		DUManager dum = fZPrj.getDUM();
	//
	//		DesignUnit du = dum.getDU(aDUUID);
	//
	//		ZStack<ASTObject> stack = new ZStack<ASTObject>();
	//
	//		stack.push(du);
	//		while (!stack.isEmpty()) {
	//			ASTObject obj = stack.pop();
	//
	//			if (obj == null) {
	//				continue;
	//			}
	//
	//			if (obj instanceof Name) {
	//
	//				SourceLocation location = obj.getLocation();
	//
	//				logger.info("SATest: Searching for %s at %s...", obj, location);
	//
	//				ReferenceSearchResult result = runIGReferenceSearch(location.fSF, aPath, location.fLine, location.fCol, true, true);
	//				// no asserts - we're searching blindly for all identifiers
	//				// assertNotNull (result);
	//				if (result != null) {
	//					result.dump(0, System.out);
	//				}
	//			}
	//
	//			int n = obj.getNumChildren();
	//			for (int i = 0; i < n; i++) {
	//				ASTObject child = obj.getChild(i);
	//
	//				stack.push(child);
	//			}
	//		}
	//
	//	}
	//
	//	@Override
	//	protected void tearDown() {
	//		if (fZPrj != null) {
	//			fZPrj.shutdown();
	//			fZPrj = null;
	//		}
	//	}
	//
	//	private static final int BENCHMARK_ITERATIONS = 10;
	//
	//	private void runBenchmark() throws Exception {
	//
	//		setupTest("text/leonExtern", "test/leonExtern/BuildPath.txt");
	//
	//		SourceFile sf = new SourceFile(new File("examples/leonSOC/lib/gaisler/greth/grethm.vhd"));
	//
	//		long startTime = System.currentTimeMillis();
	//
	//		for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
	//			ReferenceSearchResult result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.M100", 84, 22, true, true);
	//			assertNotNull(result);
	//			result.dump(0, System.out);
	//			assertEquals(905, result.countRefs());
	//
	//			result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.M100", 84, 17, true, true);
	//			assertNotNull(result);
	//			result.dump(0, System.out);
	//			assertEquals(103, result.countRefs());
	//
	//			result = runIGReferenceSearch(sf, "WORK.LEON3MP(RTL):ETH0.E1.", 84, 55, true, true);
	//			assertNotNull(result);
	//			result.dump(0, System.out);
	//			assertEquals(83, result.countRefs());
	//		}
	//
	//		long stopTime = System.currentTimeMillis();
	//
	//		double t = ((double) stopTime - startTime) / 1000.0;
	//
	//		logger.info("\n\ntotal benchmark time: %5.2fs", t);
	//	}
	//
	//	public static void main(String args[]) {
	//
	//		IGSATest sat = new IGSATest();
	//
	//		try {
	//			sat.runBenchmark();
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
}
