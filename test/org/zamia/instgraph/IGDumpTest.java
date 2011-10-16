/* 
 * Copyright 2009,2010,2011 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Test;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.vhdl.ast.AST2DOT;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.DMUID;

/**
 * @author Guenter Bartsch
 */
public class IGDumpTest {

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

	private Toplevel getTL(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();
		return bp.getToplevel(0);
	}

	private DMUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	private void runTest(String aTestDir, String aBuildPathName, int aNumNodes, File aASTDotFile, File aIGDotFile) throws Exception {
		setupTest(aTestDir, aTestDir + File.separator + aBuildPathName);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		DMUID duuid = getUID(fZPrj);

		int n = fZPrj.getERM().getNumErrors();
		logger.error("IGDumpTest: Build finished. Found %d errors.", n);

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("IGDumpTest: error %6d/%6d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		if (n > 0) {
			n = fZPrj.getIGM().countNodes(duuid);
			logger.info("IGDumpTest: elaborated model for %s has %d unique modules.", duuid, n);
			assertEquals(aNumNodes, n);
		}

		Toplevel tl = getTL(fZPrj);

		Architecture arch = fZPrj.getDUM().getArchitecture(tl.getDUUID().getLibId(), tl.getDUUID().getId());

		if (arch != null && aASTDotFile != null) {
			AST2DOT dot = new AST2DOT(arch, fZPrj.getZDB());

			dot.blacklistField("fParent");
			dot.blacklistField("fSource");
			dot.blacklistField("fStartCol");
			dot.blacklistField("fStartLine");
			dot.blacklistField("fEndCol");
			dot.blacklistField("fEndLine");
			dot.blacklistField("fDeclarationMap");

			PrintWriter out = null;
			try {

				out = new PrintWriter(new BufferedWriter(new FileWriter(aASTDotFile)));

				dot.convert(out);

				logger.info("IGDumpTest: wrote dot file to %s", aASTDotFile);

			} catch (IOException e) {
				e.printStackTrace();

			} finally {
				if (out != null) {
					out.close();
				}
			}

		}

		IGModule module = fZPrj.getIGM().findModule(tl);
		if (module != null) {

			if (aIGDotFile == null) {
				module.dump(23);
			} else {

				IG2DOT dot = new IG2DOT(module);

				dot.blacklistField("fImportedLibs");
				dot.blacklistField("fImportedPackages");
				dot.blacklistField("fZPrjID");
				dot.blacklistField("fSFDBID");
				dot.blacklistField("fLine");
				dot.blacklistField("fCol");
				dot.blacklistField("fScore");
				dot.blacklistField("fFailed");
				dot.blacklistField("fReject");
				dot.blacklistField("fInertial");
				dot.blacklistField("fDelay");

				PrintWriter out = null;
				try {

					out = new PrintWriter(new BufferedWriter(new FileWriter(aIGDotFile)));

					dot.convert(out);

					logger.info("IGDumpTest: wrote dot file to %s", aIGDotFile);

				} catch (IOException e) {
					e.printStackTrace();

				} finally {
					if (out != null) {
						out.close();
					}
				}
			}
		}
	}

	private void runTest(String aTestDir, int aNumNodes, File aASTDotFile, File aIGDotFile) throws Exception {
		runTest(aTestDir, "BuildPath.txt", aNumNodes, aASTDotFile, aIGDotFile);
	}

	@After
	public void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}

	@Test
	public void testDotLiterals2() throws Exception {

		runTest("examples/semantic/literal2Test", 1, new File("/tmp/ast.dot"), new File("/tmp/ig.dot"));
	}

	@Test
	public void testLiterals2() throws Exception {

		runTest("examples/semantic/literal2Test", 1, null, null);
	}

	public static void main(String args[]) {

		if (args.length != 3) {
			System.out.println("Args: " + args.length);

			System.err.println("usage: ig2dot <projectdir> <ast.dot> <ig.dot>");
			System.exit(1);
		}

		String prjDir = args[0];
		File astDotFile = new File(args[1]);
		File igDotFile = new File(args[2]);

		IGDumpTest igt = new IGDumpTest();
		try {

			igt.runTest(prjDir, -1, astDotFile, igDotFile);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
