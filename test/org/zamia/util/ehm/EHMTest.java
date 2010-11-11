/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.util.ehm;

import org.junit.Test;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.LLFSHashMap;

import java.io.File;
import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Guenter Bartsch
 */
public class EHMTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static int NUM_ENTRIES = 2100000;

	//public final static int NUM_ENTRIES = 420000;
	@Test
	public void testLLFSHM() throws Exception {

		LLFSHashMap hm = new LLFSHashMap(NUM_ENTRIES);

		for (int i = 0; i < NUM_ENTRIES; i++) {
			hm.put(i, NUM_ENTRIES - i);
		}

		assertEquals(NUM_ENTRIES, hm.size());

		int count = 0;
		int as = hm.getAllocedSize();
		for (int i = 0; i < as; i++) {
			if (!hm.getFree(i)) {
				count++;
			}
		}
		assertEquals(NUM_ENTRIES, count);

		Random rnd = new Random();

		for (int i = 0; i < NUM_ENTRIES; i++) {

			int key = rnd.nextInt(NUM_ENTRIES);
			long value = hm.get(key);

			assertEquals(NUM_ENTRIES - key, value);
		}

	}

	@Test
	public void testIterators() throws Exception {

		String dirName = System.getProperty("java.io.tmpdir") + File.separator + "EHMTest";

		File dir = new File(dirName);
		dir.mkdirs();

		String fileName = dirName + File.separator + "index.pages";
		String idxName = dirName + File.separator + "test.idx";

		File file = new File(fileName);
		file.delete();
		File idx = new File(idxName);
		idx.delete();

		EHMPageManager manager = new EHMPageManager(file);

		ExtendibleHashMap ehm = new ExtendibleHashMap(manager, idx);

		long start = System.currentTimeMillis();

		for (int i = 0; i < NUM_ENTRIES; i++) {

			ehm.put(i, Integer.MAX_VALUE - i);

			if (i % 100000 == 0) {

				long stop = System.currentTimeMillis();

				double seconds = (stop - start) / 1000.0;

				logger.info("Stored %d/%d entries in %f seconds => %f stores/sec.", i, NUM_ENTRIES, seconds, i / seconds);

			}
		}

		ehm.flush();
		manager.flush();

		HashSet<Long> keys = new HashSet<Long>();
		EHMIterator it = ehm.keyIterator();
		int count = 0;
		while (it.hasNext()) {
			long key = it.next();

			keys.add(key);

			count++;
		}

		assertEquals(NUM_ENTRIES, count);

		for (int i = 0; i < NUM_ENTRIES; i++) {
			assertTrue("Key " + i + " is missing", keys.contains(new Long(i)));
		}

		HashSet<Long> values = new HashSet<Long>();
		it = ehm.valueIterator();
		count = 0;
		while (it.hasNext()) {
			long v = it.next();

			values.add(v);

			count++;
		}

		assertEquals(NUM_ENTRIES, count);

		for (int i = 0; i < NUM_ENTRIES; i++) {
			assertTrue("Value " + i + " is missing", values.contains(new Long(Integer.MAX_VALUE - i)));
		}
	}

	@Test
	public void test1() throws Exception {

		String dirName = System.getProperty("java.io.tmpdir") + File.separator + "EHMTest";

		File dir = new File(dirName);
		dir.mkdirs();

		String fileName = dirName + File.separator + "index.pages";
		String idxName = dirName + File.separator + "test.idx";

		File file = new File(fileName);
		file.delete();
		File idx = new File(idxName);
		idx.delete();

		EHMPageManager manager = new EHMPageManager(file);

		ExtendibleHashMap ehm = new ExtendibleHashMap(manager, idx);

		long start = System.currentTimeMillis();

		for (int i = 0; i < NUM_ENTRIES; i++) {

			ehm.put(i, Integer.MAX_VALUE - i);

			if (i % 100000 == 0) {

				long stop = System.currentTimeMillis();

				double seconds = (stop - start) / 1000.0;

				logger.info("Stored %d/%d entries in %f seconds => %f stores/sec.", i, NUM_ENTRIES, seconds, i / seconds);

			}
		}

		ehm.flush();
		manager.flush();

		Random rnd = new Random();

		for (int i = 0; i < NUM_ENTRIES; i++) {

			long key = rnd.nextInt(NUM_ENTRIES);

			long v = ehm.get(key);

			assertEquals(Integer.MAX_VALUE - key, v);

			if (i % 100000 == 0) {

				long stop = System.currentTimeMillis();

				double seconds = (stop - start) / 1000.0;

				logger.info("Verified %d/%d entries in %f seconds => %f loads/sec.", i, NUM_ENTRIES, seconds, i / seconds);
			}
		}

		// test persistency

		manager = new EHMPageManager(file);

		ehm = new ExtendibleHashMap(manager, idx);

		for (int i = 0; i < NUM_ENTRIES; i++) {

			long key = rnd.nextInt(NUM_ENTRIES);

			long v = ehm.get(key);

			assertEquals(Integer.MAX_VALUE - key, v);

			if (i % 100000 == 0) {

				long stop = System.currentTimeMillis();

				double seconds = (stop - start) / 1000.0;

				logger.info("Verified %d/%d entries in %f seconds => %f loads/sec.", i, NUM_ENTRIES, seconds, i / seconds);
			}
		}
	}
}
