/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGSequenceOfStatements;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialSignalAssignment extends SequentialStatement {

	public static final boolean dump = false;

	private Target target;

	private Waveform waveform;

	private DelayMechanism delayMechanism;

	public SequentialSignalAssignment(Target target_, Waveform waveform_, DelayMechanism delayMechanism_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		target = target_;
		delayMechanism = delayMechanism_;
		target.setParent(this);
		waveform = waveform_;
		waveform.setParent(this);
	}

	public Target getTarget() {
		return target;
	}

	public Waveform getValue() {
		return waveform;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(target.toString()+" <= ");
		
		if (delayMechanism != null) {
			buf.append(delayMechanism.toString());
		}
		
		buf.append(waveform.toString());
		
		return buf.toString();
	}

	@Override
	public int getNumChildren() {
		return 3;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		switch (idx_) {
		case 0:
			return target;
		case 1:
			return waveform;
		case 2:
			return delayMechanism;
		}
		return null;
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		printlnIndented(toString() + ";", indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (category_ == ObjectCat.Signal) {
			target.findReferences(id_, category_, RefType.Write, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}

		waveform.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		
		if (delayMechanism != null) {
			delayMechanism.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		waveform.generateIGSequence(target, delayMechanism, aSeq, aContainer, aCache);
	}

}
