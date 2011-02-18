/*
 * Copyright 2004-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * 
 * Channel.java created on Jan 27, 2004 by guenter
 */
package org.zamia.vg;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashMapArray;
import org.zamia.util.Position;
import org.zamia.vg.VGGC.VGColor;

/**
 * A vertical module placement and wiring channel
 * 
 * @author Guenter Bartsch
 *
 */

public class VGChannel<NodeType, PortType, SignalType> {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static int WIRE_WIDTH = 6;

	private static final int X_BOX_DISTANCE = 50;

	class VGChannelAllocation {
		private final int fSignalIdx;

		private final VGSignal<NodeType, PortType, SignalType> fSignal;

		// channel connections (if any, can be null):

		private VGChannel<NodeType, PortType, SignalType> fChannelConnLeft, fChannelConnRight;

		private int fChannelConnRightYPos;

		private final ArrayList<VGPort<NodeType, PortType, SignalType>> fPortConnections = new ArrayList<VGPort<NodeType, PortType, SignalType>>();

		public VGChannelAllocation(VGSignal<NodeType, PortType, SignalType> aSignal, int aSignalIdx) {
			fSignal = aSignal;
			fSignalIdx = aSignalIdx;
		}

		public void addPortConnection(VGPort<NodeType, PortType, SignalType> aPort) {
			fPortConnections.add(aPort);
		}

		public int getNumPortConnections() {
			return fPortConnections.size();
		}

		public VGPort<NodeType, PortType, SignalType> getPortConnection(int aIdx) {
			return fPortConnections.get(aIdx);
		}

		public void setChannelConnLeft(VGChannel<NodeType, PortType, SignalType> aChannel) {
			if (fChannelConnLeft != null && fChannelConnLeft != aChannel) {
				throw new RuntimeException("Internal error: already have a channel connection");
			}

			fChannelConnLeft = aChannel;
		}

		public void setChannelConnRight(VGChannel<NodeType, PortType, SignalType> aChannel) {
			if (fChannelConnRight != null && fChannelConnRight != aChannel) {
				throw new RuntimeException("Internal error: already have a channel connection");
			}

			fChannelConnRight = aChannel;
		}

		public VGChannel<NodeType, PortType, SignalType> getChannelConnLeft() {
			return fChannelConnLeft;
		}

		public VGChannel<NodeType, PortType, SignalType> getChannelConnRight() {
			return fChannelConnRight;
		}

		public int getSignalIdx() {
			return fSignalIdx;
		}

		public void setChannelConnRightYPos(int aYPos) {
			fChannelConnRightYPos = aYPos;
		}

		public int getChannelConnRightYPos() {
			return fChannelConnRightYPos;
		}
	}

	private HashMapArray<VGSignal<NodeType, PortType, SignalType>, VGChannelAllocation> fSignals;

	private ArrayList<VGBox<NodeType, PortType, SignalType>> fBoxes;

	private int fIdx;

	private VGLayout<NodeType, PortType, SignalType> fLayout;

	private int fXPos;

	public VGChannel(VGLayout<NodeType, PortType, SignalType> aLayout, int aIdx) {
		fLayout = aLayout;
		fSignals = new HashMapArray<VGSignal<NodeType, PortType, SignalType>, VGChannelAllocation>();
		fBoxes = new ArrayList<VGBox<NodeType, PortType, SignalType>>();
		fIdx = aIdx;
	}

	void addSignal(VGSignal<NodeType, PortType, SignalType> aSignal) {
		if (fSignals.containsKey(aSignal))
			return;

		VGChannelAllocation alloc = new VGChannelAllocation(aSignal, fSignals.size());

		// System.out.println ("Signal "+s+" routed in "+this);
		fSignals.put(aSignal, alloc);
	}

	void add(VGBox<NodeType, PortType, SignalType> aBox) {
		fBoxes.add(aBox);

		aBox.setCol(fIdx);

		if (fBoxes.size() < 2)
			aBox.setYPos(0);
		else {
			VGBox<NodeType, PortType, SignalType> box = fBoxes.get(fBoxes.size() - 2);
			aBox.setYPos(box.getYPos() + box.getHeight() + X_BOX_DISTANCE);
		}
	}

