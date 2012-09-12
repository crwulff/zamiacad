/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.zdb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashMapArray;
import org.zamia.zdb.ZDB.ZDBInputStream;


/**
 * Persistent part of ZDB (essentially the indexes and offsets)
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZDBPersistentData {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static String OFFSETS_FILENAME = "offsets.zdb";

	public final static int CURRENT_VERSION = 90701; // 0.9.7 rev 1

	private long fCurId = 1;

	private int fVersion = CURRENT_VERSION;

	private HashMap<String, HashMapArray<String, Long>> fIndices = new HashMap<String, HashMapArray<String, Long>>();

	private HashMap<String, Object> fNamedObjects = new HashMap<String, Object>();

	void clear() {

		fCurId = 1;

		fIndices = new HashMap<String, HashMapArray<String, Long>>();

		fNamedObjects = new HashMap<String, Object>();

	}

	void save(File aPDFile) {

		try {

			ObjectOutputStream oos = ZDB.openObjectOutputStream(aPDFile);
			try {
			
				oos.writeInt(CURRENT_VERSION);

				oos.writeLong(fCurId);

				/*
				 * save indices 
				 */

				int n = fIndices.size();

				oos.writeInt(n);

				for (String id : fIndices.keySet()) {

					oos.writeUTF(id);

					HashMapArray<String, Long> idx = fIndices.get(id);

					int m = idx.size();

					oos.writeInt(m);

					for (String key : idx.keySet()) {

						Long value = idx.get(key);

						oos.writeUTF(key);
						oos.writeLong(value.longValue());
					}
				}

				/*
				 * save named objects 
				 */

				n = fNamedObjects.size();

				oos.writeInt(n);

				for (String id : fNamedObjects.keySet()) {

					oos.writeUTF(id);

					Object obj = fNamedObjects.get(id);

					oos.writeObject(obj);
				}
				
			} finally {
				oos.close();
			}

		} catch (Throwable t) {
			el.logException(t);
		}

	}

	boolean load(File aPDFile, ZDB aZDB) {

		if (aPDFile.exists() && aPDFile.canRead()) {
			try {
				
				ObjectInputStream ois = aZDB.new ZDBInputStream(ZDB.openInputStream(aPDFile));
 
				try {
				
					int v = ois.readInt();
					if (v != CURRENT_VERSION) {
						logger.error("ZDB: Wrong version: was expecting V %d, found V %d.", ZDBPersistentData.CURRENT_VERSION, v);
						return false;
					}

					fCurId = ois.readLong();

					/*
					 * load indices
					 */

					int n = ois.readInt();

					for (int i = 0; i < n; i++) {

						String id = ois.readUTF();

						int m = ois.readInt();

						HashMapArray<String, Long> idx = new HashMapArray<String, Long>(m);

						fIndices.put(id, idx);

						for (int j = 0; j < m; j++) {

							String key = ois.readUTF();

							long value = ois.readLong();

							idx.put(key, value);
						}
					}

					/*
					 * load named objects
					 */

					n = ois.readInt();

					for (int i = 0; i < n; i++) {

						String id = ois.readUTF();

						Object obj = ois.readObject();

						fNamedObjects.put(id, obj);
					}

					return true;
				} finally {
					ois.close();
				}
			} catch (Throwable e) {
				el.logException(e);
			}
		}

		return false;
	}

	long getNextId() {
		return fCurId++;
	}

	HashMapArray<String, Long> getIdx(String aIdxName) {
		return fIndices.get(aIdxName);
	}

	void putIdx(String aIdxName, HashMapArray<String, Long> aIdx) {
		fIndices.put(aIdxName, aIdx);
	}

	void delIdx(String aIdxName) {
		fIndices.remove(aIdxName);
	}

	Object getNamedObject(String aObjName) {
		return fNamedObjects.get(aObjName);
	}

	void createNamedObject(String aObjName, Object aObj) {
		fNamedObjects.put(aObjName, aObj);
	}

	void delNamedObject(String aObjName) {
		fNamedObjects.remove(aObjName);
	}

	public int getVersion() {
		return fVersion;
	}

	void dump() {

		Iterator<String> keys = fNamedObjects.keySet().iterator();
		
		HashMap<String, Integer> numObjectsByClass = new HashMap<String, Integer>();

		HashMap<String, Integer> sizeofObjectsByClass = new HashMap<String, Integer>();

		long count = 0;
		long totalSize = 0;

		while (keys.hasNext()) {

			String key = keys.next();

			Object o = fNamedObjects.get(key);
			
			if (o == null) {
				continue;
			}

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

}
