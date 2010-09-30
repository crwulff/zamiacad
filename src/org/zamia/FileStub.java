/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 15, 2008
 */
package org.zamia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import org.zamia.util.LevelGZIPOutputStream;
import org.zamia.util.ZHash;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class FileStub {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static int VERSION = 1;

	public static int NUM_HEADER_LINES = 2;

	public final static boolean dump = false;

	private String fPath;

	private boolean fValid;

	private String fCachePath;

	public FileStub(String aPath, String aFileCachePathStr) {
		fPath = aPath;
		fValid = false;

		fCachePath = aFileCachePathStr + File.separator + ZHash.encodeZ(fPath);
	}

	public synchronized void validate() {
		if (fValid) {
			if (dump)
				logger.debug("FSCache: HIT MEM ON '%s'", fPath);
			return;
		}

		load();

		if (!fValid) {
			if (dump)
				logger.debug("FSCache: MISS ON '%s'", fPath);
			copy();
		} else {
			if (dump)
				logger.debug("FSCache: HIT FILE ON '%s'", fPath);
		}

	}

	private synchronized void load() {

		File f = new File(fCachePath);
		if (f.exists()) {
			BufferedReader in = null;
			try {

				if (FSCache.compress) {
					in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fCachePath))));
				} else {
					in = new BufferedReader(new InputStreamReader(new FileInputStream(fCachePath)));
				}

				String path = in.readLine();
				if (path == null || !path.equals(fPath)) {
					throw new IOException("Warning: Wrong path. " + path + " vs " + fPath + ". Was reading from cache file " + fCachePath + ". Will re-fetch file from source.");
				}

				String version = in.readLine();
				if (version == null || Integer.parseInt(version) != VERSION) {
					throw new IOException("Warning: Wrong version. Was reading from cache file " + fCachePath + ". Will re-fetch file from source.");
				}

				fValid = true;

			} catch (IOException e) {
				el.logException(e);
			} catch (NumberFormatException e) {
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
	}

	private synchronized void copy() {

		if (FSCache.enableSlowDown) {
			try {
				Thread.sleep(FSCache.slowDownDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		boolean copyOK = false;

		PrintWriter out = null;
		BufferedReader in = null;
		try {

			in = new BufferedReader(new FileReader(fPath));
			if (FSCache.compress) {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new LevelGZIPOutputStream(new FileOutputStream(fCachePath + ".1"), Deflater.BEST_SPEED))));
			} else {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fCachePath + ".1"))));
			}

			// write header

			out.println(fPath);
			out.println(VERSION);

			// write body

			while (true) {

				String line = in.readLine();
				if (line == null)
					break;

				out.println(line);
			}

			copyOK = true;

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
			if (out != null) {
				out.close();
			}
		}

		if (copyOK) {

			File f = new File(fCachePath + ".1");
			File fTo = new File(fCachePath);
			if (f.renameTo(fTo)) {
				fValid = true;
			} else {
				logger.error("FSCache: FileStub.copy() failed: couldn't rename %s to %s.", f, fTo);
			}
		}

	}

	public String getPath() {
		return fPath;
	}

	public synchronized boolean isValid() {
		return fValid;
	}

	public synchronized String getCachedPath() {
		if (!fValid) {
			logger.error("FSCache: getCanonicalPath() on invalid file cache entry for path '%s'", fPath);
		}
		return fCachePath;
	}

}
