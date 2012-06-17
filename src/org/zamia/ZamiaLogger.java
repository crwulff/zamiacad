/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.zamia.util.ZamiaTmpDir;

import com.spinn3r.log5j.Logger;

/**
 * Helper class to set up a Logger for use in zamiaCAD.
 * 
 * @author Guenter Bartsch
 */

public class ZamiaLogger {

	private static ZamiaLogger instance = null;

	private Logger logger = null;

	private RollingFileAppender logFileAppender;

	private PatternLayout layout;

	private static Level level = Level.DEBUG;

	public static final String FILENAME = "zamia.log";

	private static final long MAXLOGFILESIZE = 500 * 1024 * 1024;

	private static PrintStream out = null;

	private WriterAppender fConsoleAppender;

	private WriterAppender fWriterAppender;

	private String fFileName;

	public static void setup(Level level_) {
		level = level_;
	}

	private ZamiaLogger() {

		//		layout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss,SSS} %m%n");

		layout = new PatternLayout("%d{MMM dd HH:mm:ss} %m%n");

		logger = Logger.getLogger();

		logger.setLevel(level);

		fFileName = System.getenv("ZAMIA_LOG");

		if (fFileName == null) {
			File tmpDir = ZamiaTmpDir.getTmpDir();
			fFileName = tmpDir.getAbsolutePath() + File.separator + FILENAME;
		}

		//		String home = System.getProperty("user.home");

		try {
			logFileAppender = new RollingFileAppender(layout, fFileName, true);
			logFileAppender.setMaximumFileSize(MAXLOGFILESIZE);
			logFileAppender.setMaxBackupIndex(2);
			logger.addAppender(logFileAppender);

			PatternLayout hibernateLayout = new PatternLayout("%d{MMM dd HH:mm:ss} Hibernate: %m%n");
			RollingFileAppender hibernateAppender = new RollingFileAppender(hibernateLayout, fFileName, true);
			hibernateAppender.setMaximumFileSize(MAXLOGFILESIZE);
			hibernateAppender.setMaxBackupIndex(2);

			org.apache.log4j.Logger hibernateLogger = Logger.getLogger("org.hibernate");
			hibernateLogger.addAppender(hibernateAppender);

			hibernateAppender.setThreshold(Level.INFO);

		} catch (IOException e) {
			e.printStackTrace();
		}

		refreshAppenders();

		printHeader();
	}

	public void refreshAppenders() {

		// we always want to have output on the console

		if (fConsoleAppender != null) {
			Logger.getRootLogger().removeAppender(fConsoleAppender);
			logger.removeAppender(fConsoleAppender);
		}

		fConsoleAppender = new ConsoleAppender(layout);
		logger.addAppender(fConsoleAppender);
		fConsoleAppender.setThreshold(Level.INFO);

		// additionally, we want output on the zamia console if available

		if (fWriterAppender != null) {
			Logger.getRootLogger().removeAppender(fWriterAppender);
			logger.removeAppender(fWriterAppender);
		}

		if (out != null) {
			fWriterAppender = new WriterAppender(layout, out);
			logger.addAppender(fWriterAppender);
			fWriterAppender.setThreshold(Level.INFO);
		}
	}

	private void printHeader() {

		logger.info("zamiaCAD Log");
		logger.info("============");
		logger.info("");

		DateFormat defaultDate = DateFormat.getDateTimeInstance();
		logger.info("Current timestamp: %s", defaultDate.format(new Date()));

		logger.info("");

		logger.info("JVM Version   : " + System.getProperty("java.version"));
		Runtime r = Runtime.getRuntime();
		logger.info("Free Memory   : " + r.freeMemory());
		logger.info("Total Memory  : " + r.totalMemory());
		logger.info("Max Memory    : " + r.maxMemory());

		logger.info("");
	}

	public static ZamiaLogger getInstance() {
		if (instance == null) {
			instance = new ZamiaLogger();
		}
		return instance;
	}

	public Logger getLogger() {
		return logger;
	}

	// --- wrapper log5j ---

	public void info(String format, Object... args) {

		if (!logger.isInfoEnabled())
			return;

		logger.info(format, args);
	}

	public void info(String format, Throwable t, Object... args) {

		if (!logger.isInfoEnabled())
			return;

		logger.info(format, t, args);
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	public void debug(int indent, String format, Object... args) {

		if (!logger.isDebugEnabled())
			return;

		StringBuilder buf = new StringBuilder(indent * 2);
		for (int i = 0; i < indent; i++) {
			buf.append("  ");
		}

		logger.debug(buf + format, args);
	}

	public void debug(String format, Object... args) {

		if (!logger.isDebugEnabled())
			return;

		logger.debug(format, args);
	}

	public void debug(String format, Throwable t, Object... args) {

		if (!logger.isDebugEnabled())
			return;

		logger.debug(format, t, args);
	}

	public void error(String format, Object... args) {
		logger.error(format, args);
	}

	public void error(String format, Throwable t, Object... args) {
		logger.error(format, t, args);
	}

	public void fatal(String format, Object... args) {
		logger.fatal(format, args);
	}

	public void fatal(String format, Throwable t, Object... args) {
		logger.fatal(format, t, args);
	}

	public void warn(String format, Object... args) {
		logger.warn(format, args);
	}

	public void warn(String format, Throwable t, Object... args) {
		logger.warn(format, t, args);
	}

	public void log(Level level_, String string) {
		logger.log(level_, string);
	}

	public void log(Level level_, String string, Object... args) {
		logger.log(level_, Logger.sprintf(string, args));
	}

	public static void setConsoleOutput(PrintStream out_) {
		out = out_;

		if (instance != null) {
			instance.refreshAppenders();
		}

	}

	public String getLogFileName() {
		return fFileName;
	}
	
	/**Severity level 0-note, 1-warn, 2-err, 3-fatal corresponds to StaticValue severityLevel
	 * This method is called from assertion and report and must continue if not fatal.
	 * @throws ZamiaException */
	public void log(int severityLevel, String reportMsg, SourceLocation sourceLocation) throws ZamiaException {
		
//		System.out.println("FATAL_INT = " + Level.FATAL_INT); // = 50000 
//		System.out.println("ERROR_INT = " + Level.ERROR_INT); // = 40000
//		System.out.println("WARN_INT = " + Level.WARN_INT); // = 30000
//		System.out.println("INFO_INT = " + Level.INFO_INT); / = 20000 => VHDL note
//		System.out.println("DEBUG_INT = " + Level.DEBUG_INT); // = 10000
		
		logger.log(Level.toLevel(20000 + severityLevel * 10000), reportMsg + " at " + sourceLocation);
		
		//0 -note, 1-warn, 2-err, 3-fatal. By spec, in case of 0, 1 and 2, we continue 
		if (severityLevel == 3) {
			throw new ZamiaException (reportMsg, sourceLocation); 
		}
		
	}

}
