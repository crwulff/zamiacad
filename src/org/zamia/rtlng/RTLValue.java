/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class RTLValue extends RTLItem {

	public enum BitValue {
		BV_U, BV_X, BV_0, BV_1, BV_Z
	};

	private final BitValue fBit;

	private final RTLType fType;

	public RTLValue(RTLValueBuilder aBuilder) {
		super(aBuilder.getLocation(), aBuilder.getZDB());

		fType = aBuilder.getType();
		fBit = aBuilder.getBit();
	}

	public RTLType getType() {
		return fType;
	}

	public BitValue getBit() {
		return fBit;
	}

	public RTLValue getRecordFieldValue(String aId) {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented.");
	}

	public RTLValue getValue(int aI) {
		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented.");
	}

	@Override
	public String toString() {
		switch (fType.getCat()) {
		case BIT:
			switch (fBit) {
			case BV_0:
				return "'0'";
			case BV_1:
				return "'1'";
			case BV_U:
				return "'U'";
			case BV_X:
				return "'X'";
			}
			return fBit.toString();
		case ARRAY:
			// FIXME: implement
			return "ARRAY VALUE";
		case RECORD:
			// FIXME: implement
			return "RECORD VALUE";
		}
		return "???";
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42;
	}

	@Override
	public boolean equals(Object aObj) {

		if (!(aObj instanceof RTLValue))
			return false;

		if (this == aObj)
			return true;

		RTLValue v = (RTLValue) aObj;

		RTLType t = v.getType();
		if (!getType().isCompatible(t))
			return false;

		switch (t.getCat()) {
		case BIT:
			return fBit == v.getBit();
		}

		// FIXME: implement
		throw new RuntimeException("Sorry, not implemented yet.");
	}

}
