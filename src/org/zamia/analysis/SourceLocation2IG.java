/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.analysis;

import java.io.IOException;
import java.util.HashSet;

import org.zamia.ASTNode;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGStructure;
import org.zamia.util.Pair;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DesignUnit;

/**
 * 
 * @author guenter and aoun
 * 
 */

public class SourceLocation2IG {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private SourceLocation2IG() {
	}

	private static void error(String aMsg, SourceLocation aLocation) throws ZamiaException {

		String msg = "IGItemSearch: " + aMsg;
		logger.error("%s", msg);

		if (aLocation != null) {
			SourceFile sf = aLocation.fSF;
			sf.logExcerpt(aLocation.fLine);
		}

		//throw new ZamiaException (aMsg, aLocation);
	}

	/**
	 * Starting from a given source location + tlp, find the IG object closest to the cursor location
	 * plus a tlp that corresponds as closely as possible to that same cursor location.
	 * 
	 * background: the tlp provided by the user can be slightly off so we need to correct it here
	 * 
	 * example:
	 * 
	 * 1: M1                -- a module
	 * 2:   .M2             -- a nested module
	 * 3:      .S           -- a signal declared here
	 * 4:      .P1:process  -- a process named P1 in M2
	 * 5:         S <= ...  -- a reference to signal S
	 * 6:      process      -- an anonymous process in M2
	 * 7:         f <= S... -- another reference to signal S
	 *         
	 * SL     TLP       => IGObject   TLP     
	 * 3,7    M1.M2.P1  => S          M1.M2.S
	 * 5,9    M1.M2     => S          M1.M2.P1
	 * 7,14   M1.M2.P1  => S          M1.M2.
	 * 7,14   M1.M2     => S          M1.M2.
	 *  
	 * @param aLocation
	 * @param aPath
	 * @param aZPrj
	 * @return
	 * @throws IOException
	 * @throws ZamiaException
	 */

	public static Pair<IGItem, ToplevelPath> findNearestItem(SourceLocation aLocation, ToplevelPath aPath, ZamiaProject aZPrj) throws IOException, ZamiaException {

		logger.debug("IGItemSearch: findNearestItem(): starting. location=%s, path=%s", aLocation.toStringAbsolutePath(), aPath);

		/*
		 * we need to find a parent in ig/zil world that matches the current source location
		 */

		ASTNode node = SourceLocation2AST.findNearestASTNode(aLocation, true, aZPrj);

		if (node == null) {
			error("findNearestItem(): Couldn't find any AST object corresponding to the current cursor position.", aLocation);
			return null;
		}

		logger.debug("IGItemSearch: findNearestItem(): found AST object: %s", node);

		// figure out entity duuid
		while (!(node instanceof DesignUnit)) {

			logger.debug("IGItemSearch: looking for DesignUnit AST node, asto=%s", node);

			node = node.getParent();
			if (node == null) {
				error("findNearestItem(): Couldn't find any AST design unit corresponding to the current cursor position.", aLocation);
				return null;
			}
		}

		logger.debug("IGItemSearch: findNearestItem(): finished search for AST design unit, asto=%s", node);

		DMUID duuid = ((DesignUnit) node).getDMUID().getEntityDUUID();

		logger.debug("IGItemSearch: findNearestItem(): found entity DUUID: %s", duuid);

		IGManager igm = aZPrj.getIGM();
		ToplevelPath path = aPath;

		/*
		 * starting at the path supplied by the user,
		 * 
		 * if the path corresponds to an instantiation that instantiates
		 * the DUUID we have determined, add a null segment and we're done.
		 * 
		 * otherwise, search upwards until we find a module that matches the DUUID
		 */

		IGItem moduleItem = igm.findItem(path.getToplevel(), path.getPath());
		ToplevelPath modulePath = path;

		if (moduleItem instanceof IGInstantiation && ((IGInstantiation) moduleItem).getChildDUUID().getEntityDUUID().equals(duuid)) {

			logger.debug("IGItemSearch: findNearestItem(): supplied path corresponds to a matching instantiation, path: %s, item: %s", modulePath, moduleItem);

			modulePath = modulePath.descend();
			moduleItem = igm.findItem(modulePath.getToplevel(), modulePath.getPath());

			logger.debug("IGItemSearch: findNearestItem(): null segment appended, path: %s, item: %s", modulePath, moduleItem);

		} else {

			while (!(moduleItem instanceof IGModule)) {

				logger.debug("IGItemSearch: findNearestItem(): looking for parent ig module, path: %s, item: %s", modulePath, moduleItem);

				if (modulePath == null || modulePath.getNumSegments() == 0) {
					error("IGItemSearch: findNearesItem(): path is empty. failed to find module.", aLocation);
					return null;
				}

				modulePath = modulePath.getNullParent();
				moduleItem = igm.findItem(modulePath.getToplevel(), modulePath.getPath());
			}

		}

		logger.debug("IGItemSearch: findNearestItem(): found parent ig module (?), path: %s, item: %s", modulePath, moduleItem);

		if (!(moduleItem instanceof IGModule)) {
			error("IGItemSearch: findNearestItem(): This is not a module: " + moduleItem, aLocation);
			return null;
		}

		// does it match the duuid we found via AST ?

		IGModule module = (IGModule) moduleItem;
		DMUID igDUUID = module.getDUUID().getEntityDUUID();
		if (!igDUUID.equals(duuid)) {
			error("IGItemSearch: findNearestItem(): Found a module, but DUUID doesn't match. Was looking for " + duuid + ", found " + igDUUID, aLocation);
			return null;
		}

		Pair<IGItem, ToplevelPath> res = findNearestItemP(0, 0, aLocation.fLine, aLocation.fCol, moduleItem, modulePath);

		if (res != null) {
			IGItem item = res.getFirst();
			if (item != null) {
				SourceLocation loc = item.computeSourceLocation();
				if (loc.fLine == aLocation.fLine) {
					return res;
				}
			}
		}

		error("findNearestItem(): No IG item found that matches the current cursor location.", aLocation);
		return null;
	}

