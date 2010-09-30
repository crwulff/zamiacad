/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 17, 2009
 */
package org.zamia.plugin.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.zamia.plugin.ZamiaPlugin;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZDBExportWizardPage1 extends WizardPage {

	private ISelection fSelection;

	private Text fContainerText;

	private Text fFileText;

	private IProject fPrj;

	public ZDBExportWizardPage1(ISelection aSelection) {
		super("wizardPage");
		setTitle("Export ZDB File");
		setDescription("This wizard dumps all ZDB contents to a file.");
		fSelection = aSelection;
	}

	public void createControl(Composite aParent) {
		Composite container = new Composite(aParent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Project:");

		fContainerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fContainerText.setLayoutData(gd);
		fContainerText.setEditable(false);

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fFileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fFileText.setLayoutData(gd);
		fFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseFile();
			}
		});

		
		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {

		fPrj = null;

		if (fSelection != null && fSelection.isEmpty() == false && fSelection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) fSelection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();

				fPrj = container.getProject();

				fContainerText.setText(fPrj.getName());
			}
		}
		fFileText.setText("");
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select a project");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {

				Path path = ((Path) result[0]);

				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource resource = root.findMember(path);

				if (resource != null) {
					fPrj = resource.getProject();
					fContainerText.setText(fPrj.getName());
				}
			}
		}

		dialogChanged();
	}

	private void handleBrowseFile() {
		
		FileDialog dialog = new FileDialog(ZamiaPlugin.getShell(), SWT.SAVE);
		dialog.setText("Export ZDB...");
		dialog.setFilterExtensions(new String[] { "*.zdb" });

		String fileName = dialog.open();
		if (fileName != null) {
			fFileText.setText(fileName);
		}
	}

	private void dialogChanged() {
		String fileName = getFileName();

		if (fPrj == null) {
			updateStatus("Project must be specified");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String aMessage) {
		setErrorMessage(aMessage);
		setPageComplete(aMessage == null);
	}

	public IProject getProject() {
		return fPrj;
	}

	public String getFileName() {
		return fFileText.getText();
	}
}
