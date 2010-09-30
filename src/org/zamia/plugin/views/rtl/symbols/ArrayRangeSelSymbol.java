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
import org.zamia.rtl.RTLArrayRangeSel;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;


/**
 * 
 * @author guenter bartsch
 * 
 */
public class ArrayRangeSelSymbol extends Symbol {

	public static final int PORT_DISTANCE = 40;

	private HashMap<String, Position> portPositions;

	public final static int HEIGHT = 40;

	public final static int WIDTH = 200;

	public ArrayRangeSelSymbol() {
		portPositions = new HashMap<String, Position>();

		portPositions.put(RTLPort.a_str, new Position(0, 10));
		portPositions.put(RTLPort.z_str, new Position(WIDTH, HEIGHT - 10));
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
	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos, int ypos, boolean hilight) {

		gc.setFont(v_.getSmallFont());

		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			gc.setLineWidth((int) (4 * v_.getZoomFactor()));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}
		int th = gc.getFontMetrics().getHeight();

		RTLArrayRangeSel ars = (RTLArrayRangeSel) module_;

		int left = ars.getLeft();
		int right = ars.getRight();
		boolean ascending = ars.isAscending();

		String ascLabel = ascending ? " to " : " downto ";

		gc.drawText("[" + left + ascLabel + right + "]", tX(v_, 15, xpos), tY(v_, HEIGHT-10, ypos) - th, true);
		gc.drawLine(tX(v_, 10, xpos), tY(v_, HEIGHT-10, ypos), tX(v_, WIDTH, xpos), tY(v_, HEIGHT-10, ypos));

		gc.drawLine(tX(v_, 10, xpos), tY(v_, 0, ypos), tX(v_, 10, xpos), tY(v_, HEIGHT, ypos));

		gc.drawLine(tX(v_, 0, xpos), tY(v_, 10, ypos), tX(v_, 10, xpos), tY(v_, 10, ypos));
	}

	@Override
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos, int ypos, boolean selected_) throws IOException {
		// TODO Auto-generated method stub

	}

}
