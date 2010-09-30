/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.events.*;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.jface.viewers.*;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class NewFileWizardPage extends WizardPage {
	private Text fContainerText;

	private Text fFileText;

	private ISelection fSelection;

	public NewFileWizardPage(ISelection aSelection) {
		super("wizardPage");
		setTitle("New VHDL File");
		setDescription("This wizard creates a new file with *.vhdl extension.");
		fSelection = aSelection;
	}

	public void createControl(Composite aParent) {
		Composite container = new Composite(aParent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		fContainerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fContainerText.setLayoutData(gd);
		fContainerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

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
		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {
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
				fContainerText.setText(container.getFullPath().toString());
			}
		}
		fFileText.setText("new_file.vhdl");
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				fContainerText.setText(((Path) result[0]).toOSString());
			}
		}
	}

	private void dialogChanged() {
		String container = getContainerName();
		String fileName = getFileName();

		if (container.length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if ((ext.equalsIgnoreCase("vhdl") == false) && (ext.equalsIgnoreCase("vhd") == false) && ext.equalsIgnoreCase("bench") == false) {
				updateStatus("File extension must be either \"vhdl\" or \"vhd\" or \"bench\"");
				return;
			}
		}
		updateStatus(null);
	}

	private void updateStatus(String aMessage) {
		setErrorMessage(aMessage);
		setPageComplete(aMessage == null);
	}

	public String getContainerName() {
		return fContainerText.getText();
	}

	public String getFileName() {
		return fFileText.getText();
	}
}