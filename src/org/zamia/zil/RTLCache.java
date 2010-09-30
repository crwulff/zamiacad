/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 28, 2009
 */
package org.zamia.zil;

import java.util.HashMap;

import org.zamia.ZamiaProject;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.VariableBinding;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class RTLCache {

	private RTLGraph fGraph;
	private HashMap<ZILTargetOperation, RTLSignalAE> toCache = new HashMap<ZILTargetOperation, RTLSignalAE>();
	private HashMap<ZILIObject, RTLSignal> fSignalCache = new HashMap<ZILIObject, RTLSignal>();
	private HashMap<ZILInterfaceSignal, RTLPort> fPortCache = new HashMap<ZILInterfaceSignal, RTLPort>();
	private HashMap<ZILSubProgram, ZILInterpreterCode> fCodeCache = new HashMap<ZILSubProgram, ZILInterpreterCode>();
	private HashMap<ZILVariable, VariableBinding> fVariableBindings = new HashMap<ZILVariable, VariableBinding>();
	private ZamiaProject fZPrj;
	
	public RTLCache (RTLGraph aGraph, ZamiaProject aZPrj) {
		fGraph = aGraph;
		fZPrj = aZPrj;
	}
	
	public RTLGraph getGraph() {
		return fGraph;
	}

	public void put(ZILTargetOperation aTargetOperation, RTLSignalAE aResult) {
		toCache.put(aTargetOperation, aResult);
	}
	
	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	public RTLSignalAE get(ZILTargetOperation aTargetOperation) {
		return toCache.get(aTargetOperation);
	}

	public RTLSignal getSignal(ZILIObject aOperation) {
		return fSignalCache.get(aOperation);
	}
	
	public void setSignal(ZILIObject aOperation, RTLSignal aSignal) {
		fSignalCache.put(aOperation, aSignal);
	}

	public RTLPort getPort(ZILInterfaceSignal aInterface) {
		return fPortCache.get(aInterface);
	}

	public void setPort(ZILInterfaceSignal aInterface, RTLPort aPort) {
		fPortCache.put(aInterface, aPort);
	}

	public ZILInterpreterCode getCode(ZILSubProgram aSubProgram) {
		return fCodeCache.get(aSubProgram);
	}
	
	public void setCode(ZILSubProgram aSubProgram, ZILInterpreterCode aCode) {
		fCodeCache.put(aSubProgram, aCode);
	}

	public void setVariableBinding(ZILVariable aVariable, VariableBinding aBinding) {
		fVariableBindings.put(aVariable, aBinding);
	}

	public VariableBinding getVariableBinding(ZILVariable aVariable) {
		return fVariableBindings.get(aVariable);
	}

}
