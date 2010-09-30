/*
 * Copyright 2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DUUID.LUType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("unchecked")
public class SimRunnerConfig {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	// for now, these are hard-coded constants:

	public static final int SIM_BUILTIN = 0;

	public static final int SIM_VCD_IMPORT = 1;

	public static final int SIM_AET_IMPORT = 2;

	public static final String RUN_SIM = "RUN_SIM";

	public static final String ATTR_PROJECT = ZamiaPlugin.PLUGIN_ID + ".ATTR_PROJECT";

	public static final String ATTR_TOPLEVEL = ZamiaPlugin.PLUGIN_ID + ".ATTR_TOPLEVEL";

	public static final String ATTR_TRACES = ZamiaPlugin.PLUGIN_ID + ".ATTR_TRACES";

	public static final String ATTR_FILENAME = ZamiaPlugin.PLUGIN_ID + ".ATTR_FILENAME";

	public static final String ATTR_PREFIX = ZamiaPlugin.PLUGIN_ID + ".ATTR_PREFIX";

	public static final String ATTR_SIMULATOR = ZamiaPlugin.PLUGIN_ID + ".ATTR_SIMULATOR";

	private IProject fProject;

	private String fFilename;

	private DUUID fToplevel;

	private int fSimulator;

	public String[] fEnvP = null;

	private String fRun;

	private ILaunchConfiguration fConfiguration;

	private boolean fIsDebug;

	private List fTraces;
	
	private String fPrefix;

	public SimRunnerConfig(ILaunchConfiguration aConfiguration, String aMode, String aRun) throws CoreException {
		fConfiguration = aConfiguration;
		fRun = aRun;
		fIsDebug = aMode.equals(ILaunchManager.DEBUG_MODE);

		fToplevel = getToplevel(aConfiguration);
		fTraces = getTraces(aConfiguration);
		fFilename = getFilename(aConfiguration);
		fSimulator = getSimulator(aConfiguration);
		fPrefix = getPrefix(aConfiguration);

		//find the project
		IWorkspace w = ResourcesPlugin.getWorkspace();
		String projName = aConfiguration.getAttribute(ATTR_PROJECT, "");
		if (projName.length() == 0) {
			throw new CoreException(ZamiaPlugin.makeStatus(IStatus.ERROR, "Unable to get project for the run", null));
		}

		fProject = w.getRoot().getProject(projName);

		if (fProject == null) { //Ok, we could not find it out
			throw new CoreException(ZamiaPlugin.makeStatus(IStatus.ERROR, "Could not get project for resource: " + projName, null));
		}

		//make the environment
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		fEnvP = launchManager.getEnvironment(aConfiguration);
	}

	private List getTraces(ILaunchConfiguration aConfiguration) throws CoreException {
		List traces = aConfiguration.getAttribute(ATTR_TRACES, (List) null);
		if (traces == null) {
			traces = new ArrayList<String>();
		}
		return traces;
	}

	private DUUID getToplevel(ILaunchConfiguration aConfiguration) throws CoreException {
		String str = aConfiguration.getAttribute(ATTR_TOPLEVEL, "MYENTITY");

		try {
			return DUUID.parse(str);
		} catch (ZamiaException e) {
			el.logException(e);
		}

		return new DUUID(LUType.Entity, "WORK", "MYENTITY", null);
	}

	public DUUID getToplevel() {
		return fToplevel;
	}

	private String getFilename(ILaunchConfiguration aConfiguration) throws CoreException {
		File f = new File("");
		return aConfiguration.getAttribute(ATTR_FILENAME, f.getAbsolutePath());
	}

	public String getFilename() {
		return fFilename;
	}

	private int getSimulator(ILaunchConfiguration aConfiguration) throws CoreException {
		return aConfiguration.getAttribute(ATTR_SIMULATOR, 0);
	}

	public int getSimulator() {
		return fSimulator;
	}

	private String getPrefix(ILaunchConfiguration aConfiguration) throws CoreException {
		return aConfiguration.getAttribute(ATTR_PREFIX, "");
	}
	
	public String getPrefix() {
		return fPrefix;
	}

	public ILaunchConfiguration getConfiguration() {
		return fConfiguration;
	}

	public String[] getEnvp() {
		return fEnvP;
	}

	public boolean isDebug() {
		return fIsDebug;
	}

	public IProject getProject() {
		return fProject;
	}

	public String getRun() {
		return fRun;
	}

	public List getTraces() {
		return fTraces;
	}

	public void setTraces(List aTraces) throws CoreException {
		ILaunchConfigurationWorkingCopy configWC = fConfiguration.getWorkingCopy();

		configWC.setAttribute(ATTR_TRACES, aTraces);

		configWC.doSave();

		fTraces = aTraces;
	}

}
