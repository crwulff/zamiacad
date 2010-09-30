/*
 * Copyright 2004-2007 by the authors indicated in the @author tags.
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
import org.zamia.rtl.RTLPort;

/**
 * @author guenter bartsch
 */

public abstract class Symbol {
	
	abstract public int getWidth();

	abstract public int getHeight();

	abstract public Position getPortPosition(RTLPort p);

	abstract public void paint(RTLView viewer_, GC gc, RTLModule module_, int xpos,
			int ypos, boolean hilight);
	
	abstract public void print (PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos,
			int ypos, boolean selected_) throws IOException ;

	// utility functions
	
	protected int tX(RTLView viewer, double i, double xpos) {
		return viewer.tX(xpos + i);
	}
	protected int tY(RTLView viewer, double i, double ypos) {
		return viewer.tY(ypos + i);
	}
	protected int tW(RTLView viewer, double x) {
		return viewer.tW(x);
	}
	protected int tH(RTLView viewer, double y) {
		return viewer.tH(y);
	}

	public String isPortHit(int mx, int my, int xpos, int ypos) {
		return null;
	}
	
//	protected int tXPrint(double i, double xpos) {
//		return viewer.tXPrint(xpos + i);
//	}
//	protected int tYPrint(double i, double ypos) {
//		return viewer.tYPrint(ypos + i);
//	}
//	protected int tWPrint(double x) {
//		return viewer.tWPrint(x);
//	}
//	protected int tHPrint(double y) {
//		return viewer.tHPrint(y);
//	}
}
