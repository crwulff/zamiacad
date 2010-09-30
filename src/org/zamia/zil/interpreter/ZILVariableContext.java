/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.zil.interpreter;

import java.io.Serializable;

import org.zamia.util.HashMapArray;
import org.zamia.zil.ZILIReferable;
import org.zamia.zil.ZILValue;


/*
*
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class ZILVariableContext implements Serializable {
	public HashMapArray<ZILIReferable,ZILValue> objects = new HashMapArray<ZILIReferable,ZILValue>();
}

