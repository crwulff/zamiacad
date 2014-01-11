package org.zamia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.ref.IGSimRef;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;

/**
 * @author valentin
 *
 */
public class BasicTest {
	
		// If we need capturing console, http://stackoverflow.com/questions/2169330
	public static class CaputreLogMessagesContaining extends AppenderSkeleton {
		
		{
			ZamiaLogger.getInstance().getLogger().addAppender(this);
		}
		
		public void startCapturing(String[] pieces) {
			toCaputre.addAll(Arrays.asList(pieces));
		}
		public List<String> toCaputre = new LinkedList<>();
		protected void append(LoggingEvent arg0) {
			String msg = arg0.getMessage().toString().toUpperCase();
			
			for (ListIterator<String> li = toCaputre.listIterator(); li.hasNext() ; ) {
				String piece = li.next().toUpperCase();
				System.out.println(msg);
				if (msg.contains(piece))
					li.remove();
			}
		}
		public void close() {} public boolean requiresLayout() {return false;}
		
	};
	
	protected CaputreLogMessagesContaining expectedLogMessages;
	
	@Before
	public void setUp() {
		expectedLogMessages = new CaputreLogMessagesContaining();
	}
	public final static ZamiaLogger logger = ZamiaLogger.getInstance();
	
	/**Default implementation expects 0 exceptions.*/
	public class ErrorChecker {
		
		protected void assertContains(int i, String expected) {
			String msg = fZPrj.getERM().getError(i).toString();
			assertTrue(msg + "\n\t ^^^ is the error("+i+") message that failed to contain the following string vvv\n"+expected, msg.contains(expected));
		}
		
		public void handle() throws ZamiaException {
			int nErr = fZPrj.getERM().getNumErrors();
			logger.error("IGTest: Build finished. Found %d errors.", nErr);

			for (int i = 0; i < nErr; i++) {
				ZamiaException em = fZPrj.getERM().getError(i);
				logger.error("IGTest: error %6d/%6d: %s", i + 1, nErr, em.toString());
			}

			if (nErr == 1)
				throw fZPrj.getERM().getError(0);
			else
				assertEquals("No errors expected", 0, nErr);
		}
	}
	
	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("IG Test Tmp Project", aBasePath, sf, null);
		fZPrj.clean();
	}
	
	public ZamiaProject fZPrj;
	private IGSimRef fSim;
	private final static BigInteger NANO_FACTOR = new BigInteger("1000000");
	

	private void checkSignalValue(String signalName, String valueAsString) {

		IGStaticValue value = fSim.getValue(new PathName(signalName));

		assertEquals("Signal " + signalName + " has wrong value.", valueAsString, value.toString());
	}

	
	protected void runTest(String aTestDir, String aBuildPathName, int aNumNodes, ErrorChecker errChecker) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		errChecker.handle();
		
		BuildPath bp = fZPrj.getBuildPath();
		int count = 0;
		Collection<String> toplevels = new ArrayList<>(bp.getNumToplevels());
		for (Toplevel tl : bp.toplevels()) {
			DMUID duuid = fZPrj.getDUM().getArchDUUID(tl.getDUUID());
			count += fZPrj.getIGM().countNodes(duuid);
		}
		
		logger.info("IGTest: elaborated model for (%s) has %d unique modules.", Utils.concatenate(toplevels), count);
		assertEquals("Num of nodes", aNumNodes, count);
	}

	protected void runTest(String aTestDir, String aBuildPathName, int aNumNodes, ErrorChecker errChecker, int aNanos) throws Exception {
		runTest(aTestDir, aBuildPathName, aNumNodes, errChecker);
		
		fSim = new IGSimRef();

		BuildPath bp = fZPrj.getBuildPath();
		for (Toplevel tl : bp.toplevels()) {
			ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));
			fSim.open(tlp, null, null, fZPrj);
			fSim.reset();
			fSim.run(new BigInteger("" + aNanos).multiply(NANO_FACTOR));
		}
	}
	
	public void runTest(String aTestDir, String aBuildPathName, int aNumNodes, int aNanos) throws Exception {
		runTest(aTestDir, aBuildPathName, aNumNodes, new ErrorChecker(), aNanos);

	}
	
	public void runTest(String aTestDir, String aBuildPathName, int aNumNodes) throws Exception {
		runTest(aTestDir, aBuildPathName, aNumNodes, new ErrorChecker());
	}
	
	public void runTest(String aTestDir, int aNumNodes, int aNanos) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes, aNanos);
	}
	
	protected void runTest(String aTestDir, int aNumNodes) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes);
	}

	@After
	public void tearDown() {
		
		if (fZPrj != null) {
			// fZPrj.getZDB().dump();
			fZPrj.shutdown();
			fZPrj = null;
		}
		
		ZamiaLogger.getInstance().getLogger().removeAppender(expectedLogMessages);

		//It seems to be a bad idea to raise errors here but 
		// it is very convenient.
		for (String str : expectedLogMessages.toCaputre)
			assertTrue("log fails to contain \"" + str + "\"", false);

	}

	
}
