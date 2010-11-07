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
public class RTLBitSel extends RTLModule {

	private RTLPort a, z;
	private ZILType inType;
	private int offset;
	private int width;
	private ZILType resType;

	public RTLBitSel (ZILType inType_, int offset_, int width_, ZILType resType_, RTLGraph parent_, String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		inType = inType_;
		offset = offset_;
		width = width_;
		resType = resType_;
		
		try {
			a = createPort(RTLPort.a_str, inType, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, resType, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLBitSel";
	}

	public RTLPort getA() {
		return a;
	}

	public RTLPort getZ() {
		return z;
	}

	public int getOffset() {
		return offset;
	}

	public int getWidth() {
		return width;
	}

}
