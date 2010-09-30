/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 21, 2009
 */
package org.zamia.vhdl.ast;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public abstract class ConcurrentSignalAssignment extends ConcurrentStatement {

	protected DelayMechanism delayMechanism;

	protected boolean guarded = false;
	
	public ConcurrentSignalAssignment(ASTObject parent_, long location_) {
		super(parent_, location_);
	}

	public void setGuarded(boolean guarded_) {
		guarded = guarded_;
	}

	public void setDelayMechanism(DelayMechanism dm_) {
		delayMechanism = dm_;
	}
	
}
