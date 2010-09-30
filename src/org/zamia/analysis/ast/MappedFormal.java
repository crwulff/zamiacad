/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.analysis.ast;

import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.InterfaceDeclaration;
/**
 * @author guenter bartsch
 */

public class MappedFormal {

	public InterfaceDeclaration fFormal;
	public ASTObject fASTObject;
	
	public MappedFormal(InterfaceDeclaration aFormal, ASTObject aASTObject) {
		
		fFormal = aFormal;
		fASTObject = aASTObject;
	}
	
}
