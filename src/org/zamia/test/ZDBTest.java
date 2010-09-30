/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2008
 */
package org.zamia.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.zamia.ZamiaLogger;
import org.zamia.util.FileUtils;
import org.zamia.util.ZamiaTmpDir;
import org.zamia.zdb.ZDB;

import junit.framework.TestCase;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZDBTest extends TestCase {

	//public final static int NUM_ITERATIONS=250000;
	public final static int NUM_ITERATIONS = 250000;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZDB fZDB;
	
	public final boolean dump = false;

	public void testRandom() throws Exception {

		File tmpDir = ZamiaTmpDir.getTmpDir();
		
		File file = new File(tmpDir.getAbsolutePath()+File.separator+"ZDBTest2");

		FileUtils.deleteDirRecursive(file);

		fZDB = new ZDB(file, null);

		// generate random data, feed it into db

		ArrayList<Long> keys = new ArrayList<Long>(NUM_ITERATIONS);
		HashMap<Long, Integer> data = new HashMap<Long, Integer>(NUM_ITERATIONS);

		Random rg = new Random(42l);
		for (int it = 0; it < NUM_ITERATIONS; it++) {

			Integer obj = new Integer(rg.nextInt());
			long dbid = fZDB.store(obj);
			data.put(dbid, obj);
			keys.add(dbid);

			if (dump) {
				System.out.printf("Object %s got dbid %d\n", obj, dbid);
			}

			if (it % 1000 == 0) {
				int p = it * 100 / NUM_ITERATIONS;
				System.out.printf("Creating DB: %d%% done.\n", p);
			}

		}

		// check and modify at random

		for (int it = 0; it < NUM_ITERATIONS; it++) {

			int iEntry = rg.nextInt(NUM_ITERATIONS);
			long dbid = keys.get(iEntry);

			if (rg.nextBoolean()) {
				// check

				Integer dbObj = (Integer) fZDB.load(dbid);
				Integer memObj = data.get(dbid);

				if (dump) {
					System.out.printf("Checking dbid %d, db said %s, mem said %s\n", dbid, dbObj, memObj);
				}
				
				assertEquals(memObj, dbObj);

			} else {
				// modify

				Integer newValue = new Integer(rg.nextInt());
				data.put(dbid, newValue);
				fZDB.update(dbid, newValue);

				if (dump) {
					System.out.printf("Updated dbid %d to %s\n", dbid, newValue);
				}
				
			}

			if (it % 1000 == 0) {
				int p = it * 100 / NUM_ITERATIONS;
				System.out.printf("Checking DB: %d%% done (%d iterations).\n", p, it + 1);
			}
		}
	}

	public void testZDB() throws Exception {

		File tmpDir = ZamiaTmpDir.getTmpDir();
		
		File file = new File(tmpDir.getAbsolutePath()+File.separator+"ZDBTest");

		fZDB = new ZDB(file, null);

		String abc = "ABC Radio";
		String ww1 = "Westwood One Radio";
		String cbs = "CBS Radio";
		String afn = "AFN Radio";

		long abcID = fZDB.store(abc);
		long ww1ID = fZDB.store(ww1);
		long cbsID = fZDB.store(cbs);
		long afnID = fZDB.store(afn);

		fZDB.putIdx("RadioIdx", "ABC", abcID);
		fZDB.putIdx("RadioIdx", "WW1", ww1ID);
		fZDB.putIdx("RadioIdx", "CBS", cbsID);

		// a small stress test

		long keys[] = new long[1000];

		for (int i = 0; i < 1000; i++) {

			String str = "Object #" + i;
			keys[i] = fZDB.store(str);
		}

		fZDB.shutdown();

		fZDB = new ZDB(file, null);

		String abc2 = (String) fZDB.load(abcID);
		assertEquals(abc, abc2);
		String afn2 = (String) fZDB.load(afnID);
		assertEquals(afn, afn2);
		String cbs2 = (String) fZDB.load(cbsID);
		assertEquals(cbs, cbs2);
		String ww12 = (String) fZDB.load(ww1ID);
		assertEquals(ww1, ww12);

		// idx test

		abc2 = (String) fZDB.load(fZDB.getIdx("RadioIdx", "ABC"));
		assertEquals(abc, abc2);
		cbs2 = (String) fZDB.load(fZDB.getIdx("RadioIdx", "CBS"));
		assertEquals(cbs, cbs2);
		ww12 = (String) fZDB.load(fZDB.getIdx("RadioIdx", "WW1"));
		assertEquals(ww1, ww12);

		for (int i = 999; i >= 0; i--) {

			String str2 = (String) fZDB.load(keys[i]);
			String str = "Object #" + i;

			assertEquals(str, str2);
		}

	}

}
