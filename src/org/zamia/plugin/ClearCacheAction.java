/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia.plugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ClearCacheAction implements IWorkbenchWindowActionDelegate {

	public final static FSCache fsCache = FSCache.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IWorkbenchWindow fWindow;

	public void run(IAction action) {

		int rc = ZamiaPlugin.askQuestion(fWindow.getShell(), "Clear Cached Data?", "Are you sure you want to\nclear the local disk caches?\nWarning: You will have to rebuild all projects.", SWT.ICON_QUESTION | SWT.YES | SWT.NO);

		if (rc == SWT.YES) {

			
			Job job = new Job("Clearing cached data...") {
				protected IStatus run(IProgressMonitor monitor) {

					logger.info("ClearCacheAction: Clearing cached data...");

					IWorkspace w = ResourcesPlugin.getWorkspace();
					
					IProject[] prjs = w.getRoot().getProjects();
					
					int n = prjs.length;

					int totalWorkUnits = n + 1;
					
					monitor.beginTask("Clearing cached data", totalWorkUnits);
					
					monitor.subTask("Cleaning FS Cache");
					
					fsCache.cleanAll();
					
					monitor.worked(1);
					
					for (int i = 0; i<n; i++) {
						IProject prj = prjs[i];
						
						if (!prj.isOpen()) {
							continue;
						}
						
						logger.info("ClearCacheAction: Cleaning project %s...", prj);

						monitor.subTask("Cleaning project "+prj);
						
						ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);
						
						try {
							zprj.clean();
						} catch (Throwable e) {
							el.logException(e);
						}
						
						monitor.worked(1);
					}
					
					logger.info("ClearCacheAction: Clearing cached data...done.");
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.LONG);
			job.schedule(); 
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow aWindow) {
		fWindow = aWindow;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
