/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 10, 2009
 */
package org.zamia.rtl.sim;


/**
 * Right now this is just a code dump for old
 * simulator command line interface code
 * @author Guenter Bartsch
 *
 */

public class SimulatorCmdLine {
//	private RTLGraph parsePath(String signalPath_) throws SimException {
//		if (root == null)
//			throw new SimException("Not root netlist set.");
//
//		String pes[] = signalPath_.split("/");
//		RTLGraph nl = root;
//
//		int n = pes.length;
//		for (int i = 0; i < (n - 1); i++) {
//			String s = pes[i];
//			if (s == null || s.length() < 1 || s.equals(root.getInstanceName()))
//				continue;
//			RTLModule module = nl.findSub(s);
//			if (module == null)
//				throw new SimException("Cannot resolve path element: " + s + " in " + nl);
//
//			if (!(module instanceof RTLGraph))
//				throw new SimException("Not a netlist: " + module);
//			nl = (RTLGraph) module;
//		}
//		return nl;
//	}
//
//	private void trace(String signalPath_) throws SimException {
//
//		RTLGraph rtlg = parsePath(signalPath_);
//
//		String pes[] = signalPath_.split("/");
//		int n = pes.length;
//		String regexp = SimpleRegexp.convert(pes[n - 1]);
//		n = rtlg.getNumSignals();
//		boolean found = false;
//		for (int i = 0; i < n; i++) {
//			RTLSignal s = rtlg.getSignal(i);
//
//			int m = s.getNumIds();
//
//			for (int j = 0; j < m; j++) {
//				String id = s.getId(j);
//				if (id.matches(regexp)) {
//					trace(s);
//					found = true;
//					break;
//				}
//			}
//		}
//		if (!found)
//			throw new SimException("No signal id matched " + signalPath_);
//	}

//	private RTLPort findPort(String portName) {
//		RTLGraph rtlg = root;
//		if (rtlg == null)
//			return null;
//		// FIXME: handle path
//		return rtlg.findPort(portName);
//	}
//
//	private void assign(String portName, ZILValue value_) throws SimException, ZamiaException {
//		RTLPort p = findPort(portName);
//		if (p == null)
//			throw new SimException("unknown port " + portName);
//
//		assign(p, value_);
//	}
//
//	private ZILValue parseString(String bitStr_) throws SimException {
//		// int len = bitStr_.length();
//		// switch (len) {
//		// case 1 :
//		// return bitStr_.charAt(0);
//		// case 3 :
//		// if (bitStr_.charAt(0) != '\'')
//		// throw new SimException("Malformed bit literal: " + bitStr_);
//		// return bitStr_.charAt(1);
//		// default :
//		// throw new SimException("Malformed bit literal: " + bitStr_);
//		// }
//
//		// FIXME
//		throw new SimException("Sorry, not implemented yet.");
//	}

//	public void doCommand(String cmd) throws SimException, ZamiaException {
//		String[] result = cmd.split("\\s");
//		if (result.length < 1)
//			return;
//
//		if (result[0].equals("run")) {
//			if (result.length < 2) {
//				throw new SimException("usage: run <cycles>");
//			} else {
//				int cycles = Integer.valueOf(result[1]).intValue();
//				run(cycles);
//			}
//		} else if (result[0].equals("trace")) {
//			if (result.length < 2) {
//				throw new SimException("usage: trace /signal/path");
//			} else {
//				if (dump) {
//					logger.debug("Simulator: trace: tracing");
//				}
//				trace(result[1]);
//			}
//		} else if (result[0].equals("untrace")) {
//			if (result.length < 2) {
//				throw new SimException("usage: untrace /signal/path");
//			} else {
//				untrace(result[1]);
//			}
//			// } else if (result[0].equals("assign")) {
//			// if (result.length < 3) {
//			// throw new SimException("usage: assign /signal/path <value>");
//			// } else {
//			// assign(result[1], parseString(result[2]));
//			// }
//		} else {
//			throw new SimException("unknown command " + result[0]);
//		}
//	}

}
