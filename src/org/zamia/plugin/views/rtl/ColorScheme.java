/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 4, 2006
 */

package org.zamia.plugin.views.rtl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public abstract class ColorScheme {

	protected Color black;
	protected Color blue;
	protected Color white;
	protected Color green;
	protected Color yellow;
	protected Color red;
	protected Color gray;

	public ColorScheme(Display display_) {
		black = display_.getSystemColor(SWT.COLOR_BLACK);
		blue = display_.getSystemColor(SWT.COLOR_BLUE);
		white = display_.getSystemColor(SWT.COLOR_WHITE);
		green = display_.getSystemColor(SWT.COLOR_DARK_GREEN);
		yellow = display_.getSystemColor(SWT.COLOR_YELLOW);
		red = display_.getSystemColor(SWT.COLOR_RED);
		gray = display_.getSystemColor(SWT.COLOR_GRAY);
	}

	public abstract Color getBgColor();
	public abstract Color getSignalColor();
	public abstract Color getPinColor();
	public abstract Color getModuleColor();
	public abstract Color getSignalLabelColor();
	public abstract Color getModuleLabelColor();
	public abstract Color getHilightColor();
	public abstract Color getAnnotationColor();
	public abstract Color getShadowColor();
}
