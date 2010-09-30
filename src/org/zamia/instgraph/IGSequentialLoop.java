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
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.interpreter.IGAttributeStmt;
import org.zamia.instgraph.interpreter.IGBinaryOpStmt;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGJumpNCStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.instgraph.interpreter.IGPushRefStmt;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.util.HashSetArray;
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
		fBody.computeAccessedItems(aFilterItem, aFilterType, aDepth, aAccessedItems);

	}

	public IGContainer getContainer() {
		if (fContainer == null) {
			fContainer = (IGContainer) getZDB().load(fContainerDBID);
		}

		return fContainer;
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {

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
			IGOperation one = param.getType().getOne(computeSourceLocation());

			// init loop var

			aCode.add(new IGPushRefStmt(param, computeSourceLocation(), getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.LEFT, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGPopStmt(false, false, false, computeSourceLocation(), getZDB()));

			// loop header: check boundaries

			IGLabel loop = new IGLabel();
			aCode.defineLabel(loop);
			IGLabel lhdesc = new IGLabel();
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.ASCENDING, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpNCStmt(lhdesc, computeSourceLocation(), getZDB()));

			aCode.add(new IGPushStmt(getParameter(), computeSourceLocation(), getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGBinaryOpStmt(BinOp.LESSEQ, b, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpNCStmt(loopExitLabel, computeSourceLocation(), getZDB()));
			IGLabel loopbody = new IGLabel();
			aCode.add(new IGJumpStmt(loopbody, computeSourceLocation(), getZDB()));

			aCode.defineLabel(lhdesc);
			aCode.add(new IGPushStmt(getParameter(), computeSourceLocation(), getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGBinaryOpStmt(BinOp.GREATEREQ, b, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpNCStmt(loopExitLabel, computeSourceLocation(), getZDB()));

			// loop body:

			aCode.defineLabel(loopbody);
			fBody.generateCode(aCode);

			// loop footer: inc/dec loop var, jump back to header

			IGLabel lfdesc = new IGLabel();
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.ASCENDING, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpNCStmt(lfdesc, computeSourceLocation(), getZDB()));

			aCode.add(new IGPushRefStmt(getParameter(), computeSourceLocation(), getZDB()));
			aCode.add(new IGPushStmt(getParameter(), computeSourceLocation(), getZDB()));
			one.generateCode(true, aCode);
			aCode.add(new IGBinaryOpStmt(BinOp.ADD, getParameter().getType(), computeSourceLocation(), getZDB()));
			aCode.add(new IGPopStmt(false, false, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpStmt(loop, computeSourceLocation(), getZDB()));

			aCode.defineLabel(lfdesc);

			aCode.add(new IGPushRefStmt(getParameter(), computeSourceLocation(), getZDB()));
			aCode.add(new IGPushStmt(getParameter(), computeSourceLocation(), getZDB()));
			one.generateCode(true, aCode);
			aCode.add(new IGBinaryOpStmt(BinOp.SUB, getParameter().getType(), computeSourceLocation(), getZDB()));
			aCode.add(new IGPopStmt(false, false, false, computeSourceLocation(), getZDB()));
			aCode.add(new IGJumpStmt(loop, computeSourceLocation(), getZDB()));

			break;

		case INFINITE:

			loop = new IGLabel();
			aCode.defineLabel(loop);

			fBody.generateCode(aCode);

			aCode.add(new IGJumpStmt(loop, computeSourceLocation(), getZDB()));
			break;

		case WHILE:

			loop = new IGLabel();
			aCode.defineLabel(loop);

			fCond.generateCode(true, aCode);
			aCode.add(new IGJumpNCStmt(loopExitLabel, computeSourceLocation(), getZDB()));

			fBody.generateCode(aCode);

			aCode.add(new IGJumpStmt(loop, computeSourceLocation(), getZDB()));

			break;

		default:
			// FIXME:
			throw new ZamiaException("Sorry, not implemented yet.");
		}

		aCode.removeLoopExitLabel(loopLabel);

		aCode.defineLabel(loopExitLabel);

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
