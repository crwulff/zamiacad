/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2008
 */
package org.zamia.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.ZamiaProject.VHDLLanguageSupport;
import org.zamia.instgraph.IGManager;
import org.zamia.util.ZamiaTmpDir;
import org.zamia.vhdl.ast.DUUID;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class FSCacheTest extends TestCase {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean enableLeonOfflineTest = true;

	public final static boolean enableErrorRecoveryTest = true;

	public final static boolean enableDirCacheTest = true;

	private FSCache fFSCache;

	private String fNewDir1;

	private String fTmpDirStr;

	private String fNewDir2;

	private String[] fNewFiles;

	private void deleteDirRek(File aDir) {

		File files[] = aDir.listFiles();

		if (files != null) {

			int n = files.length;
			for (int i = 0; i < n; i++) {
				deleteDirRek(files[i]);
			}
		}

		if (!aDir.delete()) {
			fail("Coudln't delete '" + aDir + "'");
		}

	}

	public void testDirCacheErrorRecovery() throws Exception {

		if (!enableErrorRecoveryTest)
			return;

		/*
		 * Idea behind this test: DirCache should be able to recover
		 * from errors caused by IOExceptions thrown while it tries listing
		 * directories (e.g. to determine file existence, directories ...)
		 * 
		 * to test this, we create this directory/file structure
		 * 
		 * aDir/
		 * aDir/bDir/
		 * aDir/bDir/aFile
		 * 
		 * now, we block bDir read permissions for the current user and call
		 * 
		 * FSCache.list("aDir/bDir")
		 * 
		 * which will result in an IOException thrown so we get a null result.
		 * 
		 * Then, we enable bDir read permissions and call FSCahce.list() again
		 * which should now return the correct response.
		 * 
		 */

		fFSCache = FSCache.getInstance();

		// we need a directory that doesn't exist (don't want to destroy any data on disk)

		File baseDir = ZamiaTmpDir.getTmpDir();

		File tmpDir = null;
		int cnt = 0;
		do {
			tmpDir = new File(baseDir.getAbsolutePath() + File.separator + "FSCacheTest" + cnt);
			cnt++;
		} while (tmpDir.exists());

		fTmpDirStr = tmpDir.getAbsolutePath();

		fNewDir1 = fTmpDirStr + File.separator + "dir1";
		fNewDir2 = fNewDir1 + File.separator + "dir2";

		String newFile = fNewDir2 + File.separator + "hello.txt";

		createDummyDir(fTmpDirStr);
		createDummyDir(fNewDir1);
		createDummyDir(fNewDir2);

		createDummyFile(newFile);

		File f = new File(fNewDir2);

		f.setReadable(false);

		checkList(f.getAbsolutePath(), false);

		f.setReadable(true);

		checkList(f.getAbsolutePath(), true);

		deleteDirRek(tmpDir);
	}

	public void testDirCache() throws Exception {

		if (!enableDirCacheTest)
			return;

		fFSCache = FSCache.getInstance();

		// we need a directory that doesn't exist (don't want to destroy any data on disk)

		File baseDir = ZamiaTmpDir.getTmpDir();

		File tmpDir = null;
		int cnt = 0;
		do {
			tmpDir = new File(baseDir.getAbsolutePath() + File.separator + "FSCacheTest" + cnt);
			cnt++;
		} while (tmpDir.exists());

		fTmpDirStr = tmpDir.getAbsolutePath();

		fNewDir1 = fTmpDirStr + File.separator + "dir1";
		fNewDir2 = fTmpDirStr + File.separator + "dir2";

		fNewFiles = new String[6];

		fNewFiles[0] = fNewDir1 + File.separator + "file1";
		fNewFiles[1] = fNewDir1 + File.separator + "file2";
		fNewFiles[2] = fNewDir2 + File.separator + "file1";
		fNewFiles[3] = fNewDir2 + File.separator + "file2";
		fNewFiles[4] = fTmpDirStr + File.separator + "file1";
		fNewFiles[5] = fTmpDirStr + File.separator + "file2";

		fFSCache.invalidate(fTmpDirStr);

		checkCache(false);
		checkCache(false);
		checkCache(false);

		createDummyFiles();

		fFSCache.invalidate(fTmpDirStr);

		checkCache(true);

		checkCache(true);

		deleteDirRek(tmpDir);

		checkCache(true);
		checkCache(true);

		fFSCache.invalidate(fTmpDirStr);
		checkCache(false);

	}

	private void checkList(String aPath, boolean aShouldWork) {

		String[] files = fFSCache.list(aPath, true);

		assertEquals(aShouldWork, files != null);
	}

	private void checkDir(String aDirStr, boolean aShouldWork) throws Exception {
		assertEquals(aShouldWork, fFSCache.isDirectory(aDirStr, true));
		assertEquals(aShouldWork, fFSCache.exists(aDirStr, true));
	}

	private void checkFile(String aFileName, boolean aShouldWork) throws Exception {
		assertEquals(aShouldWork, fFSCache.exists(aFileName, true));
	}

	private void checkCache(boolean aShouldWork) throws Exception {

		checkDir(fTmpDirStr, aShouldWork);
		checkDir(fNewDir1, aShouldWork);
		checkDir(fNewDir2, aShouldWork);

		for (int i = 0; i < fNewFiles.length; i++) {
			checkFile(fNewFiles[i], aShouldWork);
		}
	}

	private void createDummyFile(String aFileName) throws Exception {
		BufferedWriter w = new BufferedWriter(new FileWriter(aFileName));

		w.append("Hello, World!");

		w.close();

	}

	private void createDummyDir(String aFileName) throws Exception {
		File newDir = new File(aFileName);
		if (!newDir.mkdir()) {
			fail("Couldn't create '" + newDir.getAbsolutePath() + "'");
		}
	}

	private void createDummyFiles() throws Exception {

		File tmpDir = new File(fTmpDirStr);
		if (!tmpDir.mkdirs()) {
			fail("Couldn't create dir '" + tmpDir.getAbsolutePath() + "'");
		}

		// two dirs

		createDummyDir(fNewDir1);
		createDummyDir(fNewDir2);

		// fill with dummy files

		for (int i = 0; i < fNewFiles.length; i++) {
			createDummyFile(fNewFiles[i]);
		}

	}

	/*************************************************
	 * 
	 * Big offline build test:
	 * 
	 * build leon once, then move the sources away and build it again solely
	 * based on cached stat/file data
	 * 
	 * 
	 */

	private static String tmpDir = ZamiaTmpDir.getTmpDir().getAbsolutePath() + File.separator + "zamia-test";

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String buildPath, VHDLLanguageSupport aVHDLLanguageSupport) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(buildPath);

		assertTrue(fFSCache.exists(buildPath, true));

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("Offline Build test", aBasePath, sf, null, aVHDLLanguageSupport);
		fZPrj.clean();

	}

	public void setupTest(String aBasePath, String buildPath) throws Exception {
		setupTest(aBasePath, buildPath, VHDLLanguageSupport.VHDL2008);
	}

	private DUUID getUID(ZamiaProject aZPrj) {
		BuildPath bp = aZPrj.getBuildPath();

		Toplevel tl = bp.getToplevel(0);

		return aZPrj.getDUM().getArchDUUID(tl.getDUUID());
	}

	public void testAdd4Offline() throws Exception {

		fFSCache = FSCache.getInstance();

		fFSCache.cleanAll();

		setupTest(tmpDir, "test/BuildPathAdd4.txt");

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		int n = fZPrj.getERM().getNumErrors();

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("Error %d/%d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		IGManager igm = fZPrj.getIGM();

		DUUID duuid = getUID(fZPrj);

		assertNotNull(duuid);

		int nNodes = igm.countNodes(duuid);

		logger.info("Add4 IG has %d nodes.", nNodes);

		assertEquals(13, nNodes);

		// now, move the sources

		File f = new File("test/add4");
		File f2 = new File("test/add4.1");
		f.renameTo(f2);

		// and build again

		setupTest(tmpDir, "test/BuildPathAdd4.txt");

		builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		n = fZPrj.getERM().getNumErrors();

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("Error %d/%d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		nNodes = igm.countNodes(duuid);

		logger.info("Add4 IG has %d nodes.", nNodes);

		assertEquals(13, nNodes);

		f2.renameTo(f);

		fZPrj.shutdown();
	}

	public void testLeonOffline() throws Exception {

		fFSCache = FSCache.getInstance();

		if (!enableLeonOfflineTest)
			return;

		fFSCache.cleanAll();

		setupTest(tmpDir, "test/BuildPathLeonSOCExtern.txt", VHDLLanguageSupport.VHDL2002);

		ZamiaProjectBuilder builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		int n = fZPrj.getERM().getNumErrors();

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("Error %d/%d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		IGManager igm = fZPrj.getIGM();

		DUUID duuid = getUID(fZPrj);

		assertNotNull(duuid);

		int nNodes = igm.countNodes(duuid);

		logger.info("Leon IG has %d nodes.", nNodes);

		assertEquals(1913, nNodes);

		// now, move the sources
		File f = new File("examples/leonSOC");
		File f2 = new File("examples/leonSOC.1");
		f.renameTo(f2);

		// and build again

		setupTest(tmpDir, "test/BuildPathLeonSOCExtern.txt", VHDLLanguageSupport.VHDL2002);

		builder = fZPrj.getBuilder();

		builder.build(true, true, null);

		n = fZPrj.getERM().getNumErrors();

		for (int i = 0; i < n; i++) {
			ZamiaException em = fZPrj.getERM().getError(i);
			logger.error("Error %d/%d: %s", i + 1, n, em.toString());
		}

		assertEquals(0, n);

		nNodes = igm.countNodes(duuid);

		logger.info("Leon IG has %d nodes.", nNodes);

		assertEquals(1913, nNodes);

		f2.renameTo(f);

	}

	@Override
	protected void tearDown() {
		if (fZPrj != null) {
			fZPrj.shutdown();
			fZPrj = null;
		}
	}

	public static void main(String[] args) {

		FSCacheTest t = new FSCacheTest();
		try {
			t.testLeonOffline();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
