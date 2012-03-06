/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.analysis.ig.IGAssignmentsSearch.AccessedThroughItems;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.interpreter.IGAttributeStmt;
import org.zamia.instgraph.interpreter.IGBinaryOpStmt;
import org.zamia.instgraph.interpreter.IGEnterNewContextStmt;
import org.zamia.instgraph.interpreter.IGExitContextStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGJumpNCStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.instgraph.interpreter.IGNewObjectStmt;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class IGSequentialLoop extends IGSequentialStatement {
	public enum SeqLoopType {
		INFINITE, FOR, WHILE
	};

	private SeqLoopType fSeqLoopType;

	private IGSequenceOfStatements fBody;

	private long fContainerDBID;

	private transient IGContainer fContainer = null;

	private IGOperation fCond;

	private IGOperation fRange;

	private long fParameterDBID;

	public IGSequentialLoop(IGOperation aCond, long aParentContainerDBID, String aId, SourceLocation aSrc, ZDB aZDB) throws ZamiaException {
		this(SeqLoopType.WHILE, aParentContainerDBID, aId, aSrc, aZDB);
		fCond = aCond;
	}

	public IGSequentialLoop(IGObject aParameter, IGOperation aRange, long aParentContainerDBID, String aId, SourceLocation aSrc, ZDB aZDB) throws ZamiaException {
		this(SeqLoopType.FOR, aParentContainerDBID, aId, aSrc, aZDB);
		fContainer.add(aParameter);
		fParameterDBID = aParameter.getDBID();
		fRange = aRange;
	}

	public IGSequentialLoop(SeqLoopType aSeqLoopType, long aParentContainerDBID, String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
		fSeqLoopType = aSeqLoopType;
		fContainer = new IGContainer(aParentContainerDBID, aSrc, aZDB);
		fContainerDBID = aZDB.store(fContainer);
	}

	private IGObject getParameter() {
		return (IGObject) getZDB().load(fParameterDBID);
	}

	public void setBody(IGSequenceOfStatements aBody) {
		fBody = aBody;
	}

	@Override
	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {
//		fBody.computeAccessedItems(aFilterItem, aFilterType, aDepth, aAccessedItems);
		HashSetArray<IGItemAccess> accessedConditions = new HashSetArray<IGItemAccess>();
		
		if (fSeqLoopType == SeqLoopType.WHILE) {
			fCond.computeAccessedItems(false, aFilterItem, aFilterType, aDepth, accessedConditions);
			aAccessedItems.addAll(accessedConditions);
			
			if (aAccessedItems instanceof AccessedThroughItems) {
				((AccessedThroughItems)aAccessedItems).ifStack.push(new Pair<IGSequentialStatement, HashSetArray<IGItemAccess>>(this, accessedConditions));
			}
		}
		fBody.computeAccessedItems(aFilterItem, aFilterType, aDepth, aAccessedItems);
		if (fSeqLoopType == SeqLoopType.WHILE && aAccessedItems instanceof AccessedThroughItems) {
			((AccessedThroughItems)aAccessedItems).ifStack.pop();
		}
	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

		SourceLocation location = computeSourceLocation();
		
		IGLabel loopExitLabel = new IGLabel();

		String loopLabel = getId();
		if (loopLabel == null) {

			// generate one

			loopLabel = "___###___anonymous_loop_label_" + hashCode();
		}

		aCode.addLoopExitLabel(loopLabel, loopExitLabel);

		switch (fSeqLoopType) {
		case FOR:

			IGType b = getContainer().findBoolType();
			IGObject param = getParameter();
			IGOperation one = param.getType().getOne(location);

			// init loop var

			aCode.add(new IGEnterNewContextStmt(location, getZDB()));
			aCode.add(new IGNewObjectStmt(param, location, getZDB()));
			
			aCode.add(new IGPushStmt(param, location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.LEFT, false, location, getZDB()));
			aCode.add(new IGPopStmt(false, false, false, location, getZDB()));

			// loop header: check boundaries

			IGLabel loop = new IGLabel();
			aCode.defineLabel(loop);
			IGLabel lhdesc = new IGLabel();
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.ASCENDING, false, location, getZDB()));
			aCode.add(new IGJumpNCStmt(lhdesc, location, getZDB()));

			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, location, getZDB()));
			aCode.add(new IGBinaryOpStmt(BinOp.LESSEQ, b, location, getZDB()));
			aCode.add(new IGJumpNCStmt(loopExitLabel, location, getZDB()));
			IGLabel loopbody = new IGLabel();
			aCode.add(new IGJumpStmt(loopbody, location, getZDB()));

			aCode.defineLabel(lhdesc);
			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, location, getZDB()));
			aCode.add(new IGBinaryOpStmt(BinOp.GREATEREQ, b, location, getZDB()));
			aCode.add(new IGJumpNCStmt(loopExitLabel, location, getZDB()));

			// loop body:

			aCode.defineLabel(loopbody);
			fBody.generateCode(aCode);

			// loop footer: inc/dec loop var, jump back to header

			IGLabel lfdesc = new IGLabel();
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.ASCENDING, false, location, getZDB()));
			aCode.add(new IGJumpNCStmt(lfdesc, location, getZDB()));

			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			one.generateCode(true, aCode);
			aCode.add(new IGBinaryOpStmt(BinOp.ADD, getParameter().getType(), location, getZDB()));
			aCode.add(new IGPopStmt(false, false, false, location, getZDB()));
			aCode.add(new IGJumpStmt(loop, location, getZDB()));

			aCode.defineLabel(lfdesc);

			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			aCode.add(new IGPushStmt(getParameter(), location, getZDB()));
			one.generateCode(true, aCode);
			aCode.add(new IGBinaryOpStmt(BinOp.SUB, getParameter().getType(), location, getZDB()));
			aCode.add(new IGPopStmt(false, false, false, location, getZDB()));
			aCode.add(new IGJumpStmt(loop, location, getZDB()));

			aCode.defineLabel(loopExitLabel);
			aCode.add(new IGExitContextStmt(location, getZDB()));

			break;

		case INFINITE:

			loop = new IGLabel();
			aCode.defineLabel(loop);

			fBody.generateCode(aCode);

			aCode.add(new IGJumpStmt(loop, location, getZDB()));
			
			aCode.defineLabel(loopExitLabel);

			break;

		case WHILE:

			loop = new IGLabel();
			aCode.defineLabel(loop);

			fCond.generateCode(true, aCode);
			aCode.add(new IGJumpNCStmt(loopExitLabel, location, getZDB()));

			fBody.generateCode(aCode);

			aCode.add(new IGJumpStmt(loop, location, getZDB()));

			aCode.defineLabel(loopExitLabel);

			break;

		default:
			// FIXME:
			throw new ZamiaException("Sorry, not implemented yet.");
		}

		aCode.removeLoopExitLabel(loopLabel);


	}

	@Override
	public IGItem getChild(int aIdx) {
		switch (aIdx) {
		case 0:
			return fRange;
		case 1:
			return getParameter();
		case 2:
			return fCond;
		case 3:
			return getContainer();
		}

		return fBody;
	}

	@Override
	public int getNumChildren() {
		return 5;
	}

}
