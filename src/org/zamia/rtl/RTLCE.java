/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 4, 2008
 */
package org.zamia.rtl;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.ZILType;


/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLCE extends RTLModule {

	private RTLPort e, ze;
	private ZILType type;

	public RTLCE(ZILType t_, RTLGraph parent_, String instanceName_,
			VHDLNode src_) {
		super(parent_, instanceName_, src_);

		type = t_;
		try {
			e = createPort("E", ZILType.bit, PortDir.IN, src_);
			ze = createPort("ZE", type, PortDir.OUT, src_);

		} catch (ZamiaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getClassName() {
		return "RTLCE";
	}

	public RTLPort getE() {
		return e;
	}

	public RTLPort getZE() {
		return ze;
	}


}
