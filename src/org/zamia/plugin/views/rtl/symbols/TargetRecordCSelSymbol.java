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
import org.eclipse.swt.graphics.Point;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLTargetRecordCSel;


/**
 * 
 * @author guenter bartsch
 *
 */
public class TargetRecordCSelSymbol extends Symbol {

	public static final int PORT_DISTANCE = 40;
	private HashMap<RTLPort, Position> portPositions;

	private int width, height;
	private RTLTargetRecordCSel csel;
	
	public TargetRecordCSelSymbol(RTLTargetRecordCSel csel_, RTLView control_) {
		csel = csel_;
		
		calcPortPositions(control_);
	}

	private void calcPortPositions(RTLView control_) {
		portPositions = new HashMap<RTLPort, Position>();
		
		double z = control_.getZoomFactor();

		GC gc = new GC(control_.getViewSite().getShell());
		
		gc.setFont(control_.getSmallFont());
		
		width = 40;
		
		int n = csel.getNumInputs();
		
		height = Math.max(n,2) * PORT_DISTANCE + 6;
		
		int yi = (height - n * PORT_DISTANCE)/2+3 ; // current input port position
		for (int i = 0; i < csel.getNumInputs(); i++) {
			RTLPort p = csel.getInput(i);
			portPositions.put(p, new Position(0, yi));

			String l = csel.getInputId(i);
			Point te = gc.textExtent(l);
			
			int w = (int)(te.x / z) + 40;
			if (width<w) {
				width = w;
			}
			
			
			yi += PORT_DISTANCE;
		}

		gc.dispose();
		
		portPositions.put(csel.getZ(), new Position(width, height/2-PORT_DISTANCE/2));
		portPositions.put(csel.getZE(), new Position(width, height/2+PORT_DISTANCE/2));
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
		
		int n = csel.getNumInputs();
		
		int yi = (height - n * PORT_DISTANCE)/2+3 ; // current input port position
		for (int i = 0; i < csel.getNumInputs(); i++) {
			
			String id = csel.getInputId(i);

			gc.drawText("."+id, tX(v_, 10, xpos), tY(v_, yi, ypos) - th, true);
			gc.drawLine(tX(v_, 0, xpos), tY(v_, yi, ypos), tX(v_, width-20, xpos), tY(v_, yi, ypos));
			
			yi += PORT_DISTANCE;
		}

		gc.drawLine(tX(v_, width-20, xpos), tY(v_, 0, ypos), tX(v_, width-20, xpos), tY(v_, height, ypos));
		int y= height/2-PORT_DISTANCE/2;
		gc.drawLine(tX(v_, width-20, xpos), tY(v_, y, ypos), tX(v_, width, xpos), tY(v_, y, ypos));
		y+=PORT_DISTANCE;
		gc.drawLine(tX(v_, width-20, xpos), tY(v_, y, ypos), tX(v_, width, xpos), tY(v_, y, ypos));
	}

	@Override
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_,
			int xpos, int ypos, boolean selected_) throws IOException {
		// TODO Auto-generated method stub

	}

}
