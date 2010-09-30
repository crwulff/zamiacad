/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 28, 2009
 */
package org.zamia.util;

import java.io.PrintStream;

import org.zamia.ZamiaLogger;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class LoggerPrintStream extends PrintStream {

	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private static LoggerPrintStream instance = null;

	public static LoggerPrintStream getInstance() {
		if (instance == null) {
			instance = new LoggerPrintStream();
		}
		return instance;
	}

	private StringBuilder fBuf = new StringBuilder();

	private LoggerPrintStream() {
		super(System.out);
	}

	private void printChar(char aC) {

		if (aC == '\n') {
			logger.info("%s", fBuf);
			fBuf = new StringBuilder();
		} else {
			fBuf.append(aC);
		}
	}

	public void print(String aStr) {
		int l = aStr.length();
		for (int i = 0; i < l; i++) {
			printChar(aStr.charAt(i));
		}
	}

	public void println() {
		printChar('\n');
	}

	public void println(String aStr) {
		print(aStr);
		println();
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		for (int i = off; i < (off + len); i++) {
			printChar((char) buf[i]);
		}
	}

	@Override
	public void write(int b) {
		printChar((char) b);
	}

}
