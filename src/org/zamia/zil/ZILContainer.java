/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 18, 2008
 */
package org.zamia.zil;

import org.zamia.DUManager;
import org.zamia.ZamiaException;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DesignUnit;
import org.zamia.vhdl.ast.DUUID.LUType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public abstract class ZILContainer extends ZILObject implements ZILIContainer {

	private HashMapArray<String, ZILIObject> fItems;
	private HashSetArray<String> fImporters;
	private DUManager fDUM;

	public ZILContainer(String aId, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		super(aId, aType, aContainer, aSrc);
		fItems = new HashMapArray<String, ZILIObject>();
	}

	public void addEntityImporter(String aLibId, DUManager aDUM) {
		if (fImporters == null) {
			fImporters = new HashSetArray<String>();
		}
		fImporters.add(aLibId);
		fDUM = aDUM;
	}

	public void add(ZILIObject aObject, ASTObject aSrc) throws ZamiaException {

		String id = aObject.getId();

		ZILIObject oldObject = fItems.get(id);

		ZILIObject obj = aObject;

		if (oldObject != null) {
			if (oldObject instanceof ZILSubProgram) {

				ZILSubProgramSet sps = new ZILSubProgramSet(id, obj.getType(), obj.getContainer());

				sps.add((ZILSubProgram) oldObject);

				if (!(obj instanceof ZILSubProgram)) {
					throw new ZamiaException("This scope already contains an item named " + id + " which is a subprogram.", aSrc);
				}

				sps.add((ZILSubProgram) obj);

				obj = sps;
			} else if (oldObject instanceof ZILSubProgramSet) {

				ZILSubProgramSet sps = (ZILSubProgramSet) oldObject;

				if (obj instanceof ZILSubProgramSet) {

					obj = sps.merge((ZILSubProgramSet) obj);

					obj = sps;

				} else if (obj instanceof ZILSubProgram) {
					sps.add((ZILSubProgram) obj);

					obj = sps;

				} else {
					throw new ZamiaException("This scope already contains an item named " + id + " which is a subprogram.", aSrc);
				}

				// FIXME ?
				// } else {
				// if (!(obj instanceof ZILLibraryStub))
				// throw new ZamiaException
				// ("This scope already contains an item named "+id+".", aSrc);
			}
		}

		fItems.put(id, obj);
	}

	public ZILIObject resolve(String aId) {
		ZILIObject item = fItems.get(aId);
		if (item != null) {
			return item;
		}
		ZILIContainer container = getContainer();
		if (container != null) {
			return container.resolve(aId);
		}

		if (fDUM != null) {
			int n = fImporters.size();
			for (int i = 0; i < n; i++) {

				String libId = fImporters.get(i);

				DUUID duuid = new DUUID(LUType.Entity, libId, aId, null);
				
				try {
					DesignUnit du = fDUM.getDU(duuid);
					
					if (du != null) {
						return new ZILDUUID(aId, duuid);
					}		
				} catch (ZamiaException e) {
					el.logZamiaException(e);
				}
			}
		}

		return null;
	}

	public int getNumItems() {
		return fItems.size();
	}

	public ZILIObject getItem(int aIdx) {
		return fItems.get(aIdx);
	}

}
