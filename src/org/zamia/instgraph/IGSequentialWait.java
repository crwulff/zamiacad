/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 23, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.interpreter.IGCancelAllWakeupsStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGJumpCStmt;
import org.zamia.instgraph.interpreter.IGJumpEventStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGJumpTimeoutStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.instgraph.interpreter.IGScheduleEventWakeupStmt;
import org.zamia.instgraph.interpreter.IGScheduleTimedWakeupStmt;
import org.zamia.instgraph.interpreter.IGWaitStmt;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSequentialWait extends IGSequentialStatement {

	private IGOperation fTimeoutClause;

	private IGOperation fConditionClause;

	private ArrayList<IGOperation> fSensitivityList;

	public IGSequentialWait(IGOperation aTimeoutClause, IGOperation aConditionClause, ArrayList<IGOperation> aSensitivityList, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);

		fTimeoutClause = aTimeoutClause;
		fConditionClause = aConditionClause;

		if (aSensitivityList != null) {
			int n = aSensitivityList.size();
			fSensitivityList = new ArrayList<IGOperation>(n);
			for (int i = 0; i < n; i++) {
				fSensitivityList.add(aSensitivityList.get(i));
			}
		}
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {

		if (fTimeoutClause != null) {
			fTimeoutClause.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		if (fConditionClause != null) {
			fConditionClause.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
		}

		if (fSensitivityList != null) {
			int n = fSensitivityList.size();
			for (int i = 0; i < n; i++) {
				getSensitivityListOp(i).computeAccessedItems(false, aFilterItem, aFilterType, aDepth, aAccessedItems);
				//addItemAccess(getSensitivityListSignal(i), AccessType.Read, aDepth, aFilterItem, aFilterType, aAccessedItems);
			}
		}
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		if (fTimeoutClause != null) {
			fTimeoutClause.generateCode(true, aCode);
			aCode.add(new IGScheduleTimedWakeupStmt(computeSourceLocation(), getZDB()));
		}

		if (fSensitivityList != null) {

			int n = getNumSensitivityOps();

			for (int i = 0; i < n; i++) {

				IGOperation zs = getSensitivityListOp(i);
				zs.generateCodeRef(true, false, aCode);

				aCode.add(new IGScheduleEventWakeupStmt(computeSourceLocation(), getZDB()));
			}
		}

		HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();

		if (fConditionClause != null) {

			fConditionClause.computeAccessedItems(false, null, null, 0, accessedItems);

			int n = accessedItems.size();
			for (int i = 0; i < n; i++) {

				IGItemAccess acc = accessedItems.get(i);

				IGItem item = acc.getItem();
				if (item instanceof IGObject) {

					IGObject obj = (IGObject) item;

					aCode.add(new IGPushStmt(obj, computeSourceLocation(), getZDB()));
					aCode.add(new IGScheduleEventWakeupStmt(computeSourceLocation(), getZDB()));
				}
			}

		}

		IGLabel waitLoopLabel = new IGLabel();
		IGLabel waitDoneLabel = new IGLabel();
		aCode.defineLabel(waitLoopLabel);

		aCode.add(new IGWaitStmt(computeSourceLocation(), getZDB()));

		if (fTimeoutClause != null) {
			aCode.add(new IGJumpTimeoutStmt(waitDoneLabel, computeSourceLocation(), getZDB()));
		}

		if (fSensitivityList != null) {

			int n = getNumSensitivityOps();

			for (int i = 0; i < n; i++) {

				IGOperation zs = getSensitivityListOp(i);
				zs.generateCode(true, aCode);

				aCode.add(new IGJumpEventStmt(waitDoneLabel, computeSourceLocation(), getZDB()));
			}
		}

		if (fConditionClause != null) {

			fConditionClause.generateCode(true, aCode);
			
			aCode.add(new IGJumpCStmt(waitDoneLabel, computeSourceLocation(), getZDB()));
		}
		
		
		aCode.add(new IGJumpStmt(waitLoopLabel, computeSourceLocation(), getZDB()));

		aCode.defineLabel(waitDoneLabel);
		
		aCode.add(new IGCancelAllWakeupsStmt(computeSourceLocation(), getZDB()));
	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fConditionClause;
		case 1:
			return fTimeoutClause;
		}
		return getSensitivityListOp(aIdx - 2);
	}

	@Override
	public int getNumChildren() {
		return 2 + getNumSensitivityOps();
	}

	public int getNumSensitivityOps() {
		return fSensitivityList == null ? 0 : fSensitivityList.size();
	}

	public IGOperation getSensitivityListOp(int aIdx) {
		return fSensitivityList.get(aIdx);
	}

}
