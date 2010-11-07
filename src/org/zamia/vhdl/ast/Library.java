/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Dec 16, 2004
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
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.util.HashMapArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class Library extends DeclarativeItem {

	private HashMapArray<String, DMUID> fDUs;

	public Library(String aId) {
		super(aId, null, 0l);
		fDUs = new HashMapArray<String, DMUID>();
	}

	public int getNumDUs() {
		return fDUs.size();
	}

	public DMUID getDU(int aIdx) {
		return fDUs.get(aIdx);
	}

	public void dump(PrintStream aOut) {
		// FIXME
		// out_.println ("DUMPING LIBRARY "+id);
		// out_.println ("========================================");
		// out_.println ();
		// out_.println ("Entities:");
		// out_.println ("---------");
		// for (Iterator i = entities.keySet().iterator(); i.hasNext();) {
		// String id = (String) i.next();
		// out_.println(" "+id);
		// }
		// out_.println ("Architectures:");
		// out_.println ("--------------");
		// int n = architectures.size();
		// for (int i = 0; i<n; i++) {
		// Architecture arch = architectures.get(i);
		// out_.println(" "+arch.getId());
		// if (arch instanceof ArchitectureNL) {
		// ArchitectureNL anl = (ArchitectureNL) arch;
		// anl.dump();
		// }
		// }
	}

	public String toString() {
		return "Library " + id;
	}

	/*
	 * next two methods are called by the parser/indexer
	 */

	@Override
	public VHDLNode getChild(int aIdx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void add(DMUID aDUUID) {
		fDUs.put(aDUUID.getUID(), aDUUID);
	}

	@Override
	public void findReferences(String aId, ObjectCat aCat, RefType aRefType, int aDepth, ZamiaProject aZPrj, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult aResult, ArrayList<SearchJob> aTODO) {

		// FIXME: todo

		logger.warn("%s: findReferences not implemented yet.", getClass());

	}

	@Override
	public IGContainerItem computeIG(ArrayList<IGContainerItem> aSpecItems, IGContainer aContainer, IGElaborationEnv aCache) throws ZamiaException {
		throw new ZamiaException("Sorry, not implemented yet.", this);
	}

}
