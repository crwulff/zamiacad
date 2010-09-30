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
import org.eclipse.swt.graphics.Point;
import org.zamia.plugin.views.rtl.PSUtils;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class LiteralSymbol extends Symbol {

	public final static int SCALE = 8;

	private static final int LENGTH_MAX = 12;
	
	private int width, height;
	private Position qPos;

	public LiteralSymbol(RTLLiteral module_, RTLView control_) {
		super ();
		String l = getLabel(module_);

		double z = control_.getZoomFactor();
		
		GC gc = new GC(control_.getViewSite().getShell());
		
		gc.setFont(control_.getNormalFont());
		
		Point te = gc.textExtent(l);
		
		width = (int)(te.x / z) + 20;
		height = (int)(te.y / z) + 2;

		gc.dispose();
		
		qPos = new Position (width,height/2);
	}

	private String getLabel(RTLLiteral l) {
		String v = l.getValue().toHRString();
		if (v.length()>LENGTH_MAX) {
			v = v.substring(0, LENGTH_MAX-4)+"...";
		}
		return v;
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}

	public Position getPortPosition(RTLPort p) {
		return qPos;
	}

	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos, int ypos, boolean hilight) {

		gc.setFont(v_.getNormalFont());

//		gc.setXORMode(true);
//		gc.setBackground(v_.getColorScheme().getHilightColor());
//		gc.fillRectangle(tX(v_, 0, xpos), tY(v_, 0, ypos), tW(v_, getWidth()),
//				tH(v_, getHeight()));
//		gc.setXORMode(false);

		
		if (hilight) {
			gc.setLineWidth((int) (2*v_.getZoomFactor()));
			gc.setForeground(v_.getColorScheme().getHilightColor());
		} else {
			gc.setLineWidth((int) (2*v_.getZoomFactor()));
			gc.setForeground(v_.getColorScheme().getModuleLabelColor());
		}
		
		RTLLiteral l = (RTLLiteral) module_;
		String v = getLabel(l);
		
		gc.drawLine(tX(v_, width-5,xpos),tY(v_, height/2,ypos),tX(v_, width,xpos),tY(v_, height/2,ypos));

		gc.drawText(v, tX(v_, 0, xpos), tY(v_, 2,ypos));
	}
	
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos, int ypos, boolean selected_) throws IOException {
		RTLLiteral l = (RTLLiteral) module_;
		String str = l.getValue().toString();

		PSUtils.psDrawLine(par_, out_, 45+xpos,6+ypos,50+xpos,6+ypos,1.0);
		PSUtils.psDrawTextRight (par_, out_, 45+xpos, 2+ypos, str, 10.0);
	}
}
