/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 12, 2010
 */
package org.zamia.rtl.symbols;

import java.util.HashMap;
import java.util.HashSet;

import org.zamia.rtl.RTLNode;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.nodes.RTLNBinaryOp;
import org.zamia.rtl.nodes.RTLNBinaryOp.BinaryOp;
import org.zamia.util.MutablePosition;
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

	private HashMap<RTLPort, MutablePosition> fPortPositions;

	private HashSet<RTLPort> fTweakedPorts;

	public RTLSymbolBinaryLogic(RTLNBinaryOp aOp, VGGC aGC) {

		fTweakedPorts = new HashSet<RTLPort>(3);
		fPortPositions = new HashMap<RTLPort, MutablePosition>(3);

		fPortPositions.put(aOp.getA(), new MutablePosition(0, PAY));
		fPortPositions.put(aOp.getB(), new MutablePosition(0, PBY));
		fPortPositions.put(aOp.getZ(), new MutablePosition(WIDTH, HEIGHT / 2));

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

		MutablePosition mp = fPortPositions.get(aPort);
		return new Position(mp.getX(), mp.getY());
	}

	@Override
	public void tweakPortPosition(RTLPort aPort) {
		if (fTweakedPorts.contains(aPort)) {
			return;
		}

		fTweakedPorts.add(aPort);

		MutablePosition pos = fPortPositions.get(aPort);

		pos.setY(pos.getY() + 2);
	}

	@Override
	public void unTweakPortPosition(RTLPort aPort) {

		if (!fTweakedPorts.contains(aPort)) {
			return;
		}

		fTweakedPorts.remove(aPort);

		MutablePosition pos = fPortPositions.get(aPort);

		pos.setY(pos.getY() - 2);
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

		MutablePosition pos = fPortPositions.get(aModule.findPort("A"));
		fGC.drawLine(aXPos, aYPos + pos.getY(), aXPos + XMAR, aYPos + pos.getY());

		pos = fPortPositions.get(aModule.findPort("B"));
		fGC.drawLine(aXPos, aYPos + pos.getY(), aXPos + XMAR, aYPos + pos.getY());

		fGC.drawRectangle(aXPos + XMAR, aYPos + 1, getWidth() - 2 * XMAR, getHeight() - 2);

		pos = fPortPositions.get(aModule.findPort("Z"));
		if (fNeg) {
			fGC.drawOval(aXPos + getWidth() - XMAR + 3, aYPos + pos.getY(), 3, 3);
			fGC.drawLine(aXPos + getWidth() - XMAR + 6, aYPos + pos.getY(), aXPos + getWidth(), aYPos + pos.getY());
		} else {
			fGC.drawLine(aXPos + getWidth() - XMAR, aYPos + pos.getY(), aXPos + getWidth(), aYPos + pos.getY());
		}
	}

}
