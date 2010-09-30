/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 7, 2008
 */
package org.zamia.plugin.editors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.zamia.ToplevelPath;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ShowReferencesDialog extends Dialog {

	private Button fScopeLocalRadioButton;

	private Button fScopeDownRadioButton;

	private Button fScopeGlobalRadioButton;

	private Button fAliasedSignalsCheckBox;

	public boolean fScopeLocal = true;

	public boolean fScopeDown = false;

	public boolean fScopeGlobal = false;

	private Text fSearchJobText;

	private Button fPathCheckBox, fWritersOnlyCheckBox, fReadersOnlyCheckBox;

	private Text fPathText;

	private ToplevelPath fPath;

	private String fSearchJobTextStr = "";

	private boolean fUsePath, fWritersOnly, fReadersOnly;

	protected ShowReferencesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {

		Shell shell = getShell();
		shell.setText("Search References");

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fSearchJobText = new Text(panel, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gd.widthHint = 400;
		gd.heightHint = 100;
		fSearchJobText.setLayoutData(gd);

		Composite mainPanel = createMainPanel(panel);
		setGridData(mainPanel, SWT.FILL, true, SWT.TOP, false);

		Composite buttonBar = createMyButtonBar(panel);
		setGridData(buttonBar, SWT.FILL, true, SWT.BOTTOM, false);

		applyDialogFont(panel);

		updateWidgetStates();

		return panel;
	}

	private Composite createMainPanel(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		panel.setLayout(layout);

		Composite scopeGroup = createScopeGroup(panel);
		setGridData(scopeGroup, SWT.FILL, true, SWT.FILL, false);

		Composite optionsGroup = createOptionsGroup(panel);
		setGridData(optionsGroup, SWT.FILL, true, SWT.FILL, true);

		return panel;
	}

	class MySelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {

			fScopeLocal = fScopeLocalRadioButton.getSelection();
			fScopeDown = fScopeDownRadioButton.getSelection();
			fScopeGlobal = fScopeGlobalRadioButton.getSelection();

		}

	}

	private Composite createScopeGroup(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText("Scope");
		GridLayout groupLayout = new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fScopeLocalRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fScopeLocalRadioButton.setText("local");
		setGridData(fScopeLocalRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fScopeLocalRadioButton.setSelection(fScopeLocal);
		fScopeLocalRadioButton.addSelectionListener(new MySelectionListener());

		fScopeDownRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fScopeDownRadioButton.setText("local+down");
		setGridData(fScopeDownRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fScopeDownRadioButton.setSelection(fScopeDown);
		fScopeDownRadioButton.addSelectionListener(new MySelectionListener());

		fScopeGlobalRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
		fScopeGlobalRadioButton.setText("global");
		setGridData(fScopeGlobalRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fScopeGlobalRadioButton.setSelection(fScopeGlobal);
		fScopeGlobalRadioButton.addSelectionListener(new MySelectionListener());

		return panel;
	}

	public boolean isSearchUp() {
		return fScopeGlobal;
	}

	public boolean isSearchDown() {
		return fScopeGlobal || fScopeDown;
	}

	private Composite createMyButtonBar(Composite parent) {

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Button cancelButton = createButton(panel, IDialogConstants.CANCEL_ID, "Cancel", false);
		setGridData(cancelButton, SWT.RIGHT, true, SWT.BOTTOM, false);

		Button searchButton = createButton(panel, IDialogConstants.OK_ID, "Search", true);
		setGridData(searchButton, SWT.RIGHT, false, SWT.BOTTOM, false);

		return panel;
	}

	private Composite createOptionsGroup(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);

		Group group = new Group(panel, SWT.SHADOW_NONE);
		group.setText("Options");
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		groupLayout.makeColumnsEqualWidth = true;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite pathPanel = new Composite(group, SWT.NONE);
		GridLayout pathLayout = new GridLayout();
		pathLayout.marginWidth = 0;
		pathLayout.marginHeight = 0;
		pathLayout.numColumns = 2;
		pathPanel.setLayout(pathLayout);
		pathPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fPathCheckBox = new Button(pathPanel, SWT.CHECK | SWT.LEFT);
		fPathCheckBox.setText("Use Path");
		setGridData(fPathCheckBox, SWT.LEFT, false, SWT.CENTER, true);
		fPathCheckBox.setSelection(false);
		fPathCheckBox.setEnabled(false);
		fPathCheckBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent aE) {
			}

			@Override
			public void widgetSelected(SelectionEvent aE) {
				fUsePath = fPathCheckBox.getSelection();
			}

		});

		fPathText = new Text(pathPanel, SWT.BORDER | SWT.READ_ONLY);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
		fPathText.setLayoutData(gd);

		fWritersOnlyCheckBox = new Button(pathPanel, SWT.CHECK | SWT.LEFT);
		fWritersOnlyCheckBox.setText("Drivers only");
		setGridData(fWritersOnlyCheckBox, SWT.LEFT, false, SWT.CENTER, true);
		fWritersOnlyCheckBox.setSelection(false);
		fWritersOnlyCheckBox.setEnabled(true);
		fWritersOnlyCheckBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent aE) {
			}

			@Override
			public void widgetSelected(SelectionEvent aE) {
				fWritersOnly = fWritersOnlyCheckBox.getSelection();
			}

		});

		fReadersOnlyCheckBox = new Button(pathPanel, SWT.CHECK | SWT.LEFT);
		fReadersOnlyCheckBox.setText("Readers only");
		setGridData(fReadersOnlyCheckBox, SWT.LEFT, false, SWT.CENTER, true);
		fReadersOnlyCheckBox.setSelection(false);
		fReadersOnlyCheckBox.setEnabled(true);
		fReadersOnlyCheckBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent aE) {
			}

			@Override
			public void widgetSelected(SelectionEvent aE) {
				fReadersOnly = fReadersOnlyCheckBox.getSelection();
			}

		});

		fAliasedSignalsCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
		fAliasedSignalsCheckBox.setText("Trace aliased signals");
		setGridData(fAliasedSignalsCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fAliasedSignalsCheckBox.setSelection(false);
		fAliasedSignalsCheckBox.setEnabled(false); // FIXME: not implemented yet

		return panel;
	}

	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd = new GridData();
		component.setLayoutData(gd);
		gd.horizontalAlignment = horizontalAlignment;
		gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
		gd.verticalAlignment = verticalAlignment;
		gd.grabExcessVerticalSpace = grabExcessVerticalSpace;
	}

	private void updateWidgetStates() {
		if (fSearchJobText != null && !fSearchJobText.isDisposed()) {
			fSearchJobText.setText(fSearchJobTextStr);
		}
		if (fPathCheckBox != null && !fPathCheckBox.isDisposed()) {
			fPathCheckBox.setEnabled(fPath != null);
			fPathCheckBox.setSelection(fPath != null);
			fUsePath = fPath != null;
		}
		if (fWritersOnlyCheckBox != null && !fWritersOnlyCheckBox.isDisposed()) {
			fWritersOnlyCheckBox.setEnabled(true);
			fWritersOnlyCheckBox.setSelection(fWritersOnly);
		}
		if (fReadersOnlyCheckBox != null && !fReadersOnlyCheckBox.isDisposed()) {
			fReadersOnlyCheckBox.setEnabled(true);
			fReadersOnlyCheckBox.setSelection(fReadersOnly);
		}
		if (fPathText != null && !fPathText.isDisposed()) {
			if (fPath == null) {
				fPathText.setText("");
			} else {
				fPathText.setText(fPath.toString());
			}
		}
	}

	public void setPath(ToplevelPath aPath) {
		fPath = aPath;
		updateWidgetStates();
	}

	public ToplevelPath getPath() {
		return fPath;
	}

	public boolean isUsePath() {
		return fUsePath;
	}

	public boolean isWritersOnly() {
		return fWritersOnly;
	}

	public boolean isReadersOnly() {
		return fReadersOnly;
	}

	public void setSearchJobText(String aString) {
		fSearchJobTextStr = aString;
		updateWidgetStates();
	}
}
