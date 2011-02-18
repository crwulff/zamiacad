/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 12, 2010
 */
package org.zamia.rtl.symbols;

import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.nodes.RTLNUnaryOp;
import org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp;
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

public class RTLSymbolUnaryLogic implements VGSymbol<RTLNode, RTLPort, RTLSignal> {

	private final static int WIDTH = 40;

	private final static int HEIGHT = 20;

	private final static int XMAR = 10;

	private final String fLabel;

	private final boolean fNeg;

	private final VGGC fGC;

	public RTLSymbolUnaryLogic(RTLNUnaryOp aOp, VGGC aGC) {

		UnaryOp op = aOp.getOp();

		switch (op) {
		case NOT:
			fLabel = "";
			fNeg = false;
			break;
		case BUF:
			fLabel = "";
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
			return new Position(0, getHeight() / 2);
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

		if (aHilight) {
			fGC.setForeground(VGColor.HIGHLIGHT);
		} else {
			fGC.setForeground(VGColor.MODULE_LABEL);
		}

		fGC.setLineWidth(2);

		int th = fGC.getFontHeight();
		int tw = fGC.textWidth(fLabel);

		fGC.drawText(fLabel, aXPos + getWidth() / 2 - tw / 2, aYPos + getHeight() / 2 + th / 2, true);

		if (aHilight) {
			fGC.setForeground(VGColor.HIGHLIGHT);
		} else {
			fGC.setForeground(VGColor.MODULE);
		}

		fGC.drawLine(aXPos, aYPos + getHeight() / 2, aXPos + XMAR, aYPos + getHeight() / 2);

		fGC.drawRectangle(aXPos + XMAR, aYPos + 1, getWidth() - 2 * XMAR, getHeight() - 2);

		if (!fNeg) {
			fGC.drawOval(aXPos + getWidth() - XMAR + 3, aYPos + getHeight() / 2, 3, 3);
			fGC.drawLine(aXPos + getWidth() - XMAR + 6, aYPos + getHeight() / 2, aXPos + getWidth(), aYPos + getHeight() / 2);
		} else {
			fGC.drawLine(aXPos + getWidth() - XMAR, aYPos + getHeight() / 2, aXPos + getWidth(), aYPos + getHeight() / 2);
		}
	}

}
