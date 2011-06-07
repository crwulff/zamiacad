/* 
 * Copyright 2009, 2010, 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 20, 2009
 */
package org.zamia.analysis.ig;

import java.io.PrintStream;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ig.IGRSType.TypeCat;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGRSResult {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private HashMapArray<PathName, IGRSNode> fNodeMap;
	
	private HashMap<Long, IGRSType> fTypeCache;

	private final IGRSNode fRoot;

	private final Toplevel fToplevel;

	private final ZamiaProject fZPrj;

	private IGManager fIGM;

	private IGRSType fBitType;

	public IGRSResult(Toplevel aToplevel, ZamiaProject aZPrj) throws ZamiaException {
		fToplevel = aToplevel;
		fZPrj = aZPrj;
		fIGM = fZPrj.getIGM();

		IGModule module = fIGM.findModule(aToplevel);
		if (module == null) {
			throw new ZamiaException("Couldn't find toplevel " + aToplevel);
		}

		fRoot = new IGRSNode(aToplevel.getDUUID().getId(), null, module.computeSourceLocation(), new PathName(""), this);
		fNodeMap = new HashMapArray<PathName, IGRSNode>();
		fTypeCache = new HashMap<Long, IGRSType>();
		
		fBitType = new IGRSType(TypeCat.BIT, null);

	}

	public IGRSNode getOrCreateNode(PathName aPath, IGItem aItem) {

		PathName path = aPath;

		if (path.endsInNull()) {
			path = path.getParent();
		}

		IGRSNode node = fNodeMap.get(path);
		if (node != null) {
			return node;
		}

		int n = path.getNumSegments();

		if (n == 0) {
			return fRoot;
		}

		node = getOrCreateNode(path.getParent(), null);

		if (node == null) {
			return null;
		}

		IGItem item = aItem;
		if (item == null) {
			item = fIGM.findItem(fToplevel, path.append((String) null));
			if (item == null) {
				logger.error("VisualGraph: Have no IGItem corresponding to %s", aPath);
				return null;
			}
		}
		
		String lastSegment = path.getSegment(n - 1);
		node = node.getOrCreateChild(lastSegment, item.getDBID(), item.computeSourceLocation(), path);

		return node;
	}

	private IGRSType getCachedType(IGTypeStatic aType) {

		long dbid = aType.getDBID();

		return fTypeCache.get(dbid);
	}

	private void setCachedType(IGTypeStatic aType, IGRSType aT) {
		fTypeCache.put(aType.getDBID(), aT);
	}

	IGRSType synthesizeType(IGType aType) throws ZamiaException {

		SourceLocation location = aType.computeSourceLocation();

		if (!(aType instanceof IGTypeStatic)) {
			throw new ZamiaException("Type " + aType + " is not static => not synthesizable", location);
		}

		IGTypeStatic type = (IGTypeStatic) aType;

		IGRSType t = getCachedType(type);
		if (t != null) {
			return t;
		}

		switch (type.getCat()) {
		case ARRAY:

			IGRSType et = synthesizeType(type.getStaticElementType(null));

			IGTypeStatic it = type.getStaticIndexType(null);

			t = new IGRSType(TypeCat.ARRAY, location);

			t.setArrayParams(et, (int) it.getStaticLeft(location).getOrd(), it.isAscending(), (int) it.getStaticRight(location).getOrd());

			break;

		case RECORD:
			t = new IGRSType(TypeCat.RECORD, location);

			int n = type.getNumRecordFields(location);
			for (int i = 0; i < n; i++) {

				IGRecordField rf = type.getRecordField(i, location);

				t.addField(rf.getId(), synthesizeType(rf.getType()));

			}

			break;

		case INTEGER:
			t = new IGRSType(TypeCat.INTEGER, location);
			break;

		default:
			if (!type.isBit() && !type.isBool() && !isStdLogic(type)) {
				throw new ZamiaException("Type " + type + " is not synthesizable", location);
			}

			t = fBitType;
		}

		setCachedType(type, t);

		return t;
	}

	private boolean isStdLogic(IGTypeStatic aType) {
		// FIXME: check values ?
		return aType.isCharEnum();
	}

	
	public void dump(int aI, PrintStream aOut) {
		fRoot.dump(aI, aOut);
	}

	public int countNodes() {
		return fRoot.countNodes();
	}

	public int countConns() {
		return fRoot.countConns();
	}

	public IGRSNode getRoot() {
		return fRoot;
	}

	
}
