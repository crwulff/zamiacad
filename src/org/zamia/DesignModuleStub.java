/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 13, 2009
 */
package org.zamia;

import java.io.Serializable;

import org.zamia.vhdl.ast.DMUID;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class DesignModuleStub implements Serializable {
	
	private final DMUID fDUUID;
	
	private final SourceFile fSF;
	
	private final int fPriority;

	private boolean fUseFSCache;
	
	public DesignModuleStub (DMUID aDUUID, SourceFile aSF, int aPriority, boolean aUseFSCache) {
		fDUUID = aDUUID;
		fSF = aSF;
		fPriority = aPriority;
		fUseFSCache = aUseFSCache;
	}
	
	public SourceFile getSourceFile() {
		return fSF;
	}
	
	public int getPriority() {
		return fPriority;
	}
	
	public DMUID getDUUID() {
		return fDUUID;
	}
	
	@Override
	public String toString() {
		return "Stub: "+fDUUID;
	}

	public boolean isUseFSCache() {
		return fUseFSCache;
	}
}
