/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 19, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGNewObjectStmt;
import org.zamia.vhdl.ast.AssociationList;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSubProgram extends IGContainerItem {

	public enum IGBuiltin {
		NOW, DEALLOCATE, ENDFILE, READLINE, READ, WRITE, WRITELINE, FILE_CLOSE, FILE_OPEN, BOOL_AND, BOOL_OR, BOOL_NAND, BOOL_NOR, BOOL_XOR, BOOL_XNOR, BOOL_NOT, BIT_AND, BIT_OR, BIT_NAND, BIT_NOR, BIT_XOR, BIT_XNOR, BIT_NOT, INT_MUL, INT_POWER, INT_DIV, INT_ADD, INT_MINUS, INT_NEG, INT_POS, INT_ABS, INT_REM, INT_MOD, REAL_MUL, REAL_POWER, REALINT_POWER, REAL_DIV, REAL_ADD, REAL_MINUS, REAL_NEG, REAL_POS, REAL_ABS, TIME_DIVINT, STRING_EQUALS, STRING_NEQUALS, STRING_LESS, STRING_LESSEQ, STRING_GREATER, STRING_GREATEREQ, STRING_CONCAT, BITVECTOR_AND, BITVECTOR_OR, BITVECTOR_NAND, BITVECTOR_NOR, BITVECTOR_XOR, BITVECTOR_XNOR, BITVECTOR_NOT, BITVECTOR_SLL, BITVECTOR_SRL, BITVECTOR_SLA, BITVECTOR_SRA, BITVECTOR_ROL, BITVECTOR_ROR, BITVECTOR_EQUALS, BITVECTOR_NEQUALS, BITVECTOR_LESS, BITVECTOR_LESSEQ, BITVECTOR_GREATER, BITVECTOR_GREATEREQ, BITVECTOR_CONCAT, SCALAR_EQUALS, SCALAR_NEQUALS, SCALAR_LESS, SCALAR_LESSEQ, SCALAR_GREATER, SCALAR_GREATEREQ, ARRAY_EQUALS, ARRAY_NEQUALS, ARRAY_LESS, ARRAY_LESSEQ, ARRAY_GREATER, ARRAY_GREATEREQ, ARRAY_AND, ARRAY_OR, ARRAY_NAND, ARRAY_NOR, ARRAY_XOR, ARRAY_XNOR, ARRAY_NOT, RECORD_EQUALS, RECORD_NEQUALS, ARRAY_CONCATAA, ARRAY_CONCATAE, ARRAY_CONCATEA, ARRAY_CONCATEE
	};

	public static final String fBuiltinNames[] = { "NOW", "DEALLOCATE", "ENDFILE", "READLINE", "READ", "WRITE", "WRITELINE", "FILE_CLOSE", "FILE_OPEN" };

	public static HashMap<String, IGBuiltin> fBuiltins = new HashMap<String, IGBuiltin>();

	static {
		int n = fBuiltinNames.length;
		IGBuiltin[] v = IGBuiltin.values();
		for (int i = 0; i < n; i++) {
			fBuiltins.put(fBuiltinNames[i], v[i]);
		}
	}

	public static IGBuiltin getBuiltin(String aId) {
		return fBuiltins.get(aId);
	}

	private long fContainerDBID;

	private long fReturnTypeDBID;

	private IGSequenceOfStatements fCode;

	private IGBuiltin fBuiltin;

	private IGInterpreterCode fInterpreterCode;

	private String fSignature;

	private String fSignature2;

	public IGSubProgram(long aParentContainerId, IGType aReturnType, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);

		if (aReturnType != null) {
			fReturnTypeDBID = save(aReturnType);
		} else {
			fReturnTypeDBID = 0;
		}

		IGContainer container = new IGContainer(aParentContainerId, aSrc, aZDB);
		container.setReturnType(aReturnType);
		fContainerDBID = aZDB.store(container);
	}

	public IGContainer getContainer() {
		return (IGContainer) getZDB().load(fContainerDBID);
	}

	public IGType getType() {
		if (fReturnTypeDBID == 0)
			return null;
		return (IGType) getZDB().load(fReturnTypeDBID);
	}

	public void setCode(IGSequenceOfStatements aCode) throws ZamiaException {
		fCode = aCode;

		fInterpreterCode = new IGInterpreterCode(getId(), computeSourceLocation());

		// create and init local items

		IGContainer subContainer = getContainer();

		int n = subContainer.getNumLocalItems();
		for (int i = 0; i < n; i++) {

			IGContainerItem item = subContainer.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject obj = (IGObject) item;
			if (obj.getDirection() != OIDir.NONE) {
				continue;
			}
			fInterpreterCode.add(new IGNewObjectStmt(obj, computeSourceLocation(), getZDB()));
		}

		fCode.generateCode(fInterpreterCode);

	}

	public IGSequenceOfStatements getCode() {
		return fCode;
	}

	public void setBuiltin(IGBuiltin aBuiltin) {
		fBuiltin = aBuiltin;
	}

	public IGBuiltin getBuiltin() {
		return fBuiltin;
	}

	public IGInterpreterCode getInterpreterCode() {
		return fInterpreterCode;
	}

	@Override
	public IGItem getChild(int aIdx) {
		if (aIdx == 0)
			return getContainer();
		return fCode;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		IGType rt = getReturnType();
		if (rt != null) {
			buf.append("FUNCTION ");
		} else {
			buf.append("PROCEDURE ");
		}
		buf.append(getId() + "(");

		IGContainer container = getContainer();
		int nInterf = container.getNumInterfaces();
		for (int i = 0; i < nInterf; i++) {
			IGObject interf = container.getInterface(i);

			buf.append(interf);
			if (i < nInterf - 1) {
				buf.append(", ");
			}
		}

		buf.append(") ");
		if (rt != null) {
			buf.append("RETURN " + rt);
		}
		return buf.toString();
	}

	public boolean isFunction() {
		return fReturnTypeDBID != 0;
	}

	public void setContainer(IGContainer aContainer) {
		fContainerDBID = aContainer.store();
	}

	public String getSignature2() {
		if (fSignature2 == null) {
			computeSignatures();
		}
		return fSignature2;
	}

	public void computeSignatures() {

		StringBuilder buf = new StringBuilder(getId() + " ");

		IGType rt = getReturnType();
		if (rt == null) {
			buf.append(" null ");
		} else {
			buf.append(rt.computeSignature());
		}

		IGContainer container = getContainer();
		int nInterf = container.getNumInterfaces();
		for (int i = 0; i < nInterf; i++) {
			IGObject interf = container.getInterface(i);
			buf.append(interf.getId() + ":");
			IGType t = interf.getType();
			buf.append(t.computeSignature());
			if (i < nInterf - 1) {
				buf.append(", ");
			}
		}

		fSignature = buf.toString();

		buf = new StringBuilder(getId() + " ");

		rt = getReturnType();
		if (rt == null) {
			buf.append(" null ");
		} else {
			buf.append(rt.getOriginalType().store());
		}

		container = getContainer();
		nInterf = container.getNumInterfaces();
		for (int i = 0; i < nInterf; i++) {
			IGObject interf = container.getInterface(i);

			IGType t = interf.getType();
			buf.append("||");
			buf.append(t.getOriginalType().store());
		}

		fSignature2 = buf.toString();
	}

	public String getSignature() {
		if (fSignature == null) {
			computeSignatures();
		}
		return fSignature;
	}

	public boolean matches(IGSubProgram aSub) {
		String s1 = getSignature();
		String s2 = aSub.getSignature();
		return s1.equals(s2);
	}

	public IGType getReturnType() {
		return (IGType) getZDB().load(fReturnTypeDBID);
	}

	public boolean conforms(IGSubProgram aSub2) throws ZamiaException {
		IGContainer container2 = aSub2.getContainer();
		IGContainer container = getContainer();
		int m2 = container2.getNumInterfaces();
		int m1 = container.getNumInterfaces();

		if (m1 != m2) {
			return false;
		}

		for (int j = 0; j < m1; j++) {

			IGObject iface1 = container.getInterface(j);
			IGObject iface2 = container2.getInterface(j);

			String id1 = iface1.getId();
			String id2 = iface2.getId();

			IGType t1 = iface1.getType();
			IGType t2 = iface2.getType();

			if (!t1.conforms(t2) || !id1.equals(id2)) {
				return false;
			}
		}

		IGType rt1 = container.getReturnType();
		IGType rt2 = container2.getReturnType();
		if (rt1 != null) {
			return rt1.conforms(rt2);
		} else {
			return rt2 == null;
		}
	}

	public IGOperationInvokeSubprogram generateInvocation(AssociationList aAL, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, SourceLocation aLocation,
														  ErrorReport aReport) throws ZamiaException {
		return generateInvocation(aAL, aContainer, aEE, aCache, aLocation, aLocation, aReport);
	}

	public IGOperationInvokeSubprogram generateInvocation(AssociationList aAL, IGContainer aContainer, IGElaborationEnv aEE, IGOperationCache aCache, SourceLocation aLocation, SourceLocation aOpLocation,
														  ErrorReport aReport) throws ZamiaException {

		IGContainer formalScope = getContainer();
		IGElaborationEnv formalEE = new IGElaborationEnv(aEE.getZamiaProject());
		IGOperationCache formalCache = new IGOperationCache();

		ArrayList<IGObject> interfaces = formalScope.getInterfaces();

		IGMappings mappings = aAL.map(formalScope, formalEE, formalCache, aContainer, aEE, aCache, interfaces, false, aReport, false);

		if (mappings != null && !mappings.isFailed()) {
			return new IGOperationInvokeSubprogram(mappings, this, aLocation, aOpLocation, aEE.getZDB());
		}

		//aReport.append("Mapping failed for " + this + " from " + computeSourceLocation(), aLocation);

		return null;
	}

}
