/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 9, 2009
 */
package org.zamia.util;

import org.zamia.ExceptionLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A collection of static methods for dealing with files
 * 
 * @author Guenter Bartsch
 *
 */

public class FileUtils {

    private static ExceptionLogger el = ExceptionLogger.getInstance();

	public static void deleteDirRecursive(File aDir) {
		
		if (!aDir.exists())
			return;
		
		if (aDir.isDirectory()) {
			File[] files = aDir.listFiles();
			for (int i = 0; i<files.length; i++) {
				deleteDirRecursive(files[i]);
			}
		}

		aDir.delete();
	}
	
	public static void fixDirPerms(File aDir) {
		aDir.setReadable(false, false);
		aDir.setReadable(true, true);
		aDir.setExecutable(false, false);
		aDir.setExecutable(true, true);
	}
	
	public static long du(File aDir) {
		
		ZStack<File> stack = new ZStack<File>();
		stack.push(aDir);
		
		HashSet<File> done = new HashSet<File>();
		long size = 0;
		
		while (!stack.isEmpty()) {
			File f = stack.pop();
			if (done.contains(f)) {
				continue;
			}
			done.add(f);
			
			if (f.isFile()) {
				size += f.length();
			} else {
				File[] files = f.listFiles();
				for (int i = 0; i<files.length; i++) {
					stack.push(files[i]);
				}
			}
		}

		return size;
	}

	public static boolean copy(File aSrc, File aDest) {
		try {
			org.apache.commons.io.FileUtils.copyFile(aSrc, aDest);
			return true;
		} catch (IOException e) {
            el.logException(e);
			return false;
		}
	}

	public static boolean unzip(File aZipFile) {
		return unzip(aZipFile, null);
	}

	public static boolean unzip(File aZipFile, Map<String, String> aFilePathsToUnzip) {

		boolean isOk = true;

		if (aFilePathsToUnzip == null) {
			aFilePathsToUnzip = Collections.emptyMap();
		}

		try {
			final int BUFFER = 2048;
			BufferedOutputStream dest;
			FileInputStream fis = new FileInputStream(aZipFile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!aFilePathsToUnzip.isEmpty() && !aFilePathsToUnzip.containsKey(entry.getName())) {
					continue;
				}
				System.out.println("Extracting: " + entry);
				int count;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				String name = entry.getName();
				if (!aFilePathsToUnzip.isEmpty()) {
					name = aFilePathsToUnzip.get(entry.getName());
				}
				FileOutputStream fos = new FileOutputStream(new File(aZipFile.getParent(), name));
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();
		} catch (Exception e) {
			el.logException(e);
			isOk = false;
		}

		return isOk;
	}
}
