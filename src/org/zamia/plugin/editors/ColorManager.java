/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * A singleton, color management/registry.
 * 
 * 
 * @author guenter bartsch
 *
 */

public class ColorManager {

	protected HashMap<RGB,Color> fColorTable = new HashMap<RGB,Color>(10);

	private static ColorManager instance;
	
	private ColorManager() {
		
	}
	
	public static ColorManager getInstance() {
		if (instance == null)
			instance = new ColorManager();
		return instance;
	}
	
	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
