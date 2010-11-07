/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionAccess extends TypeDefinition {

	private TypeDefinition td;

	public TypeDefinitionAccess(TypeDefinition td_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		setTypeDefinition(td_);
	}

	private void setTypeDefinition(TypeDefinition td_) {
		td = td_;
		td.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		return td;
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aCache) {

		IGType subtype = td.computeIG(aContainer, aCache);

		IGType type = new IGType(TypeCat.ACCESS, null, null, null, subtype, null, false, getLocation(), aCache.getZDB());

		try {
			// declare implicit deallocate subprogram

			IGSubProgram sub = new IGSubProgram(aContainer.getDBID(), null, "DEALLOCATE", getLocation(), aCache.getZDB());

			IGContainer container = sub.getContainer();

			IGObject intf = new IGObject(OIDir.INOUT, null, IGObjectCat.VARIABLE, type, "T_PTR", getLocation(), aCache.getZDB());
			container.addInterface(intf);
			container.storeOrUpdate();
			
			sub.setBuiltin(IGBuiltin.DEALLOCATE);
			
			aContainer.add(sub);
			
		} catch (ZamiaException e) {
			reportError(e);
		}

		return type;
	}
	
	@Override
	public String toString() {
		return "ACCESS "+td;
	}
}