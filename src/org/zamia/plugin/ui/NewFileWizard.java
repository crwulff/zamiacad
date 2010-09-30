/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import java.util.Calendar;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/**
 * @author guenter bartsch
 */
public class NewFileWizard extends Wizard implements INewWizard {

	private NewFileWizardPage page;

	private ISelection selection;

	public NewFileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		page = new NewFileWizardPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(String containerName, String fileName, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));

		if (resource == null || !resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container (Project) \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(fileName);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		// IProject fProject = file.getProject();
		// Project fZamiaProject = new Project();
		// fZamiaProject.setProject(fProject);
		// fZamiaProject.getZamiaFileName();
		// fZamiaProject.getProjectFiles();
		// fZamiaProject.setFilename(fZamiaProject.zamiaFileName);
		// String fPath = fProject.getLocation().toOSString();
		// try {
		// fZamiaProject.addFile(fPath + System.getProperty("file.separator") +
		// file.getName());
		// fZamiaProject.save();
		// } catch (ProjectException e) {
		// } catch (IOException e) {
		//			
		// }
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private InputStream openContentStream(String _fileName) {
		String contents = "";
		Calendar rightNow = Calendar.getInstance();
		String createdTime = rightNow.get(Calendar.HOUR) + ":" + rightNow.get(Calendar.MINUTE) + "  " + rightNow.get(Calendar.YEAR) + "." + rightNow.get(Calendar.MONTH) + "."
				+ rightNow.get(Calendar.DAY_OF_MONTH);
		if (_fileName.endsWith("vhdl") || _fileName.endsWith("vhd")) {
			contents = "-- " + _fileName + " created on " + createdTime;
		} else if (_fileName.endsWith("bench")) {
			contents = "# " + _fileName + " created on " + createdTime;
		}
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "org.zamia.plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
