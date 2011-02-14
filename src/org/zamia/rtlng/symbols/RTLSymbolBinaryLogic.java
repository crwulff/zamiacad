/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 12, 2010
 */
package org.zamia.rtlng.symbols;

import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.nodes.RTLNBinaryOp;
import org.zamia.rtlng.nodes.RTLNBinaryOp.BinaryOp;
import org.zamia.util.Position;
import org.zamia.vg.VGGC;
import org.zamia.vg.VGGC.VGColor;
import org.zamia.vg.VGGC.VGFont;
import org.zamia.vg.VGSymbol;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLSymbolBinaryLogic implements VGSymbol<RTLNode, RTLPort, RTLSignal> {

	private final static int WIDTH = 60;

	private final static int HEIGHT = 30;

	private final static int PAY = 10;

	private final static int PBY = 20;
	
	private final static int XMAR = 10;

	private final String fLabel;

	private final boolean fNeg;

	private final VGGC fGC;

	public RTLSymbolBinaryLogic(RTLNBinaryOp aOp, VGGC aGC) {

		BinaryOp op = aOp.getOp();

		switch (op) {
		case AND:
			fLabel = "&";
			fNeg = false;
			break;
		case NAND:
			fLabel = "&";
			fNeg = true;
			break;
		case OR:
			fLabel = "≥1";
			fNeg = false;
			break;
		case NOR:
			fLabel = "≥1";
			fNeg = true;
			break;
		case XOR:
			fLabel = "=1";
			fNeg = false;
			break;
		case XNOR:
			fLabel = "=1";
			fNeg = true;
			break;
		default:
			fLabel = "???";
			fNeg = false;
		}

		fGC = aGC;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public Position getPortPosition(RTLPort aPort) {

		String id = aPort.getId();
		if (id.equals("A")) {
			return new Position(0, PAY);
		} else if (id.equals("B")) {
			return new Position(0, PBY);
		}

		return new Position(getWidth(), getHeight() / 2);
	}

	@Override
	public void tweakPortPosition(RTLPort aPort) {
	}

	@Override
	public void unTweakPortPosition(RTLPort aPort) {
	}

	@Override
	public void paint(RTLNode aModule, int aXPos, int aYPos, boolean aHilight) {
		fGC.setFont(VGFont.LARGE);
		fGC.setForeground(VGColor.MODULE_LABEL);
		fGC.setLineWidth(2);

		int th = fGC.getFontHeight();
		int tw = fGC.textWidth(fLabel);

		fGC.drawText(fLabel, aXPos + getWidth() / 2 - tw / 2, aYPos + getHeight() / 2 + th / 2, true);

		fGC.drawLine(aXPos, aYPos + PAY, aXPos + XMAR, aYPos + PAY);
		fGC.drawLine(aXPos, aYPos + PBY, aXPos + XMAR, aYPos + PBY);

		fGC.drawRectangle(aXPos + XMAR, aYPos + 1, getWidth() - 2*XMAR, getHeight() - 2);

		if (fNeg) {
			fGC.drawOval(aXPos + getWidth() - XMAR + 3, aYPos + getHeight() / 2, 3, 3);
			fGC.drawLine(aXPos + getWidth() - XMAR + 6, aYPos + getHeight() / 2, aXPos + getWidth(), aYPos + getHeight() / 2);
		} else {
			fGC.drawLine(aXPos + getWidth() - XMAR, aYPos + getHeight() / 2, aXPos + getWidth(), aYPos + getHeight() / 2);
		}
	}

}
