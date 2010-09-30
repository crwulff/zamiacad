/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2009
 */
package org.zamia.plugin.ui;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZDBExportWizard extends Wizard implements IImportWizard {

	private IStructuredSelection fSelection;

	private ZDBExportWizardPage1 fMainPage;

	static class ExportZDBJob extends Job {

		private ZamiaProject fZPrj;

		private String fFileName;

		public ExportZDBJob(ZamiaProject aZPrj, String aFileName) {
			super("Exporting ZDB from " + aZPrj);
			fZPrj = aZPrj;
			fFileName = aFileName;
		}

		protected IStatus run(IProgressMonitor monitor) {

			//IProject prj = ZamiaProjectMap.getProject(fZPrj);

			ZDB zdb = fZPrj.getZDB();

			zdb.exportToFile(fFileName, fZPrj);

			return Status.OK_STATUS;
		}
	}

	public ZDBExportWizard() {
	}

	public void addPages() {
		super.addPages();
		fMainPage = new ZDBExportWizardPage1(fSelection);
		addPage(fMainPage);
	}

	@Override
	public boolean performFinish() {

		ZamiaPlugin.showConsole();

		IProject prj = fMainPage.getProject();
		String fileName = fMainPage.getFileName();

		ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

		File f = new File(fileName);
		if (f.exists()) {

			int answer = ZamiaPlugin.askQuestion(null, "Overwrite file?", fileName + " exists.\n\nOverwrite?", SWT.ICON_QUESTION | SWT.YES | SWT.NO);

			if (answer == SWT.NO) {
				return false;
			}
		}

		Job job = new ExportZDBJob(zprj, fileName);
		job.setPriority(Job.LONG);
		job.schedule();

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(IWorkbench aWorkbench, IStructuredSelection aSelection) {
		fSelection = aSelection;
		List selectedResources = IDE.computeSelectedResources(aSelection);
		if (!selectedResources.isEmpty()) {
			fSelection = new StructuredSelection(selectedResources);
		}

		setWindowTitle("ZDB Export");
		//setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importzip_wiz.png"));
		setNeedsProgressMonitor(true);
	}

}
