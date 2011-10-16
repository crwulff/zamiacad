/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia.verilog;

import java.io.Serializable;

import org.zamia.IDesignModule;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGDesignUnit;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.verilog.node.AModuleDeclaration;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ModuleWrapper implements IDesignModule, Serializable {

	protected transient DMUID fDMUID = null;

	private final String fId;

	private final String fLibId;

	private final AModuleDeclaration fModuleDecl;

	public ModuleWrapper (String aId, String aLibId, AModuleDeclaration aModuleDecl) {
		fId = aId;
		fLibId = aLibId;
		fModuleDecl = aModuleDecl;
	}
	
	@Override
	public DMUID getDMUID() throws ZamiaException {
		if (fDMUID != null) {
			return fDMUID;
		}

		fDMUID = getDMUID(fLibId);

		return fDMUID;
	}

	@Override
	public DMUID getDMUID(String aLibId) throws ZamiaException {
		return new DMUID(LUType.Architecture, aLibId, getId(), null);
	}

	public String getId() {
		return fId;
	}

	public AModuleDeclaration getModuleDecl() {
		return fModuleDecl;
	}

	@Override
	public SourceLocation getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void computeIG(IGManager aIGM, IGDesignUnit aDesignModule) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeStatementsIG(IGManager aIGM, IGModule aModule) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getDBID() {
		// FIXME
		return hashCode();
	}


}
