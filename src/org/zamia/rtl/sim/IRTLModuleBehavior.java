/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/
package org.zamia.rtl.sim;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public interface IRTLModuleBehavior {

	// called at simulator startup - e.g. literals get their chance
	// to set their output port value here
	public void init(RTLModule aModule, Simulator aSimulator) throws ZamiaException;

	public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException;

}
