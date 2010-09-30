/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on May 19, 2007
 */

package org.zamia.rtl;

import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.ASTObject;

/**
 * @author guenter bartsch
 */

@SuppressWarnings("serial")
public class RTLOutputPort extends RTLPortModule {

	public RTLOutputPort(RTLGraph parent_, RTLPort p, ASTObject src_) {
		super(parent_, p, src_);

		if (p.getDirection() != PortDir.OUT) {
			System.out
					.println("OutputPort: InternalCompiler error: OutputPort generated from "
							+ p);
			System.exit(1);
		}

		externalPort = p;
		internalPort = new RTLPort(this, RTLPort.z_str, p.getType(),PortDir.IN);
		add(internalPort);
	}

	public String toString() {
		return "OutPort " + instanceName;
	}

	@Override
	public String getClassName() {
		return "OutputPort";
	}

}
