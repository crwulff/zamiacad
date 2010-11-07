/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 1, 2010
 */
package org.zamia.analysis;

import org.zamia.ASTNode;
import org.zamia.IASTNodeVisitor;
import org.zamia.SourceLocation;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class SearchClosestASTNode implements IASTNodeVisitor {

	private int fGoalLine, fGoalCol;

	private int fClosestLine, fClosestCol;

	private ASTNode fClosestNode;

	public SearchClosestASTNode(int aGoalLine, int aGoalCol) {
		super();
		fGoalLine = aGoalLine;
		fGoalCol = aGoalCol;
		fClosestLine = -1;
		fClosestCol = -1;
		fClosestNode = null;
	}

	@Override
	public void visitPre(ASTNode aNode) {

		SourceLocation l = aNode.getLocation();
		if (l == null)
			return;

		if (isCloser(l.fLine, l.fCol)) {
			fClosestNode = aNode;
			fClosestLine = l.fLine;
			fClosestCol = l.fCol;
		}

	}

	private boolean isCloser(int aLine, int aCl) {
		if (aLine > fGoalLine)
			return false;
		if ((aLine == fGoalLine) && (aCl > fGoalCol))
			return false;

		if (aLine < fClosestLine)
			return false;
		if (aLine > fClosestLine)
			return true;
		if (aCl < fClosestCol)
			return false;
		return true;
	}

	public ASTNode getClosestNode() {
		return fClosestNode;
	}
	
	@Override
	public void visitPost(ASTNode aNode) {
		// TODO Auto-generated method stub

	}

}