	int getNumBoxes() {
		return fBoxes.size();
	}

	VGBox<NodeType, PortType, SignalType> getBox(int aIdx) {
		if (aIdx >= fBoxes.size())
			return null;
		return fBoxes.get(aIdx);
	}

	public int getWidth() {
		int size = 0;
		int n = fBoxes.size();
		for (int i = 0; i < n; i++) {
			VGBox<NodeType, PortType, SignalType> box = fBoxes.get(i);
			if (box == null)
				continue;
			int s = box.getWidth();
			if (s > size)
				size = s;
		}
		return size + fSignals.size() * WIRE_WIDTH;
	}

	public int getHeight() {
		int n = fBoxes.size();
		int height = 0;
		for (int i = 0; i < n; i++) {
			VGBox<NodeType, PortType, SignalType> box = fBoxes.get(i);
			int h = box.getYPos() + box.getHeight();
			if (h > height)
				height = h;
		}
		return height;
	}

	public int getRoutingChannelSize() {
		return fSignals.size() * WIRE_WIDTH;
	}

	public int getModulesPos() {
		return fXPos + fSignals.size() * WIRE_WIDTH;
	}

	private int getSignalXPos(VGSignal<NodeType, PortType, SignalType> aSignal) {
		VGChannelAllocation ca = fSignals.get(aSignal);
		return fXPos + ca.fSignalIdx * WIRE_WIDTH;
	}

	private VGChannelAllocation getSignalAllocation(VGSignal<NodeType, PortType, SignalType> aSignal) {
		VGChannelAllocation alloc = fSignals.get(aSignal);

		if (alloc == null) {
			alloc = new VGChannelAllocation(aSignal, fSignals.size());
			fSignals.put(aSignal, alloc);
		}
		return alloc;
	}

	void addPortConnection(VGPort<NodeType, PortType, SignalType> aPort, VGSignal<NodeType, PortType, SignalType> aSignal) {

		VGChannelAllocation ca = getSignalAllocation(aSignal);

		ca.addPortConnection(aPort);
	}

	void addChannelConnLeft(VGSignal<NodeType, PortType, SignalType> aSignal, VGChannel<NodeType, PortType, SignalType> aChannel) {

		VGChannelAllocation ca = getSignalAllocation(aSignal);

		ca.setChannelConnLeft(aChannel);
	}

	void addChannelConnRight(VGSignal<NodeType, PortType, SignalType> aSignal, VGChannel<NodeType, PortType, SignalType> aChannel) {

		VGChannelAllocation ca = getSignalAllocation(aSignal);

		ca.setChannelConnRight(aChannel);
	}

	VGBox<NodeType, PortType, SignalType> getBoxAt(int aY) {
		int n = fBoxes.size();
		for (int i = 0; i < n; i++) {
			VGBox<NodeType, PortType, SignalType> box = fBoxes.get(i);

			int y1 = box.getYPos();
			int y2 = box.getHeight() + y1;

			if (y1 <= aY && y2 >= aY)
				return box;
		}
		return null;
	}

	boolean isBoxPlacedAt(int aYPos) {

		int n = getNumBoxes();
		for (int i = 0; i < n; i++) {
			VGBox<NodeType, PortType, SignalType> box = getBox(i);
			if (box == null)
				continue;

			int ypos = box.getYPos();
			int h = box.getHeight();
			if ((aYPos >= ypos) && (aYPos <= ypos + h))
				return true;

		}
		return false;
	}

	public int getSignalYPos(VGSignal<NodeType, PortType, SignalType> aSignal) {
		VGChannelAllocation ca = fSignals.get(aSignal);
		if (ca == null)
			return 0;
		return ca.fChannelConnRightYPos;
	}

