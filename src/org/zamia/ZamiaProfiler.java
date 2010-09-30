/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Another singleton, this time for profiling
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaProfiler {
	
	static class LogEntry {
		long total;
		long start;
		public LogEntry() {
			total = 0;
			start = 0;
		}
	}
	
	private static ZamiaProfiler instance;

	private HashMap<String, LogEntry> logs;
	
	private long startTime;
	
	private ZamiaProfiler () {
		logs = new HashMap<String, LogEntry>();
		startTime = System.currentTimeMillis();
	}
	
	public static ZamiaProfiler getInstance() {
		if (instance == null) {
			instance = new ZamiaProfiler();
		}
		return instance;
	}

	public void startTimer(String label_) {
		
		LogEntry le = logs.get(label_);
		if (le == null) {
			le = new LogEntry();
			logs.put(label_, le);
		}
		
		le.start = System.currentTimeMillis();
	}
	
	public void stopTimer(String label_) {
		long t = System.currentTimeMillis();
		LogEntry le = logs.get(label_);
		if (le == null) {
			System.err.println ("ERROR: tried to stop unknown timer "+label_);
			return;
		}
		le.total += t - le.start;
	}
	
	public long getTotalTime() {
		long t = System.currentTimeMillis();
		return t - startTime;
	}
	
	public void dump() {
		ZamiaLogger zl = ZamiaLogger.getInstance();
		
		zl.info("");
		zl.info("Zamia Profiler Results");
		zl.info("======================");
		zl.info("");
		
		for (Iterator<String> i = logs.keySet().iterator(); i.hasNext();) {
			String label = i.next();
			
			LogEntry le = logs.get(label);
			zl.info("%8.2fs %s", ((double)le.total) / 1000.0, label);
		}
	}

	public void reset() {
		logs = new HashMap<String, LogEntry>();
		startTime = System.currentTimeMillis();
	}
}
