/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.RefreshTab;
import org.zamia.plugin.launch.ZamiaLaunchMainTab;

/**
 * @author guenter bartsch
 */

public class ZamiaTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
        ZamiaLaunchMainTab mainModuleTab = new ZamiaLaunchMainTab();
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
            mainModuleTab,
			//new ArgumentsTab(PydevPlugin.getPythonInterpreterManager(), mainModuleTab),          
			new RefreshTab(),
			new EnvironmentTab(),
			new CommonTab()	};
		setTabs(tabs);
	}
}
