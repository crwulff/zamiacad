/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 31, 2010
 */
package org.zamia.vhdl.vhdl2008;

import org.zamia.ASTNode;
import org.zamia.IASTNodeVisitor;
import org.zamia.SourceFile;
import org.zamia.vhdl.ast.Identifier;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class SourceLocationGenerator implements IASTNodeVisitor {

	private int fEndLine;

	private int fEndCol;

	private ASTNode fCurNode;

	private final SourceFile fSF;

	public SourceLocationGenerator(SourceFile aSF) {
		fSF = aSF;
	}

	@Override
	public void visitPost(ASTNode aNode) {

		fCurNode = aNode;
		fEndLine = -1000;
		fEndCol = -1000;

		aNode.setSource(fSF);

		//System.out.printf("Generating source location for %s...\n", aNode);

		if (aNode instanceof Identifier) {

			Identifier id = (Identifier) aNode;

			int line = aNode.getStartLine();
			int col = aNode.getStartCol();

			aNode.setEndLine(line);
			aNode.setEndCol(col + id.getImage().length());

			return;
		}

		// this is a production, so it will not have proper source information
		// we need to find the first and last elements in it

		aNode.visit(new IASTNodeVisitor() {

			@Override
			public void visitPre(ASTNode aNode) {

				if (aNode == fCurNode)
					return;

				int el = aNode.getEndLine();
				int ec = aNode.getEndCol();

				//System.out.printf("   CHILD: '%s' line=%d\n", aNode, sl);

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

		aNode.setEndLine(fEndLine);
		aNode.setEndCol(fEndCol);

		//System.out.printf("   DONE: sl=%d sc=%d el=%d ec=%d %s\n", fStartLine, fStartCol, fEndLine, fEndCol, fSF);

	}

	@Override
	public void visitPre(ASTNode aNode) {
	}

}
