/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILInterfaceSignal extends ZILSignal implements ZILInterface {

	private PortDir fDir;

	public ZILInterfaceSignal(String aId, ZILType aType, PortDir aDir, ZILValue aValue, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, aType, aContainer, aSrc);
		fDir = aDir;
		
		setInitialValue(aValue);
	}

	public PortDir getDir() {
		return fDir;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "%s", toString());
	}

	@Override
	public String toString() {
		return "Interface (id=" + getId() + ", type=" + getType() + ", dir=" + fDir + ", value=" + fValue + ")";
	}

	public RTLPort elaborateAsPort(RTLModule aModule, RTLCache aCache) throws ZamiaException {
		
		RTLPort p = aCache.getPort(this);
		if (p != null) {
			return p;
		}
		
		p = aModule.createPort(getId(), getType(), fDir, getSrc());
		
		aCache.setPort(this, p);
		
		if (fValue != null) {
			p.setInitialValue(fValue);
		}
		
		return p;
	}

	@Override
	public RTLSignal elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		
		RTLGraph rtlg = aCache.getGraph();

		RTLPort p = elaborateAsPort(rtlg, aCache);
		
		RTLSignal s = p.getSignal();
		if (s == null) {
			s = rtlg.createUnnamedSignal(p.getType(), getSrc());
			p.setSignal(s);
		}
		
		return s;
	}

	public void elaborateAsPortModule(RTLGraph aRtlg, RTLCache aCache) throws ZamiaException {
		RTLPort p = aCache.getPort(this);
		if (p != null) {
			return ;
		}
		
		p = aRtlg.createPort(getId(), getType(), fDir, getSrc());
		
		aCache.setPort(this, aRtlg.getPortModule(p).getInternalPort());
		
		if (fValue != null) {
			p.setInitialValue(fValue);
		}
	}

	public ZILValue getValue() {
		return fValue;
	}

//	public void inline(VariableRemapping vr_, Operation actual_, SequenceOfStatements sos_, OperationCache cache_) throws ZamiaException {
//
//		RTLGraph rtlg = cache_.getRTLGraph();
//
//		SigType t = getType().elaborate(cache_, null);
//		SigType t2 = actual_.getType(cache_, t, vr_, false);
//
//		if (!t.isCompatible(t2))
//			throw new ZamiaException("Type mismatch: " + t + " vs " + t2, actual_, cache_);
//
//		if (isSignal()) {
//
//			// create temporary signal, connect it
//
//			String signalId = vr_.remapSignal("par", getId(), t2, rtlg);
//
//			Target target = new Target(new OperationName(signalId, null, location), null, location);
//
//			if (sos_ != null) {
//
//				Waveform wv = new Waveform(new WaveformElement(actual_, null, null, actual_.getLineCol()), null, actual_.getLineCol());
//
//				SequentialSignalAssignment sva = new SequentialSignalAssignment(target, wv, 0, sos_, location);
//				sos_.add(sva);
//			}
//
//			//	// create a constant holding the variable value for subsequent type
//			//	// elaborations
//			//	Value c = actual_.getConstant(cache_, t2, null, null);
//			//	if (c != null) {
//			//		vr_.addConstant(getId(), c);
//			//	}
//
//		} else {
//			// create temporary variable, assign value
//
//			String varId = vr_.remap("par", getId(), t2, location);
//
//			Target target = new Target(new OperationName(varId, null, location), null, location);
//
//			if (sos_ != null) {
//				SequentialVariableAssignment sva = new SequentialVariableAssignment(target, actual_, sos_, location);
//				sos_.add(sva);
//			}
//
//			// create a constant holding the variable value for subsequent type
//			// elaborations
//			Value c = actual_.getConstant(cache_, t2, null, false);
//			if (c != null) {
//				vr_.addConstant(getId(), c);
//			}
//		}
//	}

	public boolean isValidTarget() {
		return fDir != PortDir.IN;
	}

	public void generateCreationCode(ZILInterpreterCode aCode) {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

}
