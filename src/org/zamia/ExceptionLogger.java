/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 31, 2008
 */
package org.zamia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.log4j.Level;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ExceptionLogger {

	private static ExceptionLogger instance;

	private static FSCache fsCache = FSCache.getInstance();

	private ZamiaLogger zl;

	private ExceptionLogger() {
		zl = ZamiaLogger.getInstance();
	}

	public static ExceptionLogger getInstance() {
		if (instance == null) {
			instance = new ExceptionLogger();
		}
		return instance;
	}

	public synchronized void logZamiaException(ZamiaException e_) {
		SourceLocation location = e_.getLocation();

		zl.error("");
		zl.error("==========================================================================");
		zl.error("EXCEPTION LOG STARTS: %s", e_.getMessage());

		logExceptionStacktrace(e_);

		if (location != null) {
			File file = location.fSF.getFile();
			if (file != null) {
				zl.error("");
				zl.error("Full path: " + file.getAbsolutePath());
				zl.error("");
			}
		}
		zl.error("--------------------------------------------------------------------------");
		zl.error("Source code excerpt:");
		if (location != null) {
			showArea(Level.ERROR, location.fSF, location.fLine);
		} else {
			zl.error("No location information given.");
		}
		zl.error("--------------------------------------------------------------------------");
	}

	public synchronized void logException(Throwable e_) {
		if (e_ instanceof ZamiaException) {
			logZamiaException((ZamiaException) e_);
		} else {
			zl.error("");
			zl.error("==========================================================================");
			logExceptionStacktrace(e_);
			zl.error("--------------------------------------------------------------------------");
		}
	}

	private void showArea(Level level, SourceFile sf_, int line_) {

		if (sf_ == null)
			return;

		Reader rd = null;
		BufferedReader buf = null;
		try {
			rd = fsCache.openFile(sf_, true);
			if (rd == null)
				return;

			zl.log(level, sf_.getAbsolutePath());

			buf = new BufferedReader(rd);

			int l = 1;
			while (l < line_ - 4) {
				String str = buf.readLine();
				if (str == null)
					return;
				l++;
			}

			for (int i = 0; i < 8; i++) {
				String str = buf.readLine();
				if (str == null)
					return;
				if (l != line_)
					zl.log(level, " %5d: %s", l, str);
				else
					zl.log(level, "*%5d: %s", l, str);
				l++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (buf != null) {
				try {
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void logExceptionStacktrace(Throwable e) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		//		StackTraceElement[] st = e.getStackTrace();
		//		for (int i = 0; i<st.length; i++) {
		//			
		//			StackTraceElement ste = st[i];
		//			
		//		}

		e.printStackTrace(pw);

		pw.flush();
		zl.error("%s", sw.getBuffer().toString());
	}

}
