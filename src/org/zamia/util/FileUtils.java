/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 9, 2009
 */
package org.zamia.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * A collection of static methods for dealing with files
 * 
 * @author Guenter Bartsch
 *
 */

public class FileUtils {

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
			return false;
		}
	}
}
