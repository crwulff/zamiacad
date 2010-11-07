/* 
 * Copyright 2005-2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on 27.05.2005
 */
package org.zamia.zil;

import java.io.Serializable;

import org.zamia.ZamiaException;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * abstract base class for char/string enum literals
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class ZILEnumLiteral extends ZILObject implements Serializable {
	private int fOrd;
	
	public ZILEnumLiteral(int aOrd, ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
		fOrd = aOrd;
	}
	
	public int getOrd() {
		return fOrd;
	}
	
	@Override 
	public boolean equals(Object o_) {
		if (!(o_ instanceof ZILEnumLiteral))
			return false;
		ZILEnumLiteral el = (ZILEnumLiteral) o_;
		return el.getId().equals(getId());
	}

	public abstract char getChar() throws ZamiaException ;

	public void setOrd(int ord_) {
		fOrd = ord_;
	}

	public abstract ZILEnumLiteral cloneEL() ;

	public ZILValue getValue() {
		return new ZILValue (this, (ZILTypeEnum) getType(), null, null);
	}
}