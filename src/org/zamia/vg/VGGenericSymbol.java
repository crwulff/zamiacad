/*
 * Copyright 2004-2011 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 * GenericSymbol.java created on 11.04.2004 by guenter
 */
package org.zamia.vg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.zamia.util.MutablePosition;
import org.zamia.util.Position;
import org.zamia.vg.VGGC.VGColor;
import org.zamia.vg.VGGC.VGFont;

/**
 * A generic symbol which represents any RTL module as a rectangular box,
 * Handles any number of input and output ports
 * 
 * 
 * @author Guenter Bartsch
 * 
 */
public class VGGenericSymbol<NodeType, PortType, SignalType> implements VGSymbol<NodeType, PortType, SignalType> {

	public final static int MIN_HEIGHT = 130;

	public final static int MIN_WIDTH = 30;

	private int fMinHeight = MIN_HEIGHT;

	private int fMinWidth = MIN_WIDTH;

	public static final int PORT_DISTANCE = 50;

	private int fPortDistance = PORT_DISTANCE;

	public static final int MAX_LABEL_LENGTH = 8;

	static class HintedPort<PortType> {
		PortType fPort;

		MutablePosition fPos;

		int fHint;

		public HintedPort(PortType aPort, MutablePosition aPosition) {
			fPort = aPort;
			fPos = aPosition;
		}
	}

	private final VGLabelProvider<NodeType, PortType, SignalType> fLabelProvider;

	private final VGGC fGC;

	private final String fLabel;

	private HashMap<VGPort<NodeType, PortType, SignalType>, MutablePosition> fPortPositions;

	private ArrayList<HintedPort<PortType>> fInputPorts;

	private ArrayList<HintedPort<PortType>> fOutputPorts;

	private HashMap<PortType, HintedPort<PortType>> fHintMap;

	private int fWidth, fHeight;

	private final VGLayout<NodeType, PortType, SignalType> fLayout;

	private final NodeType fNode;

	private final VGBox<NodeType, PortType, SignalType> fBox;

	private HashMap<PortType, VGPort<NodeType, PortType, SignalType>> fPortMap;

	private HashSet<PortType> fTweakedPorts;

	public VGGenericSymbol(NodeType aNode, VGLayout<NodeType, PortType, SignalType> aLayout, VGBox<NodeType, PortType, SignalType> aBox) {
		super();

		fPortDistance = PORT_DISTANCE;
		fMinHeight = MIN_HEIGHT;
		fMinWidth = MIN_WIDTH;

		fWidth = fMinWidth;
		fHeight = fMinHeight;

		fNode = aNode;
		fLayout = aLayout;

		fLabelProvider = fLayout.getLabelProvider();
		fBox = aBox;
		fGC = fLayout.getGC();
		fLabel = fLabelProvider.getNodeLabel(fNode);

		calcPortPositions();
	}

	public int getWidth() {
		return fWidth;
	}

	public int getHeight() {
		return fHeight;
	}

	private void calcPortPositions() {
		fPortPositions = new HashMap<VGPort<NodeType, PortType, SignalType>, MutablePosition>();
		fPortMap = new HashMap<PortType, VGPort<NodeType, PortType, SignalType>>();
		fInputPorts = new ArrayList<HintedPort<PortType>>();
		fOutputPorts = new ArrayList<HintedPort<PortType>>();
		fHintMap = new HashMap<PortType, HintedPort<PortType>>();
		fTweakedPorts = new HashSet<PortType>();

		int yi = fPortDistance * 1 / 4; // current input port position
		int yo = fPortDistance * 1 / 4; // current output port position
		int maxipw = 0;
		int maxopw = 0;

		fGC.setFont(VGFont.NORMAL);

		for (int i = 0; i < fBox.getNumPorts(); i++) {
			VGPort<NodeType, PortType, SignalType> p = fBox.getPort(i);
			PortType port = p.getPort();

			fPortMap.put(port, p);

			MutablePosition pos;
			String id = p.getLabel();
			int w = fGC.textWidth(id);
			HintedPort<PortType> hp;
			if (!p.isOutput()) {
				fPortPositions.put(p, pos = new MutablePosition(0, yi));
				yi += fPortDistance;
				hp = new HintedPort<PortType>(port, pos);
				fInputPorts.add(hp);
				if (w > maxipw)
					maxipw = w;
			} else {
				fPortPositions.put(p, pos = new MutablePosition(1, yo));
				yo += fPortDistance;
				hp = new HintedPort<PortType>(port, pos);
				fOutputPorts.add(hp);
				if (w > maxopw)
					maxopw = w;
			}
			fHintMap.put(port, hp);
		}

		fGC.setFont(VGFont.LARGE);

		int fontSize = fGC.getFontHeight();

		// System.out.println("Font size: "+fontSize);

		fHeight = Math.max(yi - fPortDistance / 2, yo - fPortDistance / 2) + fontSize;
		fWidth = fHeight * 100 / 162;

		// System.out.println ("Size of "+module.getInstanceName()+":
		// "+gc.textExtent(module.getInstanceName()));

		int l = fGC.textWidth(fLabel);

		l = Math.max(l, maxopw + maxipw + 2 * fPortDistance);

		l = l + fPortDistance;
		if (fWidth < l)
			fWidth = l;

		if (fWidth < fMinWidth)
			fWidth = fMinWidth;
		if (fHeight < fMinHeight)
			fHeight = fMinHeight;

		for (Iterator<MutablePosition> i = fPortPositions.values().iterator(); i.hasNext();) {
			MutablePosition pos = i.next();
			if (pos.getX() > 0) {
				pos.setX(fWidth - fPortDistance);
			}
		}
	}

