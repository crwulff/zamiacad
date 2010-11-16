/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 13, 2010
 */
package org.zamia.instgraph.interpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;

/**
 * IGObjectDrivers form a tree of drivers for composite type objects
 * 
 * They can be pushed on the stack an interacted with to e.g. update the object's
 * current value completely or partially.
 * 
 * This class implements the simplest object semantics suitable for constants
 * and variables and is meant to be sub-classed to implement advanced
 * semantics e.g. for signals and signal parameters.
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGObjectDriver implements Serializable {

	private final IGObjectDriver fParent;

	private ArrayList<IGObjectDriver> fArrayElementDrivers;

	private HashMap<String, IGObjectDriver> fRecordFieldDrivers;

	private IGStaticValue fValue = null;

	private final IGTypeStatic fDeclaredType;

	private IGTypeStatic fCurrentType;

	private final IGObjectCat fCat;

	private IGObjectDriver fMappedTo = null;

	private OIDir fDir;

	// for logging/debugging purposes only:
	private final String fId;

	private static int counter = 0;

	private final int fCnt = counter++;

	public IGObjectDriver(String aId, OIDir aDir, IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation) throws ZamiaException {
		fId = aId;
		fDir = aDir;
		fCat = aCat;
		fDeclaredType = aType;
		fCurrentType = aType;
		fParent = aParent;

		if (fDeclaredType.isArray()) {

			fArrayElementDrivers = new ArrayList<IGObjectDriver>();

			adaptArraySize(aLocation);
			
		} else if (fDeclaredType.isRecord()) {

			int n = fDeclaredType.getNumRecordFields(aLocation);

			fRecordFieldDrivers = new HashMap<String, IGObjectDriver>(n);

			for (int i = 0; i < n; i++) {

				IGRecordField rf = fDeclaredType.getRecordField(i, aLocation);
				IGTypeStatic rft = fDeclaredType.getStaticRecordFieldType(i);
				String rfid = rf.getId();

				fRecordFieldDrivers.put(rfid, new IGObjectDriver(fId + "." + rfid, fDir, fCat, this, rft, aLocation));

			}
		}
	}

	public void setValue(IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {
		if (fMappedTo != null) {
			fMappedTo.setValue(aValue, aLocation);
			return;
		}

		setValueInternal(aValue, aLocation);

		// update parent

		if (fParent != null) {
			fParent.updateValue(aLocation);
		}
	}

	private void adaptArraySize(SourceLocation aLocation) throws ZamiaException {

		IGTypeStatic idxT = fCurrentType.getStaticIndexType(aLocation);
		if (!fCurrentType.isUnconstrained()) {
			int card = (int) idxT.computeCardinality(aLocation);
			int l = (int) idxT.getStaticLeft(aLocation).getOrd();
			int r = (int) idxT.getStaticRight(aLocation).getOrd();
			boolean a = idxT.getStaticAscending().isTrue();
			int off = a ? l : r;

			IGTypeStatic elementType = fCurrentType.getStaticElementType(aLocation);

			int n = fArrayElementDrivers.size();
			while (n < card) {
				int idx = off + n;

				fArrayElementDrivers.add(new IGObjectDriver(fId + "(" + idx + ")", fDir, fCat, this, elementType, aLocation));
				n++;
			}
		}

	}

	protected void setValueInternal(IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {
		fValue = aValue;

		// update children

		if (fDeclaredType.isArray()) {

			IGTypeStatic t = aValue.getStaticType();
			if (!t.isArray()) {
				throw new ZamiaException("IGObjectDriver: Internal error: tried to assign non-array value to an array.", aLocation);
			}

			if (t.isUnconstrained()) {
				throw new ZamiaException("IGObjectDriver: Internal error: tried to assign unconstrained array value to an array.", aLocation);
			}

			if (fDeclaredType.isUnconstrained()) {
				fCurrentType = t;
			}
			adaptArraySize(aLocation);

			fValue = aValue;

			IGTypeStatic idxT = t.getStaticIndexType(null);
			int l = (int) idxT.getStaticLeft(aLocation).getOrd();
			int r = (int) idxT.getStaticRight(aLocation).getOrd();
			boolean a = idxT.getStaticAscending().isTrue();
			int off = a ? l : r;
			int c = (int) idxT.computeCardinality(null);

			// so we have to do an index shift?
			if (!fDeclaredType.isUnconstrained() && fDeclaredType.getCat() != IGType.TypeCat.FILE && fDeclaredType.getCat() != IGType.TypeCat.ACCESS) {

				IGTypeStatic declaredIdxT = fDeclaredType.getStaticIndexType(null);
				int declaredIdxLeft = (int) declaredIdxT.getStaticLeft(aLocation).getOrd();
				int declaredIdxRight = (int) declaredIdxT.getStaticRight(aLocation).getOrd();
				boolean declaredIdxAscending = declaredIdxT.getStaticAscending().isTrue();
				int declaredOffset = declaredIdxAscending ? declaredIdxLeft : declaredIdxRight;
				int oC = (int) declaredIdxT.computeCardinality(null);

				if (oC != c) {
					throw new ZamiaException("Interpreter: error: tried to assign object from incompatible array value " + aValue + ": " + fDeclaredType + " vs " + t, aLocation);
				}

				if (l != declaredIdxLeft || r != declaredIdxRight || a != declaredIdxAscending) {
					// yes, time to shift the index

					IGStaticValueBuilder builder = new IGStaticValueBuilder(fDeclaredType, null, aLocation);

					for (int i = 0; i < c; i++) {
						IGStaticValue v = aValue.getValue(i + off, aLocation);
						builder.set(i + declaredOffset, v, aLocation);
					}

					fValue = builder.buildConstant();
					
					t = fValue.getStaticType();
					idxT = t.getStaticIndexType(null);
					l = (int) idxT.getStaticLeft(aLocation).getOrd();
					r = (int) idxT.getStaticRight(aLocation).getOrd();
					a = idxT.getStaticAscending().isTrue();
					off = a ? l : r;
					c = (int) idxT.computeCardinality(null);

				}
			}

			// update children

			for (int i = 0; i < c; i++) {
				IGStaticValue v = fValue.getValue(i + off, aLocation);
				fArrayElementDrivers.get(i).setValueInternal(v, aLocation);
			}

		} else if (fDeclaredType.isRecord()) {

			int n = fDeclaredType.getNumRecordFields(null);

			for (int i = 0; i < n; i++) {

				IGRecordField rf = fDeclaredType.getRecordField(i, null);
				String rfid = rf.getId();

				IGStaticValue v = fValue.getRecordFieldValue(rfid, aLocation);

				fRecordFieldDrivers.get(rfid).setValueInternal(v, aLocation);

			}

		}
	}

	public IGStaticValue getValue() {
		if (fMappedTo != null) {
			return fMappedTo.getValue();
		}
		return fValue;
	}

	protected void updateValue(SourceLocation aLocation) throws ZamiaException {

		// children's value have changed, so we have to update fValue accordingly

		if (fDeclaredType.isArray()) {

			IGTypeStatic t = fCurrentType;
			if (!t.isArray()) {
				throw new ZamiaException("IGObjectDriver: Internal error.", aLocation);
			}

			if (t.isUnconstrained()) {
				throw new ZamiaException("IGObjectDriver: Internal error.", aLocation);
			}

			IGTypeStatic idxT = t.getStaticIndexType(null);
			int l = (int) idxT.getStaticLeft(aLocation).getOrd();
			int r = (int) idxT.getStaticRight(aLocation).getOrd();
			boolean a = idxT.getStaticAscending().isTrue();
			int off = a ? l : r;
			int c = (int) idxT.computeCardinality(null);

			IGStaticValueBuilder builder = new IGStaticValueBuilder(t, fId, aLocation);

			for (int i = 0; i < c; i++) {
				IGStaticValue v = fArrayElementDrivers.get(i).getValue();
				
				builder.set(i + off, v, aLocation);
			}
			
			fValue = builder.buildConstant();;

		} else if (fDeclaredType.isRecord()) {

			int n = fDeclaredType.getNumRecordFields(null);

			IGStaticValueBuilder builder = new IGStaticValueBuilder(fDeclaredType, fId, aLocation);

			for (int i = 0; i < n; i++) {

				IGRecordField rf = fDeclaredType.getRecordField(i, null);
				String rfid = rf.getId();

				IGStaticValue v = fRecordFieldDrivers.get(rfid).getValue();
				
				builder.set(rf, v, aLocation);
			}

			fValue = builder.buildConstant();

		}
	}

	@Override
	public String toString() {
		if (fMappedTo != null) {
			return "DRIVER@" + fCnt + "[" + fId + "]=>" + fMappedTo;
		}
		if (fValue != null) {
			return "DRIVER@" + fCnt + "[" + fId + "]=" + fValue;
		}
		return "DRIVER@" + fCnt + "[" + fId + "]";
	}

	// to be overriden in simulator subclass
	public boolean isEvent() {
		if (fMappedTo != null) {
			return fMappedTo.isEvent();
		}

		//		// get signal from objectItem
		//		IGObject signal;
		//		IGItem objectItem = (IGItem) getZDB().load(fObjectItemDBID);
		//		if (objectItem instanceof IGOperationObject) {
		//			signal = ((IGOperationObject) objectItem).getObject();
		//		} else if (objectItem instanceof IGObject) {
		//			signal = (IGObject) objectItem;
		//		} else
		//			throw new ZamiaException(getClass().getSimpleName() + ": cannot extract signal (" + IGObject.class.getSimpleName() + ") from " + objectItem);
		//
		//		// check signal event
		//		boolean isChanged = aRuntime.isChanged(signal, computeSourceLocation());

		return false;
	}

	// to be overriden in simulator subclass
	public boolean isActive() {
		if (fMappedTo != null) {
			return fMappedTo.isActive();
		}
		return false;
	}

	public IGObjectDriver getArrayElementDriver(int aIdx, SourceLocation aLocation) throws ZamiaException {
		if (fMappedTo != null) {
			return fMappedTo.getArrayElementDriver(aIdx, aLocation);
		}

		IGTypeStatic idxT = fCurrentType.getStaticIndexType(aLocation);

		int l = (int) idxT.getStaticLeft(aLocation).getOrd();
		int r = (int) idxT.getStaticRight(aLocation).getOrd();
		boolean a = idxT.getStaticAscending().isTrue();
		int off = a ? l : r;

		return fArrayElementDrivers.get(aIdx - off);
	}

	public void schedule(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGStaticValue aValue, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation)
			throws ZamiaException {

		if (fCat == IGObjectCat.SIGNAL) {

			aRuntime.scheduleSignalChange(aInertial, aDelay, aReject, this, aLocation);

		} else {

			setValue(aValue, aLocation);

		}
	}

	public IGObjectCat getCat() {
		if (fMappedTo != null) {
			return fMappedTo.getCat();
		}

		return fCat;
	}

	public void map(IGObjectDriver aActual, SourceLocation aLocation) throws ZamiaException {

		fMappedTo = aActual;

		//		switch (fCat) {
		//		case SIGNAL:
		//			fMappedTo = aActual;
		//			break;
		//
		//		case CONSTANT:
		//		case FILE:
		//		case VARIABLE:
		//
		//			switch (aOp) {
		//			case CALL_ENTRY:
		//
		//				if (fDir != OIDir.OUT) {
		//					setValue(aActual.getValue(), aLocation);
		//				}
		//				break;
		//
		//			case CALL_EXIT:
		//
		//				if (fDir != OIDir.IN) {
		//					aActual.setValue(getValue(), aLocation);
		//				}
		//				break;
		//
		//			default:
		//				throw new ZamiaException("Illegal mapping.", aLocation);
		//			}
		//
		//		}

	}

	public IGTypeStatic getCurrentType() {
		if (fMappedTo != null) {
			return fMappedTo.getCurrentType();
		}
		return fCurrentType != null ? fCurrentType : fDeclaredType;
	}

	private IGObjectDriver(String aId, OIDir aDir, IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, ArrayList<IGObjectDriver> aChildren, SourceLocation aLocation) throws ZamiaException {
		fId = aId;
		fDir = aDir;
		fCat = aCat;
		fDeclaredType = aType;
		fCurrentType = aType;
		fParent = aParent;

		if (!fDeclaredType.isArray() || fDeclaredType.isUnconstrained()) {
			throw new ZamiaException ("IGObjectDriver: Internal error.", aLocation);
		}
		
		fArrayElementDrivers = aChildren;
		updateValue(aLocation);
	}

	
	public IGObjectDriver getRange(IGStaticValue aR, IGTypeStatic aType, SourceLocation aLocation) throws ZamiaException {
		ArrayList<IGObjectDriver> rangeDrivers = new ArrayList<IGObjectDriver>();
		
		int left = (int) aR.getLeft().getOrd();
		int right = (int) aR.getRight().getOrd();
		boolean ascending = aR.getAscending().isTrue();

		if (ascending) {
			for (int i = left; i <= right; i++) {
				rangeDrivers.add(getArrayElementDriver(i, aLocation));
			}
		} else {
			for (int i = left; i >= right; i--) {
				rangeDrivers.add(getArrayElementDriver(i, aLocation));
			}
		}

		String id = fId+"("+aR+")";
		
		return new IGObjectDriver (id, fDir, fCat, fParent, aType, rangeDrivers, aLocation);
	}

	public IGObjectDriver getAlias(IGTypeStatic aT, SourceLocation aLocation) throws ZamiaException {

		if (!aT.isArray() || aT.isUnconstrained()) {
			return this;
		}
		
		ArrayList<IGObjectDriver> rangeDrivers = new ArrayList<IGObjectDriver>();

		IGTypeStatic idxT = aT.getStaticIndexType(aLocation);

		int left = (int) idxT.getStaticLeft(aLocation).getOrd();
		int right = (int) idxT.getStaticRight(aLocation).getOrd();
		boolean ascending = idxT.getStaticAscending().isTrue();

		if (ascending) {
			for (int i = left; i <= right; i++) {
				rangeDrivers.add(getArrayElementDriver(i, aLocation));
			}
		} else {
			for (int i = left; i >= right; i--) {
				rangeDrivers.add(getArrayElementDriver(i, aLocation));
			}
		}

		String id = "&["+toString()+"]{"+aT+"}";
		
		return new IGObjectDriver (id, fDir, fCat, fParent, aT, rangeDrivers, aLocation);
	}

}
