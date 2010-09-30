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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.plugin.build.ZamiaBuilder;
import org.zamia.plugin.views.navigator.ZamiaNavigator;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZDBImportWizard extends Wizard implements IImportWizard {

	private IStructuredSelection fSelection;

	private ZDBImportWizardPage1 fMainPage;

	static class ImportZDBJob extends Job {

		private ZamiaProject fZPrj;

		private String fFileName;

		public ImportZDBJob(ZamiaProject aZPrj, String aFileName) {
			super("Importing ZDB from " + aFileName + " to " + aZPrj);
			fZPrj = aZPrj;
			fFileName = aFileName;
		}

		protected IStatus run(IProgressMonitor monitor) {

			ZDB zdb = fZPrj.getZDB();

			zdb.importFromFile(fFileName, fZPrj);
			
			fZPrj.zdbChanged();

			IProject prj = ZamiaProjectMap.getProject(fZPrj);

			ZamiaBuilder.setAutoBuildEnabled(false);
			
			ZamiaBuilder.linkExternalSources(prj, fZPrj, monitor) ;

			ZamiaBuilder.setAutoBuildEnabled(true);

			Display d = Display.getDefault();

			d.asyncExec(new Runnable() {

				public void run() {
					IWorkbenchPage page = ZamiaPlugin.getWorkbenchWindow().getActivePage();

					ZamiaNavigator zamiaNavigator = (ZamiaNavigator) page.findView(ZamiaNavigator.VIEW_ID);
					zamiaNavigator.refresh();
				}
			});
			

			return Status.OK_STATUS;
		}
	}

	public ZDBImportWizard() {
	}

	public void addPages() {
		super.addPages();
		fMainPage = new ZDBImportWizardPage1(fSelection);
		addPage(fMainPage);
	}

	@Override
	public boolean performFinish() {

		ZamiaPlugin.showConsole();

		IProject prj = fMainPage.getProject();
		String fileName = fMainPage.getFileName();

		ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

		File f = new File(fileName);
		if (!f.exists() || !f.canRead()) {
			ZamiaPlugin.showError(null, "Cannot open .zdb file", fileName + " cannot be opened.", "Doesn't exist / permission problem?");
			return false;
		}

		Job job = new ImportZDBJob(zprj, fileName);
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

		setWindowTitle("ZDB Import");
		//setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importzip_wiz.png"));
		setNeedsProgressMonitor(true);
	}

}
