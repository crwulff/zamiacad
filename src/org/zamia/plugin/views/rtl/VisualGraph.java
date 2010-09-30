/* 
 * Copyright 2008,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 20, 2008
 */
package org.zamia.plugin.views.rtl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPortModule;
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class VisualGraph {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private HashMapArray<RTLModule, VisualModule> nodeMap;

	private HashSetArray<VisualModule> nodes;

	private HashMapArray<String, VisualSignal> signals;

	private RTLView rtlv;

	public VisualGraph(RTLGraph rtlg_, RTLView rtlv_) {

		rtlv = rtlv_;

		createGraph(rtlg_);

	}

	public void createGraph(RTLGraph rtlg_) {

		clean();

		int n = rtlg_.getNumSubs();
		for (int i = 0; i < n; i++) {
			RTLModule module = rtlg_.getSub(i);
			VisualModule vm = new VisualModule(module, rtlv);
			nodeMap.put(module, vm);
			nodes.add(vm);
		}

		n = rtlg_.getNumSignals();
		for (int i = 0; i < n; i++) {

			RTLSignal signal = rtlg_.getSignal(i);
			VisualSignal vs = new VisualSignal(this, signal);
			add(vs);

			int m = signal.getNumConns();
			for (int j = 0; j < m; j++) {

				RTLPort fp = signal.getConn(j);

				VisualModule vm = nodeMap.get(fp.getModule());
				VisualPort vp = vm.getPort(fp.getId());

				vp.setSignal(vs);
			}
		}
	}

	private void add(VisualSignal vs_) {
		String id = vs_.getId();

		if (id != null) {
			signals.put(id, vs_);
		}
	}

	public void clean() {
		nodeMap = new HashMapArray<RTLModule, VisualModule>();
		nodes = new HashSetArray<VisualModule>();
		signals = new HashMapArray<String, VisualSignal>();
	}

	public int getNumSubs() {
		return nodes.size();
	}

	public VisualModule getSub(int i) {
		return nodes.get(i);
	}

	public int levelize(HashMap<VisualModule, Integer> moduleDepth_,
			HashMap<Integer, ArrayList<VisualModule>> depthModule_) {
		// step 1: compute number of connected inputs per module
		// push those modules which do not have connected inputs
		// on the stack (inputmodules / literals)

		HashMap<VisualModule, Integer> numConnectedInputs = new HashMap<VisualModule, Integer>();
		LinkedList<VisualModule> queue = new LinkedList<VisualModule>();
		HashSet<VisualModule> todo = new HashSet<VisualModule>();
		// keep track of how often we have reached each module
		// and what the maximum logic depth was
		// when # reached == # connected inputs we know it's
		// logic depth and push it on the stack
		HashMap<VisualModule, Integer> reached = new HashMap<VisualModule, Integer>();

		int nSubs = getNumSubs();

		for (int i = 0; i < nSubs; i++) {
			VisualModule module = getSub(i);
			RTLModule rtlm = module.getRTLModule();

			// System.out.println ("LEVELIZE Sub #"+i+" : "+rtlm);

			int numPorts = module.getNumPorts();
			int numCI = 0;
			for (int j = 0; j < numPorts; j++) {

				VisualPort input = module.getPort(j);
				if (input.getDirection() != PortDir.IN) {
					continue;
				}

				VisualSignal s = input.getSignal();
				if ((s != null) && (s.getNumDrivers() > 0)) {

					// a little hack to break feedback loops caused by registers
					int numI = module.getNumInputs();
					if (numI > 1) {
						VisualModule driver = s.getDriver(0).getModule();
						if (!(driver.getRTLModule() instanceof RTLRegister)) {
							numCI++;
						}
					} else
						numCI++;
				}

			}
			numConnectedInputs.put(module, numCI);
			reached.put(module, 0);
			todo.add(module);
			if (numCI == 0) {
				queue.add(module);

				if (!(rtlm instanceof RTLPortModule)
						&& !(rtlm instanceof RTLLiteral))
					moduleDepth_.put(module, 1);
				else
					moduleDepth_.put(module, 0);
			}
		}

		// step 2: recursively levelize remaining modules

		int maxDepth = 0;
		boolean placedRegs = false;

		int numTodo = todo.size();
		int count = 0;
		int pold = 0;

		while (true) {

			int pc = 0;
			if (numTodo > 0)
				pc = count * 100 / numTodo;

			if (pc > pold) {
				pold = pc;
				// System.out.print("\rLevelize: " + pc + "% done.");
			}
			count++;

			while (!queue.isEmpty()) {
				VisualModule module = queue.poll();
				RTLModule rtlm = module.getRTLModule();
				if (!todo.contains(module)) {
					continue;
				}
				if ((rtlm instanceof RTLRegister) && !placedRegs) {
					continue;
				}

				int depth = ((Integer) moduleDepth_.get(module)).intValue();
				todo.remove(module);

				int numPorts = module.getNumPorts();
				for (int iPort = 0; iPort < numPorts; iPort++) {

					VisualPort port = module.getPort(iPort);
					if (port.getDirection() == PortDir.IN)
						continue;

					VisualSignal signal = port.getSignal();

					if (signal == null)
						continue;

					int nConns = signal.getNumConns();
					for (int iConn = 0; iConn < nConns; iConn++) {
						VisualPort conn = signal.getConn(iConn);

						if (conn.getDirection() != PortDir.IN)
							continue;

						VisualModule receiver = conn.getModule();

						if ((receiver != module) && (todo.contains(receiver))) {

							// calc depth of this receiver

							Integer i = (Integer) moduleDepth_.get(receiver);
							if (i == null) {
								moduleDepth_.put(receiver, depth + 1);
								if (depth >= maxDepth) {
									maxDepth = depth + 1;
								}
							} else {
								int r_depth = i.intValue();
								if (depth >= r_depth) {
									moduleDepth_.put(receiver, depth + 1);
									if (depth >= maxDepth) {
										maxDepth = depth + 1;
									}
								}
							}

							// how often have we reached this receiver?
							int numReached = reached.get(receiver);
							numReached++;
							reached.put(receiver, numReached);

							// how many connected inputs has this receiver?
							int numCI = numConnectedInputs.get(receiver);

							if (numCI == numReached)
								queue.add(receiver);
						}

					}

				}

			}
			if (!todo.isEmpty()) {
				// there seem to be some feedbacks in this
				// graph, so we need to push modules in
				// (we want to levelize all modules)

				// have we placed the registers yet?
				if (placedRegs) {
					// yes => push in those modules with the highest
					// number of already connected inputs

					int mostOftenReached = 0;

					for (Iterator<VisualModule> i = todo.iterator(); i
							.hasNext();) {
						VisualModule module = i.next();
						int numReached = reached.get(module);
						if (numReached > mostOftenReached)
							mostOftenReached = numReached;
					}

					for (Iterator<VisualModule> i = todo.iterator(); i
							.hasNext();) {
						VisualModule module = i.next();

						int numReached = reached.get(module);
						if (numReached == mostOftenReached) {

							queue.add(module);
							moduleDepth_.put(module, maxDepth);
						}
					}
				} else {
					// ah - perfect time to push in the registers
					// for well-formed circuits that should solve all
					// feedback problems

					for (Iterator<VisualModule> i = todo.iterator(); i
							.hasNext();) {
						VisualModule module = i.next();
						RTLModule rtlm = module.getRTLModule();
						if (rtlm instanceof RTLRegister) {
							queue.add(module);
							moduleDepth_.put(module, maxDepth);
						}
					}
					placedRegs = true;
				}
			} else
				break;
		}

		// generates depthModule from moduleDepth
		// moduleDepth: module --> depth
		// depthModule: depth (integer) --> list of modules (arrayList)
		ArrayList<VisualModule> list = null;
		for (int i = 0; i < nSubs; i++) {
			VisualModule module = getSub(i);
			Object depth = moduleDepth_.get(module);
			if (depth == null)
				continue;

			if (depthModule_.containsKey(depth)) {
				list = (ArrayList<VisualModule>) depthModule_.get(depth);
			} else {
				list = new ArrayList<VisualModule>();
			}
			list.add(module);
			depthModule_.put((Integer) depth, list);
		}

		return maxDepth;
	}

	public int getNumSignals() {
		return signals.size();
	}

	public VisualSignal getSignal(int idx_) {
		return signals.get(idx_);
	}

	public VisualSignal getSignal(RTLSignal s) {
		return signals.get(s.getId());
	}

	public VisualModule getModule(RTLModule module) {
		return nodeMap.get(module);
	}

	public void removeBuiltins() {

		int i = 0;
		while (i<getNumSubs()) {

			VisualModule module = getSub(i);

			RTLModule rtlModule = module.getRTLModule();

			if ((rtlModule instanceof RTLGraph)
					|| (rtlModule instanceof RTLPortModule)) {
				i++;
				continue;
			}

			int m = module.getNumPorts();

			VisualSignal s = null;

			for (int j = 0; j < m; j++) {

				VisualPort port = module.getPort(j);

				if (s == null) {
					s = port.getSignal();
				} else {
					VisualSignal s2 = port.getSignal();
					if (s2 != null) {
						sigJoin(s, s2);
					}
				}
			}

			remove(module);
		}
	}

	private void remove(VisualModule module_) {
		for (int i = 0; i < module_.getNumPorts(); i++) {
			VisualPort port = module_.getPort(i);
			VisualSignal s = port.getSignal();
			if (s != null)
				s.removePortConn(port);
		}

		// remove module itself

		nodes.remove(module_);
		nodeMap.remove(module_.getRTLModule());
	}

	public VisualSignal sigJoin(VisualSignal driving_, VisualSignal receiving) {

		if (driving_ == receiving)
			return driving_;

		VisualSignal res, victim;
		res = receiving;
		victim = driving_;

		while (victim.getNumConns() > 0) {
			VisualPort p = victim.getConn(0);
			p.setSignal(res);
		}

		remove(victim);

		// FIXME:
		// int n = victim.getNumIds();
		// for (int i = 0; i < n; i++) {
		// String id = victim.getId(i);
		// signals.put(id, res);
		// res.addId(id);
		// }

		return res;
	}

	private void remove(VisualSignal s_) {
		signals.remove(s_.getId());
	}

}