	private static Pair<IGItem, ToplevelPath> findNearestItemP(int aLine, int aCol, int aGoalLine, int aGoalCol, IGItem aParent, ToplevelPath aPath) {

		if (aParent == null)
			return null;

		int line = aLine;
		int col = aCol;

		ZStack<Pair<IGItem, ToplevelPath>> s = new ZStack<Pair<IGItem, ToplevelPath>>();
		s.push(new Pair<IGItem, ToplevelPath>(aParent, aPath));

		HashSet<IGItem> done = new HashSet<IGItem>();
		IGItem closest = null;
		ToplevelPath closestPath = aPath;

		while (!s.isEmpty()) {

			Pair<IGItem, ToplevelPath> pair = s.pop();
			IGItem o = pair.getFirst();
			ToplevelPath path = pair.getSecond();

			if (o == null)
				continue;

			if (done.contains(o))
				continue;

			done.add(o);

			int n = o.getNumChildren();
			for (int i = n - 1; i >= 0; i--) {
				IGItem o2 = o.getChild(i);
				if (o2 != null) {

					ToplevelPath childPath = path;
					if (o2 instanceof IGStructure) {
						IGStructure struct = (IGStructure) o2;

						String label = struct.getLabel();

						if (label != null && label.length() > 0) {
							childPath = path.append(struct.getLabel());
						}
					} else if (o2 instanceof IGProcess) {
						IGProcess proc = (IGProcess) o2;

						String label = proc.getLabel();

						if (label != null && label.length() > 0) {
							childPath = path.append(proc.getLabel());
						}
					}

					s.push(new Pair<IGItem, ToplevelPath>(o2, childPath));
				}
			}

			SourceLocation l = o.computeSourceLocation();
			if (l == null)
				continue;

			if (l.fLine != aGoalLine) {
				continue;
			}

			if (isCloser(l.fLine, l.fCol, line, col, aGoalLine, aGoalCol)) {
				logger.debug("SourceLocation2IG: findNearestItemP: item=%s, line=%d, col=%d, goalLine=%d, goalCol=%d", o, l.fLine, l.fCol, aGoalLine, aGoalCol);
				closest = o;
				closestPath = (o instanceof IGObject) ? path.append(((IGObject) o).getId()) : path;
				line = l.fLine;
				col = l.fCol;
			}
		}

		return new Pair<IGItem, ToplevelPath>(closest, closestPath);
	}

	private static boolean isCloser(int aY, int aX, int aOY, int aOX, int aMaxY, int aMaxX) {
		if (aY > aMaxY)
			return false;
		if ((aY == aMaxY) && (aX > aMaxX))
			return false;

		if (aY < aOY)
			return false;
		if (aY > aOY)
			return true;
		if (aX < aOX)
			return false;
		return true;
	}

}
