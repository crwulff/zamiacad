/* 
 * Copyright 2003-2011 by the authors indicated in the @author tags. 
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
import java.text.SimpleDateFormat;
import java.util.Date;

import jline.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.w3c.dom.Node;

import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.cli.jython.ZCJInterpreter;
import org.zamia.util.Pair;
import org.zamia.util.XMLUtils;
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

	public final static String VERSION = "0.11.2";

	public final static String COPYRIGHT1 = "Copyright 2003-2011 by the zamiaCAD project and is covered by the";

	public final static String COPYRIGHT2 = "GNU General Public License, Version 3";

	public final static String COPYRIGHT3 = "";

	public final static String COPYRIGHT4 = "See the LICENSE file for details.";

	public final static String INFO1 = "Contact: support@zamia.org";

	public final static String INFO2 = "";

	private String fProjectId = null;

	private String fProjectBasePath;

	private String fScriptFile = null;

	private String fXmlFile = null;

	private boolean fStartShell = true;

	private String fScript = null;

	private String[] fExtraArgs;

	private ZamiaProject fZPrj;

	private ZCJInterpreter fZCJ;

	public Zamia(String args[]) throws IOException, ZamiaException {

		ZamiaLogger.setup(Level.DEBUG);

		// determine default values for project variables

		String curDirPath = System.getProperty("user.dir");

		fProjectBasePath = curDirPath;

		processArgs(args);

		File curDir = new File(fProjectBasePath);
		fProjectBasePath = curDir.getAbsolutePath();

		fProjectId = curDir.getAbsoluteFile().getName();

		String projectBuildPath = fProjectBasePath + File.separator + "BuildPath.txt";

		String projectDataPath = ZamiaTmpDir.getTmpDir().getAbsolutePath();

		logger.info("project id   : %s", fProjectId);
		logger.info("project base : %s", fProjectBasePath);
		logger.info("BuildPath.txt: %s", projectBuildPath);

		File bp = new File(projectBuildPath);
		if (!bp.exists()) {
			logger.error("%s not found.",projectBuildPath);
			System.exit(2);
		}

		try {
			fZPrj = new ZamiaProject(fProjectId, fProjectBasePath, new SourceFile(new File(projectBuildPath)), projectDataPath);

			fZCJ = fZPrj.getZCJ();

			try {

				if (fScriptFile != null) {
					File scriptFile = new File(fProjectBasePath, fScriptFile);
					if (scriptFile.exists()) {
						logger.info("Running script file %s", scriptFile.getAbsoluteFile());
						fZCJ.evalFile(scriptFile.getAbsolutePath());
					} else {
						logger.error("Specified script file %s doesn't exist!", scriptFile.getAbsoluteFile());
					}
				}

				if (fXmlFile != null) {
					File xmlFile = new File(fProjectBasePath, fXmlFile);
					if (xmlFile.exists()) {
						for (Pair<File, Node> scriptFile : XMLUtils.extractScriptFiles(xmlFile, fProjectBasePath)) {
							fZCJ.setObject("actionXmlFile", xmlFile);
							fZCJ.setObject("actionRootNode", scriptFile.getSecond());
							logger.info("Running script file %s from XML file %s", scriptFile.getFirst().getAbsoluteFile(), xmlFile.getAbsoluteFile());
							fZCJ.evalFile(scriptFile.getFirst().getAbsolutePath());
						}
					} else {
						logger.error("Specified XML file %s doesn't exist!", xmlFile.getAbsoluteFile());
					}
				}

				if (fScript != null) {
					logger.info("Running command line script...");
					fZCJ.eval(fScript);
				}

			} catch (Throwable e) {
				el.logException(e);
			}

		} catch (ZDBException e) {
			el.logException(e);
			System.exit(1);
		}
	}

	private void processArgs(String args[]) {
		CommandLineParser parser = new GnuParser();
		Options options = new Options();

		Option option = new Option("p", "project id (default: dir name)");
		option.setLongOpt("project-id");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		option = new Option("f", "python script file to execute");
		option.setLongOpt("script-file");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		option = new Option("d", "project base directory (default: current dir)");
		option.setLongOpt("project-dir");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		option = new Option("x", "xml script file to execute");
		option.setLongOpt("xml-script-file");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		option = new Option("s", "python script to execute");
		option.setLongOpt("script");
		option.setValueSeparator('=');
		option.setArgs(1);
		options.addOption(option);

		option = new Option("q", "quit when finished executing script (no shell)");
		option.setLongOpt("quit");
		options.addOption(option);

		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("f")) {
				fScriptFile = line.getOptionValue("f");
			}

			if (line.hasOption("d")) {
				fProjectBasePath += File.separator + line.getOptionValue("d");
			}

			if (line.hasOption("x")) {
				fXmlFile = line.getOptionValue("x");
			}

			if (line.hasOption("s")) {
				fScript = line.getOptionValue("s");
			}

			if (line.hasOption("q")) {
				fStartShell = false;
			}

			if (line.hasOption("p")) {
				fProjectId = line.getOptionValue("p");
			}

			fExtraArgs = line.getArgs();

			if (fExtraArgs.length != 0) {
				throw new ParseException("Unexpected extra arguments.");
			}

		} catch (ParseException e) {
			logger.error("");
			logger.error("Error while processing command line options: %s", e.toString());
			logger.error("");
			String cmdLine = "zamiacad [options]\n\n";

			String footer = "\n";
			String header = "\n";

			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(60, cmdLine, header, options, footer, false);
			System.exit(1);
		}
	}

	public void run() {
		if (!fStartShell) {
			return;
		}

		logger.info("");
		logger.info("Starting python shell.");
		logger.info("Type help() for help, type quit or exit to exit.");
		logger.info("");

		try {
			ConsoleReader reader = new ConsoleReader();
			reader.setBellEnabled(false);

			SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm:ss");

			boolean doRun = true;
			while (doRun) {

				String prompt = format.format(new Date()) + " > ";

				String line = reader.readLine(prompt);

				if (line.equals("exit") || line.equals("quit")) {
					doRun = false;
				} else {
					try {
						fZCJ.eval(line);
					} catch (Throwable t) {
						el.logException(t);
					}
				}
			}

		} catch (IOException e) {
			el.logException(e);
		}
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	private static long startTime;

	public static void main(String[] args) {
		// performance evaluation code
		startTime = System.currentTimeMillis();

		logger.info("zamiaCAD V%s http://www.zamia.org/", VERSION);
		logger.info("");
		logger.info("%s", COPYRIGHT1);
		logger.info("%s", COPYRIGHT2);
		logger.info("%s", COPYRIGHT3);
		logger.info("%s", COPYRIGHT4);
		logger.info("");
		logger.info("%s", INFO1);
		logger.info("%s", INFO2);

		Zamia zamia;
		try {
			zamia = new Zamia(args);
			zamia.run();
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
		logger.info("Thank you for using zamiaCAD.");
	}

	public static void printStats() {
		long allocedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		logger.info("Heap alloc   : %d MB", allocedMem / (1024 * 1024));
	}

}
