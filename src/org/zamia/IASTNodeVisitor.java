/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 31, 2010
 */
package org.zamia;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface IASTNodeVisitor {

	/**
	 * Called before visiting the children 
	 * 
	 * @param aNode
	 */
	
	public void visitPre (ASTNode aNode);
	
	/**
	 * Called after visiting the children 
	 * 
	 * @param aNode
	 */
	
	public void visitPost (ASTNode aNode);
	
}
