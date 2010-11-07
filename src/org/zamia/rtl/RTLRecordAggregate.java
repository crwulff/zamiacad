/* 
 * Copyright 2007-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.rtl;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILRecordField;
import org.zamia.zil.ZILTypeRecord;




/**
 * Represents an elaborated record aggregate in the RTL Graph
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class RTLRecordAggregate extends RTLModule {

	private ZILTypeRecord resType;
	private RTLPort z;
	private HashMapArray<ZILRecordField, RTLPort> inputs;
	
	public RTLRecordAggregate (ZILTypeRecord resType_, RTLGraph parent_, String instanceName_, VHDLNode src_) throws ZamiaException {
		super(parent_, instanceName_, src_);
		
		resType = resType_;

		int n = resType.getNumRecordFields();
		inputs = new HashMapArray<ZILRecordField, RTLPort>(n);
		for (int i = 0 ; i<n; i++) {
			ZILRecordField rf = resType.getRecordField(i);
			RTLPort p = createPort("["+i+"]", rf.type, PortDir.IN, src_);
			inputs.put(rf,p);
		}
		
		z = createPort("Z", resType, PortDir.OUT, src_);
	}
	
	@Override
	public String getClassName() {
		return "RecordAggregate";
	}

	public RTLPort getInput(int i_) {
		return inputs.get(i_);
	}
	
	public RTLPort getInput(ZILRecordField rf_) {
		return inputs.get(rf_);
	}
	
	public RTLPort getZ() {
		return z;
	}
	
}
