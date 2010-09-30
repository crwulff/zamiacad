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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.zamia.util.ZHash;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class FileStat {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static int VERSION = 1;

	private String fPath;

	private String fCachePath;

	private boolean fValid;

	private boolean fExists;

	private boolean fIsDirectory;

	private String fCanonicalPath;

	private long fLastModified;

	private String fFiles[];

	public FileStat(String aPath, String aStatCachePathStr) {
		fPath = aPath;
		fValid = false;

		fCachePath = aStatCachePathStr + File.separator + ZHash.encodeZ(fPath);
	}

	public synchronized void validate() {
		if (fValid) {
			return;
		}

		load();

		if (!fValid) {
			stat();
		}

		save();
	}

	private synchronized void load() {

		File f = new File(fCachePath);
		if (f.exists()) {
			BufferedReader in = null;

			try {

				in = new BufferedReader(new FileReader(fCachePath));

				String path = in.readLine();
				if (path == null || !path.equals(fPath)) {
					logger.info("FileStat: Wrong path in '%s': path was %s, should have been %s", fCachePath, path, fPath);
				} else {

					String version = in.readLine();
					if (version == null || Integer.parseInt(version) != VERSION) {
						throw new IOException("Wrong version.");
					}

					fCanonicalPath = in.readLine();
					if (fCachePath == null) {
						throw new IOException("Canonical path not present.");
					}

					String line = in.readLine();
					if (line == null) {
						throw new IOException("IsDirectory not present.");
					}
					fIsDirectory = line.equals("true");

					line = in.readLine();
					if (line == null) {
						throw new IOException("Exists not present.");
					}
					fExists = line.equals("true");

					line = in.readLine();
					if (line == null) {
						throw new IOException("LastModified not present.");
					}
					fLastModified = Long.parseLong(line);

					if (fIsDirectory) {
						line = in.readLine();
						if (line == null) {
							throw new IOException("number of files not present.");
						}
						int n = Integer.parseInt(line);
						fFiles = new String[n];
						for (int i = 0; i < n; i++) {
							fFiles[i] = in.readLine();
							if (fFiles[i] == null) {
								throw new IOException("too few file entries present.");
							}
						}
					}

					fValid = fExists;
				}
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

	private synchronized void save() {

		if (!fValid) {
			return;
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fCachePath)));

			out.println(fPath); // this is needed for FSCache.invalidate() to work
			out.println(VERSION);
			out.println(fCanonicalPath);
			out.println(fIsDirectory);
			out.println(fExists);
			out.println(fLastModified);

			if (fIsDirectory) {
				int n = fFiles != null ? fFiles.length : 0;
				out.println(n);
				for (int i = 0; i < n; i++) {
					out.println(fFiles[i]);
				}
			}

			out.close();

		} catch (IOException e) {
			el.logException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private synchronized void stat() {

		if (FSCache.enableSlowDown) {
			try {
				Thread.sleep(FSCache.slowDownDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		File file = new File(fPath);

		fFiles = null;
		fExists = file.exists();
		fValid = false;

		try {
			fCanonicalPath = file.getCanonicalPath();
		} catch (IOException e) {
			el.logException(e);
			fCanonicalPath = file.getAbsolutePath();
		}

		if (fExists) {

			fLastModified = file.canRead() ? file.lastModified() : 0;
			fIsDirectory = file.isDirectory();

			if (fIsDirectory) {
				if (!file.canRead()) {
					logger.error("FSCache: cannot read dir '%s' (permission denied?)", fCanonicalPath);

					fValid = false;
				} else {
					File[] files = file.listFiles();

					int n = files != null ? files.length : 0;
					fFiles = new String[n];

					for (int i = 0; i < n; i++) {
						File f = files[i];
						String fileName = f.getName();
						fFiles[i] = fileName;
					}
					fValid = true;
				}
			} else {
				fValid = true;
			}
		} else {
			fIsDirectory = false;
			fLastModified = 0;
			fValid = false;
		}
	}

	public String getPath() {
		return fPath;
	}

	public synchronized boolean isValid() {
		return fValid;
	}

	public synchronized String[] getFiles() {
		if (!fValid) {
			logger.error("FSCache: getFiles() on invalid stat cache entry for path '%s'", fPath);
		}
		if (!fIsDirectory) {
			logger.error("FSCache: getFiles() on non-directory '%s'", fPath);
		}
		return fFiles;
	}

	public synchronized boolean isDirectory() {
		if (!fValid) {
			logger.error("FSCache: isDirectory() on invalid stat cache entry for path '%s'", fPath);
		}
		return fIsDirectory;
	}

	public synchronized boolean exists() {
		if (!fValid) {
			logger.error("FSCache: exists() on invalid stat cache entry for path '%s'", fPath);
		}
		return fExists;
	}

	public synchronized long getLastModified() {
		if (!fValid) {
			logger.error("FSCache: getLastModified() on invalid stat cache entry for path '%s'", fPath);
		}
		return fLastModified;
	}

	public synchronized String getCanonicalPath() {
		if (!fValid) {
			logger.error("FSCache: getCanonical() on invalid stat cache entry for path '%s'", fPath);
		}
		return fCanonicalPath;
	}
}
