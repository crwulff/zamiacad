/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * 
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaPropertyPage extends PropertyPage {

	private static final String TOPLEVELS_TITLE = "&Toplevels (comma separated):";
	public static final String TOPLEVELS_PROPERTY = "Toplevels";
	private static final String DEFAULT_TOPLEVELS = "";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text toplevelsText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ZamiaPropertyPage() {
		super();
	}

//	private void addSeparator(Composite parent) {
//		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
//		GridData gridData = new GridData();
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		separator.setLayoutData(gridData);
//	}

	private void addToplevelsSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for toplevels field
		Label ownerLabel = new Label(composite, SWT.NONE);
		ownerLabel.setText(TOPLEVELS_TITLE);

		// Owner text field
		toplevelsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		toplevelsText.setLayoutData(gd);

		// Populate owner text field
		try {
			String owner =
				((IResource) getElement()).getPersistentProperty(
					new QualifiedName("Zamia", TOPLEVELS_PROPERTY));
			toplevelsText.setText((owner != null) ? owner : DEFAULT_TOPLEVELS);
		} catch (CoreException e) {
			toplevelsText.setText(DEFAULT_TOPLEVELS);
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addToplevelsSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		// Populate the owner text field with the default value
		toplevelsText.setText(DEFAULT_TOPLEVELS);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("Zamia", TOPLEVELS_PROPERTY),
				toplevelsText.getText());
			IProject prj = (IProject) getElement();
			
			prj.build(IncrementalProjectBuilder.FULL_BUILD, null);
			
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}