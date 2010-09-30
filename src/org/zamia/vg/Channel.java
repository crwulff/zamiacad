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

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashMapArray;


/**
 * A vertical module placement and wiring channel
 * 
 * @author Guenter Bartsch
 *
 */

public class Channel {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static int WIRE_WIDTH = 6;

	class ChannelConnection {

		private int yPos;

		VGPort p;

		//		Channel c;

		public ChannelConnection(VGPort p_) {
			p = p_;
		}

		public ChannelConnection(int yPos_, Channel c_) {
			yPos = yPos_;
			//			c = c_;
		}

		public int getYPos() {
			if (p != null)
				return par.getPortPosition(p).getY();
			return yPos;
		}
	}

	static class ChannelAllocation {
		int pos;

		VGSignal signal;

		// channel connection:
		int yPos;

		Channel c;

		// int min, max;

		ArrayList<ChannelConnection> connections;

		public ChannelAllocation(VGSignal s_, int pos_) {
			signal = s_;
			pos = pos_;
			reset();
		}

		public void reset() {
			// min = -1;
			// max = -1;
			connections = new ArrayList<ChannelConnection>(2);
		}
	}

	private int pos;

	private HashMapArray<VGSignal, ChannelAllocation> signals;

	private ArrayList<VGBox> slots;

	private int idx;

	private int numModules;

	private Layout par;

	public Channel(Layout par_, int idx_) {
		pos = 0;
		par = par_;
		signals = new HashMapArray<VGSignal, ChannelAllocation>();
		slots = new ArrayList<VGBox>();
		idx = idx_;
		numModules = 0;
	}

	public void addSignal(VGSignal s_) {
		if (signals.containsKey(s_))
			return;

		ChannelAllocation alloc = new ChannelAllocation(s_, signals.size());

		// System.out.println ("Signal "+s+" routed in "+this);
		signals.put(s_, alloc);
	}

	public int getNumSlots() {
		return slots.size();
	}

	public VGBox getModule(int idx_) {
		if (idx_ >= slots.size())
			return null;
		return slots.get(idx_);
	}

