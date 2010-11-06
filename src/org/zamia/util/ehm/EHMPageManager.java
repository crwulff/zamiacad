/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 27, 2010
 */
package org.zamia.util.ehm;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.util.LLFSHashMap;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class EHMPageManager {

	public final static int PAGE_ENTRIES = 512;

	public final static int CACHE_MAX_SIZE = 32768;

	//	public final static int PAGE_ENTRIES = 4;
	//	public final static int CACHE_MAX_SIZE = 4;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private long fNumPages;

	private File fFile;

	private HashMap<Long, EHMCacheEntry> fCache;

	private EHMCacheEntry fCacheHead, fCacheTail;

	public EHMPageManager(File aFile) {

		fFile = aFile;

		fNumPages = 0;

		start();

		if (fFile.exists() && fFile.canRead()) {

			DataInputStream in = null;

			try {

				in = new DataInputStream(new FileInputStream(fFile));

				fNumPages = in.readLong();

			} catch (IOException e) {
				el.logException(e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public synchronized void start() {
		fCache = new HashMap<Long, EHMCacheEntry>();
		fCacheHead = null;
		fCacheTail = null;
	}
	
	public synchronized void clear() {
		fFile.delete();
		start();
	}

	public long alloc() {
		return fNumPages++;
	}

	private synchronized void storeInMem(long aId, LLFSHashMap aPage, boolean aMarkDirty) {
		// put it into memory cache

		EHMCacheEntry entry = new EHMCacheEntry(aId, aPage, aMarkDirty);

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

		EHMCacheEntry evictedEntry = fCache.remove(id);

		if (evictedEntry == null) {
			logger.error("EHM: Internal error: id %d was not part of cache.\n", id);
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
		}
	}

	private long idToOffset(long aId) {
		
		long bytesPerPage = PAGE_ENTRIES * 16 + 4;
		
		long offset = 8 + aId * bytesPerPage;
		return offset;
	}

	private synchronized void storeOnDisk(EHMCacheEntry aEntry) {

		long id = aEntry.getId();

		LLFSHashMap page = aEntry.getPage();

		long offset = idToOffset(id);

		RandomAccessFile out = null;
		try {

			out = new RandomAccessFile(fFile, "rw");

			out.seek(offset);

			int nEntries = 0;

			byte buf[] = new byte[PAGE_ENTRIES * 16 + 4];

			int byteIdx = 0;

			int s = page.size();
			
			buf[byteIdx++] = (byte) ((int) (s >>> 24) & 0xFF);
			buf[byteIdx++] = (byte) ((int) (s >>> 16) & 0xFF);
			buf[byteIdx++] = (byte) ((int) (s >>> 8) & 0xFF);
			buf[byteIdx++] = (byte) ((int) (s >>> 0) & 0xFF);
			
			int n = page.getAllocedSize();
			for (int i = 0; i<n; i++) {
				
				if (page.getFree(i)) {
					continue;
				}
				nEntries++;
				
				long k = page.getKey(i);

				buf[byteIdx++] = (byte) ((int) (k >>> 56) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 48) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 40) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 32) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 24) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 16) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 8) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (k >>> 0) & 0xFF);

				long v = page.getValue(i);

				buf[byteIdx++] = (byte) ((int) (v >>> 56) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 48) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 40) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 32) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 24) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 16) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 8) & 0xFF);
				buf[byteIdx++] = (byte) ((int) (v >>> 0) & 0xFF);
			}

			out.write(buf);

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

	public synchronized LLFSHashMap load(long aId) {

		if (aId < 0) {
			return null;
		}

		// look for that object in memory cache

		EHMCacheEntry entry = fCache.get(aId);

		//logger.debug ("EHM: loading %d", aId);

		LLFSHashMap page = null;
		if (entry != null) {

			page = entry.getPage();

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
			return page;
		}

		// we didn't find it in memory, so load it from disk

		long offset = idToOffset(aId);

		if (offset < 0) {
			// invalid id
			return null;
		}

		DataInputStream in = null;
		try {
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(fFile)));
			long lo = in.skip(offset);
			if (lo != offset) {
				throw new IOException("Failed to seek.");
			}

			page = new LLFSHashMap(PAGE_ENTRIES);
			
			int nEntries = in.readInt();

			for (int i = 0; i < nEntries; i++) {

				long key = in.readLong();
				long value = in.readLong();

				page.put(key, value);
			}

		} catch (IOException e) {
			logger.error("EHM: IOException while reading element %s (file: '%s')", aId, fFile.getAbsolutePath());
			el.logException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
		}

		if (page != null) {
			storeInMem(aId, page, false);
		}

		return page;
	}

	public synchronized void store(long aId, LLFSHashMap aPage) {

		EHMCacheEntry entry = fCache.get(aId);

		if (entry != null) {
			entry.setDirty(true);
			entry.setPage(aPage);

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

			storeInMem(aId, aPage, true);

		}
	}

	public synchronized void flush() {
		logger.info("EHMPageManager: flush(): evicting memory cache...");

		while (fCacheHead != null) {
			evict();
		}

		RandomAccessFile ras = null;

		try {

			ras = new RandomAccessFile(fFile, "rw");

			ras.seek(0);
			ras.writeLong(fNumPages);

		} catch (IOException e) {
			el.logException(e);
		} finally {
			if (ras != null) {
				try {
					ras.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		logger.info("EHMPageManager: flush(): done.");
	}

}
