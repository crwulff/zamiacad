/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
 */

package org.zamia.analysis;

import java.io.IOException;
import java.util.HashSet;

import org.zamia.DUManager;
import org.zamia.SFDUInfo;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DesignUnit;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SourceLocation2AST {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private SourceLocation2AST() {
	}

	public static ASTObject findNearestASTObject(SourceLocation aLocation, boolean aCheckLine, ZamiaProject aZPrj) throws IOException, ZamiaException {
		DUManager dum = aZPrj.getDUM();
		SourceFile sf = aLocation.fSF;

		SFDUInfo info = dum.compileFile(sf, null);
		if (info == null) {
			return null;
		}
		
		ASTObject nearest = null;
		int n = info.getNumDUUIDs();
		for (int i = n - 1; i >= 0; i--) {
			DUUID duuid = info.getDUUID(i);

			DesignUnit du = dum.getDU(duuid);
			
			if (du == null) {
				continue;
			}

			ASTObject io = searchClosestASTObject(0, 0, aLocation.fLine, aLocation.fCol, du);
			if (io != null) {
				SourceLocation loc = io.getLocation();
				if (!aCheckLine || loc.fLine == aLocation.fLine) {
					nearest = io;
					break;
				}
			}
		}
		return nearest;
	}

	private static ASTObject searchClosestASTObject(int aLine, int aCol, int aGoalLine, int aGoalCol, ASTObject aParent) {

		if (aParent == null)
			return null;

		int line = aLine;
		int col = aCol;

		ZStack<ASTObject> s = new ZStack<ASTObject>();
		s.push(aParent);

		HashSet<ASTObject> done = new HashSet<ASTObject>();

		ASTObject closest = null;

		while (!s.isEmpty()) {

			ASTObject o = s.pop();

			// logger.debug
			// ("searchClosestIO: o=%s, closest=%s, line=%d, goalLine=%d", o,
			// closest, line, goalLine_);

			if (o == null)
				continue;

			if (done.contains(o))
				continue;

			done.add(o);

			SourceLocation l = o.getLocation();
			if (l == null)
				continue;

			if (isCloser(l.fLine, l.fCol, line, col, aGoalLine, aGoalCol)) {
				closest = o;
				line = l.fLine;
				col = l.fCol;
			}

			int n = o.getNumChildren();
			for (int i = n - 1; i >= 0; i--) {
				ASTObject o2 = o.getChild(i);
				s.push(o2);
			}
		}

		return closest;
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
