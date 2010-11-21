/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.instgraph.interpreter;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGObject.IGObjectCat;


/**
 * assignment targets get ultimately turned into object writer chains.
 * 
 * @author Guenter Bartsch
 * 
 */
@Deprecated
public class IGObjectWriter {

	public enum SNOp {
		OBJECT, RECORD, ARRAYINDEX, ARRAYRANGE
	};

	private final SNOp fSNOp;

	private IGObjectWriter fNext, fPrev;

	private final IGRecordField fRF;

	private final int fIdx;

	private final IGObject fObject;

	private final int fMin;

	private final int fMax;

	private IGStaticValue fValue;

	private final IGTypeStatic fType;

	private final SourceLocation fLocation;

	public IGObjectWriter(IGObject aObject, SourceLocation aLocation) {
		fObject = aObject;
		fSNOp = SNOp.OBJECT;
		fLocation = aLocation;
		fRF = null;
		fIdx = 0;
		fMin = 0;
		fMax = 0;
		fType = null;
	}

	public IGObjectWriter(IGObjectWriter aPrev, IGRecordField aRF, SourceLocation aLocation) {
		fSNOp = SNOp.RECORD;
		fRF = aRF;
		aPrev.setNext(this);
		fPrev = aPrev;
		fLocation = aLocation;
		fIdx = 0;
		fObject = null;
		fMin = 0;
		fMax = 0;
		fType = null;
	}

	public IGObjectWriter(IGObjectWriter aPrev, int aIdx, SourceLocation aLocation) {
		fSNOp = SNOp.ARRAYINDEX;
		fIdx = aIdx;
		aPrev.setNext(this);
		fPrev = aPrev;
		fLocation = aLocation;
		fRF = null;
		fObject = null;
		fMin = 0;
		fMax = 0;
		fType = null;
	}

	public IGObjectWriter(IGObjectWriter aPrev, IGTypeStatic aType, int aMin, int aMax, SourceLocation aLocation) {
		fMin = aMin;
		fMax = aMax;
		fType = aType;
		fLocation = aLocation;
		fSNOp = SNOp.ARRAYRANGE;
		aPrev.setNext(this);
		fPrev = aPrev;
		fRF = null;
		fIdx = 0;
		fObject = null;
	}

	public void setNext(IGObjectWriter aSN) {
		fNext = aSN;
	}

	public void execute(IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {
		if (fPrev != null) {
			fPrev.execute(aRuntime);
			return;
		}

		if (fNext == null) {
			// optimization: no builder necessary
			aRuntime.setObjectValue(fObject, fValue, getLocation());
			return;
		}

		if (fSNOp != SNOp.OBJECT) {
			throw new ZamiaException("ObjectWriter: Internal error. SIGNAL/VAR op expected, found " + fSNOp, getLocation());
		}

		IGStaticValue value = aRuntime.getObjectValue(fObject);

		IGStaticValueBuilder builder = new IGStaticValueBuilder(value, null);

		fNext.executeInternal(builder, aRuntime);

		aRuntime.setObjectValue(fObject, builder.buildConstant(), getLocation());
	}

	private SourceLocation getLocation() {
		return fLocation;
	}

	private void executeInternal(IGStaticValueBuilder aBuilder, IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {

		IGStaticValueBuilder builder = aBuilder;

		switch (fSNOp) {
		case ARRAYINDEX:
			builder = builder.getBuilder(fIdx, null);
			break;

		case ARRAYRANGE:
			builder = builder.getBuilder(fType, fMin, fMax, null);
			break;

		case RECORD:
			builder = builder.getBuilder(fRF, null);
			break;

		default:
			throw new ZamiaException("ObjectWriter: Internal error. ARRAYINDEX/ARRAYRANGE/RECORD op expected, found " + fSNOp);
		}

		if (fNext != null) {
			fNext.executeInternal(builder, aRuntime);
		} else {
			builder.setConstant(fValue);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("OW(");
		switch (fSNOp) {
		case OBJECT:
			buf.append("obj=" + fObject);
			break;
		case ARRAYINDEX:
			buf.append("array idx=" + fIdx);
			break;
		case ARRAYRANGE:
			buf.append("array range=[" + fMin + ":" + fMax + "]");
			break;
		case RECORD:
			buf.append("record fild=" + fRF);
			break;
		}

		if (fNext != null) {
			buf.append(" => ");
			buf.append(fNext.toString());
		}

		if (fValue != null) {
			buf.append("; VALUE: " + fValue);
		}

		buf.append(")");
		return buf.toString();
	}

	public IGStaticValue getValue() {
		return fValue;
	}

	public void setValue(IGStaticValue aValue) {
		fValue = aValue;
	}

	public IGObject getObject() {
		if (fPrev != null) {
			return fPrev.getObject();
		}
		return fObject;
	}

	public void schedule(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGInterpreterRuntimeEnv aRuntime) throws ZamiaException {
		IGObject obj = getObject();

		if (obj.getCat() == IGObjectCat.SIGNAL) {

			aRuntime.scheduleSignalChange(aInertial, aDelay, aReject, this, fLocation);

		} else {
			execute(aRuntime);
		}
	}

}
