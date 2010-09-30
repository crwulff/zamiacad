/*

 * Copyright 2006,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 */

package org.zamia.vg;


import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.Position;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class Layout {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Channel channels[];

	private VisualGraph vg;

	private Position totalSize = null;

	// printing
	public final static int PAGE_HEIGHT = 841;

	public final static int PAGE_WIDTH = 595;

	// private static final double OPTIMIZER_TIMEOUT = 3.0; // 3 seconds

	private static final int EXPAND_BUTTON_SIZE = 30;

	private static final int EXPAND_BUTTON_BORDER = 5;

	private HashSetArray<VGPort> expandablePorts;

	private HashMap<VGBox, Integer> moduleDepth;

	private HashMap<Integer, ArrayList<VGBox>> depthModule;

	private int maxDepth;

	private HashMapArray<VGPort, VGSignal> metaSignals;

	private VisualGraph fVG;

	public Layout(VisualGraph aVG) {
		fVG = aVG;

		placeAndRoute();
	}

	//	private void computeMetaSignals() {
	//
	//		metaSignals = new HashMapArray<VGPort, VGSignal>();
	//
	//		HashMapArray<VGPort, VGPort> metaPorts = new HashMapArray<VGPort, VGPort>();
	//
	//		// compute meta ports (first input, first output of each module)
	//
	//		int n = vg.getNumSubs();
	//		for (int i = 0; i < n; i++) {
	//
	//			VGBox sub = vg.getSub(i);
	//
	//			VGPort input = null;
	//			VGPort output = null;
	//
	//			int nPorts = sub.getNumPorts();
	//			for (int j = 0; j < nPorts; j++) {
	//
	//				VGPort p = sub.getPort(j);
	//
	//				if (p.getDirection() == PortDir.IN) {
	//
	//					if (input == null) {
	//						input = p;
	//					}
	//
	//					metaPorts.put(p, input);
	//				} else {
	//
	//					if (output == null) {
	//						output = p;
	//					}
	//
	//					metaPorts.put(p, output);
	//				}
	//			}
	//		}
	//
	//		// compute meta signals
	//
	//		n = vg.getNumSubs();
	//		for (int i = 0; i < n; i++) {
	//
	//			VGBox sub = vg.getSub(i);
	//
	//			int nPorts = sub.getNumPorts();
	//			for (int j = 0; j < nPorts; j++) {
	//
	//				VGPort p = sub.getPort(j);
	//
	//				if (p.getDirection() == PortDir.IN)
	//					continue;
	//
	//				VGPort src = metaPorts.get(p);
	//
	//				VGSignal s = p.getSignal();
	//
	//				if (s == null)
	//					continue;
	//
	//				VGSignal metaSignal = metaSignals.get(src);
	//
	//				if (metaSignal == null) {
	//					metaSignal = new VGSignal(vg, s.getRTLSignal());
	//					metaSignal.addPortConn(src);
	//					metaSignals.put(src, metaSignal);
	//				}
	//
	//				int m = s.getNumConns();
	//				for (int k = 0; k < m; k++) {
	//
	//					p = s.getConn(k);
	//
	//					if (p.getDirection() != PortDir.IN)
	//						continue;
	//
	//					VGPort dest = metaPorts.get(p);
	//
	//					metaSignal.addPortConn(dest);
	//				}
	//			}
	//		}
	//
	//	}
	//

	/**
	 * place all modules on channels according to their logic depth
	 * 
	 */
	//	private void prePlace() {
	//
	//		moduleDepth = new HashMap<VGBox, Integer>();
	//		depthModule = new HashMap<Integer, ArrayList<VGBox>>();
	//
	//		VGBox box = vg.getRoot();
	//
	//		maxDepth = box.levelize(moduleDepth, depthModule);
	//
	//		//		if (metaSignals == null) {
	//		//
	//		//			computeMetaSignals();
	//		//
	//		//		}
	//
	//		channels = new Channel[maxDepth + 2];
	//
	//		for (int i = 0; i < maxDepth + 2; i++) {
	//			channels[i] = new Channel(this, i);
	//		}
	//
	//		int n = box.getNumSubs();
	//		HashSet<VGBox> modules = new HashSet<VGBox>(n);
	//		ArrayList<VGBox> stack = new ArrayList<VGBox>();
	//
	//		for (int i = 0; i < n; i++) {
	//			VGBox module = vg.getSub(i);
	//			if (!isModuleVisible(module))
	//				continue;
	//
	//			modules.add(module);
	//
	//			if (module.getRTLModule() instanceof RTLInputPort) {
	//				stack.add(module);
	//			}
	//		}
	//
	//		int finalCol = channels.length - 1;
	//
	//		while (!modules.isEmpty()) {
	//			while (!stack.isEmpty()) {
	//
	//				VGBox module = stack.remove(stack.size() - 1);
	//				if (!modules.contains(module))
	//					continue;
	//				modules.remove(module);
	//
	//				int col = moduleDepth.get(module);
	//				if (module.getRTLModule() instanceof RTLOutputPort) {
	//					col = finalCol;
	//				}
	//
	//				module.setCol(col);
	//				channels[col].add(module);
	//
	//				// System.logger.debug("Module " + module + " is in column " +
	//				// col);
	//
	//				ArrayList<VGBox> l = module.getSuccessors();
	//				for (int i = 0; i < l.size(); i++) {
	//					VGBox m2 = l.get(i);
	//					if (modules.contains(m2))
	//						stack.add(m2);
	//				}
	//
	//				l = module.getPredecessors();
	//				for (int i = 0; i < l.size(); i++) {
	//					VGBox m2 = l.get(i);
	//					if (!(m2.getRTLModule() instanceof RTLLiteral))
	//						continue;
	//					if (modules.contains(m2)) {
	//						modules.remove(m2);
	//
	//						channels[col - 1].add(m2);
	//					}
	//				}
	//
	//			}
	//			if (!modules.isEmpty())
	//				stack.add(modules.iterator().next());
	//		}
	//
	//		for (int i = 0; i < n; i++) {
	//			VGBox module = vg.getSub(i);
	//
	//			if (!isModuleVisible(module))
	//				continue;
	//
	//			// if (module instanceof RTLOutputPort) {
	//			// GBox box = new GBox(module, finalCol, view);
	//			// gBoxMap.put(module, box);
	//			// channels[finalCol].add(box);
	//			//
	//			// }
	//
	//			// if (!gBoxMap.containsKey(module)) {
	//			// System.logger.debug("WARNING: " + module
	//			// + " has not been placed.");
	//			//
	//			// int col = 0;
	//			//
	//			// ArrayList<VGBox> l = module.getSuccessors();
	//			// GBox box;
	//			// for (int j = 0; j < l.size(); j++) {
	//			// VGBox m2 = l.get(j);
	//			//
	//			// box = gBoxMap.get(m2);
	//			//
	//			// if (box != null) {
	//			// col = box.getCol() - 1;
	//			// break;
	//			// }
	//			// }
	//			// if (col <= 0) {
	//			//
	//			// l = module.getPredecessors();
	//			// for (int j = 0; j < l.size(); j++) {
	//			// VGBox m2 = l.get(j);
	//			// box = gBoxMap.get(m2);
	//			//
	//			// if (box != null) {
	//			// col = box.getCol() + 1;
	//			// break;
	//			// }
	//			// }
	//			// }
	//			// if (col <= 0)
	//			// col = 0;
	//			//
	//			// box = new GBox(module, col, view);
	//			// gBoxMap.put(module, box);
	//			// channels[col].add(box);
	//			// }
	//		}
	//	}

	//
	//	// private int calcPrePlaceCost(VGBox g_, int grow_) {
	//	//
	//	// int cost = 0;
	//	// int gcol = board.getCol(g_);
	//	// int n = g_.getNumPorts();
	//	// for (int i = 0; i < n; i++) {
	//	// VGPort p = g_.getPort(i);
	//	//
	//	// if (p.getDirection() == PortDir.IN) {
	//	//
	//	// VGSignal s = p.getSignal();
	//	// if (s == null)
	//	// continue;
	//	//
	//	// if (s.getNumDrivers() < 1)
	//	// continue;
	//	//
	//	// VGPort dp = s.getDriver(0);
	//	// VGBox driver = dp.getModule();
	//	//
	//	// int dcol = board.getCol(driver);
	//	// if (dcol != (gcol - 1))
	//	// continue;
	//	// int drow = board.getRow(driver);
	//	//
	//	// cost += Math.abs(drow - grow_);
	//	// }
	//	// }
	//	//
	//	// return cost;
	//	// }
	//	//
	//	// private void optimizePrePlacement() {
	//	// // go through vertical channels and
	//	// // (greedily) optimize module placement according to
	//	// // our cost function (number of non-neighbouring port connections)
	//	//
	//	// int n = board.getWidth();
	//	// int m = board.getHeight();
	//	// for (int i = 1; i < n; i++) {
	//	//
	//	// while (true) {
	//	// // find pair of modules with highest gain when exchanged
	//	// int highGain = Integer.MIN_VALUE;
	//	// int highRow1 = 0;
	//	// int highRow2 = 0;
	//	//
	//	// Channel c = board.getVC(i);
	//	// for (int j = 0; j < m; j++) {
	//	// GBox box = c.getGBox(j);
	//	// VGBox module1 = null;
	//	// if (box != null) {
	//	// module1 = box.getModule();
	//	// }
	//	//
	//	// for (int k = 0; k < m; k++) {
	//	// box = c.getGBox(k);
	//	// if (box == null)
	//	// continue;
	//	// VGBox module2 = box.getModule();
	//	// if (module2 == null)
	//	// continue;
	//	// if (module1 == module2)
	//	// continue;
	//	//
	//	// // System.logger.debug("testing " + module1 + " against" +
	//	// // module2);
	//	//
	//	// int cost11 = 0;
	//	// if (module1 != null)
	//	// cost11 = calcPrePlaceCost(module1, j);
	//	// int cost22 = calcPrePlaceCost(module2, k);
	//	//
	//	// int cost12 = 0;
	//	// if (module1 != null)
	//	// cost12 = calcPrePlaceCost(module1, k);
	//	//
	//	// int cost21 = calcPrePlaceCost(module2, j);
	//	//
	//	// int gain = (cost11 + cost22) - (cost12 + cost21);
	//	//
	//	// // System.logger.debug("Gain: " + gain);
	//	//
	//	// if (gain > highGain) {
	//	// highGain = gain;
	//	// highRow1 = j;
	//	// highRow2 = k;
	//	// }
	//	// }
	//	// }
	//	//
	//	// if (highGain > 0) {
	//	// board.exchange(i, highRow1, highRow2);
	//	// } else
	//	// break;
	//	// }
	//	// }
	//	// }
	//	//
	//	// private void optimizePinPositions() {
	//	// if (fixPinPositions)
	//	// return;
	//	//
	//	// // optimize pin position of genericsymbols
	//	//
	//	// int n = board.getWidth();
	//	// for (int i = 0; i < n; i++) {
	//	//
	//	// Channel c = board.getVC(i);
	//	//
	//	// int m = c.getNumSlots();
	//	// for (int j = 0; j < m; j++) {
	//	// GBox box = c.getGBox(j);
	//	// if (box == null)
	//	// continue;
	//	// VGBox module = box.getModule();
	//	// if (module == null)
	//	// continue;
	//	// Symbol sym = box.getSymbol();
	//	// if (!(sym instanceof GenericSymbol))
	//	// continue;
	//	// GenericSymbol gs = (GenericSymbol) sym;
	//	//
	//	// int l = module.getNumPorts();
	//	// for (int k = 0; k < l; k++) {
	//	// VGPort p = module.getPort(k);
	//	// VGSignal s = p.getSignal();
	//	// if (s == null)
	//	// continue;
	//	// if (p.getDirection() != PortDir.IN) {
	//	//
	//	// int nReaders = s.getNumReaders();
	//	// if (nReaders > 0) {
	//	// int pinoff = 0;
	//	// for (int iReader = 0; iReader < nReaders; iReader++) {
	//	//
	//	// VGPort rp = s.getReader(iReader);
	//	//
	//	// VGBox reader = rp.getModule();
	//	//
	//	// GBox dbox = board.getGBox(reader);
	//	//
	//	// Symbol dsym = dbox.getSymbol();
	//	// int poff = dsym.getPortPosition(rp).y;
	//	//
	//	// // bartsch changed 12.03.2006
	//	// // pinoff += dbox.getRow() * 1000 + poff;
	//	// pinoff += dbox.getYPos() + poff;
	//	// }
	//	// gs.setPinHint(p, pinoff / nReaders);
	//	// }
	//	// } else {
	//	//
	//	// if (s.getNumDrivers() < 1)
	//	// continue;
	//	//
	//	// VGPort dp = s.getDriver(0);
	//	//
	//	// VGBox driver = dp.getModule();
	//	//
	//	// GBox dbox = board.getGBox(driver);
	//	//
	//	// Symbol dsym = dbox.getSymbol();
	//	// int pinoff = dsym.getPortPosition(dp).y;
	//	//
	//	// // bartsch changed 12.03.2006
	//	// // gs.setPinHint(p, dbox.getRow() * 1000 + pinoff);
	//	// gs.setPinHint(p, dbox.getYPos() + pinoff);
	//	// }
	//	// }
	//	// gs.sortPins();
	//	// }
	//	// }
	//	// }
	//	//
	//
	Position getPortPosition(VGPort aPort) {
		return null;
		// FIXME return aPort.getBox().getPortPosition(aPort);
	}

	//
	//	public int getCol(VGBox module_) {
	//		return module_.getCol();
	//	}
	//
	//	public int getCol(VGPort p) {
	//		return getCol(p.getModule());
	//	}
	//
	//	private int calcVGBoxPlaceScore(VGBox box_) {
	//
	//		Channel c = box_.getVChannel();
	//
	//		int ymin1 = box_.getYPos();
	//		int ymax1 = ymin1 + box_.getHeight();
	//
	//		// collision check:
	//		int n = c.getNumSlots();
	//		for (int i = 0; i < n; i++) {
	//			VGBox b2 = c.getModule(i);
	//
	//			if (b2 == box_)
	//				continue;
	//
	//			int ymin2 = b2.getYPos();
	//			int ymax2 = ymin2 + b2.getHeight();
	//
	//			if (ymin2 <= ymin1 && ymax2 >= ymin1)
	//				return Integer.MIN_VALUE;
	//			if (ymin2 >= ymin1 && ymax2 <= ymax1)
	//				return Integer.MIN_VALUE;
	//			if (ymin2 <= ymax1 && ymax2 >= ymax1)
	//				return Integer.MIN_VALUE;
	//
	//		}
	//
	//		int score = 0;
	//
	//		int l = box_.getNumPorts();
	//		for (int k = 0; k < l; k++) {
	//			VGPort p = box_.getPort(k);
	//			VGSignal s = p.getSignal();
	//			if (s == null)
	//				continue;
	//
	//			Position pos1 = getPortPosition(p);
	//			int x1 = pos1.x;
	//			int y1 = pos1.y;
	//			if (p.getDirection() != PortDir.IN) {
	//
	//				if (box_.getNumInputs() == 0) {
	//					int nConns = s.getNumConns();
	//					if (nConns > 0) {
	//						for (int iConn = 0; iConn < nConns; iConn++) {
	//
	//							VGPort rp = s.getConn(iConn);
	//
	//							if (rp.getDirection() == PortDir.OUT) {
	//								continue;
	//							}
	//
	//							VGBox rm = rp.getModule();
	//							if (!isModuleVisible(rm))
	//								continue;
	//
	//							Position pos2 = getPortPosition(rp);
	//							int x2 = pos2.x;
	//							int y2 = pos2.y;
	//							int ydist = Math.abs(y1 - y2);
	//							int xdist = Math.abs(x1 - x2) + 1;
	//
	//							boolean blocked = false;
	//							int sc = getCol(p) + 1;
	//							int dc = getCol(rp) - 1;
	//							for (int x = sc; x <= dc; x++) {
	//
	//								if (isModulePlacedAt(x, y1)) {
	//									blocked = true;
	//								}
	//							}
	//
	//							if (!blocked) {
	//								if (y1 == y2) {
	//
	//									score += 100000;
	//								} else {
	//									score -= ydist * 100 / xdist;
	//								}
	//							} else {
	//								score -= 50000;
	//							}
	//
	//						}
	//					}
	//				}
	//
	//			} else {
	//
	//				VGPort dp = null;
	//
	//				int nConns = s.getNumConns();
	//				for (int iConn = 0; iConn < nConns; iConn++) {
	//
	//					VGPort vp = s.getConn(iConn);
	//					if (p.getDirection() == PortDir.OUT) {
	//						dp = vp;
	//						break;
	//					}
	//				}
	//				if (dp == null)
	//					continue;
	//
	//				VGBox dm = dp.getModule();
	//				if (!isModuleVisible(dm))
	//					continue;
	//
	//				Position pos2 = getPortPosition(dp);
	//				int x2 = pos2.x;
	//				int y2 = pos2.y;
	//				int ydist = Math.abs(y1 - y2);
	//				int xdist = Math.abs(x1 - x2) + 1;
	//
	//				boolean blocked = false;
	//				int sc = getCol(dp) + 1;
	//				int dc = getCol(p) - 1;
	//				for (int x = sc; x <= dc; x++) {
	//
	//					if (isModulePlacedAt(x, y1)) {
	//						blocked = true;
	//					}
	//				}
	//
	//				if (!blocked) {
	//					if (y1 == y2) {
	//
	//						score += 100000;
	//					} else {
	//						score -= ydist * 100 / xdist;
	//					}
	//				} else {
	//					score -= 50000;
	//				}
	//			}
	//		}
	//		return score;
	//	}
	//
	//	public Channel getVC(int col_) {
	//		return channels[col_];
	//	}
	//
	//	private boolean isModulePlacedAt(int col_, int y_) {
	//		Channel c = getVC(col_);
	//		return c.isModulePlacedAt(y_);
	//
	//	}
	//
	//	private void optimizeYPlacement(VGBox module_) {
	//		int bestScore = calcVGBoxPlaceScore(module_);
	//		int bestYPos = module_.getYPos();
	//
	//		int l = module_.getNumPorts();
	//
	//		for (int k = 0; k < l; k++) {
	//			VGPort p = module_.getPort(k);
	//			VGSignal s = p.getSignal();
	//			if (s == null)
	//				continue;
	//
	//			// if (module_.getNumInputs() > 0 && p.getDirection() != PortDir.IN)
	//			// continue;
	//
	//			Position offset = module_.getPortOffset(p.getRTLPort());
	//			// Position pos1 = getPortPosition(p);
	//
	//			int nConns = s.getNumConns();
	//			for (int iConn = 0; iConn < nConns; iConn++) {
	//
	//				VGPort p2 = s.getConn(iConn);
	//
	//				if (p2 == p)
	//					continue;
	//
	//				VGBox p2m = p2.getModule();
	//				if (!isModuleVisible(p2m))
	//					continue;
	//
	//				int ypos = getPortPosition(p2).y - offset.y;
	//
	//				if (ypos < 0)
	//					continue;
	//
	//				module_.setYPos(ypos);
	//
	//				int score = calcVGBoxPlaceScore(module_);
	//				// System.logger.debug(" Score for " +
	//				// module.getInstanceName() + " at ypos " + ypos +
	//				// ": " + score);
	//				if (score > bestScore) {
	//					bestScore = score;
	//					bestYPos = ypos;
	//				}
	//			}
	//		}
	//
	//		// System.logger.debug("*******" + module + "Best Y pos
	//		// score: " + bestScore + " YPos: " + bestYPos);
	//
	//		module_.setYPos(bestYPos);
	//
	//	}
	//
	//	private void optimizeYPlacement() {
	//
	//		// find best non-colliding y position for each box
	//
	//		int n = channels.length;
	//		// always run this optimization several times
	//		// since modules moved later may make room for
	//		// modules which were moved before them
	//
	//		// long startTime = System.currentTimeMillis();
	//		//
	//		// for (int r = 0; r < 30; r++) {
	//		// System.logger.debug("===================== optimize rtl module
	//		// placement iter=" + r + " ===========");
	//
	//		// left to right
	//
	//		for (int i = 1; i < n; i++) {
	//
	//			// System.logger.debug(" ++++++++++++++ Channel #" + i);
	//
	//			Channel c = channels[i];
	//
	//			int m = c.getNumSlots();
	//			for (int j = 0; j < m; j++) {
	//				VGBox module = c.getModule(j);
	//				if (module == null)
	//					continue;
	//
	//				optimizeYPlacement(module);
	//			}
	//		}
	//
	//		// right to left
	//
	//		for (int i = n - 2; i >= 0; i--) {
	//
	//			// System.logger.debug(" ++++++++++++++ Channel #" + i);
	//
	//			Channel c = channels[i];
	//
	//			int m = c.getNumSlots();
	//			for (int j = 0; j < m; j++) {
	//				VGBox module = c.getModule(j);
	//				if (module == null)
	//					continue;
	//
	//				optimizeYPlacement(module);
	//			}
	//		}
	//
	//		// double time = (System.currentTimeMillis() - startTime) / 1000.0;
	//		// if (time > OPTIMIZER_TIMEOUT) {
	//		// break;
	//		// }
	//		// }
	//	}
	//
	//	//
	//	// private void optimizePortPlacement() {
	//	// int n = rtlg.getNumSubs();
	//	// for (int i = 0; i < n; i++) {
	//	// VGBox module = rtlg.getSub(i);
	//	// if (!(module instanceof VGPortModule))
	//	// continue;
	//	//
	//	// if (!isModuleVisible(module))
	//	// continue;
	//	//
	//	// VGPortModule port = (VGPortModule) module;
	//	//
	//	// VGPort internalPort = port.getInternalPort();
	//	// VGSignal s = internalPort.getSignal();
	//	// if (s == null)
	//	// continue;
	//	//
	//	// VGSignal signalToRoute = s;
	//	//
	//	// // System.logger.debug("optimizing port placement for : "
	//	// // + internalPort);
	//	//
	//	// // find connected pins
	//	// int numConns = s.getNumConns();
	//	// for (int j = 0; j < numConns; j++) {
	//	// VGPort p = s.getConn(j);
	//	// if (p != internalPort) {
	//	//
	//	// // way from pin to primary port free?
	//	//
	//	// VGPort source = internalPort;
	//	// VGPort dest = p;
	//	//
	//	// int sr = board.getRow(source);
	//	// int sc = board.getCol(source);
	//	// int dr = board.getRow(dest);
	//	// int dc = board.getCol(dest);
	//	// if (source.getDirection() == PortDir.OUT)
	//	// sc++;
	//	// if (dest.getDirection() == PortDir.OUT)
	//	// dc++;
	//	//
	//	// // source should be left, dest should be right
	//	// if (sc > dc) {
	//	// int tmp;
	//	// tmp = sr;
	//	// sr = dr;
	//	// dr = tmp;
	//	// tmp = sc;
	//	// sc = dc;
	//	// dc = tmp;
	//	// VGPort tmpPrt;
	//	// tmpPrt = source;
	//	// source = dest;
	//	// dest = tmpPrt;
	//	// }
	//	//
	//	// boolean blocking = false;
	//	// int row = sr;
	//	// if (source == internalPort)
	//	// row = dr;
	//	// for (int x = sc; x < dc; x++) {
	//	// if (board.getGBoxAt(x, row) != null) {
	//	// blocking = true;
	//	// }
	//	// }
	//	// // System.logger.debug("block check for " + source + " <-> "
	//	// // + dest + " => blocking: " + blocking);
	//	//
	//	// if (!blocking) {
	//	// int y = board.getPortPosition(p).y;
	//	//
	//	// board.setPortYPlacementHint(internalPort, y);
	//	// continue;
	//	// }
	//	//
	//	// Channel c = board.getHChannel(signalToRoute);
	//	//
	//	// board.setPortYPlacementHint(internalPort, c.getSignalIdx(signalToRoute) +
	//	// c.getPos());
	//	// }
	//	// }
	//	// }
	//	//
	//	// if (board.getWidth() > 1) {
	//	// board.resolveCollisions(0);
	//	// board.resolveCollisions(board.getWidth() - 1);
	//	// }
	//	// }
	//	//
	//	// private void preRouteSignal(VGSignal s, VGPort source, VGPort
	//	// dest) {
	//	//
	//	// // System.logger.debug("Routing signal " + s + " from " + source + " to "
	//	// // + dest);
	//	//
	//	// if (!isModuleVisible(source.getModule()) ||
	//	// !isModuleVisible(dest.getModule()))
	//	// return;
	//	//
	//	// /* source/dest col/rows */
	//	//
	//	// int sr = board.getRow(source);
	//	// int sc = board.getCol(source);
	//	// int dr = board.getRow(dest);
	//	// int dc = board.getCol(dest);
	//	// if (source.getDirection() == PortDir.OUT)
	//	// sc++;
	//	// if (dest.getDirection() == PortDir.OUT)
	//	// dc++;
	//	//
	//	// // we want to route from left to right
	//	// if (sc > dc) {
	//	// int tmp;
	//	// tmp = sr;
	//	// sr = dr;
	//	// dr = tmp;
	//	// tmp = sc;
	//	// sc = dc;
	//	// dc = tmp;
	//	// VGPort tmpPrt;
	//	// tmpPrt = source;
	//	// source = dest;
	//	// dest = tmpPrt;
	//	// }
	//	//
	//	// VGSignal signalToRoute = s;
	//	//
	//	// System.logger.debug("Routing signal " + signalToRoute + " from " + source
	//	// + " to " + dest + " source coord: " + sc + "/" + sr + ", dest coord: " +
	//	// dc + "/" + dr);
	//	//
	//	// // step 1: move from pin to next vert. channel
	//	// board.addSignalV(signalToRoute, sc);
	//	//
	//	// if (sc != dc) {
	//	//
	//	// int hr;
	//	//
	//	// // step 2: move up/down to dest hor. channel
	//	// if (dr > sr)
	//	// hr = dr;
	//	// else
	//	// hr = sr;
	//	// board.addSignalH(signalToRoute, hr);
	//	//
	//	// // step 3: move left/right to dest vert. channel
	//	// board.addSignalV(signalToRoute, dc);
	//	// }
	//	// // else
	//	// // board.addSignalH(signalToRoute, sr);
	//	// }
	//	//
	//	// private void preRoute() {
	//	//
	//	// HashSet<VGSignal> routedSignals = new HashSet<VGSignal>();
	//	//
	//	// int nCols = board.getWidth();
	//	// for (int i = 0; i < nCols; i++) {
	//	//
	//	// int nRows = board.getHeight();
	//	//
	//	// for (int j = 0; j < nRows; j++) {
	//	//
	//	// GBox box = board.getVC(i).getGBox(j);
	//	// if (box == null)
	//	// continue;
	//	// VGBox g = box.getModule();
	//	// if (g == null)
	//	// continue;
	//	//
	//	// int nPorts = g.getNumPorts();
	//	// for (int k = 0; k < nPorts; k++) {
	//	// VGPort sp = g.getPort(k);
	//	// VGSignal s = sp.getSignal();
	//	//
	//	// if (s == null)
	//	// continue;
	//	//
	//	// if (!routedSignals.contains(s)) {
	//	// routedSignals.add(s);
	//	//
	//	// int nConns = s.getNumConns();
	//	// for (int l = 0; l < nConns; l++) {
	//	// VGPort dp = s.getConn(l);
	//	// if (sp == dp)
	//	// continue;
	//	// // System.logger.debug(" routing from " + sp + " to "
	//	// // + dp);
	//	// preRouteSignal(s, sp, dp);
	//	// }
	//	// }
	//	// }
	//	// }
	//	// }
	//	// }
	//	//
	//	//
	//	private void routeSignal(VGSignal s, VGPort source, VGPort dest) {
	//
	//		/* source/dest cols */
	//
	//		int sc = getCol(source);
	//		int dc = getCol(dest);
	//		if (source.getDirection() == PortDir.OUT)
	//			sc++;
	//		if (dest.getDirection() == PortDir.OUT)
	//			dc++;
	//
	//		// we want to route from left to right
	//		if (sc > dc) {
	//			int tmp;
	//			tmp = sc;
	//			sc = dc;
	//			dc = tmp;
	//			VGPort tmpPrt;
	//			tmpPrt = source;
	//			source = dest;
	//			dest = tmpPrt;
	//		}
	//
	//		Channel c = channels[sc];
	//		if (c.addPinConnection(source, s) && showPins) {
	//			VGBox module = source.getModule();
	//			Symbol sym = module.getSymbol();
	//			if (sym instanceof GenericSymbol) {
	//				GenericSymbol gs = (GenericSymbol) sym;
	//				gs.tweakPortPosition(source.getRTLPort());
	//			}
	//		}
	//
	//		int y = getPortPosition(source).y;
	//
	//		while (sc < dc) {
	//			sc++;
	//			Channel c2 = channels[sc];
	//			y = c.addChannelConnection(s, c2, y);
	//			c = c2;
	//		}
	//
	//		if (c.addPinConnection(dest, s) && showPins) {
	//			VGBox module = dest.getModule();
	//			Symbol sym = module.getSymbol();
	//			if (sym instanceof GenericSymbol) {
	//				GenericSymbol gs = (GenericSymbol) sym;
	//				gs.tweakPortPosition(dest.getRTLPort());
	//			}
	//		}
	//
	//		// VGSignal signalToRoute = s;
	//		//
	//		// // System.logger.debug("Routing signal from " + source + " " + sc +
	//		// "/"
	//		// // + sr + " to " + dest + " " + dc + "/" + dr);
	//		//
	//		// // small optimization: no modules blocking the horizontal way?
	//		// boolean blockingSR = false;
	//		// for (int x = sc; x < dc; x++) {
	//		//
	//		// int y = board.getPortPosition(source).y;
	//		//
	//		// if (board.isModulePlacedAt(x, y)) {
	//		// blockingSR = true;
	//		// }
	//		// }
	//		//
	//		// boolean blockingDR = false;
	//		// for (int x = sc; x < dc; x++) {
	//		// int y = board.getPortPosition(dest).y;
	//		// if (board.isModulePlacedAt(x, y)) {
	//		// blockingDR = true;
	//		// }
	//		// }
	//		//
	//		// Channel vc = null;
	//		// int cx = 0;
	//		//
	//		// if (blockingSR) {
	//		//
	//		// /*
	//		// * move from pin to next vert. channel
	//		// */
	//		//
	//		// vc = board.getVC(sc);
	//		// int offset = vc.getSignalIdx(signalToRoute);
	//		//
	//		// cx = vc.getPos() + offset;
	//		// int cy = board.getPortPosition(source).y;
	//		//
	//		// board.addPinConnection(source, cx);
	//		//
	//		// vc.addConnector(signalToRoute, cy, true, source);
	//		//
	//		// if (blockingDR) {
	//		//
	//		// // move up/down to signal hor. channel
	//		// Channel hc = board.getHChannel(signalToRoute);
	//		// offset = hc.getSignalIdx(signalToRoute);
	//		// cy = hc.getPos() + offset;
	//		// hc.addConnector(signalToRoute, cx, true, null);
	//		// vc.addConnector(signalToRoute, cy, false, null);
	//		//
	//		// // move left/right to dest vert. channel
	//		// vc = board.getVC(dc);
	//		// offset = vc.getSignalIdx(signalToRoute);
	//		// cx = vc.getPos() + offset;
	//		// hc.addConnector(signalToRoute, cx, true, null);
	//		// vc.addConnector(signalToRoute, cy, true, null);
	//		// }
	//		// } else {
	//		// /*
	//		// * move from pin to dest vert. channel
	//		// */
	//		//
	//		// vc = board.getVC(dc);
	//		// int offset = vc.getSignalIdx(signalToRoute);
	//		// cx = vc.getPos() + offset;
	//		// int cy = board.getPortPosition(source).y;
	//		//
	//		// board.addPinConnection(source, cx);
	//		// vc.addConnector(signalToRoute, cy, true, null);
	//		// }
	//		//
	//		// // move up/down to pin
	//		// Position ppos = board.getPortPosition(dest);
	//		// int pposy = ppos.y;
	//		// vc.addConnector(signalToRoute, pposy, false, dest);
	//		//
	//		// // move left/right to pin
	//		// board.addPinConnection(dest, cx);
	//	}
	//
	//	private void routeSignal(VGSignal signal) {
	//		// connect the leftmost port in the list with all
	//		// the others
	//
	//		int pos = Integer.MAX_VALUE;
	//		VGPort sp = null;
	//		int m = signal.getNumConns();
	//		for (int j = 0; j < m; j++) {
	//			VGPort port = signal.getConn(j);
	//			VGBox module = port.getModule();
	//			if (!isModuleVisible(module)) {
	//				continue;
	//			}
	//
	//			int d = module.getCol();
	//			if (d < pos) {
	//				pos = d;
	//				sp = port;
	//			}
	//		}
	//		if (sp == null)
	//			return;
	//
	//		if (!isModuleVisible(sp.getModule()))
	//			return;
	//
	//		int num = signal.getNumConns();
	//		for (int j = 0; j < num; j++) {
	//			VGPort dp = signal.getConn(j);
	//			if (sp == dp)
	//				continue;
	//			// System.logger.debug(" routing from " + sp + " to " + dp);
	//
	//			if (!isModuleVisible(dp.getModule())) {
	//				expandablePorts.add(sp);
	//			} else {
	//				routeSignal(signal, sp, dp);
	//			}
	//		}
	//	}
	//
	//	private void route() {
	//
	//		// now route the signals
	//
	//		if (showPins) {
	//
	//			int n = vg.getNumSignals();
	//			for (int i = 0; i < n; i++) {
	//				VGSignal signal = vg.getSignal(i);
	//				routeSignal(signal);
	//			}
	//
	//		} else {
	//
	//			int n = metaSignals.size();
	//			for (int i = 0; i < n; i++) {
	//				VGSignal signal = metaSignals.get(i);
	//				routeSignal(signal);
	//			}
	//
	//		}
	//	}
	//
	//	//
	//	// private void optimizeVChannelSignalOrdering() {
	//	// int n = board.getWidth();
	//	// for (int i = 1; i < n; i++) {
	//	// Channel c = board.getVC(i);
	//	// c.optimizeSignalOrdering();
	//	// }
	//	// }
	//	//
	//	// private void clearChannelAllocators() {
	//	// board.clearPinConnections();
	//	// int n = board.getWidth();
	//	// for (int i = 0; i < n; i++) {
	//	// Channel c = board.getVC(i);
	//	// c.clearAllocators();
	//	// }
	//	// n = board.getHeight();
	//	// for (int i = 0; i < n; i++) {
	//	// Channel c = board.getHC(i);
	//	// c.clearAllocators();
	//	// }
	//	// }
	//
	//	private void finalizePlacement() {
	//
	//		// compute ychannel positions and sizes
	//
	//		int w = 0;
	//		int h = 0;
	//
	//		for (int i = 0; i < channels.length; i++) {
	//			Channel c = channels[i];
	//			c.setXPos(w);
	//			w += c.getWidth();
	//			int hh = c.getHeight();
	//			if (hh > h)
	//				h = hh;
	//		}
	//		totalSize = new Position(w + 100, h + 100);
	//
	//		if (showPins) {
	//			// optimize output port ypos
	//			Channel c = channels[channels.length - 1];
	//			Channel c2 = channels[channels.length - 2];
	//			int n = c.getNumSlots();
	//			for (int i = 0; i < n; i++) {
	//
	//				VGBox module = c.getModule(i);
	//				if (module == null)
	//					continue;
	//
	//				if (!(module.getRTLModule() instanceof RTLOutputPort))
	//					continue;
	//
	//				RTLOutputPort pm = (RTLOutputPort) module.getRTLModule();
	//				RTLPort p = pm.getInternalPort();
	//				RTLSignal s = p.getSignal();
	//				if (s == null)
	//					continue;
	//
	//				VGSignal vs = vg.getSignal(s);
	//				int y = c2.getSignalYPos(vs);
	//				if (y == 0)
	//					continue;
	//
	//				if (c.getCollidingModule(module, y) != null)
	//					continue;
	//
	//				Position offset = module.getPortOffset(p);
	//
	//				module.setYPos(y - offset.y);
	//			}
	//		}
	//	}

	private void placeAndRoute() {

		// prePlace needs to re-compute these

		moduleDepth = null;
		depthModule = null;
		metaSignals = null;

		expandablePorts = new HashSetArray<VGPort>();

		totalSize = new Position(50, 50);

		logger.info("Place and route (PAR) starts on %s", fVG);
		logger.info("======================================================");

		long startTime = System.currentTimeMillis();

		/*
		 * calculate the logic depth of each module - function levelize() place
		 * modules in vchannels according to their logic depth
		 */

		logger.debug("PAR 1/4: prePlace()");
		//prePlace();

		/*
		 * fine-tune y placement, try to place connected ports on the same Y pos
		 */

		double time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		logger.debug("PAR 2/4: optimizeYPlacement()");
		// FIXME optimizeYPlacement();

		/*
		 * route the signals
		 */

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		logger.debug("PAR 3/4: route()");
		// FIXME route();

		/*
		 * finalize placement, VChannels get their final X positions, calculate
		 * totalSize
		 */

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("         PAR time elapsed so far: " + time + "s.");
		logger.debug("PAR 4/4: finalizePlacement()");
		// FIXME finalizePlacement();

		time = (System.currentTimeMillis() - startTime) / 1000.0;
		logger.debug("\nPAR done. total time elapsed: " + time + "s.\n");
	}

	public int getNumChannels() {
		if (channels == null)
			return 0;
		return channels.length;
	}

	public Channel getChannel(int i) {
		return channels[i];
	}

}
