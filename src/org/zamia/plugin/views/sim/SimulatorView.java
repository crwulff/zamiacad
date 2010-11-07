/*
 * Copyright 2006-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 24, 2006
 */

package org.zamia.plugin.views.sim;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimObserver;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.instgraph.sim.vcd.VCDImport;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.plugin.editors.ReferencesSearchQuery;
import org.zamia.plugin.editors.ZamiaEditor;
import org.zamia.plugin.launch.SimRunnerConfig;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class SimulatorView extends ViewPart implements IGISimObserver {

	public enum TraceDisplayMode {
		BIN, OCT, HEX, DEC
	};

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private Composite fControl;

	private Text fRunText, fInfoLabel;

	private IGISimulator fSimulator;

	private TraceDialog fTraceDialog;

	private ToolItem fTraceTI, fUnTraceTI, fNewLineTI, fRunTI, fRestartTI, fJobTI, fPrevTransTI, fNextTransTI, fGotoCycleTI;

	private SimRunnerConfig fConfig;

	private Lock fSimJobLock;

	private Canvas fWaveformCanvas;

	private Tree fTree;

	private HashMap<String, TraceLine> fUIDTraceLineMap;

	private HashMap<PathName, TraceLine> fSignalPathTraceLineMap;

	private HashMap<TraceLine, TreeItem> fTraceLineTreeItemMap;

	private Display fDisplay;

	private BigInteger fStartTime = BigInteger.ZERO, fEndTime = BigInteger.ZERO, fCursorTime = BigInteger.ZERO;

	private int fVisibleWidth;

	private int fImageWidth;

	private int fXOffset, fYOffset;

	private IGISimCursor fCursor;

	public final static int BORDER_WIDTH = 14;

	public final static int DEFAULT_PIXELS_PER_UNIT = 20;

	private static final double DEFAULT_FS_PER_UNIT = 1000000.0; // nanoseconds

	private String fUnitName = "ns";

	private double fFSPerUnit = DEFAULT_FS_PER_UNIT;

	private double fPixelsPerUnit = DEFAULT_PIXELS_PER_UNIT;

	private boolean fMousePressed = false;

	public static final int NUM_COLORS = 16;

	private Color fColors[];

	private String[] fColorNames = { "Purple", "Light Gray", "Red", "Light Blue", "Yellow", "Light Green", "Blue", "Gray", "Orange", "Dark Gray", "Light Red", "Brown", "Magenta", "Green", "Cyan", "White" };

	private Color fYellow, fBlack, fWhite;

	private int fColor = 0;

	private Menu fPopupMenu;

	private Image fOffscreenImage = null;

	private GC fOffscreenGC;

	private Lock fOffscreenLock;

	private WaveformPaintJobScheduler fScheduler = null;

	private Thread fSchedulerThread = null;

	private Composite fWVComposite;

	private Image fMinusIcon;

	private Label fTimeUnitLabel;

	private Shell fShell;

	public SimulatorView() throws ZamiaException {
		fSimJobLock = new ReentrantLock();
		fOffscreenLock = new ReentrantLock();
	}

	class GotoTransitionJob extends Job {

		private boolean fGotoNext;

		private ArrayList<TraceLine> fTraceLines;

		private BigInteger fTime;

		private Display fDisplay;

		private BigInteger fNewTime;

		public GotoTransitionJob(boolean aGotoNext, ArrayList<TraceLine> aTLs, BigInteger aTime) {
			super("Goto transition...");
			fGotoNext = aGotoNext;
			fTraceLines = aTLs;
			fTime = aTime;
			fDisplay = getSite().getShell().getDisplay();
		}

		@Override
		protected IStatus run(IProgressMonitor aMonitor) {

			try {
				fSimJobLock.lock();

				fNewTime = fGotoNext ? fSimulator.getEndTime() : fSimulator.getStartTime();

				IGISimCursor cursor = fSimulator.createCursor();

				int n = fTraceLines.size();
				for (int i = 0; i < n; i++) {

					TraceLine tl = fTraceLines.get(i);

					if (fGotoNext) {
						BigInteger t = tl.findNextTransition(cursor, fTime, fEndTime);

						if (t != null && t.compareTo(fSimulator.getEndTime()) < 0) {

							if (t.compareTo(fNewTime) < 0) {
								fNewTime = t;
							}
						}

					} else {
						BigInteger t = tl.findPreviousTransition(cursor, fTime, fStartTime);
						if (t != null) {

							if (t.compareTo(fNewTime) > 0) {
								fNewTime = t;
							}
						}

					}
				}

				cursor.dispose();
				cursor = null;

				if (fNewTime != null && fNewTime.compareTo(fSimulator.getEndTime()) < 0) {
					fDisplay.syncExec(new Runnable() {
						public void run() {
							moveCursor(fNewTime);
						}
					});
				} else {
					//ZamiaPlugin.showError(getSite().getShell(), "No more transitions found.", "No transition found.", "Signal doesn't have further transitions.");
					fDisplay.syncExec(new Runnable() {
						public void run() {
							fDisplay.beep();
						}
					});
				}

			} catch (Throwable e1) {
				el.logException(e1);
			} finally {
				fSimJobLock.unlock();
			}
			return Status.OK_STATUS;
		}

	}

	Color getBlack() {
		return fBlack;
	}

	public void createPartControl(Composite aParent) {

		fDisplay = aParent.getDisplay();
		fShell = aParent.getShell();

		fYellow = fDisplay.getSystemColor(SWT.COLOR_YELLOW);
		fBlack = fDisplay.getSystemColor(SWT.COLOR_BLACK);
		fWhite = fDisplay.getSystemColor(SWT.COLOR_WHITE);

		fMinusIcon = ZamiaPlugin.getImage("/share/images/minus.gif");

		fColors = new Color[16];

		fColors[0] = new Color(fDisplay, 200, 100, 255); // purple
		fColors[1] = new Color(fDisplay, 200, 200, 200); // light gray
		fColors[2] = new Color(fDisplay, 255, 0, 0); // red
		fColors[3] = new Color(fDisplay, 150, 150, 255); // light blue
		fColors[4] = new Color(fDisplay, 255, 255, 0); // yellow
		fColors[5] = new Color(fDisplay, 150, 255, 150); // light green
		fColors[6] = new Color(fDisplay, 80, 80, 255); // blue
		fColors[7] = new Color(fDisplay, 170, 170, 170); // gray
		fColors[8] = new Color(fDisplay, 255, 120, 00); // orange
		fColors[9] = new Color(fDisplay, 120, 120, 120); // dark gray
		fColors[10] = new Color(fDisplay, 255, 150, 150); // light red
		fColors[11] = new Color(fDisplay, 139, 71, 38); // brown
		fColors[12] = new Color(fDisplay, 255, 0, 255); // magenta
		fColors[13] = new Color(fDisplay, 80, 255, 80); // green
		fColors[14] = new Color(fDisplay, 0, 255, 255); // cyan
		fColors[15] = new Color(fDisplay, 255, 255, 255); // white

		fControl = new Composite(aParent, SWT.NONE);

		fTraceDialog = new TraceDialog(getViewSite().getShell());
		fTraceDialog.setTitle("Select signals to trace");

		/*
		 * setup gui
		 */

		GridLayout gl = new GridLayout();
		fControl.setLayout(gl);
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		// a coolbar for navigation and zoom

		CoolBar coolbar = new CoolBar(fControl, SWT.NONE);
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		coolbar.setLayoutData(gd);

		// Start coolitem

		Composite comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		gl.numColumns = 9;
		gl.horizontalSpacing = 2;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		fInfoLabel = new Text(comp, SWT.BORDER);
		fInfoLabel.setText("Simulator not started yet.                ");
		fInfoLabel.setEditable(false);

		Label l = new Label(comp, SWT.NONE);
		l.setText(" Run :");

		fRunText = new Text(comp, SWT.BORDER);
		fRunText.setText("100");

		fTimeUnitLabel = new Label(comp, SWT.NONE);
		fTimeUnitLabel.setText("ns");

		ToolBar tb = new ToolBar(comp, SWT.FLAT);

		fRunTI = new ToolItem(tb, SWT.NONE);
		Image icon = ZamiaPlugin.getImage("/share/images/run.gif");
		fRunTI.setImage(icon);
		fRunTI.setToolTipText("Run");
		fRunTI.setEnabled(false);
		fRunTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {

					String str = fRunText.getText();

					int t = Integer.parseInt(str);

					fSimulator.run(BigInteger.valueOf(Math.round(t * fFSPerUnit)));
				} catch (ZamiaException ex) {
					MessageBox box = new MessageBox(fControl.getShell(), SWT.ICON_ERROR);
					box.setText("Simulator Error");
					box.setMessage("Simulator exception caught:\n" + ex);
					box.open();
				}
			}
		});
		fRestartTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/restart.gif");
		fRestartTI.setImage(icon);
		fRestartTI.setToolTipText("Restart");
		fRestartTI.setEnabled(false);
		fRestartTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					fSimulator.reset();
				} catch (ZamiaException e2) {
					MessageBox box = new MessageBox(fControl.getShell(), SWT.ICON_ERROR);
					box.setText("Simulator Error");
					box.setMessage("Simulator exception caught:\n" + e2);
					box.open();
				}
			}
		});
		fTraceTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/trace.gif");
		fTraceTI.setImage(icon);
		fTraceTI.setToolTipText("Trace...");
		fTraceTI.setEnabled(false);
		fTraceTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doTrace();
			}
		});
		fUnTraceTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/untrace.gif");
		fUnTraceTI.setImage(icon);
		fUnTraceTI.setToolTipText("Untrace");
		fUnTraceTI.setEnabled(false);
		fUnTraceTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doUnTrace();
			}
		});

		fNewLineTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/new_line.gif");
		fNewLineTI.setImage(icon);
		fNewLineTI.setToolTipText("Add new group/marker line");
		fNewLineTI.setEnabled(false);
		fNewLineTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doNewLine();
			}
		});

		CoolItem citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);

		// navigation coolitem

		comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		tb = new ToolBar(comp, SWT.FLAT);

		fPrevTransTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/prev_transition.gif");
		fPrevTransTI.setImage(icon);
		fPrevTransTI.setToolTipText("Go to previous transition");
		fPrevTransTI.setEnabled(false);
		fPrevTransTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				gotoTransition(false);
			}
		});

		fNextTransTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/next_transition.gif");
		fNextTransTI.setImage(icon);
		fNextTransTI.setToolTipText("Go to next transition");
		fNextTransTI.setEnabled(false);
		fNextTransTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				gotoTransition(true);
			}
		});

		fGotoCycleTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/goto_cycle.gif");
		fGotoCycleTI.setImage(icon);
		fGotoCycleTI.setToolTipText("Go to time/cycle");
		fGotoCycleTI.setEnabled(false);
		fGotoCycleTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				gotoCycle();
			}
		});

		citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);

		// zoom coolitem

		comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		tb = new ToolBar(comp, SWT.FLAT);
		tb.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		ToolItem ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoom100.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom 1:1");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				zoomDefault();
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoomin.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom In 2x");
		ti.setEnabled(true);
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				zoomIn();
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoomout.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom out 2x");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				zoomOut();
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/zoomFull.gif");
		ti.setImage(icon);
		ti.setToolTipText("Zoom Full");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				zoomFull();
			}
		});

		citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);

		// job coolitem

		comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		tb = new ToolBar(comp, SWT.FLAT);
		tb.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		fJobTI = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/RedDot0.gif");
		fJobTI.setImage(icon);
		fJobTI.setToolTipText("Stop");
		fJobTI.setEnabled(false);
		fJobTI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fScheduler.cancelCurrentJob();
			}
		});

		citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);

		/*
		 * waveform viewer
		 */

		fWVComposite = new Composite(fControl, SWT.NONE);
		fWVComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// grid layout please
		gl = new GridLayout();
		fWVComposite.setLayout(gl);
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		/*
		 * waveform display sash with table on the left, canvas on the right
		 */

		SashForm sash = new SashForm(fWVComposite, SWT.NONE);
		sash.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Composite treeComposite = new Composite(sash, SWT.NONE);

		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();

		treeComposite.setLayout(treeColumnLayout);

		fTree = new Tree(treeComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fTree.setLinesVisible(true);

		if (Util.isMotif()) {
			// dark gray because motif uses black as highlight color
			Color bg = new Color(fDisplay, 80, 80, 80);
			fTree.setBackground(bg);
		} else {
			fTree.setBackground(fBlack);
		}
		fTree.setForeground(fWhite);

		TreeColumn column1 = new TreeColumn(fTree, SWT.LEFT);
		column1.setText("Signal");
		treeColumnLayout.setColumnData(column1, new ColumnWeightData(50, 150, true));

		TreeColumn column2 = new TreeColumn(fTree, SWT.LEFT);
		column2.setText("Value");
		treeColumnLayout.setColumnData(column2, new ColumnWeightData(50, 4096, true));

		fTree.setHeaderVisible(true);
		fTree.setRedraw(true);
		fTree.pack();

		fTree.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem[] items = fTree.getSelection();

				if (items.length == 0)
					return;

				doDoubleClick((TraceLine) items[0].getData());
			}

			public void mouseDown(MouseEvent e) {
				fWaveformCanvas.redraw();
				fTree.forceFocus();
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		fTree.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent aE) {
			}

			public void widgetSelected(SelectionEvent aE) {
				moveCursor(fCursorTime);
				//repaint();
			}
		});

		fTree.addTreeListener(new TreeListener() {

			@Override
			public void treeCollapsed(TreeEvent aE) {
				if (aE.item instanceof TreeItem) {
					((TreeItem) aE.item).setExpanded(false);
				}
				repaint();
			}

			@Override
			public void treeExpanded(TreeEvent aE) {
				if (aE.item instanceof TreeItem) {
					((TreeItem) aE.item).setExpanded(true);
				}
				repaint();
			}
		});

		fTree.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent aE) {

				if ((aE.stateMask & SWT.CTRL) != 0) {
					if (aE.keyCode == SWT.ARROW_LEFT) {

						gotoTransition(false);
						aE.doit = false;

					} else if (aE.keyCode == SWT.ARROW_RIGHT) {

						gotoTransition(true);
						aE.doit = false;

					} else if (aE.keyCode == SWT.KEYPAD_ADD || aE.keyCode == 'I' || aE.keyCode == 'i' || aE.keyCode == '+') {

						zoomIn();
						aE.doit = false;

					} else if (aE.keyCode == SWT.KEYPAD_SUBTRACT || aE.keyCode == 'O' || aE.keyCode == 'o' || aE.keyCode == '-') {

						zoomOut();
						aE.doit = false;

					} else if (aE.keyCode == SWT.KEYPAD_SUBTRACT || aE.keyCode == 'F' || aE.keyCode == 'f') {

						zoomFull();
						aE.doit = false;

					} else if (aE.keyCode == SWT.KEYPAD_1 || aE.keyCode == '1') {

						zoomDefault();
						aE.doit = false;

					} else if (aE.keyCode == 'l') {

						doTrace();
						aE.doit = false;

					}

					if ((aE.stateMask & SWT.SHIFT) != 0) {

						if (aE.keyCode == 'g') {
							doFindReferences(false);
							aE.doit = false;
						}
						if (aE.keyCode == 'h') {
							doFindReferences(true);
							aE.doit = false;
						}
					}
				} else {

					if (aE.keyCode == '\b' || aE.keyCode == 127) {

						doUnTrace();
						aE.doit = false;

					}
				}
			}

			public void keyReleased(KeyEvent aE) {
				// TODO Auto-generated method stub

			}
		});

		/*
		 * tree drag and drop
		 */

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

		DragSource source = new DragSource(fTree, DND.DROP_MOVE);
		source.setTransfer(types);
		source.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {

				//DragSource ds = (DragSource) event.widget;

				ArrayList<TraceLine> selection = getSelectedTraces();

				StringBuilder buf = new StringBuilder();
				int n = selection.size();
				for (int i = 0; i < n; i++) {

					TraceLine tl = selection.get(i);
					if (tl instanceof TraceLineSignal) {

						TraceLineSignal tls = (TraceLineSignal) tl;

						if (!tls.isFullSignal()) {
							continue;
						}
					}

					buf.append(selection.get(i).getUID());
					if (i < n - 1) {
						buf.append(";");
					}
				}

				event.data = buf.toString();
			}
		});

		DropTarget target = new DropTarget(fTree, DND.DROP_MOVE);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}

				// Allow dropping text only
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				// Provide visual feedback
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			public void drop(DropTargetEvent event) {

				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {

					DropTarget target = (DropTarget) event.widget;
					Tree tree = (Tree) target.getControl();
					String data = (String) event.data;

					if (!(event.item instanceof TreeItem)) {
						return;
					}

					TreeItem treeItem = (TreeItem) event.item;

					TraceLine parentTL = (TraceLine) treeItem.getData();
					TreeItem parentItem = null;
					int index = -1;
					if (parentTL instanceof TraceLineSignal) {

						TraceLineSignal tls = (TraceLineSignal) parentTL;
						if (!tls.isFullSignal()) {
							return;
						}

						parentItem = treeItem.getParentItem();

						if (parentItem != null) {
							TreeItem[] items = parentItem.getItems();
							for (int i = 0; i < items.length; i++) {
								if (items[i] == treeItem) {
									index = i;
									break;
								}
							}
						} else {
							TreeItem[] items = tree.getItems();
							for (int i = 0; i < items.length; i++) {
								if (items[i] == treeItem) {
									index = i;
									break;
								}
							}
						}
					} else {
						if (parentTL instanceof TraceLineMarkers) {

							parentItem = treeItem;
							index = 0;

						} else {
							return;
						}
					}

					String uids[] = data.split(";");

					logger.info("SimulatorView: drop(): uids: %d", uids.length);

					if (uids != null) {

						for (int i = 0; i < uids.length; i++) {

							String uid = uids[i];
							TraceLine tl = fUIDTraceLineMap.get(uid);

							if (tl == null) {
								continue;
							}

							TreeItem item = fTraceLineTreeItemMap.get(tl);

							if (item == null) {
								continue;
							}

							item.dispose();

							if (parentItem != null) {
								item = new TreeItem(parentItem, SWT.NONE, index);
							} else {
								item = new TreeItem(fTree, SWT.NONE, index);
							}
							item.setData(tl);
							item.setText(new String[] { tl.getLabel(), "" });
							fTraceLineTreeItemMap.put(tl, item);
							item.setForeground(getColor(tl.getColor()));

						}
					}

					updateValueColumn();
					repaint();
					saveTraces();
				}
			}
		});

		/*
		 * tree tooltips
		 */

		fTree.setToolTipText("");

		// Implement a "fake" tooltip
		final Listener labelListener = new Listener() {
			public void handleEvent(Event event) {
				Label label = (Label) event.widget;
				Shell shell = label.getShell();
				switch (event.type) {
				case SWT.MouseDown:
					shell.dispose();
					break;
				case SWT.MouseExit:
					shell.dispose();
					break;
				}
			}
		};

		Listener treeTooltipListener = new Listener() {
			Shell tip = null;

			Label label = null;

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Dispose:
				case SWT.KeyDown:
				case SWT.MouseMove: {
					if (tip == null)
						break;
					tip.dispose();
					tip = null;
					label = null;
					break;
				}
				case SWT.MouseHover: {
					TreeItem item = fTree.getItem(new Point(event.x, event.y));
					if (item != null) {
						if (tip != null && !tip.isDisposed()) {
							tip.dispose();
						}

						TraceLine tl = (TraceLine) item.getData();
						if (tl == null) {
							return;
						}

						tip = new Shell(fShell, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
						tip.setBackground(fDisplay.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
						FillLayout layout = new FillLayout();
						layout.marginWidth = 2;
						tip.setLayout(layout);
						label = new Label(tip, SWT.NONE);
						label.setForeground(fDisplay.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
						label.setBackground(fDisplay.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
						//label.setData("_TABLEITEM", item);
						label.setText(tl.getToolTip());
						label.addListener(SWT.MouseExit, labelListener);
						label.addListener(SWT.MouseDown, labelListener);
						Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						Rectangle rect = item.getBounds(0);
						Point pt = fTree.toDisplay(rect.x, rect.y);
						tip.setBounds(pt.x, pt.y, size.x, size.y);
						tip.setVisible(true);
					}
				}
				}
			}
		};
		fTree.addListener(SWT.Dispose, treeTooltipListener);
		fTree.addListener(SWT.KeyDown, treeTooltipListener);
		fTree.addListener(SWT.MouseMove, treeTooltipListener);
		fTree.addListener(SWT.MouseHover, treeTooltipListener);

		createPopupMenu();

		fWaveformCanvas = new Canvas(sash, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		fWaveformCanvas.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				handleResize();
				startCanvasPaintJob();
			}
		});
		fWaveformCanvas.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem[] items = fTree.getSelection();

				if (items.length == 0)
					return;

				doDoubleClick((TraceLine) items[0].getData());
			}

			private TreeItem fSelectedItem;

			public void mouseDown(MouseEvent aEvent) {

				fSelectedItem = null;

				TreeItem[] items = fTree.getItems();
				int hh = Util.isMotif() ? 0 : fTree.getHeaderHeight();
				for (int i = 0; i < items.length; i++) {
					TreeItem item = items[i];

					if (selectRek(item, aEvent, hh)) {
						break;
					}
				}

				moveCursor(tXI(aEvent.x + fXOffset - BORDER_WIDTH));
				fMousePressed = true;
				fTree.forceFocus();

				if (fSelectedItem != null) {
					TraceLine tl = (TraceLine) fSelectedItem.getData();
					if (tl instanceof TraceLineMarkers) {

						TraceLineMarkers tlm = (TraceLineMarkers) tl;

						TraceLineMarker marker = tlm.findNearestMarker(aEvent.x);

						if (marker != null) {
							int xdiff = aEvent.x - marker.getX();

							//logger.info("xdiff: %d", xdiff);

							if (xdiff >= 0 && xdiff <= 16) {
								tlm.delete(marker);
							} else {
								String label = ZamiaPlugin.inputDialog(getSite().getShell(), "Marker Label", "Please enter a new label for the marker:", marker.getLabel());

								if (label == null || label.length() < 1) {
									return;
								}

								label = label.replace(':', ' ');

								marker.setLabel(label);
							}
							saveTraces();
							repaint();
						}
					}
				}

			}

			private boolean selectRek(TreeItem aItem, MouseEvent aEvent, int aHH) {
				Rectangle r = aItem.getBounds(0);
				int ypos = r.y + aHH;
				int ypos2 = r.y + r.height + aHH;

				if (ypos <= aEvent.y && ypos2 >= aEvent.y) {

					fTree.setSelection(aItem);

					fSelectedItem = aItem;

					return true;
				} else {
					int n = aItem.getItemCount();
					for (int i = 0; i < n; i++) {
						if (selectRek(aItem.getItem(i), aEvent, aHH)) {
							return true;
						}
					}
				}
				return false;
			}

			public void mouseUp(MouseEvent e) {
				fMousePressed = false;
			}
		});
		fWaveformCanvas.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (fMousePressed) {
					double d = (double) (e.x - BORDER_WIDTH + fXOffset) / fPixelsPerUnit * fFSPerUnit;
					moveCursor(BigInteger.valueOf((long) d).add(fStartTime));
				}
			}
		});

		/* Set up the paint canvas scroll bars */
		ScrollBar horizontal = fWaveformCanvas.getHorizontalBar();
		horizontal.setVisible(true);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		horizontal = fTree.getHorizontalBar();
		horizontal.setVisible(true);

		ScrollBar vertical = fWaveformCanvas.getVerticalBar();
		vertical.setVisible(false);

		vertical = fTree.getVerticalBar();
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
		handleResize();
		fWaveformCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent aPaintEvent) {
				paintWaveformCanvas(aPaintEvent);
			}

		});

		int weights[] = { 45, 55 };
		sash.setWeights(weights);

		fWVComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				shutdown();
			}
		});
	}

	private void createPopupMenu() {
		fPopupMenu = new Menu(fTree.getShell(), SWT.POP_UP);

		/*
		 * Mode menu
		 */

		MenuItem modeItem = new MenuItem(fPopupMenu, SWT.CASCADE);
		modeItem.setText("Mode");

		Menu modeMenu = new Menu(fPopupMenu);
		modeItem.setMenu(modeMenu);

		MenuItem item = new MenuItem(modeMenu, SWT.PUSH);
		item.setText("Binary");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				setSignalDisplayMode(TraceDisplayMode.BIN);
			}
		});
		item = new MenuItem(modeMenu, SWT.PUSH);
		item.setText("Octal");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				setSignalDisplayMode(TraceDisplayMode.OCT);
			}
		});
		item = new MenuItem(modeMenu, SWT.PUSH);
		item.setText("Decimal");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				setSignalDisplayMode(TraceDisplayMode.DEC);
			}
		});
		item = new MenuItem(modeMenu, SWT.PUSH);
		item.setText("Hexadecimal");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				setSignalDisplayMode(TraceDisplayMode.HEX);
			}
		});

		/*
		 * Color menu
		 */
		MenuItem colorItem = new MenuItem(fPopupMenu, SWT.CASCADE);
		colorItem.setText("Color");

		Menu colorMenu = new Menu(fPopupMenu);
		colorItem.setMenu(colorMenu);

		for (int i = 0; i < 16; i++) {
			item = new MenuItem(colorMenu, SWT.PUSH);
			item.setText(fColorNames[i]);
			item.setData(new Integer(i));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					MenuItem item = (MenuItem) e.widget;
					int color = ((Integer) item.getData()).intValue();
					setSignalColor(color);
				}
			});
		}

		item = new MenuItem(fPopupMenu, SWT.SEPARATOR);

		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Add Array Slice...");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				doAddArraySlice();
			}
		});
		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Untrace");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				doUnTrace();
			}
		});
		item = new MenuItem(fPopupMenu, SWT.SEPARATOR);
		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Goto Declaration");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TreeItem[] items = fTree.getSelection();

				if (items.length == 0)
					return;

				TraceLine tl = (TraceLine) items[0].getData();

				doDoubleClick(tl);
			}
		});
		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Find References CTRL+SHIFT+G");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				doFindReferences(false);
			}

		});
		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Find Drivers CTRL+SHIFT+H");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				doFindReferences(true);
			}

		});
		item = new MenuItem(fPopupMenu, SWT.SEPARATOR);
		item = new MenuItem(fPopupMenu, SWT.PUSH);
		item.setText("Copy Path to Clipboard");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TreeItem[] items = fTree.getSelection();

				if (items.length == 0)
					return;

				TraceLine tl = (TraceLine) items[0].getData();

				if (!(tl instanceof TraceLineSignal)) {
					return;
				}

				TraceLineSignal tls = (TraceLineSignal) tl;

				StringSelection ss = new StringSelection(tls.getSignalPath().toString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		});

	}

	class SimRunJob extends Job {

		private SimRunnerConfig fNewConfig;

		// private RTLGraph fRTLG;

		public SimRunJob(SimRunnerConfig aConfig) {
			super("(Re-)Launch simulator");
			fNewConfig = aConfig;
		}

		@Override
		protected IStatus run(IProgressMonitor aMonitor) {

			try {
				fSimJobLock.lock();

				Display display = getSite().getShell().getDisplay();

				display.syncExec(new Runnable() {

					public void run() {
						disconnect();

						if (fSimulator != null) {
							fSimulator.shutdown();
							fInfoLabel.setText("Simulator is not running.");

							fTraceTI.setEnabled(false);
							fUnTraceTI.setEnabled(false);
							fNewLineTI.setEnabled(false);
							fRunTI.setEnabled(false);
							fRestartTI.setEnabled(false);

							fPrevTransTI.setEnabled(false);
							fNextTransTI.setEnabled(false);
							fGotoCycleTI.setEnabled(false);
						}
					}
				});

				fConfig = fNewConfig;

				int sim = fConfig.getSimulator();

				DMUID tlDUUID = fConfig.getToplevel();
				// String libId = tlDUUID.getLibId();
				// String entityId = tlDUUID.getId();
				// String archId = tlDUUID.getArchId();
				Toplevel tl = new Toplevel(tlDUUID, null);
				ToplevelPath tlp = new ToplevelPath(tl, new PathName(""));

				IProject prj = fConfig.getProject();

				ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

				if (sim == SimRunnerConfig.SIM_BUILTIN) {

					// FIXME

					throw new RuntimeException("Sorry, broken.");

					// // built-in simulator
					//
					// try {
					// fRTLG = zprj.elaborate(libId, entityId, archId);
					//
					// fRTLG.link(zprj);
					//
					// display.syncExec(new Runnable() {
					//
					// public void run() {
					//
					// IWorkbenchWindow window =
					// ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
					//
					// IWorkbenchPage page = window.getActivePage();
					//
					// RTLView rtlView = (RTLView)
					// page.findView("org.zamia.plugin.views.rtl.RTLView");
					//
					// rtlView.setRTLGraph(fRTLG);
					//
					// try {
					// Simulator simulator = new Simulator();
					// fSimulator = simulator;
					// fSimulator.open(fRTLG);
					//
					// fInfoLabel.setText("Built-In Simulator: " +
					// fRTLG.getInstanceName());
					// } catch (ZamiaException e) {
					// fSimulator = null;
					// }
					// }
					// });
					//
					// } catch (ZamiaException e) {
					// el.logException(e);
					// String lstr = e.getLocation() != null ?
					// e.getLocation().toString() : "unknown";
					// ZamiaPlugin.showError(fControl.getShell(), "Error",
					// e.getMessage(), lstr);
					// } catch (Exception e) {
					// el.logException(e);
					// ZamiaPlugin.showError(fControl.getShell(), "Error",
					// e.getMessage(), "unknown");
					// }
				} else if (sim == SimRunnerConfig.SIM_VCD_IMPORT) {

					// VCD importer

					VCDImport vcdImport = new VCDImport();
					fSimulator = vcdImport;

					String filename = fConfig.getFilename();
					try {
						File f = new File(filename);

						fSimulator.open(tlp, f, new PathName(fConfig.getPrefix()), zprj);

						display.syncExec(new Runnable() {
							public void run() {
								String filename = fConfig.getFilename();
								File f = new File(filename);
								fInfoLabel.setText(f.getName());
							}
						});

					} catch (Exception e) {
						el.logException(e);
						ZamiaPlugin.showError(fControl.getShell(), "Error while importing VCD File", "While parsing the VCD file\n" + filename + "\nan error occured:\n" + e, "unknown.");
						fSimulator = null;
					}
				}

				display.syncExec(new Runnable() {
					public void run() {
						if (fSimulator != null) {

							connect();

							fTraceDialog.connect(fSimulator);

							fTraceTI.setEnabled(true);
							fUnTraceTI.setEnabled(true);
							fNewLineTI.setEnabled(true);
							fRunTI.setEnabled(fSimulator.isSimulator());
							fRestartTI.setEnabled(fSimulator.isSimulator());

							fPrevTransTI.setEnabled(true);
							fNextTransTI.setEnabled(true);
							fGotoCycleTI.setEnabled(true);

							int sim = fConfig.getSimulator();
							if (sim == SimRunnerConfig.SIM_AET_IMPORT) {
								fUnitName = "cy";
							} else {
								fUnitName = "ns";
							}
							fTimeUnitLabel.setText(fUnitName);

						} else {
							fInfoLabel.setText("No simulator started yet.          ");

							fTraceTI.setEnabled(false);
							fUnTraceTI.setEnabled(false);
							fNewLineTI.setEnabled(false);
							fRunTI.setEnabled(false);
							fRestartTI.setEnabled(false);

							fPrevTransTI.setEnabled(false);
							fNextTransTI.setEnabled(false);
							fGotoCycleTI.setEnabled(false);
						}
					}
				});

			} catch (Throwable t) {
				el.logException(t);
			} finally {
				fSimJobLock.unlock();
			}
			return Status.OK_STATUS;
		}
	}

	public void run(SimRunnerConfig aConfig, IProgressMonitor aMonitor) {
		SimRunJob job = new SimRunJob(aConfig);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	public void setFocus() {
		if (fControl != null) {
			fControl.setFocus();
		}
	}

	private void calcSize(CoolItem item) {
		Control control = item.getControl();
		Point pt = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pt = item.computeSize(pt.x, pt.y);
		item.setSize(pt);
	}

	private void doDoubleClick(TraceLine aTL) {
		if (aTL instanceof TraceLineSignal) {
			TraceLineSignal tls = (TraceLineSignal) aTL;
			showSource(tls.getSignalPath());
		} else if (aTL instanceof TraceLineMarkers) {

			String label = ZamiaPlugin.inputDialog(getSite().getShell(), "Marker Label", "Please enter a label for the new marker:", "");

			if (label == null || label.length() < 1) {
				return;
			}

			label = label.replace(':', ' ');

			TraceLineMarkers tlm = (TraceLineMarkers) aTL;
			tlm.addMarker(fCursorTime, label);

			saveTraces();
			repaint();

			fMousePressed = false;
		}
	}

	private void showSource(PathName aPath) {

		logger.debug("SimulatorView: showSource, path=%s", aPath);

		if (fConfig == null) {
			return;
		}

		PathName path = aPath;

		logger.debug("SimulatorView: showSource, path after prefix removal: %s", path);

		IProject prj = fConfig.getProject();

		ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

		IGManager igm = zprj.getIGM();

		Toplevel tl = new Toplevel(fConfig.getToplevel(), null);

		IGItem item = igm.findItem(tl, path);

		if (item != null) {

			SourceLocation location = item.computeSourceLocation();

			IEditorPart ep = ZamiaPlugin.showSource(getSite().getPage(), prj, location, 0);

			if (ep instanceof ZamiaEditor) {
				ZamiaEditor editor = (ZamiaEditor) ep;

				editor.setPath(new ToplevelPath(tl, path.getNullParent()));
			}
		}
	}

	public boolean hasSignal(PathName aPath) {
		if (fSimulator == null) {
			return false;
		}

		PathName path = aPath;

		List<PathName> res = fSimulator.findSignalNamesRegexp(path.toString(), 1);
		return res != null && !res.isEmpty();
	}

	int getNextColor() {
		return fColor++ % NUM_COLORS;
	}

	public void trace(PathName aSignalPath) {

		PathName path = aSignalPath;

		if (fSimulator != null) {

			IGTypeStatic type = getSignalType(path);

			if (type == null) {
				logger.error("SimulatorView: Was asked to add trace for %s, but failed to compute a type for it.", path);
				return;
			}

			TraceLineSignal tls = new TraceLineSignal(aSignalPath, TraceDisplayMode.HEX, getNextColor(), type);

			try {
				fSimulator.trace(path);
				
				ArrayList<TraceLine> sel = getSelectedTraces();
				int n = sel.size();
				TraceLine parent = null;
				for (int i = 0; i<n; i++) {
					TraceLine tl = sel.get(i);
					if (tl instanceof TraceLineMarkers) {
						parent = tl;
					}
				}
				addTrace(tls, parent);
				syncedRepaint();
				selectAndReveal(path);
				saveTraces();
			} catch (ZamiaException e) {
				el.logException(e);
			}

		} else {
			ZamiaPlugin.showError(getSite().getShell(), "No simulator", "Please launch a simulator first.", "Sim viewer is disconnected.");
		}
	}

	public Toplevel getToplevel() {
		if (fConfig == null)
			return null;

		DMUID duuid = fConfig.getToplevel();

		return new Toplevel(duuid, null);
	}

	private void gotoTransition(boolean aNextTransition) {
		ArrayList<TraceLine> ss = getSelectedTraces();

		BigInteger time = getCursorTime();

		GotoTransitionJob job = new GotoTransitionJob(aNextTransition, ss, time);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private void gotoCycle() {
		BigInteger fsPerUnit = BigInteger.valueOf((long) fFSPerUnit);

		long ct = getCursorTime().divide(fsPerUnit).longValue();

		String timeStr = ZamiaPlugin.inputDialog(getSite().getShell(), "Enter Time", "Time to move cursor to:", "" + ct);

		if (timeStr == null) {
			return;
		}

		try {
			BigInteger newTime = new BigInteger(timeStr).multiply(fsPerUnit);

			moveCursor(newTime);
		} catch (Throwable t) {
			el.logException(t);
		}
	}

	void setRedDotState(int aState) {
		Display display = getViewSite().getShell().getDisplay();
		final int state = aState;
		display.asyncExec(new Runnable() {
			public void run() {
				Image icon = ZamiaPlugin.getImage("/share/images/RedDot" + state + ".gif");
				fJobTI.setImage(icon);
				fJobTI.setEnabled(state > 0);
			}
		});
	}

	private void doAddArraySlice() {
		try {
			ArrayList<TraceLine> sel = getSelectedTraces();
			int n = sel.size();

			if (n != 1) {
				ZamiaPlugin.showError(getSite().getShell(), "Selection Error", "Please select exactly one signal", "Multiple/no signals selected");
				return;
			}

			TraceLine tl = sel.get(0);

			String sliceStr = ZamiaPlugin.inputDialog(getSite().getShell(), "Enter Slice", "Please specify the array range (slice) to trace,\nfor a single element specify its index, e.g. 23 or 42\nfor a slice specify the range, e.g. 23:42", "");

			if (sliceStr == null) {
				return;
			}

			int min = 0, max = 0;

			if (sliceStr.contains(":")) {
				String[] parts = sliceStr.split(":");
				if (parts.length != 2) {
					ZamiaPlugin.showError(getSite().getShell(), "Slice String Parse Error", "Failed to parse " + sliceStr, "Wrong number of boundaries given.");
					return;
				}
				try {
					min = Integer.parseInt(parts[0]);
					max = Integer.parseInt(parts[1]);

					if (min > max) {
						int h = min;
						min = max;
						max = h;
					}

				} catch (Throwable t) {
					ZamiaPlugin.showError(getSite().getShell(), "Slice String Parse Error", "Failed to parse " + sliceStr, t.toString());
					return;
				}

			} else {
				try {
					min = Integer.parseInt(sliceStr);
					max = min;
				} catch (Throwable t) {
					ZamiaPlugin.showError(getSite().getShell(), "Slice String Parse Error", "Failed to parse " + sliceStr, t.toString());
					return;
				}
			}

			addTraceSlice(tl, min, max, getNextColor(), TraceDisplayMode.HEX);

			saveTraces();
			repaint();
		} catch (Throwable t) {
			el.logException(t);
			ZamiaPlugin.showError(getSite().getShell(), "Exception caught", "Exception caught - see log for details.", t.toString());
			return;
		}
	}

	private void doTrace() {
		fTraceDialog.open();
		Object sel[] = fTraceDialog.getResult();
		if (sel == null)
			return;
		for (int i = 0; i < sel.length; i++) {
			if (sel[i] instanceof PathName) {
				PathName path = (PathName) sel[i];
				try {
					if (fSimulator.isSimulator()) {
						fSimulator.trace(path);
					}
					trace(path);
				} catch (Exception e1) {
					el.logException(e1);
				}
			}
		}
		repaint();
	}

	private void doUnTrace() {
		ArrayList<TraceLine> sel = getSelectedTraces();
		int n = sel.size();
		for (int i = 0; i < n; i++) {

			TraceLine tl = sel.get(i);

			if (tl instanceof TraceLineSignal) {
				TraceLineSignal tls = (TraceLineSignal) tl;
				try {
					fSimulator.unTrace(tls.getSignalPath());
				} catch (ZamiaException e1) {
					el.logException(e1);
				}
				fSignalPathTraceLineMap.remove(tls);
			}

			TreeItem item = fTraceLineTreeItemMap.get(tl);
			if (item != null) {
				item.dispose();
			}
			fUIDTraceLineMap.remove(tl.getUID());
			fTraceLineTreeItemMap.remove(tl);
		}
		saveTraces();
		repaint();
	}

	private void doNewLine() {
		String label = ZamiaPlugin.inputDialog(getSite().getShell(), "Marker Line Label", "Please enter a label for the new marker line:", "New marker line");
		if (label == null) {
			return;
		}
		label = label.replace(':', ' ');

		TraceLineMarkers tl = new TraceLineMarkers(label, getNextColor());

		addTrace(tl, null);

		saveTraces();
		repaint();

		selectAndReveal(tl);
	}

	SimRunnerConfig getConfig() {
		return fConfig;
	}

	private void setSignalColor(int aColor) {
		ArrayList<TraceLine> sel = getSelectedTraces();

		int n = sel.size();
		for (int i = 0; i < n; i++) {

			TraceLine tl = sel.get(i);

			if (tl == null) {
				continue;
			}

			tl.setColor(aColor);

			TreeItem item = fTraceLineTreeItemMap.get(tl);
			if (item != null) {
				item.setForeground(fColors[aColor]);
			}

		}
		saveTraces();
		repaint();
	}

	private void setSignalDisplayMode(TraceDisplayMode aMode) {
		ArrayList<TraceLine> sel = getSelectedTraces();

		int n = sel.size();
		for (int i = 0; i < n; i++) {

			TraceLine tl = sel.get(i);

			if (tl == null) {
				continue;
			}

			if (!(tl instanceof TraceLineSignal)) {
				continue;
			}

			TraceLineSignal tls = (TraceLineSignal) tl;
			tls.setTDM(aMode);
		}
		saveTraces();
		updateValueColumn();
		repaint();
	}

	private void connect() {

		fCursor = fSimulator.createCursor();

		fSignalPathTraceLineMap = new HashMap<PathName, TraceLine>();
		fUIDTraceLineMap = new HashMap<String, TraceLine>();
		fTraceLineTreeItemMap = new HashMap<TraceLine, TreeItem>();

		fStartTime = fSimulator.getStartTime();
		fEndTime = fSimulator.getEndTime();
		fCursorTime = fStartTime;
		fFSPerUnit = DEFAULT_FS_PER_UNIT;
		fPixelsPerUnit = DEFAULT_PIXELS_PER_UNIT;

		fSimulator.addObserver(this);

		fWaveformCanvas.setMenu(fPopupMenu);
		fTree.setMenu(fPopupMenu);

		notifyChanges(fSimulator, fSimulator.getEndTime());

		fScheduler = new WaveformPaintJobScheduler(this);
		fSchedulerThread = new Thread(fScheduler);
		fSchedulerThread.start();

		loadTraces();
	}

	private void disconnect() {

		if (fScheduler != null) {
			fScheduler.cancel();
			try {
				fSchedulerThread.join();
			} catch (InterruptedException e) {
				el.logException(e);
			}
			fScheduler = null;
			fSchedulerThread = null;
		}

		if (fSimulator != null) {
			fSimulator.removeObserver(this);
		}
		fSimulator = null;
		if (fCursor != null) {
			fCursor.dispose();
			fCursor = null;
		}

		fTree.removeAll();
		fUIDTraceLineMap = new HashMap<String, TraceLine>();
		fTraceLineTreeItemMap = new HashMap<TraceLine, TreeItem>();
		fSignalPathTraceLineMap = new HashMap<PathName, TraceLine>();

		fStartTime = BigInteger.ZERO;
		fEndTime = BigInteger.ZERO;
		fCursorTime = BigInteger.ZERO;

		repaint();
	}

	public void notifyReset(IGISimulator aSim) {
		fStartTime = fSimulator.getStartTime();
		fEndTime = fSimulator.getEndTime();
		fCursorTime = fStartTime;
		fDisplay.asyncExec(new Runnable() {
			public void run() {
				handleResize();
				repaint();
			}
		});
	}

	public void notifyChanges(IGISimulator aSimulator, BigInteger aTime) {
		if (aTime.compareTo(fEndTime) > 0) {
			fCursorTime = aTime;
		}
		fEndTime = aTime;
		fDisplay.asyncExec(new Runnable() {
			public void run() {
				handleResize();
				repaint();
			}
		});
	}

	/**
	 * Make sure cursor stays visible, adapt horizontal scroll position if
	 * necessary
	 * 
	 * @return true if scrolling position had to be adjusted, false otherwise
	 */
	private boolean autoscroll() {

		boolean positionAdjusted = false;

		// cursor position:
		int cx = tX(fCursorTime);

		int cxa = cx - fXOffset;

		if (cxa > fVisibleWidth) {
			ScrollBar horizontal = fWaveformCanvas.getHorizontalBar();
			if (horizontal != null) {
				horizontal.setEnabled(true);
				fXOffset = cx - fVisibleWidth + 30;

				horizontal.setValues(fXOffset, 0, fImageWidth, fVisibleWidth, 8, fVisibleWidth / 2);
			}
			positionAdjusted = true;
		} else if (cxa < 0) {
			ScrollBar horizontal = fWaveformCanvas.getHorizontalBar();
			if (horizontal != null) {
				horizontal.setEnabled(true);
				fXOffset = cx - 30;
				if (fXOffset < 0)
					fXOffset = 0;

				horizontal.setValues(fXOffset, 0, fImageWidth, fVisibleWidth, 8, fVisibleWidth / 2);
			}
			positionAdjusted = true;
		}
		return positionAdjusted;
	}

	private void repaint() {
		autoscroll();
		startCanvasPaintJob();
	}

	void syncedRepaint() {
		fDisplay.asyncExec(new Runnable() {
			public void run() {
				repaint();
			}
		});
	}

	/**
	 * Will only redraw the waveformCanvas
	 * 
	 * intended to be called by WaveformPaintJob only.
	 */
	void syncedCanvasRedraw() {
		fDisplay.asyncExec(new Runnable() {
			public void run() {
				fWaveformCanvas.redraw();
			}
		});
	}

	int tX(BigInteger aTime) {

		double t = aTime.subtract(fStartTime).doubleValue();

		return BORDER_WIDTH + (int) (t / fFSPerUnit * fPixelsPerUnit);
	}

	int tW(BigInteger aTime) {

		double w = aTime.doubleValue() / fFSPerUnit * fPixelsPerUnit;

		return (int) w;
	}

	BigInteger tWI(double aX) {
		double d = aX / fPixelsPerUnit * fFSPerUnit;
		return BigInteger.valueOf((long) d);
	}

	BigInteger tXI(double aX) {
		return tWI(aX).add(fStartTime);
	}

	private void paintWaveformCanvas(PaintEvent aPaintEvent) {
		GC gc = aPaintEvent.gc;

		Rectangle clientArea = fWaveformCanvas.getClientArea();
		gc.setClipping(clientArea);

		if (fOffscreenImage == null) {
			gc.setBackground(fDisplay.getSystemColor(SWT.COLOR_BLACK));
			gc.setForeground(fDisplay.getSystemColor(SWT.COLOR_GREEN));
			gc.fillRectangle(0, 0, clientArea.width, clientArea.height);

			gc.drawText("No Data", 20, clientArea.height / 2);
		} else {

			int w = fOffscreenImage.getBounds().width > clientArea.width ? clientArea.width : fOffscreenImage.getBounds().width;
			int h = fOffscreenImage.getBounds().height > clientArea.height ? clientArea.height : fOffscreenImage.getBounds().height;

			gc.drawImage(fOffscreenImage, 0, 0, w, h, 0, 0, w, h);

			/*
			 * Highlight selected traces
			 */

			TreeItem[] sitems = fTree.getSelection();
			HashSet<TraceLine> selectedTraces = new HashSet<TraceLine>();
			for (int i = 0; i < sitems.length; i++) {
				TraceLine tl = (TraceLine) sitems[i].getData();
				selectedTraces.add(tl);
			}

			gc.setAlpha(100);

			TreeItem items[] = fTree.getItems();
			for (int i = 0; i < items.length; i++) {

				drawHighlight(items[i], selectedTraces, gc);

			}

			gc.setAlpha(255);

			/*
			 * draw cursor
			 */

			int textHeight = gc.getFontMetrics().getHeight();

			int cx = tX(fCursorTime) - fXOffset;
			gc.setAlpha(125);
			gc.setBackground(fYellow);
			gc.setForeground(fYellow);
			gc.fillRectangle(cx - 2, textHeight, 4, clientArea.height - textHeight);
			gc.setAlpha(255);
			gc.setBackground(fBlack);

			BigInteger fsPerUnit = BigInteger.valueOf((long) fFSPerUnit);
			String timeStr = "" + fCursorTime.divide(fsPerUnit) + fUnitName;
			int timeStrWidth = gc.textExtent(timeStr).x;
			gc.fillRectangle(cx - timeStrWidth / 2 - 1, 0, timeStrWidth + 2, textHeight);
			gc.drawText(timeStr, cx - timeStrWidth / 2, 0);
			gc.drawRectangle(cx - timeStrWidth / 2 - 1, 0, timeStrWidth + 2, textHeight);

			BigInteger timeOffset = tXI(fXOffset);
			BigInteger endTimeOffset = tXI(fXOffset + fVisibleWidth);

			if (endTimeOffset.compareTo(fStartTime) < 0) {
				endTimeOffset = fStartTime;
			}
			if (endTimeOffset.compareTo(fEndTime) > 0) {
				endTimeOffset = fEndTime;
			}
			if (timeOffset.compareTo(fStartTime) < 0) {
				timeOffset = fStartTime;
			}
			if (timeOffset.compareTo(fEndTime) > 0) {
				timeOffset = fEndTime;
			}

		}
	}

	private void drawHighlight(TreeItem aTreeItem, HashSet<TraceLine> aSelectedTraces, GC aGC) {

		if (aTreeItem.getExpanded()) {
			int n = aTreeItem.getItemCount();
			for (int i = 0; i < n; i++) {
				TreeItem child = aTreeItem.getItem(i);
				drawHighlight(child, aSelectedTraces, aGC);
			}
		}

		if (aSelectedTraces.contains(aTreeItem.getData())) {
			int hh = Util.isMotif() ? 0 : fTree.getHeaderHeight();
			Rectangle clientArea = fWaveformCanvas.getClientArea();

			Rectangle r = aTreeItem.getBounds(0);
			int ypos = r.y + hh;
			if (ypos < 0 || ypos > clientArea.height) {
				return;
			}

			aGC.setForeground(fWhite);
			aGC.setBackground(fWhite);

			aGC.fillRectangle(0, ypos, clientArea.width, r.height);
		}
	}

	public String getSignalValueStr(PathName aSignalPath, BigInteger aTime) {
		try {

			if (!fCursor.gotoTransition(aSignalPath, aTime)) {
				return "???";
			}
			IGStaticValue value = fCursor.getCurrentValue();
			if (value == null) {
				return "---";
			}

			return formatSignalValue(aSignalPath, value);
		} catch (Throwable t) {
			return "???";
		}
	}

	private void startCanvasPaintJob() {

		Rectangle clientArea = fWaveformCanvas.getClientArea();

		WaveformPaintJob job = new WaveformPaintJob(this, clientArea);

		if (fScheduler != null) {
			fScheduler.schedule(job);
		}
	}

	private void zoomIn() {

		int cursorXOld = tX(fCursorTime);

		fPixelsPerUnit *= 2;

		int cursorXNew = tX(fCursorTime);

		fXOffset += cursorXNew - cursorXOld;

		handleResize();
		repaint();
	}

	private void zoomOut() {

		int cursorXOld = tX(fCursorTime);

		fPixelsPerUnit /= 2;

		int cursorXNew = tX(fCursorTime);

		fXOffset += cursorXNew - cursorXOld;

		handleResize();
		repaint();
	}

	private void zoomFull() {
		fPixelsPerUnit = (fVisibleWidth * fFSPerUnit) / (fEndTime.subtract(fStartTime).doubleValue());
		handleResize();
		repaint();
	}

	private void zoomDefault() {

		int cursorXOld = tX(fCursorTime);

		fPixelsPerUnit = DEFAULT_PIXELS_PER_UNIT;

		int cursorXNew = tX(fCursorTime);

		fXOffset += cursorXNew - cursorXOld;

		handleResize();
		repaint();
	}

	private void handleResize() {
		fWVComposite.update();

		Rectangle visibleRect = fWaveformCanvas.getClientArea();
		fVisibleWidth = visibleRect.width - BORDER_WIDTH;

		long totalWidth = fEndTime != null ? tW(fEndTime.subtract(fStartTime)) : 0l;

		fImageWidth = (int) totalWidth + 30;

		ScrollBar horizontal = fWaveformCanvas.getHorizontalBar();
		if (horizontal != null) {
			fXOffset = Math.min(fXOffset, fImageWidth - fVisibleWidth);
			int max = fImageWidth - fVisibleWidth;
			if (max < 0) {
				horizontal.setEnabled(false);
				horizontal.setSelection(0);
				fXOffset = 0;
				horizontal.setValues(fXOffset, 0, fVisibleWidth, fVisibleWidth, 8, fVisibleWidth);
			} else {

				horizontal.setEnabled(true);
				horizontal.setValues(fXOffset, 0, fImageWidth, fVisibleWidth, 8, fVisibleWidth);
			}
		}

		// repaint();
	}

	private void scrollHorizontally(ScrollBar aScrollBar) {
		if (fImageWidth > fVisibleWidth) {
			final int oldOffset = fXOffset;
			final int newOffset = Math.min(aScrollBar.getSelection(), fImageWidth - fVisibleWidth);
			if (oldOffset != newOffset) {
				//fWVComposite.update();
				fXOffset = newOffset;
				startCanvasPaintJob();
			}
		}
	}

	private void scrollVertically(ScrollBar aScrollBar) {
		final int oldOffset = fYOffset;
		final int newOffset = aScrollBar.getSelection();
		if (oldOffset != newOffset) {
			fWaveformCanvas.update();
			fYOffset = newOffset;
			startCanvasPaintJob();
		}
	}

	private void shutdown() {
		if (fSimulator != null) {
			fSimulator.removeObserver(this);
		}
	}

	private ArrayList<TraceLine> getSelectedTraces() {
		TreeItem tis[] = fTree.getSelection();

		ArrayList<TraceLine> sel = new ArrayList<TraceLine>(tis.length);
		for (int i = 0; i < tis.length; i++) {

			Object data = tis[i].getData();

			sel.add((TraceLine) data);
		}

		return sel;
	}

	private void moveCursor(BigInteger aTime) {

		BigInteger time = aTime.subtract(aTime.mod(BigInteger.valueOf((long) fFSPerUnit)));

		if (time.compareTo(fStartTime) < 0) {
			time = fStartTime;
		} else if (time.compareTo(fEndTime) > 0) {
			time = fEndTime;
		}
		fCursorTime = time;

		updateValueColumn();

		if (autoscroll()) {
			startCanvasPaintJob();
		} else {
			fWaveformCanvas.redraw();
		}
	}

	private void updateValueColumnRek(TreeItem aItem) {

		TraceLine tl = (TraceLine) aItem.getData();
		aItem.setText(1, tl.getValueStr(fCursor, fCursorTime));

		int n = aItem.getItemCount();
		for (int i = 0; i < n; i++) {
			TreeItem child = aItem.getItem(i);
			updateValueColumnRek(child);
		}
	}

	private void updateValueColumn() {
		TreeItem items[] = fTree.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			updateValueColumnRek(item);
		}

		TreeColumn col = fTree.getColumns()[1];
		col.pack();
	}

	public BigInteger getCursorTime() {
		return fCursorTime;
	}

	TraceLineSignal addTraceSignal(TraceLine aParent, PathName aSignalPath, int aColor, TraceDisplayMode aTDM) throws ZamiaException {

		IGTypeStatic type = getSignalType(aSignalPath);

		if (type == null) {
			throw new ZamiaException("Couldn't find " + aSignalPath);
		}

		TraceLineSignal tls = new TraceLineSignal(aSignalPath, aTDM, aColor, type);

		addTrace(tls, aParent);

		return tls;
	}

	TraceLineSignalRF addTraceRecordField(TraceLine aParent, String aField, int aColor, TraceDisplayMode aTDM) throws ZamiaException {
		if (aParent == null || !(aParent instanceof TraceLineSignal)) {
			throw new ZamiaException("SimulatorView: addTraceRecordField: invalid parent: " + aParent);
		}

		TraceLineSignal tls = (TraceLineSignal) aParent;

		IGTypeStatic type = tls.getType();

		if (!type.isRecord()) {
			throw new ZamiaException("SimulatorView: addTraceRecordField: Not a record type: " + type);
		}

		IGTypeStatic subtype = null;

		int n = type.getNumRecordFields(null);
		for (int i = 0; i < n; i++) {
			IGRecordField rf = type.getRecordField(i, null);
			if (rf.getId().equalsIgnoreCase(aField)) {
				subtype = type.getStaticRecordFieldType(i);
				break;
			}
		}

		if (subtype == null) {
			throw new ZamiaException("SimulatorView: addTraceRecordField: Field not found: " + aField);
		}

		TraceLineSignalRF tlsrf = new TraceLineSignalRF(tls, aField, aTDM, aColor, subtype);

		addTrace(tlsrf, aParent);

		return tlsrf;
	}

	TraceLineSignalArraySlice addTraceSlice(TraceLine aParent, int aMin, int aMax, int aColor, TraceDisplayMode aTDM) throws ZamiaException {
		if (aParent == null || !(aParent instanceof TraceLineSignal)) {
			throw new ZamiaException("SimulatorView: addTraceRecordField: invalid parent: " + aParent);
		}

		TraceLineSignal tls = (TraceLineSignal) aParent;

		IGTypeStatic type = tls.getType();

		if (!type.isArray()) {
			throw new ZamiaException("SimulatorView: addTraceSlice: Not an array type: " + type);
		}

		IGTypeStatic subtype = null;

		int min = aMin;
		int max = aMax;

		if (min > max) {
			int h = min;
			min = max;
			max = h;
		}

		IGTypeStatic idxType = type.getStaticIndexType(null);

		IGStaticValue range = idxType.getStaticRange();

		boolean ascending = range.getAscending().isTrue();

		IGStaticValue rangeLeft = range.getLeft(null);
		IGStaticValue rangeRight = range.getRight(null);

		int rangeMin = ascending ? rangeLeft.getInt() : rangeRight.getInt();
		int rangeMax = ascending ? rangeRight.getInt() : rangeLeft.getInt();

		if (min < rangeMin || max > rangeMax) {
			throw new ZamiaException("SimulatorView: addTraceSlice: Out of bounds. Legal boundary: " + rangeMin + ":" + rangeMax);
		}

		if (min != max) {

			int left = min;
			int right = max;

			if (!ascending) {
				left = max;
				right = min;
			}

			IGStaticValue leftValue = new IGStaticValueBuilder(rangeLeft, null).setNum(left).buildConstant();
			IGStaticValue rightValue = new IGStaticValueBuilder(rangeRight, null).setNum(right).buildConstant();
			IGStaticValue sliceRange = new IGStaticValueBuilder(range, null).setLeft(leftValue).setRight(rightValue).buildConstant();

			subtype = type.createSubtype(sliceRange, null);

		} else {
			subtype = type.getStaticElementType(null);
		}

		TraceLineSignalArraySlice tlsas = new TraceLineSignalArraySlice(tls, min, max, aTDM, aColor, subtype);

		addTrace(tlsas, aParent);

		return tlsas;
	}

	private void addTrace(TraceLine aTraceLine, TraceLine aParent) {

		if (!fTraceLineTreeItemMap.containsKey(aTraceLine)) {

			TreeItem item;
			if (aParent != null) {

				TreeItem parent = fTraceLineTreeItemMap.get(aParent);
				if (parent == null) {
					logger.error("WaveformViewer: addTrace(): parent %s not found!", aParent);
					return;
				}

				item = new TreeItem(parent, SWT.NONE);

			} else {
				int idx = 0;

				TreeItem tis[] = fTree.getSelection();

				if (tis.length > 0) {
					TreeItem sel = tis[0];

					TreeItem[] items = fTree.getItems();
					int i = 0;
					while (i < items.length) {
						if (items[i] == sel) {
							idx = i+1;
							break;
						}
						i++;
					}
				}

				item = new TreeItem(fTree, SWT.NONE, idx);
			}

			String valueStr = aTraceLine.getValueStr(fCursor, fCursorTime);

			item.setText(new String[] { aTraceLine.getLabel(), valueStr });
			item.setData(aTraceLine);
			item.setForeground(fColors[aTraceLine.getColor()]);

			fUIDTraceLineMap.put(aTraceLine.getUID(), aTraceLine);
			fTraceLineTreeItemMap.put(aTraceLine, item);
			if (aTraceLine instanceof TraceLineSignal) {
				TraceLineSignal tls = (TraceLineSignal) aTraceLine;
				if (tls.isFullSignal()) {
					fSignalPathTraceLineMap.put(tls.getSignalPath(), tls);
				}
				IGTypeStatic type = tls.getType();

				try {
					if (type.isRecord()) {
						int nRFs = type.getNumRecordFields(null);
						for (int i = 0; i < nRFs; i++) {
							IGRecordField rf = type.getRecordField(i, null);

							IGTypeStatic subType = type.getStaticRecordFieldType(i);

							TraceLineSignalRF tlsrf = new TraceLineSignalRF(tls, rf.getId(), TraceDisplayMode.HEX, getNextColor(), subType);

							addTrace(tlsrf, tls);

						}
					}
				} catch (Throwable t) {
					el.logException(t);
				}
			}
		}
		TreeColumn col = fTree.getColumns()[1];
		col.pack();
	}

	public BigInteger getStartTime() {
		return fStartTime;
	}

	public BigInteger getEndTime() {
		return fEndTime;
	}

	private void selectAndReveal(TraceLine aTL) {
		TreeItem item = fTraceLineTreeItemMap.get(aTL);
		if (item != null) {
			fTree.setSelection(item);
			fTree.showSelection();
		}
	}

	public void selectAndReveal(PathName aSignalPath) {
		TraceLine tl = fSignalPathTraceLineMap.get(aSignalPath);
		if (tl == null) {
			return;
		}
		selectAndReveal(tl);
	}

	public IGISimulator getSim() {
		return fSimulator;
	}

	int getXOffset() {
		return fXOffset;
	}

	int getVisibleWidth() {
		return fVisibleWidth;
	}

	Tree getTree() {
		return fTree;
	}

	double getFSPerUnit() {
		return fFSPerUnit;
	}

	private void saveTraces() {

		// we need to traverse the tree
		ArrayList<String> traces = new ArrayList<String>();

		int n = fTree.getItemCount();
		for (int i = 0; i < n; i++) {
			TreeItem root = fTree.getItem(i);
			saveTracesRek(root, traces);
		}

		try {
			fConfig.setTraces(traces);
		} catch (CoreException e) {
			el.logException(e);
		}
	}

	private void saveTracesRek(TreeItem aItem, ArrayList<String> aTraces) {

		TraceLine tl = (TraceLine) aItem.getData();

		int n = aItem.getItemCount();

		tl.save(aTraces, n);

		for (int i = 0; i < n; i++) {
			saveTracesRek(aItem.getItem(i), aTraces);
		}

	}

	IGTypeStatic getSignalType(PathName aSignalPath) {

		IGTypeStatic type = null;

		try {

			if (fCursor.gotoTransition(aSignalPath, fCursorTime)) {
				IGStaticValue value = fCursor.getCurrentValue();
				if (value != null) {
					type = value.getStaticType();
				}
			}
		} catch (Throwable t) {
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	private int loadTracesRek(TraceLine aParent, List aTraces, int aIndex) throws ZamiaException {

		int index = aIndex;

		int n = aTraces.size();

		if (index >= n) {
			return index;
		}

		String str = (String) aTraces.get(index);
		index++;

		String[] parts = str.split(":");

		if (parts.length == 2) {
			// legacy format support 
			try {
				PathName signalPath = new PathName(parts[0]);
				TraceDisplayMode mode = TraceDisplayMode.valueOf(parts[1]);

				IGTypeStatic type = getSignalType(signalPath);

				if (type != null) {
					fSimulator.trace(signalPath);

					TraceLineSignal tls = new TraceLineSignal(signalPath, mode, getNextColor(), type);

					addTrace(tls, aParent);
				} else {
					logger.error("SimulatorView: loadTraces(): signal %s seems to have vanished.", signalPath);
				}
			} catch (NumberFormatException e) {
				el.logException(e);
			} catch (ZamiaException e) {
				el.logException(e);
			}

		} else {

			if (parts.length > 2) {

				// new format

				TraceLine tl = null;
				int nChildren = 0;

				try {
					if (parts[0].equals("signal") && parts.length == 5) {

						PathName path = new PathName(parts[1]);
						int color = Integer.parseInt(parts[2]);
						TraceDisplayMode tdm = TraceDisplayMode.valueOf(parts[3]);

						if (hasSignal(path)) {
							tl = addTraceSignal(aParent, path, color, tdm);
						} else {
							logger.error("SimulatorView: loadTraces(): signal %s seems to have vanished.", path);
						}

						nChildren = Integer.parseInt(parts[4]);

					} else if (parts[0].equals("rf") && parts.length == 5) {

						String field = parts[1];
						int color = Integer.parseInt(parts[2]);
						TraceDisplayMode tdm = TraceDisplayMode.valueOf(parts[3]);
						nChildren = Integer.parseInt(parts[4]);

						tl = addTraceRecordField(aParent, field, color, tdm);

					} else if (parts[0].equals("slice") && parts.length == 6) {

						int min = Integer.parseInt(parts[1]);
						int max = Integer.parseInt(parts[2]);
						int color = Integer.parseInt(parts[3]);
						TraceDisplayMode tdm = TraceDisplayMode.valueOf(parts[4]);
						nChildren = Integer.parseInt(parts[5]);

						tl = addTraceSlice(aParent, min, max, color, tdm);

					} else if (parts[0].equals("markers") && parts.length == 5) {

						String label = parts[1];
						int color = Integer.parseInt(parts[2]);
						nChildren = Integer.parseInt(parts[3]);
						int nMarkers = Integer.parseInt(parts[4]);

						tl = new TraceLineMarkers(label, color);

						addTrace(tl, aParent);

						for (int i = 0; i < nMarkers; i++) {
							index = loadTracesRek(tl, aTraces, index);
						}

					} else if (parts[0].equals("marker") && parts.length == 3) {

						String label = parts[1];
						BigInteger t = new BigInteger(parts[2]);

						if (aParent instanceof TraceLineMarkers) {

							TraceLineMarkers tlm = (TraceLineMarkers) aParent;

							tlm.addMarker(t, label);
						}
					}
				} catch (Throwable t) {
					el.logException(t);
				}

				for (int i = 0; i < nChildren; i++) {
					index = loadTracesRek(tl, aTraces, index);
				}

			} else {
				logger.error("WaveformViewer: loadTraces(): failed to parse line '%s' (2)", str);
			}
		}

		return index;
	}

	@SuppressWarnings("unchecked")
	private void loadTraces() {
		try {
			List traces = fConfig.getTraces();

			if (traces != null) {

				int n = traces.size();

				int idx = 0;
				while (idx < n) {
					idx = loadTracesRek(null, traces, idx);
				}
			}
		} catch (Throwable t) {
			el.logException(t);
		}
	}

	public String formatSignalValue(PathName aPath, IGStaticValue aValue) {

		TraceLine tl = fSignalPathTraceLineMap.get(aPath);
		if (tl == null || !(tl instanceof TraceLineSignal)) {
			return aValue.toHexString();
		}

		TraceLineSignal tls = (TraceLineSignal) tl;
		return TraceLineSignal.formatSignalValue(aValue, tls.getTDM());
	}

	GC resizeOffscreenImage(Rectangle aClientArea) {

		fOffscreenLock.lock();
		try {

			if (fOffscreenImage != null && !fOffscreenImage.getBounds().equals(aClientArea)) {
				fOffscreenGC.dispose();
				fOffscreenGC = null;
				fOffscreenImage.dispose();
				fOffscreenImage = null;
			}

			if (fOffscreenImage == null) {
				fOffscreenImage = new Image(fDisplay, aClientArea.width, aClientArea.height);
				fOffscreenGC = new GC(fOffscreenImage);
			}
		} finally {
			fOffscreenLock.unlock();
		}

		return fOffscreenGC;
	}

	public Display getDisplay() {
		return fDisplay;
	}

	Color getColor(int aColor) {
		return fColors[aColor];
	}

	Image getMinusIcon() {
		return fMinusIcon;
	}

	private void doFindReferences(boolean aWritersOnly) {
		try {
			TreeItem[] items = fTree.getSelection();

			if (items.length == 0)
				return;

			TraceLine tline = (TraceLine) items[0].getData();

			if (!(tline instanceof TraceLineSignal)) {
				return;
			}

			TraceLineSignal tls = (TraceLineSignal) tline;

			PathName signalPath = tls.getSignalPath();
			
			NewSearchUI.activateSearchResultView();

			ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(fConfig.getProject());

			Toplevel tl = new Toplevel(fConfig.getToplevel(), null);

			ToplevelPath path = new ToplevelPath(tl, signalPath);

			IGManager igm = zprj.getIGM();

			IGItem item = igm.findItem(tl, signalPath);

			if (item != null) {

				SourceLocation location = item.computeSourceLocation();

				NewSearchUI.runQueryInBackground(new ReferencesSearchQuery(zprj, path, location, true, true, false, true, aWritersOnly, false));
			}

		} catch (Throwable t) {
			el.logException(t);
		}

	}

}
