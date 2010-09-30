/* 
 * Copyright 2009, 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2009
 */
package org.zamia.vg;

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

public class VisualGraph {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private HashMapArray<PathName, VGBox> nodeMap;

	private final VGBox fRoot;

	private final Toplevel fToplevel;

	private final ZamiaProject fZPrj;

	private IGManager fIGM;

	public VisualGraph(Toplevel aToplevel, ZamiaProject aZPrj) throws ZamiaException {
		fToplevel = aToplevel;
		fZPrj = aZPrj;
		fIGM = fZPrj.getIGM();

		IGModule module = fIGM.findModule(aToplevel);
		if (module == null) {
			throw new ZamiaException("Couldn't find toplevel " + aToplevel);
		}

		fRoot = new VGBox(aToplevel.getDUUID().getId(), module.getDBID(), null, module.computeSourceLocation(), new PathName(""));
		nodeMap = new HashMapArray<PathName, VGBox>();
	}

	public VGBox getOrCreateBox(PathName aPath, IGItem aItem) {

		PathName path = aPath;

		if (path.endsInNull()) {
			path = path.getParent();
		}

		VGBox box = nodeMap.get(path);
		if (box != null) {
			return box;
		}

		int n = path.getNumSegments();

		if (n == 0) {
			return fRoot;
		}

		box = getOrCreateBox(path.getParent(), null);

		if (box == null) {
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
		box = box.createChild(lastSegment, item.getDBID(), item.computeSourceLocation(), path);

		return box;
	}

	public void dump(int aI, PrintStream aOut) {
		fRoot.dump(aI, aOut);
	}

	public int countBoxes() {
		return fRoot.countBoxes();
	}

	public int countConns() {
		return fRoot.countConns();
	}

	public VGBox getRoot() {
		return fRoot;
	}

	
}
