/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 23, 2009
 */
package org.zamia;

import java.util.LinkedList;

/**
 * This class is used to collect delayed warnings and error messages.
 * 
 * Particularly useful in identifier/name resolution when errors are generated
 * during disambiguation while it is not yet clear if the name resolution will
 * succeed or not.
 * 
 * if it works, we throw away the messages, if it fails, we report them all.
 * 
 * @author Guenter Bartsch
 * 
 */

public class ErrorReport {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	public final static int ENTRY_LIMIT = 32;

	static class ErrorReportEntry {
		private String fMsg;

		private SourceLocation fLocation;

		public ErrorReportEntry(String aMsg, SourceLocation aLocation) {
			fMsg = aMsg;
			fLocation = aLocation;
		}

		@Override
		public String toString() {
			if (fLocation == null) {
				return fMsg;
			}
			return fLocation + ": " + fMsg;
		}
	}

	private LinkedList<ErrorReportEntry> fEntries = new LinkedList<ErrorReportEntry>();

	private boolean fOverflow = false;

	public ErrorReport() {
	}

	public void append(String aMsg, SourceLocation aLocation) {
		if (fEntries.size() > ENTRY_LIMIT) {
			fOverflow = true;
			fEntries.removeFirst();
		} 
		fEntries.add(new ErrorReportEntry(aMsg, aLocation));
	}

	public void append(ZamiaException aMsg) {
		append(aMsg.getMessage(), aMsg.getLocation());
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (fOverflow) {
			buf.append("...\n");
		}
		for (ErrorReportEntry entry : fEntries) {
			buf.append(entry.toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	public void log() {
		logger.debug("Error report:\n%s", toString());
	}

	public int getNumEntries() {
		return fEntries.size();
	}
	
	protected LinkedList<ErrorReportEntry> getEntries() {
		return fEntries;
	}
	
	public void append(ErrorReport aReport) {
		LinkedList<ErrorReportEntry> entries = aReport.getEntries();
		for (ErrorReportEntry entry : entries) {
			if (fEntries.size() > ENTRY_LIMIT) {
				fOverflow = true;
				fEntries.removeFirst();
			}
			fEntries.add(entry);
		}
	}
}