	public void setPinHint(PortType aPort, int aHint) {
		HintedPort<PortType> hp = fHintMap.get(aPort);
		hp.fHint = aHint;
	}

	public void tweakPortPosition(PortType aPort) {

		if (fTweakedPorts.contains(aPort)) {
			return;
		}

		fTweakedPorts.add(aPort);

		MutablePosition pos = fPortPositions.get(fPortMap.get(aPort));

		pos.setY(pos.getY() + fPortDistance / 8);
		if (pos.getY() > getHeight() - (fPortDistance / 2)) {
			fHeight += fPortDistance / 8;
		}
	}

	@Override
	public void unTweakPortPosition(PortType aPort) {

		if (!fTweakedPorts.contains(aPort)) {
			return;
		}

		fTweakedPorts.remove(aPort);

		MutablePosition pos = fPortPositions.get(fPortMap.get(aPort));

		pos.setY(pos.getY() - fPortDistance / 8);
	}

	public void sortPins() {
		Collections.sort(fInputPorts, new Comparator<HintedPort<PortType>>() {
			public int compare(HintedPort<PortType> hp1, HintedPort<PortType> hp2) {
				return hp1.fHint - hp2.fHint;
			}
		});
		int n = fInputPorts.size();
		int yo = fPortDistance * 1 / 4;
		for (int i = 0; i < n; i++) {
			HintedPort<PortType> hp = fInputPorts.get(i);
			hp.fPos.setY(yo);
			yo += fPortDistance;
		}

		Collections.sort(fOutputPorts, new Comparator<HintedPort<PortType>>() {
			public int compare(HintedPort<PortType> hp1, HintedPort<PortType> hp2) {
				return hp1.fHint - hp2.fHint;
			}
		});
		n = fOutputPorts.size();
		yo = fPortDistance * 1 / 4;
		for (int i = 0; i < n; i++) {
			HintedPort<PortType> hp = fOutputPorts.get(i);
			hp.fPos.setY(yo);
			yo += fPortDistance;
		}
	}

	public Position getPortPosition(PortType p) {
		MutablePosition mp = fPortPositions.get(fPortMap.get(p));
		return new Position(mp.getX(), mp.getY());
	}

	@Override
	public void paint(NodeType aModule, int aXPos, int aYPos, boolean aHilight) {

		//updateFonts();

		fGC.setFont(VGFont.NORMAL);

		int w = getWidth();
		if (w < 1)
			w = 1;

		if (aHilight) {
			fGC.setForeground(VGColor.HIGHLIGHT);
			fGC.setLineWidth(4);
		} else {
			fGC.setForeground(VGColor.MODULE);
			fGC.setLineWidth(2);
		}

		fGC.setFont(VGFont.LARGE);

		int th = fGC.getFontHeight();

		fGC.setBackground(VGColor.BACKGROUND);
		if (aHilight) {
			fGC.setForeground(VGColor.HIGHLIGHT);
		} else {
			fGC.setForeground(VGColor.MODULE);
		}
		fGC.fillRectangle(fPortDistance / 2 + aXPos, aYPos, getWidth() - fPortDistance * 2, getHeight() - th - 1);
		fGC.drawRectangle(fPortDistance / 2 + aXPos, aYPos, getWidth() - fPortDistance * 2 - 1, getHeight() - th - 2);

		if (w < 20)
			return;

		// draw names for hierarchical subs
		if (!aHilight) {
			fGC.setForeground(VGColor.MODULE_LABEL);
		}

		fGC.drawText(fLabel, fPortDistance / 2 + 4 + aXPos, getHeight() + aYPos, true);

		fGC.setFont(VGFont.NORMAL);
		th = fGC.getFontHeight();

		// draw ports
		for (Iterator<VGPort<NodeType, PortType, SignalType>> i = fPortPositions.keySet().iterator(); i.hasNext();) {
			VGPort<NodeType, PortType, SignalType> p = i.next();
			MutablePosition pos = fPortPositions.get(p);

			String label = p.getLabel();

			fGC.setLineWidth(p.getWidth());

			if (!p.isOutput()) {
				fGC.drawLine(aXPos, pos.getY() + aYPos, fPortDistance / 2 + aXPos, pos.getY() + aYPos);
				fGC.drawText(label, fPortDistance / 2 + 4 + aXPos, pos.getY() + aYPos + th / 2, true);
			} else {
				fGC.drawLine(pos.getX() - fPortDistance / 2 + aXPos, pos.getY() + aYPos, pos.getX() + aXPos, pos.getY() + aYPos);
				int ww = fGC.textWidth(label);
				fGC.drawText(label, pos.getX() - fPortDistance / 2 - 4 + aXPos - ww, pos.getY() + aYPos + th / 2, true);
			}
		}

	}

	//	@Override
	//	public String isPortHit(int mx, int my, int aXPos, int aYPos) {
	//		for (Iterator<Entry<PortType, Position>> i = portPositions.entrySet().iterator(); i.hasNext();) {
	//			Entry<PortType, Position> e = i.next();
	//			Position pos = e.getValue();
	//			PortType pb = e.getKey();
	//			int size = 4;
	//			int x = pos.x;
	//			if (x == 0)
	//				x = 10;
	//			else
	//				x -= 10;
	//			int y = pos.y;
	//
	//			if (Math.abs(mx - aXPos - x) < size && Math.abs(my - aYPos - y) < size)
	//				return pb.getId();
	//
	//		}
	//		return null;
	//	}

}
