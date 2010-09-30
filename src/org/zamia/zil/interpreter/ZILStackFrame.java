/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.zil.interpreter;

import java.io.Serializable;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.PortVarWriter;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILFileObject;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;
import org.zamia.zil.ZILVariable;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILStackFrame implements Serializable {

	private ZILVariable fVariable;

	private ZILValue fValue;

	private ZILFileObject fFileObject;

	private ZILType fType;
	
	private PortVarWriter fSW;

	public ZILValue getLiteral() {
		return fValue;
	}

	public ZILStackFrame(ZILValue aValue) {
		fValue = aValue;
	}

	public ZILStackFrame(ZILVariable aVariable) {
		fVariable = aVariable;
	}

	public ZILStackFrame(ZILFileObject aFileObject) {
		fFileObject = aFileObject;
	}

	public ZILStackFrame(ZILType aType) {
		fType = aType;
	}

	public ZILStackFrame(PortVarWriter aSw) {
		fSW = aSw;
	}

	public ZILValue getValue(Simulator aSimulator) throws ZamiaException {

		if (fValue != null) {
			return fValue;
		} else if (fVariable != null) {
			// FIXME: implement
			throw new ZamiaException("StackFrame.getValue(): Sorry, variables not implemented yet.");
		}
		throw new ZamiaException("StackFrame.getValue(): Internal error - empty.");
	}

	public boolean getBool(Simulator aSimulator) throws ZamiaException {
		
		ZILValue v = getValue(aSimulator);

		return v.getBit() == ZILValue.BIT_1;
	}

	public int getInt(Simulator aSimulator) throws ZamiaException {
		ZILValue v = getValue(aSimulator);

		return v.getInt((ASTObject) null);
	}
	
	public ZILType getType() {
		return fType;
	}
	
	@Override
	public String toString () {
		if (fFileObject != null)
			return "SF File Object "+fFileObject;
		if (fType != null)
			return "SF Type "+fType;
		if (fValue != null)
			return "SF Value "+fValue;
		if ( fVariable != null)
			return "SF Variable "+fVariable;
		if (fSW != null)
			return "SF SignalWriter " + fSW;
		return "SF VOID ?";
	}

	public PortVarWriter getSignalVarWriter() {
		return fSW;
	}
}
