/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * 
 * Channel.java created on Jan 27, 2004 by guenter
 */
package org.zamia.plugin.views.rtl;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLSignal;
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

		VisualPort p;
		
//		Channel c;

		public ChannelConnection(VisualPort p_) {
			p = p_;
		}

		public ChannelConnection(int yPos_, Channel c_) {
			yPos = yPos_;
//			c = c_;
		}

		public int getYPos() {
			if (p != null)
				return par.getPortPosition(p).y;
			return yPos;
		}
	}

	static class ChannelAllocation {
		int pos;

		VisualSignal signal;

		// channel connection:
		int yPos;

		Channel c;

		// int min, max;

		ArrayList<ChannelConnection> connections;

		public ChannelAllocation(VisualSignal s_, int pos_) {
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

	private HashMapArray<VisualSignal, ChannelAllocation> signals;

	private ArrayList<VisualModule> slots;

	private int idx;

	private int numModules;

	private PlaceAndRoute par;

	public Channel(PlaceAndRoute par_, int idx_) {
		pos = 0;
		par = par_;
		signals = new HashMapArray<VisualSignal, ChannelAllocation>();
		slots = new ArrayList<VisualModule>();
		idx = idx_;
		numModules = 0;
	}

	public void addSignal(VisualSignal s_) {
		if (signals.containsKey(s_))
			return;

		ChannelAllocation alloc = new ChannelAllocation(s_, signals.size());

		// System.out.println ("Signal "+s+" routed in "+this);
		signals.put(s_, alloc);
	}

	public int getNumSlots() {
		return slots.size();
	}

	public VisualModule getModule(int idx_) {
		if (idx_ >= slots.size())
			return null;
		return slots.get(idx_);
	}

	public int getWidth() {
		int size = 0;
		int n = slots.size();
		for (int i = 0; i < n; i++) {
			VisualModule module = slots.get(i);
			if (module == null)
				continue;
			int s = module.getWidth();
			if (s > size)
				size = s;
		}
		return size + signals.size() * WIRE_WIDTH;
	}

	public void setXPos(int pos_) {
		pos = pos_;
	}

	public int getXPos() {
		return pos;
	}

	public int getHeight() {
		int n = slots.size();
		int height = 0;
		for (int i = 0; i<n; i++) {
			VisualModule box = slots.get(i);
			int h = box.getYPos() + box.getHeight();
			if (h > height)
				height = h;
		}
		return height;
	}

	public int getRoutingChannelSize() {
		return signals.size() * WIRE_WIDTH;
	}

	public int getModulesPos() {
		return pos + signals.size() * WIRE_WIDTH;
	}

	public int getSignalIdx(VisualSignal s_) {

		if (!signals.containsKey(s_))
			System.out.println("Signal " + s_ + " missing in " + this);

		int idx = signals.get(s_).pos * WIRE_WIDTH;
		// System.out.println("index of " + s + " in " + this +" is " + idx);
		return idx;
	}

	public String toString() {
		return "VChannel" + idx;
	}

	public int getNumModules() {
		return numModules;
	}

	public void clearSlot(int slot_) {
		put(slot_, null);
	}

	public void paint(RTLView rtlview_, Display display_, GC gc_) {

		// if (hor) {
		// gc.setXORMode(true);
		// if (getNumSignals() > 0) {
		// gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
		//
		// // System.out.println ("Signals in hchannel ypos="+pos+":");
		// //
		// // for (Iterator<Signal> i=signals.keySet().iterator();
		// // i.hasNext();) {
		// // Signal s = i.next();
		// // System.out.println (" Signal: "+s);
		// //
		// // }
		//
		// } else
		// gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		//
		// gc.fillRectangle(viewer.tX(0), viewer.tY(pos), viewer.tW(1000),
		// viewer.tH(getRoutingChannelSize()));
		// gc.setXORMode(false);
		// }

		int nSignals = signals.size();
		for (int i = 0; i<nSignals; i++) {
			VisualSignal s = signals.get(i).signal;

			boolean selected = rtlview_.isSignalHilight(s.getRTLSignal());

			ChannelAllocation ca = signals.get(s);

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			int n = ca.connections.size();
			for (int j = 0; j < n; j++) {
				int pos = ca.connections.get(j).getYPos();
				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
			}

			if (ca.c != null) {
				if (ca.yPos > max)
					max = ca.yPos;
				if (ca.yPos < min)
					min = ca.yPos;
			}

			double w = 1.0;
			if (!s.getType().isBit())
				w = WIRE_WIDTH - 3;

			if (selected) {
				gc_.setLineWidth((int) (w * 2 * rtlview_.getZoomFactor()));
				gc_.setForeground(rtlview_.getColorScheme().getHilightColor());
			} else {
				// NetListAnnotation a = viewer.getAnnotation(s);
				// if (a == null) {
				gc_.setLineWidth((int) (w * rtlview_.getZoomFactor()));
				gc_.setForeground(rtlview_.getColorScheme().getSignalColor());
				// gc.setForeground(viewer.getColorScheme().getHilightColor());
				// } else {
				// if (a.highlight)
				// gc
				// .setLineWidth((int) (viewer.getZoomFactor() * 2.0 * w));
				// else
				// gc.setLineWidth((int) (w * viewer.getZoomFactor()));
				//
				// gc.setForeground(viewer.getAnnotationSWTColor(a));
				// }
			}

			int sx, sy, dx, dy;
			sx = pos + ca.pos * WIRE_WIDTH;
			dx = pos + ca.pos * WIRE_WIDTH;
			sy = min;
			dy = max;

			gc_.drawLine(rtlview_.tX(sx), rtlview_.tY(sy), rtlview_.tX(dx), rtlview_
					.tY(dy));

			if (selected) {
				gc_.setBackground(rtlview_.getColorScheme().getHilightColor());
			} else {
				gc_.setBackground(rtlview_.getColorScheme().getSignalColor());
			}

			// channel connection?
			if (ca.c != null) {
				sy = ca.yPos;
				dx = ca.c.getSignalXPos(s);
				gc_.drawLine(rtlview_.tX(sx), rtlview_.tY(sy), rtlview_.tX(dx), rtlview_
						.tY(sy));
			}

			for (int j = 0; j < n; j++) {

				ChannelConnection conn = ca.connections.get(j);

				sy = conn.getYPos();

				if (conn.p != null) {
					dx = par.getPortPosition(conn.p).x;

					gc_.drawLine(rtlview_.tX(sx), rtlview_.tY(sy), rtlview_.tX(dx),
							rtlview_.tY(sy));
				}

				if (sy == max)
					continue;

				if (sy == min)
					continue;

				gc_.fillOval(rtlview_.tX(sx - (double) WIRE_WIDTH / 2), rtlview_
						.tY(sy - (double) WIRE_WIDTH / 2),
						(int) (WIRE_WIDTH * rtlview_.getZoomFactor()),
						(int) ((double) WIRE_WIDTH * rtlview_.getZoomFactor()));

			}

		}
	}

	private int getSignalXPos(VisualSignal s) {
		ChannelAllocation ca = signals.get(s);
		return pos + ca.pos * WIRE_WIDTH;
	}

	public void put(int slot_, VisualModule module_) {

		if (getModule(slot_) != null && module_ != null) {
			System.out.println("Collision!! slot " + slot_ + " in channel "
					+ this + " was already occupied.");
			numModules--;
		}

		while (slots.size() <= slot_) {
			slots.add(null);
		}
		slots.set(slot_, module_);

		if (module_ != null) {
			numModules++;

			module_.setVChannel(this);
		}
	}

	private ChannelAllocation getSignalAllocation(VisualSignal s_) {
		ChannelAllocation alloc = signals.get(s_);

		if (alloc == null) {
			alloc = new ChannelAllocation(s_, signals.size());
			signals.put(s_, alloc);
		}
		return alloc;
	}

	public boolean addPinConnection(VisualPort p_, VisualSignal s_) {

		ChannelAllocation ca = getSignalAllocation(s_);

		int n = ca.connections.size();
		for (int i = 0; i < n; i++) {
			if (ca.connections.get(i).p == p_) {
				// System.out.println ("warning: port was already connected to
				// this signal wire channel.");
				return false;
			}
		}

		ca.connections.add(new ChannelConnection(p_));
		
		return allocatorAt(par.getPortPosition(p_).y, s_);
		
	}

	public int addChannelConnection(VisualSignal s_, Channel c_, int y_) {

		ChannelAllocation ca = getSignalAllocation(s_);

		if (ca.c != null)
			return ca.yPos;

		boolean collisionFound = false;

		do {
			collisionFound = false;
			VisualModule module = getModuleAt(y_);
			while (module != null) {
				y_ = module.getYPos() + module.getHeight() + 1;
				module = getModuleAt(y_);
				collisionFound = true;
			}

			while (allocatorAt(y_, s_) || c_.allocatorAt(y_, s_)) {
				collisionFound = true;
				y_ += WIRE_WIDTH;
			}

		} while (collisionFound);

		ca.c = c_;
		ca.yPos = y_;

		c_.addConnection(s_, y_, c_);

		return y_;
	}

	private boolean allocatorAt(int y_, VisualSignal s_) {

		int n = signals.size();
		for (int i = 0; i < n; i++) {
			ChannelAllocation ca = signals.get(i);

			if (ca.signal == s_)
				continue;
			
			if (ca.c != null) {
				if (y_ == ca.yPos)
					return true;
			}
			
			int m = ca.connections.size();
			for (int j = 0; j<m; j++) {
				ChannelConnection conn = ca.connections.get(j);
				
				if (conn.getYPos() == y_)
					return true;
				
			}
		}
		return false;
	}

	private void addConnection(VisualSignal s_, int y_, Channel c_) {
		ChannelAllocation ca = getSignalAllocation(s_);
		ca.connections.add(new ChannelConnection(y_, c_));
	}

	public VisualModule getModuleAt(int y_) {
		int n = slots.size();
		for (int i = 0; i < n; i++) {
			VisualModule module = slots.get(i);

			int y1 = module.getYPos();
			int y2 = module.getHeight() + y1;

			if (y1 <= y_ && y2 >= y_)
				return module;
		}
		return null;
	}

	public void print(PlaceAndRoute par_, PrintWriter out_) {

		int nSignals = signals.size();
		for (int i = 0; i<nSignals; i++) {
			VisualSignal s = signals.get(i).signal;

			ChannelAllocation ca = signals.get(s);

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			int n = ca.connections.size();
			for (int j = 0; j < n; j++) {
				int pos = ca.connections.get(j).getYPos();
				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
			}

			if (ca.c != null) {
				if (ca.yPos > max)
					max = ca.yPos;
				if (ca.yPos < min)
					min = ca.yPos;
			}

			double w = 1.0;
			if (!s.getType().isBit())
				w = WIRE_WIDTH - 3;

			int sx, sy, dx, dy;
			sx = pos + ca.pos * WIRE_WIDTH;
			dx = pos + ca.pos * WIRE_WIDTH;
			sy = min;
			dy = max;

			PSUtils.psDrawLine(par_, out_, sx, sy, dx, dy, w);

			// channel connection?
			if (ca.c != null) {
				sy = ca.yPos;
				dx = ca.c.getSignalXPos(s);
				PSUtils.psDrawLine(par_, out_, sx, sy, dx, sy, w);
			}

			for (int j = 0; j < n; j++) {

				ChannelConnection conn = ca.connections.get(j);

				sy = conn.getYPos();

				if (conn.p != null) {
					dx = par.getPortPosition(conn.p).x;

					PSUtils.psDrawLine(par_, out_, sx, sy, dx, sy, w);
				}

				if (sy == max)
					continue;

				if (sy == min)
					continue;

				PSUtils.psDrawArc(par_, out_, sx - (double) WIRE_WIDTH / 2, sy - (double) WIRE_WIDTH / 2, WIRE_WIDTH / 4, 0, 360, false);

			}

		}
	}

	public int getNumSignals() {
		return signals.size();
	}

	// private int val[];
	//
	// private ChannelAllocation solution[];
	//
	// private int bestScore;
	//
	// private int now;

	// private int calcSignalOrderScore() {
	// int n = getNumSignals();
	// ChannelAllocation cas[] = new ChannelAllocation[n];
	// for (int i = 0; i < n; i++) {
	// cas[i] = this.signals.get(val[i]);
	// }
	//
	// int score = 0;
	//
	// for (int i = 0; i < n; i++) {
	// ChannelAllocation ca = cas[i];
	//
	// // System.out.println ("Collisions of signal: "+ca.signal);
	//
	// int m = ca.connections.size();
	// for (int j = 0; j < m; j++) {
	//
	// ChannelConnection cc = ca.connections.get(j);
	// if (cc.left) {
	// // System.out.println(" CCL "+cc.pos);
	// for (int k = 0; k < i; k++) {
	// ChannelAllocation ca2 = cas[k];
	// if ((ca2.min <= cc.ypos) && (ca2.max >= cc.ypos)) {
	// score--;
	// // System.out.println (" + "+ca2.signal);
	// }
	// }
	// } else {
	// // System.out.println(" CCR "+cc.pos);
	// for (int k = i + 1; k < n; k++) {
	// ChannelAllocation ca2 = cas[k];
	// if ((ca2.min <= cc.ypos) && (ca2.max >= cc.ypos)) {
	// score--;
	// // System.out.println (" - "+ca2.signal);
	// }
	// }
	// }
	// }
	// }
	//
	// return score;
	// }

	// private void visit(int k) {
	// now++;
	// val[k] = now;
	// int n = getNumSignals();
	// if (now == (n - 1)) {
	// if (n < 6) {
	// System.out.print("Permutation: ");
	// for (int j = 0; j < n; j++)
	// System.out.print(val[j]);
	// System.out.println();
	// }
	// int score = calcSignalOrderScore();
	// if (n < 6)
	// System.out.println("Score: " + score);
	// if (score > bestScore) {
	// bestScore = score;
	// System.out.println("******* Score: " + bestScore);
	// for (int i = 0; i < n; i++) {
	// solution[i] = signals.get(val[i]);
	// System.out.println("[" + i + "] " + solution[i].signal);
	// }
	// }
	// }
	// for (int t = 0; t < n; t++) {
	// if (val[t] == -1)
	// visit(t);
	// }
	// now--;
	// val[k] = -1;
	// }
	//
	// private void updateSolution(int score_) {
	// if (score_ > bestScore) {
	// bestScore = score_;
	// // System.out.println("******* Score: " + bestScore);
	// int n = getNumSignals();
	// for (int j = 0; j < n; j++) {
	// solution[j] = signals.get(val[j]);
	// // System.out.println("[" + j + "] " + solution[j].signal);
	// }
	// }
	// }
	//
	// public void optimizeSignalOrderingOld() {
	//
	// // System.out.println("Optimizing channel: " + idx);
	//
	// int n = getNumSignals();
	// val = new int[n];
	// solution = new ChannelAllocation[n];
	// for (int i = 0; i < n; i++) {
	// val[i] = i;
	// solution[i] = signals.get(i);
	// }
	//
	// bestScore = calcSignalOrderScore();
	// int oldBestScore = bestScore;
	// while (true) {
	// for (int i = 0; i < 10000; i++) {
	// int p1 = (int) (Math.random() * n);
	// int p2 = (int) (Math.random() * n);
	// if (p1 != p2) {
	//
	// int h = val[p1];
	// val[p1] = val[p2];
	// val[p2] = h;
	//
	// int score = calcSignalOrderScore();
	// // System.out.println ("Score: "+score+" p1: "+p1+" p2:
	// // "+p2);
	// if (score > bestScore)
	// updateSolution(score);
	// else {
	// h = val[p1];
	// val[p1] = val[p2];
	// val[p2] = h;
	// }
	// }
	// if (bestScore == 0)
	// break;
	// }
	// if (bestScore == 0)
	// break;
	//
	// if (bestScore > oldBestScore) {
	// oldBestScore = bestScore;
	// } else
	// break;
	// }
	// signals = new HashMapArray<RTLSignal, ChannelAllocation>();
	// for (int i = 0; i < n; i++) {
	// ChannelAllocation ca = solution[i];
	// signals.put(ca.signal, ca);
	// ca.pos = i;
	// }
	// }
	//
	// // bartsch changed 13.03.2006
	// // just sort signal according to their topmost
	// // allocator
	// public void optimizeSignalOrdering() {
	//
	// // System.out.println("Optimizing channel: " + idx);
	//
	// int n = getNumSignals();
	// ArrayList<ChannelAllocation> sol = new ArrayList<ChannelAllocation>(n);
	// for (int i = 0; i < n; i++) {
	// ChannelAllocation ca = signals.get(i);
	//
	// int m = ca.connections.size();
	// if (m < 2) {
	// ca.weight = 0;
	// sol.add(ca);
	// continue;
	// }
	//
	// // just look at the first two connections to determine
	// // if this is an upwards or downwards link
	//
	// ChannelConnection cc1 = ca.connections.get(0);
	// ChannelConnection cc2 = ca.connections.get(1);
	// if (cc1.left) {
	// if (cc1.ypos < cc2.ypos) {
	// // upwards
	// ca.weight = -cc1.ypos;
	// } else {
	// // downwards
	// ca.weight = cc1.ypos;
	// }
	// } else {
	// if (cc1.ypos > cc2.ypos) {
	// // upwards
	// ca.weight = -cc2.ypos;
	// } else {
	// // downwards
	// ca.weight = cc2.ypos;
	// }
	// }
	// sol.add(ca);
	// }
	//
	// Collections.sort(sol, new Comparator<ChannelAllocation>() {
	// public int compare(ChannelAllocation ca1, ChannelAllocation ca2) {
	// return ca1.weight - ca2.weight;
	// }
	// });
	//
	// // solution = new ChannelAllocation[n];
	// //
	// // for (int i = 0; i < n; i++) {
	// // int m = getNumSignals();
	// // int top = Integer.MAX_VALUE;
	// // int idx = 0;
	// // for (int j = 0; j<m; j++) {
	// //
	// // ChannelAllocation ca = signals.get(j);
	// // if (ca.min < top) {
	// // top = ca.min;
	// // idx = j;
	// // }
	// // }
	// // solution[i] = signals.get(idx);
	// // signals.remove(solution[i].signal);
	// // }
	//
	// signals = new HashMapArray<RTLSignal, ChannelAllocation>();
	// for (int i = 0; i < n; i++) {
	// // ChannelAllocation ca = solution[i];
	// ChannelAllocation ca = sol.get(i);
	// signals.put(ca.signal, ca);
	// ca.pos = i;
	// }
	// }
	//
	// public void optimizeSignalOrderingSwap() {
	//
	// System.out.println("Optimizing channel: " + idx);
	//
	// bestScore = Integer.MIN_VALUE;
	// int n = getNumSignals();
	// val = new int[n];
	// solution = new ChannelAllocation[n];
	// for (int i = 0; i < n; i++) {
	// val[i] = i;
	// }
	//
	// int oldBestScore = bestScore;
	// while (true) {
	// for (int i = 0; i < n; i++) {
	// int score = calcSignalOrderScore();
	// updateSolution(score);
	// for (int j = 0; j < n; j++) {
	//
	// if (i == j)
	// continue;
	//
	// int h = val[j];
	// val[j] = val[i];
	// val[i] = h;
	//
	// score = calcSignalOrderScore();
	// if (score > bestScore)
	// updateSolution(score);
	// else {
	// h = val[j];
	// val[j] = val[i];
	// val[i] = h;
	// }
	// }
	// }
	// if (bestScore > oldBestScore) {
	// oldBestScore = bestScore;
	// } else
	// break;
	// }
	// signals = new HashMapArray<RTLSignal, ChannelAllocation>();
	// for (int i = 0; i < n; i++) {
	// ChannelAllocation ca = solution[i];
	// signals.put(ca.signal, ca);
	// ca.pos = i;
	// }
	// }
	//
	// public void optimizeSignalOrderingExh() {
	//
	// System.out.println("Optimizing channel: " + idx);
	//
	// bestScore = Integer.MIN_VALUE;
	// int n = getNumSignals();
	// val = new int[n];
	// solution = new ChannelAllocation[n];
	// for (int i = 0; i < n; i++) {
	// val[i] = -1;
	// }
	// now = -2;
	// visit(0);
	//
	// signals = new HashMapArray<RTLSignal, ChannelAllocation>();
	// for (int i = 0; i < n; i++) {
	// ChannelAllocation ca = solution[i];
	// signals.put(ca.signal, ca);
	// ca.pos = i;
	// }
	// }

	public void clearAllocators() {
		int n = getNumSignals();
		for (int i = 0; i < n; i++) {
			ChannelAllocation ca = signals.get(i);
			ca.reset();
		}
	}

	public boolean isModulePlacedAt(int pos_) {

		int n = getNumSlots();
		for (int i = 0; i < n; i++) {
			VisualModule module = getModule(i);
			if (module == null)
				continue;

			int ypos = module.getYPos();
			int h = module.getSymbol().getHeight();
			if ((pos_ >= ypos) && (pos_ <= ypos + h))
				return true;

		}
		return false;
	}

	public void add(VisualModule module_) {

		// sanity check
		int n = slots.size();
		for (int i = 0; i < n; i++) {
			if (slots.get(i) == module_)
				logger.error("Channel.add(): sanity check failed!");
		}

		slots.add(module_);
		module_.setCol(idx);
		module_.setVChannel(this);

		if (slots.size() < 2)
			module_.setYPos(0);
		else {
			VisualModule module = slots.get(slots.size() - 2);
			module_.setYPos(module.getYPos() + module.getHeight() + 50);
		}
	}

	public RTLSignal getSignalHit(RTLView rtlc_, int mx_, int my_) {
		
//		if (idx==0)
//			System.out.println ("[ MOUSE DOWN ] "+mx_+"/"+my_);
		
		int w = rtlc_.tW(WIRE_WIDTH/2);
		if (w<4)
			w = 4;
//		if (idx==0)
//			System.out.println("  [WIRE WIDTH] "+w);
		
		int nSignals = signals.size();
		for (int iSignals1 = 0; iSignals1 < nSignals; iSignals1++) {
			ChannelAllocation ca = signals.get(iSignals1);
			VisualSignal s = ca.signal;
//			if (idx==0)
//				System.out.println("  [ SIGNAL ] "+s.getId());

			int x2 = rtlc_.tX(pos + ca.pos * WIRE_WIDTH);
			
//			if (idx==0)
//				System.out.println("    [ XPOS ] x2="+x2);

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			int nConnections1 = ca.connections.size();
			for (int iConnections1 = 0; iConnections1 < nConnections1; iConnections1++) {

				ChannelConnection conn1 = ca.connections.get(iConnections1);

				int pos = conn1.getYPos();
				if (pos < min)
					min = pos;
				if (pos > max)
					max = pos;
				
//				if (idx==0)
//					System.out.println ("      [CONNECTION] ypos="+pos);

				if (conn1.p != null) {
					int y1 = rtlc_.tY(par.getPortPosition(conn1.p).y);
					int x1 = rtlc_.tX(par.getPortPosition(conn1.p).x);
					
					//System.out.println ("      [PORTCONN] pos="+x1+"/"+y1+" p="+conn1.p);
					
					int x3 = x2;

					if (x1 > x3) {
						int h = x1;
						x1 = x3;
						x3 = h;
						x3 += rtlc_.tW(20);
					} else {
						x1 -= rtlc_.tW(20);
					}

					if (mx_ >= x1 && mx_ <= x3 && my_ >= y1 - w
							&& my_ <= y1 + w) {
						return ca.signal.getRTLSignal();
					}
				}
			}

			if (ca.c != null) {
				if (ca.yPos > max)
					max = ca.yPos;
				if (ca.yPos < min)
					min = ca.yPos;
				
				int y1 = rtlc_.tY(ca.yPos);

				int x1 = rtlc_.tX(ca.c.getSignalXPos(s));
				
				//System.out.println ("      [CHANNELCONN] pos="+x1+"/"+y1);
				
				int x3 = x2;

				if (x1 > x3) {
					int h = x1;
					x1 = x3;
					x3 = h;
				}

				if (mx_ >= x1 && mx_ <= x3 && my_ >= y1 - w
						&& my_ <= y1 + w) {
					return s.getRTLSignal();
				}
			}

			
			min = rtlc_.tY(min);
			max = rtlc_.tY(max);
			
//			if (idx==0)
//				System.out.println("    [ YMINMAX ] min="+min+" max="+max);

			if (mx_ >= x2 - w && mx_ <= x2 + w && my_ >= min
					&& my_ <= max) {
				return s.getRTLSignal();
			}

		}

		return null;
	}
	
	public int getSignalYPos(VisualSignal s_) {
		ChannelAllocation ca = signals.get(s_);
		if (ca == null)
			return 0;
		return ca.yPos;
	}
	
	public VisualModule getCollidingModule(VisualModule module_, int yPos_){
		int n = getNumSlots();
		
		int ymin1 = yPos_;
		int ymax1 = yPos_+module_.getHeight();
		
		for (int i = 0; i < n; i++) {
			VisualModule m2 = getModule(i);

			if (m2 == module_)
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
 }