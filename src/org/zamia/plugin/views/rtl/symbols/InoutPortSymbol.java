/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.rtl.symbols;

import java.io.IOException;
import java.io.PrintWriter;


import org.eclipse.swt.graphics.GC;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLModule;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class InoutPortSymbol extends PortSymbol {

	public InoutPortSymbol(RTLModule module_, RTLView control_) {
		super (module_, control_);
		width += height; // other arrow
		portPos = new Position(width, getHeight()/2);
	}

	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos, int ypos,
			boolean hilight) {
		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			gc.setLineWidth((int) (8 * v_.getZoomFactor()));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}

		int h = getHeight();

		gc.drawLine(tX(v_, h, xpos), tY(v_, 0, ypos), tX(v_, getWidth() - h, xpos), tY(v_, 0,
				ypos));
		gc.drawLine(tX(v_, 0, xpos), tY(v_, h/2, ypos), tX(v_, h, xpos), tY(v_, h, ypos));
		gc.drawLine(tX(v_, 0, xpos), tY(v_, h/2, ypos), tX(v_, h, xpos), tY(v_, 0, ypos));
		gc.drawLine(tX(v_, h, xpos), tY(v_, h, ypos), tX(v_, getWidth() - h, xpos), tY(v_, 
				h, ypos));
		gc.drawLine(tX(v_, getWidth() - h, xpos), tY(v_, 0, ypos),
				tX(v_, getWidth(), xpos), tY(v_, h/2, ypos));
		gc.drawLine(tX(v_, getWidth() - h, xpos), tY(v_, h, ypos), tX(v_, getWidth(),
				xpos), tY(v_, h/2, ypos));

		if (!hilight)
			gc.setForeground(v_.getColorScheme().getModuleLabelColor());
		gc.drawText(module_.getInstanceName(), tX(v_, h+5, xpos), tY(v_, 2, ypos), true);
	}
	
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos, int ypos, boolean selected_) throws IOException {
		// FIXME: implement
		
		System.err.println("FIXME: InoutPortSymbol.print not implemented yet.");
	}
}