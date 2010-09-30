/*
 * Copyright 2008,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author guenter bartsch
 *
 */

public class AskQuestionJob implements Runnable {
	
	private Shell fShell;
	private String fTitle;
	private String fMsg;
	private int fStyle;
	private int fRC;

	public AskQuestionJob(Shell aShell, String aTitle, String aMsg, int aStyle) {
		fShell = aShell;
		fTitle = aTitle;
		fMsg = aMsg;
		fStyle = aStyle;
	}

	public void run() {
	
		Shell shell = fShell;
		if (shell == null) {
			shell = ZamiaPlugin.getShell();
		}
		
		MessageBox messageBox = new MessageBox(shell, fStyle); 
		messageBox.setMessage(fMsg);
		messageBox.setText(fTitle);
		fRC = messageBox.open(); 
	}
	
	public int getRC() {
		return fRC;
	}
	
}

