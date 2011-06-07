/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGMapStmt;
import org.zamia.zdb.ZDB;

/**
 * @author Guenter Bartsch
 */
@SuppressWarnings("serial")
public class IGMapping extends IGItem {

	private IGOperation fFormal;

	private IGOperation fActual;

	public IGMapping(IGOperation aFormal, IGOperation aActual, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fFormal = aFormal;
		fActual = aActual;
	}

	public IGOperation getFormal() {
		return fFormal;
	}

	public IGOperation getActual() {
		return fActual;
	}

	public void generateCode(IGInterpreterCode aCode, SourceLocation aSrc) throws ZamiaException {
		fFormal.generateCode(false, aCode);
		fActual.generateCode(true, aCode);
		aCode.add(new IGMapStmt(aSrc, getZDB()));
	}

	@Override
	public String toString() {
		return "IGMapping(formal=" + fFormal + ", actual=" + fActual + ")";
	}

	@Override
	public IGItem getChild(int aIdx) {
		return aIdx == 0 ? fActual : fFormal;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	public String toHRString() {
		return fFormal.toHRString() + " => " + fActual.toHRString();
	}
}
