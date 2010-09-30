/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl.symbols;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPortModule;

/**
 * @author guenter bartsch
 */

public abstract class PortSymbol extends Symbol {

	protected int width;
	protected int height;
	protected Position portPos;

	public PortSymbol(RTLModule module_, RTLView control_) {

		GC gc = new GC(control_.getViewSite().getShell());
		
		gc.setFont(control_.getBigFont());

		Point p = gc.textExtent(module_.getInstanceName());
		width = p.x + 20 + p.y;
		height = p.y + 8;
		
		gc.dispose();
	}

	public RTLPort getPort(RTLModule gate) {
		if (!(gate instanceof RTLPortModule))
			return null;
		RTLPortModule pg = (RTLPortModule) gate;
		RTLPort p = pg.getInternalPort();
		return p;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Position getPortPosition(RTLPort p) {
		return portPos;
	}

}
