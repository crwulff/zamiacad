/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 31, 2010
 */
package org.zamia.verilog;

import org.zamia.ASTNode;
import org.zamia.IASTNodeVisitor;
import org.zamia.SourceFile;
import org.zamia.verilog.node.Token;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class SourceLocationGenerator implements IASTNodeVisitor {

	private int fStartLine;

	private int fEndLine;

	private int fStartCol;

	private int fEndCol;

	private ASTNode fCurNode;

	private SourceFile fSF;

	@Override
	public void visitPost(ASTNode aNode) {

		fCurNode = aNode;
		fStartLine = 1000;
		fEndLine = -1000;
		fStartCol = 1000;
		fEndCol = -1000;

		//System.out.printf("Generating source location for %s...\n", aNode);

		if (aNode instanceof Token) {

			Token t = (Token) aNode;

			int l = t.getLine();
			int c = t.getPos();

			aNode.setStartLine(l);
			aNode.setEndLine(l);
			aNode.setStartCol(c);
			aNode.setEndCol(c + t.getText().length());

			//System.out.printf("   TOKEN: line=%d\n", l);

			return;
		}

		// this is a production, so it will not have proper source information
		// we need to find the first and last elements in it

		aNode.visit(new IASTNodeVisitor() {

			@Override
			public void visitPre(ASTNode aNode) {

				if (aNode == fCurNode)
					return;

				int sl = aNode.getStartLine();
				int el = aNode.getEndLine();
				int sc = aNode.getStartCol();
				int ec = aNode.getEndCol();

				//System.out.printf("   CHILD: '%s' line=%d\n", aNode, sl);

				if (sl < fStartLine) {
					fStartLine = sl;
					fStartCol = sc;
					fSF = aNode.getSource();
				}

				if (sl == fStartLine) {
					if (sc < fStartCol) {
						fStartCol = sc;
						fSF = aNode.getSource();
					}
				}

				if (el > fEndLine) {
					fEndLine = el;
					fEndCol = ec;
				}

				if (fEndLine == el) {
					if (ec > fEndCol) {
						fEndCol = ec;
					}
				}
			}

			@Override
			public void visitPost(ASTNode aNode) {
			}
		});

		aNode.setStartLine(fStartLine);
		aNode.setEndLine(fEndLine);
		aNode.setStartCol(fStartCol);
		aNode.setEndCol(fEndCol);
		aNode.setSource(fSF);

		//System.out.printf("   DONE: sl=%d sc=%d el=%d ec=%d %s\n", fStartLine, fStartCol, fEndLine, fEndCol, fSF);

	}

	@Override
	public void visitPre(ASTNode aNode) {
		// TODO Auto-generated method stub

	}

}