	//	public int getWidth() {
	//		int size = 0;
	//		int n = slots.size();
	//		for (int i = 0; i < n; i++) {
	//			VGBox module = slots.get(i);
	//			if (module == null)
	//				continue;
	//			int s = module.getWidth();
	//			if (s > size)
	//				size = s;
	//		}
	//		return size + signals.size() * WIRE_WIDTH;
	//	}
	//
	//	public void setXPos(int pos_) {
	//		pos = pos_;
	//	}
	//
	//	public int getXPos() {
	//		return pos;
	//	}
	//
	//	public int getHeight() {
	//		int n = slots.size();
	//		int height = 0;
	//		for (int i = 0; i < n; i++) {
	//			VGBox box = slots.get(i);
	//			int h = box.getYPos() + box.getHeight();
	//			if (h > height)
	//				height = h;
	//		}
	//		return height;
	//	}
	//
	//	public int getRoutingChannelSize() {
	//		return signals.size() * WIRE_WIDTH;
	//	}
	//
	//	public int getModulesPos() {
	//		return pos + signals.size() * WIRE_WIDTH;
	//	}
	//
	//	public int getSignalIdx(VisualSignal s_) {
	//
	//		if (!signals.containsKey(s_))
	//			System.out.println("Signal " + s_ + " missing in " + this);
	//
	//		int idx = signals.get(s_).pos * WIRE_WIDTH;
	//		// System.out.println("index of " + s + " in " + this +" is " + idx);
	//		return idx;
	//	}
	//
	//	public String toString() {
	//		return "VChannel" + idx;
	//	}
	//
	//	public int getNumModules() {
	//		return numModules;
	//	}
	//
	//	public void clearSlot(int slot_) {
	//		put(slot_, null);
	//	}
	//
	//	private int getSignalXPos(VisualSignal s) {
	//		ChannelAllocation ca = signals.get(s);
	//		return pos + ca.pos * WIRE_WIDTH;
	//	}
	//
	//	public void put(int slot_, VisualModule module_) {
	//
	//		if (getModule(slot_) != null && module_ != null) {
	//			System.out.println("Collision!! slot " + slot_ + " in channel " + this + " was already occupied.");
	//			numModules--;
	//		}
	//
	//		while (slots.size() <= slot_) {
	//			slots.add(null);
	//		}
	//		slots.set(slot_, module_);
	//
	//		if (module_ != null) {
	//			numModules++;
	//
	//			module_.setVChannel(this);
	//		}
	//	}
	//
	//	private ChannelAllocation getSignalAllocation(VisualSignal s_) {
	//		ChannelAllocation alloc = signals.get(s_);
	//
	//		if (alloc == null) {
	//			alloc = new ChannelAllocation(s_, signals.size());
	//			signals.put(s_, alloc);
	//		}
	//		return alloc;
	//	}
	//
	//	public boolean addPinConnection(VisualPort p_, VisualSignal s_) {
	//
	//		ChannelAllocation ca = getSignalAllocation(s_);
	//
	//		int n = ca.connections.size();
	//		for (int i = 0; i < n; i++) {
	//			if (ca.connections.get(i).p == p_) {
	//				// System.out.println ("warning: port was already connected to
	//				// this signal wire channel.");
	//				return false;
	//			}
	//		}
	//
	//		ca.connections.add(new ChannelConnection(p_));
	//
	//		return allocatorAt(par.getPortPosition(p_).y, s_);
	//
	//	}
	//
	//	public int addChannelConnection(VisualSignal s_, Channel c_, int y_) {
	//
	//		ChannelAllocation ca = getSignalAllocation(s_);
	//
	//		if (ca.c != null)
	//			return ca.yPos;
	//
	//		boolean collisionFound = false;
	//
	//		do {
	//			collisionFound = false;
	//			VisualModule module = getModuleAt(y_);
	//			while (module != null) {
	//				y_ = module.getYPos() + module.getHeight() + 1;
	//				module = getModuleAt(y_);
	//				collisionFound = true;
	//			}
	//
	//			while (allocatorAt(y_, s_) || c_.allocatorAt(y_, s_)) {
	//				collisionFound = true;
	//				y_ += WIRE_WIDTH;
	//			}
	//
	//		} while (collisionFound);
	//
	//		ca.c = c_;
	//		ca.yPos = y_;
	//
	//		c_.addConnection(s_, y_, c_);
	//
	//		return y_;
	//	}
	//
	//	private boolean allocatorAt(int y_, VisualSignal s_) {
	//
	//		int n = signals.size();
	//		for (int i = 0; i < n; i++) {
	//			ChannelAllocation ca = signals.get(i);
	//
	//			if (ca.signal == s_)
	//				continue;
	//
	//			if (ca.c != null) {
	//				if (y_ == ca.yPos)
	//					return true;
	//			}
	//
	//			int m = ca.connections.size();
	//			for (int j = 0; j < m; j++) {
	//				ChannelConnection conn = ca.connections.get(j);
	//
	//				if (conn.getYPos() == y_)
	//					return true;
	//
	//			}
	//		}
	//		return false;
	//	}
	//
	//	private void addConnection(VisualSignal s_, int y_, Channel c_) {
	//		ChannelAllocation ca = getSignalAllocation(s_);
	//		ca.connections.add(new ChannelConnection(y_, c_));
	//	}
	//
	//	public VisualModule getModuleAt(int y_) {
	//		int n = slots.size();
	//		for (int i = 0; i < n; i++) {
	//			VisualModule module = slots.get(i);
	//
	//			int y1 = module.getYPos();
	//			int y2 = module.getHeight() + y1;
	//
	//			if (y1 <= y_ && y2 >= y_)
	//				return module;
	//		}
	//		return null;
	//	}
	//
	//	public int getNumSignals() {
	//		return signals.size();
	//	}
	//
	//	public void clearAllocators() {
	//		int n = getNumSignals();
	//		for (int i = 0; i < n; i++) {
	//			ChannelAllocation ca = signals.get(i);
	//			ca.reset();
	//		}
	//	}
	//
	//	public boolean isModulePlacedAt(int pos_) {
	//
	//		int n = getNumSlots();
	//		for (int i = 0; i < n; i++) {
	//			VisualModule module = getModule(i);
	//			if (module == null)
	//				continue;
	//
	//			int ypos = module.getYPos();
	//			int h = module.getSymbol().getHeight();
	//			if ((pos_ >= ypos) && (pos_ <= ypos + h))
	//				return true;
	//
	//		}
	//		return false;
	//	}
	//
	//	public void add(VisualModule module_) {
	//
	//		// sanity check
	//		int n = slots.size();
	//		for (int i = 0; i < n; i++) {
	//			if (slots.get(i) == module_)
	//				logger.error("Channel.add(): sanity check failed!");
	//		}
	//
	//		slots.add(module_);
	//		module_.setCol(idx);
	//		module_.setVChannel(this);
	//
	//		if (slots.size() < 2)
	//			module_.setYPos(0);
	//		else {
	//			VisualModule module = slots.get(slots.size() - 2);
	//			module_.setYPos(module.getYPos() + module.getHeight() + 50);
	//		}
	//	}
	//
	//	public int getSignalYPos(VisualSignal s_) {
	//		ChannelAllocation ca = signals.get(s_);
	//		if (ca == null)
	//			return 0;
	//		return ca.yPos;
	//	}
	//
	//	public VisualModule getCollidingModule(VisualModule module_, int yPos_) {
	//		int n = getNumSlots();
	//
	//		int ymin1 = yPos_;
	//		int ymax1 = yPos_ + module_.getHeight();
	//
	//		for (int i = 0; i < n; i++) {
	//			VisualModule m2 = getModule(i);
	//
	//			if (m2 == module_)
	//				continue;
	//
	//			int ymin2 = m2.getYPos();
	//			int ymax2 = ymin2 + m2.getHeight();
	//
	//			if (ymin2 <= ymin1 && ymax2 >= ymin1)
	//				return m2;
	//			if (ymin2 >= ymin1 && ymax2 <= ymax1)
	//				return m2;
	//			if (ymin2 <= ymax1 && ymax2 >= ymax1)
	//				return m2;
	//		}
	//		return null;
	//	}
}