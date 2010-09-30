/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jun 16, 2006
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
public class Identifier extends ASTObject {

	private String fImage;

	private boolean fExtended;

	public Identifier(String aImage, boolean aExtended, long aLocation) {
		super(aLocation);
		
		fImage = aImage;
		fExtended = aExtended;
	}

	public boolean isExtended() {
		return fExtended;
	}
	
	public String getImage() {
		return fImage;
	}
	
	@Override
	public String toString() {
		return fImage;
	}

	@Override
	public boolean equals(Object aObject) {
		if (!(aObject instanceof Identifier)) {
			return false;
		}

		Identifier id2 = (Identifier) aObject;
		return fImage.equalsIgnoreCase(id2.fImage) && fExtended == id2.isExtended();
	}
	
	@Override
	public int hashCode() {
		return fImage.toUpperCase().hashCode();
	}

	@Override
	public void findReferences(String aId, ObjectCat aCategory, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) throws ZamiaException {
	}

	@Override
	public ASTObject getChild(int aIdx) {
		return null;
	}

	@Override
	public int getNumChildren() {
		return 0;
	}
}
