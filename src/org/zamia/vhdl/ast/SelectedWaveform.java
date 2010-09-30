/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
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
public class SelectedWaveform extends ASTObject {

	private Waveform waveform;
	private ArrayList<Range> choices;
	
	public SelectedWaveform (Waveform waveform_, ArrayList<Range> choices_, ASTObject parent_, long location_) {
		super (parent_, location_);
		waveform = waveform_;
		waveform.setParent(this);
		choices = choices_;
		if (choices != null) {
			int n = choices.size();
			for (int i = 0; i<n; i++) {
				Range choice = choices.get(i);
				if (choice != null)
					choice.setParent(this);
			}
		}
	}
	
	public Waveform getWaveform() {
		return waveform;
	}
	
	public ArrayList<Range> getChoices() {
		return choices;
	}

	@Override
	public int getNumChildren() {
		return choices.size()+1;
	}

	@Override
	public ASTObject getChild(int idx_) {
		if (idx_ == 0)
			return waveform;
		return choices.get(idx_-1);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache, ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
		waveform.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
		if (choices != null) {
			
			int n = choices.size();
			for (int i = 0; i<n; i++) {
				
				Range choice = choices.get(i);
				
				if (choice == null)
					continue;
				choice.findReferences(id_, category_, refType_, depth_+1, zprj_, aContainer, aCache, result_, aTODO);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder( waveform +" WHEN ");
		
		int n = choices.size();
		for (int i = 0; i<n; i++) {
			buf.append(choices.get(i));
			if (i<n-1) {
				buf.append(", ");
			}
		}
		return buf.toString();
	}
}
