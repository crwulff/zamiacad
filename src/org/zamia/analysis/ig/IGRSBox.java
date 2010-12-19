/* 
 * Copyright 2009, 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 27, 2009
 */
package org.zamia.analysis.ig;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.zamia.SourceLocation;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * IGModule, IGProcess, IGSequentialStatement, ... counterpart
 * 
 * @author Guenter Bartsch
 *
 */
public class IGRSBox {

	private final String fTitle;

	private final long fDBID;

	private final HashMapArray<String, IGRSSignal> fSignals;

	private final HashMapArray<String, IGRSBox> fChildren;

	private final IGRSBox fParent;

	private final SourceLocation fLocation;

	private final PathName fPath;

	public IGRSBox(String aTitle, long aDBID, IGRSBox aParent, SourceLocation aLocation, PathName aPath) {
		fTitle = aTitle;
		fDBID = aDBID;
		fParent = aParent;
		fLocation = aLocation;
		fPath = aPath;

		fSignals = new HashMapArray<String, IGRSSignal>();
		fChildren = new HashMapArray<String, IGRSBox>();
	}

	public IGRSBox createChild(String aName, long aDBID, SourceLocation aLocation, PathName aPath) {

		IGRSBox box = fChildren.get(aName);

		if (box != null) {
			return box;
		}

		box = new IGRSBox(aName, aDBID, this, aLocation, aPath);

		fChildren.put(aName, box);

		return box;
	}

	public IGRSSignal getOrCreateSignal(IGObject aObj) {

		String id = aObj.getId();
		IGRSSignal signal = fSignals.get(id);
		if (signal != null) {
			return signal;
		}

		long dbid = aObj.getDBID();
		signal = new IGRSSignal(this, id, aObj.getDirection(), dbid, aObj.computeSourceLocation(), fPath.append(id));

		fSignals.put(id, signal);

		return signal;
	}

	public void dump(int aI, PrintStream aOut) {

		VHDLNode.printlnIndented(fTitle + " {", aI, aOut);

		int n = fSignals.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal s = fSignals.get(i);
			s.dump(aI + 2, aOut);
		}

		n = fChildren.size();
		for (int i = 0; i < n; i++) {
			IGRSBox child = fChildren.get(i);
			child.dump(aI + 2, aOut);
		}

