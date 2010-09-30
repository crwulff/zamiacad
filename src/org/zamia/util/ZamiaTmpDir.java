/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2010
 */
package org.zamia.util;

import java.io.File;

/**
 * Sole purpose of this class is to hold the getTmpDir() static method without any reference to ZamiaLogger
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaTmpDir {

	private static File tmpDir = null;

	public static File getTmpDir() {

		if (tmpDir != null)
			return tmpDir;

		String username = System.getProperty("user.name");
		if (username == null) {
			username = "zamia";
		}

		/*
		 * 1st attempt: $ZAMIA_DATA_DIR
		 */

		String zdr = System.getenv("ZAMIA_DATA_DIR");
		if (zdr != null) {

			tmpDir = new File(zdr);

			if (tmpDir.exists() || tmpDir.mkdirs()) {
				return tmpDir;
			} else {
				System.err.printf("ZamiaProject: ZAMIA_DATA_DIR environment variable set, but cannot create or use %s\n", tmpDir.getAbsolutePath());
				System.exit(1);
			}
		}

		/*
		 * 2nd attempt: /data/<username>/zamia
		 */

		if (!Native.isWindows()) {

			tmpDir = new File("/data/" + username + "/zamia");

			if (tmpDir.exists() || tmpDir.mkdirs())
				return tmpDir;
		}

		/*
		 * 3rd attempt: <tmp>/<username>/zamia
		 */

		tmpDir = new File(System.getProperty("java.io.tmpdir") + File.separator + username + File.separator + "zamia");
		if (!tmpDir.exists() && !tmpDir.mkdirs()) {
			System.err.println("Failed to create tmp dir " + tmpDir);
			System.exit(1);
		}

		return tmpDir;
	}

}
