/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 11, 2007
 */

package org.zamia.rtl;


import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;


/**
 * 
 * @author guenter bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLTargetRecordSel extends RTLModule {

	private RTLPort d, e, z, ze;
	private ZILType tIn, tOut;
	private String id;

	public RTLTargetRecordSel(ZILType tIn_, ZILType tOut_, String id_,
			RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		try {

			tIn = tIn_;
			tOut = tOut_;
			id = id_;
			d = createPort("D", tIn, PortDir.IN, src_);
			e = createPort("E", tIn, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, tOut, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, tOut, PortDir.OUT, src_);

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetRecordSel";
	}

	public RTLPort getD() {
		return d;
	}

	public RTLPort getE() {
		return e;
	}

	public RTLPort getZ() {
		return z;
	}

	public RTLPort getZE() {
		return ze;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(RTLModule module2) {
		
		if (!(module2 instanceof RTLTargetRecordSel))
			return false;
		
		RTLTargetRecordSel trs = (RTLTargetRecordSel) module2;
		if (!trs.getId().equals(getId()))
			return false;
		
		return super.equals(module2);
	}
}
