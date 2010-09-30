/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 4, 2006
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ColorSchemeZamia extends ColorScheme {

	public ColorSchemeZamia (Display display_) {
		super (display_);
	}
	
	@Override
	public Color getBgColor() {
		return white;
	}

	@Override
	public Color getSignalColor() {
		return blue;
	}

	@Override
	public Color getPinColor() {
		return black;
	}

	@Override
	public Color getModuleColor() {
		return black;
	}

	@Override
	public Color getSignalLabelColor() {
		return blue;
	}

	@Override
	public Color getModuleLabelColor() {
		return blue;
	}

	@Override
	public Color getHilightColor() {
		return red;
	}

	@Override
	public Color getAnnotationColor() {
		return green;
	}

	@Override
	public Color getShadowColor() {
		return gray;
	}
}
