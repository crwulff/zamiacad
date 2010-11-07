/* 
 * Copyright 2007-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 23, 2007
 */
package org.zamia.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.util.ZamiaTmpDir;

/**
 * Simple helper class to drive the Zamia compilation/elaboration infrastructure
 * conveniently
 * 
 * @author Guenter Bartsch
 * 
 */

public class Builder {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static String tmpDir = ZamiaTmpDir.getTmpDir().getAbsolutePath() + File.separator + "zamia-test";

	private ZamiaProject fZPrj;

	private ERManager fERM;

	private DMManager fDUM;

	private PrintStream fOut;

	//private int numLines = 0;

	private int fNumFiles = 0;

	private long fStartTime;

	private String fWorkLibId;

	public Builder(PrintStream aOut) {
		fOut = aOut;
		try {
			fZPrj = new ZamiaProject("Builder Tmp Project", tmpDir, null, null);
			fDUM = fZPrj.getDUM();
			fERM = fZPrj.getERM();
			fZPrj.clean();
		} catch (Throwable e) {
			el.logException(e);
		}
		setWorkLibId("WORK");
		fNumFiles = 0;
		fStartTime = System.currentTimeMillis();
	}

	public int compileDir(File aDir, String aWorkLibId) throws IOException, ZamiaException {

		int nErrors = 0;

		setWorkLibId(aWorkLibId);

		File[] files = aDir.listFiles();
		if (files == null)
			return 0;
		for (int i = 0; i < files.length; i++) {
			File f = files[i];

			if (f.isDirectory()) {
				nErrors += compileDir(f, aWorkLibId);
			} else {
				String filename = f.getName();

				if (ZamiaProjectBuilder.fileNameAcceptable(filename)) {

					try {
						nErrors += compileFile(f);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return nErrors;
	}

	private int compileFile(File aFile) throws IOException, ZamiaException {
		fDUM.compileFile(new SourceFile(new File(aFile.getAbsolutePath())), null, fWorkLibId, 0, true, false, false);

		//numLines += cr.getNumLines();
		fNumFiles++;
		long elapsedTime = (System.currentTimeMillis() - fStartTime) / 1000;
		fOut.printf("%3d:%02d", elapsedTime / 60, elapsedTime % 60);

		long allocedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		fOut.print(" " + allocedMem / (1024 * 1024) + "MB ");

		fOut.println("Compiled " + fNumFiles + " files. Last file compiled: " + aFile.getName());
		int n = fERM.getNumErrors();
		if (n > 0) {

			fOut.println();
			fOut.println("*** " + n + " error(s) found.");
			fOut.println();

			for (int i = 0; i < n; i++) {
				fOut.println();
				fOut.println("***** Error #" + (i + 1) + "/" + n + " in file " + aFile.getAbsolutePath());
				ZamiaException err = fERM.getError(i);
				fOut.println(err);
				fOut.println();
				showArea(aFile, err.getLocation().fLine);

				ZamiaException ze = new ZamiaException("Syntax error found: " + err, err.getLocation());
				ExceptionLogger.getInstance().logZamiaException(ze);

			}
		}
		return n;
	}

	private void showArea(File aFile, int aLine) {

		int l = 1;
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new FileReader(aFile));

			while (l < aLine - 4) {
				String str = buf.readLine();
				if (str == null)
					return;
				l++;
			}

			for (int i = 0; i < 8; i++) {
				String str = buf.readLine();
				if (str == null)
					return;
				fOut.printf("%5d: %s\n", l, str);
				l++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void clean() {
		try {
			fZPrj.clean();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setWorkLibId("WORK");
		fNumFiles = 0;
		fStartTime = System.currentTimeMillis();
	}

	public int compileFile(String aFile) throws IOException, ZamiaException {
		return compileFile(new File(aFile));
	}

	public void setWorkLibId(String aLibId) {
		fWorkLibId = aLibId;
	}

	public int compileDir(String aDirName, String aLibId) throws IOException, ZamiaException {
		return compileDir(new File(aDirName), aLibId);
	}

	public ZamiaProject getZamiaProject() {
		return fZPrj;
	}

	public DMManager getDUM() {
		return fDUM;
	}

	public void shutdown() {
		fZPrj.shutdown();
	}
}
