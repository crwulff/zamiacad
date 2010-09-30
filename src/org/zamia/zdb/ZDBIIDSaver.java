/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zdb;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public interface ZDBIIDSaver {

	public void setDBID(long aId);

	public long getDBID();

	public void setZDB(ZDB aZDB);

}
