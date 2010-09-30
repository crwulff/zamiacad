/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.rtl;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author guenter bartsch
 */
public class ProcessDialog extends ElementListSelectionDialog {

	public ProcessDialog (Shell parent_) {
		super (parent_, new LabelProvider() {
			
		});
		setElements ( new String[] {"Flatten", "Dissolve", "Constant Propagation", "Remove FlipFlops", "Design Rule Fix"});
		setMultipleSelection(true);
	}
}
