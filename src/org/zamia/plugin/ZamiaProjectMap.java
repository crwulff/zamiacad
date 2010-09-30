/*
 * Copyright 2006-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 16, 2006
 */

package org.zamia.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.build.ZamiaErrorObserver;
import org.zamia.zdb.ZDBException;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaProjectMap {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private static HashMap<IProject, ZamiaProject> fZPrjs = new HashMap<IProject, ZamiaProject>();

	private static HashMap<ZamiaProject, IProject> fIPrjs = new HashMap<ZamiaProject, IProject>();

	public static ZamiaProject getZamiaProject(IProject aProject) {

		ZamiaProject zprj = fZPrjs.get(aProject);

		if (zprj == null) {
			try {

				String baseDir = aProject.getLocation().toOSString();

				String localPath = "BuildPath.txt";

				SourceFile bpsf = new SourceFile(new File(baseDir + File.separator + localPath));
				bpsf.setLocalPath(localPath);

				while (zprj == null) {

					try {
						zprj = new ZamiaProject(aProject.getName(), baseDir, bpsf);
					} catch (ZDBException e) {

						File lockfile = e.getLockFile();

						int answer = ZamiaPlugin.askQuestion(null, "Lockfile exists", "A lockfile for project\n\n" + aProject.getName() + "\n\nalready exists:\n\n"
								+ lockfile.getAbsolutePath() + "\n\nAnother instance of ZamiaCAD is probably running.", SWT.ICON_ERROR | SWT.ABORT | SWT.RETRY | SWT.IGNORE);

						switch (answer) {
						case SWT.ABORT:
							logger.info("ZamiaProjectMap: Shutting down because lockfile was in the way.");
							try {
								PlatformUI.getWorkbench().close();
							} catch (Throwable t) {
								el.logException(t);
							}
							System.exit(1);

						case SWT.RETRY:
							break;

						case SWT.IGNORE:
							logger.info("ZamiaProjectMap: deleting lockfile '%s'.", lockfile.getAbsolutePath());
							lockfile.delete();
							break;
						}
					}
				}

				fZPrjs.put(aProject, zprj);
				fIPrjs.put(zprj, aProject);

				// hook up error observer

				ERManager erm = zprj.getERM();
				erm.addObserver(new ZamiaErrorObserver(aProject));

			} catch (ZamiaException e1) {
				el.logException(e1);
			} catch (IOException e) {
				el.logException(e);
			}
		}
		return zprj;
	}

	public static IProject getProject(ZamiaProject aZPrj) {
		return fIPrjs.get(aZPrj);
	}

	public static void remove(IProject aPrj) {
		ZamiaProject zprj = getZamiaProject(aPrj);
		fZPrjs.remove(aPrj);
		fIPrjs.remove(zprj);
	}

	public static void shutdown() {
		long startTime = System.currentTimeMillis();
		for (ZamiaProject zprj : fIPrjs.keySet()) {
			System.out.println("Shutting down project " + zprj + "...");
			zprj.shutdown();
			System.out.println("Shutting down project " + zprj + "...done.");
		}
		long stopTime = System.currentTimeMillis();
		double d = ((double) stopTime - startTime) / 1000.0;
		System.out.printf("Shutdown took %fs.\n", d);
	}

}
