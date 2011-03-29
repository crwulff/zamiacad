/* 
 * Copyright 2003-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 *  
 * Created on Nov 29, 2003
 * 
 */

package org.zamia.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.util.ZamiaTmpDir;
import org.zamia.zdb.ZDBException;

/**
 * Zamia main class
 * 
 * basically sets up the cli und gets the whole thing running
 * 
 * @author Guenter Bartsch
 */

public class Zamia {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static String VERSION = "0.10.1";

	public final static String COPYRIGHT1 = "Copyright 2003-2010 by Guenter Bartsch.";

	public final static String COPYRIGHT2 = "All rights reserved.";

	public final static String COPYRIGHT3 = "";

	public final static String COPYRIGHT4 = "See the LICENSE file for details.";

	public final static String INFO1 = "Author: Guenter Bartsch <guenter@zamia.org>";

	public final static String INFO2 = "";

	public final static String INFO3 = "";

	public final static String INFO4 = "Online documentation: http://www.zamia.org";

	public final static String INFO5 = "Bug tracking: http://zamiacad.sf.net/";

	public final static String INFO6 = "";

	private String fProjectId;

	private String[] fExtraArgs;

	private ZamiaProject fZPrj;

	public Zamia(String args[]) throws IOException, ZamiaException {

		ZamiaLogger.setup(Level.DEBUG);

		// determine default values for project variables

		String curDirPath = System.getProperty("user.dir");
		File curDir = new File(curDirPath);

		//logger.info ("Current dir: %s", curDir);

		fProjectId = curDir.getAbsoluteFile().getName();

		//logger.info ("Project id: %s", projectId);

		String projectBasePath = curDirPath;

		String projectBuildPath = projectBasePath + File.separator + "BuildPath.txt";

		String projectDataPath = ZamiaTmpDir.getTmpDir().getAbsolutePath();

		processArgs(args);

		logger.debug("projectId is %s", fProjectId);

		try {
			fZPrj = new ZamiaProject(fProjectId, projectBasePath, new SourceFile(new File(projectBuildPath)), projectDataPath);

		} catch (ZDBException e) {
			el.logException(e);
			System.exit(1);
		}
	}

	private void processArgs(String args[]) {
		CommandLineParser parser = new GnuParser();
		Options options = new Options();

		Option option = new Option("p", "project id");
		option.setLongOpt("project-id");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("p")) {
				fProjectId = line.getOptionValue("p");
			}

			fExtraArgs = line.getArgs();

			if (fExtraArgs.length != 1) {
				throw new ParseException("No BuildPath.txt given.");
			}

		} catch (ParseException e) {
			logger.error("");
			logger.error("Error while processing command line options: %s", e.toString());
			logger.error("");
			String cmdLine = "zamiacad [options...] BuildPath.txt\n\n";

			String footer = "\n";
			String header = "\n";

			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(60, cmdLine, header, options, footer, false);
			System.exit(1);
		}
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	private static long startTime;

	public static void main(String[] args) {
		// performance evaluation code
		startTime = System.currentTimeMillis();

		logger.info("ZamiaCAD V%s http://www.zamiacad.com/", VERSION);
		logger.info("");
		logger.info("%s", COPYRIGHT1);
		logger.info("%s", COPYRIGHT2);
		logger.info("%s", COPYRIGHT3);
		logger.info("%s", COPYRIGHT4);
		logger.info("");
		logger.info("%s", INFO1);
		logger.info("%s", INFO2);
		logger.info("%s", INFO3);
		logger.info("%s", INFO4);
		logger.info("%s", INFO5);
		logger.info("%s", INFO6);

		Zamia zamia;
		try {
			zamia = new Zamia(args);
			zamia.shutdown();
		} catch (Throwable e) {
			el.logException(e);
		}

		logger.info("");
		printStats();
		long time = System.currentTimeMillis() - startTime;
		logger.info("Runtime      : %d sec", time / 1000);
	}

	private void shutdown() {
		logger.info("Shutting down...");
		fZPrj.shutdown();
		logger.info("Shutting down...done.");
		logger.info("");
		logger.info("Thank you for using ZamiaCAD.");
	}

	public static void printStats() {
		long allocedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		logger.info("Heap alloc   : %d MB", allocedMem / (1024 * 1024));
	}

}
