/*
 * Copyright 2007,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.launch;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.views.sim.TopLevelDialog;
import org.zamia.vhdl.ast.DUUID;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZamiaLaunchMainTab extends AbstractLaunchConfigurationTab {

	private WidgetListener fWidgetListener = new WidgetListener();

	private Combo fSimField;

	private Text fProjectField, fToplevelField, fFileField, fPrefixField;

	private Button fBrowseProjectButton, fBrowseToplevelButton, fBrowseFileButton;

	public ZamiaLaunchMainTab() {
	}

	/**
	 * creates the widgets
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		comp.setLayout(gridLayout);

		createProjectEditor(comp);
	}

	/**
	 * ok, choose project button just pressed
	 */
	public void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null) {
			return;
		}

		// FIXME
		// ZamiaNature zamiaNature = ZamiaNature.getZamiaNature(project);
		// if(zamiaNature == null){
		// String msg = "The selected project must have the Zamia nature
		// associated.";
		// String title = "Invalid project (no Zamia nature associated).";
		// ErrorDialog.openError(getShell(), title, msg,
		// SimRunnerConfig.makeStatus(IStatus.WARNING, title, null));
		// return;
		// }

		String projectName = project.getName();
		fProjectField.setText(projectName);

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// no defaults to set
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String toplevel = configuration.getAttribute(SimRunnerConfig.ATTR_TOPLEVEL, "WORK.MYENTITY");
			fToplevelField.setText(toplevel);
		} catch (CoreException e) {
			fToplevelField.setText("");
		}

		try {
			fProjectField.setText(configuration.getAttribute(SimRunnerConfig.ATTR_PROJECT, ""));
		} catch (CoreException e) {
			fProjectField.setText("");
		}

		File f = new File("");
		String defPath = f.getAbsolutePath();

		try {
			fFileField.setText(configuration.getAttribute(SimRunnerConfig.ATTR_FILENAME, defPath));
		} catch (CoreException e) {
			fFileField.setText(defPath);
		}

		try {
			fPrefixField.setText(configuration.getAttribute(SimRunnerConfig.ATTR_PREFIX, ""));
		} catch (CoreException e) {
			fPrefixField.setText("");
		}

		try {
			int sim = configuration.getAttribute(SimRunnerConfig.ATTR_SIMULATOR, 0);

			fSimField.select(sim);

			fBrowseToplevelButton.setEnabled(true);
			fToplevelField.setEnabled(true);
			fPrefixField.setEnabled(true);
			fBrowseFileButton.setEnabled(sim != SimRunnerConfig.SIM_BUILTIN);
			fFileField.setEnabled(sim != SimRunnerConfig.SIM_BUILTIN);

		} catch (CoreException e) {
			fSimField.select(0);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		String value;
		value = fProjectField.getText().trim();
		setAttribute(conf, SimRunnerConfig.ATTR_PROJECT, value);

		value = fFileField.getText();
		setAttribute(conf, SimRunnerConfig.ATTR_FILENAME, value);

		int sim = fSimField.getSelectionIndex();
		conf.setAttribute(SimRunnerConfig.ATTR_SIMULATOR, sim);

		value = fToplevelField.getText().trim();
		setAttribute(conf, SimRunnerConfig.ATTR_TOPLEVEL, value);

		value = fPrefixField.getText().trim();
		setAttribute(conf, SimRunnerConfig.ATTR_PREFIX, value);
	}

	/**
	 * the name for this tab
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Main";
	}

	@Override
	public Image getImage() {
		return ZamiaPlugin.getImage("share/images/zamia.gif");
	}

	/**
	 * make a dialog and return a project (does no validation) and may return
	 * null.
	 */
	private IProject chooseProject() {

		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), null, false, "Choose the project for the run");
		dialog.open();
		Object[] objects = dialog.getResult();
		if (objects != null && objects.length == 1) {
			if (objects[0] instanceof IPath) {
				IPath p = (IPath) objects[0];
				if (p.segmentCount() > 0) {
					String string = p.segment(0);
					IWorkspace w = ResourcesPlugin.getWorkspace();
					return w.getRoot().getProject(string);
				}
			}
		}
		return null;
	}

	private void createProjectEditor(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Project");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);

		fProjectField = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjectField.setLayoutData(gd);
		fProjectField.addModifyListener(fWidgetListener);

		fBrowseProjectButton = createPushButton(group, "Browse...", null);
		fBrowseProjectButton.addSelectionListener(fWidgetListener);

		group = new Group(parent, SWT.NONE);
		group.setText("Simulator");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);

		/*
		 * simulator combo box
		 */

		Label label = new Label(group, SWT.NONE);
		label.setText("Simulator:");

		fSimField = new Combo(group, SWT.READ_ONLY);
		fSimField.add("Built-In");
		fSimField.add("VCD Import");
		fSimField.add("AET Import");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSimField.setLayoutData(gd);
		fSimField.addSelectionListener(fWidgetListener);

		label = new Label(group, SWT.NONE);
		label.setText("");

		/*
		 * toplevel editline + choose button
		 */

		label = new Label(group, SWT.NONE);
		label.setText("Toplevel:");

		fToplevelField = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fToplevelField.setLayoutData(gd);
		//		fToplevelField.addModifyListener(modifyListener);
		fToplevelField.addModifyListener(fWidgetListener);

		fBrowseToplevelButton = createPushButton(group, "Browse...", null);
		fBrowseToplevelButton.setText("Browse...");
		fBrowseToplevelButton.setEnabled(true);
		fBrowseToplevelButton.addSelectionListener(fWidgetListener);

		/*
		 * file editline + choose button
		 */

		label = new Label(group, SWT.NONE);
		label.setText("File:");

		fFileField = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fFileField.setLayoutData(gd);
		fFileField.addModifyListener(fWidgetListener);
		fFileField.setEditable(true);

		fBrowseFileButton = createPushButton(group, "Browse...", null);
		fBrowseFileButton.setText("Browse...");
		fBrowseFileButton.setEnabled(false);
		fBrowseFileButton.addSelectionListener(fWidgetListener);

		/*
		 * file editline
		 */

		label = new Label(group, SWT.NONE);
		label.setText("Signal path prefix:");

		fPrefixField = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPrefixField.setLayoutData(gd);
		fPrefixField.addModifyListener(fWidgetListener);
		fPrefixField.setEditable(true);
	}

	private void setAttribute(ILaunchConfigurationWorkingCopy conf, String name, String value) {
		if (value == null || value.length() == 0) {
			conf.setAttribute(name, (String) null);
		} else {
			conf.setAttribute(name, value);
		}
	}

	/**
	 * @return
	 */
	private IProject getProjectFromTextWidget() {
		IWorkspace w = ResourcesPlugin.getWorkspace();
		IResource project = w.getRoot().findMember(fProjectField.getText());

		if (project instanceof IProject) {
			return (IProject) project;
		}
		return null;
	}

	private class WidgetListener implements ModifyListener, SelectionListener {

		private void validate() {
			setErrorMessage(null);
			setMessage(null);

			IProject project = getProjectFromTextWidget();

			if (project == null) {
				setErrorMessage("invalid project");
				fBrowseToplevelButton.setEnabled(false);
				fBrowseFileButton.setEnabled(false);
				fToplevelField.setEnabled(false);
				fFileField.setEnabled(false);
				fPrefixField.setEnabled(false);
			} else {
				int sim = fSimField.getSelectionIndex();
				if (sim == SimRunnerConfig.SIM_BUILTIN) {
					fBrowseToplevelButton.setEnabled(true);
					fToplevelField.setEnabled(true);
					fBrowseFileButton.setEnabled(false);
					fFileField.setEnabled(false);
					fPrefixField.setEnabled(false);

					String value = fToplevelField.getText();

					try {
						DUUID.parse(value);
					} catch (ZamiaException e1) {
						setErrorMessage(e1.getMessage());
					}

				} else if (sim == SimRunnerConfig.SIM_VCD_IMPORT || sim == SimRunnerConfig.SIM_AET_IMPORT) {
					fBrowseToplevelButton.setEnabled(true);
					fToplevelField.setEnabled(true);
					fBrowseFileButton.setEnabled(true);
					fFileField.setEnabled(true);
					fPrefixField.setEnabled(true);

					File file = new File(fFileField.getText());
					if (!file.exists()) {
						setErrorMessage("'" + file.getAbsolutePath() + "' is not a file or does not exist.");
					}

				} else if (sim > 0) {
					setErrorMessage("Unknown simulator: " + sim);
					fBrowseToplevelButton.setEnabled(false);
					fBrowseFileButton.setEnabled(false);
					fToplevelField.setEnabled(false);
					fFileField.setEnabled(false);
					fPrefixField.setEnabled(false);
				}
			}

			updateLaunchConfigurationDialog();

		}

		public void modifyText(ModifyEvent e) {

			validate();
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fBrowseProjectButton) {
				handleProjectButtonSelected();
			} else if (source == fBrowseToplevelButton) {
				handleToplevelsButtonSelected();
			} else if (source == fBrowseFileButton) {
				handleFileButtonSelected();
			}
			validate();
		}

		private void handleToplevelsButtonSelected() {

			IProject project = getProjectFromTextWidget();
			if (project == null) {
				return;
			}

			TopLevelDialog tod = new TopLevelDialog(getShell(), project);

			tod.setTitle("Select toplevel to simulate");
			tod.setInput(this);
			tod.open();
			Object o = tod.getFirstResult();

			if (o instanceof Toplevel) {
				Toplevel tl = (Toplevel) o;

				fToplevelField.setText(tl.getDUUID().toString());
			}
		}

		private void handleFileButtonSelected() {
			FileDialog dialog = new FileDialog(getShell());

			int sim = fSimField.getSelectionIndex();

			String extension;

			if (sim == SimRunnerConfig.SIM_VCD_IMPORT) {
				extension = "*.vcd";
			} else {
				extension = "*.aet";
			}

			dialog.setFilterExtensions(new String[] { extension });

			if (Util.isMotif()) {
				File file = new File(fFileField.getText());
				String filterPath = null;
				if (file.isDirectory()) {
					filterPath = file.getAbsolutePath();
				} else {
					File parent = file.getParentFile();
					if (parent != null) {
						filterPath = parent.getAbsolutePath();
					}
				}

				// System.out.printf("Filter path : %s\n", filterPath);

				dialog.setFilterPath(filterPath);
			}
			dialog.setFileName(fFileField.getText());

			String fileName = dialog.open();
			if (fileName != null) {
				fFileField.setText(fileName);
				validate();
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

}
