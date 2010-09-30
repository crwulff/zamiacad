/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.rtl.sim;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort;
import org.zamia.zil.ZILRecordField;
import org.zamia.zil.ZILValue;
import org.zamia.zil.ZILVariable;
import org.zamia.zil.interpreter.ZILInterpreterRuntimeEnv;


/**
 * assignment targets get ultimately turned into signal writer chains.
 * 
 * @author Guenter Bartsch
 * 
 */

public class PortVarWriter {

	public enum SNOp {
		SIGNAL, VAR, RECORD, ARRAYINDEX, ARRAYRANGE, VALUE
	};

	private SNOp fSNOp;

	private RTLPort fPort;
	private PortVarWriter fNext, fPrev;

	private ZILRecordField fRF;

	private int fIdx;

	private ZILVariable fVariable;

	private int fMin;

	private int fMax;

	private ZILValue fValue;

	public PortVarWriter(RTLPort aPort) {
		fPort = aPort;
		fSNOp = SNOp.SIGNAL;
	}

	public PortVarWriter(ZILVariable aVariable) {
		fVariable = aVariable;
		fSNOp = SNOp.VAR;
	}

	public PortVarWriter(ZILValue aValue) {
		fValue = aValue;
		fSNOp = SNOp.VALUE;
	}

	public PortVarWriter(PortVarWriter aPrev, ZILRecordField aRF) {
		fSNOp = SNOp.RECORD;
		fRF = aRF;
		aPrev.setNext(this);
		fPrev = aPrev;
	}

	public PortVarWriter(PortVarWriter aPrev, int aIdx) {
		fSNOp = SNOp.ARRAYINDEX;
		fIdx = aIdx;
		aPrev.setNext(this);
		fPrev = aPrev;
	}

	public PortVarWriter(PortVarWriter aPrev, int aMin, int aMax) {
		fMin = aMin;
		fMax = aMax;
		fSNOp = SNOp.ARRAYRANGE;
		aPrev.setNext(this);
		fPrev = aPrev;
	}

	public void setNext(PortVarWriter aSN) {
		fNext = aSN;
	}

	public void setValue(ZILValue aValue) {
		fValue = aValue;
	}

	public boolean isVariable() {
		return fSNOp == SNOp.VAR;
	}

	public void execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime) throws ZamiaException {
		if (fPrev != null) {
			fPrev.execute(aSim, aRuntime);
			return;
		}

		ZILValue value;

		switch (fSNOp) {

		case SIGNAL:
			value = aSim.getDelta(fPort);
			break;

		case VAR:
			value = aRuntime.getObjectValue(fVariable);
			break;

		case VALUE:
			value = fValue;
			break;

		default:
			throw new ZamiaException("PortVarWriter: Internal error. SIGNAL/VAR op expected, found " + fSNOp);
		}

		if (fNext != null) {
			executeInternal(value, aSim, aRuntime);
		} else {
			value.modifyValue(fValue);
		}
	}

	private void executeInternal(ZILValue aValue, Simulator aSim, ZILInterpreterRuntimeEnv aRuntime) throws ZamiaException {

		ZILValue value = aValue;

		switch (fSNOp) {
		case ARRAYINDEX:
			value = value.getValue(fIdx);
			break;
		case ARRAYRANGE:
			
			break;
		case RECORD:
			value = value.getValue(fRF);
			break;
		default:
			throw new ZamiaException("PortVarWriter: Internal error. ARRAYINDEX/ARRAYRANGE/RECORD op expected, found " + fSNOp);
		}

		if (fNext != null) {
			executeInternal(value, aSim, aRuntime);
		} else {
			value.modifyValue(fValue);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("PVW(");
		switch (fSNOp) {
		case SIGNAL:
			buf.append("port=" + fPort);
			break;
		case VAR:
			buf.append("var=" + fVariable);
			break;
		case ARRAYINDEX:
			buf.append("array idx=" + fIdx);
			break;
		case ARRAYRANGE:
			buf.append("array range=[" + fMin + ":" + fMax + "]");
			break;
		case RECORD:
			buf.append("record fild=" + fRF);
			break;
		}

		if (fNext != null) {
			buf.append(" => ");
			buf.append(fNext.toString());
		}

		if (fValue != null) {
			buf.append("; VALUE: " + fValue);
		}

		buf.append(")");
		return buf.toString();
	}

	public RTLPort getPort() throws ZamiaException {
		
		if (fSNOp != SNOp.SIGNAL) {
			throw new ZamiaException ("Simulator: Internal error: getPort() called on "+this);
		}
		
		return fPort;
	}

	public ZILValue getValue() {
		if (fNext != null)
			return fNext.getValue();
		return fValue;
	}

}
