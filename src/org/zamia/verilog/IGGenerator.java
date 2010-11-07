package org.zamia.verilog;

import org.zamia.verilog.analysis.DepthFirstAdapter;
import org.zamia.verilog.node.AModuleDeclaration;
import org.zamia.verilog.node.PIdentifier;

/**
 * 
 * @author Guenter Bartsch
 *
 */

class IGGenerator extends DepthFirstAdapter {

	@Override
	public void caseAModuleDeclaration(AModuleDeclaration aModuleDecl) {

		PIdentifier id = aModuleDecl.getIdentifier();

		System.out.printf("Found a module declaration: '%s'\n", id);

	}

}
