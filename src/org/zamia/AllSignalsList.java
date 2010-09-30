/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 24, 2010
 */
package org.zamia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zamia.instgraph.IGInstMapInfo;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.util.PathName;
import org.zamia.util.SimpleRegexp;


/**
 * 
 * @author Guenter Bartsch
 *
 */
public class AllSignalsList implements Runnable {

	public final static int MAX_NUM_SIGNALS = 10000000;

	public final static String IDX_FILENAME = "all_signals.lst";

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	private ZamiaProject fZPrj;

	private Thread fThread;

	private Lock fLock;

	private boolean fCanceled = false;

	private Condition fNewJobCond;

	private File fDBDir;

	private File fIdxFile;

	private IGManager fIGM;

	private int fNumSignals;

	public AllSignalsList(File aDBDir, ZamiaProject aZPrj) {

		fZPrj = aZPrj;
		fIGM = aZPrj.getIGM();

		fDBDir = aDBDir;

		fIdxFile = new File(fDBDir.getAbsolutePath() + File.separator + IDX_FILENAME);

		fLock = new ReentrantLock();

		fNewJobCond = fLock.newCondition();

		fThread = new Thread(this, fZPrj.getId() + ": all signals list");
		fThread.start();
	}

	public void shutdown() {
		await();

		//		try {
		//			fThread.join();
		//		} catch (InterruptedException e) {
		//			el.logException(e);
		//		}
	}

	public void cancelIndex() {
		fCanceled = true;
		fLock.lock();

		try {

			fCanceled = false;

		} finally {
			fLock.unlock();
		}

	}

	public void await() {
		fLock.lock();

		try {

		} finally {
			fLock.unlock();
		}
	}

	public void updateIndex() {

		fLock.lock();

		try {

			cancelIndex();

			fNewJobCond.signalAll();

		} finally {
			fLock.unlock();
		}

	}

	@Override
	public void run() {

		fLock.lock();

		try {

			while (true) {

				fNewJobCond.await();

				PrintWriter out = null;

				try {

					out = new PrintWriter(new BufferedWriter(new FileWriter(fIdxFile)));

					BuildPath bp = fZPrj.getBuildPath();
					if (bp == null) {
						continue;
					}

					IGManager igm = fZPrj.getIGM();

					fNumSignals = 0;

					int n = bp.getNumToplevels();
					for (int i = 0; i < n; i++) {

						Toplevel tl = bp.getToplevel(i);

						IGModule module = igm.findModule(tl);

						if (module != null) {
							generateIndex(out, module.getDBID(), tl, new PathName(""));
						}
					}
				} catch (Throwable t) {
					el.logException(t);
				} finally {
					if (out != null) {
						out.close();
					}
				}
			}
		} catch (InterruptedException e) {
			el.logException(e);
		} finally {
			fLock.unlock();
		}
	}

	public List<String> searchRegex(String aRegexp, int aLimit) {

		ArrayList<String> result = new ArrayList<String>();

		String regex = SimpleRegexp.convert(aRegexp);
		Pattern pattern = Pattern.compile(regex);

		fLock.lock();

		try {

			BufferedReader in = null;

			try {

				in = new BufferedReader(new FileReader(fIdxFile));

				String line;

				while ((line = in.readLine()) != null) {

					Matcher m = pattern.matcher(line);
					if (m.matches()) {
						result.add(line);
						if (result.size() >= aLimit) {
							break;
						}
					}

				}

			} catch (IOException e) {
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

		} finally {
			fLock.unlock();
		}

		return result;
	}

	public File getIdxFile() {
		return fIdxFile;
	}

	private void generateIndex(PrintWriter aOut, long aDBID, Toplevel aTL, PathName aPathName) {

		if (fNumSignals > MAX_NUM_SIGNALS) {
			logger.error("AllSignalsList: Too many signals. Aborting index generation.");
			return;
		}

		Iterator<String> it = fIGM.getSignalIdIterator(aDBID);

		if (it != null) {
			while (it.hasNext() && fNumSignals < MAX_NUM_SIGNALS) {

				if (fCanceled) {
					return;
				}

				PathName path = aPathName.append(it.next());

				ToplevelPath tlp = new ToplevelPath(aTL, path);

				String str = tlp.toString();
				
				fNumSignals++;

				aOut.println(str);
			}
		}

		Iterator<IGInstMapInfo> it2 = fIGM.getInstIterator(aDBID);
		if (it2 != null) {
			while (it2.hasNext()) {

				if (fCanceled) {
					return;
				}

				IGInstMapInfo info = it2.next();

				PathName path = aPathName.append(info.getLabel());

				generateIndex(aOut, info.getDBID(), aTL, path);
			}
		}
	}

}
