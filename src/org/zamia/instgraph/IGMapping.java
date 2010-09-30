/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
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

	public void generateEntryCode(boolean aIgnoreDir, IGInterpreterCode aIc, SourceLocation aSrc) throws ZamiaException {

		OIDir dir = fFormal.getDirection();

		if (aIgnoreDir || dir == OIDir.IN || dir == OIDir.INOUT) {
			fFormal.generateCodeRef(false, !aIgnoreDir, aIc);
			fActual.generateCode(true, aIc);
			aIc.add(new IGPopStmt(false, false, false, aSrc, getZDB()));
		}
	}

	@Override
	public String toString() {
		return "IGMapping(formal=" + fFormal + ", actual=" + fActual + ")";
	}

	public void generateExitCode(IGInterpreterCode aCode, SourceLocation aSrc) throws ZamiaException {

		OIDir dir = fFormal.getDirection();

		if (dir == OIDir.OUT || dir == OIDir.INOUT) {
			fActual.generateCodeRef(true, true, aCode);
			fFormal.generateCode(false, aCode);
			aCode.add(new IGPopStmt(false, false, false, aSrc, getZDB()));
		}
	}

	@Override
	public IGItem getChild(int aIdx) {
		return aIdx == 0 ? fActual : fFormal;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}
}
