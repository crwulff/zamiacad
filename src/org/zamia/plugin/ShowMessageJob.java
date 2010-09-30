/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author guenter bartsch
 *
 */

public class ShowMessageJob implements Runnable {

	private Shell fShell;

	private String fTitle;

	private String fMsg;

	private String fReason;

	private boolean fIsError;

	public ShowMessageJob(Shell aShell, String aTitle, String aMsg, String aReason, boolean aIsError) {
		fShell = aShell;
		fTitle = aTitle;
		fMsg = aMsg;
		fReason = aReason;
		fIsError = aIsError;
	}

	public ShowMessageJob(Shell aShell, String aTitle, String aMsg, String aReason) {
		this(aShell, aTitle, aMsg, aReason, true);
	}

	public void run() {
		Shell shell = fShell;
		if (shell == null) {
			shell = ZamiaPlugin.getShell();
		}

		Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, fReason, null);
		if (fIsError) {
			ErrorDialog.openError(shell, fTitle, fMsg, err);
		} else {
			MessageDialog.openInformation(shell, fTitle, fMsg);
		}
	}

}
