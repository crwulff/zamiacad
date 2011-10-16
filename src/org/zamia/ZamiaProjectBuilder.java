/* 
 * Copyright 2008-2009,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 13, 2008
 */
package org.zamia;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zamia.cli.jython.ZCJInterpreter;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.rtl.RTLManager;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.VHDLIndexer;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaProjectBuilder {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static FSCache fsCache = FSCache.getInstance();

	private static final String PYTHON_BUILD_INIT_CMD = "zamia_build_init";

	public final static boolean dump = true;

	private static final int NUM_THREADS = 8;

	private ZamiaProject fZPrj;

	private int fNumTodoFiles;

	private int fTotalNumDoneFiles, fTotalNumLines, fTotalNumChars;

	private ExecutorService fExecutorService;

	private LinkedList<VHDLIndexer> fIndexerPool;

	private HashSet<String> fTodoList; // for debug output only

	private final DMManager fDUM;

	private IZamiaMonitor fMonitor = null;

	private final ERManager fERM;

	private boolean fBuildPathErrs;

	private int fLastWorked; // for progress reporting

	public ZamiaProjectBuilder(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fDUM = fZPrj.getDUM();
		fERM = fZPrj.getERM();
	}

	/**
	 * If aFullBuild=false and the build path has not changed in a significant way (adding
	 * or removing toplevels is ok, everything else will trigger a full build), this tries to do an 
	 * incremental build based on the given set of changed source files. 
	 * 
	 * if that fails (e.g. a package was changed), returns -1 otherwise returns the number of rebuilt nodes.
	 * 
	 * if a full build is done this will return 0
	 * 
	 * @param aChanged
	 * @return number if rebuilt nodes or -1 if full build is required or 0 if full build was done
	 * @throws ZamiaException 
	 * @throws IOException 
	 */

	public int build(boolean aFullBuild, boolean aBPChanged, HashSetArray<SourceFile> aSFs) throws ZamiaException, IOException {

		boolean fullBuild = aFullBuild;
		if (fullBuild || aBPChanged) {
			fullBuild = parseBuildPath(aFullBuild, false);
		}

		if (fBuildPathErrs) {
			logger.error("ZamiaProjectBuilder: Aborting build because BuildPath.txt contains errors.");
			return 0;
		}

		if (isCanceled()) {
			logger.info("ZamiaProjectBuilder: Canceled.");
			return 0;
		}

		if (!fullBuild) {
			// check whether all toplevels truly exist
			// if some or all are missing, past builds have failed so we
			// need to upgrade this build to a full one

			BuildPath bp = fZPrj.getBuildPath();
			if (bp == null) {
				logger.error("ZamiaProjectBuilder: Build aborted, no BuildPath.txt found.");
				return 0;
			}
			IGManager igm = fZPrj.getIGM();
			int n = bp.getNumToplevels();
			for (int i = 0; i < n; i++) {
				Toplevel tl = bp.getToplevel(i);
				IGModule m = igm.findModule(tl);
				if (m == null) {
					logger.info("ZamiaProjectBuilder: Toplevel %s is missing => upgrading to full build.", tl);
					fullBuild = true;
					break;
				}
			}
		}

		int numChanged = 0;

		if (fullBuild) {

			fullBuild();

		} else {

			numChanged = incrementalBuild(aSFs);
		}

		if (isCanceled()) {
			logger.info("ZamiaProjectBuilder: Canceled.");
			return 0;
		}

		ZamiaProfiler.getInstance().startTimer("ZDB commit");
		setTaskName("ZDB commit");
		fZPrj.getZDB().flush();
		worked(100);

		ZamiaProfiler.getInstance().stopTimer("ZDB commit");

		ZamiaProfiler.getInstance().dump();

		return numChanged;
	}

	private void removeBPErrors(BuildPath aBP) {
		SourceFile sf = aBP.getSourceFile();

		fERM.removeErrors(sf);

		int n = aBP.getNumIncludes();
		for (int i = 0; i < n; i++) {

			String include = aBP.getInclude(i);

			File includeFile = new File(include);

			sf = new SourceFile(includeFile.getAbsoluteFile());

			fERM.removeErrors(sf);
		}
	}

	private boolean detectBPErrors(BuildPath aBP) {
		SourceFile sf = aBP.getSourceFile();

		if (fERM.getNumErrors(sf) > 0) {
			return true;
		}

		int n = aBP.getNumIncludes();
		for (int i = 0; i < n; i++) {

			String include = aBP.getInclude(i);

			File includeFile = new File(include);

			sf = new SourceFile(includeFile.getAbsoluteFile());

			if (fERM.getNumErrors(sf) > 0) {
				return true;
			}
		}

		return false;
	}

	private boolean parseBuildPath(boolean aFullBuild, boolean aSupressFullBuild) {

		logger.info("ZamiaProjectBuilder: Parsing BuildPath.txt ...");
		setTaskName("Parsing BuildPath.txt");
		worked(100);

		fBuildPathErrs = false;

		boolean fullBuild = aFullBuild;

		BuildPath oldbp = fZPrj.getBuildPath();
		SourceFile sf = oldbp.getSourceFile();
		if (sf == null) {
			logger.info("ZamiaProjectBuilder: Not parsing build path because no source file given.");
			fBuildPathErrs = true;
			ZamiaProfiler.getInstance().stopTimer("BP parsing");
			return fullBuild;
		}

		removeBPErrors(oldbp);

		try {
			BuildPath newbp = new BuildPath();
			newbp.setSrc(sf);

			newbp.parse(null, true, fZPrj);
			fZPrj.setBuildPath(newbp);

			if (detectBPErrors(newbp)) {
				fBuildPathErrs = true;
				return false;
			}

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: Canceled.");
				return false;
			}

			fullBuild = fullBuild || newbp.sigDiff(oldbp);

			if (fullBuild && !aSupressFullBuild) {
				fZPrj.clean();
				fZPrj.setBuildPath(newbp);
				fZPrj.initJythonInterpreter();
			} else {
				fZPrj.setBuildPath(newbp);
			}

		} catch (IOException e) {
			el.logException(e);
			fERM.addError(new ZamiaException(e.getMessage(), new SourceLocation(sf, 0)));
		} catch (ZamiaException e) {
			el.logException(e);
			fERM.addError(new ZamiaException(e.getMessage(), e.getLocation()));
		}

		return fullBuild;
	}

	private void fullBuild() throws ZamiaException, IOException {

		ZamiaProfiler.getInstance().reset();

		logger.info("ZamiaProjectBuilder: Running Tcl build init...");
		setTaskName("Running Tcl build init...");

		tclBuildInit(true);

		if (isCanceled()) {
			logger.info("ZamiaProjectBuilder: Canceled.");
			return;
		}

		logger.info("ZamiaProjectBuilder: Indexing...");
		setTaskName("Indexing...");

		indexProject();

		if (isCanceled()) {
			logger.info("ZamiaProjectBuilder: Canceled.");
			return;
		}

		logger.info("ZamiaProjectBuilder: Parsing local sources...");
		setTaskName("Parsing...");

		parseLocalSources();

		if (isCanceled()) {
			logger.info("ZamiaProjectBuilder: Canceled.");
			return;
		}

		rebuildIGs();

		synthesize();
	}

	/*
	 * Indexer stuff
	 */

	private synchronized void countStats(boolean aIndexing, int aNumChars, int aNumLines) {
		fTotalNumDoneFiles++;
		fTotalNumChars += aNumChars;
		fTotalNumLines += aNumLines;
		if (aIndexing) {
			logger.info("ZamiaProjectBuilder: Indexing: %d files processed, %d to go, %d lines so far.", fTotalNumDoneFiles, fNumTodoFiles, fTotalNumLines);
		} else {
			logger.info("ZamiaProjectBuilder: Parsing: %d files processed, %d lines so far.", fTotalNumDoneFiles, fTotalNumLines);
		}

		int worked = fTotalNumDoneFiles * 1000 / (fTotalNumDoneFiles + fNumTodoFiles);
		if (worked > fLastWorked) {
			worked(1);
			fLastWorked++;
		}
	}

	private synchronized void decTodoFiles() {
		fNumTodoFiles--;
		notifyAll();
	}

	private synchronized void incTodoFiles() {
		fNumTodoFiles++;
		notifyAll();
	}

	private synchronized void addToTodoList(String aPath) {
		fTodoList.add(aPath);
	}

	private synchronized void removeFromTodoList(String aPath) {
		fTodoList.remove(aPath);
	}

	class ProcessFileJob implements Runnable {

		private String fPath;

		private int fPriority;

		private String fLibId;

		private boolean fUseFSCache;

		private BuildPath fBP;

		private boolean fBottomUp;

		private boolean fRecursive;

		private int fDepth;

		public ProcessFileJob(String aPath, String aLibId, int aPriority, boolean aUseFSCache, boolean aBottomUp, boolean aRecursive, int aDepth) {
			fPath = aPath;
			fLibId = aLibId;
			fPriority = aPriority;
			fBP = fZPrj.getBuildPath();
			fUseFSCache = aUseFSCache;
			fBottomUp = aBottomUp;
			fRecursive = aRecursive;
			fDepth = aDepth;
			addToTodoList(aPath);
		}

		public void run() {

			if (!isCanceled()) {
				if (dump) {
					logger.debug("ZamiaProjectBuilder: Working on path '%s'", fPath);
				}

				if (fsCache.isDirectory(fPath, fUseFSCache) && (fRecursive || fDepth == 0)) {
					if (!isCanceled()) {
						String[] files = fsCache.list(fPath, fUseFSCache);

						if (files != null) {
							for (int i = 0; i < files.length; i++) {
								String fn = files[i];

								if (isCanceled()) {
									break;
								}

								String filename = fPath + File.separator + fn;

								incTodoFiles();
								fExecutorService.execute(new ProcessFileJob(filename, fLibId, fPriority, fUseFSCache, fBottomUp, fRecursive, fDepth + 1));
							}
						}

						countStats(true, 0, 0);
					}
				} else {
					File f = new File(fPath);

					if (fileAcceptable(f, fBP)) {
						if (dump) {
							logger.debug("ZamiaProjectBuilder: accepting: " + fPath);
						}

						SourceFile sf = new SourceFile(f);

						VHDLIndexer indexer = getVHDLIndexer();
						if (indexer != null) {

							Reader reader = null;
							try {
								reader = fsCache.openFile(sf, fUseFSCache);
								if (!isCanceled()) {
									indexer.parse(reader, fLibId, sf, fPriority, fUseFSCache, fBottomUp, fDUM);
								}
							} catch (IOException e) {
								logger.error("ZamiaProjectBuilder: IOException caught while indexing '%s': %s", sf.getAbsolutePath(), e.getMessage());
								el.logException(e);
							} catch (ZamiaException e) {
								el.logZamiaException(e);
							} finally {
								if (reader != null) {
									try {
										reader.close();
									} catch (IOException e) {
										el.logException(e);
									}
								}
							}
							countStats(true, sf.getNumChars(), sf.getNumLines());

							putVHDLIndexer(indexer);
						} else {
							logger.error("ZamiaProjectBuilder: *** Error: indexer NULL!!!!!");
						}

					} else {
						if (dump) {
							logger.debug("ZamiaProjectBuilder: rejecting: " + fPath);
						}
					}

				}

			}
			decTodoFiles();
			removeFromTodoList(fPath);
		}
	}

	private synchronized VHDLIndexer getVHDLIndexer() {
		try {
			while (fIndexerPool.isEmpty()) {
				wait();
			}
		} catch (InterruptedException e) {
			el.logException(e);
		}
		return fIndexerPool.remove();
	}

	private synchronized void putVHDLIndexer(VHDLIndexer aIndexer) {
		fIndexerPool.add(aIndexer);
		notifyAll();
	}

	public static boolean fileNameAcceptable(String aFileName) {
		String fn = aFileName.toLowerCase();
		boolean acceptable = fn.endsWith(".vhd") || fn.endsWith(".vhdl") || fn.endsWith(".v");

		if (dump) {
			logger.debug("File '%s' is acceptable: %s", aFileName, acceptable ? "yes" : "no");
		}

		return acceptable;
	}

	private boolean fileAcceptable(File aFile, BuildPath aBuildPath) {
		String name = aFile.getName();
		boolean acceptable = fileNameAcceptable(name);

		if (acceptable) {
			int n = aBuildPath.getNumIgnorePatterns();
			for (int i = 0; i < n; i++) {
				String pattern = aBuildPath.getIgnorePattern(i);

				if (name.matches(pattern))
					return false;
			}
		}

		return acceptable;
	}

	private synchronized String getDirListJobsString() {
		StringBuilder buf = new StringBuilder();
		int n = fTodoList.size();
		int m = 0;
		for (Iterator<String> i = fTodoList.iterator(); i.hasNext();) {
			String path = i.next();
			buf.append(path);
			buf.append('\n');
			m++;
			if (m > 3) {
				break;
			}
		}
		if (m < n) {
			buf.append("\n...");
		}
		return buf.toString();
	}

	public void tclBuildInit(boolean aIsFullBuild) {
		ZCJInterpreter zti = fZPrj.getZCJ();

		if (zti.hasCommand(PYTHON_BUILD_INIT_CMD)) {
			try {

				String tclBool = aIsFullBuild ? "1" : "0";

				zti.eval(PYTHON_BUILD_INIT_CMD + " " + tclBool);
			} catch (Throwable e) {
				el.logException(e);
			}
		}
	}

	private void indexProject() {

		long startTime = System.currentTimeMillis();

		logger.info("ZamiaProjectBuilder: Starting multi-threaded indexing...");
		logger.info("ZamiaProjectBuilder: ===================================");

		ZamiaProfiler.getInstance().startTimer("Indexing");

		fExecutorService = Executors.newFixedThreadPool(NUM_THREADS);

		fIndexerPool = new LinkedList<VHDLIndexer>();
		for (int i = 0; i < NUM_THREADS; i++) {
			fIndexerPool.add(new VHDLIndexer());
		}

		fLastWorked = 0;

		fTodoList = new HashSet<String>();

		BuildPath bp = fZPrj.getBuildPath();

		fNumTodoFiles = 0;
		int n = bp.getNumEntries();
		for (int i = 0; i < n; i++) {
			BuildPathEntry entry = bp.getEntry(i);

			if (entry.fExtern) {
				logger.info("ZamiaProjectBuilder: *** listing external source: '%s'", entry.fPrefix);

				//				if (!entry.fReadonly) {
				//					fsCache.invalidate(entry.fPrefix);
				//				}

				incTodoFiles();
				fExecutorService.execute(new ProcessFileJob(entry.fPrefix, entry.fLibId, entry.fPriority, entry.fReadonly, entry.fBottomUp, entry.fRecursive, 0));
			}
		}

		// Join will wait for just a single thread. A better solution might be a
		// java.util.concurrent.CountDownLatch. Simply initialize the latch with
		// the count set to the number of worker threads. Each worker thread
		// should call countDown() just before it exits, and the main thread
		// simply calls await(), which will block until the counter reaches
		// zero.

		while (!isCanceled() && getTodoDirs() > 0) {
			logger.info("ZamiaProjectBuilder: Waiting for indexing of external files to complete. " + fNumTodoFiles + " jobs to go.");
			logger.info("ZamiaProjectBuilder: %s", getDirListJobsString());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				el.logException(e);
			}
		}

		if (isCanceled()) {
			fsCache.setCancelWait(true);
		}

		logger.debug("ZamiaProjectBuilder: Listing of external files is complete now. Thread pool shutdown.");

		try {
			if (isCanceled()) {
				fExecutorService.shutdownNow();
			} else {
				fExecutorService.shutdown();
			}
			fExecutorService.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			el.logException(e);
		}
		fsCache.setCancelWait(false);

		double time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.info("ZamiaProjectBuilder: Indexing external sources done. " + fTotalNumChars + " chars in " + fTotalNumLines + " lines in " + fTotalNumDoneFiles
				+ " files so far. Time elapsed: " + time + "s.");
		ZamiaProfiler.getInstance().stopTimer("Indexing");

		if (fLastWorked < 1000) {
			worked(1000 - fLastWorked);
		}
	}

	private SFDMInfo compileFile(SourceFile aSF, boolean aIsFullBuild) throws IOException, ZamiaException {
		BuildPath aBuildPath = fZPrj.getBuildPath();

		SFDMInfo info = null;

		if (fileAcceptable(aSF.getFile(), aBuildPath)) {

			BuildPathEntry entry = aBuildPath.findEntry(aSF);

			if (entry.fLibId != null) {
				info = fDUM.compileFile(aSF, null, entry.fLibId, entry.fPriority, entry.fBottomUp, entry.fReadonly, false);
				countStats(false, aSF.getNumChars(), aSF.getNumLines());
			}
		}
		return info;
	}

	private void compileDir(String aAbsPath, String aLocalPath) throws IOException, ZamiaException {

		File dir = new File(aAbsPath);

		String[] files = dir.list();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			String fn = files[i];

			if (fn.equals("ZDB"))
				continue;

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: Canceled.");
				return;
			}

			String absPath = aAbsPath + File.separator + fn;
			String localPath = aLocalPath.length() > 0 ? aLocalPath + File.separator + fn : fn;

			File f = new File(absPath);

			if (f.isDirectory()) {
				compileDir(absPath, localPath);
			} else {
				SourceFile sf = new SourceFile(f);
				sf.setLocalPath(localPath);

				compileFile(sf, true);
			}
		}
	}

	public void parseLocalSources() throws IOException, ZamiaException {

		long startTime = System.currentTimeMillis();
		ZamiaProfiler.getInstance().startTimer("Parsing");

		compileDir(fZPrj.getBasePath(), "");
		worked(1000);

		double time = (System.currentTimeMillis() - startTime) / 1000.0;

		logger.info("ZamiaProjectBuilder: Finished parsing local sources. Runtime: " + time + " s. " + fTotalNumChars + " chars, " + fTotalNumLines + " lines processed in "
				+ fTotalNumDoneFiles + " files.");
		ZamiaProfiler.getInstance().stopTimer("Parsing");
	}

	private void rebuildIGs() {
		logger.info("ZamiaProjectBuilder: Building instantiation graph(s):");
		logger.info("ZamiaProjectBuilder: ================================");
		setTaskName("Elaborating (IG)...");

		long startTime = System.currentTimeMillis();
		ZamiaProfiler.getInstance().startTimer("IG");

		BuildPath bp = fZPrj.getBuildPath();

		IGManager igm = fZPrj.getIGM();

		int n = bp.getNumToplevels();

		for (int i = 0; i < n; i++) {

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: Canceled.");
				break;
			}

			Toplevel toplevel = bp.getToplevel(i);

			igm.buildIG(toplevel, fMonitor, 1000 / n);
		}

		double d = System.currentTimeMillis() - startTime;

		logger.info("ZamiaProjectBuilder: Finished building instantiation graph(s). Time elapsed: %fs", d / 1000.0);
		ZamiaProfiler.getInstance().stopTimer("IG");

	}

	private void synthesize() throws ZamiaException {
		logger.info("ZamiaProjectBuilder: Synthesizing:");
		logger.info("ZamiaProjectBuilder: =============");
		setTaskName("Synthesizing...");

		long startTime = System.currentTimeMillis();
		ZamiaProfiler.getInstance().startTimer("Synth");

		BuildPath bp = fZPrj.getBuildPath();

		RTLManager rtlm = fZPrj.getRTLM();

		rtlm.clean();

		int n = bp.getNumSynthTLs();

		for (int i = 0; i < n; i++) {

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: Canceled.");
				break;
			}

			Toplevel toplevel = bp.getSynthTL(i);

			rtlm.buildRTL(toplevel, fMonitor, 1000 / n);
		}

		double d = System.currentTimeMillis() - startTime;

		logger.info("ZamiaProjectBuilder: Finished synthesizing. Time elapsed: %fs", d / 1000.0);
		ZamiaProfiler.getInstance().stopTimer("Synth");

	}

	private synchronized int getTodoDirs() {
		return fNumTodoFiles;
	}

	public void clean() {
		fTotalNumChars = 0;
		fTotalNumLines = 0;
		fTotalNumDoneFiles = 0;
	}

	public void setMonitor(IZamiaMonitor aMonitor) {
		fMonitor = aMonitor;
	}

	private boolean isCanceled() {
		return fMonitor != null ? fMonitor.isCanceled() : false;
	}

	private void worked(int aUnits) {
		if (fMonitor == null) {
			return;
		}
		fMonitor.worked(aUnits);
	}

	private void setTaskName(String aName) {
		if (fMonitor == null) {
			return;
		}
		fMonitor.setTaskName(aName);
	}

	private int incrementalBuild(HashSetArray<SourceFile> aChanged) throws IOException, ZamiaException {

		ZamiaProfiler.getInstance().reset();

		logger.info("ZamiaProjectBuilder: Starting an incremental build.");

		tclBuildInit(false);

		BuildPath bp = fZPrj.getBuildPath();

		/*
		 * always re-build all source files that have errors
		 */

		HashSetArray<SourceFile> changed = new HashSetArray<SourceFile>();

		int n = aChanged.size();
		for (int i = 0; i < n; i++) {
			changed.add(aChanged.get(i));
		}

		ERManager erm = fZPrj.getERM();

		n = erm.getNumErrors();
		for (int i = 0; i < n; i++) {
			ZamiaException err = erm.getError(i);

			SourceLocation location = err.getLocation();

			if (location == null) {
				continue;
			}

			SourceFile sf = location.fSF;
			if (sf == null) {
				continue;
			}

			logger.info("ZamiaProjectBuilder: Adding '%s' to list of changed files because it has errors.", sf);

			changed.add(location.fSF);
		}

		/*
		 * figure out affected DUs, drop them from DUM (we will re-parse those files in a moment)
		 */

		ZamiaProfiler.getInstance().startTimer("Parsing");

		HashSetArray<DMUID> affectedDUUIDs = new HashSetArray<DMUID>();

		boolean needFullBuild = false;

		n = changed.size();
		for (int i = 0; i < n; i++) {
			SourceFile sf = changed.get(i);
			logger.info("ZamiaProjectBuilder: Changed source file %2d/%2d: %s", i + 1, n, sf);

			SFDMInfo info = fDUM.removeStubs(sf);
			if (info != null) {
				int m = info.getNumDMUIDs();
				for (int j = 0; j < m; j++) {
					DMUID duuid = info.getDMUID(j);

					if (!needFullBuild) {
						if (duuid.getType() != LUType.Entity && duuid.getType() != LUType.Architecture) {
							logger.info("ZamiaProjectBuilder: Non-Entity/Architecture DU '%s' was changed => need a full rebuild.", duuid);
							needFullBuild = true;
						}
					}

					affectedDUUIDs.add(duuid);
				}
			}

			BuildPathEntry entry = bp.findEntry(sf);

			if (entry.fLibId != null) {
				info = compileFile(sf, false);

				if (!needFullBuild && info != null) {
					int m = info.getNumDMUIDs();
					for (int j = 0; j < m; j++) {
						DMUID duuid = info.getDMUID(j);

						if (duuid.getType() != LUType.Entity && duuid.getType() != LUType.Architecture) {
							logger.info("ZamiaProjectBuilder: Non-Entity/Architecture DU '%s' was changed => need a full rebuild.", duuid);
							//needFullBuild = true;
						}
					}
				}
			}

			if (isCanceled()) {
				logger.info("ZamiaProjectBuilder: Canceled.");
				return -1;
			}

			worked(1000 / n);
		}

		ZamiaProfiler.getInstance().stopTimer("Parsing");

		//if (needFullBuild) {
		//	return -1;
		//}

		logger.info("ZamiaProjectBuilder: Number of DUs affected by incremental build: %d", affectedDUUIDs.size());

		/*
		 * figure out which IG nodes are affected by this, then
		 * - drop them from the graph and instantiators/instaniations lists
		 * - mark their instantiators as dirty
		 */

		IGManager igm = fZPrj.getIGM();
		n = igm.rebuildNodes(affectedDUUIDs, fMonitor);

		/*
		 * always re-synthesize all rtl modules
		 */

		synthesize();

		return n;
	}

	public void zdbChanged() {
		parseBuildPath(false, true);
	}
}
