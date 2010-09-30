/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 13, 2008
 */
package org.zamia;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import org.zamia.util.FileUtils;
import org.zamia.util.ZHash;
import org.zamia.util.ZamiaTmpDir;


/**
 * buffer access to remote files and directories
 * 
 * a true singleton.
 * 
 * This FileSystem-Cache has these properties:
 * 
 * - caches file contents and directory information - thread-safe - can be
 * disabled / made transparent per call ( useCache-parameter) - pure java
 * implementation - operations can be canceled - can be invalidated completely /
 * partially
 * 
 * @author Guenter Bartsch
 * 
 */

public class FSCache {

	static class ReaderInputStream extends InputStream {
		private Reader fReader;

		public ReaderInputStream(Reader aReader) {
			super();
			fReader = aReader;
		}

		@Override
		public int read() throws IOException {
			int t = fReader.read();
			return t;
		}
	}

	public final static boolean dump = false;

	public static final boolean compress = false;

	public final static boolean enableSlowDown = false;

	public final static long slowDownDelay = 25000;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static FSCache instance = null;

	private File fFileCacheDir, fStatCacheDir;

	private String fFileCacheDirStr, fStatCacheDirStr;

	private HashSet<String> fValidatingFiles, fValidatingStats;

	private Lock fFilesLock, fStatsLock;

	private Condition fFileValidatedCond, fStatValidatedCond;

	private boolean fEnabled = false;

	private FSCache() {

		String override = System.getenv("ZAMIA_FS_CACHE");
		if (override == null || !"disabled".equalsIgnoreCase(override)) {
			fEnabled = true;
		}

		File tmpDir = ZamiaTmpDir.getTmpDir();
		fixDirPerms(tmpDir);

		fFileCacheDir = new File(ZamiaTmpDir.getTmpDir() + File.separator + "fs_cache" + File.separator + "files");
		fFileCacheDir.mkdirs();
		fixDirPerms(fFileCacheDir);
		fFileCacheDirStr = fFileCacheDir.getAbsolutePath();

		fStatCacheDir = new File(ZamiaTmpDir.getTmpDir() + File.separator + "fs_cache" + File.separator + "stats");
		fStatCacheDir.mkdirs();
		fixDirPerms(fStatCacheDir);
		fStatCacheDirStr = fStatCacheDir.getAbsolutePath();

		fFilesLock = new ReentrantLock();
		fValidatingFiles = new HashSet<String>();
		fFileValidatedCond = fFilesLock.newCondition();

		fStatsLock = new ReentrantLock();
		fValidatingStats = new HashSet<String>();
		fStatValidatedCond = fStatsLock.newCondition();
	}

	private void fixDirPerms(File aDir) {
		aDir.setReadable(false, false);
		aDir.setReadable(true, true);
		aDir.setExecutable(false, false);
		aDir.setExecutable(true, true);
	}

	public static FSCache getInstance() {
		if (instance == null) {
			instance = new FSCache();
		}
		return instance;
	}

