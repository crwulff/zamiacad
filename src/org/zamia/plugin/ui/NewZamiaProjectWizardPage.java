/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.ui;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class NewZamiaProjectWizardPage extends WizardPage {
	
	private static final String PAGE_NAME = "Create New ZamiaCAD Project";

	private Text fBasedirText;

	private Text fProjectText;

	private String fBasedir;

	private String fProject;

	public NewZamiaProjectWizardPage() {
		super(PAGE_NAME, "Create New ZamiaCAD Project", null);
		setTitle("New ZamiaCAD Project");
		setDescription("Select the project name, saving directory and its toplevel entity");
	}

	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		Label label = new Label(container, SWT.NULL);
		label.setText("&Project working path:");
		String defaultPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		fBasedirText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fBasedirText.setLayoutData(gd);
		fBasedirText.setText(defaultPath);
		fBasedirText.addModifyListener(new ModifyListener() {
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
		label.setText("Project &name:");
		fProjectText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjectText.setLayoutData(gd);
		fProjectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		dialogChanged();
		setControl(container);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		// dialog.setFilterPath(""); //FIXME the path here should be default
		// path
		fBasedirText.setText(System.getProperty("user.home"));
		String result = dialog.open();
		if (result != null) {
			fBasedirText.setText(result);
		}
	}

	private void dialogChanged() {
		fBasedir = fBasedirText.getText();
		fProject = fProjectText.getText();

		if (fBasedir.length() == 0) {
			updateStatus("Project working path must be specified");
			return;
		}
		if (fProject.length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}
		updateStatus(null);
	}

	public String getBasedirName() {
		return fBasedir;
	}

	public String getProjectName() {
		return fProject;
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}