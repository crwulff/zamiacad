/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 9, 2008
 */
package org.zamia.zil;

import java.util.ArrayList;

import org.zamia.ComponentStub;
import org.zamia.ZamiaException;
import org.zamia.rtl.RTLComponent;
import org.zamia.rtl.RTLGraph;
import org.zamia.util.ZHash;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zil.synthesis.Binding;
import org.zamia.zil.synthesis.Bindings;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZILInstantiation extends ZILConcurrentStatement {

	class ActualGeneric {
		public String fId;
		public ZILValue fValue;
		public ActualGeneric(String aId, ZILValue aValue) {
			fId = aId;
			fValue = aValue;
		}
	}
	
	private DMUID fDUUID; // the architecture we're referencing
	private ArrayList <ActualGeneric> fActualGenerics;
	private String fSignature;
	private ArrayList<ZILTargetOperationDestination> fPortMap;
	private ArrayList<ZILInterfaceSignal> fPorts;

	public ZILInstantiation(DMUID aDUUID, String aId, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aId, aContainer, aSrc);
		
		fDUUID = aDUUID;
		
		fActualGenerics = new ArrayList<ActualGeneric>();
	}

	public DMUID getDUUID() {
		return fDUUID;
	}
	
	public String getSignature() {
		
		return fSignature;
	}

//	public ArrayList<ZILValue> getActualGenerics() {
//		
//		return fActualGenerics;
//	}

	public void addActualGeneric(String aId, ZILValue aValue) {
		
		fActualGenerics.add(new ActualGeneric(aId, aValue));
		
	}
	
	@Override
	public int hashCode() {
		
		String sig = getSignature();
		
		return sig.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ComponentStub))
			return false;
		
		ComponentStub stub = (ComponentStub) obj;
		
		String sig1 = getSignature();
		String sig2 = stub.getSignature();
		
		return sig1.equals(sig2);
	}

	public void computeSignature() {
		StringBuilder buf = new StringBuilder ();
		
		int n = fActualGenerics.size();
		for (int i = 0; i<n; i++) {
			
			ActualGeneric actual = fActualGenerics.get(i);
			
			buf.append("###");
			buf.append(actual.fId);
			buf.append(":=");
			buf.append(actual.fValue);
		}

		fSignature = fDUUID.getUID() + "_H_"+ZHash.encodeZ(buf.toString());
	}

	public void setPortAssociationList(ArrayList<ZILInterfaceSignal> aPorts, ArrayList<ZILTargetOperationDestination> aPortMap) {
		fPorts = aPorts;
		fPortMap = aPortMap;
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "instantiation id=%s, duuid=%s {", getId(), getDUUID());
		logger.debug(aIndent+1, "generic map");
		int n = fActualGenerics.size();
		for (int i = 0; i<n; i++) {
			ActualGeneric ag = fActualGenerics.get(i);
			logger.debug(aIndent+2,"%s := %s", ag.fId, ag.fValue);
		}
		logger.debug(aIndent+1, "port map");
		n = fPortMap.size();
		for (int i = 0; i<n; i++) {
			ZILTargetOperation pm = fPortMap.get(i);
			pm.dump(aIndent+2);
		}
		logger.debug(aIndent, "}", getId());
	}

	@Override
	public String toString() {
		return "Instantiation (duuid="+getDUUID()+")";
	}

	@Override
	public Bindings computeBindings(RTLCache aCache) throws ZamiaException {

		RTLGraph rtlg = aCache.getGraph();

		Bindings bindings = new Bindings();

		ComponentStub stub = new ComponentStub(fDUUID);
		
		if (fActualGenerics != null) {
			
			int n = fActualGenerics.size();
			for (int i = 0; i<n; i++) {
			
				ZILValue v = fActualGenerics.get(i).fValue;
				
				stub.addActualGeneric(v);
			}
		}
		
		stub.computeSignature();

		RTLComponent component = new RTLComponent(stub, rtlg, getId(), getSrc());

		// create components ports
		
		int n = fPorts.size();
		for (int i = 0; i<n; i++) {
			
			ZILInterfaceSignal intf = fPorts.get(i);
		
			intf.elaborateAsPort(component, aCache);
			
		}
		
		// now connect them
		
		n = fPortMap.size();
		for (int i = 0; i<n; i++) {
			
			ZILTargetOperationDestination tod = fPortMap.get(i);
			
			Binding b = tod.computeBinding(null, aCache);
			
			bindings.merge(b);
		}
		
		rtlg.add(component);

		return bindings;
	}

	@Override
	public void elaborate(RTLCache aCache) throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException ("Sorry, not implemented yet.");
	}

	@Override
	public boolean isBindingsProducer(RTLCache aCache) throws ZamiaException {
		return true;
	}

//	private void connect(RTLPort p_, FormalPart formalPart_, Operation actualPart_, OperationCache envCache_, Bindings bindings_, VariableBindings vbs_) throws ZamiaException {
//		SigType type = p_.getType();
//
//		RTLSignal sFormal = p_.getSignal();
//		RTLGraph rtlg = envCache_.getRTLGraph();
//		if (sFormal == null) {
//
//			// we need to create this signal via a resolver so the portName
//			// we're creating below (in case of a non-input port) can actually
//			// be resolved to this signal
//
//			Resolver resolver = actualPart_.getDesignUnit().getResolver(envCache_);
//
//			if (resolver == null) {
//				throw new ZamiaException("Internal error: failed to find resolver.", this);
//			}
//
//			sFormal = resolver.createSignal(rtlg.getUnnamedSignalId(), type, actualPart_, rtlg);
//			p_.setSignal(sFormal);
//		}
//
//		OperationName fn;
//		if (formalPart_ != null) {
//			fn = formalPart_.getName();
//		} else {
//			fn = new OperationName(id, this, getLineCol());
//		}
//
//		if (p_.getDirection() == PortDir.IN) {
//
//			Operation actualPart = actualPart_.resolveVariables(vbs_, null, envCache_);
//			actualPart.setParent(actualPart_.getParent());
//
//			TargetOperation target = fn.elaborateExtensionsAsTarget(actualPart, sFormal, envCache_, null, 0);
//			bindings_.bind(sFormal, target);
//
//		} else {
//
//			OperationName portName = fn.cloneName();
//			portName.setId(sFormal.getId());
//			portName.setParent(actualPart_.getParent());
//
//			if (!(actualPart_ instanceof OperationName))
//				throw new ZamiaException("Name expected here.", actualPart_, envCache_);
//
//			OperationName actualName = (OperationName) actualPart_;
//			actualName = (OperationName) actualName.resolveVariables(vbs_, null, envCache_);
//			actualName.setParent(actualPart_.getParent());
//
//			Bindings bindings = actualName.computeBindings(portName, null, envCache_, null);
//			bindings_.merge(bindings);
//		}
//	}

	
}
