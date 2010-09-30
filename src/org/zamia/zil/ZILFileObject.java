/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZILFileObject extends ZILObject implements ZILIReferable {

	private PortDir fMode;
	private String fFilename;

	public ZILFileObject(String aId, ZILType aType, String aFilename, PortDir aMode, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, aType, aContainer, aSrc);
		fFilename = aFilename;
		fMode = aMode;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "FileObject (id="+getId()+", type="+getType()+", filename="+fFilename+", mode="+fMode+")";
	}

	public RTLSignal elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

	public void generateInterpreterCodeRef(boolean aIsInertial, boolean aHaveDelay, boolean aHaveReject, ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

	public boolean isValidTarget() {
		return true;
	}
}
