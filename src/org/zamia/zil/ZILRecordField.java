/*
 * Copyright 2005-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 6, 2005
 */

package org.zamia.zil;

import java.io.Serializable;

/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class ZILRecordField implements Serializable {
	public ZILType type;
	public String id;
	public ZILRecordField(String id_, ZILType type_) {
		type = type_;
		id = id_;
	}
	public ZILType getType() {
		return type;
	}
}