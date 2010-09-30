/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */

package org.zamia.zil;

import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.zil.interpreter.ZILInterpreterCode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public interface ZILInterface extends ZILIReferable {

	public PortDir getDir() ;

	public void generateCreationCode(ZILInterpreterCode aCode);

}
