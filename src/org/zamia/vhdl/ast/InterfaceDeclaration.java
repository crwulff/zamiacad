/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 13.06.2005
 */

package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.rtl.RTLPort.PortDir;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class InterfaceDeclaration extends DeclarativeItem {

	public enum InterfaceKind {
		SIGNAL, VARIABLE, FILE, CONSTANT
	};

	protected TypeDefinition type;

	private Operation value;

	private InterfaceKind kind;

	private PortDir dir;

	public InterfaceDeclaration(String id_, TypeDefinition type_, PortDir dir_, Operation value_, InterfaceKind kind_, ASTObject parent_, long location_) {
		super(id_, parent_, location_);
		setType(type_);
		setValue(value_);
		kind = kind_;
		dir = dir_;
	}

	private void setValue(Operation value_) {
		value = value_;
		if (value != null)
			value.setParent(this);
	}

	public Operation getValue() {
		return value;
	}

	public String getId() {
		return id;
	}

	private void setType(TypeDefinition type_) {
		type = type_;
		type.setParent(this);
	}

	public TypeDefinition getType() {
		return type;
	}

	public PortDir getDir() {
		return dir;
	}

	public boolean isSignal() {
		return kind == InterfaceKind.SIGNAL;
	}

	@Override
	public String toString() {
		return getId() + " " + dir.name() + " " + getType();
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return type;
		case 1:
			return value;
		}
		return null;
	}

	public InterfaceKind getKind() {
		return kind;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		if (category_ == ObjectCat.Type) {

			// FIXME: TODO

			// if (td instanceof TypeDefinitionSubType)
			//			
			// if (getId().equals(id_)) {
			//				
			// result_.add(new ReferenceSite(getLocation(), false));
			//				
			// }
		} else {
			if (id_.equals(getId())) {
				result_.add(new ReferenceSite(this, RefType.Declaration));
			}
		}
	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {

		IGType t = type.computeIG(aContainer, aEE);
		IGOperation initialValue = value != null ? value.computeIGOperation(t, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null) : null;

		IGObject obj = null;

		OIDir oiDir = OIDir.NONE;
		switch (dir) {
		case BUFFER:
			oiDir = OIDir.BUFFER;
			break;
		case IN:
			oiDir = OIDir.IN;
			break;
		case INOUT:
			oiDir = OIDir.INOUT;
			break;
		case LINKAGE:
			oiDir = OIDir.LINKAGE;
			break;
		case OUT:
			oiDir = OIDir.OUT;
			break;
		}

		switch (kind) {
		case SIGNAL:
			obj = new IGObject(oiDir, initialValue, IGObjectCat.SIGNAL, t, getId(), getLocation(), aEE.getZDB());
			break;
		case CONSTANT:
			obj = new IGObject(oiDir, initialValue, IGObjectCat.CONSTANT, t, getId(), getLocation(), aEE.getZDB());
			break;
		case VARIABLE:
			obj = new IGObject(oiDir, initialValue, IGObjectCat.VARIABLE, t, getId(), getLocation(), aEE.getZDB());
			break;
		case FILE:
			obj = new IGObject(oiDir, initialValue, IGObjectCat.FILE, t, getId(), getLocation(), aEE.getZDB());
			break;

		}

		return obj;
	}

}