	public VGBox<NodeType, PortType, SignalType> getCollidingBox(VGBox<NodeType, PortType, SignalType> aBox, int aYPos) {
		int n = getNumBoxes();

		int ymin1 = aYPos;
		int ymax1 = aYPos + aBox.getHeight();

		for (int i = 0; i < n; i++) {
			VGBox<NodeType, PortType, SignalType> m2 = getBox(i);

			if (m2 == aBox)
				continue;

			int ymin2 = m2.getYPos();
			int ymax2 = ymin2 + m2.getHeight();

			if (ymin2 <= ymin1 && ymax2 >= ymin1)
				return m2;
			if (ymin2 >= ymin1 && ymax2 <= ymax1)
				return m2;
			if (ymin2 <= ymax1 && ymax2 >= ymax1)
				return m2;
		}
		return null;
	}

	void setXPos(int aXPos) {
		fXPos = aXPos;
	}

	int getXPos() {
		return fXPos;
	}

	public void paint(VGSelectionProvider<NodeType, SignalType> aSelectionProvider) {

		VGGC gc = fLayout.getGC();

		int nBoxes = fBoxes.size();
		for (int i = 0; i < nBoxes; i++) {
			VGBox<NodeType, PortType, SignalType> box = fBoxes.get(i);

			box.paint(aSelectionProvider.isNodeSelected(box.getNode()));
		}

		int nSignals = fSignals.size();
		for (int i = 0; i < nSignals; i++) {
			VGSignal<NodeType, PortType, SignalType> signal = fSignals.get(i).fSignal;

			boolean selected = aSelectionProvider.isSignalSelected(signal.getSignal());

			VGChannelAllocation ca = fSignals.get(signal);

			int minConnY = Integer.MAX_VALUE;
			int maxConnY = Integer.MIN_VALUE;

			int n = ca.getNumPortConnections();
			for (int j = 0; j < n; j++) {
				VGPort<NodeType, PortType, SignalType> p = ca.getPortConnection(j);

				int pos = fLayout.getPortPosition(p).getY();

				if (pos < minConnY)
					minConnY = pos;
				if (pos > maxConnY)
					maxConnY = pos;
			}

			VGChannel<NodeType, PortType, SignalType> channel = ca.getChannelConnLeft();
			if (channel != null) {
				int pos = channel.getSignalAllocation(signal).getChannelConnRightYPos();
				if (pos < minConnY)
					minConnY = pos;
				if (pos > maxConnY)
					maxConnY = pos;
			}

			channel = ca.getChannelConnRight();
			if (channel != null) {
				int pos = ca.getChannelConnRightYPos();
				if (pos < minConnY)
					minConnY = pos;
				if (pos > maxConnY)
					maxConnY = pos;
			}

			int w = signal.getWidth();

			if (selected) {
				gc.setLineWidth(w * 2);
				gc.setForeground(VGColor.HIGHLIGHT);
			} else {
				gc.setLineWidth(w);
				gc.setForeground(VGColor.SIGNAL);
			}

			int sx, sy, dx, dy;
			sx = fXPos + ca.fSignalIdx * WIRE_WIDTH;
			dx = fXPos + ca.fSignalIdx * WIRE_WIDTH;
			sy = minConnY;
			dy = maxConnY;

			gc.drawLine(sx, sy, dx, dy);

			if (selected) {
				gc.setBackground(VGColor.HIGHLIGHT);
			} else {
				gc.setBackground(VGColor.SIGNAL);
			}

			// channel connection?
			if (channel != null) {
				sy = ca.getChannelConnRightYPos();
				dx = channel.getSignalXPos(signal);
				gc.drawLine(sx, sy, dx, sy);
				if (sy != maxConnY && sy != minConnY) {
					gc.fillOval(sx, sy, WIRE_WIDTH/3, WIRE_WIDTH/3);
				}
			}

			for (int j = 0; j < n; j++) {

				VGPort<NodeType, PortType, SignalType> p = ca.getPortConnection(j);

				Position ppos = fLayout.getPortPosition(p);

				sy = ppos.getY();
				dx = ppos.getX();

				gc.drawLine(sx, sy, dx, sy);

				if (sy == maxConnY)
					continue;

				if (sy == minConnY)
					continue;

				gc.fillOval(sx, sy, WIRE_WIDTH/3, WIRE_WIDTH/3);

			}
		}
	}

