/*
 * Copyright 2006,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.editors;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StructuredSelection;
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
import org.zamia.util.SimpleRegexp;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.DeclarativeItem;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class OutlineSearchDialog extends SelectionStatusDialog {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	class SearchManager implements Runnable {

		private String fSearchString;

		private boolean fNewSearchJob = false;

		private boolean fCanceled = false;

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
					String searchRegexp = getSearchString();

					if (fContentProvider != null) {
						logger.debug("OutlineSearchDialog: Starting search on '%s'", searchRegexp);

						String regex = SimpleRegexp.convert(searchRegexp);
						Pattern p = Pattern.compile(regex);

						Object[] rootElements = fContentProvider.getElements(null);

						fResults = new ArrayList<StructuredSelection>();

						int n = rootElements != null ? rootElements.length : 0;

						for (int i = 0; i < n; i++) {
							findElements(p, rootElements[i], new ZStack<Object>());
						}

						if (!fCanceled) {
							fDisplay.asyncExec(new Runnable() {

								public void run() {
									try {
										fList.removeAll();
										for (int i = 0; i < fResults.size(); i++) {

											StructuredSelection sel = fResults.get(i);

											int sn = sel.size();

											String estr = sn > 0 ? sel.toArray()[sn - 1].toString() : "";

											fList.add(estr);
										}
									} catch (Throwable t) {
										el.logException(t);
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

		private void findElements(Pattern aPattern, Object aElement, ZStack<Object> aParents) {

			if (aElement == null) {
				return;
			}

			if (fResults.size() > fSearchLimit) {
				return;
			}

			if (aElement instanceof DeclarativeItem) {

				DeclarativeItem di = (DeclarativeItem) aElement;

				String str = di.getId();
				Matcher m = aPattern.matcher(str);

				if (m.matches()) {

					int n = aParents.size();
					Object path[] = new Object[n + 1];
					for (int i = 0; i < n; i++) {
						path[i] = aParents.get(i);
					}
					path[n] = aElement;

					fResults.add(new StructuredSelection(path));
				}
			}

			Object[] children = fContentProvider.getChildren(aElement);

			int n = children != null ? children.length : 0;
			for (int i = 0; i < n; i++) {
				Object child = children[i];
				aParents.push(child);
				findElements(aPattern, children[i], aParents);
				aParents.pop();
			}
		}

		public void cancel() {
			fCanceled = true;
		}
	}

	private ArrayList<StructuredSelection> fResults;

	private Text fPattern;

	private List fList;

	private Label fProgressLabel;

	private Thread fSearchManagerThread;

	private SearchManager fSearchManager;

	private ZamiaOutlineContentProvider fContentProvider;

	private Display fDisplay;

	private Combo fCombo;

	private int fSearchLimit = 25;

	public OutlineSearchDialog(Shell aShell) {
		super(aShell);

		fDisplay = aShell.getDisplay();

	}

	public void connect(ZamiaOutlineContentProvider aContentProvider) {
		fContentProvider = aContentProvider;
	}

	public void disconnect() {
		fContentProvider = null;
	}

	protected void applyFilter() {
		fSearchManager.setSearchString(fPattern.getText());
	}

	@Override
	protected void computeResult() {

		int idx = fList.getSelectionIndex();
		if (idx < 0) {
			return;
		}

		ArrayList<StructuredSelection> res = new ArrayList<StructuredSelection>(1);
		res.add(fResults.get(idx));

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
		fPattern.setText("**");

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
		l.setText("Max. number of elements displayed:");
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

		applyDialogFont(content);

		// apply filter even if pattern is empty (display history)
		applyFilter();

		return dialogArea;
	}

	private void createHeader(Composite parent) {
		Composite header = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setText("Search Outline Element (simple regexp)");
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

	private void createLabels(Composite parent) {
		Composite labels = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		Label listLabel = new Label(labels, SWT.NONE);
		listLabel.setText("Outline search result:");

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
