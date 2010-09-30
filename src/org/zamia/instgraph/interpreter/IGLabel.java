/*
 * Copyright 2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 27.06.2004
 */
package org.zamia.instgraph.interpreter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGLabel implements Serializable {
	
	private boolean defined = false;
	private int adr;
	private ArrayList<IGJumpStmt> fixups = new ArrayList<IGJumpStmt>(1);

	public int getAdr(IGJumpStmt js_) {
		if (!defined) {
			fixups.add(js_);
		}
		return adr;
	}
	public void setAdr (int adr_) {
		adr = adr_;
		defined = true;
		int n = fixups.size();
		for (int i=0; i<n; i++) {
			IGJumpStmt js = fixups.get(i);
			js.setAdr(adr_);
		}
	}
}
