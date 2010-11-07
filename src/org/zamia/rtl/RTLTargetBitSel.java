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
public class RTLTargetBitSel extends RTLModule {

	private RTLPort d, e, z, ze;
	private ZILType tIn, tOut;
	private int offset;
	private int width;

	public RTLTargetBitSel(ZILType tIn_, ZILType tOut_, int offset_, int width_,
			RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		try {

			tIn = tIn_;
			tOut = tOut_;
			
			offset = offset_;
			width = width_;
			
			d = createPort("D", tIn, PortDir.IN, src_);
			e = createPort(RTLPort.e_str, tIn, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, tOut, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, tOut, PortDir.OUT, src_);

		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetBitSel";
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

	public int getOffset() {
		return offset;
	}

	public int getWidth() {
		return width;
	}
}
