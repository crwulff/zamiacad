/* 
 * Copyright 2009, 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2009
 */
package org.zamia.analysis.ig;

import java.io.PrintStream;

import org.zamia.ExceptionLogger;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGRSResult {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private HashMapArray<PathName, IGRSNode> fNodeMap;

	private final IGRSNode fRoot;

	private final Toplevel fToplevel;

	private final ZamiaProject fZPrj;

	private IGManager fIGM;

	public IGRSResult(Toplevel aToplevel, ZamiaProject aZPrj) throws ZamiaException {
		fToplevel = aToplevel;
		fZPrj = aZPrj;
		fIGM = fZPrj.getIGM();

		IGModule module = fIGM.findModule(aToplevel);
		if (module == null) {
			throw new ZamiaException("Couldn't find toplevel " + aToplevel);
		}

		fRoot = new IGRSNode(aToplevel.getDUUID().getId(), null, module.computeSourceLocation(), new PathName(""));
		fNodeMap = new HashMapArray<PathName, IGRSNode>();
	}

	public IGRSNode getOrCreateNode(PathName aPath, IGItem aItem) {

		PathName path = aPath;

		if (path.endsInNull()) {
			path = path.getParent();
		}

		IGRSNode node = fNodeMap.get(path);
		if (node != null) {
			return node;
		}

		int n = path.getNumSegments();

		if (n == 0) {
			return fRoot;
		}

		node = getOrCreateNode(path.getParent(), null);

		if (node == null) {
			return null;
		}

		IGItem item = aItem;
		if (item == null) {
			item = fIGM.findItem(fToplevel, path.append((String) null));
			if (item == null) {
				logger.error("VisualGraph: Have no IGItem corresponding to %s", aPath);
				return null;
			}
		}
		
		String lastSegment = path.getSegment(n - 1);
		node = node.getOrCreateChild(lastSegment, item.getDBID(), item.computeSourceLocation(), path);

		return node;
	}

	public void dump(int aI, PrintStream aOut) {
		fRoot.dump(aI, aOut);
	}

	public int countNodes() {
		return fRoot.countNodes();
	}

	public int countConns() {
		return fRoot.countConns();
	}

	public IGRSNode getRoot() {
		return fRoot;
	}

	
}
