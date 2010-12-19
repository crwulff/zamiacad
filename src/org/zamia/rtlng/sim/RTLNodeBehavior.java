/*
 * Copyright 2007,2009,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/
package org.zamia.rtlng.sim;

import org.zamia.ZamiaException;
import org.zamia.rtlng.RTLNode;
import org.zamia.rtlng.RTLValue;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public interface RTLNodeBehavior {

	// called at simulator startup - e.g. literals get their chance
	// to set their output port value here
	public void reset(RTLNode aNode, RTLSimulator aSimulator, RTLSimContext aContext) throws ZamiaException;

	public void portChange(RTLPortSimAnnotation aPA, RTLValue aValue, RTLSimulator aSimulator) throws ZamiaException;

}
