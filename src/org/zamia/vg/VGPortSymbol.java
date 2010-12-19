/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2010
 */
package org.zamia.vg;

import org.zamia.util.Position;
import org.zamia.vg.VGGC.VGColor;
import org.zamia.vg.VGGC.VGFont;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class VGPortSymbol<NodeType, PortType, SignalType> implements VGSymbol<NodeType, PortType, SignalType> {

	private static final int BORDER_WIDTH = 10;

	private static final int ARROW_WIDTH = 20;

	private static final int PIN_WIDTH = 15;

	private final VGLayout<NodeType, PortType, SignalType> fLayout;

	private final VGGC fGC;

	private final String fLabel;

	private boolean fOutput;

	public VGPortSymbol(boolean aOutput, String aLabel, VGLayout<NodeType, PortType, SignalType> aLayout) {
		fLayout = aLayout;
		fGC = fLayout.getGC();
		fLabel = aLabel;
		fOutput = aOutput;
	}

	@Override
	public int getWidth() {
		fGC.setFont(VGFont.NORMAL);
		return fGC.textWidth(fLabel) + ARROW_WIDTH + 2 * BORDER_WIDTH + PIN_WIDTH;
	}

	@Override
	public int getHeight() {
		fGC.setFont(VGFont.NORMAL);
		return fGC.getFontHeight() * 2;
	}

	@Override
	public Position getPortPosition(PortType aPort) {

		if (fOutput) {
			return new Position(0, getHeight() / 2);

		}

		return new Position(getWidth(), getHeight() / 2);
	}

	@Override
	public void paint(NodeType aModule, int aXPos, int aYPos, boolean aHilight) {

		fGC.setFont(VGFont.NORMAL);

		if (aHilight) {
			fGC.setForeground(VGColor.HIGHLIGHT);
			fGC.setLineWidth(8);
		} else {
			fGC.setForeground(VGColor.MODULE);
			fGC.setLineWidth(2);
		}

		int h = getHeight();
		int w = getWidth();
		int th = fGC.getFontHeight();

		if (fOutput) {

			fGC.drawLine(aXPos + w, aYPos, PIN_WIDTH + ARROW_WIDTH + aXPos, aYPos);
			fGC.drawLine(aXPos + w, aYPos, aXPos + w, h + aYPos);
			fGC.drawLine(aXPos + w, h + aYPos, PIN_WIDTH + ARROW_WIDTH + aXPos, h + aYPos);

			fGC.drawLine(PIN_WIDTH + ARROW_WIDTH + aXPos, aYPos, aXPos + PIN_WIDTH, h / 2 + aYPos);
			fGC.drawLine(PIN_WIDTH + ARROW_WIDTH + aXPos, h + aYPos, aXPos + PIN_WIDTH, h / 2 + aYPos);

			fGC.drawLine(aXPos, h / 2 + aYPos, aXPos + PIN_WIDTH, h / 2 + aYPos);

		} else {

			fGC.drawLine(aXPos, aYPos, w - ARROW_WIDTH - PIN_WIDTH + aXPos, aYPos);
			fGC.drawLine(aXPos, aYPos, aXPos, h + aYPos);
			fGC.drawLine(aXPos, h + aYPos, w - ARROW_WIDTH - PIN_WIDTH + aXPos, h + aYPos);

			fGC.drawLine(w - ARROW_WIDTH - PIN_WIDTH + aXPos, aYPos, w - PIN_WIDTH + aXPos, h / 2 + aYPos);
			fGC.drawLine(w - ARROW_WIDTH - PIN_WIDTH + aXPos, h + aYPos, w - PIN_WIDTH + aXPos, h / 2 + aYPos);

			fGC.drawLine(aXPos + w - PIN_WIDTH, h / 2 + aYPos, aXPos + w, h / 2 + aYPos);
		}

		if (!aHilight) {
			fGC.setForeground(VGColor.MODULE_LABEL);
		}

		if (fOutput) {
			fGC.drawText(fLabel, ARROW_WIDTH + BORDER_WIDTH + PIN_WIDTH + aXPos, aYPos + h / 2 + th / 2, true);
		} else {
			fGC.drawText(fLabel, BORDER_WIDTH + aXPos, aYPos + h / 2 + th / 2, true);
		}
	}

	@Override
	public void tweakPortPosition(PortType aPort) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unTweakPortPosition(PortType aPort) {
		// TODO Auto-generated method stub
		
	}

}
