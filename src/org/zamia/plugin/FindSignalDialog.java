/*
 * Copyright 2006,2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
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
import org.zamia.BuildPath;
import org.zamia.ExceptionLogger;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGInstMapInfo;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.util.HashSetArray;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class FindSignalDialog extends SelectionStatusDialog {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	class SearchManager implements Runnable {

		private String fSearchString;

		private boolean fNewSearchJob = false;

		private boolean fCanceled = false;

		public Pattern fPattern;

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
					setRedDotState(0);

					String searchString = getSearchString();

					setRedDotState(1);

					logger.debug("FindSignalDialog: Starting search on '%s'", searchString);

					long startTime = System.currentTimeMillis();

					fDisplay.asyncExec(new Runnable() {
						public void run() {
							fList.removeAll();
						}
					});

					fResults = new HashSetArray<ToplevelPath>();

					try {

						Pattern pattern = fGlob.compile(searchString, GlobCompiler.CASE_INSENSITIVE_MASK);

						BuildPath bp = fZPrj.getBuildPath();
						if (bp == null) {
							continue;
						}

						int n = bp.getNumToplevels();
						for (int i = 0; i < n; i++) {

							Toplevel tl = bp.getToplevel(i);

							IGModule module = fIGM.findModule(tl);

							if (module != null) {
								searchSignals(pattern, module.getDBID(), tl, new PathName(""));
							}
						}

						long stopTime = System.currentTimeMillis();

						long d = (stopTime - startTime) / 1000;

						logger.debug("Search job took %d seconds", d);

						if (!fCanceled) {
							fDisplay.asyncExec(new Runnable() {

								public void run() {
									fList.removeAll();
									for (int i = 0; i < fResults.size(); i++) {

										try {
											ToplevelPath tlp = fResults.get(i);

											String pstr = tlp.toString();

											fList.add(pstr);
										} catch (Throwable t) {
										}
									}
								}
							});

						}
					} catch (MalformedPatternException e1) {
						el.logException(e1);
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		class AddResultJob implements Runnable {
			private final String fStr;

			public AddResultJob(String aStr) {
				fStr = aStr;
			}

			@Override
			public void run() {
				fList.add(fStr);
			}
		}

		private void searchSignals(Pattern aPattern, long aDBID, Toplevel aTL, PathName aPathName) {

			Iterator<String> it = fIGM.getSignalIdIterator(aDBID);

			if (it != null) {
				while (it.hasNext()) {

					if (fCanceled || fNewSearchJob || fResults.size() > fSearchLimit) {
						return;
					}

					PathName path = aPathName.append(it.next());

					String str = path.toString();

					if (fMatcher.matches(str, aPattern)) {

						ToplevelPath tlp = new ToplevelPath(aTL, path);

						fResults.add(tlp);

						fDisplay.asyncExec(new AddResultJob(tlp.toString()));
					}
				}
			}

			Iterator<IGInstMapInfo> it2 = fIGM.getInstIterator(aDBID);
			if (it2 != null) {
				while (it2.hasNext()) {

					if (fCanceled || fNewSearchJob || fResults.size() > fSearchLimit) {
						return;
					}

					IGInstMapInfo info = it2.next();

					PathName path = aPathName.append(info.getLabel());

					searchSignals(aPattern, info.getDBID(), aTL, path);
				}
			}
		}

		public void cancel() {
			fCanceled = true;
		}
	}

	private HashSetArray<ToplevelPath> fResults;

	private Text fPatternText;

	private List fList;

	private Label fProgressLabel;

	private Thread fSearchManagerThread;

	private SearchManager fSearchManager;

	private ZamiaProject fZPrj;

	private Display fDisplay;

	private Combo fCombo;

	private int fSearchLimit = 25;

	private Label fBusyLabel;

	private IGManager fIGM;

	private GlobCompiler fGlob;

	private Perl5Matcher fMatcher;

	public FindSignalDialog(Shell aShell, ZamiaProject aZPrj) {
		super(aShell);

		fDisplay = aShell.getDisplay();
		fZPrj = aZPrj;
		fIGM = aZPrj.getIGM();
		fGlob = new GlobCompiler();
		fMatcher = new Perl5Matcher();
	}

	protected void applyFilter() {
		fSearchManager.setSearchString(fPatternText.getText());
	}

	@Override
	protected void computeResult() {

		int idx = fList.getSelectionIndex();

		ArrayList<ToplevelPath> res = new ArrayList<ToplevelPath>(1);

		if (idx >= 0) {
			res.add(fResults.get(idx));
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

		fPatternText = new Text(content, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPatternText.setLayoutData(gd);
		fPatternText.setText("**");

		createLabels(content);

		fList = new List(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

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
		layout.numColumns = 3;
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

		fBusyLabel = new Label(comp, SWT.NONE);
		Image icon = ZamiaPlugin.getImage("/share/images/RedDot0.gif");
		fBusyLabel.setImage(icon);
		l.setLayoutData(new GridData(GridData.END));

		fPatternText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applyFilter();
			}
		});

		fPatternText.addKeyListener(new KeyAdapter() {
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
		headerLabel.setText("Signal path name regexp:");
		headerLabel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
					e.detail = SWT.TRAVERSE_NONE;
					fPatternText.setFocus();
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
		fPatternText.setFocus();
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

	private void setRedDotState(int aState) {
		Display display = Display.getDefault();
		final int state = aState;
		display.asyncExec(new Runnable() {
			public void run() {
				Image icon = ZamiaPlugin.getImage("/share/images/RedDot" + state + ".gif");
				fBusyLabel.setImage(icon);
			}
		});
	}

}
