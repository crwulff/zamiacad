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
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import org.zamia.BuildPath;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.Utils;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.util.FileUtils;
import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.LevelGZIPOutputStream;
import org.zamia.util.ObjectSize;
import org.zamia.util.ZHash;
import org.zamia.util.ehm.EHMIterator;
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

	private static int CACHE_MAX_SIZE = 
			16384; // <- Guenter optimum determined by experiment (does opening zdb per every open project affect this num?)
			//32768;
			//65536;
			//3; // debug purposes

	static int FLUSH_OUT_WHEN = 1512; // this does not seem to have any noticabe effect in the range 100-100000
	
	public static final boolean dump = false;

	// overridable by environment
	public static boolean ENABLE_COMPRESSION = false; 
	public static boolean ENABLE_STATISTICS = false;
	public static boolean ENABLE_LOCKING = true;

	private static final int BUFSIZE = 1 << 16;

	private File fDBDir;

	private HashMap<Long, ZDBCacheEntry> fCache;

	public ZDBCacheEntry fCacheHead, fCacheTail;

	private ZDBPersistentData fPD;

	private File fDataFile, fPDFile, fEHMPagesFile, fOffsetsFile;
	private FileLock fLock = null;
	private long fLength;

	private Object fOwner;

	// statistics:
	private Utils.StatCounter fNumObjectsByClass, fSizeofObjectsByClass;

	// to avoid loops:
	private HashMap<Object, Long> fCurrentlyStoring;

	// EHM support:

	private EHMPageManager fEHMManager;

	private HashMap<String, ExtendibleHashMap> fEHMs;

	private ExtendibleHashMap fOffsets;

	static {
		ENABLE_LOCKING = Utils.getEnvBool("ZAMIA_LOCKING", ENABLE_LOCKING);
		ENABLE_COMPRESSION = Utils.getEnvBool("ZAMIA_COMPRESSION", ENABLE_COMPRESSION);
		ENABLE_STATISTICS = Utils.getEnvBool("ZAMIA_STATISTICS", ENABLE_STATISTICS);
		CACHE_MAX_SIZE = Utils.getEnvInt("ZAMIA_CACHE_SIZE", CACHE_MAX_SIZE);
		FLUSH_OUT_WHEN = Utils.getEnvInt("ZAMIA_WRITER_SIZE", FLUSH_OUT_WHEN);
	}
	
	public ZDB(File aDBDir, Object aOwner) throws ZDBException, FileNotFoundException {

		fDBDir = aDBDir;
		fOwner = aOwner;
		fPDFile = new File(fDBDir.getAbsolutePath() + File.separator + PD_FILENAME);
		fDataFile = new File(fDBDir.getAbsolutePath() + File.separator + DATA_TABLE_FILENAME);
		fEHMPagesFile = new File(fDBDir.getAbsolutePath() + File.separator + EHM_PAGES_FILENAME);
		fOffsetsFile = new File(fDBDir.getAbsolutePath() + File.separator + OFFSETS_FILENAME);

		if (ENABLE_STATISTICS) {
			fNumObjectsByClass = new Utils.StatCounter();
			fSizeofObjectsByClass = new Utils.StatCounter();
		}

		start();
	}

	public Object getOwner() {
		return fOwner;
	}

	private synchronized void start() throws ZDBException, FileNotFoundException {

		mkdirChecked(fDBDir);

		doLock();

		initStructures();

		fEHMManager = new EHMPageManager(fEHMPagesFile);
		fEHMs = new HashMap<String, ExtendibleHashMap>();
		fOffsets = new ExtendibleHashMap(fEHMManager, fOffsetsFile);

		if (!fPD.load(fPDFile)) {
			fPD.clear();
			fOffsets.clear();
		}
	}

	public synchronized void clear() {

		baosToDiskThread.shutdown();

		fEHMManager.clear();
		for (ExtendibleHashMap ehm : fEHMs.values()) {
			ehm.clear();
		}
		fOffsets.clear();

		FileUtils.deleteDirRecursive(fDBDir);
		mkdirChecked(fDBDir);

		initStructures();
	}

	private void initStructures() {

		fCache = new HashMap<Long, ZDBCacheEntry>();
		fCacheHead = null;
		fCacheTail = null;
		fCurrentlyStoring = new HashMap<Object, Long>();

		fPD = new ZDBPersistentData();

		baosToDiskThread = new BaosToDiskThread();
	}

	private void printStats() {

		if (ENABLE_STATISTICS) {
			logger.info("ZDB: statistics");
			logger.info("ZDB: ==========");
			logger.info("ZDB:");

			for (String clsName : fNumObjectsByClass.keySet()) {
				Long num = fNumObjectsByClass.get(clsName);
				int n = num.intValue();
				Long size = fSizeofObjectsByClass.get(clsName);
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

		baosToDiskThread.shutdown();
		baosToDiskThread.sanityCheck();
		baosToDiskThread = new BaosToDiskThread();

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
		baosToDiskThread.shutdown();

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

			fNumObjectsByClass.inc(clsName);
			fSizeofObjectsByClass.inc(clsName, ObjectSize.deepSizeOf(aObj));
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
		} 
		
		fCacheTail = fCacheTail.getPrev();
		if (fCacheTail != null) {
			fCacheTail.setNext(null);
		} else {
			fCacheHead = null;
		}
		
		if (evictedEntry != null && evictedEntry.isDirty()) {
			storeOnDisk(evictedEntry);
		}
		
	}

	//It may happen that the same id entry is evicted twice so that second eviction happens while
	// the first one is still in the queue. Then, we should replace the value cached in the queue with 
	// the new one and increment the counter. It is neccessary to keep object in the cache until last version  
	// is saved to the disk to that when record is not found in the cache it is on the disk for sure. 
//	static class EvictingSet extends HashMap<Long, Pair<Object, Integer>> {
//		public synchronized void put(Long id, Object value) {
//			Pair<Object, Integer> p = put(id, new Pair<Object, Integer>(value, 1));
//			
//			if (p != null) {
//				System.err.println(p.getSecond() + "-ary eviction");
//				put(id, new Pair<Object, Integer>(value, p.getSecond()+1)); // update the value and increment the count
//			}
//		}
//
//		public void remove(Long id) {
//			assert Thread.holdsLock(this);
//			Pair<Object, Integer> p = super.remove(id);
//			if (p.getSecond() != 1) {
//				System.err.println("there was a duplicate eviction! Cool!");
//				p = new Pair<Object, Integer>(p.getFirst(), p.getSecond()-1);
//				put((Long)id, p);
//			}
//		}
//		public Object get(Long key) {
//			Pair<Object, Integer> pair = super.get(key);
//			return pair == null ? null : pair.getFirst();
//		}
//	}

	//A simpler and possibly faster version of eviction set. It assumes that duplicate evictions are unlikely.
	@SuppressWarnings("serial")
	static class EvictingSet extends HashMap<Long, Object> {
		public synchronized void put(long id, Object data) {
			Object o = super.put(id, data);
			if (o != null) throw new AssertionError("Evicting a record while previous eviction is not completed. The code supporting this case was disabled because it is considered unlikely. Check the cache size and let me know if this happens."); 
		}
		
	}

	private synchronized void storeOnDisk(ZDBCacheEntry aEntry) {

		long id = aEntry.getId();

		Object obj = aEntry.getObject();

		baosToDiskThread.addCurrentlyEvicting(aEntry);
		// since serialization can trigger more store operations
		// (some objects may have writeObject() methods which trigger ZDB.store())
		// we first serialize to mem and then write out the whole object in one go

		BAOS baos = new BAOS();

		try {
			
			ObjectOutputStream serializer = new ObjectOutputStream(ENABLE_COMPRESSION ? new LevelGZIPOutputStream(baos, Deflater.BEST_SPEED) : baos);
			serializer.writeObject(obj);
			serializer.close();
			
			//logger.info ("ZDB: File for %d is '%s'", id, dataFile);

			fOffsets.put(baos.id = id, baos.offset = fLength);
			baosToDiskThread.submit(baos);
			fLength += baos.size();
			
		} catch (IOException e) {
			el.logException(e);
		}

	}

	static class BAOS extends ByteArrayOutputStream {
		public long id, offset;
		byte[] getBytes() { return this.buf; }
		BAOS() {super();}
		BAOS(int capacity) {super(capacity);}
	}
	
	BaosToDiskThread baosToDiskThread;
	BAOS END_OF_STREAM = new BAOS(0); // null is dreadfully forbidden in the BlockingQueue
	
	//TODO: a single static thread, started when first ZDB is created and disposed as  
	// the last zdb shuts down, could serve all ZDBs, provided that all data.bin files
	// are located on the same disk (so that concurrence between writers does not make sense).
	class BaosToDiskThread extends Thread {

		private final RandomAccessFile raf;

		private final EvictingSet fCurrentlyEvicting; // We should not (re)load the value until it was saved. This set keeps the save queue.

		BaosToDiskThread() {
			super("BaosToDisk - " + fOwner);
			raf = createNewFile();
			fCurrentlyEvicting = new EvictingSet();
			fLength = fDataFile.length();
			start();
		}

		private RandomAccessFile createNewFile() {
			try {
				return new RandomAccessFile(fDataFile, "rw");
			} catch (FileNotFoundException e) {
				// if we cannot create files in temp directory,
				// then just crushing zamia with NPE is not a that bad thing
				el.logException(e);
				return null;
			}
		}

		public void submit(BAOS baos) {
			try {
				input.put(baos);
			} catch (InterruptedException e) {
				el.logException(e);
			}
		}
		
		public void shutdown() {
			submit(END_OF_STREAM);
			try {
				join();
			} catch (InterruptedException e) { el.logException(e); }
			safeClose(raf);
		}
		
		private BlockingQueue<BAOS> input = new LinkedBlockingQueue<BAOS>(10);
		private BAOS buf = new BAOS(2 * FLUSH_OUT_WHEN);
		private long offset;
		private Collection<Long> ids = new ArrayList<Long>();
		
		private void flush() throws IOException {
			synchronized (raf) {
				assert offset == raf.length() : "specified offset " + offset + " is different from file size " + raf.length();
				raf.seek(offset); // seek can be avoided if we ensure that no seeks were done for other purposes
				raf.write(buf.getBytes(), 0, buf.size());
			}

			synchronized (fCurrentlyEvicting) {
				for (Long id : ids) {
					fCurrentlyEvicting.remove(id);
				}
			}
			ids.clear();
			
			buf.reset();
			if (buf.getBytes().length > 2 * FLUSH_OUT_WHEN)
				buf = new BAOS(2 * FLUSH_OUT_WHEN);
		}
		
		public void run() {
			while (true) {
				try {
					
					BAOS baos = input.take();
					
					if (baos == END_OF_STREAM) {
						if (!ids.isEmpty()) {
							flush();
						}
						break;
					}
					
					if (ids.isEmpty()) {
						offset = baos.offset; // remember location of the first chunk of the series
					}
					
					ids.add(baos.id);
					baos.writeTo(buf);
					if (buf.size() > FLUSH_OUT_WHEN)
						flush();
					
				} catch (Exception e) {
					el.logException(e);
				}
				
			}
			
		}

		public Object readObject(long aOffset) throws ClassNotFoundException, IOException {

			synchronized (raf) {
				raf.seek(aOffset);
				InputStream rafIs = new BufferedInputStream(new InputStream() {
					public int read() throws IOException {
						throw new IOException("not implemneted, sorry"); // tell me if this happens
						//return raf.read();
					}

					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						return raf.read(b, off, len);
					}
				});

				ObjectInputStream in = new ObjectInputStream(ENABLE_COMPRESSION ? new GZIPInputStream(rafIs) : rafIs);
				return in.readObject();
			}
		}

		public void exportTo(DataOutputStream aOutputStream) throws IOException {

			synchronized (raf) {
				raf.seek(0);

				byte buffer[] = new byte[BUFSIZE];

				int n;
				while ((n = raf.read(buffer)) > 0) {
					aOutputStream.write(buffer, 0, n);
				}
			}
		}

		public void importFrom(DataInputStream aInputStream) throws IOException {

			synchronized (raf) {
				raf.seek(0);

				byte buffer[] = new byte[BUFSIZE];

				int n;
				do {
					n = aInputStream.read(buffer);
					if (n > 0) {
						raf.write(buffer, 0, n);
					}
				} while (n > 0);
			}
		}

		public void sanityCheck() {
			assert !Thread.holdsLock(fCurrentlyEvicting);
			assert fCurrentlyEvicting.isEmpty() :
					"After flush completed, " + fCurrentlyEvicting.size() + " entries remains evicting";
		}

		public void addCurrentlyEvicting(ZDBCacheEntry aEntry) {
			fCurrentlyEvicting.put(aEntry.getId(), aEntry.getObject());
		}

		public Object getCurrentlyEvicting(long aId) {
			synchronized (fCurrentlyEvicting) {
				return fCurrentlyEvicting.get(aId);
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

		obj = baosToDiskThread.getCurrentlyEvicting(aId);
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

		try {
			obj = baosToDiskThread.readObject(offset);
		} catch (IOException e) {
			logger.error("ZDB: IOException while reading element %s (file: '%s')", aId, fDataFile.getAbsolutePath());
			el.logException(e);
		} catch (ClassNotFoundException e) {
			el.logException(e);
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
		
		// Why do we introduce deleted flag instead of just removing from the list here?
		// Guenter says that possibly because of (multithreaded) recurrence. 
		// If it is not really needed then cache can be replaced by LinkedHashMap
		ZDBCacheEntry entry = fCache.remove(aId);
		if (entry != null) {
			entry.setDirty(false);
			entry.setDeleted(true); 
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

	public <T> void index(String primaryIdx, String name, T value) {
		long collectionDBID = getIdx(primaryIdx, name);
		if (collectionDBID != 0) {
			HashSetArray<T> collection = (HashSetArray<T>) load(collectionDBID);

			if (!collection.add(value))
				update(collectionDBID, collection);
			
		} else {
			HashSetArray<T> instantiators = new HashSetArray<T>();

			instantiators.add(value);
			collectionDBID = store(instantiators);
			putIdx(primaryIdx, name, collectionDBID);
		}
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
		if (!ENABLE_LOCKING)
			return;

		File lockFile = new File(fDBDir.getAbsolutePath() + File.separator + LOCK_FILENAME);

		try {
			fLock = new RandomAccessFile(lockFile, "rw").getChannel().tryLock();
		} catch (IOException e) {el.logException(e);}
		if (fLock == null)
			throw new ZDBException("ZDB: failed to create a lock file, " + lockFile.getAbsolutePath() + ". Another instance may be running.", lockFile);
		
	}

	private void doUnLock() {
		if (ENABLE_LOCKING) {
			try {
				fLock.release();
			} catch (IOException e) {el.logException(e);}
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

			try {
				baosToDiskThread.exportTo(dout);
			} catch (IOException t) {
				el.logException(t);
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

			try {
				baosToDiskThread.importFrom(din);
			} catch (IOException t) {
				el.logException(t);
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

	/*
	 * Dump a statistical overview of all object stored inside this ZDB instance
	 */

	public void dump() {

		logger.info("ZDB: Analyzing...");
		logger.info("ZDB: ============");
		logger.info("ZDB:");

		logger.info("ZDB:");
		logger.info("ZDB: 1/2: Persistent Objects");
		logger.info("ZDB: -----------------------");
		logger.info("ZDB:");
		
		fPD.dump();
		
		logger.info("ZDB:");
		logger.info("ZDB: 2/2: Data Table");
		logger.info("ZDB: ---------------");
		logger.info("ZDB:");

		
		EHMIterator keys = fOffsets.keyIterator();

		HashMap<String, Integer> numObjectsByClass = new HashMap<String, Integer>();

		HashMap<String, Integer> sizeofObjectsByClass = new HashMap<String, Integer>();

		long count = 0;
		long totalSize = 0;

		//int subCount = 0;

		while (keys.hasNext()) {

			long key = keys.next();

			/*
			 * load the object
			 */

			Object o = load(key);
			
			if (o == null) {
				continue;
			}

			//			if (o instanceof IGSubProgram) {
			//				
			//				Object2Dot o2d = new Object2Dot(o);
			//				
			//				PrintWriter out = null;
			//				try {
			//
			//					out = new PrintWriter(new BufferedWriter(new FileWriter("/tmp/sub"+subCount+".dot")));
			//
			//					o2d.convert(out);
			//
			//				} catch (IOException e) {
			//					e.printStackTrace();
			//
			//				} finally {
			//					if (out != null) {
			//						out.close();
			//					}
			//				}
			//				
			//				subCount++;
			//			}

			/*
			 * serialize it to mem just to get the exact size
			 * of the serialized representation of it
			 */

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream serializer = new ObjectOutputStream(baos);
				serializer.writeObject(o);
				serializer.flush();
			} catch (IOException e) {
				el.logException(e);
			}

			int size = baos.size();
			totalSize += size;

			/*
			 * update stats
			 */

			String clsName = o.getClass().toString();

			Integer cnt = numObjectsByClass.get(clsName);
			int n = 0;
			if (cnt != null) {
				n = cnt.intValue();
			}
			n++;
			numObjectsByClass.put(clsName, n);

			Integer s = sizeofObjectsByClass.get(clsName);
			int s2 = size;
			if (s != null) {
				s2 += s.intValue();
			}
			sizeofObjectsByClass.put(clsName, s2);

			count++;
		}

		for (String clsName : numObjectsByClass.keySet()) {
			Integer num = numObjectsByClass.get(clsName);
			long n = num.intValue();
			Integer size = sizeofObjectsByClass.get(clsName);
			long s = size.intValue();
			logger.info("ZDB: %6d, %9d KB, %9d avg bytes, %3d%% %s", n, s / 1024, s / n, s * 100 / totalSize, clsName);
		}

		logger.info("ZDB: Total size %s KB in %d objects", totalSize / 1024, count);

	}

	public static ObjectInputStream openObjectInputStream(File file) throws IOException {
		InputStream fis = new BufferedInputStream(new FileInputStream(file));
		return new ObjectInputStream(
				ZDB.ENABLE_COMPRESSION ? new BufferedInputStream(new GZIPInputStream(fis)) : fis);
	}

	public static ObjectOutputStream openObjectOutputStream(File file) throws IOException {
		OutputStream fs = new BufferedOutputStream(new FileOutputStream(file));
		OutputStream os = ZDB.ENABLE_COMPRESSION ? new BufferedOutputStream(new LevelGZIPOutputStream(fs, Deflater.BEST_SPEED)) : fs;
		return new ObjectOutputStream(os);
	}

	public static void safeClose(Closeable out) {
		try {
			out.close();
		} catch (IOException e) {
			el.logException(e);
		}
	}

}