	/**
	 * returns vertical signal lengths (== routing penalty)
	 */
	public int route() {

		/*
		 * we want to compute Y position for all connections
		 * and we want them to be collision-free
		 * 
		 * so no two connections of different signals are allowed to have
		 * the same Y position
		 * 
		 * so, we need to choose channel connection Y positions wisely
		 * and tweak port positions if necessary
		 */

		HashMap<Integer, SignalType> connections = new HashMap<Integer, SignalType>();

		int nSignals = fSignals.size();
		for (int i = 0; i < nSignals; i++) {
			VGSignal<NodeType, PortType, SignalType> signal = fSignals.get(i).fSignal;
			SignalType s = signal.getSignal();

			VGChannelAllocation ca = fSignals.get(signal);

			int n = ca.getNumPortConnections();
			for (int j = 0; j < n; j++) {
				VGPort<NodeType, PortType, SignalType> p = ca.getPortConnection(j);

				VGBox<NodeType, PortType, SignalType> box = p.getBox();

				VGSymbol<NodeType, PortType, SignalType> symbol = box.getSymbol();

				symbol.unTweakPortPosition(p.getPort());

				int pos = fLayout.getPortPosition(p).getY();

				SignalType s2 = connections.get(pos);

				if (s != null) {

					if (s != s2) {

						symbol.tweakPortPosition(p.getPort());

						pos = fLayout.getPortPosition(p).getY();

						connections.put(pos, s);
					}

				} else {
					connections.put(pos, s);
				}
			}

			/*
			 * connection coming in from left channel
			 */

			VGChannel<NodeType, PortType, SignalType> lc = ca.getChannelConnLeft();

			if (lc != null) {
				VGChannelAllocation ca2 = lc.getSignalAllocation(signal);

				int pos = ca2.getChannelConnRightYPos();
				connections.put(pos, s);
			}
		}

		/*
		 * calc Y pos of channel connections to right channel
		 */

		int penalty = 0;

		for (int i = 0; i < nSignals; i++) {
			VGSignal<NodeType, PortType, SignalType> signal = fSignals.get(i).fSignal;
			SignalType s = signal.getSignal();

			VGChannelAllocation ca = fSignals.get(signal);

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			int n = ca.getNumPortConnections();
			for (int j = 0; j < n; j++) {
				VGPort<NodeType, PortType, SignalType> p = ca.getPortConnection(j);

				int pos = fLayout.getPortPosition(p).getY();

				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
			}

			VGChannel<NodeType, PortType, SignalType> lc = ca.getChannelConnLeft();
			if (lc != null) {
				VGChannelAllocation ca2 = lc.getSignalAllocation(signal);

				int pos = ca2.getChannelConnRightYPos();
				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
			}

			VGChannel<NodeType, PortType, SignalType> rc = ca.getChannelConnRight();
			if (rc != null) {
				// find first collision-free position

				int pos = min;

				while (true) {

					// colliding connection ?

					SignalType s2 = connections.get(pos);

					if (s2 != null && s2 != s) {
						pos += 5;
						continue;
					}

					// colliding box ?

					int nBoxes = getNumBoxes();
					boolean coll = false;
					for (int j = 0; j < nBoxes; j++) {

						VGBox<NodeType, PortType, SignalType> box = getBox(j);

						int boxY1 = box.getYPos();
						int boxY2 = boxY1 + box.getHeight();

						if (pos >= boxY1 && pos <= boxY2) {
							coll = true;
							pos = boxY2 + 5;
							break;
						}
					}

					if (!coll) {
						break;
					}
				}

				connections.put(pos, s);
				ca.setChannelConnRightYPos(pos);
				
				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
			}

			penalty += (max - min);

		}

		return penalty;
	}
}