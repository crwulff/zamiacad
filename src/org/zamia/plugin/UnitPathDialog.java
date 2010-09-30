/*
 * Copyright 2006,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.zamia.BuildPath;
import org.zamia.ExceptionLogger;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGStructure;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class UnitPathDialog extends SelectionStatusDialog {

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

			return fSearchString;
		}

		public void run() {

			while (!fCanceled) {

				try {
					String searchString = getSearchString();

					logger.debug("UnitPathDialog: Starting search on '%s'", searchString);

					IGManager igm = fZPrj.getIGM();

					BuildPath bp = fZPrj.getBuildPath();

					final ArrayList<ItemTLPathTuple> res = new ArrayList<ItemTLPathTuple>();

					if (!searchString.contains(":")) {

						// list toplevels

						if (bp != null) {

							int n = bp.getNumToplevels();
							for (int i = 0; i < n; i++) {

								Toplevel tl = bp.getToplevel(i);

								ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));
								res.add(new ItemTLPathTuple(tlp, null));
							}
						}
					} else {
						
						try {
							ToplevelPath tlp = new ToplevelPath(searchString);
							
							IGItem item = igm.findItem(tlp.getToplevel(), tlp.getPath());
							if (item instanceof IGModule) {
								
								IGModule  module = (IGModule) item;
								
								IGStructure struct = module.getStructure();
								int m = struct.getNumStatements();
								for (int j = 0; j < m; j++) {

									IGConcurrentStatement stmt = struct.getStatement(j);

									String label = stmt.getLabel();
									if (label != null) {
									
										ToplevelPath tlp2 = tlp.append(label);

										res.add(new ItemTLPathTuple(tlp2, stmt));
									}
								}
							} else if (item instanceof IGStructure) {
								IGStructure struct = (IGStructure) item;
								int m = struct.getNumStatements();
								for (int j = 0; j < m; j++) {

									IGConcurrentStatement stmt = struct.getStatement(j);

									String label = stmt.getLabel();
									if (label != null) {
									
										ToplevelPath tlp2 = tlp.append(label);

										res.add(new ItemTLPathTuple(tlp2, stmt));
									}
								}
								IGContainer container = struct.getContainer();
								m = container.getNumLocalItems();
								for (int j = 0; j < m; j++) {

									IGContainerItem decl = container.getLocalItem(j);

									String label = decl.getId();
									if (label != null) {
									
										ToplevelPath tlp2 = tlp.append(label);

										res.add(new ItemTLPathTuple(tlp2, decl));
									}
								}
								
							}
						} catch (ZamiaException e1) {
							el.logException(e1);
						} 
					}

					if (!fCanceled) {
						fDisplay.asyncExec(new Runnable() {

							public void run() {
								fList.removeAll();
								fResultMap = new HashMap<Integer, ItemTLPathTuple>();
								int n = res.size();
								for (int i = 0; i < n; i++) {

									ItemTLPathTuple ipt = res.get(i);

									if (ipt.fStmt != null) {
										//fList.add(ipt.fStmt.getLabel() + ": " + ipt.fStmt.getChildDUUID().toString());
										fList.add(ipt.fStmt.toString());
									} else {
										fList.add(ipt.fTL.getToplevel().getDUUID().toString());
									}
									fResultMap.put(i, ipt);
								}
							}
						});
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

	private Text fPattern;

	private List fList;

	private Label fProgressLabel;

	private Thread fSearchManagerThread;

	private SearchManager fSearchManager;

	private Display fDisplay;

	private ZamiaProject fZPrj;

	private HashMap<Integer, ItemTLPathTuple> fResultMap;

	public UnitPathDialog(Shell aShell, ZamiaProject aZPrj) {
		super(aShell);

		fDisplay = aShell.getDisplay();
		fZPrj = aZPrj;

	}

	protected void applyFilter() {
		String pattern = fPattern.getText().toUpperCase();
		if (pattern.length() == 0 || pattern.endsWith(".") || pattern.endsWith(":")) {
			fSearchManager.setSearchString(pattern);
		}

	}

	@Override
	protected void computeResult() {

		ArrayList<ToplevelPath> res = new ArrayList<ToplevelPath>(1);

		try {
			res.add(new ToplevelPath(fPattern.getText()));
		} catch (ZamiaException e) {
			el.logZamiaException(e);
		}

		setResult(res);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		return super.createButton(parent, id, label, false);
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

		gd = new GridData(GridData.FILL_BOTH);
		fList.setLayoutData(gd);

		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		fPattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				//System.out.println("KeyCode: " + e.keyCode);
				if (e.keyCode == SWT.ARROW_DOWN) {
					if (fList.getItemCount() > 0) {
						fList.setFocus();
					}
				} else if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					okPressed();
				}
			}
		});

		fList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {

					int sel = fList.getSelectionIndex();
					if (sel >= 0) {

						ItemTLPathTuple ipt = fResultMap.get(sel);

						if (ipt != null) {

							fPattern.setText(ipt.fTL.toString() + PathName.separator);
							fPattern.setFocus();
						}
					}
				}
			}
		});
		
		fList.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent aE) {
				int sel = fList.getSelectionIndex();
				if (sel >= 0) {

					ItemTLPathTuple ipt = fResultMap.get(sel);

					if (ipt != null) {

						fPattern.setText(ipt.fTL.toString() + PathName.separator);
						fPattern.setFocus();
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent aE) {
			}

			@Override
			public void mouseUp(MouseEvent aE) {
			}});

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
		headerLabel.setText("Specify unit or signal path");
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
