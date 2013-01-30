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
		
		
		//LRM 10.2 Wait statement: If no sensitivity clause appears, the sensitivity set is 
		// set to primaries in the condition of the condition clause.
		if (fSensitivityList == null && fConditionClause != null) {

			HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();

			fConditionClause.computeAccessedItems(false, null, AccessType.Read, 0, accessedItems);
			int n = accessedItems.size();
			fSensitivityList = new ArrayList<IGOperation>(n);
			for (int i = 0; i < n; i++) {

				IGItemAccess acc = accessedItems.get(i);

				IGItem item = acc.getItem();
				if (item instanceof IGObject) {
					IGOperationObject obj = new IGOperationObject((IGObject) item, aSrc, aZDB);
					fSensitivityList.add(obj);
				}
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
				zs.generateCode(true, aCode);

				aCode.add(new IGScheduleEventWakeupStmt(computeSourceLocation(), getZDB()));
			}
		}

		IGLabel waitLoopLabel = new IGLabel();
		IGLabel waitDoneLabel = new IGLabel();
		aCode.defineLabel(waitLoopLabel);

		aCode.add(new IGWaitStmt(computeSourceLocation(), getZDB()));

		//The timeout clause specifies the maximum amount of time the process will remain  
		// suspended at this wait statement.
		if (fTimeoutClause != null) {
			aCode.add(new IGJumpTimeoutStmt(waitDoneLabel, computeSourceLocation(), getZDB()));
		}

		//LRM: The suspended process also resumes as a result of an event occurring on any signal  
		// in the sensitivity set of the wait statement. If such an event occurs, the condition in  
		// the condition clause is evaluated.
		if (fSensitivityList != null) {

			IGLabel condLabel = new IGLabel();
			
			int n = getNumSensitivityOps();

			for (int i = 0; i < n; i++) {

				IGOperation zs = getSensitivityListOp(i);
				zs.generateCode(true, aCode);

				aCode.add(new IGJumpEventStmt(condLabel, computeSourceLocation(), getZDB()));
			}
			
			aCode.defineLabel(condLabel);
			
			if (fConditionClause != null) {

				fConditionClause.generateCode(true, aCode);

				aCode.add(new IGJumpCStmt(waitDoneLabel, computeSourceLocation(), getZDB()));
				
				// LRM: if condition is FALSE, the process suspends again. Such repeated suspension 
				// does not involve the recalculation of the timeout interval.
				aCode.add(new IGJumpStmt(waitLoopLabel, computeSourceLocation(), getZDB()));
				
			}

		}

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

	@Override
	public void dump(int aIndent) {
		logger.debug(aIndent, "wait cond=%s sens=%s time=%s", fConditionClause, fSensitivityList, fTimeoutClause);
	}

	public IGOperation getTimeoutClause() {
		return fTimeoutClause;
	}

	public IGOperation getConditionClause() {
		return fConditionClause;
	}

	public ArrayList<IGOperation> getSensitivityList() {
		return fSensitivityList;
	}

}
