/* 
 * Copyright 2008,2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 22, 2008
 */
package org.zamia;

import java.io.Serializable;
import java.util.ArrayList;

import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.ZHash;
import org.zamia.vhdl.ast.DMUID;


/**
 * Specifies a component that should be instantiated
 * 
 * At the moment, this will always contain an architecture name
 * plus actual values for all generics
 * 
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class ComponentStub implements Serializable {

	private DMUID fDUUID; // the architecture we're referencing
	private ArrayList <IGStaticValue> fActualGenerics;
	private String fSignature;
	
	public ComponentStub(DMUID aDUUID) {
		
		fDUUID = aDUUID;
		
		fActualGenerics = new ArrayList<IGStaticValue>();
	}

	public DMUID getDUUID() {
		return fDUUID;
	}
	
	public String getSignature() {
		
		return fSignature;
	}

	public ArrayList<IGStaticValue> getActualGenerics() {
		
		return fActualGenerics;
	}

	public void addActualGeneric(IGStaticValue aValue) {
		
		fActualGenerics.add(aValue);
		
	}
	
	public String toString() {
	
		StringBuilder buf = new StringBuilder ();
		
		int n = fActualGenerics.size();
		for (int i = 0; i<n; i++) {
			
			IGStaticValue actual = fActualGenerics.get(i);
			
			buf.append("###");
			buf.append(actual);
		}

		return "ComponentStub: " + fDUUID.getUID() + "_H_"+buf.toString();
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
			
			IGStaticValue actual = fActualGenerics.get(i);
			
			buf.append("###");
			buf.append(actual);
		}

		fSignature = fDUUID.getUID() + "_H_"+ZHash.encodeZ(buf.toString());
	}

}


