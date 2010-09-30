/*
 * Copyright 2006-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 */

package org.zamia.plugin.views.rtl;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.plugin.views.rtl.symbols.GenericSymbol;
import org.zamia.plugin.views.rtl.symbols.Symbol;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLInputPort;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLOutputPort;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class PlaceAndRoute {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();
	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Channel channels[];

	private VisualGraph vg;
	private RTLGraph rtlg;

	private RTLView view;

	private HashSet<RTLModule> visibleModules;

	private Position totalSize = null;

	// printing
	public final static int PAGE_HEIGHT = 841;

	public final static int PAGE_WIDTH = 595;

	// private static final double OPTIMIZER_TIMEOUT = 3.0; // 3 seconds

	private static final int EXPAND_BUTTON_SIZE = 30;

	private static final int EXPAND_BUTTON_BORDER = 5;

	public double printZoomFactor;

	// these might be swapped in landscape mode:
	private int pageWidth = PAGE_WIDTH;

	private int pageHeight = PAGE_HEIGHT;

	private int mediumWidth = PAGE_WIDTH;

	private int mediumHeight = PAGE_HEIGHT;

	private HashSetArray<VisualPort> expandablePorts;

	private HashMap<VisualModule, Integer> moduleDepth;

	private HashMap<Integer, ArrayList<VisualModule>> depthModule;

	private int maxDepth;

	private HashMapArray<VisualPort, VisualSignal> metaSignals;

	private boolean showPins;
	private boolean showBuiltins;

	public PlaceAndRoute(RTLView control_) {
		view = control_;
	}

	public boolean isModuleVisible(VisualModule module_) {
		RTLModule rtlm = module_.getRTLModule();
		
		if (rtlm == null)
			return false;
		
		boolean visible = visibleModules.contains(rtlm);
		
//		logger.info ("Module '%s' is visible: "+visible, rtlm);
		
		return visible;
	}

	private void computeMetaSignals() {

		metaSignals = new HashMapArray<VisualPort, VisualSignal>();

		HashMapArray<VisualPort, VisualPort> metaPorts = new HashMapArray<VisualPort, VisualPort>();

		// compute meta ports (first input, first output of each module)

		int n = vg.getNumSubs();
		for (int i = 0; i < n; i++) {

			VisualModule sub = vg.getSub(i);

			VisualPort input = null;
			VisualPort output = null;

			int nPorts = sub.getNumPorts();
			for (int j = 0; j < nPorts; j++) {

				VisualPort p = sub.getPort(j);

				if (p.getDirection() == PortDir.IN) {

					if (input == null) {
						input = p;
					}

					metaPorts.put(p, input);
				} else {

					if (output == null) {
						output = p;
					}

					metaPorts.put(p, output);
				}
			}
		}

		// compute meta signals

		n = vg.getNumSubs();
		for (int i = 0; i < n; i++) {

			VisualModule sub = vg.getSub(i);

			int nPorts = sub.getNumPorts();
			for (int j = 0; j < nPorts; j++) {

				VisualPort p = sub.getPort(j);

				if (p.getDirection() == PortDir.IN)
					continue;

				VisualPort src = metaPorts.get(p);

				VisualSignal s = p.getSignal();

				if (s == null)
					continue;

				VisualSignal metaSignal = metaSignals.get(src);

				if (metaSignal == null) {
					metaSignal = new VisualSignal(vg, s.getRTLSignal());
					metaSignal.addPortConn(src);
					metaSignals.put(src, metaSignal);
				}

				int m = s.getNumConns();
				for (int k = 0; k < m; k++) {

					p = s.getConn(k);

					if (p.getDirection() != PortDir.IN)
						continue;

					VisualPort dest = metaPorts.get(p);

					metaSignal.addPortConn(dest);
				}
			}
		}

	}

	/**
	 * place all modules on channels according to their logic depth
	 * 
	 */
	private void prePlace() {

		if (vg == null) {
			vg = new VisualGraph(rtlg, view);
		}
		
		// levelize stuff
		if (moduleDepth == null) {
			moduleDepth = new HashMap<VisualModule, Integer>();
			depthModule = new HashMap<Integer, ArrayList<VisualModule>>();
			
			if (!showBuiltins) {
				vg.removeBuiltins();
			}

			maxDepth = vg.levelize(moduleDepth, depthModule);
		}

		if (metaSignals == null) {

			computeMetaSignals();

		}

		channels = new Channel[maxDepth + 2];

		for (int i = 0; i < maxDepth + 2; i++) {
			channels[i] = new Channel(this, i);
		}

		int n = vg.getNumSubs();
		HashSet<VisualModule> modules = new HashSet<VisualModule>(n);
		ArrayList<VisualModule> stack = new ArrayList<VisualModule>();

		for (int i = 0; i < n; i++) {
			VisualModule module = vg.getSub(i);
			if (!isModuleVisible(module))
				continue;

			modules.add(module);

			if (module.getRTLModule() instanceof RTLInputPort) {
				stack.add(module);
			}
		}

		int finalCol = channels.length - 1;

		while (!modules.isEmpty()) {
			while (!stack.isEmpty()) {

				VisualModule module = stack.remove(stack.size() - 1);
				if (!modules.contains(module))
					continue;
				modules.remove(module);

				int col = moduleDepth.get(module);
				if (module.getRTLModule() instanceof RTLOutputPort) {
					col = finalCol;
				}

				module.setCol(col);
				channels[col].add(module);

				// System.logger.debug("Module " + module + " is in column " +
				// col);

				ArrayList<VisualModule> l = module.getSuccessors();
				for (int i = 0; i < l.size(); i++) {
					VisualModule m2 = l.get(i);
					if (modules.contains(m2))
						stack.add(m2);
				}

				l = module.getPredecessors();
				for (int i = 0; i < l.size(); i++) {
					VisualModule m2 = l.get(i);
					if (!(m2.getRTLModule() instanceof RTLLiteral))
						continue;
					if (modules.contains(m2)) {
						modules.remove(m2);

						channels[col - 1].add(m2);
					}
				}

			}
			if (!modules.isEmpty())
				stack.add(modules.iterator().next());
		}

		for (int i = 0; i < n; i++) {
			VisualModule module = vg.getSub(i);

			if (!isModuleVisible(module))
				continue;

			// if (module instanceof RTLOutputPort) {
			// GBox box = new GBox(module, finalCol, view);
			// gBoxMap.put(module, box);
			// channels[finalCol].add(box);
			//
			// }

			// if (!gBoxMap.containsKey(module)) {
			// System.logger.debug("WARNING: " + module
			// + " has not been placed.");
			//
			// int col = 0;
			//
			// ArrayList<VisualModule> l = module.getSuccessors();
			// GBox box;
			// for (int j = 0; j < l.size(); j++) {
			// VisualModule m2 = l.get(j);
			//
			// box = gBoxMap.get(m2);
			//
			// if (box != null) {
			// col = box.getCol() - 1;
			// break;
			// }
			// }
			// if (col <= 0) {
			//
			// l = module.getPredecessors();
			// for (int j = 0; j < l.size(); j++) {
			// VisualModule m2 = l.get(j);
			// box = gBoxMap.get(m2);
			//
			// if (box != null) {
			// col = box.getCol() + 1;
			// break;
			// }
			// }
			// }
			// if (col <= 0)
			// col = 0;
			//
			// box = new GBox(module, col, view);
			// gBoxMap.put(module, box);
			// channels[col].add(box);
			// }
		}
	}

	// private int calcPrePlaceCost(VisualModule g_, int grow_) {
	//
	// int cost = 0;
	// int gcol = board.getCol(g_);
	// int n = g_.getNumPorts();
	// for (int i = 0; i < n; i++) {
	// VisualPort p = g_.getPort(i);
	//
	// if (p.getDirection() == PortDir.IN) {
	//
	// VisualSignal s = p.getSignal();
	// if (s == null)
	// continue;
	//
	// if (s.getNumDrivers() < 1)
	// continue;
	//
	// VisualPort dp = s.getDriver(0);
	// VisualModule driver = dp.getModule();
	//
	// int dcol = board.getCol(driver);
	// if (dcol != (gcol - 1))
	// continue;
	// int drow = board.getRow(driver);
	//
	// cost += Math.abs(drow - grow_);
	// }
	// }
	//
	// return cost;
	// }
	//
	// private void optimizePrePlacement() {
	// // go through vertical channels and
	// // (greedily) optimize module placement according to
	// // our cost function (number of non-neighbouring port connections)
	//
	// int n = board.getWidth();
	// int m = board.getHeight();
	// for (int i = 1; i < n; i++) {
	//
	// while (true) {
	// // find pair of modules with highest gain when exchanged
	// int highGain = Integer.MIN_VALUE;
	// int highRow1 = 0;
	// int highRow2 = 0;
	//
	// Channel c = board.getVC(i);
	// for (int j = 0; j < m; j++) {
	// GBox box = c.getGBox(j);
	// VisualModule module1 = null;
	// if (box != null) {
	// module1 = box.getModule();
	// }
	//
	// for (int k = 0; k < m; k++) {
	// box = c.getGBox(k);
	// if (box == null)
	// continue;
	// VisualModule module2 = box.getModule();
	// if (module2 == null)
	// continue;
	// if (module1 == module2)
	// continue;
	//
	// // System.logger.debug("testing " + module1 + " against" +
	// // module2);
	//
	// int cost11 = 0;
	// if (module1 != null)
	// cost11 = calcPrePlaceCost(module1, j);
	// int cost22 = calcPrePlaceCost(module2, k);
	//
	// int cost12 = 0;
	// if (module1 != null)
	// cost12 = calcPrePlaceCost(module1, k);
	//
	// int cost21 = calcPrePlaceCost(module2, j);
	//
	// int gain = (cost11 + cost22) - (cost12 + cost21);
	//
	// // System.logger.debug("Gain: " + gain);
	//
	// if (gain > highGain) {
	// highGain = gain;
	// highRow1 = j;
	// highRow2 = k;
	// }
	// }
	// }
	//
	// if (highGain > 0) {
	// board.exchange(i, highRow1, highRow2);
	// } else
	// break;
	// }
	// }
	// }
	//
	// private void optimizePinPositions() {
	// if (fixPinPositions)
	// return;
	//
	// // optimize pin position of genericsymbols
	//
	// int n = board.getWidth();
	// for (int i = 0; i < n; i++) {
	//
	// Channel c = board.getVC(i);
	//
	// int m = c.getNumSlots();
	// for (int j = 0; j < m; j++) {
	// GBox box = c.getGBox(j);
	// if (box == null)
	// continue;
	// VisualModule module = box.getModule();
	// if (module == null)
	// continue;
	// Symbol sym = box.getSymbol();
	// if (!(sym instanceof GenericSymbol))
	// continue;
	// GenericSymbol gs = (GenericSymbol) sym;
	//
	// int l = module.getNumPorts();
	// for (int k = 0; k < l; k++) {
	// VisualPort p = module.getPort(k);
	// VisualSignal s = p.getSignal();
	// if (s == null)
	// continue;
	// if (p.getDirection() != PortDir.IN) {
	//
	// int nReaders = s.getNumReaders();
	// if (nReaders > 0) {
	// int pinoff = 0;
	// for (int iReader = 0; iReader < nReaders; iReader++) {
	//
	// VisualPort rp = s.getReader(iReader);
	//
	// VisualModule reader = rp.getModule();
	//
	// GBox dbox = board.getGBox(reader);
	//
	// Symbol dsym = dbox.getSymbol();
	// int poff = dsym.getPortPosition(rp).y;
	//
	// // bartsch changed 12.03.2006
	// // pinoff += dbox.getRow() * 1000 + poff;
	// pinoff += dbox.getYPos() + poff;
	// }
	// gs.setPinHint(p, pinoff / nReaders);
	// }
	// } else {
	//
	// if (s.getNumDrivers() < 1)
	// continue;
	//
	// VisualPort dp = s.getDriver(0);
	//
	// VisualModule driver = dp.getModule();
	//
	// GBox dbox = board.getGBox(driver);
	//
	// Symbol dsym = dbox.getSymbol();
	// int pinoff = dsym.getPortPosition(dp).y;
	//
	// // bartsch changed 12.03.2006
	// // gs.setPinHint(p, dbox.getRow() * 1000 + pinoff);
	// gs.setPinHint(p, dbox.getYPos() + pinoff);
	// }
	// }
	// gs.sortPins();
	// }
	// }
	// }
	//

	Position getPortPosition(VisualPort p_) {
		return p_.getModule().getPortPosition(p_.getRTLPort());
	}

	public int getCol(VisualModule module_) {
		return module_.getCol();
	}

	public int getCol(VisualPort p) {
		return getCol(p.getModule());
	}

	private int calcVisualModulePlaceScore(VisualModule box_) {

		Channel c = box_.getVChannel();

		int ymin1 = box_.getYPos();
		int ymax1 = ymin1 + box_.getHeight();

		// collision check:
		int n = c.getNumSlots();
		for (int i = 0; i < n; i++) {
			VisualModule b2 = c.getModule(i);

			if (b2 == box_)
				continue;

			int ymin2 = b2.getYPos();
			int ymax2 = ymin2 + b2.getHeight();

			if (ymin2 <= ymin1 && ymax2 >= ymin1)
				return Integer.MIN_VALUE;
			if (ymin2 >= ymin1 && ymax2 <= ymax1)
				return Integer.MIN_VALUE;
			if (ymin2 <= ymax1 && ymax2 >= ymax1)
				return Integer.MIN_VALUE;

		}

		int score = 0;

		int l = box_.getNumPorts();
		for (int k = 0; k < l; k++) {
			VisualPort p = box_.getPort(k);
			VisualSignal s = p.getSignal();
			if (s == null)
				continue;

			Position pos1 = getPortPosition(p);
			int x1 = pos1.x;
			int y1 = pos1.y;
			if (p.getDirection() != PortDir.IN) {

				if (box_.getNumInputs() == 0) {
					int nConns = s.getNumConns();
					if (nConns > 0) {
						for (int iConn = 0; iConn < nConns; iConn++) {

							VisualPort rp = s.getConn(iConn);

							if (rp.getDirection() == PortDir.OUT) {
								continue;
							}

							VisualModule rm = rp.getModule();
							if (!isModuleVisible(rm))
								continue;

							Position pos2 = getPortPosition(rp);
							int x2 = pos2.x;
							int y2 = pos2.y;
							int ydist = Math.abs(y1 - y2);
							int xdist = Math.abs(x1 - x2) + 1;

							boolean blocked = false;
							int sc = getCol(p) + 1;
							int dc = getCol(rp) - 1;
							for (int x = sc; x <= dc; x++) {

								if (isModulePlacedAt(x, y1)) {
									blocked = true;
								}
							}

							if (!blocked) {
								if (y1 == y2) {

									score += 100000;
								} else {
									score -= ydist * 100 / xdist;
								}
							} else {
								score -= 50000;
							}

						}
					}
				}

			} else {

				VisualPort dp = null;

				int nConns = s.getNumConns();
				for (int iConn = 0; iConn < nConns; iConn++) {

					VisualPort vp = s.getConn(iConn);
					if (p.getDirection() == PortDir.OUT) {
						dp = vp;
						break;
					}
				}
				if (dp == null)
					continue;

				VisualModule dm = dp.getModule();
				if (!isModuleVisible(dm))
					continue;

				Position pos2 = getPortPosition(dp);
				int x2 = pos2.x;
				int y2 = pos2.y;
				int ydist = Math.abs(y1 - y2);
				int xdist = Math.abs(x1 - x2) + 1;

				boolean blocked = false;
				int sc = getCol(dp) + 1;
				int dc = getCol(p) - 1;
				for (int x = sc; x <= dc; x++) {

					if (isModulePlacedAt(x, y1)) {
						blocked = true;
					}
				}

				if (!blocked) {
					if (y1 == y2) {

						score += 100000;
					} else {
						score -= ydist * 100 / xdist;
					}
				} else {
					score -= 50000;
				}
			}
		}
		return score;
	}

	public Channel getVC(int col_) {
		return channels[col_];
	}

	private boolean isModulePlacedAt(int col_, int y_) {
		Channel c = getVC(col_);
		return c.isModulePlacedAt(y_);

	}

	private void optimizeYPlacement(VisualModule module_) {
		int bestScore = calcVisualModulePlaceScore(module_);
		int bestYPos = module_.getYPos();

		int l = module_.getNumPorts();

		for (int k = 0; k < l; k++) {
			VisualPort p = module_.getPort(k);
			VisualSignal s = p.getSignal();
			if (s == null)
				continue;

			// if (module_.getNumInputs() > 0 && p.getDirection() != PortDir.IN)
			// continue;

			Position offset = module_.getPortOffset(p.getRTLPort());
			// Position pos1 = getPortPosition(p);

			int nConns = s.getNumConns();
			for (int iConn = 0; iConn < nConns; iConn++) {

				VisualPort p2 = s.getConn(iConn);

				if (p2 == p)
					continue;

				VisualModule p2m = p2.getModule();
				if (!isModuleVisible(p2m))
					continue;

				int ypos = getPortPosition(p2).y - offset.y;

				if (ypos < 0)
					continue;

				module_.setYPos(ypos);

				int score = calcVisualModulePlaceScore(module_);
				// System.logger.debug(" Score for " +
				// module.getInstanceName() + " at ypos " + ypos +
				// ": " + score);
				if (score > bestScore) {
					bestScore = score;
					bestYPos = ypos;
				}
			}
		}

		// System.logger.debug("*******" + module + "Best Y pos
		// score: " + bestScore + " YPos: " + bestYPos);

		module_.setYPos(bestYPos);

	}

	private void optimizeYPlacement() {

		// find best non-colliding y position for each box

		int n = channels.length;
		// always run this optimization several times
		// since modules moved later may make room for
		// modules which were moved before them

		// long startTime = System.currentTimeMillis();
		//
		// for (int r = 0; r < 30; r++) {
		// System.logger.debug("===================== optimize rtl module
		// placement iter=" + r + " ===========");

		// left to right

		for (int i = 1; i < n; i++) {

			// System.logger.debug(" ++++++++++++++ Channel #" + i);

			Channel c = channels[i];

			int m = c.getNumSlots();
			for (int j = 0; j < m; j++) {
				VisualModule module = c.getModule(j);
				if (module == null)
					continue;

				optimizeYPlacement(module);
			}
		}

		// right to left

		for (int i = n - 2; i >= 0; i--) {

			// System.logger.debug(" ++++++++++++++ Channel #" + i);

			Channel c = channels[i];

			int m = c.getNumSlots();
			for (int j = 0; j < m; j++) {
				VisualModule module = c.getModule(j);
				if (module == null)
					continue;

				optimizeYPlacement(module);
			}
		}

		// double time = (System.currentTimeMillis() - startTime) / 1000.0;
		// if (time > OPTIMIZER_TIMEOUT) {
		// break;
		// }
		// }
	}

	//
	// private void optimizePortPlacement() {
	// int n = rtlg.getNumSubs();
	// for (int i = 0; i < n; i++) {
	// VisualModule module = rtlg.getSub(i);
	// if (!(module instanceof VisualPortModule))
	// continue;
	//
	// if (!isModuleVisible(module))
	// continue;
	//
	// VisualPortModule port = (VisualPortModule) module;
	//
	// VisualPort internalPort = port.getInternalPort();
	// VisualSignal s = internalPort.getSignal();
	// if (s == null)
	// continue;
	//
	// VisualSignal signalToRoute = s;
	//
	// // System.logger.debug("optimizing port placement for : "
	// // + internalPort);
	//
	// // find connected pins
	// int numConns = s.getNumConns();
	// for (int j = 0; j < numConns; j++) {
	// VisualPort p = s.getConn(j);
	// if (p != internalPort) {
	//
	// // way from pin to primary port free?
	//
	// VisualPort source = internalPort;
	// VisualPort dest = p;
	//
	// int sr = board.getRow(source);
	// int sc = board.getCol(source);
	// int dr = board.getRow(dest);
	// int dc = board.getCol(dest);
	// if (source.getDirection() == PortDir.OUT)
	// sc++;
	// if (dest.getDirection() == PortDir.OUT)
	// dc++;
	//
	// // source should be left, dest should be right
	// if (sc > dc) {
	// int tmp;
	// tmp = sr;
	// sr = dr;
	// dr = tmp;
	// tmp = sc;
	// sc = dc;
	// dc = tmp;
	// VisualPort tmpPrt;
	// tmpPrt = source;
	// source = dest;
	// dest = tmpPrt;
	// }
	//
	// boolean blocking = false;
	// int row = sr;
	// if (source == internalPort)
	// row = dr;
	// for (int x = sc; x < dc; x++) {
	// if (board.getGBoxAt(x, row) != null) {
	// blocking = true;
	// }
	// }
	// // System.logger.debug("block check for " + source + " <-> "
	// // + dest + " => blocking: " + blocking);
	//
	// if (!blocking) {
	// int y = board.getPortPosition(p).y;
	//
	// board.setPortYPlacementHint(internalPort, y);
	// continue;
	// }
	//
	// Channel c = board.getHChannel(signalToRoute);
	//
	// board.setPortYPlacementHint(internalPort, c.getSignalIdx(signalToRoute) +
	// c.getPos());
	// }
	// }
	// }
	//
	// if (board.getWidth() > 1) {
	// board.resolveCollisions(0);
	// board.resolveCollisions(board.getWidth() - 1);
	// }
	// }
	//
	// private void preRouteSignal(VisualSignal s, VisualPort source, VisualPort
	// dest) {
	//
	// // System.logger.debug("Routing signal " + s + " from " + source + " to "
	// // + dest);
	//
	// if (!isModuleVisible(source.getModule()) ||
	// !isModuleVisible(dest.getModule()))
	// return;
	//
	// /* source/dest col/rows */
	//
	// int sr = board.getRow(source);
	// int sc = board.getCol(source);
	// int dr = board.getRow(dest);
	// int dc = board.getCol(dest);
	// if (source.getDirection() == PortDir.OUT)
	// sc++;
	// if (dest.getDirection() == PortDir.OUT)
	// dc++;
	//
	// // we want to route from left to right
	// if (sc > dc) {
	// int tmp;
	// tmp = sr;
	// sr = dr;
	// dr = tmp;
	// tmp = sc;
	// sc = dc;
	// dc = tmp;
	// VisualPort tmpPrt;
	// tmpPrt = source;
	// source = dest;
	// dest = tmpPrt;
	// }
	//
	// VisualSignal signalToRoute = s;
	//
	// System.logger.debug("Routing signal " + signalToRoute + " from " + source
	// + " to " + dest + " source coord: " + sc + "/" + sr + ", dest coord: " +
	// dc + "/" + dr);
	//
	// // step 1: move from pin to next vert. channel
	// board.addSignalV(signalToRoute, sc);
	//
	// if (sc != dc) {
	//
	// int hr;
	//
	// // step 2: move up/down to dest hor. channel
	// if (dr > sr)
	// hr = dr;
	// else
	// hr = sr;
	// board.addSignalH(signalToRoute, hr);
	//
	// // step 3: move left/right to dest vert. channel
	// board.addSignalV(signalToRoute, dc);
	// }
	// // else
	// // board.addSignalH(signalToRoute, sr);
	// }
	//
	// private void preRoute() {
	//
	// HashSet<VisualSignal> routedSignals = new HashSet<VisualSignal>();
	//
	// int nCols = board.getWidth();
	// for (int i = 0; i < nCols; i++) {
	//
	// int nRows = board.getHeight();
	//
	// for (int j = 0; j < nRows; j++) {
	//
	// GBox box = board.getVC(i).getGBox(j);
	// if (box == null)
	// continue;
	// VisualModule g = box.getModule();
	// if (g == null)
	// continue;
	//
	// int nPorts = g.getNumPorts();
	// for (int k = 0; k < nPorts; k++) {
	// VisualPort sp = g.getPort(k);
	// VisualSignal s = sp.getSignal();
	//
	// if (s == null)
	// continue;
	//
	// if (!routedSignals.contains(s)) {
	// routedSignals.add(s);
	//
	// int nConns = s.getNumConns();
	// for (int l = 0; l < nConns; l++) {
	// VisualPort dp = s.getConn(l);
	// if (sp == dp)
	// continue;
	// // System.logger.debug(" routing from " + sp + " to "
	// // + dp);
	// preRouteSignal(s, sp, dp);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	//
	private void routeSignal(VisualSignal s, VisualPort source, VisualPort dest) {

		/* source/dest cols */

		int sc = getCol(source);
		int dc = getCol(dest);
		if (source.getDirection() == PortDir.OUT)
			sc++;
		if (dest.getDirection() == PortDir.OUT)
			dc++;

		// we want to route from left to right
		if (sc > dc) {
			int tmp;
			tmp = sc;
			sc = dc;
			dc = tmp;
			VisualPort tmpPrt;
			tmpPrt = source;
			source = dest;
			dest = tmpPrt;
		}

		Channel c = channels[sc];
		if (c.addPinConnection(source, s) && showPins) {
			VisualModule module = source.getModule();
			Symbol sym = module.getSymbol();
			if (sym instanceof GenericSymbol) {
				GenericSymbol gs = (GenericSymbol) sym;
				gs.tweakPortPosition(source.getRTLPort());
			}
		}

		int y = getPortPosition(source).y;

		while (sc < dc) {
			sc++;
			Channel c2 = channels[sc];
			y = c.addChannelConnection(s, c2, y);
			c = c2;
		}

		if (c.addPinConnection(dest, s) && showPins) {
			VisualModule module = dest.getModule();
			Symbol sym = module.getSymbol();
			if (sym instanceof GenericSymbol) {
				GenericSymbol gs = (GenericSymbol) sym;
				gs.tweakPortPosition(dest.getRTLPort());
			}
		}

		// VisualSignal signalToRoute = s;
		//
		// // System.logger.debug("Routing signal from " + source + " " + sc +
		// "/"
		// // + sr + " to " + dest + " " + dc + "/" + dr);
		//
		// // small optimization: no modules blocking the horizontal way?
		// boolean blockingSR = false;
		// for (int x = sc; x < dc; x++) {
		//
		// int y = board.getPortPosition(source).y;
		//
		// if (board.isModulePlacedAt(x, y)) {
		// blockingSR = true;
		// }
		// }
		//
		// boolean blockingDR = false;
		// for (int x = sc; x < dc; x++) {
		// int y = board.getPortPosition(dest).y;
		// if (board.isModulePlacedAt(x, y)) {
		// blockingDR = true;
		// }
		// }
		//
		// Channel vc = null;
		// int cx = 0;
		//
		// if (blockingSR) {
		//
		// /*
		// * move from pin to next vert. channel
		// */
		//
		// vc = board.getVC(sc);
		// int offset = vc.getSignalIdx(signalToRoute);
		//
		// cx = vc.getPos() + offset;
		// int cy = board.getPortPosition(source).y;
		//
		// board.addPinConnection(source, cx);
		//
		// vc.addConnector(signalToRoute, cy, true, source);
		//
		// if (blockingDR) {
		//
		// // move up/down to signal hor. channel
		// Channel hc = board.getHChannel(signalToRoute);
		// offset = hc.getSignalIdx(signalToRoute);
		// cy = hc.getPos() + offset;
		// hc.addConnector(signalToRoute, cx, true, null);
		// vc.addConnector(signalToRoute, cy, false, null);
		//
		// // move left/right to dest vert. channel
		// vc = board.getVC(dc);
		// offset = vc.getSignalIdx(signalToRoute);
		// cx = vc.getPos() + offset;
		// hc.addConnector(signalToRoute, cx, true, null);
		// vc.addConnector(signalToRoute, cy, true, null);
		// }
		// } else {
		// /*
		// * move from pin to dest vert. channel
		// */
		//
		// vc = board.getVC(dc);
		// int offset = vc.getSignalIdx(signalToRoute);
		// cx = vc.getPos() + offset;
		// int cy = board.getPortPosition(source).y;
		//
		// board.addPinConnection(source, cx);
		// vc.addConnector(signalToRoute, cy, true, null);
		// }
		//
		// // move up/down to pin
		// Position ppos = board.getPortPosition(dest);
		// int pposy = ppos.y;
		// vc.addConnector(signalToRoute, pposy, false, dest);
		//
		// // move left/right to pin
		// board.addPinConnection(dest, cx);
	}

	private void routeSignal(VisualSignal signal) {
		// connect the leftmost port in the list with all
		// the others

		int pos = Integer.MAX_VALUE;
		VisualPort sp = null;
		int m = signal.getNumConns();
		for (int j = 0; j < m; j++) {
			VisualPort port = signal.getConn(j);
			VisualModule module = port.getModule();
			if (!isModuleVisible(module)) {
				continue;
			}

			int d = module.getCol();
			if (d < pos) {
				pos = d;
				sp = port;
			}
		}
		if (sp == null)
			return;

		if (!isModuleVisible(sp.getModule()))
			return;

		int num = signal.getNumConns();
		for (int j = 0; j < num; j++) {
			VisualPort dp = signal.getConn(j);
			if (sp == dp)
				continue;
			// System.logger.debug(" routing from " + sp + " to " + dp);

			if (!isModuleVisible(dp.getModule())) {
				expandablePorts.add(sp);
			} else {
				routeSignal(signal, sp, dp);
			}
		}
	}

	private void route() {

		// now route the signals

		if (showPins) {

			int n = vg.getNumSignals();
			for (int i = 0; i < n; i++) {
				VisualSignal signal = vg.getSignal(i);
				routeSignal(signal);
			}

		} else {

			int n = metaSignals.size();
			for (int i = 0; i < n; i++) {
				VisualSignal signal = metaSignals.get(i);
				routeSignal(signal);
			}

		}
	}

	//
	// private void optimizeVChannelSignalOrdering() {
	// int n = board.getWidth();
	// for (int i = 1; i < n; i++) {
	// Channel c = board.getVC(i);
	// c.optimizeSignalOrdering();
	// }
	// }
	//
	// private void clearChannelAllocators() {
	// board.clearPinConnections();
	// int n = board.getWidth();
	// for (int i = 0; i < n; i++) {
	// Channel c = board.getVC(i);
	// c.clearAllocators();
	// }
	// n = board.getHeight();
	// for (int i = 0; i < n; i++) {
	// Channel c = board.getHC(i);
	// c.clearAllocators();
	// }
	// }

	private void finalizePlacement() {

		// compute ychannel positions and sizes

		int w = 0;
		int h = 0;

		for (int i = 0; i < channels.length; i++) {
			Channel c = channels[i];
			c.setXPos(w);
			w += c.getWidth();
			int hh = c.getHeight();
			if (hh > h)
				h = hh;
		}
		totalSize = new Position(w + 100, h + 100);

		if (showPins) {
			// optimize output port ypos
			Channel c = channels[channels.length - 1];
			Channel c2 = channels[channels.length - 2];
			int n = c.getNumSlots();
			for (int i = 0; i < n; i++) {

				VisualModule module = c.getModule(i);
				if (module == null)
					continue;

				if (!(module.getRTLModule() instanceof RTLOutputPort))
					continue;

				RTLOutputPort pm = (RTLOutputPort) module.getRTLModule();
				RTLPort p = pm.getInternalPort();
				RTLSignal s = p.getSignal();
				if (s == null)
					continue;

				VisualSignal vs = vg.getSignal(s);
				int y = c2.getSignalYPos(vs);
				if (y == 0)
					continue;

				if (c.getCollidingModule(module, y) != null)
					continue;

				Position offset = module.getPortOffset(p);

				module.setYPos(y - offset.y);
			}
		}
	}

	public Position placeAndRoute(RTLGraph rtlg_, boolean showPins_, boolean showBuiltins_,
			HashSet<RTLModule> visibleRTLModules_) {

		if (rtlg != rtlg_ || showBuiltins != showBuiltins_) {

			// prePlace needs to re-compute these

			moduleDepth = null;
			depthModule = null;
			metaSignals = null;
			vg = null;
		}

		
		rtlg = rtlg_;
		showPins = showPins_;
		showBuiltins = showBuiltins_;
		visibleModules = visibleRTLModules_;

		expandablePorts = new HashSetArray<VisualPort>();

		totalSize = new Position(50, 50);

		logger.info("\nPlace and route (PAR) starts on " + rtlg_);
		logger.info("======================================================");

		long startTime = System.currentTimeMillis();

		logger.info("");
		logger.info("Number of modules: " + rtlg_.getNumSubs());
		logger.info("Number of signals: " + rtlg_.getNumSignals());
		logger.info("");

		/*
		 * calculate the logic depth of each module - function levelize() place
		 * modules in vchannels according to their logic depth
		 */

		logger.debug("PAR 1/4: prePlace()");
		prePlace();

		/*
		 * fine-tune y placement, try to place connected ports on the same Y pos
		 */

		double time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		logger.debug("PAR 2/4: optimizeYPlacement()");
		optimizeYPlacement();

		/*
		 * route the signals
		 */

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		// if (showSignals_) {
		logger.debug("PAR 3/4: route()");
		route();
		// } else {
		// logger.debug("PAR 3/4: skipping routing (showSignals is false)");
		// }

		/*
		 * finalize placement, VChannels get their final X positions, calculate
		 * totalSize
		 */

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		logger.debug("PAR 4/4: finalizePlacement()");
		finalizePlacement();

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("\nPAR done. total time elapsed: " + time + "s.\n");

		return totalSize;
	}

	private void printRTLGraph(PrintWriter out) throws IOException {
		// draw modules
		int n = vg.getNumSubs();
		for (int i = 0; i < n; i++) {
			VisualModule module = vg.getSub(i);

			// if (hilightVisualModules != null)
			// module.paint(display, offscreenGC_,
			// hilightVisualModules.contains(box
			// .getModule()));
			// else
			// module.paint(display, offscreenGC_, false);
			module.print(this, out, false);
		}

		// draw channels
		for (int i = 0; i < channels.length; i++) {
			Channel c = channels[i];
			c.print(this, out);
		}

		logger.debug("showpage");
	}

	public void print(Format format, PrintStream outFile) throws IOException {
		int xTiles = format.getNumX(), yTiles = format.getNumY();

		// automatic landscape detection
		double aspect_graph = (double) totalSize.x / (double) totalSize.y;
		double aspect_media = (xTiles * PAGE_WIDTH)
				/ (double) (yTiles * PAGE_HEIGHT);

		int border = 20;

		boolean portrait = false;
		if (aspect_media > 1) {
			portrait = aspect_graph > 1;
		} else {
			portrait = aspect_graph < 1;
		}

		if (portrait) {
			// pageWidth = PAGE_WIDTH;
			// pageHeight = PAGE_HEIGHT;
			mediumWidth = (xTiles * (PAGE_WIDTH - 2 * border));
			mediumHeight = (yTiles * (PAGE_HEIGHT - 2 * border));
		} else {
			// pageWidth = PAGE_HEIGHT;
			// pageHeight = PAGE_WIDTH;
			mediumHeight = (xTiles * (PAGE_WIDTH - 2 * border));
			mediumWidth = (yTiles * (PAGE_HEIGHT - 2 * border));
		}

		double scaleX = mediumWidth / (double) totalSize.x;
		double scaleY = mediumHeight / (double) totalSize.y;

		if (scaleX > scaleY)
			printZoomFactor = scaleY;
		else
			printZoomFactor = scaleX;

		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);

		// generate PS code for the rtl graph
		printRTLGraph(out);

		// produce preamble
		outFile.println("%!PS-Adobe-3.0");
		if (portrait)
			outFile.println("%%BoundingBox: 0 0 " + pageWidth + " "
					+ pageHeight);
		else
			outFile.println("%%BoundingBox: 0 0 " + pageHeight + " "
					+ pageWidth);
		outFile.println("%%Creator: Zamia RTL Graph Viewer");
		outFile.println("%%DocumentData: Clean8Bit");
		outFile.println("%%DocumentPaperSizes: A4");
		if (portrait)
			outFile.println("%%Orientation: Portrait");
		else {
			outFile.println("%%Orientation: Landscape");
		}
		outFile.println("%%Pages: " + (xTiles * yTiles));
		outFile.println("%%PageOrder: Ascend");
		outFile.println("%%EndComments");

		// produce ps header

		outFile.println("/rightshow ");
		outFile.println("{ dup stringwidth pop");
		outFile.println("  120 exch sub");
		outFile.println("  0 rmoveto");
		outFile.println(" show } def");

		outFile.println("/clipping_path ");
		outFile.println("  { newpath");
		outFile.println("    " + border + " " + border + " moveto");
		outFile.println("    " + (PAGE_WIDTH - border) + " " + border
				+ " lineto");
		outFile.println("    " + (PAGE_WIDTH - border) + " "
				+ (PAGE_HEIGHT - border) + " lineto");
		outFile.println("    " + border + " " + (PAGE_HEIGHT - border)
				+ " lineto");
		outFile.println("  closepath } def");

		for (int page = 1, x = 0; x < xTiles; x++) {
			for (int y = 0; y < yTiles; y++, page++) {

				outFile.println("%%Page: " + page);
				outFile.println("%%BeginDocument: rtlgraph");
				outFile.println("gsave");

				double xOff = x * (pageWidth - 2 * border);
				double yOff = y * (pageHeight - 2 * border);

				String result = "";
				if (portrait) {
					result = "clipping_path clip " + border + " " + border
							+ " translate " + -xOff + " " + -yOff
							+ " translate ";
				} else {
					result = "clipping_path clip " + mediumHeight
							+ " 0 translate 90 rotate " + border + " "
							+ -border + " translate " + -yOff + " " + xOff
							+ " translate ";
				}
				outFile.println(result);
				outFile.println(sw.toString());
				outFile.println("grestore");
				outFile.println("%%EndDocument");
			}
		}

	}

	public double getPrintZoomFactor() {
		return printZoomFactor;
	}

	public double tXPrint(double x_) {
		return x_ * getPrintZoomFactor();
	}

	public double tYPrint(double y_) {
		double d;
		d = mediumHeight - y_ * getPrintZoomFactor();
		return d;
	}

	public double tWPrint(double x) {
		double d = x * getPrintZoomFactor();
		return d;
	}

	public double tHPrint(double y) {
		double d = y * getPrintZoomFactor();
		return d;
	}

	public void paint(RTLView viewer_, Display display_, GC gc_) {
		
		if (channels == null)
			return;

		// channels draw their connections themselves now

		for (int i = 0; i < channels.length; i++) {
			Channel c = channels[i];
			c.paint(viewer_, display_, gc_);
		}

		// expandable ports (dynamic mode)

		int n = expandablePorts.size();
		for (int i = 0; i < n; i++) {
			VisualPort port = expandablePorts.get(i);

			Position pos = getPortPosition(port);

			gc_.setBackground(viewer_.getColorScheme().getBgColor());
			gc_.setForeground(viewer_.getColorScheme().getModuleColor());
			gc_.setLineWidth((int) (4 * viewer_.getZoomFactor()));
			gc_.fillRectangle(viewer_.tX(pos.x - EXPAND_BUTTON_SIZE / 2),
					viewer_.tY(pos.y - EXPAND_BUTTON_SIZE / 2), viewer_
							.tW(EXPAND_BUTTON_SIZE), viewer_
							.tH(EXPAND_BUTTON_SIZE));
			gc_.drawRectangle(viewer_.tX(pos.x - EXPAND_BUTTON_SIZE / 2),
					viewer_.tY(pos.y - EXPAND_BUTTON_SIZE / 2), viewer_
							.tW(EXPAND_BUTTON_SIZE), viewer_
							.tH(EXPAND_BUTTON_SIZE));

			gc_.setLineWidth((int) (4 * viewer_.getZoomFactor()));

			gc_.drawLine(viewer_.tX(pos.x), viewer_.tY(pos.y
					- EXPAND_BUTTON_SIZE / 2 + EXPAND_BUTTON_BORDER), viewer_
					.tX(pos.x), viewer_.tY(pos.y + EXPAND_BUTTON_SIZE / 2
					- EXPAND_BUTTON_BORDER));
			gc_.drawLine(viewer_.tX(pos.x - EXPAND_BUTTON_SIZE / 2
					+ EXPAND_BUTTON_BORDER), viewer_.tY(pos.y), viewer_
					.tX(pos.x + EXPAND_BUTTON_SIZE / 2 - EXPAND_BUTTON_BORDER),
					viewer_.tY(pos.y));

		}
	}

	public int getNumChannels() {
		if (channels == null)
			return 0;
		return channels.length;
	}

	public Channel getChannel(int i) {
		return channels[i];
	}

	public VisualPort getHitExpandButton(RTLView rtlc_, int x_, int y_) {
		int n = expandablePorts.size();
		for (int i = 0; i < n; i++) {
			VisualPort port = expandablePorts.get(i);

			Position pos = getPortPosition(port);

			int xp = rtlc_.tX(pos.x - EXPAND_BUTTON_SIZE / 2);
			int yp = rtlc_.tY(pos.y - EXPAND_BUTTON_SIZE / 2);
			int w = rtlc_.tW(EXPAND_BUTTON_SIZE);
			int h = rtlc_.tH(EXPAND_BUTTON_SIZE);
			if ((x_ >= xp) && (y_ >= yp) && (x_ <= (xp + w))
					&& (y_ <= (yp + h))) {
				return port;
			}
		}
		return null;
	}

	public VisualModule getModule(RTLModule module) {
		if (vg == null)
			return null;
		return vg.getModule(module);
	}

}
