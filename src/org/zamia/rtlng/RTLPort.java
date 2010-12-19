/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import org.zamia.SourceLocation;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLPort extends RTLItem {

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

	private RTLSignal fSignal;

	private RTLNode fNode;

	private PortDir fDir;

	private String fId;

	private RTLType fType;

	private RTLValue fInitialValue;

	public RTLPort(RTLNode aNode, String aId, RTLType aType, PortDir aDir, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);

		fNode = aNode;
		fDir = aDir;
		fSignal = null;
		fId = aId;
		fType = aType;
	}

	public void setSignal(RTLSignal aSignal) {

		if (aSignal != null)
			aSignal = aSignal.getCurrent();

		if (fSignal == aSignal)
			return;

		if (fSignal != null)
			fSignal.removePortConn(this);

		if (aSignal != null) {
			//System.out.println ("adding myself to signal "+s_+"'s conn
			// hashmap");
			aSignal.addPortConn(this);
		}
		fSignal = aSignal;
	}

	public String getId() {
		return fId;
	}

	public RTLNode getNode() {
		return fNode;
	}

	public RTLSignal getSignal() {
		return fSignal;
	}

	public RTLType getType() {
		return fType;
	}

	public PortDir getDirection() {
		return fDir;
	}

	public String toString() {
		return "RTLPort(id=" + fId + ", module=" + fNode + ")";
	}

	public void setInitialValue(RTLValue aValue) {
		fInitialValue = aValue;
	}

	public RTLValue getInitialValue() {
		return fInitialValue;
	}

}
