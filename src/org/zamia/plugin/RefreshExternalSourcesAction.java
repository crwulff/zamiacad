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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.build.ZamiaBuilder;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class RefreshExternalSourcesAction implements IWorkbenchWindowActionDelegate {

	public final static FSCache fsCache = FSCache.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IWorkbenchWindow fWindow;

	class RefreshExternalSourcesJob extends Job {

		private ZamiaProject fZPrj;

		public RefreshExternalSourcesJob(ZamiaProject aZPrj) {
			super("Refreshing external sources on " + aZPrj);
			fZPrj = aZPrj;
		}

		protected IStatus run(IProgressMonitor monitor) {

			IProject prj = ZamiaProjectMap.getProject(fZPrj);
			
			ZamiaBuilder.setAutoBuildEnabled(false);
			ZamiaBuilder.linkExternalSources(prj, fZPrj, monitor);
			ZamiaBuilder.setAutoBuildEnabled(true);
			
			return Status.OK_STATUS;
		}

	}

	public void run(IAction action) {

		ZamiaPlugin.showConsole();

		ZamiaProject zPrj = ZamiaPlugin.findCurrentProject();
		if (zPrj == null) {
			ZamiaPlugin.showError(fWindow.getShell(), "No project selected.", "Please select a project in the navigator.", "No project selected.");
			return;
		}

		Job job = new RefreshExternalSourcesJob(zPrj);
		job.setPriority(Job.LONG);
		job.schedule();
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow aWindow) {
		fWindow = aWindow;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