		VHDLNode.printlnIndented("}", aI, aOut);
	}

	public int countBoxes() {
		int count = 1;
		int n = fChildren.size();
		for (int i = 0; i < n; i++) {
			IGRSBox child = fChildren.get(i);
			count += child.countBoxes();
		}
		return count;
	}

	public int countConns() {
		int count = 0;
		int n = fChildren.size();
		for (int i = 0; i < n; i++) {
			IGRSBox child = fChildren.get(i);
			count += child.countConns();
		}
		n = fSignals.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal s = fSignals.get(i);

			count += s.getNumConns();

		}
		return count;
	}

	public String getTitle() {
		return fTitle;
	}

	public long getDBID() {
		return fDBID;
	}

	public IGRSBox getParent() {
		return fParent;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public PathName getPath() {
		return fPath;
	}

	private int getNumSubs() {
		return fChildren.size();
	}

	private IGRSBox getSub(int aIdx) {
		return fChildren.get(aIdx);
	}

	public int getNumSignals() {
		return fSignals.size();
	}

	public IGRSSignal getSignal(int aIdx) {
		return fSignals.get(aIdx);
	}

	public HashSetArray<IGRSSignal> getReceivers() {
		HashSetArray<IGRSSignal> receivers = new HashSetArray<IGRSSignal>();

		int n = fSignals.size();
		for (int i = 0; i < n; i++) {
			IGRSSignal s = fSignals.get(i);

			OIDir dir = s.getDir();
			if (dir == OIDir.IN || dir == OIDir.NONE) {
				continue;
			}

			int m = s.getNumExternalConns();
			for (int j = 0; j < m; j++) {

				IGRSSignal conn = s.getExternalConn(j);

				receivers.add(conn);
			}
		}

		return receivers;
	}

	/**
	 * calculates the depth of each box and stores it into a HashMap. Also
	 * generates the reverse mapping of levels to lists of boxs
	 * 
	 * @param aBoxDepth
	 *            VGBox -> Integer mapping
	 * @param aDepthBox
	 *            Integer -> ArrayList of VGBoxs mapping
	 * @return maximum depth of graph
	 */
	public int levelize(HashMap<IGRSBox, Integer> aBoxDepth, HashMap<Integer, ArrayList<IGRSBox>> aDepthBox) {

		// step 1: compute number of connected inputs per box
		// push those boxs which do not have connected inputs
		// on the stack (input boxes / literals)

		HashMap<IGRSBox, Integer> numConnectedInputs = new HashMap<IGRSBox, Integer>();
		LinkedList<IGRSBox> queue = new LinkedList<IGRSBox>();
		HashSet<IGRSBox> todo = new HashSet<IGRSBox>();

		// keep track of how often we have reached each box
		// and what the maximum logic depth was
		// when # reached == # connected inputs we know its
		// logic depth and push it on the stack
		HashMap<IGRSBox, Integer> reached = new HashMap<IGRSBox, Integer>();

		int n = getNumSubs();
		for (int i = 0; i < n; i++) {
			IGRSBox box = getSub(i);
			numConnectedInputs.put(box, 0);
			reached.put(box, 0);
		}

		n = getNumSignals();
		for (int i = 0; i < n; i++) {

			IGRSSignal s = getSignal(i);

			int m = s.getNumConns();
			for (int j = 0; j < m; j++) {

				IGRSSignal conn = s.getConn(j);

				if (conn.getDir() != OIDir.IN) {
					continue;
				}

				IGRSBox box = conn.getBox();

				int numCI = numConnectedInputs.get(box);
				numCI++;
				numConnectedInputs.put(box, numCI);

				todo.add(box);
			}
		}

		// step 2: put all boxes that do not have any connected inputs 
		// into the first column

		n = getNumSubs();
		for (int i = 0; i < n; i++) {
			IGRSBox box = getSub(i);
			int numCI = numConnectedInputs.get(box);
			if (numCI == 0) {
				queue.add(box);

				aBoxDepth.put(box, 0);
			}
		}

		// step 3: recursively levelize remaining boxes

		int maxDepth = 0;

		while (true) {

			while (!queue.isEmpty()) {
				IGRSBox box = queue.poll();
				if (!todo.contains(box)) {
					continue;
				}

				int depth = aBoxDepth.get(box);
				todo.remove(box);

				HashSetArray<IGRSSignal> receivers = box.getReceivers();
				n = receivers.size();

				for (int i = 0; i < n; i++) {

					IGRSSignal receiver = receivers.get(i);
					IGRSBox rbox = receiver.getBox();

					if ((rbox != box) && (todo.contains(rbox))) {

						// calc depth of this receiver

						Integer md = (Integer) aBoxDepth.get(rbox);
						if (md == null) {
							aBoxDepth.put(rbox, depth + 1);
							if (depth >= maxDepth) {
								maxDepth = depth + 1;
							}
						} else {
							int r_depth = md.intValue();
							if (depth >= r_depth) {
								aBoxDepth.put(rbox, depth + 1);
								if (depth >= maxDepth) {
									maxDepth = depth + 1;
								}
							}
						}

						// how often have we reached this receiver?
						int numReached = reached.get(rbox);
						numReached++;
						reached.put(rbox, numReached);

						// how many connected inputs has this receiver?
						int numCI = numConnectedInputs.get(rbox);

						if (numCI == numReached)
							queue.add(rbox);
					}
				}

			}
			if (!todo.isEmpty()) {
				// there seem to be some feedbacks in this
				// graph, so we need to push boxes in
				// (we want to levelize all boxes)

				// => push in those boxes with the highest
				// number of already connected inputs

				int mostOftenReached = 0;

				for (Iterator<IGRSBox> i = todo.iterator(); i.hasNext();) {
					IGRSBox box = i.next();
					int numReached = reached.get(box);
					if (numReached > mostOftenReached)
						mostOftenReached = numReached;
				}

				for (Iterator<IGRSBox> i = todo.iterator(); i.hasNext();) {
					IGRSBox box = i.next();

					int numReached = reached.get(box);
					if (numReached == mostOftenReached) {

						queue.add(box);
						aBoxDepth.put(box, maxDepth);
					}
				}
			} else
				break;
		}

		// generates depthBox from boxDepth
		// boxDepth: box --> depth
		// depthBox: depth (integer) --> list of boxes (arrayList)
		ArrayList<IGRSBox> list = null;
		n = getNumSubs();
		for (int i = 0; i < n; i++) {
			IGRSBox box = getSub(i);
			Object depth = aBoxDepth.get(box);
			if (depth == null)
				continue;

			if (aDepthBox.containsKey(depth)) {
				list = (ArrayList<IGRSBox>) aDepthBox.get(depth);
			} else {
				list = new ArrayList<IGRSBox>();
			}
			list.add(box);
			aDepthBox.put((Integer) depth, list);
		}

		return maxDepth;
	}

}
