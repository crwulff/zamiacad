/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.zil.interpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.VHDLNode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ZILInterpreter extends RTLModule implements Serializable {

	private transient ZILInterpreterRuntimeEnv fRuntime;
	private ZILInterpreterCode fCode;
	private HashMap<RTLSignal,RTLPort> fSignalMap;

	public static final String INTCOMP_NAME="intcomp";
	
	public String getClassName() {
		return INTCOMP_NAME;
	}

	public ZILInterpreter(RTLGraph aParent, VHDLNode aSrc) throws ZamiaException {
		super(aParent, null, aSrc);
		
		fSignalMap = new HashMap<RTLSignal,RTLPort>();
	}
	
	public ZILInterpreterRuntimeEnv getRuntime() {
		return fRuntime;
	}
	
	public RTLPort lookupSignalPort (RTLSignal aSignal) {
		return fSignalMap.get (aSignal);
	}
	
	public RTLPort connectToSignal (RTLSignal aSignal, VHDLNode aSrc) throws ZamiaException {
		
		RTLSignal signal = aSignal.getCurrent();
		
		RTLPort port = fSignalMap.get(signal);
		if (port == null) {
			port = createPort (signal.getId(), signal.getType(), PortDir.INOUT, aSrc);
			
			if (!port.getType().isCompatible(signal.getType()))
				throw new ZamiaException ("Type mismatch", aSrc);
			
			port.setSignal(signal);
			
			fSignalMap.put (signal, port);
		}
		return port;
	}
	

	public ZILInterpreterCode getCode() {
		return fCode;
	}
	
	public void reset(Simulator aSimulator) throws ZamiaException {
		fRuntime = new ZILInterpreterRuntimeEnv(fCode, this);
		for (Iterator<RTLPort> i = fSignalMap.values().iterator(); i.hasNext();) {
			RTLPort port = i.next();
			port.setDriving(false);
		}
		
		int n = fCode.getNumStmts();
		for (int i = 0; i<n; i++) {
			ZILStmt stmt = fCode.getStmt(i);
			stmt.init(aSimulator);
		}
	}


	public void setCode(ZILInterpreterCode aCode) {
		fCode = aCode;
	}


	public void wire() throws ZamiaException {
		int n = fCode.getNumStmts();
		for (int i = 0; i<n; i++) {
			ZILStmt stmt = fCode.getStmt(i);
			
			stmt.wire(this);
		}
	}



//	public static void setPort(InterpreterComponent gate_, RTLPort port_, char value_,
//			Simulator sim_) throws SimException, ZamiaException {
//
//		Port n = gate_.getNotifyPort();
//		SequentialCode code = gate_.getCode();
//		
//		// is this our notification port
//		if (port_ == n) {
//			
//			// oki, so a wait for statement has been completed.
//			
//			code.resume(gate_, sim_);
//			
//		} else {
//			sim_.setValue(port_, value_);
//			
//			// is this our wait until clock port?
//			Clock clk = gate_.getWaitUntilClock();
//			if (clk != null) {
//				Port p = gate_.lookupSignalPort(clk.getSignalBit());
//				if (p == port_) {
//					if ((clk.isRisingEdge() && (value_ == Value.BIT_1)) || (!clk.isRisingEdge() && (value_ == Value.BIT_0))) {
//						// bingo - that was the event we were waiting for
//						
//						gate_.setWaitUntilClock(null);
//						
//						code.resume(gate_, sim_);
//						
//					}
//				}				
//			}
//		}
//	}

}
