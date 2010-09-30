/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 19, 2009
 */
package org.zamia.plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class UnitSelectionDialog extends FilteredItemsSelectionDialog {

	private ArrayList<UnitWrapper> fUnits;

	public UnitSelectionDialog(Shell aShell, ArrayList<UnitWrapper> aUnits) {
		super(aShell, false);
		fUnits = aUnits;
		setTitle("ZamiaCAD Unit Selection Dialog");
		setSelectionHistory(new ResourceSelectionHistory());
	}

	private class ResourceSelectionHistory extends SelectionHistory {
		protected Object restoreItemFromMemento(IMemento element) {
			return null;
		}

		protected void storeItemToMemento(Object item, IMemento element) {
		}
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
			public boolean matchItem(Object item) {
				return matches(item.toString());
			}

			public boolean isConsistentItem(Object item) {
				return true;
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
		progressMonitor.beginTask("Searching", fUnits.size()); //$NON-NLS-1$
		for (Iterator<UnitWrapper> iter = fUnits.iterator(); iter.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = ZamiaPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = ZamiaPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	@Override
	public String getElementName(Object item) {
		return item.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		};
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

}
