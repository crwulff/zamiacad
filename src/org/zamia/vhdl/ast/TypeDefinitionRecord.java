/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.util.ArrayList;
import java.util.Iterator;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGSubProgram.IGBuiltin;
import org.zamia.instgraph.IGType.TypeCat;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class TypeDefinitionRecord extends TypeDefinition {

	private ArrayList<RecordElementDeclaration> elements; // of ElementDeclaration

	public TypeDefinitionRecord(ASTObject parent_, long location_) {
		super(parent_, location_);
		elements = new ArrayList<RecordElementDeclaration>();
	}

	public void addElement(String id_, TypeDefinition t_, long location_) {
		t_.setParent(this);
		elements.add(new RecordElementDeclaration(id_, t_, this, location_));
	}

	@Override
	public int getNumChildren() {
		return elements.size();
	}

	@Override
	public ASTObject getChild(int idx_) {
		return elements.get(idx_);
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aCache) {
		IGType rt = new IGType(TypeCat.RECORD, null, null, null, null, null, false, getLocation(), aCache.getZDB());

		for (Iterator<RecordElementDeclaration> i = elements.iterator(); i.hasNext();) {
			RecordElementDeclaration element = (RecordElementDeclaration) i.next();

			IGType elementType = element.getTypeDefinition().computeIG(aContainer, aCache);
			try {
				rt.addRecordField(element.getId(), elementType, element.getLocation());
			} catch (ZamiaException e) {
				reportError(e);
			}
		}

		try {
			IGTypeStatic sType = rt.computeStaticType(aCache.getInterpreterEnv(), ASTErrorMode.RETURN_NULL, new ErrorReport());
			if (sType != null) {
				rt = sType;
			}

			IGType b = aContainer.findBoolType();

			aContainer.addBuiltinOperator("\"=\"", rt, rt, b, IGBuiltin.RECORD_EQUALS, getLocation());
			aContainer.addBuiltinOperator("\"/=\"", rt, rt, b, IGBuiltin.RECORD_NEQUALS, getLocation());

		} catch (ZamiaException e) {
		}

		rt.storeOrUpdate();

		return rt;
	}

}