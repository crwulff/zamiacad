/*
 * Copyright 2007, 2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.plugin.views.navigator.IGModuleWrapper;
import org.zamia.vhdl.ast.DUUID;


/**
 * Used to launch an IGNode directly, automatically generates/updates a
 * corresponding launch configuration
 * 
 * @author Guenter Bartsch
 * 
 */

public class LaunchIGModuleAction extends org.eclipse.ui.actions.ActionDelegate {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();
	public final static ExceptionLogger el = ExceptionLogger.getInstance();
	
	private IGModuleWrapper fWrapper;

	@Override
	public void selectionChanged(IAction aAction, ISelection aSelection) {

		if (aSelection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) aSelection).getFirstElement();

			if (first != null && first instanceof IGModuleWrapper) {
				fWrapper = (IGModuleWrapper) first;
			}
		}
		super.selectionChanged(aAction, aSelection);
	}

	public void run(IAction aAction) {

		if (fWrapper == null)
			return;

		try {
			logger.debug("LaunchIGModuleAction: Launching: " + fWrapper);

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

			ILaunchConfigurationType type = null;

			type = manager.getLaunchConfigurationType("org.zamia.plugin.SimLaunchConfigurationDelegate");

			//System.out.println("Type: " + type);

			ILaunchConfiguration config = null;
			// if the configuration already exists, delete it

			DUUID duuid = fWrapper.getDUUID();
			
			String id = duuid.toString();

			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations();
			for (int i = 0; i < configurations.length; i++) {
				if (configurations[i].getName().equals(id))
					configurations[i].delete();
			}
			// else create a new one
			if (config == null) {
				ILaunchConfigurationWorkingCopy configWC = type.newInstance(null, id);

				ZamiaProject zprj = fWrapper.getZPrj();
				
				IProject prj = ZamiaProjectMap.getProject(zprj);

				configWC.setAttribute(SimRunnerConfig.ATTR_TOPLEVEL, duuid.toString());
				configWC.setAttribute(SimRunnerConfig.ATTR_PROJECT, prj.getName());

				config = configWC.doSave();
			}
			ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
			manager.addLaunch(launch);
			//config.delete();
		} catch (CoreException e) {
			el.logException(e);
		}
	}

}
