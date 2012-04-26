package org.zamia.instgraph;

import java.io.File;

import org.apache.log4j.Level;
import org.junit.Test;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.vhdl.ast.DMUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class IGManagerTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("IGManager Test Tmp Project", aBasePath, sf);
		fZPrj.clean();
	}

	private DMUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, int aNumObjects) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumObjects);
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumObjects) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = getUID(fZPrj);

		int n = fZPrj.getERM().getNumErrors();
		logger.error("IGManagerTest: Build finished. Found %d errors.", n);

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("IGManagerTest: error %6d/%6d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		n = fZPrj.getIGM().countObjects(duuid);
		logger.info("IGManagerTest: elaborated model for %s has %d unique objects.", duuid, n);
		assertEquals("ObjectCounter for " + aTestDir, aNumObjects, n);

	}

	@Test
	public void testCountObjectsAdd4() throws Exception {
		runTest("examples/add4", 20);
	}
	@Test
	public void testCountObjectsGCD() throws Exception {
		runTest("examples/gcd", 8 + 2 + 1 + 3 + 1); // 15
	}
	@Test
	public void testCountObjectsErados() throws Exception {
		runTest("examples/erados", 7 + 1 + 18 + 7 + 18 + 26 + 6 + 8 + 2 + 2 + 4 + 11 + 6 + 13 + 12 + 6 + 19 + 1 + 1 + 13 + 1); // 182
	}
	@Test
	public void testCountObjectsLeonSOC() throws Exception {
		runTest("examples/leonSOC", 3855);
	}
}
