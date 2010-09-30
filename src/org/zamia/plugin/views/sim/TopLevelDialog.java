/*
 * Copyright 2006-2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.views.sim;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TopLevelDialog extends ElementTreeSelectionDialog {
	
	public TopLevelDialog(Shell aParent, IProject aProject) {
		super (aParent, new TopLevelLabelProvider(aParent), new TopLevelContentProvider(aProject));
		setAllowMultiple(false);
	}

}
