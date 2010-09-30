/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia.plugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.ZamiaException.ExCat;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.editors.ZamiaEditor;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ExternalBuildAction implements IWorkbenchWindowActionDelegate {

	public final static FSCache fsCache = FSCache.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Shell fShell;

	private IWorkbenchWindow fWindow;

	private static final String TCL_BUILD_CMD = "zamiaBuild";

	class ExternalBuildJob extends Job {

		private String fFileName;

		private ZamiaProject fZPrj;

		public ExternalBuildJob(String aFileName, ZamiaProject aZPrj) {
			super("Running external build on " + aZPrj);
			fFileName = aFileName;
			fZPrj = aZPrj;
		}

		protected IStatus run(IProgressMonitor monitor) {

			// clear markers

			ERManager erm = fZPrj.getERM();

			erm.removeErrors(ExCat.EXTERNAL);

			// now run the external builder if available

			//			ZamiaTclInterpreter zti = fZPrj.getZTI();
			//			if (zti != null) {
			//
			//				if (zti.hasCommand(TCL_BUILD_CMD)) {
			//
			//					String cmd = TCL_BUILD_CMD + " " + fFileName;
			//
			//					logger.debug("ExternalBuildAction: About to run tcl command '%s'", cmd);
			//
			//					try {
			//						zti.eval(cmd);
			//					} catch (TclException e) {
			//						el.logException(e);
			//					}
			//
			//				} else {
			//					ZamiaPlugin.showError(fShell, "No external builder", TCL_BUILD_CMD + " is not defined.", "No external builder configured?");
			//				}
			//			}

			return Status.OK_STATUS;
		}

	}

	public void run(IAction action) {

		ZamiaPlugin.showConsole();

		// figure out filename

		String fileName = null;
		ZamiaProject zprj = null;
		//IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {

			IEditorPart editor = fWindow.getActivePage().getActiveEditor();

			if (editor instanceof ZamiaEditor) {
				ZamiaEditor ze = (ZamiaEditor) editor;
				fileName = ze.getFilename();

				zprj = ze.getZPrj();

			}
		} catch (Throwable t) {
			el.logException(t);
		}

		if (zprj == null) {
			ZamiaPlugin.showError(fShell, "Project not found", "Couldn't determine which project to build", "No active editor?");
			return;
		}

		Job job = new ExternalBuildJob(fileName, zprj);
		job.setPriority(Job.LONG);
		job.schedule();
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow aWindow) {
		fWindow = aWindow;
		fShell = fWindow.getShell();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
