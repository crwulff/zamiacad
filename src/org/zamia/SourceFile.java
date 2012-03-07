/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.Serializable;

/**
 * Simple source file abstraction and (suffix based) HDL recognition
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings( { "serial", "rawtypes" })
public class SourceFile implements Comparable, Serializable {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public static final int FORMAT_VHDL = 0;

	public static final int FORMAT_VERILOG = 1;

	public static final int FORMAT_UNKNOWN = 99;

	private File fFile;

	private String fURI; // alternative for class-loader based reading of source

	// files (used for std libs)

	private int fFormat;

	private int fNumLines, fNumChars;

	// source files that are located in an (e.g. eclipse) project
	// need local path information so their target lib can be
	// determined from BuildPath.txt
	private String fLocalPath;

	private Long fId;

	public SourceFile() {
	}

	public SourceFile(File aFile, String localPath) {
		this(aFile);
		setLocalPath(localPath);
	}
	public SourceFile(File aFile) {

		if (aFile == null) {
			logger.error("SourceFile: try to create a SourceFile from null File.");
		}

		fFile = aFile;
		fFormat = guessFormat();
		flush();
	}

	public SourceFile(String aURI) {
		fURI = aURI;
		fFormat = guessFormat();
		flush();
	}

	// persistence

	public Long getId() {
		return fId;
	}

	public void setId(Long aId) {
		fId = aId;
	}

	// properties

	public void setLocalPath(String aLocalPath) {
		fLocalPath = aLocalPath;
	}

	public String getLocalPath() {
		return fLocalPath;
	}

	public void setFormat(int aFormat) {
		fFormat = aFormat;
	}

	public int getFormat() {
		return fFormat;
	}

	public void setFile(File aFile) {
		fFile = aFile;
	}

	public File getFile() {
		return fFile;
	}

	public void setURI(String aURI) {
		fURI = aURI;
	}

	public String getURI() {
		return fURI;
	}

	// other methods

	public String getFileName() {
		return fFile != null ? fFile.getName() : fURI;
	}

	public String getAbsolutePath() {
		if (fFile == null)
			return fURI;
		return fFile.getAbsolutePath();
	}

	private int guessFormat() {
		String fileName = getFileName().toLowerCase();
		if (fileName.endsWith(".v"))
			return FORMAT_VERILOG;
		else if (fileName.endsWith(".vhd"))
			return FORMAT_VHDL;
		else if (fileName.endsWith(".vhdl"))
			return FORMAT_VHDL;
		return FORMAT_UNKNOWN;
	}

	public void flush() {
		fNumLines = 0;
	}

	public void setNumLines(int aNumLines) {
		fNumLines = aNumLines;
	}

	public int getNumLines() {
		return fNumLines;
	}

	public String toString() {
		String a = getLocalPath() != null ? getLocalPath() : null;
		String b = fFile != null ? fFile.getName() : fURI;
		return "(" + a + ") " + b;
	}

	public int compareTo(Object aObject) {
		if (!(aObject instanceof SourceFile))
			return 1;
		else
			return getFileName().compareTo(((SourceFile) aObject).getFileName());
	}

	@Override
	public boolean equals(Object aObject) {

		if (!(aObject instanceof SourceFile))
			return false;

		SourceFile sf = (SourceFile) aObject;

		return fFile != null ? fFile.equals(sf.getFile()) : fURI.equals(sf.getURI());
	}

	@Override
	public int hashCode() {
		return fFile != null ? fFile.hashCode() : fURI.hashCode();

	}

	public void setNumChars(int aNumChars) {
		fNumChars = aNumChars;
	}

	public int getNumChars() {
		return fNumChars;
	}

	public boolean isLocal() {
		return fLocalPath != null;
	}

	public String extractLine(int aLine) {

		String line = null;

		FSCache fscache = FSCache.getInstance();

		BufferedReader br = null;

		int nLine = 0;
		try {

			Reader reader = fscache.openFile(this, false);

			br = new BufferedReader(reader);

			while (nLine < aLine) {
				nLine++;
				line = br.readLine();
			}
		} catch (Throwable t) {
			el.logException(t);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}
		if (nLine != aLine) {
			line = "-- ???";
		}

		return line;
	}

	public void logExcerpt(int aLine) {
		int nlines = 12;
		int line = aLine - nlines / 2;
		if (line < 0) {
			line = 0;
		}
		while (line < aLine + nlines / 2) {
			String str = extractLine(line);
			if (line == aLine) {
				logger.debug("SourceFile:    *** %5d: %s", line, str);
			} else {
				logger.debug("SourceFile:        %5d: %s", line, str);
			}
			line++;
		}
	}
}