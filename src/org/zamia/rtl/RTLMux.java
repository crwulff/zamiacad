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
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;


/**
 * This is a simple multiplexer for decision points in RTL-style synthesis
 * 
 * @author guenter bartsch
 *
 */


@SuppressWarnings("serial")
public class RTLMux extends RTLModule {

	private RTLPort d1, d2, s, z;
	private ZILType type;

	public RTLMux (ZILType type_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		type = type_;

		try {
			d1 = createPort("D1", type, PortDir.IN, src_);
			d2 = createPort("D2", type, PortDir.IN, src_);
			s = createPort(RTLPort.s_str, ZILType.bit, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, type, PortDir.OUT, src_);
		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}
	
	@Override
	public String getClassName() {
		return "RTLMux";
	}

	public RTLPort getD1() {
		return d1;
	}

	public RTLPort getD2() {
		return d2;
	}

	public RTLPort getS() {
		return s;
	}

	public RTLPort getZ() {
		return z;
	}

}
