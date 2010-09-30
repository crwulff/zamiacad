/*
 * Copyright 2006,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.views.sim;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class TraceDialog extends SelectionStatusDialog {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	class SearchManager implements Runnable {

		private String fSearchString;

		private boolean fNewSearchJob = false;

		private boolean fCanceled = false;

		private java.util.List<PathName> fNames;

		public synchronized void setSearchString(String aSearchString) {
			fSearchString = aSearchString;
			fNewSearchJob = true;
			notifyAll();
		}

		private synchronized String getSearchString() throws InterruptedException {

			while (!fNewSearchJob && !fCanceled) {
				wait(500);
			}

			fNewSearchJob = false;

			return fSearchString.toUpperCase();
		}

		public void run() {

			while (!fCanceled) {

				try {
					String searchString = getSearchString();

					if (fSimulator != null) {
						logger.debug("TraceDialog: Starting search on '%s'", searchString);

						fNames = fSimulator.findSignalNamesRegexp(searchString, fSearchLimit);

						if (!fCanceled) {
							fDisplay.asyncExec(new Runnable() {

								public void run() {
									fList.removeAll();
									fResults = new HashSetArray<PathName>();
									int n = fNames.size();
									for (int i = 0; i < n; i++) {

										PathName path = fNames.get(i);

										String pstr = path.toString();

										fList.add(pstr);

										fResults.add(path);

										logger.debug("TraceDialog: Search result: %3d/%3d: %s", i, n, path);
									}
								}
							});
						}

					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void cancel() {
			fCanceled = true;
		}
	}

	private HashSetArray<PathName> fResults;

	private Text fPattern;

	private List fList;

	private Label fProgressLabel;

	private Thread fSearchManagerThread;

	private SearchManager fSearchManager;

	private IGISimulator fSimulator;

	private Display fDisplay;

	private Combo fCombo;

	private int fSearchLimit = 25;
	
	public TraceDialog(Shell aShell) {
		super(aShell);

		fDisplay = aShell.getDisplay();

	}

	public void connect(IGISimulator aSimulator) {
		fSimulator = aSimulator;
	}

	public void disconnect() {
		fSimulator = null;
	}

	protected void applyFilter() {
		fSearchManager.setSearchString(fPattern.getText());
	}

	@Override
	protected void computeResult() {

		String sel[] = fList.getSelection();

		ArrayList<PathName> res = new ArrayList<PathName>(sel.length);

		for (int i = 0; i < sel.length; i++) {

			PathName path = new PathName(sel[i]);

			res.add(path);
		}

		setResult(res);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

		createHeader(content);

		fPattern = new Text(content, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPattern.setLayoutData(gd);

		createLabels(content);

		fList = new List(content, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);

		// list.setContentProvider(contentProvider);
		// list.setLabelProvider(getItemsListLabelProvider());
		// list.setInput(new Object[0]);
		// list.setItemCount(contentProvider.getNumberOfElements());
		gd = new GridData(GridData.FILL_BOTH);
		fList.setLayoutData(gd);

		/*
		 * limit drop down listbox
		 */

		Composite comp = new Composite(content, SWT.NONE);

		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		comp.setLayout(layout);

		Label l = new Label(comp, SWT.NONE);
		l.setText("Max. number of signals displayed:");
		l.setLayoutData(new GridData(GridData.BEGINNING));

		fCombo = new Combo(comp, SWT.READ_ONLY);

		fCombo.add("25");
		fCombo.add("50");
		fCombo.add("100");

		fCombo.select(0);

		fCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent aE) {
			}

			public void widgetSelected(SelectionEvent aE) {
				
				int limitIdx = fCombo.getSelectionIndex();
				fSearchLimit = 25;
				switch (limitIdx) {
				case 0:
					fSearchLimit = 25;
					break;
				case 1:
					fSearchLimit = 50;
					break;
				case 2:
					fSearchLimit = 100;
					break;
				}

				
				applyFilter();
			}
		});

		// createPopupMenu();

		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		fPattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (fList.getItemCount() > 0) {
						fList.setFocus();
					}
				}
			}
		});

		// list.getTable().addKeyListener(new KeyAdapter() {
		// public void keyPressed(KeyEvent e) {
		//
		// if (e.keyCode == SWT.DEL) {
		//
		// List selectedElements = ((StructuredSelection)
		// list.getSelection()).toList();
		//
		// Object item = null;
		// boolean isSelectedHistory = true;
		//
		// for (Iterator it = selectedElements.iterator(); it.hasNext();) {
		// item = it.next();
		// if (item instanceof ItemsListSeparator || !isHistoryElement(item)) {
		// isSelectedHistory = false;
		// break;
		// }
		// }
		// if (isSelectedHistory)
		// removeSelectedItems(selectedElements);
		//
		// }
		//
		// if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) != 0 &&
		// (e.stateMask & SWT.CTRL) != 0) {
		// StructuredSelection selection = (StructuredSelection)
		// list.getSelection();
		//
		// if (selection.size() == 1) {
		// Object element = selection.getFirstElement();
		// if (element.equals(list.getElementAt(0))) {
		// pattern.setFocus();
		// }
		// if (list.getElementAt(list.getTable().getSelectionIndex() - 1)
		// instanceof ItemsListSeparator)
		// list.getTable().setSelection(list.getTable().getSelectionIndex() -
		// 1);
		// list.getTable().notifyListeners(SWT.Selection, new Event());
		//
		// }
		// }
		//
		// if (e.keyCode == SWT.ARROW_DOWN && (e.stateMask & SWT.SHIFT) != 0 &&
		// (e.stateMask & SWT.CTRL) != 0) {
		//
		// if (list.getElementAt(list.getTable().getSelectionIndex() + 1)
		// instanceof ItemsListSeparator)
		// list.getTable().setSelection(list.getTable().getSelectionIndex() +
		// 1);
		// list.getTable().notifyListeners(SWT.Selection, new Event());
		// }
		//
		// }
		// });

		applyDialogFont(content);

		// apply filter even if pattern is empty (display history)
		applyFilter();

		return dialogArea;
	}

	/**
	 * Create a new header which is labelled by headerLabel.
	 * 
	 * @param parent
	 * @return Label the label of the header
	 */
	private void createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setText("Select signals to be traced");
		headerLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					fPattern.setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headerLabel.setLayoutData(gd);

		header.setLayoutData(gd);
	}

	/**
	 * Create the labels for the list and the progress. Return the list label.
	 * 
	 * @param parent
	 * @return Label
	 */
	private void createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel.setText("Signal search result:");

		listLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					fList.setFocus();
				}
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		fProgressLabel = new Label(labels, SWT.RIGHT);
		fProgressLabel.setLayoutData(gd);

		labels.setLayoutData(gd);
	}

	@Override
	public void create() {
		fSearchManager = new SearchManager();
		fSearchManagerThread = new Thread(fSearchManager);

		super.create();

		fSearchManagerThread.start();
		fPattern.setFocus();
	}

	@Override
	protected void initializeBounds() {
		Point size = new Point(500, 600);
		Point location = getInitialLocation(size);
		getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
	}

	public boolean close() {
		fSearchManager.cancel();
		try {
			fSearchManagerThread.join();
		} catch (InterruptedException e) {
			el.logException(e);
		}

		return super.close();
	}

}
