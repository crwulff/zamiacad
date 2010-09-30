/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.RefreshTab;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZamiaLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
        ZamiaLaunchMainTab mainTab = new ZamiaLaunchMainTab();
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
            mainTab,
			//new ArgumentsTab(PydevPlugin.getPythonInterpreterManager(), mainModuleTab),          
			new RefreshTab(),
			new EnvironmentTab(),
			new CommonTab()	};
		setTabs(tabs);
	}
}
