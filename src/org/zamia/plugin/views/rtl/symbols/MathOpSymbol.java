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
import java.util.HashMap;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.zamia.plugin.views.rtl.PSUtils;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLOperationMath;
import org.zamia.rtl.RTLPort;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class MathOpSymbol extends Symbol {

	private int width, height;
	//private Position APos, BPos, QPos;
	private HashMap<String,Position> portPositions;

	public MathOpSymbol() {
		super();
		width = 120;
		height = 160;
		
		portPositions = new HashMap<String,Position>();
		portPositions.put ("A",new Position (0,20));
		portPositions.put ("B",new Position (0,140));
		portPositions.put ("Z",new Position (100,80));
	}

	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}

	public Position getPortPosition(RTLPort p) {
		return (Position) portPositions.get(p.getId());
	}

	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos, int ypos, boolean hilight) {

		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			double w = 4 * v_.getZoomFactor();
			gc.setLineWidth((int) (w));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}
		
		gc.drawLine(tX(v_,  20,xpos),tY(v_,  0,ypos),tX(v_, 20,xpos),tY(v_, 40,ypos));
		gc.drawLine(tX(v_,  20,xpos),tY(v_,120,ypos),tX(v_, 20,xpos),tY(v_,160,ypos));
		gc.drawLine(tX(v_, 100,xpos),tY(v_, 60,ypos),tX(v_,100,xpos),tY(v_,100,ypos));
		gc.drawLine(tX(v_,  40,xpos),tY(v_, 60,ypos),tX(v_, 40,xpos),tY(v_,100,ypos));
		gc.drawLine(tX(v_,  20,xpos),tY(v_, 40,ypos),tX(v_, 40,xpos),tY(v_, 60,ypos));
		gc.drawLine(tX(v_,  20,xpos),tY(v_,120,ypos),tX(v_, 40,xpos),tY(v_,100,ypos));
		gc.drawLine(tX(v_,  20,xpos),tY(v_,  0,ypos),tX(v_,100,xpos),tY(v_, 60,ypos));
		gc.drawLine(tX(v_,  20,xpos),tY(v_, 160,ypos),tX(v_,100,xpos),tY(v_,100,ypos));

		gc.setForeground(v_.getColorScheme().getModuleLabelColor());
		gc.setLineWidth((int) (4 * v_.getZoomFactor()));

		gc.drawLine(tX(v_,  0,xpos),tY(v_, 20,ypos),tX(v_, 20,xpos),tY(v_, 20,ypos));
		gc.drawLine(tX(v_,  0,xpos),tY(v_,140,ypos),tX(v_, 20,xpos),tY(v_,140,ypos));
		gc.drawLine(tX(v_,100,xpos),tY(v_, 80,ypos),tX(v_,120,xpos),tY(v_, 80,ypos));
		
		String sym="";
		RTLOperationMath mathOp = (RTLOperationMath) module_;
		switch (mathOp.getOp()) {
			case NEG:
				sym = "-1";
				break;
			case ADD:
				sym = "+";
				break;
			case SUB:
				sym = "-";
				break;
			case MUL:
				sym = "*";
				break;
			case DIV:
				sym = "/";
				break;
			case REM:
				sym = "%";
				break;
			case POWER:
				sym = "**";
				break;
			case ABS:
				sym = "||";
				break;
			default:
				sym = "?"+mathOp.getOp();
				break;
		}
		Font oldfont = gc.getFont();
		int fontSize = tW(v_, 28.0);

		if (fontSize < 2)
			fontSize = 2;
		
		Font font = new Font(v_.getDisplay(), "Sans", fontSize, SWT.BOLD);
		gc.setFont(font);
		
		gc.drawText(sym, tX(v_, 50, xpos), tY(v_, 55,ypos));
//		int ww = gc.textExtent(label).x;
//		gc.drawText(label, tX(v_, getWidth()/2, xpos)-ww/2, tY(v_, getHeight()/2, ypos) - fontSize, true);

		gc.setFont(oldfont);
		font.dispose();

	}
	
	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos, int ypos, boolean selected_) throws IOException {

		double w = 2.0;

		PSUtils.psDrawLine(par_, out_, 20+xpos, ypos, 20+xpos, 40+ypos, w);
		PSUtils.psDrawLine(par_, out_, 20+xpos, 120+ypos, 20+xpos, 160+ypos, w);
		PSUtils.psDrawLine(par_, out_, 100+xpos, 60+ypos, 100+xpos, 100+ypos, w);
		PSUtils.psDrawLine(par_, out_, 40+xpos, 60+ypos, 40+xpos, 100+ypos, w);
		
//		gc.drawLine(tX(v_,  40,xpos),tY(v_, 60,ypos),tX(v_, 40,xpos),tY(v_,100,ypos));
//		gc.drawLine(tX(v_,  20,xpos),tY(v_, 40,ypos),tX(v_, 40,xpos),tY(v_, 60,ypos));
//		gc.drawLine(tX(v_,  20,xpos),tY(v_,120,ypos),tX(v_, 40,xpos),tY(v_,100,ypos));
//		gc.drawLine(tX(v_,  20,xpos),tY(v_,  0,ypos),tX(v_,100,xpos),tY(v_, 60,ypos));
//		gc.drawLine(tX(v_,  20,xpos),tY(v_, 160,ypos),tX(v_,100,xpos),tY(v_,100,ypos));
//
//		w = 4.0;
//
//		gc.drawLine(tX(v_,  0,xpos),tY(v_, 20,ypos),tX(v_, 20,xpos),tY(v_, 20,ypos));
//		gc.drawLine(tX(v_,  0,xpos),tY(v_,140,ypos),tX(v_, 20,xpos),tY(v_,140,ypos));
//		gc.drawLine(tX(v_,100,xpos),tY(v_, 80,ypos),tX(v_,120,xpos),tY(v_, 80,ypos));
//		
//		String sym="";
//		RTLOperationMath mathOp = (RTLOperationMath) module_;
//		switch (mathOp.getOp()) {
//			case NEG:
//				sym = "-1";
//				break;
//			case ADD:
//				sym = "+";
//				break;
//			case SUB:
//				sym = "-";
//				break;
//			case MUL:
//				sym = "*";
//				break;
//			case DIV:
//				sym = "/";
//				break;
//			case REM:
//				sym = "%";
//				break;
//			case POWER:
//				sym = "**";
//				break;
//			case ABS:
//				sym = "||";
//				break;
//			default:
//				sym = "?"+mathOp.getOp();
//				break;
//		}
//		Font oldfont = gc.getFont();
//		int fontSize = tW(v_, 28.0);
//
//		if (fontSize < 2)
//			fontSize = 2;
//		
//		Font font = new Font(v_.getDisplay(), "Sans", fontSize, SWT.BOLD);
//		gc.setFont(font);
//		
//		gc.drawText(sym, tX(v_, 50, xpos), tY(v_, 55,ypos));
////		int ww = gc.textExtent(label).x;
////		gc.drawText(label, tX(v_, getWidth()/2, xpos)-ww/2, tY(v_, getHeight()/2, ypos) - fontSize, true);
//
//		gc.setFont(oldfont);
//		font.dispose();
	}
}