	public void invalidate(String aPathPrefix) {

		fStatsLock.lock();
		try {
			while (!fValidatingStats.isEmpty()) {
				try {
					fStatValidatedCond.await();
				} catch (InterruptedException e) {
				}
			}

			File[] files = fStatCacheDir.listFiles();
			for (int i = 0; i < files.length; i++) {

				BufferedReader in = null;
				try {
					in = new BufferedReader(new FileReader(files[i]));

					String path = in.readLine();

					if (path == null || path.startsWith(aPathPrefix) || aPathPrefix.startsWith(path)) {
						files[i].delete();
					}

				} catch (IOException e) {
					el.logException(e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							el.logException(e);
						}
					}
				}
			}
		} finally {
			fStatsLock.unlock();
		}
	}

	private boolean isEnabled(boolean aUseCache) {
		return fEnabled && aUseCache;
	}

	/********************************************************************************
	 * 
	 * 
	 * file stat cache part
	 * 
	 * 
	 ********************************************************************************/

	public boolean isDirectory(String aPath, boolean aUseCache) {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			return f.isDirectory();
		}

		FileStat info = getFileStat(aPath);

		if (!info.isValid()) {
			logger.error("FSCache.isDirectory: Invalid path: '%s'", aPath);
		}

		return info.isDirectory();
	}

	public boolean exists(String aPath, boolean aUseCache) {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			return f.exists();
		}

		FileStat info = getFileStat(aPath);

		if (!info.isValid()) {
			logger.error("FSCache.exists: Invalid path: '%s'", aPath);
		}

		return info.exists();
	}

	public boolean exists(SourceFile aSF, boolean aUseFSCache) {

		String uri = aSF.getURI();
		if (uri != null)
			return true;

		return exists(aSF.getAbsolutePath(), aUseFSCache);
	}

	public String[] list(String aPath, boolean aUseCache) {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			return f.list();
		}

		FileStat info = getFileStat(aPath);

		if (!info.isValid()) {
			logger.error("FSCache.list: Invalid path: '%s'", aPath);
		}

		return info.getFiles();
	}

	public String getCanonicalPath(String aPath, boolean aUseCache) {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			try {
				return f.getCanonicalPath();
			} catch (IOException e) {
				el.logException(e);
			}
			return f.getAbsolutePath();
		}

		FileStat info = getFileStat(aPath);

		if (!info.isValid()) {
			logger.error("FSCache.getCanonicalPath: Invalid path: '%s'", aPath);
		}

		return info.getCanonicalPath();
	}

	public long getLastModified(String aPath, boolean aUseCache) {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			return f.lastModified();
		}

		FileStat info = getFileStat(aPath);

		if (!info.isValid()) {
			logger.error("FSCache.getLastModified: Invalid path: '%s'", aPath);
		}

		return info.getLastModified();
	}

	private FileStat getFileStat(String aPath) {

		File f = new File(cleanupPath(aPath));
		String path = f.getAbsolutePath();

		FileStat info = null;

		fStatsLock.lock();
		try {
			while (fValidatingStats.contains(path)) {
				try {
					fStatValidatedCond.await();
				} catch (InterruptedException e) {
				}
			}

			info = new FileStat(aPath, fStatCacheDirStr);

			if (!info.isValid()) {

				fValidatingStats.add(path);
				fStatsLock.unlock();

				info.validate();

				fStatsLock.lock();
				fValidatingStats.remove(path);
				fStatValidatedCond.signalAll();
			}
		} finally {
			fStatsLock.unlock();
		}

		return info;
	}

	public static String getDirPath(String aPath) {
		int pos = aPath.lastIndexOf(File.separatorChar);
		if (pos < 0) {
			logger.error("FSCache.getDirPath: Invalid path: '%s'", aPath);
			return null;
		}
		String dirPath = aPath.substring(0, pos);
		if (dirPath.length() == 0) {
			dirPath = "" + File.separatorChar;
		}
		return dirPath;
	}

	public static String getFilePath(String aPath) {
		int pos = aPath.lastIndexOf(File.separatorChar);
		if (pos < 0) {
			logger.error("FSCache.getFilePath: Invalid path: '%s'", aPath);
			return null;
		}
		String fileName = aPath.substring(pos + 1);
		return fileName;
	}

	/********************************************************************************
	 * 
	 * 
	 * File cache part
	 * 
	 * 
	 ********************************************************************************/

	private String cleanupPath(String aPath) {
		String path = aPath;
		while (path.contains("//")) {
			path = path.replace("//", "/");
		}
		return path;
	}

	private FileStub getFileInfo(String aPath) {

		File f = new File(cleanupPath(aPath));
		String path = f.getAbsolutePath();

		FileStub stub = null;

		fFilesLock.lock();
		try {
			while (fValidatingFiles.contains(path)) {
				try {
					fFileValidatedCond.await();
				} catch (InterruptedException e) {
				}
			}

			stub = new FileStub(path, fFileCacheDirStr);

			if (!stub.isValid()) {

				fValidatingFiles.add(path);
				fFilesLock.unlock();

				stub.validate();

				fFilesLock.lock();
				fValidatingFiles.remove(path);
				fFileValidatedCond.signalAll();
			}
		} finally {
			fFilesLock.unlock();
		}

		return stub;
	}

	public Reader openFile(String aPath, boolean aUseCache) throws IOException {

		if (!isEnabled(aUseCache)) {
			File f = new File(aPath);
			return new BufferedReader(new FileReader(f));
		}
		
		FileStub fileInfo = getFileInfo(aPath);

		if (!fileInfo.isValid()) {
			throw new IOException("FSCache: openFile called on invalid/non-existent path: " + aPath);
		}
		String cachePath = fileInfo.getCachedPath();

		BufferedReader rd = null;
		if (compress) {
			rd = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cachePath))));
		} else {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(cachePath)));
		}

		// skip header

		for (int i = 0; i < FileStub.NUM_HEADER_LINES; i++) {
			rd.readLine();
		}

		return rd;
	}

	public Reader openFile(SourceFile aSF, boolean aUseCache) throws IOException {

		if (aSF.isLocal()) {
			return new BufferedReader(new FileReader(aSF.getFile()));
		}

		String uri = aSF.getURI();
		if (uri != null) {

			InputStream stream = this.getClass().getResourceAsStream(uri);

			if (stream == null) {
				throw new IOException("Unable to find " + uri);
			}

			return new BufferedReader(new InputStreamReader(stream));
		}

		return openFile(aSF.getAbsolutePath(), aUseCache);
	}

	public InputStream openFileStream(String aPath, boolean aUseCache) throws IOException {
		return new ReaderInputStream(openFile(aPath, aUseCache));
	}

	public OutputStream openFileWrite(String aPath) throws FileNotFoundException {

		String canonicalPath = getCanonicalPath(aPath, false);

		String cachePath = getFileCachePath(canonicalPath);
		File cacheFile = new File(cachePath);

		if (cacheFile.exists()) {
			if (!cacheFile.delete()) {
				logger.error("FSCache: Couldn't delete cache file %s", cacheFile.getAbsolutePath());
			}
		}

		return new BufferedOutputStream(new FileOutputStream(canonicalPath));
	}

	private String getFileCachePath(String aPath) {
		String cachePath = ZHash.encodeZ(aPath);
		return fFileCacheDirStr + File.separator + cachePath;
	}

	public void setCancelWait(boolean aCancelWait) {
		// FIXME: disabled for now.
		// fCancelWait = aCancelWait;
	}

	public void cleanAll() {

		logger.info("FSCache: Cleaning FSCache...");

		fFilesLock.lock();
		try {
			fStatsLock.lock();
			try {
				FileUtils.deleteDirRecursive(fFileCacheDir);

				if (!fFileCacheDir.exists() && !fFileCacheDir.mkdirs()) {
					logger.error("FSCache: FATAL: failed to create %s.", fFileCacheDir);
					System.exit(1);
				}
				fixDirPerms(fFileCacheDir);
				fFileCacheDirStr = fFileCacheDir.getAbsolutePath();

				FileUtils.deleteDirRecursive(fStatCacheDir);

				if (!fStatCacheDir.exists() && !fStatCacheDir.mkdirs()) {
					logger.error("FSCache: FATAL: failed to create %s.", fStatCacheDir);
					System.exit(1);
				}
				fixDirPerms(fStatCacheDir);
				fStatCacheDirStr = fStatCacheDir.getAbsolutePath();

			} finally {
				fStatsLock.unlock();
			}
		} finally {
			fFilesLock.unlock();
		}

		logger.info("FSCache: Cleaning FSCache done.");
	}

}
