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

import org.zamia.ASTNode;
import org.zamia.DMManager;
import org.zamia.IDesignModule;
import org.zamia.SFDMInfo;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.vhdl.ast.DMUID;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SourceLocation2AST {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private SourceLocation2AST() {
	}

	public static ASTNode findNearestASTNode(SourceLocation aLocation, boolean aCheckLine, ZamiaProject aZPrj) throws IOException, ZamiaException {
		DMManager dum = aZPrj.getDUM();
		SourceFile sf = aLocation.fSF;

		SFDMInfo info = dum.compileFile(sf, null);
		if (info == null) {
			return null;
		}

		ASTNode nearest = null;
		int n = info.getNumDMUIDs();
		for (int i = n - 1; i >= 0; i--) {
			DMUID dmuid = info.getDMUID(i);

			IDesignModule dm = dum.getDM(dmuid);

			if (dm == null) {
				continue;
			}

			if (dm instanceof ASTNode) {

				ASTNode node = (ASTNode) dm;

				SearchClosestASTNode visitor = new SearchClosestASTNode(aLocation.fLine, aLocation.fCol);

				node.visit(visitor);

				ASTNode closestNode = visitor.getClosestNode();
				if (closestNode != null) {
					SourceLocation loc = closestNode.getLocation();
					if (!aCheckLine || loc.fLine == aLocation.fLine) {
						nearest = closestNode;
						break;
					}
				}
			}
		}
		return nearest;
	}

}
