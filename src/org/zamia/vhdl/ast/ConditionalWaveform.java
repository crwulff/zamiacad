/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 16, 2005
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
public class ConditionalWaveform extends ASTObject {

	private Waveform waveform;

	private Operation cond;

	public ConditionalWaveform(Waveform waveform_, Operation cond_, ASTObject parent_, long location_) {
		super(parent_, location_);
		waveform = waveform_;
		waveform.setParent(this);
		setCond(cond_);
	}

	public ConditionalWaveform(Waveform waveform_, ASTObject parent_, long location_) {
		this(waveform_, null, parent_, location_);
	}

	public Operation getCond() {
		return cond;
	}

	public Waveform getWaveform() {
		return waveform;
	}

	public void setCond(Operation cond_) {
		cond = cond_;
		if (cond != null)
			cond.setParent(this);
	}

	/**
	 * check if condition == null
	 * 
	 * @return true if condition == null
	 */
	public boolean isDefault() {
		return cond == null;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	@Override
	public ASTObject getChild(int idx_) {
		switch (idx_) {
		case 0:
			return cond;
		case 1:
			return waveform;
		}
		return null;
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		if (cond != null)
			cond.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

		waveform.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
	}

	@Override
	public String toString() {
		if (cond == null)
			return waveform.toVHDL();
		return waveform.toVHDL() + " WHEN " + cond.toVHDL();
	}

}
