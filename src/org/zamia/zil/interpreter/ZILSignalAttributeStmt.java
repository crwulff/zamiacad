/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 20, 2009
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.SignalChange;
import org.zamia.rtl.sim.SignalInfo;
import org.zamia.rtl.sim.SignalLogEntry;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILTypePhysical;
import org.zamia.zil.ZILValue;
import org.zamia.zil.ZILOperationSignalAttribute.SAOp;


/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class ZILSignalAttributeStmt extends ZILStmt {

	private SAOp fOperation;
	private RTLSignal fSignal;
	private long fTime;
	private RTLPort fPort;
	private ZILTypePhysical fTimeType;

	public ZILSignalAttributeStmt(SAOp aOperation, RTLSignal aSignal, long aTime, ZILTypePhysical aTimeType, VHDLNode aSrc) {
		super(aSrc);
		fOperation = aOperation;
		fSignal = aSignal;
		fTime = aTime;
		fTimeType = aTimeType;
	}

	
	@Override
	public ReturnStatus execute(Simulator aSim, ZILInterpreterRuntimeEnv aRuntime, ZILInterpreter aInterpreter) throws ZamiaException {
		
		switch (fOperation) {
		case DELAYED:
			SignalInfo si = aSim.getSignalInfoInternal(fSignal);
			
			if (si == null)
				throw new ZamiaException ("Internal error: Signal not traced!");
			
			SignalLogEntry entry = si.getEventEntry(aSim.getCurrentTime() - fTime);

			ZILValue v = entry.fValue != null ? entry.fValue : ZILValue.generateUValue(fSignal.getType(), null, null);
			
			aRuntime.push(v);
			
			break;
			
		case STABLE:
			SignalLogEntry lastEntry = aSim.getLastEntryInternal (fSignal.getCurrent());
			
			while (lastEntry != null && !lastEntry.fIsEvent) {
				lastEntry = lastEntry.fPrev;
			}
			
			boolean res = lastEntry == null || (aSim.getCurrentTime() - lastEntry.fTime) > fTime; 
			
			pushBool(res, aRuntime);
			break;
			
		case QUIET: 
			lastEntry = aSim.getLastEntryInternal (fSignal.getCurrent());
			
			res = lastEntry == null || (aSim.getCurrentTime() - lastEntry.fTime) > fTime; 
			
			pushBool(res, aRuntime);
			break;
			
		case TRANSACTION: 
		case ACTIVE:
			
			SignalChange sc = aSim.getSignalActivity(fSignal.getCurrent());
			
			res = sc != null ; 
			
			pushBool(res, aRuntime);
			break;

		case EVENT: 
			sc = aSim.getSignalActivity(fSignal.getCurrent());
			
			res = sc != null && sc.isEvent(); 
			
			pushBool(res, aRuntime);
			break;
			
		case LAST_EVENT: 
			lastEntry = aSim.getLastEntryInternal (fSignal.getCurrent());
			
			while (lastEntry != null && !lastEntry.fIsEvent) {
				lastEntry = lastEntry.fPrev;
			}

			long resT = lastEntry != null ? aSim.getCurrentTime() - lastEntry.fTime : Long.MAX_VALUE;
			
			aRuntime.push(new ZILValue (resT, fTimeType, null, null));
			
			break;

		case LAST_ACTIVE:
			lastEntry = aSim.getLastEntryInternal (fSignal.getCurrent());
			
			resT = lastEntry != null ? aSim.getCurrentTime() - lastEntry.fTime : Long.MAX_VALUE;
			
			aRuntime.push(new ZILValue (resT, fTimeType, null, null));
			
			break;
			
		case LAST_VALUE:
			lastEntry = aSim.getLastEntryInternal (fSignal.getCurrent());
			
			if (lastEntry.fPrev != null) {
				lastEntry = lastEntry.fPrev;
			}
			
			aRuntime.push(lastEntry.fValue);
			
			break;
			
		case DRIVING:
			
			pushBool(fPort.isDriving(), aRuntime);
			
			break;
			
		case DRIVING_VALUE:
			// FIXME: ???
			
			aRuntime.push(aSim.getValue(fSignal));
			break;
			
		default:
			throw new ZamiaException ("Sorry, not implemented yet.");
		}

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "SIGNAL ATTRIBUTE signal="+fSignal+", attr="+fOperation+", time="+fTime;
	}
	
	@Override
	public void wire(ZILInterpreter aInterpreter) throws ZamiaException {
		if (fSignal != null) {
			fSignal = fSignal.getCurrent();
			fPort = aInterpreter.connectToSignal(fSignal, getSource());
		}
	}

	@Override
	public void init(Simulator aSimulator) throws ZamiaException {
		if (fSignal != null) {

			fSignal = fSignal.getCurrent();

			switch (fOperation) {
			case DELAYED:
			case STABLE:
			case QUIET:
			case LAST_EVENT:
			case LAST_ACTIVE:
			case LAST_VALUE:
				aSimulator.traceInternal(fSignal);
				break;
			}
		}
	}
	
}
