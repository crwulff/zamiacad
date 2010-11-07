/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia;

import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.vhdl.ast.DMUID;

/**
 * Common interface of VHDL (DesignUnit) and Verilog (Module) top-level AST nodes
 * 
 * @author Guenter Bartsch
 *
 */

public interface IDesignModule {

	public DMUID getDMUID() throws ZamiaException;

	public DMUID getDMUID(String aLibId) throws ZamiaException;

	public SourceLocation getLocation();

	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule);

	public void computeStatementsIG(IGManager aIGM, IGModule aModule);

}
