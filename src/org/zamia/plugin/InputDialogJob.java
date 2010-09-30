/*
 * Copyright 2008,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author guenter bartsch
 *
 */

public class InputDialogJob implements Runnable {
	
	private Shell fShell;
	private final String fTitle;
	private final String fMsg;
	private final String fInitialValue;
	private String fValue;
	private int fRC;

	public InputDialogJob(Shell aShell, String aTitle, String aMsg, String aInitialValue) {
		fShell = aShell;
		fTitle = aTitle;
		fInitialValue = aInitialValue;
		fMsg = aMsg;
	}

	public void run() {
	
		Shell shell = fShell;
		if (shell == null) {
			shell = new Shell();
		}
		
		InputDialog dlg = new InputDialog(fShell, fTitle, fMsg, fInitialValue, null); 
		
		fRC = dlg.open();
		fValue = dlg.getValue();
	}
	
	public int getRC() {
		return fRC;
	}
	
	public String getValue() {
		return fValue;
	}
	
}

