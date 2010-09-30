/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 29, 2007
 */

package org.zamia.plugin.views.rtl.symbols;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.eclipse.swt.graphics.GC;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLArrayCSel;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;


/**
 * 
 * @author guenter bartsch
 *
 */
public class ArrayCSelSymbol extends Symbol {

	public static final int PORT_DISTANCE = 40;
	private HashMap<RTLPort, Position> portPositions;

	private int width, height;
	private RTLArrayCSel csel;
	
	public ArrayCSelSymbol(RTLArrayCSel csel_) {
		csel = csel_;
		
		calcPortPositions();
	}

	private void calcPortPositions() {
		portPositions = new HashMap<RTLPort, Position>();
		width = 60;
		
		int n = csel.getNumOutputs();
		
		height = Math.max(n,1) * PORT_DISTANCE + 6;
		
		int yi = PORT_DISTANCE ; // current input port position
		for (int i = 0; i < n; i++) {
			RTLPort p = csel.getOutput(i);
			portPositions.put(p, new Position(width, yi));
			yi += PORT_DISTANCE;
		}
		
		portPositions.put(csel.getD(), new Position(0, height/2-PORT_DISTANCE/2));
	}

	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Position getPortPosition(RTLPort p) {
		return portPositions.get(p);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos,
			int ypos, boolean hilight) {
		
		gc.setFont(v_.getSmallFont());
		
		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			gc.setLineWidth((int) (4 * v_.getZoomFactor()));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}
		int th = gc.getFontMetrics().getHeight();
		

		int n = csel.getNumOutputs();
		
		int yi = PORT_DISTANCE ; // current output port position
		for (int i = 0; i < n; i++) {
			int offset = csel.getOutputOffset(i);

			gc.drawText("["+offset+"]", tX(v_, 15, xpos), tY(v_, yi, ypos) - th, true);
			gc.drawLine(tX(v_, 10, xpos), tY(v_, yi, ypos), tX(v_, width, xpos), tY(v_, yi, ypos));
			
			yi += PORT_DISTANCE;
		}

		gc.drawLine(tX(v_, 10, xpos), tY(v_, 0, ypos), tX(v_, 10, xpos), tY(v_, height, ypos));
		
		int y= height/2-PORT_DISTANCE/2;
		gc.drawLine(tX(v_, 0, xpos), tY(v_, y, ypos), tX(v_, 10, xpos), tY(v_, y, ypos));
	}

	@Override
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_,
			int xpos, int ypos, boolean selected_) throws IOException {
		// TODO Auto-generated method stub

	}

}
