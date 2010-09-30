/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 24, 2008
 */
package org.zamia.zil.interpreter;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.Simulator;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeDiscrete;
import org.zamia.zil.ZILValue;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class ZILConcatStmt extends ZILStmt {

	private ZILType type;

	public ZILConcatStmt(ZILType type_, ASTObject src_) {
		super(src_);
		type = type_;
	}

	@Override
	public ReturnStatus execute(Simulator sim_, ZILInterpreterRuntimeEnv runtime_, ZILInterpreter comp) throws ZamiaException {

		ZILStackFrame sf1 = runtime_.pop();
		ZILValue v1 = sf1.getValue(sim_);
		ZILType t1 = v1.getType();

		ZILStackFrame sf2 = runtime_.pop();
		ZILValue v2 = sf2.getValue(sim_);
		ZILType t2 = v2.getType();

		ZILTypeArray sta = (ZILTypeArray) type;
		ZILTypeDiscrete idxType = sta.getIndexType();
//		boolean ascending = idxType != null ? idxType.isAscending() : true;
//
//		ZILValue res = new ZILValue(sta, null, null);

		// FIXME : finish
		throw new ZamiaException ("Sorry, this code is not finished yet.");
		
//		if (!ascending) {
//
//			if (t1 instanceof ZILTypeArray) {
//				int n = v1.getNumValues();
//				for (int i = 0; i < n; i++) {
//					// FIXME: array idx range
//					res.setValue(i, v1.getValue(i));
//				}
//			} else {
//				res.addValue(v1);
//			}
//
//			if (t2 instanceof ZILTypeArray) {
//				int n = v2.getNumValues();
//				for (int i = 0; i < n; i++) {
//					// FIXME: array idx range
//					res.setValue(i, v2.getValue(i));
//				}
//			} else {
//				res.setValue(v2);
//			}
//
//		} else {
//
//			if (t2 instanceof ZILTypeArray) {
//				int n = v2.getNumValues();
//				for (int i = 0; i < n; i++) {
//					// FIXME: array idx range
//					res.setValue(i, v2.getValue(i));
//				}
//			} else {
//				res.addValue(v2);
//			}
//
//			if (t1 instanceof ZILTypeArray) {
//				int n = v1.getNumValues();
//				for (int i = 0; i < n; i++) {
//					// FIXME: array idx range
//					res.addValue(i, v1.getValue(i));
//				}
//			} else {
//				res.addValue(v1);
//			}
//
//		}
//
//		runtime_.push(res);
//
//		return false;
	}

	@Override
	public String toString() {
		return "CONCAT";
	}
}
