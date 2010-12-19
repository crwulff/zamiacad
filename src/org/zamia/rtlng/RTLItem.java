/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 17, 2010
 */
package org.zamia.rtlng;

import java.io.Serializable;

import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.zdb.ZDB;
import org.zamia.zdb.ZDBIIDSaver;

/**
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class RTLItem implements Serializable, ZDBIIDSaver {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private String fZPrjID;

	protected long fDBID;

	private long fSFDBID;

	private int fLine;

	private int fCol;

	private transient ZDB fZDB;

	private transient SourceLocation fLocation;

	public RTLItem(SourceLocation aLocation, ZDB aZDB) {

		if (aZDB != null) {
			fZDB = aZDB;
			ZamiaProject zprj = (ZamiaProject) aZDB.getOwner();
			fZPrjID = zprj.getId();
			setSourceLocation(aLocation);
		}
	}

	public long save(RTLItem aItem) {
		if (aItem == null)
			return 0;
		long dbid = aItem.getDBID();
		if (dbid == 0) {
			dbid = getZDB().store(aItem);
		}
		return dbid;
	}

	public long storeOrUpdate() {
		if (fDBID == 0) {
			save(this);
		} else {
			getZDB().update(fDBID, this);
		}
		return fDBID;
	}

	public long store() {
		return save(this);
	}

	public void setDBID(long aId) {
		if (fDBID != 0) {
			logger.warn("IGItem: warning: re-storing %s DBID: %d => %d", this, fDBID, aId);
		}
		fDBID = aId;
	}

	public void setZDB(ZDB aZDB) {
		fZDB = aZDB;
	}

	public ZDB getZDB() {

		if (fZDB == null) {
			ZamiaProject zprj = ZamiaProject.lookupProject(fZPrjID);
			if (zprj != null) {
				fZDB = zprj.getZDB();
			}
		}

		return fZDB;
	}

	public ZamiaProject getZPrj() {
		ZDB db = getZDB();
		return (ZamiaProject) db.getOwner();
	}

	protected RTLManager getRTLManager() {
		return getZPrj().getRTLM();
	}


	public long getDBID() {
		return fDBID;
	}

	public long getSFDBID() {
		return fSFDBID;
	}

	public int getLine() {
		return fLine;
	}

	public int getCol() {
		return fCol;
	}

	private long getOrCreateSFHID(SourceFile aSF) {

		String path = aSF.getAbsolutePath();

		long id = fZDB.getIdx("SFIdx", path);

		if (id != 0) {
			return id;
		}

		id = fZDB.store(aSF);
		fZDB.putIdx("SFIdx", path, id);

		return id;
	}

	private SourceFile getSF(long aSfhid) {
		return (SourceFile) getZDB().load(aSfhid);
	}

	public SourceLocation computeSourceLocation() {

		if (fLocation != null) {
			return fLocation;
		}

		long sfDBID = getSFDBID();

		if (sfDBID == 0) {
			return null;
		}

		SourceFile sf = getSF(sfDBID);

		fLocation = new SourceLocation(sf, getLine(), getCol());

		return fLocation;
	}

	public void setSourceLocation(SourceLocation aLocation) {
		if (aLocation != null) {
			fSFDBID = getOrCreateSFHID(aLocation.fSF);
			fLine = aLocation.fLine;
			fCol = aLocation.fCol;
			fLocation = aLocation;
		}
	}

}
