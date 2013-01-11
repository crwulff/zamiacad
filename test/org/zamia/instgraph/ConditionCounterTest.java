package org.zamia.instgraph;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;
import org.zamia.*;
import org.zamia.vhdl.ast.DMUID;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class ConditionCounterTest {

    public final static ZamiaLogger logger = ZamiaLogger.getInstance();

    private final static boolean NO_ROBSY = !(new File("examples/robsy/BuildPath.txt").exists());

    private ZamiaProject fZPrj;

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

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void runTest(String aTestDir, String aBuildPathName, int aNumConditions) throws Exception {
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

        int cond = fZPrj.getIGM().countConditions(duuid);
        logger.info("IGTest: elaborated model for %s has %d conditions.", duuid, n);
        assertEquals(aNumConditions, cond);
    }

    @After
    public void tearDown() throws Exception {
        if (fZPrj != null) {
            fZPrj.shutdown();
            fZPrj = null;
        }
    }

    @Test
    public void testb04() throws Exception {
        runTest("examples/b04", "BuildPath.txt", 12);
    }

    @Test
    public void testGCD() throws Exception {
        runTest("examples/gcd", "BuildPath.txt", 20);
    }

    @Test
    public void testRobsyAlu() throws Exception {
        if (NO_ROBSY) {
            return;
        }
        runTest("examples/robsy", "BuildPathAlu.txt", 357);
    }

    @Test
    public void testRobsy() throws Exception {
        if (NO_ROBSY) {
            return;
        }
        runTest("examples/robsy", "BuildPath.txt", 939);
    }

    @Test
    public void testRobsyRange() throws Exception {
        if (NO_ROBSY) {
            return;
        }
        runTest("examples/robsy", "BuildPath.txt", 939);

        int numRange = fZPrj.getIGM().countConditionsInRange(getUID(), "src/processor/stack_interface_mod.vhd", 77, 96);
        assertEquals(18, numRange);
    }
}
