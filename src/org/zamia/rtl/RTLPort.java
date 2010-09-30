/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.rtl;

import java.io.Serializable;

import org.zamia.zil.ZILType;
import org.zamia.zil.ZILValue;



/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class RTLPort implements Serializable {

	public enum PortDir {
		IN, OUT, INOUT, LINKAGE, BUFFER
	}

	public static final String z_str = "Z";
	public static final String ze_str = "ZE";
	public static final String s_str = "S";
	public static final String a_str = "A";
	public static final String d_str = "D";
	public static final String e_str = "E";
	public static final String q_str = "Q";
	public static final String cp_str = "Clk";
	public static final String b_str = "B";
	
	private RTLSignal signal;
	private RTLModule module;
	private PortDir dir;
	private String id;
	private ZILType type;
	private boolean driving;
	private ZILValue initialValue;

	public RTLPort(RTLModule module_, String id_, ZILType type_, PortDir direction_) {
		module = module_;
		dir = direction_;
		signal = null;
		id = id_;
		type = type_;
		if (dir == PortDir.OUT)
			driving = true;
		else
			driving = false;
	}

	public void setSignal(RTLSignal s_) {

		if (s_ != null)
			s_ = s_.getCurrent();
		
		if (signal == s_)
			return;

		if (signal != null)
			signal.removePortConn(this);

		if (s_ != null) {
			//System.out.println ("adding myself to signal "+s_+"'s conn
			// hashmap");
			s_.addPortConn(this);
		}
		signal = s_;
	}

	public String getId() {
		return id;
	}

	public RTLModule getModule() {
		return module;
	}

	public RTLSignal getSignal() {
		return signal;
	}

	public ZILType getType() {
		return type;
	}

	public void setDriving(boolean b) {
		driving = b;
	}

	public PortDir getDirection() {
		return dir;
	}

	public String toString () {
		return "RTLPort(id="+id+", module="+module+")";
	}

	public boolean isDriving() {
		return driving;
	}

	public String getPath() {
		return getModule().getPath()+"/"+getId();
	}

	public void setInitialValue(ZILValue aValue) {
		initialValue = aValue;
	}

	public ZILValue getInitialValue() {
		return initialValue;
	}
}
