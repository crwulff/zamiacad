/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 22, 2008
 */
package org.zamia.instgraph;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGProcess extends IGConcurrentStatement implements Scope {

	private IGSequenceOfStatements fSOS;

	private boolean fPostponed; // FIXME: implement semantics

	private long fContainerDBID;

	private transient IGContainer fContainer = null;

	public IGProcess(boolean aPostponed, long aParentContainerDBID, String aLabel, SourceLocation aLocation, ZDB aZDB) {
		super(aLabel, aLocation, aZDB);

		fContainer = new IGContainer(aParentContainerDBID, aLocation, aZDB);
		fContainerDBID = aZDB.store(fContainer);
	}

	public void setStatementSequence(IGSequenceOfStatements aSOS) {
		fSOS = aSOS;
	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
	}
	
	public IGProcess getStructure() {
		return this;
	}	

	@Override
	public String toString() {
		if (fPostponed) {
			return "postponed process (id=" + getLabel() + ")";
		}
		return "process (id=" + getLabel() + ")";
	}

	public void appendFinalWait(ArrayList<IGOperation> aSensSignals) {

		ArrayList<IGOperation> sensSignals = aSensSignals;

		if (sensSignals == null) {
			HashSetArray<IGObject> sens = new HashSetArray<IGObject>();
			fSOS.computeReadSignals(sens);

			int n = sens.size();

			if (n > 0) {

				if (sensSignals == null) {
					sensSignals = new ArrayList<IGOperation>(n);
				}

				for (int i = 0; i < n; i++) {
					sensSignals.add(new IGOperationObject(sens.get(i), null, getZDB()));
				}
			}
		} else if (sensSignals.size() == 0) {
			sensSignals = null;
		}

		fSOS.add(new IGSequentialWait(null, null, sensSignals, null, null, getZDB()));

		appendRestart();
	}

	public void appendRestart() {
		// Make process loop (run from WAIT stmt to WAIT stmt)
		fSOS.add(new IGSequentialRestart(null, null, getZDB()));
	}

	@Override
	public IGItem findChild(String aLabel) {
		IGContainer container = getContainer();
		ArrayList<IGContainerItem> items = container.findLocalItems(aLabel);
		if (items == null) {
			return null;
		}
		int n = items.size();
		if (n == 0) {
			return null;
		}
		if (n > 1) {
			logger.warn("Warning: IGProcess %s: more than one item labeled %s found.", this, aLabel);
		}
		return items.get(0);
	}

	@Override
	public IGItem getChild(int aIdx) {
		return aIdx == 0 ? getContainer() : fSOS;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	public IGSequenceOfStatements getSequenceOfStatements() {
		return fSOS;
	}

}
