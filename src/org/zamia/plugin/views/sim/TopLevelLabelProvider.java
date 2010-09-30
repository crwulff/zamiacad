/*
 * Copyright 2006-2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.views.sim;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.zamia.Toplevel;
import org.zamia.plugin.ZamiaPlugin;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class TopLevelLabelProvider extends LabelProvider {

	private Image fArchIcon;

	public TopLevelLabelProvider(Composite parent) {
		fArchIcon = ZamiaPlugin.getImage("/share/images/arch.gif");
	}

	public Image getImage(Object element) {
		if (element instanceof Toplevel) {
			return fArchIcon;
		}
		return null;
	}

	public String getText(Object element) {

		if (element instanceof Toplevel) {
			return ((Toplevel) element).toString();
		}
		return "???";
	}
}
