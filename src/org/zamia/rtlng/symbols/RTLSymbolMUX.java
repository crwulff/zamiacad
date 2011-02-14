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

public class RTLSymbolMUX implements VGSymbol<RTLNode, RTLPort, RTLSignal> {

	private final static int WIDTH = 40;

	private final static int HEIGHT = 60;

	private final static int PAY = 15;

	private final static int PBY = 35;

	private final static int PSY = 55;

	private final static int XMAR = 10;

	private final VGGC fGC;

	public RTLSymbolMUX(VGGC aGC) {
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
		} else if (id.equals("S")) {
			return new Position(0, PSY);
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

		// a and b ports
		
		fGC.drawLine(aXPos, aYPos + PAY, aXPos + XMAR, aYPos + PAY);
		fGC.drawLine(aXPos, aYPos + PBY, aXPos + XMAR, aYPos + PBY);

		// body
		
		fGC.drawLine(aXPos + XMAR, aYPos + PAY - 15, aXPos + XMAR, aYPos + PBY + 15);
		fGC.drawLine(aXPos + XMAR + 20, aYPos + PAY - 2, aXPos + XMAR + 20, aYPos + PBY + 2);
		fGC.drawLine(aXPos + XMAR, aYPos + PAY - 15, aXPos + XMAR + 20, aYPos + PAY - 2);
		fGC.drawLine(aXPos + XMAR, aYPos + PBY + 15, aXPos + XMAR + 20, aYPos + PBY + 2);

		// select port
		
		fGC.drawLine(aXPos, aYPos + PSY, aXPos + XMAR + 10, aYPos + PSY);
		fGC.drawLine(aXPos + XMAR + 10, aYPos + PSY, aXPos + XMAR + 10, aYPos + PSY - 12);

		// Z port
		
		fGC.drawLine(aXPos + getWidth() - XMAR, aYPos + getHeight() / 2, aXPos + getWidth(), aYPos + getHeight() / 2);
	}

}
