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
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLTargetArraySel extends RTLModule {

	private RTLPort d, e, z, ze, s;

	private ZILType tIn, tOut, tSel;

	public RTLTargetArraySel(ZILType tIn_, ZILType tOut_, ZILType tSel_, RTLGraph parent_, String instanceName_, ASTObject src_) {
		super(parent_, instanceName_, src_);

		try {

			tIn = tIn_;
			tOut = tOut_;
			tSel = tSel_;
			d = createPort("D", tIn, PortDir.IN, src_);
			e = createPort(RTLPort.e_str, tIn, PortDir.IN, src_);
			z = createPort(RTLPort.z_str, tOut, PortDir.OUT, src_);
			ze = createPort(RTLPort.ze_str, tOut, PortDir.OUT, src_);
			s = createPort(RTLPort.s_str, tSel, PortDir.IN, src_);

		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}
	}

	@Override
	public String getClassName() {
		return "RTLTargetArraySel";
	}

	public RTLPort getD() {
		return d;
	}

	public RTLPort getE() {
		return e;
	}

	public RTLPort getS() {
		return s;
	}

	public RTLPort getZ() {
		return z;
	}

	public RTLPort getZE() {
		return ze;
	}
}
