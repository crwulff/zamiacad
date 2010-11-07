/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.PortVarWriter;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILRecordField;
import org.zamia.zil.ZILValue;
import org.zamia.zil.ZILVariable;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class ZILPushRefStmt extends ZILStmt {

	public enum PushOp {
		SIGNAL, VAR, RECORD, ARRAYINDEX, ARRAYRANGE, VALUE
	};

	private PushOp fPushOp;

	private boolean fInertial;

	private boolean fHaveDelay;

	private boolean fHaveReject;

	private RTLSignal fSignal;

	private RTLPort fPort;

	private ZILRecordField fRF;

	private ZILVariable fVariable;

	private ZILValue fValue;

	/**
	 * push ref signal
	 * 
	 * @param aSignal
	 * @param aInertial
	 * @param aHaveDelay
	 * @param aHaveReject
	 * @param aSrc
	 */
	public ZILPushRefStmt(RTLSignal aSignal, boolean aInertial, boolean aHaveDelay, boolean aHaveReject, VHDLNode aSrc) {
		super(aSrc);

		fPushOp = PushOp.SIGNAL;

		fSignal = aSignal;
		fInertial = aInertial;
		fHaveDelay = aHaveDelay;
		fHaveReject = aHaveReject;
	}

	public ZILPushRefStmt(ZILVariable aVariable, VHDLNode aSrc) {
		super(aSrc);

		fPushOp = PushOp.VAR;

		fVariable = aVariable;
	}

	public ZILPushRefStmt(ZILValue aValue, VHDLNode aSrc) {
		super(aSrc);
		fPushOp = PushOp.VALUE;

		fValue = aValue;
	}

	/**
	 * push record field ref
	 * 
	 * @param aId
	 * @param aSrc
	 */

	public ZILPushRefStmt(ZILRecordField aRF, VHDLNode aSrc) {
		super(aSrc);

		fPushOp = PushOp.RECORD;

		fRF = aRF;
	}

	/**
	 * push ref array index
	 * 
	 * @param aSrc
	 */
	public ZILPushRefStmt(boolean aHaveRange, VHDLNode aSrc) {
		super(aSrc);

		fPushOp = aHaveRange ? PushOp.ARRAYRANGE : PushOp.ARRAYINDEX;
	}

	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		switch (fPushOp) {
		case SIGNAL:

			long delay = 0;
			if (fHaveDelay) {
				ZILValue zd = aRuntime.pop().getLiteral();
				delay = zd.getReal(getSource()).longValue();
			}

			long reject = delay;
			if (fHaveReject) {
				ZILValue zr = aRuntime.pop().getLiteral();
				reject = zr.getReal(getSource()).longValue();
			}

			PortVarWriter sw = new PortVarWriter(fPort);

			aRuntime.push(sw);

			aSim.shedule(sw, fInertial, delay, reject, aRuntime);

			break;

		case VAR:
			sw = new PortVarWriter(fVariable);

			aRuntime.push(sw);

			break;

		case VALUE:
			sw = new PortVarWriter(fValue);

			aRuntime.push(sw);

			break;

		case RECORD:

			ZILStackFrame sf = aRuntime.pop();

			PortVarWriter svw = sf.getSignalVarWriter();

			PortVarWriter svw2 = new PortVarWriter(svw, fRF);

			aRuntime.push(svw2);

			break;

		case ARRAYINDEX:

			int idx = aRuntime.popInt(aSim);

			sf = aRuntime.pop();

			svw = sf.getSignalVarWriter();

			svw2 = new PortVarWriter(svw, idx);

			aRuntime.push(svw2);

			break;

		case ARRAYRANGE:
			int iMax = aRuntime.popInt(aSim);
			int iMin = aRuntime.popInt(aSim);

			sf = aRuntime.pop();

			svw = sf.getSignalVarWriter();

			svw2 = new PortVarWriter(svw, iMin, iMax);

			aRuntime.push(svw2);

			break;

		default:
			throw new ZamiaException("Unknown op : " + fPushOp);
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder("PUSH REF ");

		switch (fPushOp) {
		case SIGNAL:
			buf.append("SIGNAL ");
			buf.append(fSignal.toString());
			break;
		case VAR:
			buf.append("VAR ");
			buf.append(fVariable.toString());
			break;
		case RECORD:
			buf.append("RECORD FIELD " + fRF);
			break;
		case ARRAYINDEX:
			buf.append("ARRAY INDEX");
			break;
		case ARRAYRANGE:
			buf.append("ARRAY RANGE");
			break;
		case VALUE:
			buf.append("VALUE ");
			buf.append(fValue.toString());
			break;
		}

		return buf.toString();
	}

	@Override
	public void wire(ZILInterpreter aInterpreter) throws ZamiaException {
		if (fSignal != null) {
			fPort = aInterpreter.connectToSignal(fSignal, getSource());
		}
	}

}
