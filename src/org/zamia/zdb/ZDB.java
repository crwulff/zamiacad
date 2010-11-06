/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.zdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import org.zamia.BuildPath;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.util.FileUtils;
import org.zamia.util.HashMapArray;
import org.zamia.util.LevelGZIPOutputStream;
import org.zamia.util.ObjectSize;
import org.zamia.util.ZHash;
import org.zamia.util.ehm.EHMPageManager;
import org.zamia.util.ehm.ExtendibleHashMap;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZDB {

	public final static String LOCK_FILENAME = "lock";

	public final static String PD_FILENAME = "pd.bin";

	public final static String DATA_TABLE_FILENAME = "data.bin";

	public final static String EHM_PAGES_FILENAME = "ehm.pages";

	public final static String OFFSETS_FILENAME = "offsets.ehm";

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private static final int CACHE_MAX_SIZE = 16384;

	//private static final int CACHE_MAX_SIZE = 32768;

	//private static final int CACHE_MAX_SIZE = 65536;

	//private static final int CACHE_MAX_SIZE = 8; // debug purposes

	public static final boolean dump = false;

	public static final boolean ENABLE_STATISTICS = false;

	public static final boolean ENABLE_COMPRESSION = true;

	public static final boolean ENABLE_LOCKING = true;

	private static final int BUFSIZE = 1 << 16;

	private File fDBDir;

	private HashMap<Long, ZDBCacheEntry> fCache;

	private ZDBCacheEntry fCacheHead, fCacheTail;

	private ZDBPersistentData fPD;

	private File fLockFile, fDataFile, fPDFile, fEHMPagesFile, fOffsetsFile;

	private Object fOwner;

	// statistics:

	private HashMap<String, Integer> fNumObjectsByClass;

	private HashMap<String, Integer> fSizeofObjectsByClass;

	// to avoid loops:
	private HashMap<Object, Long> fCurrentlyStoring;

	private HashMap<Long, Object> fCurrentlyEvicting;

	private boolean fLockingEnabled = false;

	// EHM support:

	private EHMPageManager fEHMManager;

	private HashMap<String, ExtendibleHashMap> fEHMs;

	private ExtendibleHashMap fOffsets;

	public ZDB(File aDBDir, Object aOwner) throws ZDBException {

		if (ENABLE_LOCKING) {
			String override = System.getenv("ZAMIA_LOCKING");
			if (override == null || !"disabled".equalsIgnoreCase(override)) {
				fLockingEnabled = true;
			}
		}

		fDBDir = aDBDir;
		fOwner = aOwner;
		fPDFile = new File(fDBDir.getAbsolutePath() + File.separator + PD_FILENAME);
		fLockFile = new File(fDBDir.getAbsolutePath() + File.separator + LOCK_FILENAME);
		fDataFile = new File(fDBDir.getAbsolutePath() + File.separator + DATA_TABLE_FILENAME);
		fEHMPagesFile = new File(fDBDir.getAbsolutePath() + File.separator + EHM_PAGES_FILENAME);
		fOffsetsFile = new File(fDBDir.getAbsolutePath() + File.separator + OFFSETS_FILENAME);

		if (ENABLE_STATISTICS) {
			fNumObjectsByClass = new HashMap<String, Integer>();
			fSizeofObjectsByClass = new HashMap<String, Integer>();
		}

		start();
	}

	public Object getOwner() {
		return fOwner;
	}

	private synchronized void start() throws ZDBException {

		mkdirChecked(fDBDir);

		doLock();

		fCache = new HashMap<Long, ZDBCacheEntry>();
		fCacheHead = null;
		fCacheTail = null;
		fCurrentlyStoring = new HashMap<Object, Long>();
		fCurrentlyEvicting = new HashMap<Long, Object>();

		fEHMManager = new EHMPageManager(fEHMPagesFile);
		fEHMs = new HashMap<String, ExtendibleHashMap>();
		fOffsets = new ExtendibleHashMap(fEHMManager, fOffsetsFile);

		fPD = new ZDBPersistentData();

		if (!fPD.load(fPDFile)) {
			fPD.clear();
			fOffsets.clear();
		}

	}

	public synchronized void clear() {
		fCache = new HashMap<Long, ZDBCacheEntry>();
		fCacheHead = null;
		fCacheTail = null;
		fCurrentlyStoring = new HashMap<Object, Long>();
		fCurrentlyEvicting = new HashMap<Long, Object>();

		fPD = new ZDBPersistentData();

		fEHMManager.clear();
		for (ExtendibleHashMap ehm : fEHMs.values()) {
			ehm.clear();
		}
		fOffsets.clear();

		FileUtils.deleteDirRecursive(fDBDir);
		mkdirChecked(fDBDir);
		try {
			doLock();
		} catch (ZDBException e) {
			el.logException(e);
		}
	}

	private void printStats() {

		if (ENABLE_STATISTICS) {
			logger.info("ZDB: statistics");
			logger.info("ZDB: ==========");
			logger.info("ZDB:");

			for (String clsName : fNumObjectsByClass.keySet()) {
				Integer num = fNumObjectsByClass.get(clsName);
				int n = num.intValue();
				Integer size = fSizeofObjectsByClass.get(clsName);
				int s = size.intValue();
				logger.info("ZDB: %6d, %9d Bytes, %9d AvgBytes, %s", n, s, s / n, clsName);
			}
		}
	}

	public synchronized void flush() {
		logger.info("ZDB: flush(): evicting memory cache...");

		while (fCacheHead != null) {
			evict();
		}

		logger.info("ZDB: flush(): writing EHM pages...");

		fEHMManager.flush();

		logger.info("ZDB: flush(): writing EHM nodes...");

		fOffsets.flush();
		for (ExtendibleHashMap ehm : fEHMs.values()) {
			ehm.flush();
		}

		logger.info("ZDB: flush(): writing indices and persistent objects...");

		fPD.save(fPDFile);

		long size = FileUtils.du(fDBDir) / (1024 * 1024);

		logger.info("ZDB: flush(): done. Current DB size: %d MB.", size);
	}

	public synchronized void shutdown() {

		if (ENABLE_STATISTICS) {
			printStats();
		}

		flush();

		doUnLock();
	}

	public synchronized void update(long aId, Object aObject) {

		if (dump) {
			logger.info("ZDB: update: %d => %s", aId, aObject);
		}

		ZDBCacheEntry entry = fCache.get(aId);

		if (entry != null) {
			entry.setDirty(true);
			entry.setObject(aObject);

			if (entry.getPrev() != null) {
				if (entry.getNext() != null) {
					entry.getNext().setPrev(entry.getPrev());
				} else {
					fCacheTail = entry.getPrev();
				}
				entry.getPrev().setNext(entry.getNext());

				entry.setPrev(null);

				entry.setNext(fCacheHead);
				fCacheHead.setPrev(entry);

				fCacheHead = entry;
			}

		} else {
			storeInMem(aId, aObject, true);
		}
	}

	private int counter = 0;

	public synchronized long store(Object aObj) {

		Long cId = fCurrentlyStoring.get(aObj);
		if (cId != null) {
			return cId.longValue();
		}

		if (ENABLE_STATISTICS) {

			String clsName = aObj.getClass().toString();

			counter++;
			if (counter % 32 == 0) {
				if (clsName.contains("IGType")) {
					//					System.out.println("ZDB: IGType. foobar.");
				}
			}

			Integer cnt = fNumObjectsByClass.get(clsName);
			int n = 0;
			if (cnt != null) {
				n = cnt.intValue();
			}
			n++;
			fNumObjectsByClass.put(clsName, n);

			Integer size = fSizeofObjectsByClass.get(clsName);
			int s = 0;
			if (size != null) {
				s = size.intValue();
			}
			s += ObjectSize.deepSizeOf(aObj);
			fSizeofObjectsByClass.put(clsName, s);
		}

		if (dump) {

			long size = ObjectSize.deepSizeOf(aObj) / 1024;

			logger.info("ZDB: storing %s, size is %d KBytes", aObj, size);
			if (size > 10) {
				logger.info("ZDB: large object!");
			}
		}

		long id = 0;

		if (aObj instanceof ZDBIIDSaver) {
			id = ((ZDBIIDSaver) aObj).getDBID();
			if (id == 0) {
				id = fPD.getNextId();
			}

			//((ZDBIIDSaver) aObj).prepareDBStore();
		} else {
			id = fPD.getNextId();
		}

		fCurrentlyStoring.put(aObj, id);

		if (aObj instanceof ZDBIIDSaver) {
			((ZDBIIDSaver) aObj).setDBID(id);
		}

		storeInMem(id, aObj, true);

		fCurrentlyStoring.remove(aObj);
		return id;
	}

	/**
	 * Difference between this and store() is that this function will serialize
	 * the object to disk right away
	 * 
	 * @param aObj
	 * @return
	 */

	public synchronized long storeNow(Object aObj) {
		long dbid = store(aObj);

		ZDBCacheEntry entry = fCache.get(dbid);
		if (entry != null) {
			storeOnDisk(entry);
			entry.setDirty(false);
		}

		return dbid;
	}

	private synchronized void storeInMem(long aId, Object aObj, boolean aMarkDirty) {

		// already in memory cache?

		ZDBCacheEntry entry = fCache.get(aId);
		if (entry != null) {

			if (aMarkDirty) {
				entry.setDirty(true);
			}

			return;
		}

		// put it into memory cache

		entry = new ZDBCacheEntry(aId, aObj, aMarkDirty);

		fCache.put(aId, entry);

		entry.setNext(fCacheHead);
		if (fCacheHead != null) {
			fCacheHead.setPrev(entry);
		}
		fCacheHead = entry;
		if (fCacheTail == null) {
			fCacheTail = entry;
		}

		int nCachedItems = fCache.size();

		if (nCachedItems > CACHE_MAX_SIZE) {
			evict();
		}
	}

	private synchronized void evict() {

		long id = fCacheTail.getId();

		ZDBCacheEntry evictedEntry = fCache.remove(id);

		if (evictedEntry == null) {
			if (!fCacheTail.isDeleted()) {
				System.out.printf("ZDB: Internal error: id %d was not part of cache.\n", id);
			}
		} else {
			fCurrentlyEvicting.put(id, evictedEntry.getObject());
		}

		fCacheTail = fCacheTail.getPrev();
		if (fCacheTail != null) {
			fCacheTail.setNext(null);
		} else {
			fCacheHead = null;
		}

		if (evictedEntry != null) {
			if (evictedEntry.isDirty()) {
				storeOnDisk(evictedEntry);
			}
			fCurrentlyEvicting.remove(id);
		}
	}

	private synchronized void storeOnDisk(ZDBCacheEntry aEntry) {

		long id = aEntry.getId();

		// since serialization can trigger more store operations
		// (some objects may have writeObject() methods which trigger ZDB.store())
		// we first serialize to mem and then write out the whole object in one go

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream serializer = new ObjectOutputStream(baos);
			serializer.writeObject(aEntry.getObject());
			serializer.flush();
		} catch (IOException e) {
			el.logException(e);
		}

		//logger.info ("ZDB: File for %d is '%s'", id, dataFile);

		long offset = fDataFile.length();

		fOffsets.put(id, offset);

		OutputStream out = null;
		try {

			if (ENABLE_COMPRESSION) {
				out = new LevelGZIPOutputStream(new FileOutputStream(fDataFile, true), Deflater.BEST_SPEED);
			} else {
				out = new FileOutputStream(fDataFile, true);
			}

			baos.writeTo(out);
		} catch (IOException e1) {
			el.logException(e1);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
		}
	}

	public synchronized Object load(long aId) {

		if (aId == 0) {
			return null;
		}

		// look for that object in memory cache

		ZDBCacheEntry entry = fCache.get(aId);

		//logger.debug ("ZDB: loading %d", aId);

		Object obj = null;
		if (entry != null) {

			obj = entry.getObject();

			//logger.debug ("ZDB: loading %d, was in ram: %s", aId, obj);

			if (entry.getPrev() != null) {
				if (entry.getNext() != null) {
					entry.getNext().setPrev(entry.getPrev());
				} else {
					fCacheTail = entry.getPrev();
				}
				entry.getPrev().setNext(entry.getNext());

				entry.setPrev(null);

				entry.setNext(fCacheHead);
				fCacheHead.setPrev(entry);

				fCacheHead = entry;
			}
			return obj;
		}

		obj = fCurrentlyEvicting.get(aId);
		if (obj != null) {
			//logger.debug ("ZDB: loading %d, was in evicting: %s", aId, obj);
			return obj;
		}

		// we didn't find it in memory, so load it from disk

		long offset = fOffsets.get(aId);

		if (offset < 0) {
			// invalid id
			return null;
		}

		ObjectInputStream in = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fDataFile);
			fis.skip(offset);

			if (ENABLE_COMPRESSION) {
				in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(fis)));
			} else {
				in = new ObjectInputStream(new BufferedInputStream(fis));
			}
			obj = in.readObject();
			//logger.debug ("ZDB: loading %d, was on disk: %s", aId, obj);
		} catch (IOException e) {
			logger.error("ZDB: IOException while reading element %s (file: '%s')", aId, fDataFile.getAbsolutePath());
			el.logException(e);
		} catch (ClassNotFoundException e) {
			el.logException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
		}

		if (obj != null) {

			if (obj instanceof ZDBIIDSaver) {
				((ZDBIIDSaver) obj).setZDB(this);
			}

			storeInMem(aId, obj, false);
		}

		return obj;
	}

	public synchronized void delete(long aId) {
		if (aId == 0) {
			return;
		}
		ZDBCacheEntry entry = fCache.get(aId);
		if (entry != null) {
			entry.setDirty(false);
			entry.setDeleted(true);
			fCache.remove(aId);
		}
		fOffsets.delete(aId);
	}

	/*
	 * 
	 * EHM based index support
	 * 
	 */

	public ExtendibleHashMap getOrCreateEHM(String aId) {

		ExtendibleHashMap ehm = fEHMs.get(aId);

		if (ehm == null) {

			File file = new File(fDBDir.getAbsolutePath() + File.separator + ZHash.encodeZ(aId) + ".ehm");

			ehm = new ExtendibleHashMap(fEHMManager, file);

			fEHMs.put(aId, ehm);
		}

		return ehm;
	}

	/*
	 * 
	 * String key based index support
	 * 
	 */

	public synchronized void putIdx(String aIdx, String aKey, long aId) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);

		if (idx == null) {
			idx = new HashMapArray<String, Long>();
			fPD.putIdx(aIdx, idx);
		}

		idx.put(aKey, aId);
	}

	public void putIdxObj(String aIdx, String aId, Object aObj) {
		long id = store(aObj);
		putIdx(aIdx, aId, id);
	}

	public synchronized boolean isIdxKey(String aIdx, String aKey) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);
		if (idx == null)
			return false;
		return idx.containsKey(aKey);
	}

	public synchronized long getIdx(String aIdx, String aKey) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);
		if (idx == null)
			return 0;

		Long id = idx.get(aKey);
		if (id == null) {
			return 0;
		}
		return id.longValue();
	}

	public Object getIdxObj(String aIdx, String aKey) {
		long id = getIdx(aIdx, aKey);
		if (id == 0) {
			return null;
		}
		return load(id);
	}

	public synchronized int getIdxNumEntries(String aIdx) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);
		if (idx == null)
			return 0;
		return idx.size();
	}

	public synchronized Object getIdxObj(String aIdx, int aI) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);
		if (idx == null) {
			return null;
		}

		Long idL = idx.get(aI);
		if (idL == null) {
			return null;
		}

		long id = idL.longValue();
		return load(id);
	}

	public synchronized void delIdx(String aIdx, String aKey) {
		HashMapArray<String, Long> idx = fPD.getIdx(aIdx);
		if (idx == null)
			return;

		idx.remove(aKey);
	}

	public void delIdxObj(String aIdx, String aKey) {
		long id = getIdx(aIdx, aKey);
		if (id == 0) {
			return;
		}
		delete(id);
		delIdx(aIdx, aKey);
	}

	public synchronized void delAllIdx(String aIdx) {
		fPD.delIdx(aIdx);
	}

	/*
	 * 
	 * Persistent, named objects
	 * 
	 */

	public synchronized void createNamedObject(String aObjName, Object aObj) {
		fPD.createNamedObject(aObjName, aObj);
	}

	public synchronized Object getNamedObject(String aObjName) {
		return fPD.getNamedObject(aObjName);
	}

	public synchronized void delNamedObject(String aObjName) {
		fPD.delNamedObject(aObjName);
	}

	private void mkdirChecked(File aDir) {
		if (!aDir.exists() && !aDir.mkdirs()) {
			logger.error("ZDB: Fatal: Couldn't create dir %s.", aDir.getAbsolutePath());
			System.exit(1);
		}
		FileUtils.fixDirPerms(aDir);
	}

	private void doLock() throws ZDBException {
		if (!fLockingEnabled) {
			return;
		}

		if (fLockFile.exists()) {
			logger.error("ZDB: Lockfile exists, another instance may be running.");
			logger.error("ZDB: If you are sure no other instance is running, delete '%s'.", fLockFile.getAbsolutePath());

			throw new ZDBException("ZDB: Lockfile " + fLockFile.getAbsolutePath() + " exists, another instance may be running.", fLockFile);
		}

		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(fLockFile);
			fout.write(42);
		} catch (IOException e) {
			el.logException(e);
			System.exit(1);
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
		}
		if (!fLockFile.exists()) {
			logger.error("ZDB: Failed to create lockfile '%s'.", fLockFile.getAbsolutePath());
			throw new ZDBException("ZDB: Failed to create lockfile " + fLockFile.getAbsolutePath(), fLockFile);
		}
		fLockFile.deleteOnExit();
	}

	private void doUnLock() {
		if (fLockingEnabled) {
			if (fLockFile.exists()) {
				fLockFile.delete();
			}
		}
	}

	public synchronized void exportToFile(String aFileName, ZamiaProject aZPrj) {

		flush();

		BufferedOutputStream out = null;
		try {

			out = new BufferedOutputStream(new FileOutputStream(aFileName));

			DataOutputStream dout = new DataOutputStream(out);

			/*
			 * write header
			 */

			dout.writeInt(0xCADDBEDA); // marker
			dout.writeInt(0); // version number

			/*
			 * write BuildPath.txt
			 */

			if (aZPrj != null) {

				BuildPath bp = aZPrj.getBuildPath();

				SourceFile bpsf = bp != null ? bp.getSourceFile() : null;

				File bpf = bpsf != null ? bpsf.getFile() : null;

				if (bpf != null && bpf.exists() && bpf.canRead()) {

					long length = bpf.length();

					dout.writeLong(length);

					long realLength = 0;

					BufferedInputStream in = null;
					try {
						in = new BufferedInputStream(new FileInputStream(bpf));

						byte buffer[] = new byte[BUFSIZE];

						int n = 0;
						while ((n = in.read(buffer)) > 0) {
							dout.write(buffer, 0, n);
							realLength += n;
						}

						if (realLength != length) {
							logger.error("ZDB: export() failed, bp length reported by length() was %d, in reality i got %d bytes.", length, realLength);
						}

					} catch (Throwable t) {
						el.logException(t);
					} finally {
						if (in != null) {
							in.close();
						}
					}

				} else {
					dout.writeLong(0);
				}

			} else {
				dout.writeLong(0);
			}

			dout.flush();

			/*
			 * write persistent data
			 */

			logger.info("ZDB: export(): exporting indices and persistent objects...");

			ObjectOutputStream oos = new ObjectOutputStream(dout);

			oos.writeObject(fPD);

			oos.flush();

			logger.info("ZDB: export(): exporting indices and persistent objects...done.");

			/*
			 * copy data file
			 */

			logger.info("ZDB: export(): exporting data objects...");

			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(fDataFile));

				byte buffer[] = new byte[BUFSIZE];

				int n = 0;
				while ((n = in.read(buffer)) > 0) {
					dout.write(buffer, 0, n);
				}

			} catch (Throwable t) {
				el.logException(t);
			} finally {
				if (in != null) {
					in.close();
				}
			}

			logger.info("ZDB: export(): exporting data objects... done");

			dout.flush();

		} catch (Throwable t) {
			el.logException(t);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		logger.info("ZDB: export(): finished.");
	}

	public synchronized void importFromFile(String aFileName, ZamiaProject aZPrj) {

		flush();

		BufferedInputStream in = null;
		try {

			in = new BufferedInputStream(new FileInputStream(aFileName));

			DataInputStream din = new DataInputStream(in);

			/*
			 * check header
			 */

			int marker = din.readInt();
			if (marker != 0xCADDBEDA) {
				logger.error("ZDB: import(): %s has wrong magic header.", aFileName);
				return;
			}

			int version = din.readInt();
			if (version != 0) {
				logger.error("ZDB: import(): %s has wrong version: %d", aFileName, version);
				return;
			}

			/*
			 * restore BuildPath.txt
			 */

			BuildPath bp = aZPrj.getBuildPath();

			SourceFile bpsf = bp != null ? bp.getSourceFile() : null;

			File bpf = bpsf != null ? bpsf.getFile() : null;

			if (bpf == null || !bpf.exists() || !bpf.canWrite()) {
				logger.error("ZDB: import(): cannot write BuildPath.txt");
				return;
			}

			long length = din.readLong();

			BufferedOutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(bpf));

				byte buffer[] = new byte[BUFSIZE];

				long total = 0;
				int n = 0;

				while (total < length) {

					int l = (int) (length - total);
					if (l > BUFSIZE) {
						l = BUFSIZE;
					}

					n = din.read(buffer, 0, l);

					if (n < 0) {
						break;
					}

					out.write(buffer, 0, n);

					total += n;

				}

			} catch (Throwable t) {
				el.logException(t);
			} finally {
				if (in != null) {
					out.close();
				}
			}

			/*
			 * write persistent data
			 */

			logger.info("ZDB: import(): importing indices and persistent objects...");

			ObjectInputStream ois = new ObjectInputStream(din);

			fPD = (ZDBPersistentData) ois.readObject();

			logger.info("ZDB: import(): importing indices and persistent objects...done.");

			/*
			 * copy data file
			 */

			logger.info("ZDB: import(): importing data objects...");

			BufferedOutputStream bout = null;
			try {
				bout = new BufferedOutputStream(new FileOutputStream(fDataFile));

				byte buffer[] = new byte[BUFSIZE];

				int n = 0;
				do {
					n = din.read(buffer);
					if (n > 0) {
						bout.write(buffer, 0, n);
					}
				} while (n > 0);

			} catch (Throwable t) {
				el.logException(t);
			} finally {
				if (bout != null) {
					bout.close();
				}
			}

			logger.info("ZDB: import(): importing data objects... done");

		} catch (Throwable t) {
			el.logException(t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		logger.info("ZDB: import(): finished.");
	}

}
