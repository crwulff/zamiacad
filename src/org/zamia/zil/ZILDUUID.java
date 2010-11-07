/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 21, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zil.interpreter.ZILInterpreterCode;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILDUUID implements ZILIObject{

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private DMUID fDUUID;
	private String fId;

	public ZILDUUID(String aId, DMUID aDUUID) {
		fId = aId;
		fDUUID = aDUUID;
	}
	
	public String getId() {
		return fId;
	}

	public DMUID getDUUID() {
		return fDUUID;
	}
	
	public ZILIContainer getContainer() {
		return null;
	}

	public VHDLNode getSrc() {
		return null;
	}

	public ZILType getType() {
		return ZILTypeVoid.getInstance();
	}

	public void generateInterpreterCode(ZILInterpreterCode aCode) throws ZamiaException {
		throw new ZamiaException ("Internal error.");
	}

	public ZILValue computeConstant(ZILCache cache) throws ZamiaException {
		throw new ZamiaException ("Internal error: Library stubs cannot be used as constants.", getSrc());
	}

	public boolean isConstant(ZILCache cache_) throws ZamiaException {
		// FIXME: implement in all subclasses, remove this stub
		throw new ZamiaException("Sorry, not implemented yet.");
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "DUUID (id='"+fId+"', duuid="+getDUUID()+")";
	}

	public void addAttribute(ZILAttribute aAttr) {
	}

	public ZILAttribute getAttribute(String aId) {
		return null;
	}

}
