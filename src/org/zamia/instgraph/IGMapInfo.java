/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 22, 2010
 */
package org.zamia.instgraph;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGMapInfoOp.MapInfoOp;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.util.HashSetArray;


/**
 * Simplified mapping information to speed up refsearch and sim / annotate
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public class IGMapInfo implements Serializable {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private final long fInstDBID;

	private final long fActualDBID, fFormalDBID;

	private final ArrayList<IGMapInfoOp> fFormalOps, fActualOps;

	private final HashSetArray<IGItemAccess> fActualItems;

	private final HashSetArray<IGItemAccess> fFormalItems;

	public IGMapInfo(long aInstDBID, IGMapping aMapping) {

		fInstDBID = aInstDBID;

		IGOperation formal = aMapping.getFormal();
		IGOperation actual = aMapping.getActual();

		OIDir dir = OIDir.NONE;
		try {
			dir = formal.getDirection();
		} catch (ZamiaException e) {
			el.logException(e);
		}

		boolean leftSide = dir == OIDir.OUT || dir == OIDir.INOUT;

		fActualItems = new HashSetArray<IGItemAccess>(1);
		actual.computeAccessedItems(leftSide, null, null, 0, fActualItems);

		fFormalItems = new HashSetArray<IGItemAccess>(1);
		formal.computeAccessedItems(!leftSide, null, null, 0, fFormalItems);

		fFormalOps = new ArrayList<IGMapInfoOp>(1);
		fActualOps = new ArrayList<IGMapInfoOp>(1);

		fActualDBID = generateOps(aMapping.getActual(), fActualOps);
		fFormalDBID = generateOps(aMapping.getFormal(), fFormalOps);
	}

	private Long getStatic(IGOperation aOp) {

		try {
			if (aOp instanceof IGStaticValue) {
				IGStaticValue sv = (IGStaticValue) aOp;

				return sv.getOrd();

			} else if (aOp instanceof IGOperationObject) {
				IGOperationObject oo = (IGOperationObject) aOp;
				IGObject obj = oo.getObject();

				if (obj.getCat() != IGObjectCat.CONSTANT) {
					return null;
				}

				IGOperation iv = obj.getInitialValue();
				return getStatic(iv);
			} else if (aOp instanceof IGOperationLiteral) {

				if (!(aOp instanceof IGOperationLiteral.INT)) {
					return null;
				}
				
				BigInteger num = ((IGOperationLiteral.INT)aOp).getNum();
				return num.longValue();
			}
		} catch (Throwable t) {

		}

		return null;
	}

	private long generateOps(IGOperation aOp, List<IGMapInfoOp> aOps) {

		if (aOp instanceof IGOperationObject) {

			IGOperationObject oo = (IGOperationObject) aOp;

			IGObject obj = oo.getObject();

			return obj.getDBID();

		} else if (aOp instanceof IGOperationIndex) {

			IGOperationIndex oi = (IGOperationIndex) aOp;

			Long idx = getStatic(oi.getIndex());

			if (idx == null) {

				logger.warn("IGMapInfo: Warning: non-static idx in mapping: %s", idx);

				return 0;
			}

			aOps.add(new IGMapInfoOp(MapInfoOp.INDEX, (int) idx.longValue(), (int) idx.longValue(), null));

			return generateOps(oi.getOperand(), aOps);

		} else if (aOp instanceof IGOperationRange) {

			try {
				IGOperationRange or = (IGOperationRange) aOp;

				IGOperation range = or.getRange();

				if (!(range instanceof IGRange)) {
					return 0;
				}
				
				IGRange r = (IGRange) range;
				
				Long left = getStatic(r.getLeft());
				
				if (left == null) {
					return 0;
				}
				
				Long right = getStatic(r.getLeft());
				
				if (right == null) {
					return 0;
				}
				
				IGOperation ascending = r.getAscending();
				
				if (!(ascending instanceof IGStaticValue)) {
					return 0;
				}
				
				boolean a = ((IGStaticValue) ascending).isTrue();
				
				int min = (int) (a ? left.longValue() : right.longValue());
				int max = (int) (!a ? left.longValue() : right.longValue());

				aOps.add(new IGMapInfoOp(MapInfoOp.INDEX, min, max, null));

				return generateOps(or.getOperand(), aOps);
				
			} catch (Throwable t) {
			}

			return 0;

		} else if (aOp instanceof IGOperationRecordField) {

			IGOperationRecordField orf = (IGOperationRecordField) aOp;

			IGRecordField rf = orf.getRecordField();

			aOps.add(new IGMapInfoOp(MapInfoOp.RECORDFIELD, 0, 0, rf.getId()));

			return generateOps(orf.getOperand(), aOps);
		}
		return 0;
	}

	public long getInstDBID() {
		return fInstDBID;
	}

	public long getActualDBID() {
		return fActualDBID;
	}

	public long getFormalDBID() {
		return fFormalDBID;
	}

	public int getNumFormalItems() {
		return fFormalItems.size();
	}

	public IGItemAccess getFormalItem(int aIdx) {
		return fFormalItems.get(aIdx);
	}

	public int getNumActualItems() {
		return fActualItems.size();
	}

	public IGItemAccess getActualItem(int aIdx) {
		return fActualItems.get(aIdx);
	}
}
