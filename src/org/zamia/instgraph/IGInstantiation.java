/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;
import java.util.List;

import org.zamia.SourceLocation;
import org.zamia.util.Pair;
import org.zamia.util.ZHash;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGInstantiation extends IGConcurrentStatement {

	private ArrayList<IGMapping> fMappings = new ArrayList<IGMapping>();
	private ArrayList<IGMapping> fGenericMappings = new ArrayList<IGMapping>();

	private ArrayList<Pair<String, IGStaticValue>> fActualGenerics = new ArrayList<Pair<String, IGStaticValue>>();

	private DMUID fDUUID, fChildDUUID;

	private String fSignature;

	public IGInstantiation( DMUID aDUUID, DMUID aChildDUUID, String aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);

		fDUUID = aDUUID;
		fChildDUUID = aChildDUUID;
	}

	public void add(IGMapping aMapping) {
		fMappings.add(aMapping);
	}

	@Override
	public String toString() {
		return getLabel() + ": " + fSignature;
	}

	public DMUID getChildDUUID() {
		return fChildDUUID;
	}

	public DMUID getDUUID() {
		return fDUUID;
	}

	public void addActualGeneric(String aId, IGStaticValue aActualGeneric) {
		fActualGenerics.add(new Pair<String, IGStaticValue>(aId, aActualGeneric));
	}

	public static String computeSignature(DMUID aDUUID, List<Pair<String, IGStaticValue>> aActualGenerics) {
		StringBuilder buf = new StringBuilder();

		if (aActualGenerics != null) {
			int n = aActualGenerics.size();
			for (int i = 0; i < n; i++) {

				Pair<String, IGStaticValue> actual = aActualGenerics.get(i);

				buf.append("###");
				buf.append(actual.getFirst());
				buf.append(":=");
				buf.append(actual.getSecond().toHRString());
			}
		}

		return aDUUID.getUID() + "_H_" + ZHash.encodeZ(buf.toString());
	}

	public void computeSignature() {
		fSignature = computeSignature(fChildDUUID, fActualGenerics);
	}

	public String getSignature() {
		return fSignature;
	}

	public ArrayList<Pair<String, IGStaticValue>> getActualGenerics() {
		return fActualGenerics;
	}

	@Override
	public IGItem findChild(String aLabel) {
		
		IGManager igm = getIGM();
		
		IGModule child = igm.findModule(fSignature);
		
		if (child == null) {
			return null;
		}
		
		if (aLabel == null) {
			return child;
		}
		
		return child.findChild(aLabel);
	}

	@Override
	public int getNumChildren() {
		return fMappings.size() + fGenericMappings.size();
	}

	@Override
	public IGItem getChild(int aIdx) {
		
		int idx = aIdx ;
		int n = fGenericMappings.size();
		if (idx < n) {
			return fGenericMappings.get(idx);
		}
		idx -= n;
		
		return fMappings.get(idx);
	}

	public int getNumMappings() {
		return fMappings.size();
	}
	
	public IGMapping getMapping (int aIdx) {
		return fMappings.get(aIdx);
	}

	public void addGeneric(IGMapping aMapping) {
		fGenericMappings.add(aMapping);
	}
}
