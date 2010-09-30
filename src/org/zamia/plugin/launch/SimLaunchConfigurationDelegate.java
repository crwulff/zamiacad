/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProfiler;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.views.sim.SimulatorView;


/**
 * Launch the ZamiaCAD logic simulator
 * 
 * @author Guenter Bartsch
 * 
 */
public class SimLaunchConfigurationDelegate extends org.eclipse.debug.core.model.LaunchConfigurationDelegate {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static boolean enabled = true;

	public void launch(ILaunchConfiguration aConfig, String aMode, ILaunch aLaunch, IProgressMonitor aMonitor) throws CoreException {

		if (aMonitor == null) {
			aMonitor = new NullProgressMonitor();
		}

		aMonitor.beginTask("Launching simulator...", 100);

		SimRunnerConfig runConfig = new SimRunnerConfig(aConfig, aMode, SimRunnerConfig.RUN_SIM);

		run(runConfig, aLaunch, aMonitor);

		aMonitor.worked(1);
	}

	private void run(final SimRunnerConfig aConfig, ILaunch aLaunch, final IProgressMonitor aMonitor) throws CoreException {

		if (enabled) {

			ZamiaProfiler.getInstance().dump();

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

					IWorkbenchPage page = window.getActivePage();

					try {
						SimulatorView sview = (SimulatorView) page.showView("org.zamia.plugin.views.sim.SimulatorView");
						if (sview != null) {
							sview.run(aConfig, aMonitor);
						}

					} catch (PartInitException e) {
						el.logException(e);
					}
				}
			});
		}
	}
}
