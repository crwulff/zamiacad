/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia.plugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.zamia.ExceptionLogger;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class FindSignalAction implements IWorkbenchWindowActionDelegate {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IWorkbenchWindow fWindow;

	public void run(IAction action) {

		ZamiaProject zPrj = ZamiaPlugin.findCurrentProject();
		if (zPrj == null) {
			ZamiaPlugin.showError(fWindow.getShell(), "No project selected.", "Please select a project in the navigator.", "No project selected.");
			return;
		}
		FindSignalDialog dialog = new FindSignalDialog(fWindow.getShell(), zPrj);
		dialog.setTitle("Find Signal...");

		dialog.open();
		Object sel[] = dialog.getResult();
		if (sel == null || sel.length < 1)
			return;

		ToplevelPath tlp = (ToplevelPath) sel[0];

		ZamiaPlugin.showSource(fWindow.getActivePage(), zPrj, tlp);

	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow aWindow) {
		fWindow = aWindow;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
