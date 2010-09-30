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
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class TraceLabelProvider extends LabelProvider {

	private Image fArchIcon;

	private Image fSignalIcon;

	public TraceLabelProvider() {
		fArchIcon = ZamiaPlugin.getImage("/share/images/arch.gif");
		fSignalIcon = ZamiaPlugin.getImage("/share/images/signal.gif");
	}

	public Image getImage(Object aElement) {
		if (aElement instanceof TraceDialogItem) {

			TraceDialogItem item = (TraceDialogItem) aElement;

			if (item.fIsModule)
				return fArchIcon;

			return fSignalIcon;
		}
		return null;
	}

	public String getText(Object element) {

		if (element instanceof TraceDialogItem) {

			TraceDialogItem item = (TraceDialogItem) element;

			PathName pn = item.fName;

			return pn.getSegment(pn.getNumSegments() - 1);
		}

		return "???";
	}
}
