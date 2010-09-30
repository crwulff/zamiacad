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
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGStaticValue;
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
public class TypeDefinitionFile extends TypeDefinition {

	private Name fTypeMark;

	public TypeDefinitionFile(Name aTypeMark, ASTObject aParent, long aLocation) {
		super(aParent, aLocation);
		setTypeMark(aTypeMark);
	}

	private void setTypeMark(Name aTypeMark) {
		fTypeMark = aTypeMark;
		fTypeMark.setParent(this);
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return fTypeMark;
	}

	public Name getTypeMark() {
		return fTypeMark;
	}

	@Override
	public IGType computeIG(IGContainer aContainer, IGElaborationEnv aEE) {
		IGType type = null;
		try {
			IGType elementType = fTypeMark.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			 type = new IGType(TypeCat.FILE, null, null, null, elementType, null, false, getLocation(), aEE.getZDB());

			// now declare the implicit operations

			/*
			 *  procedure read ( file f : file_type; value : out element_type ) ;
			 */

			IGSubProgram sub = new IGSubProgram(aContainer.store(), null, "READ", getLocation(), aEE.getZDB());

			sub.setBuiltin(IGBuiltin.READ);

			IGContainer container = sub.getContainer();

			IGObject intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, type, "F", getLocation(), aEE.getZDB());
			container.addInterface(intf);
			intf = new IGObject(OIDir.OUT, null, IGObjectCat.CONSTANT, elementType, "VALUE", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			sub.computeSignatures();

			sub.storeOrUpdate();
			container.storeOrUpdate();
			aContainer.add(sub);

			/*
			 * procedure write ( file f : file_type; value : in element_type ) ;
			 */

			sub = new IGSubProgram(aContainer.store(), null, "WRITE", getLocation(), aEE.getZDB());

			sub.setBuiltin(IGBuiltin.WRITE);

			container = sub.getContainer();

			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, type, "F", getLocation(), aEE.getZDB());
			container.addInterface(intf);
			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, elementType, "VALUE", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			sub.computeSignatures();

			sub.storeOrUpdate();
			container.storeOrUpdate();
			aContainer.add(sub);

			/*
			 * function endfile ( file f : file_type )  return boolean ; 
			 */

			sub = new IGSubProgram(aContainer.store(), aContainer.findBoolType(), "ENDFILE", getLocation(), aEE.getZDB());

			sub.setBuiltin(IGBuiltin.ENDFILE);

			container = sub.getContainer();

			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, type, "F", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			sub.computeSignatures();

			sub.storeOrUpdate();
			container.storeOrUpdate();
			aContainer.add(sub);

			/*
			 * procedure file_close ( file f : file_type ) ;
			 */

			sub = new IGSubProgram(aContainer.store(), null, "FILE_CLOSE", getLocation(), aEE.getZDB());

			sub.setBuiltin(IGBuiltin.WRITE);

			container = sub.getContainer();

			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, type, "F", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			sub.computeSignatures();

			sub.storeOrUpdate();
			container.storeOrUpdate();
			aContainer.add(sub);

			/*
			 * procedure file_open( file f : file_type; external_name: in string; open_kind: in file_open_kind := read_mode ) ;
			 */

			sub = new IGSubProgram(aContainer.store(), null, "FILE_OPEN", getLocation(), aEE.getZDB());

			sub.setBuiltin(IGBuiltin.WRITE);

			container = sub.getContainer();

			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, type, "F", getLocation(), aEE.getZDB());
			container.addInterface(intf);
			intf = new IGObject(OIDir.IN, null, IGObjectCat.CONSTANT, aContainer.findStringType(), "EXTERNAL_NAME", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			IGType okT = aContainer.findOpenKindType();
			IGStaticValue iv = okT.findEnumLiteral("READ_MODE");

			intf = new IGObject(OIDir.IN, iv, IGObjectCat.CONSTANT, okT, "FILE_OPEN_KIND", getLocation(), aEE.getZDB());
			container.addInterface(intf);

			sub.computeSignatures();

			sub.storeOrUpdate();
			container.storeOrUpdate();
			aContainer.add(sub);

		} catch (ZamiaException e) {
			reportError(e);
			type = IGType.createErrorType(aEE.getZDB());
		}

		return type;
	}

}