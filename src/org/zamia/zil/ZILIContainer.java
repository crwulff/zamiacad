/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 18, 2008
 */
package org.zamia.zil;

import org.zamia.DMManager;
import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface ZILIContainer extends ZILIObject {

	public ZILIObject resolve(String aId);

	public void add(ZILIObject object, VHDLNode aSrc) throws ZamiaException;
	
	public ZILIContainer getContainer();

	public void addEntityImporter(String aLibId, DMManager aDU);
}
