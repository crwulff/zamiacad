/* 
 * Copyright 2009, 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 27, 2009
 */
package org.zamia.vg;

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
import org.zamia.vhdl.ast.ASTObject;


/**
 * IGModule, IGProcess, IGSequentialStatement, ... counterpart
 * 
 * @author Guenter Bartsch
 *
 */
public class VGBox {

	private final String fTitle;

	private final long fDBID;

	private final HashMapArray<String, VGSignal> fSignals;

	private final HashMapArray<String, VGBox> fChildren;

	private final VGBox fParent;

	private final SourceLocation fLocation;

	private final PathName fPath;

	public VGBox(String aTitle, long aDBID, VGBox aParent, SourceLocation aLocation, PathName aPath) {
		fTitle = aTitle;
		fDBID = aDBID;
		fParent = aParent;
		fLocation = aLocation;
		fPath = aPath;

		fSignals = new HashMapArray<String, VGSignal>();
		fChildren = new HashMapArray<String, VGBox>();
	}

	public VGBox createChild(String aName, long aDBID, SourceLocation aLocation, PathName aPath) {

		VGBox box = fChildren.get(aName);

		if (box != null) {
			return box;
		}

		box = new VGBox(aName, aDBID, this, aLocation, aPath);

		fChildren.put(aName, box);

		return box;
	}

	public VGSignal getOrCreateSignal(IGObject aObj) {

		String id = aObj.getId();
		VGSignal signal = fSignals.get(id);
		if (signal != null) {
			return signal;
		}

		long dbid = aObj.getDBID();
		signal = new VGSignal(this, id, aObj.getDirection(), dbid, aObj.computeSourceLocation(), fPath.append(id));

		fSignals.put(id, signal);

		return signal;
	}

	public void dump(int aI, PrintStream aOut) {

		ASTObject.printlnIndented(fTitle + " {", aI, aOut);

		int n = fSignals.size();
		for (int i = 0; i < n; i++) {
			VGSignal s = fSignals.get(i);
			s.dump(aI + 2, aOut);
		}

		n = fChildren.size();
		for (int i = 0; i < n; i++) {
			VGBox child = fChildren.get(i);
			child.dump(aI + 2, aOut);
		}

		ASTObject.printlnIndented("}", aI, aOut);
	}

	public int countBoxes() {
		int count = 1;
		int n = fChildren.size();
		for (int i = 0; i < n; i++) {
			VGBox child = fChildren.get(i);
			count += child.countBoxes();
		}
		return count;
	}

	public int countConns() {
		int count = 0;
		int n = fChildren.size();
		for (int i = 0; i < n; i++) {
			VGBox child = fChildren.get(i);
			count += child.countConns();
		}
		n = fSignals.size();
		for (int i = 0; i < n; i++) {
			VGSignal s = fSignals.get(i);

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

	public VGBox getParent() {
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

	private VGBox getSub(int aIdx) {
		return fChildren.get(aIdx);
	}

	public int getNumSignals() {
		return fSignals.size();
	}

	public VGSignal getSignal(int aIdx) {
		return fSignals.get(aIdx);
	}

	public HashSetArray<VGSignal> getReceivers() {
		HashSetArray<VGSignal> receivers = new HashSetArray<VGSignal>();

		int n = fSignals.size();
		for (int i = 0; i < n; i++) {
			VGSignal s = fSignals.get(i);

			OIDir dir = s.getDir();
			if (dir == OIDir.IN || dir == OIDir.NONE) {
				continue;
			}

			int m = s.getNumExternalConns();
			for (int j = 0; j < m; j++) {

				VGSignal conn = s.getExternalConn(j);

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
	public int levelize(HashMap<VGBox, Integer> aBoxDepth, HashMap<Integer, ArrayList<VGBox>> aDepthBox) {

		// step 1: compute number of connected inputs per box
		// push those boxs which do not have connected inputs
		// on the stack (input boxes / literals)

		HashMap<VGBox, Integer> numConnectedInputs = new HashMap<VGBox, Integer>();
		LinkedList<VGBox> queue = new LinkedList<VGBox>();
		HashSet<VGBox> todo = new HashSet<VGBox>();

		// keep track of how often we have reached each box
		// and what the maximum logic depth was
		// when # reached == # connected inputs we know its
		// logic depth and push it on the stack
		HashMap<VGBox, Integer> reached = new HashMap<VGBox, Integer>();

		int n = getNumSubs();
		for (int i = 0; i < n; i++) {
			VGBox box = getSub(i);
			numConnectedInputs.put(box, 0);
			reached.put(box, 0);
		}

		n = getNumSignals();
		for (int i = 0; i < n; i++) {

			VGSignal s = getSignal(i);

			int m = s.getNumConns();
			for (int j = 0; j < m; j++) {

				VGSignal conn = s.getConn(j);

				if (conn.getDir() != OIDir.IN) {
					continue;
				}

				VGBox box = conn.getBox();

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
			VGBox box = getSub(i);
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
				VGBox box = queue.poll();
				if (!todo.contains(box)) {
					continue;
				}

				int depth = aBoxDepth.get(box);
				todo.remove(box);

				HashSetArray<VGSignal> receivers = box.getReceivers();
				n = receivers.size();

				for (int i = 0; i < n; i++) {

					VGSignal receiver = receivers.get(i);
					VGBox rbox = receiver.getBox();

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

				for (Iterator<VGBox> i = todo.iterator(); i.hasNext();) {
					VGBox box = i.next();
					int numReached = reached.get(box);
					if (numReached > mostOftenReached)
						mostOftenReached = numReached;
				}

				for (Iterator<VGBox> i = todo.iterator(); i.hasNext();) {
					VGBox box = i.next();

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
		ArrayList<VGBox> list = null;
		n = getNumSubs();
		for (int i = 0; i < n; i++) {
			VGBox box = getSub(i);
			Object depth = aBoxDepth.get(box);
			if (depth == null)
				continue;

			if (aDepthBox.containsKey(depth)) {
				list = (ArrayList<VGBox>) aDepthBox.get(depth);
			} else {
				list = new ArrayList<VGBox>();
			}
			list.add(box);
			aDepthBox.put((Integer) depth, list);
		}

		return maxDepth;
	}

}
