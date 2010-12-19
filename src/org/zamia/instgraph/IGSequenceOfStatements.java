/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on May 19, 2009
 */
package org.zamia.instgraph;

import java.util.ArrayList;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGItemAccess.AccessType;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.util.HashSetArray;
import org.zamia.zdb.ZDB;

/**
 * 
 * @author Guenter Bartsch
 *
 */
@SuppressWarnings("serial")
public class IGSequenceOfStatements extends IGSequentialStatement {

	private ArrayList<IGSequentialStatement> fStmts = new ArrayList<IGSequentialStatement>();

	public IGSequenceOfStatements(String aId, SourceLocation aSrc, ZDB aZDB) {
		super(aId, aSrc, aZDB);
	}

	public void add(IGSequentialStatement aStmt) {
		fStmts.add(aStmt);
	}

	public void computeAccessedItems(IGItem aFilterItem, AccessType aFilterType, int aDepth, HashSetArray<IGItemAccess> aAccessedItems) {

		int n = fStmts.size();

		for (int i = 0; i < n; i++) {

			IGSequentialStatement stmt = fStmts.get(i);

			stmt.computeAccessedItems(aFilterItem, aFilterType, 0, aAccessedItems);
		}
	}

	public void computeReadSignals(HashSetArray<IGObject> aSignals) {

		HashSetArray<IGItemAccess> accessedItems = new HashSetArray<IGItemAccess>();

		computeAccessedItems(null, AccessType.Read, 0, accessedItems);

		int n = accessedItems.size();
		for (int i = 0; i < n; i++) {

			IGItemAccess access = accessedItems.get(i);

			IGItem item = access.getItem();

			if (item instanceof IGObject) {
				IGObject obj = (IGObject) item;
				if (obj.getCat() == IGObjectCat.SIGNAL) {
					aSignals.add(obj);
				}
			}
		}
	}

	public int getNumStatements() {
		return fStmts.size();
	}

	public IGSequentialStatement getStatement(int aIdx) {
		return fStmts.get(aIdx);
	}

	@Override
	public void generateCode(IGInterpreterCode aCode) throws ZamiaException {
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = getStatement(i);

			stmt.generateCode(aCode);
		}
	}

	@Override
	public IGItem getChild(int aIdx) {
		return fStmts.get(aIdx);
	}

	@Override
	public int getNumChildren() {
		return fStmts.size();
	}

	@Override
	public void dump(int aIndent) {
		int n = getNumStatements();
		for (int i = 0; i < n; i++) {
			IGSequentialStatement stmt = getStatement(i);
			stmt.dump(aIndent + 2);
		}
	}

}
