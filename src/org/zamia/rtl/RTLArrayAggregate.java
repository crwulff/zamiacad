/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.rtl;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeDiscrete;


/**
 * Represents an elaborated array aggregate in the RTL Graph
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class RTLArrayAggregate extends RTLModule {

	private ZILTypeArray resType;
	private RTLPort z;
	private ArrayList<RTLPort> inputs;
	
	public RTLArrayAggregate (ZILTypeArray resType_, RTLGraph parent_, String instanceName_, VHDLNode src_) throws ZamiaException {
		super(parent_, instanceName_, src_);
		
		resType = resType_;

		ZILTypeDiscrete idxType = resType.getIndexType();
		ZILType elementType = resType.getElementType();
		int cardinality = (int) idxType.getCardinality();
		inputs = new ArrayList<RTLPort>(cardinality);
		for (int i = 0; i<cardinality; i++) {
			RTLPort p = createPort("["+i+"]", elementType, PortDir.IN, src_);
			inputs.add(p);
		}
		
		z = createPort(RTLPort.z_str, resType, PortDir.OUT, src_);
	}
	
	@Override
	public String getClassName() {
		return "Aggregate";
	}

	public RTLPort getInput(int i_) {
		return inputs.get(i_);
	}
	
	public RTLPort getZ() {
		return z;
	}
	
}
