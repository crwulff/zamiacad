/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Simple launch shortcut for intermediate files
 * 
 * @author Guenter Bartsch
 *
 */

public class LaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			if (((IStructuredSelection) selection).getFirstElement() instanceof IFile) {
				launch((IFile) ((IStructuredSelection) selection)
						.getFirstElement(), mode);
			}
		}
	}

	public void launch(IEditorPart editor, String mode) {
		FileEditorInput editorInput = (FileEditorInput) editor.getEditorInput();
		launch(editorInput.getFile(), mode);
	}

	private void launch(IFile bin, String mode) {
//		ILaunchConfiguration config = findLaunchConfiguration(bin, mode);
//		if (config != null) {
//			DebugUITools.launch(config, mode);
//		}
		
		System.out.println ("Launching: "+bin+" mode: "+mode);
		
	}

//	private ILaunchConfiguration findLaunchConfiguration(IFile bin, String mode) {
//		ILaunchConfiguration configuration = null;
//		List candidateConfigs = Collections.EMPTY_LIST;
//		try {
//			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
//					.getLaunchManager().getLaunchConfigurations();
//			candidateConfigs = new ArrayList(configs.length);
//			for (int i = 0; i < configs.length; i++) {
//				ILaunchConfiguration config = configs[i];
//				String projectName = config.getAttribute(
//						ZamiaPlugin.ATTR_PROJECT,
//						(String) null);
//				String toplevel = config.getAttribute(
//						ZamiaPlugin.ATTR_TOPLEVEL,
//						(String) null);
//				String name = bin.getName();
//				if (toplevel != null && programFile.equals(name)) {
//					if (projectName != null
//							&& projectName.equals(bin.getProject().getName())) {
//						candidateConfigs.add(config);
//					}
//				}
//			}
//		} catch (CoreException e) {
//			PerlDebugPlugin.log(e);
//		}
//
//		// If there are no existing configs associated with the File and the
//		// project, create one.
//		// If there is more then one config associated with the File, return
//		// the first one.
//		int candidateCount = candidateConfigs.size();
//		if (candidateCount < 1) {
//			configuration = createConfiguration(bin);
//		} else {
//			configuration = (ILaunchConfiguration) candidateConfigs.get(0);
//		}
//		return configuration;
//	}

//	private ILaunchConfiguration createConfiguration(IFile file) {
//		ILaunchConfiguration config = null;
//		String projectName = file.getProjectRelativePath().toString();
//		ILaunchConfigurationType[] configType = getLaunchManager()
//				.getLaunchConfigurationTypes();
//		ILaunchConfigurationType type = null;
//		for (int i = 0; i < configType.length; i++) {
//			if (configType[i].getIdentifier().equals(
//					DEBUG_LAUNCH_CONFIGURATION_PERL)) {
//				type = configType[i];
//			}
//		}
//		try {
//			if (type != null) {
//				ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
//						getLaunchManager()
//								.generateUniqueLaunchConfigurationNameFrom(
//										file.getName()));
//				wc.setAttribute(
//						PerlLaunchConfigurationConstants.ATTR_STARTUP_FILE,
//						projectName);
//				wc.setAttribute(
//						PerlLaunchConfigurationConstants.ATTR_PROJECT_NAME,
//						file.getProject().getName());
//				wc
//						.setAttribute(
//								PerlLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
//								(String) null);
//				config = wc.doSave();
//			}
//		} catch (CoreException e) {
//			PerlDebugPlugin.log(e);
//		}
//		return config;
//	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

}
