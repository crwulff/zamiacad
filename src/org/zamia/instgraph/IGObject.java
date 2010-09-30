/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.zdb.ZDB;


/**
 * Represents an VHDL object.
 * 
 * There are four VHDL object types:
 * 
 * Constant Signal Variable File
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public final class IGObject extends IGContainerItem {

	public enum OIDir {
		NONE, IN, OUT, INOUT, BUFFER, LINKAGE
	};

	public enum IGObjectCat {
		CONSTANT, SIGNAL, VARIABLE, FILE
	}

	private IGObjectCat fCat;

	private OIDir fDir;

	private IGOperation fInitialValue;

	private long fTypeDBID;

	private boolean fIsShared = false;

	public IGObject(OIDir aDir, IGOperation aInitialValue, IGObjectCat aCat, IGType aType, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);

//		if (aSrc == null) {
//			System.out.println ("foobar. bummer.");
//		}
		
		fCat = aCat;

		if (aType == null) {
			logger.error("Internal error: Creating a null=typed object. %s", aSrc);
		}

		fTypeDBID = save(aType);

		fDir = aDir;
		fInitialValue = aInitialValue;
	}

	public void setShared(boolean aIsShared) {
		fIsShared = aIsShared;
	}

	public IGObjectCat getCat() {
		return fCat;
	}

	public IGOperation getInitialValue() {
		return fInitialValue;
	}

	public IGType getType() {
		return (IGType) getZDB().load(fTypeDBID);
	}

	public OIDir getDirection() {
		return fDir;
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fInitialValue;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();
		if (fIsShared) {
			buf.append("SHARED ");
		}
		buf.append(fCat.name() + " " + getId() + " : " + getType());

//		if (fInitialValue != null) {
//			buf.append(" := " + fInitialValue);
//		}
		return buf.toString();
	}

	public void setInitialValue(IGOperation aIv) {
		fInitialValue = aIv;
	}
}
