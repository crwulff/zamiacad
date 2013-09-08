/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph;

import java.math.BigInteger;

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
import org.zamia.instgraph.interpreter.IGJumpCStmt;
import org.zamia.instgraph.interpreter.IGJumpNCStmt;
import org.zamia.instgraph.interpreter.IGJumpStmt;
import org.zamia.instgraph.interpreter.IGLabel;
import org.zamia.instgraph.interpreter.IGNewObjectStmt;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.instgraph.interpreter.IGPushStmt;
import org.zamia.instgraph.interpreter.IGStmt;
import org.zamia.instgraph.interpreter.IGPushStmt.OBJECT;
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
	public void generateCode(final IGInterpreterCode aCode) throws ZamiaException {

		final SourceLocation location = computeSourceLocation();
		
		IGLabel loopExitLabel = new IGLabel();

		String loopLabel = getId();
		if (loopLabel == null) {

			// generate one

			loopLabel = "___###___anonymous_loop_label_" + hashCode();
		}

		aCode.addLoopExitLabel(loopLabel, loopExitLabel);

		switch (fSeqLoopType) {
		case FOR:

			/*
			 * The plot:
			 * addCode(enter context)
			 * addCode(set param := left)
			 * addcode(if param > RIGHT goto exit)
			 * loop:
			 *     addCode(body)
			 *     addCode( param == RIGHT ? goto exit) 
			 *     addCode( param := (asc ? successor : predcessor)) 
			 *     goto loop
			 * exit:
			 * addCode(exit context)
			 * 
			 */
			IGType b = getContainer().findBoolType();
			final IGObject param = getParameter();

			class Condition {

				public void generate(IGStmt ascStmt, IGStmt descStmt) throws ZamiaException {
					IGLabel ascLabel = new IGLabel();
					IGLabel exitLabel = new IGLabel();
					fRange.generateCode(true, aCode);
					aCode.add(new IGAttributeStmt(param.getType(), AttrOp.ASCENDING, false, location, getZDB()));
					aCode.add(new IGJumpCStmt(ascLabel, location, getZDB()));
					
					// descending
					aCode.add(descStmt);
					aCode.add(new IGJumpStmt(exitLabel, location, getZDB()));
					
					aCode.defineLabel(ascLabel);
					aCode.add(ascStmt);
					
					aCode.defineLabel(exitLabel);
				}
				
			}

			class ConditionStatic extends Condition {

				final boolean asc;
				ConditionStatic (boolean asc) {this.asc = asc;}
				public void generate(IGStmt ascStmt, IGStmt descStmt) throws ZamiaException {
					aCode.add(asc ? ascStmt : descStmt);
				}
				
			}

			Condition conditional;
			if (fRange instanceof IGRange) {
				IGRange range = ((IGRange) fRange);
				IGStaticValue ascSV = (IGStaticValue) range.getAscending();
				boolean asc = ascSV.isTrue();
				conditional = new ConditionStatic(asc);
			} else {
				conditional = new Condition();
			}
			
			
			// init loop var
			aCode.add(new IGEnterNewContextStmt(location, getZDB()));
			aCode.add(new IGNewObjectStmt(param, location, getZDB()));

			aCode.add(new IGPushStmt.OBJECT(param, location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.LEFT, false, location, getZDB()));
			aCode.add(new IGPopStmt(false, false, false, location, getZDB()));

			// loop header: check boundaries
			aCode.add(new IGPushStmt.OBJECT(getParameter(), location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, location, getZDB()));
			conditional.generate(new IGBinaryOpStmt(BinOp.GREATER, b, location, getZDB()),
						new IGBinaryOpStmt(BinOp.LESS, b, location, getZDB()));
			aCode.add(new IGJumpCStmt(loopExitLabel, location, getZDB()));
			
			// loop body:
			
			IGLabel loop = new IGLabel();
			aCode.defineLabel(loop);

			fBody.generateCode(aCode);

			// loop footer: inc/dec loop var, jump back to header

			aCode.add(new IGPushStmt.OBJECT(getParameter(), location, getZDB()));
			fRange.generateCode(true, aCode);
			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHT, false, location, getZDB()));
			aCode.add(new IGBinaryOpStmt(BinOp.EQUAL, b, location, getZDB()));
			aCode.add(new IGJumpCStmt(loopExitLabel, location, getZDB()));

			aCode.add(new IGPushStmt.OBJECT(getParameter(), location, getZDB()));
			aCode.add(new IGPushStmt.OBJECT(getParameter(), location, getZDB()));
			aCode.add(new IGPushStmt.TYPE(param.getType(), location, getZDB()));
			conditional.generate(new IGAttributeStmt(param.getType(), AttrOp.SUCC, true, location, getZDB()), 
					new IGAttributeStmt(param.getType(), AttrOp.PRED, true, location, getZDB()));
			
			// It would be nicer to avoid extra range checks with rightof function, 
			// that happen when computing succ dynamically. But, unfortunately,  
			// i in high downto low computes rightof(high) = high+1 because it is in-
			// teger ascending range, despite it is a downto range and we should have 
			// predcessors at the right. 
//			aCode.add(new IGAttributeStmt(param.getType(), AttrOp.RIGHTOF, true, location, getZDB()));
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
