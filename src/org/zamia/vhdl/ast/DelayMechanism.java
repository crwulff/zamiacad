/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 21, 2009
 */
package org.zamia.vhdl.ast;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class DelayMechanism extends VHDLNode {

	private boolean isInertial;

	private Operation rejectTime;

	public DelayMechanism(boolean isInertial_, Operation rejectTime_, VHDLNode parent_, long location_) {
		super(parent_, location_);

		isInertial = isInertial_;
		rejectTime = rejectTime_;
	}

	@Override
	public void findReferences(String id_, ObjectCat category, RefType refType_, int depth_, ZamiaProject zPrj_, IGContainer container_, IGElaborationEnv cache_,
			ReferenceSearchResult aResult_, ArrayList<SearchJob> todo_) throws ZamiaException {

		if (rejectTime != null) {
			rejectTime.findReferences(id_, category, refType_, depth_, zPrj_, container_, cache_, aResult_, todo_);
		}
	}

	public Operation getRejectTime() {
		return rejectTime;
	}
	
	public boolean isInertial() {
		return isInertial;
	}
	
	@Override
	public VHDLNode getChild(int idx_) {
		return rejectTime;
	}

	@Override
	public int getNumChildren() {
		return 1;
	}

	@Override
	public String toString() {
		if (!isInertial) {
			return "TRANSPORT";
		}

		if (rejectTime != null) {
			return "REJECT " + rejectTime.toVHDL() + " INERTIAL";
		}

		return "INERTIAL";
	}

}
