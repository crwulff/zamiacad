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
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;


/**
 * 
 * @author guenter bartsch
 *
 */
public class CESymbol extends Symbol {

	public final static int WIDTH = 33;
	public final static int HEIGHT = 33;
	
	private HashMap<String,Position> portPositions;
	
	public CESymbol() {
		portPositions = new HashMap<String,Position>();
		portPositions.put (RTLPort.e_str,new Position (0,HEIGHT/2));
		portPositions.put (RTLPort.ze_str,new Position (WIDTH,HEIGHT/2));
	}

	
	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public Position getPortPosition(RTLPort p) {
		return portPositions.get(p.getId());
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos,
			int ypos, boolean hilight) {
		
		gc.setFont(v_.getSmallFont());
		
		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			gc.setBackground(v_.getColorScheme().getHilightColor());
			gc.setLineWidth((int) (4 * v_.getZoomFactor()));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setBackground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}

		gc.drawLine(tX(v_, 0, xpos), tY(v_, HEIGHT/2, ypos), tX(v_, WIDTH, xpos), tY(v_, HEIGHT/2, ypos));
		
		int off = WIDTH/4;
		
		gc.fillOval(tX(v_, off, xpos), tY(v_, off, ypos), tW(v_, 2*off), tH(v_, 2*off));
	
	}

	@Override
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_,
			int xpos, int ypos, boolean selected_) throws IOException {
		// TODO Auto-generated method stub

	}

}
