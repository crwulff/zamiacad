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

	public TypeDefinitionFile(Name aTypeMark, VHDLNode aParent, long aLocation) {
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
	public VHDLNode getChild(int aIdx) {
		return fTypeMark;
	}

	public Name getTypeMark() {
		return fTypeMark;
	}

	@Override
	public IGType computeIG(final IGContainer aContainer, final IGElaborationEnv aEE) {
		
		try {
			IGType elementType = fTypeMark.computeIGAsType(aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

			final IGType type = new IGType(TypeCat.FILE, null, null, null, elementType, null, false, getLocation(), aEE.getZDB());

			// now declare the implicit operations

			class Helper {
				IGSubProgram sub;
				IGContainer container;
				Helper start(IGBuiltin id, boolean f, IGType ... returnType) throws ZamiaException {
					IGType rt = returnType.length == 0 ? null : returnType[0];
					sub = new IGSubProgram(aContainer.store(), rt, id.name(), getLocation(), aEE.getZDB());
					sub.setBuiltin(id);
					container = sub.getContainer();
					if (f)
						add(OIDir.IN, type, "F");
					return this;
				 }
				Helper fin(OIDir dir, IGType type, String name) throws ZamiaException {
					add(dir, type, name);
					fin();
					return this;
				 }
					public Helper add(OIDir dir, IGType type, String name) throws ZamiaException {
						add(dir, null, type, name);
						return this;
					}
					public Helper add(OIDir dir, IGStaticValue deflt, IGType type, String name) throws ZamiaException {
						container.addInterface(new IGObject(
								dir, deflt, IGObjectCat.CONSTANT, type, name, getLocation(), aEE.getZDB()));
						return this;
					}
					public Helper addOpenKind() throws ZamiaException {
						IGType okT = aContainer.findOpenKindType(); IGStaticValue iv = okT.findEnumLiteral("READ_MODE");
						add(OIDir.IN, iv, okT, "FILE_OPEN_KIND");
						return this;
					}
					public Helper fin() throws ZamiaException {
						sub.computeSignatures();
						sub.storeOrUpdate();
						sub.getContainer().storeOrUpdate();
						aContainer.add(sub);
						return this;
					}
			 }

			/*
			 *  procedure read ( file f : file_type; value : out element_type ) ;
			 */

			Helper h = new Helper();
			
			h.start(IGBuiltin.READ, true).fin(OIDir.OUT, elementType, "VALUE");

			/*
			 * procedure write ( file f : file_type; value : in element_type ) ;
			 */

			h.start(IGBuiltin.WRITE, true).fin(OIDir.IN, elementType, "VALUE");

			/*
			 * function endfile ( file f : file_type )  return boolean ; 
			 */

			h.start(IGBuiltin.ENDFILE, true, aContainer.findBoolType()).fin();

			
			/*
			 * procedure file_close ( file f : file_type ) ;
			 */

			h.start(IGBuiltin.FILE_CLOSE, true).fin();
			
			/*
			 * procedure file_open( file f : file_type; external_name: in string; open_kind: in file_open_kind := read_mode ) ;
			 */

			h.start(IGBuiltin.FILE_OPEN, true).add(OIDir.IN, aContainer.findStringType(), "EXTERNAL_NAME") 
				.addOpenKind().fin();
			
			/*
			 * procedure file_open( status: out file_open_status; file f : file_type; external_name: in string; open_kind: in file_open_kind := read_mode ) ;
			 */

			h.start(IGBuiltin.FILE_OPEN, false).add(OIDir.OUT, aContainer.findFileOpenStatusType(), "STATUS")
				.add(OIDir.IN, type, "F").add(OIDir.IN, aContainer.findStringType(), "EXTERNAL_NAME")
				.addOpenKind(); h.fin();

			/*
			 * Implicit procedure FLUSH (file F: TEXT) */
			h.start(IGBuiltin.FLUSH, true).fin();	
			
			return type;
		} catch (ZamiaException e) {
			reportError(e);
			return IGType.createErrorType(aEE.getZDB());
		}

		
	}
	
}