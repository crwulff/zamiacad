/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 16, 2010
 */
package org.zamia.verilog.pre;

import java.io.IOException;
import java.io.Reader;

import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VPStackFrame {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public enum VPSFType {
		FILE, CONDITION
	};

	private final SourceFile fFile;

	private final VPSFType fType;

	private final Reader fIn;

	private final boolean fCond;

	private boolean fElse = false;

	private int fLine = 0, fCol = 0;

	private int fNextLine = 0, fNextCol = 0;

	private boolean fCR = false;
	
	private boolean fEOF = false;

	private final boolean fDoClose;

	public VPStackFrame(SourceFile aFile, Reader aReader, boolean aUseCache) throws IOException {
		fFile = aFile;
		fType = VPSFType.FILE;

		FSCache fscache = FSCache.getInstance();

		fIn = aReader != null ? aReader : fscache.openFile(aFile, aUseCache);

		fDoClose = aReader == null;

		fCond = false;
	}

	public VPStackFrame(boolean aCond) {
		fType = VPSFType.CONDITION;
		fFile = null;
		fIn = null;
		fCond = aCond;
		fDoClose = false;
	}

	public VPSFType getType() {
		return fType;
	}

	public int read() throws IOException {

		int ich = fIn.read();

		fLine = fNextLine;
		fCol = fNextCol;

		if (ich >= 0) {
			char ch = (char) ich;

			switch (ch) {
			case 10:
				if (fCR) {
					fCR = false;
				} else {
					fNextLine++;
					fNextCol = 0;
				}
				break;
			case 13:
				fNextLine++;
				fNextCol = 0;
				fCR = true;
				break;
			default:
				fNextCol++;
				fCR = false;
				break;
			}
		} else {
			fEOF = true;
		}
		
		return ich;
	}
	
	boolean isEOF() {
		return fEOF;
	}

	public void close() throws IOException {
		if (fDoClose) {
			fIn.close();
		}
	}

	public SourceFile getSourceFile() {
		return fFile;
	}

	public void setElse(boolean _else) {
		fElse = _else;
	}

	public boolean isCond() {
		return fCond;
	}

	public boolean isElse() {
		return fElse;
	}

	public int getLine() {
		return fLine;
	}

	public int getCol() {
		return fCol;
	}

}
