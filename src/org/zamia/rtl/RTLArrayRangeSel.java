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
 * An elaboratet, constant array range selector
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class RTLArrayRangeSel extends RTLModule {

	private RTLPort a, z;

	private ZILType inType;

	private ZILType resType;

	private boolean ascending;

	private int right;

	private int left;

	public RTLArrayRangeSel(ZILType inType_, int left_, int right_,
			boolean ascending_, ZILType resType_, RTLGraph parent_,
			String instanceName_, VHDLNode src_) {
		super(parent_, instanceName_, src_);

		inType = inType_;
		resType = resType_;
		left = left_;
		right = right_;
		ascending = ascending_;

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
		return "RTLArrayRangeSel";
	}

	public RTLPort getA() {
		return a;
	}

	public RTLPort getZ() {
		return z;
	}

	public void dissolve() throws ZamiaException {
		// FIXME: implement
		throw new ZamiaException(
				"Internal error: Sorry, dissolve not implemented yet.");
	}

	public boolean isAscending() {
		return ascending;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

}
