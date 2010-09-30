/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.views.rtl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.fsm.FSM;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.plugin.views.fsm.FSMEditorInput;
import org.zamia.plugin.views.sim.SimulatorView;
import org.zamia.rtl.RTLFSM;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPortModule;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.rtl.sim.ISimulator;
import org.zamia.util.SimpleRegexp;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.interpreter.ZILInterpreter;
import org.zamia.zil.interpreter.ZILInterpreterCode;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class RTLView extends ViewPart implements ZoomObserver, PaintListener {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public static String VIEW_ID = "org.zamia.plugin.views.rtl.RTLView";

	public static final double TOP_MARGIN = 50.0;

	public static final double LEFT_MARGIN = 50.0;

	public static final double RIGHT_MARGIN = 50.0;

	public static final double BOTTOM_MARGIN = 50.0;

	public static final int SMALL_FONT_SIZE = 20;

	public static final int NORMAL_FONT_SIZE = 60;

	public static final int BIG_FONT_SIZE = 80;

	public static final String FONT_NAME = "Sans";

	private RTLGraph rtlg;

	private ColorScheme colorScheme;

	private PlaceAndRoute par;

	//	private HashMap<RTLSignal, Position> annotationPositions;

	private Display display;

	private Canvas canvas;

	private Text location, searchText;

	private Label selectionLabel;

	// offscreen image
	public final static int OFFSCREEN_WIDTH = 4096;

	public final static int OFFSCREEN_HEIGHT = 4096;

	private Image offscreenImage;

	private boolean offscreenValid;

	private Point offscreenOffset;

	private Point offscreenSize;

	private GC offscreenGC;

	private Position totalSize; // size of unzoomed circuit

	private Point visibleSize; // size of canvas

	private Point visibleOffset; // given by scrollbars

	private ZoomWidget zoom;

	private double zoomFactor;

	private Point zoomedSize; // = total * zoom

	private Menu popupMenu;

	private RTLModule selectedRTLModule;

	private RTLSignal selectedRTLSignal;

	private HashSet<RTLModule> highlightModules;

	private HashSet<RTLSignal> highlightSignals;

	// private static Format format;

	private HashSet<RTLModule> visibleRTLModules;

	// other speed/overview options
	private boolean showPins = false;

	private boolean showBuiltins = false;

	private ToolItem showPinsButton, showBuiltinsButton;

	private ScrollBar horizontal;

	private ScrollBar vertical;

	// simulator link (findSim* methods use these for caching)
	private SimulatorView simView = null;

	private ISimulator sim = null;

	// fonts
	private Font smallFont, normalFont, bigFont;

	private Composite control;

	// configurable limits controlling auto-activation of
	// dynamic/builtin/signal show buttons when displaying
	// a new rtl graph

	// meaning: # > soft limit => button is activated
	// # > hard limit => button is activated an disabled

	public final static int LIMIT_SOFT_PINS = 64;

	public final static int LIMIT_SOFT_BUILTINS = 500;

	public final static int LIMIT_HARD_BUILTINS = 5000;

	public final static int LIMIT_SOFT_SUBS = 1000;

	public final static int LIMIT_HARD_SUBS = 10000;

	public final static int LIMIT_SOFT_PORTS = 50;

	private boolean busyElaborating = false;

	private int ioLimit = LIMIT_SOFT_PORTS;

	private int builtinsLimit = LIMIT_SOFT_BUILTINS;

	private Text ioText, builtinsText;

	private Sash sash;

	private Composite mainBox;

	private RTLTree tree;

	private class MouseHandler implements MouseListener, MouseMoveListener {

		// private int grabEvents;
		private boolean dragMode = false;

		private int xStart, yStart;

		public MouseHandler() {
			super();
			// grabEvents = 0;
		}

		public void mouseDown(MouseEvent e_) {
			// if (dragMode) {
			// if (grabEvents++ == 0) {
			xStart = e_.x;
			yStart = e_.y;
			// }
			// } else {
			// if (grabEvents != 0) {
			// grabEvents = 0;
			// }

			dragMode = handleMouseDown(e_.x, e_.y, e_.button);
			// }
		}

		public void mouseDoubleClick(MouseEvent e_) {
			handleMouseDoubleClick(e_.x, e_.y);
		}

		public void mouseUp(MouseEvent e_) {
			// grabEvents = 0;
			dragMode = false;
		}

		public void mouseMove(MouseEvent e_) {
			if (dragMode) {

				int oldOffsetX = visibleOffset.x;
				int oldOffsetY = visibleOffset.y;
				int newOffsetX = oldOffsetX;
				int newOffsetY = oldOffsetY;

				if (zoomedSize.x > visibleSize.x) {
					newOffsetX = Math.min(oldOffsetX + (xStart - e_.x), zoomedSize.x - visibleSize.x);
					if (newOffsetX < 0)
						newOffsetX = 0;
				}
				if (zoomedSize.y > visibleSize.y) {
					newOffsetY = Math.min(oldOffsetY + (yStart - e_.y), zoomedSize.y - visibleSize.y);
					if (newOffsetY < 0)
						newOffsetY = 0;
				}
				if (oldOffsetX != newOffsetX || oldOffsetY != newOffsetY) {
					horizontal.setSelection(newOffsetX);
					vertical.setSelection(newOffsetY);
					canvas.update();
					visibleOffset.x = newOffsetX;
					visibleOffset.y = newOffsetY;
					canvas.redraw();
				}
				xStart = e_.x;
				yStart = e_.y;

			}
			// else if (grabEvents != 0) {
			// grabEvents = 0;
			// }
		}

	}

	public RTLView() {

	}

	public void createPartControl(Composite parent) {

		if (!ZamiaPlugin.ENABLE_EXPERIMENTAL_FEATURES) {
			Label l = new Label(parent, SWT.NONE);
			l.setText("This feature is still under development.");
			return;
		}

		display = parent.getDisplay();

		// control = new RTLView(parent, new ColorSchemeZamia(display), true,
		// true, this);

		colorScheme = new ColorSchemeZamia(display);

		par = new PlaceAndRoute(this);

		display = getDisplay();

		// selSignal = null;
		highlightModules = new HashSet<RTLModule>();
		highlightSignals = new HashSet<RTLSignal>();

		control = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout();
		control.setLayout(gl);
		gl.numColumns = 1;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		// a coolbar for navigation and zoom

		CoolBar coolbar = new CoolBar(control, SWT.NONE);
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		coolbar.setLayoutData(gd);

		// navigation coolitem

		Composite comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		comp.setLayout(gl);
		gl.numColumns = 3;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		location = new Text(comp, SWT.BORDER);
		location.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.character) {
				case SWT.CR:
					navigate(location.getText());
					break;
				case '1':
					zoom.setFactor(1.0);
					break;
				}
			}
		});
		gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		location.setLayoutData(gd);

		ToolBar tb = new ToolBar(comp, SWT.FLAT);

		ToolItem ti = new ToolItem(tb, SWT.NONE);
		Image icon = ZamiaPlugin.getImage("/share/images/gohome.gif");
		ti.setImage(icon);
		ti.setToolTipText("Go to toplevel RTLGraph");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RTLGraph newRTLG = rtlg;
				if (newRTLG == null)
					return;
				while (newRTLG.getParent() != null)
					newRTLG = newRTLG.getParent();
				setRTLGraph(newRTLG);
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/up.gif");
		ti.setImage(icon);
		ti.setToolTipText("Go to parent RTLGraph");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RTLGraph newNL = (RTLGraph) rtlg.getParent();
				if (newNL != null)
					setRTLGraph(newNL);
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/fileprint.gif");
		ti.setImage(icon);
		ti.setToolTipText("Print to PostScript file");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// doPrintPS();
				doPrint();
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/drc.gif");
		ti.setImage(icon);
		ti.setToolTipText("Run design rule check on RTLGraph");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				rtlg.consistencyCheck();
				setRTLGraph(rtlg);
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/show_source.gif");
		ti.setImage(icon);
		ti.setToolTipText("Show source code");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doShowSource();
			}
		});

		// finish navigation coolitem

		CoolItem citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);
		Point pt = citem.getSize();
		citem.setSize(pt.x + 400, pt.y);

		/*
		 * zoom coolitem
		 */

		comp = new Composite(coolbar, SWT.NONE);

		gl = new GridLayout();
		gl.numColumns = 3;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		tb = new ToolBar(comp, SWT.FLAT);

		zoom = new ZoomWidget(comp, 0.5, 100.0, 1.0);
		zoom.addZoomObserver(this);

		citem = new CoolItem(coolbar, SWT.NONE);
		citem.setControl(comp);
		calcSize(citem);

		/***********************************************************************
		 * 
		 * navigator / canvas sash
		 * 
		 ***********************************************************************/

		mainBox = new Composite(control, SWT.NONE);
		gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		mainBox.setLayoutData(gd);

		canvas = new Canvas(mainBox, SWT.V_SCROLL | SWT.H_SCROLL);

		sash = new Sash(mainBox, SWT.VERTICAL);
		tree = new RTLTree(mainBox);
		tree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				ITreeSelection selection = (ITreeSelection) tree.getSelection();

				Object first = selection.getFirstElement();
				if (first instanceof RTLModule) {

					selectAndReveal((RTLModule) first);

				}
			}
		});

		final FormLayout form = new FormLayout();
		mainBox.setLayout(form);

		FormData canvasData = new FormData();
		canvasData.left = new FormAttachment(0, 0);
		canvasData.right = new FormAttachment(sash, 0);
		canvasData.top = new FormAttachment(0, 0);
		canvasData.bottom = new FormAttachment(100, 0);
		canvas.setLayoutData(canvasData);

		final int limit = 20, percent = 85;
		final FormData sashData = new FormData();
		sashData.left = new FormAttachment(percent, 0);
		sashData.top = new FormAttachment(0, 0);
		sashData.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(sashData);
		sash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Rectangle sashRect = sash.getBounds();
				Rectangle shellRect = mainBox.getClientArea();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max(Math.min(e.x, right), limit);
				if (e.x != sashRect.x) {
					sashData.left = new FormAttachment(0, e.x);
					mainBox.layout();
				}
			}
		});

		FormData treeData = new FormData();
		treeData.left = new FormAttachment(sash, 0);
		treeData.right = new FormAttachment(100, 0);
		treeData.top = new FormAttachment(0, 0);
		treeData.bottom = new FormAttachment(100, 0);
		tree.getControl().setLayoutData(treeData);

		/***********************************************************************
		 * canvas
		 ***********************************************************************/

		offscreenSize = new Point(OFFSCREEN_WIDTH, OFFSCREEN_HEIGHT);
		offscreenImage = new Image(display, offscreenSize.x, offscreenSize.y);
		offscreenGC = new GC(offscreenImage);

		offscreenOffset = new Point(0, 0);
		offscreenValid = false;

		MouseHandler mouseHandler = new MouseHandler();
		canvas.addMouseListener(mouseHandler);
		canvas.addMouseMoveListener(mouseHandler);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MouseWheel) {

					Rectangle ca = canvas.getClientArea();
					double ax = (double) event.x / (double) ca.width;
					double ay = (double) event.y / (double) ca.height;

					double of = zoom.getFactor();

					if (event.count > 0) {
						zoom.setFactor(of * 1.25, false);
					} else {
						zoom.setFactor(of / 1.25, false);
					}
					doZoom(zoom.getFactor(), ax, ay);
					event.doit = false;
				}
			}
		};

		canvas.addListener(SWT.MouseWheel, listener);

		initScrollBars();

		/***********************************************************************
		 * search/status bar
		 ***********************************************************************/

		Composite statusBox = new Composite(control, SWT.NONE);
		gl = new GridLayout();
		statusBox.setLayout(gl);
		gl.numColumns = 8;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		statusBox.setLayoutData(gd);

		/*
		 * io/builtins limits toolbar
		 */

		tb = new ToolBar(statusBox, SWT.FLAT);

		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("R");
		ti.setToolTipText("Reset visibility/limits");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetVisibleModules();
			}
		});

		ioText = new Text(statusBox, SWT.BORDER);
		ioText.setTextLimit(21);
		ioText.setEditable(false);
		ioText.setText("IOs: 00000/00000");
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		ioText.setLayoutData(gd);

		tb = new ToolBar(statusBox, SWT.FLAT);

		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("+");
		ti.setToolTipText("Show more IO-Ports");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setIOLimit(getIOLimit() + 10);
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("-");
		ti.setToolTipText("Show less IO-Ports");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setIOLimit(getIOLimit() - 10);
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("*");
		ti.setToolTipText("Show All IO-Ports");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (rtlg == null)
					return;
				setIOLimit(rtlg.getNumPorts());
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/eraser.gif");
		ti.setImage(icon);
		ti.setToolTipText("Show No IO-Ports");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setIOLimit(0);
			}
		});

		builtinsText = new Text(statusBox, SWT.BORDER);
		builtinsText.setTextLimit(21);
		builtinsText.setEditable(false);
		builtinsText.setText("Builtins: 00000/00000");
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		builtinsText.setLayoutData(gd);

		tb = new ToolBar(statusBox, SWT.FLAT);

		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("+");
		ti.setToolTipText("Show more builtins");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setBuiltinsLimit(getBuiltinsLimit() + 10);
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("-");
		ti.setToolTipText("Show less builtins");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setBuiltinsLimit(getBuiltinsLimit() - 10);
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		ti.setText("*");
		ti.setToolTipText("Show all builtins");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setBuiltinsLimit(1000000);
			}
		});
		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/eraser.gif");
		ti.setImage(icon);
		ti.setToolTipText("Show no builtins");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setBuiltinsLimit(0);
			}
		});

		/*
		 * search toolbar
		 */

		searchText = new Text(statusBox, SWT.BORDER);
		searchText.setToolTipText("Simple regexp signal/module search, e.g. '*foo*'");
		searchText.setText("          ");
		searchText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.character) {
				case SWT.CR:
					doSearch(searchText.getText());
					break;
				}
			}
		});
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		searchText.setLayoutData(gd);

		tb = new ToolBar(statusBox, SWT.FLAT);

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/find.gif");
		ti.setImage(icon);
		ti.setToolTipText("Search");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(searchText.getText());
			}
		});

		ti = new ToolItem(tb, SWT.NONE);
		icon = ZamiaPlugin.getImage("/share/images/eraser.gif");
		ti.setImage(icon);
		ti.setToolTipText("Clear selection/Visible modules");
		ti.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				visibleRTLModules = new HashSet<RTLModule>();
				recalcVisibleIOs(true);

				//				if (highlightModules != null && highlightModules.size() == 0) {
				//					resetVisibleModules();
				//				} else {
				//					highlightModules = new HashSet<RTLModule>();
				//					highlightSignals = new HashSet<RTLSignal>();
				//					placeAndRoute();
				//					updateZoom(zoom.getFactor());
				//				}
			}
		});

		showPinsButton = new ToolItem(tb, SWT.CHECK);
		showPinsButton.setText("P");
		showPinsButton.setToolTipText("Display Pins");
		showPinsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showPins = showPinsButton.getSelection();

				placeAndRoute();
				// redraw
				offscreenValid = false;
				updateZoom(zoom.getFactor());
				// update(project, Project.NOTIFY_STRUCTURE);
			}
		});

		showBuiltinsButton = new ToolItem(tb, SWT.CHECK);
		showBuiltinsButton.setText("B");
		showBuiltinsButton.setToolTipText("Display Builtins");
		showBuiltinsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showBuiltins = showBuiltinsButton.getSelection();

				placeAndRoute();
				// redraw
				offscreenValid = false;
				updateZoom(zoom.getFactor());
				// update(project, Project.NOTIFY_STRUCTURE);
			}
		});

		selectionLabel = new Label(statusBox, SWT.NONE);
		selectionLabel.setText("");
		gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = false;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		selectionLabel.setLayoutData(gd);

		rtlg = null;

		reset();

		resizeFonts();

		canvas.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				updateZoom(zoom.getFactor());
			}
		});

		/* Set up the paint canvas scroll bars */
		ScrollBar horizontal = canvas.getHorizontalBar();
		horizontal.setVisible(true);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		ScrollBar vertical = canvas.getVerticalBar();
		vertical.setVisible(true);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
		handleResize();

		canvas.addPaintListener(this);

		popupMenu = new Menu(canvas.getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(popupMenu, SWT.PUSH);
		item.setText("Trace");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {

				// FIXME: implement

				//				RTLSignal s = getSelectedRTLSignal();
				//				if (s != null) {
				//					ISimulator sim = findSimulator();
				//					if (sim != null) {
				//						try {
				//							sim.trace(s.getPath());
				//						} catch (ZamiaException e1) {
				//							el.logException(e1);
				//						}
				//					}
				//				}
			}
		});
		new MenuItem(popupMenu, SWT.SEPARATOR);
		;
		item = new MenuItem(popupMenu, SWT.PUSH);
		item.setText("Show receivers");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				RTLModule g = getSelectedModule();
				doShowReceivers(g);

			}
		});
		item = new MenuItem(popupMenu, SWT.PUSH);
		item.setText("Show drivers");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				RTLModule g = getSelectedModule();
				doShowDrivers(g);
			}
		});
		// item = new MenuItem(popupMenu, SWT.PUSH);
		// item.setText("Show input cone");
		// item.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event e) {

		// FIXME

		// RTLModule g = getSelectedModule();
		//
		// if (g != null) {
		// visibleRTLModules.add(g);
		//
		// clearHighlight();
		//
		// ArrayList cone = rtlg.getInputCone(g);
		//
		// int n = cone.size();
		// for (int i = 0; i < n; i++) {
		// RTLModule r = (RTLModule) cone.get(i);
		//
		// visibleRTLModules.add(r);
		// highlightModules.add(r);
		// }
		//
		// if (dynamicViewMode) {
		// placeAndRoute();
		// updateZoom(zoom.getFactor());
		// }
		// // redraw
		// offscreenValid = false;
		// canvas.redraw();
		// // update(project, Project.NOTIFY_STRUCTURE);
		// }
		// }
		// });
		// item = new MenuItem(popupMenu, SWT.PUSH);
		// item.setText("Show output cone");
		// item.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event e) {

		// FIXME

		// RTLModule g = getSelectedModule();
		//
		// if (g != null) {
		// visibleRTLModules.add(g);
		//
		// clearHighlight();
		//
		// ArrayList drivers = rtlg.getOutputCone(g);
		//
		// int n = drivers.size();
		// for (int i = 0; i < n; i++) {
		// RTLModule r = (RTLModule) drivers.get(i);
		//
		// visibleRTLModules.add(r);
		// highlightModules.add(r);
		// }
		//
		// if (dynamicViewMode) {
		// placeAndRoute();
		// updateZoom(zoom.getFactor());
		// }
		// // redraw
		// offscreenValid = false;
		// canvas.redraw();
		// // update(project, Project.NOTIFY_STRUCTURE);
		// }
		// }
		// });

		new MenuItem(popupMenu, SWT.SEPARATOR);
		;

		item = new MenuItem(popupMenu, SWT.PUSH);
		item.setText("Show Source");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				doShowSource();
			}
		});

		// item = new MenuItem(popupMenu, SWT.PUSH);
		// item.setText("Open");
		// item.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event e) {
		// RTLModule g = getSelectedRTLModule();
		// if (g != null) {
		// if (g instanceof RTLGraph) {
		// gui.openRTLGraph((RTLGraph) g);
		// }
		// }
		// }
		// });

		canvas.setMenu(popupMenu);

	}

	private void placeAndRoute() {

		if (rtlg == null)
			return;

		// System.out.println("place and route starts...");
		reset();
		resizeFonts();

		totalSize = par.placeAndRoute(rtlg, showPins, showBuiltins, visibleRTLModules);

		handleResize();
		// System.out.println("handleResize() done");
	}

	private void reset() {
		totalSize = new Position(1, 1);
		visibleSize = new Point(1, 1);
		zoomedSize = new Point(1, 1);
		visibleOffset = new Point(0, 0);
		offscreenOffset = new Point(0, 0);
		zoomFactor = 1.0;
		//		annotationPositions = new HashMap<RTLSignal, Position>();
	}

	// public RTLGraphAnnotation getAnnotation(Object o_) {
	// return rtlg.getAnnotation(o_);
	// }

	// public Color getAnnotationSWTColor(RTLGraphAnnotation a_) {
	// switch (a_.color) {
	// case RTLGraphAnnotation.COLOR_BLUE :
	// return display.getSystemColor(SWT.COLOR_BLUE);
	// case RTLGraphAnnotation.COLOR_GREEN :
	// return display.getSystemColor(SWT.COLOR_GREEN);
	// case RTLGraphAnnotation.COLOR_MAGENTA :
	// return display.getSystemColor(SWT.COLOR_MAGENTA);
	// case RTLGraphAnnotation.COLOR_RED :
	// return display.getSystemColor(SWT.COLOR_RED);
	// case RTLGraphAnnotation.COLOR_WHITE :
	// return display.getSystemColor(SWT.COLOR_WHITE);
	// case RTLGraphAnnotation.COLOR_YELLOW :
	// return display.getSystemColor(SWT.COLOR_YELLOW);
	// }
	// return display.getSystemColor(SWT.COLOR_WHITE);
	// }

	private void paintOffscreen(GC offscreenGC_) {
		// System.out.println ("updating offscreen image");

		Font oldfont = offscreenGC_.getFont();
		int fontSize = (int) (8.0 * getZoomFactor());

		if (fontSize < 2)
			fontSize = 2;
		Font font = new Font(display, "Sans", fontSize, SWT.NONE);
		offscreenGC_.setFont(font);
		offscreenGC_.setBackground(colorScheme.getBgColor());
		// offscreenGC_.setForeground();
		offscreenGC_.setLineWidth((int) (2 * getZoomFactor()));
		offscreenGC_.fillRectangle(0, 0, offscreenSize.x, offscreenSize.y);

		// offscreenGC_.setForeground(colorScheme.get);
		// offscreenGC_.drawLine(tX(0), tY(0), tX(totalSize.x-1),
		// tY(totalSize.y-1));
		// offscreenGC_.drawRectangle(tX(0), tY(0), tW(totalSize.x),
		// tH(totalSize.y));

		if (rtlg != null) {
			// draw modules
			int n = rtlg.getNumSubs();
			for (int i = 0; i < n; i++) {
				RTLModule module = rtlg.getSub(i);

				if (!visibleRTLModules.contains(module))
					continue;

				VisualModule visualModule = par.getModule(module);

				if (visualModule != null) {

					if (highlightModules != null)
						visualModule.paint(this, offscreenGC_, highlightModules.contains(visualModule.getRTLModule()));
					else
						visualModule.paint(this, offscreenGC_, false);
				}
			}

			// draw channels / signals
			par.paint(this, display, offscreenGC_);

			// draw annotations
			// Font font2 = new Font(display, "Sans", fontSize * 2 / 3,
			// SWT.NONE);
			// offscreenGC_.setFont(font2);
			// n = rtlg.getNumSignals();
			// for (int i = 0; i < n; i++) {
			// RTLSignal s = rtlg.getSignal(i);
			//
			// Position l = annotationPositions.get(s);
			// if (l == null)
			// continue;
			//
			// String label;
			// label = s.getId();
			//
			// offscreenGC_.setForeground(colorScheme.getAnnotationColor());
			//
			// // additional annotations?
			// RTLGraphAnnotation a = rtlg.getAnnotation(s);
			// if (a != null) {
			// label += ":" + a.str;
			// offscreenGC_.setForeground(getAnnotationSWTColor(a));
			// }
			// // FIXME
			// // double sx = l.sx + (l.dx - l.sx) / 2;
			// // double sy = l.sy;
			// // offscreenGC_.drawText(label, tX(sx), tY(sy), true);
			// }

			// offscreenGC_.setLineWidth((int) (getZoomFactor() * 4.0));
			// int num = rtlg.getNumSubs();
			// for (int i = 0; i < num; i++) {
			// RTLModule module = rtlg.getSub(i);
			//
			// int numPorts = module.getNumPorts();
			// for (int j = 0; j < numPorts; j++) {
			// RTLPort port = module.getPort(j);
			// RTLGraphAnnotation a = rtlg.getAnnotation(port);
			// if (a == null)
			// continue;
			//
			// if (a.mark == false)
			// continue;
			//
			// offscreenGC_.setForeground(display
			// .getSystemColor(SWT.COLOR_RED));
			// double sx = par.getPortPosition(port).x;
			// double sy = par.getPortPosition(port).y;
			// offscreenGC_.drawLine(tX(sx - 5), tY(sy - 5), tX(sx + 5),
			// tY(sy + 5));
			// offscreenGC_.drawLine(tX(sx + 5), tY(sy - 5), tX(sx - 5),
			// tY(sy + 5));
			// }
			// }
			// font2.dispose();
		}

		offscreenGC_.setFont(oldfont);
		font.dispose();
	}

	public void updateOffscreen(GC offscreenGC_) {

		// update offscreenOffset if necessary, repaint in that case

		Point offset = new Point(offscreenOffset.x, offscreenOffset.y);

		if (offset.x > visibleOffset.x)
			offset.x = visibleOffset.x - offscreenSize.x / 2;
		if (offset.y > visibleOffset.y)
			offset.y = visibleOffset.y - offscreenSize.x / 2;

		int w = offscreenSize.x;
		int h = offscreenSize.y;

		if ((visibleOffset.x + visibleSize.x) >= offset.x + w)
			offset.x = visibleOffset.x - offscreenSize.x / 2;
		if ((visibleOffset.y + visibleSize.y) >= offset.y + h)
			offset.y = visibleOffset.y - offscreenSize.x / 2;

		if (offset.x < 0)
			offset.x = 0;
		if (offset.y < 0)
			offset.y = 0;

		if ((offset.x != offscreenOffset.x) || (offset.y != offscreenOffset.y)) {
			// System.out.println ("offscreenOffset updated to "+offset);
			offscreenValid = false;
			offscreenOffset = offset;
		}

		if (offscreenValid)
			return;

		paintOffscreen(offscreenGC_);

		offscreenValid = true;
	}

	public boolean isSignalHilight(RTLSignal s_) {
		return highlightSignals.contains(s_);
	}

	private void initScrollBars() {
		horizontal = canvas.getHorizontalBar();
		// horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		vertical = canvas.getVerticalBar();
		// vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
	}

	public void paintControl(PaintEvent e) {

		updateOffscreen(offscreenGC);

		int ox = visibleOffset.x - offscreenOffset.x;
		int oy = visibleOffset.y - offscreenOffset.y;
		Rectangle clientRect = canvas.getClientArea();
		e.gc.drawImage(offscreenImage, ox, oy, visibleSize.x, visibleSize.y, clientRect.x, clientRect.y, visibleSize.x, visibleSize.y);
	}

	/*
	 * helper functions for drawing
	 */

	private final static double CLIPPING_MAX = Integer.MAX_VALUE;

	private final static double CLIPPING_MIN = Integer.MIN_VALUE;

	public int tX(double x) {
		double d = (x + LEFT_MARGIN) * getZoomFactor() - offscreenOffset.x;
		// simple clipping
		if (d > CLIPPING_MAX)
			return (int) CLIPPING_MAX;
		if (d < CLIPPING_MIN)
			return (int) CLIPPING_MIN;
		return (int) d;

	}

	public int tY(double y) {
		double d = (y + TOP_MARGIN) * getZoomFactor() - offscreenOffset.y;
		// simple clipping
		if (d > CLIPPING_MAX)
			return (int) CLIPPING_MAX;
		if (d < CLIPPING_MIN)
			return (int) CLIPPING_MIN;
		return (int) d;
	}

	public int tW(double x) {
		double d = x * getZoomFactor();
		// simple clipping
		if (d > CLIPPING_MAX)
			return (int) CLIPPING_MAX;
		if (d < CLIPPING_MIN)
			return (int) CLIPPING_MIN;
		return (int) d;
	}

	public int tH(double y) {
		double d = y * getZoomFactor();
		// simple clipping
		if (d > CLIPPING_MAX)
			return (int) CLIPPING_MAX;
		if (d < CLIPPING_MIN)
			return (int) CLIPPING_MIN;
		return (int) d;
	}

	public void scrollHorizontally(ScrollBar scrollBar) {
		if (zoomedSize.x > visibleSize.x) {
			final int oldOffset = visibleOffset.x;
			final int newOffset = Math.min(scrollBar.getSelection(), zoomedSize.x - visibleSize.x);
			if (oldOffset != newOffset) {
				canvas.update();
				visibleOffset.x = newOffset;
				canvas.redraw();
				// canvas.scroll(
				// Math.max(oldOffset - newOffset, 0),
				// 0,
				// Math.max(newOffset - oldOffset, 0),
				// 0,
				// visibleSize.x,
				// visibleSize.y,
				// false);
			}
		}
	}

	public void scrollVertically(ScrollBar scrollBar) {
		if (zoomedSize.y > visibleSize.y) {
			final int oldOffset = visibleOffset.y;
			final int newOffset = Math.min(scrollBar.getSelection(), zoomedSize.y - visibleSize.y);
			if (oldOffset != newOffset) {
				canvas.update();
				visibleOffset.y = newOffset;
				canvas.redraw();
				// canvas.scroll(
				// 0,
				// Math.max(oldOffset - newOffset, 0),
				// 0,
				// Math.max(newOffset - oldOffset, 0),
				// visibleSize.x,
				// visibleSize.y,
				// false);
			}
		}
	}

	private void handleResize() {
		control.update();

		Rectangle visibleRect = canvas.getClientArea();
		visibleSize.x = visibleRect.width;
		visibleSize.y = visibleRect.height;

		zoomedSize.x = tW(totalSize.x + LEFT_MARGIN + RIGHT_MARGIN);
		zoomedSize.y = tH(totalSize.y + TOP_MARGIN + BOTTOM_MARGIN);

		// System.out.println ("handleResize():
		// visibleSize="+visibleSize.x+"x"+visibleSize.y+",
		// zoomedSize="+zoomedSize.x+"x"+zoomedSize.y);

		ScrollBar horizontal = canvas.getHorizontalBar();
		if (horizontal != null) {
			visibleOffset.x = Math.min(horizontal.getSelection(), zoomedSize.x - visibleSize.x);
			if (zoomedSize.x <= visibleSize.x) {
				horizontal.setEnabled(false);
				horizontal.setSelection(0);
				visibleOffset.x = 0;
				horizontal.setValues(visibleOffset.x, 0, visibleSize.x, visibleSize.x, 8, visibleSize.x);
			} else {
				horizontal.setEnabled(true);
				horizontal.setValues(visibleOffset.x, 0, zoomedSize.x, visibleSize.x, 8, visibleSize.x);
			}
		}

		ScrollBar vertical = canvas.getVerticalBar();
		if (vertical != null) {
			visibleOffset.y = Math.min(vertical.getSelection(), zoomedSize.y - visibleSize.y);
			if (zoomedSize.y <= visibleSize.y) {
				vertical.setEnabled(false);
				vertical.setSelection(0);
				visibleOffset.y = 0;
				vertical.setValues(visibleOffset.y, 0, visibleSize.y, visibleSize.y, 8, visibleSize.y);
			} else {
				vertical.setEnabled(true);
				vertical.setValues(visibleOffset.y, 0, zoomedSize.y, visibleSize.y, 8, visibleSize.y);
			}
		}
	}

	public void setShowPins(boolean showPins_) {
		showPins = showPins_;
		showPinsButton.setSelection(showPins);
	}

	public void setShowBuiltins(boolean showBuiltins_) {
		showBuiltins = showBuiltins_;
		showBuiltinsButton.setSelection(showBuiltins);
	}

	private void resetVisibleModules() {

		visibleRTLModules = new HashSet<RTLModule>();

		// all custom subs should be visible:

		int nSubs = rtlg.getNumSubs();
		int maxPins = 0;
		int nVisibleModules = 0;
		for (int i = 0; i < nSubs; i++) {
			RTLModule module = rtlg.getSub(i);
			if (module instanceof RTLGraph) {
				nVisibleModules++;

				int nPins = module.getNumPorts();
				if (nPins > maxPins) {
					maxPins = nPins;
				}

				visibleRTLModules.add(module);
			}
		}

		// fill up with builtins, if feasible

		builtinsLimit = LIMIT_SOFT_BUILTINS - nVisibleModules;
		if (builtinsLimit < 0)
			builtinsLimit = 0;
		recalcVisibleBuiltins(false);
		setShowBuiltins(builtinsLimit > 0);

		ioLimit = LIMIT_SOFT_PORTS;
		recalcVisibleIOs(false);

		// do we show pins by default? 

		setShowPins(maxPins < LIMIT_SOFT_PINS);

		placeAndRoute();
		updateZoom(zoom.getFactor());
	}

	private void recalcVisibleIOs(boolean doPR_) {

		// first make all IOs invisible,
		// then make up to the limit IOs visible again

		int nSubs = rtlg.getNumSubs();
		int nInputs = 0;
		int nOutputs = 0;
		int nVisiblePorts = 0;
		int nPorts = 0;
		for (int i = 0; i < nSubs; i++) {
			RTLModule module = rtlg.getSub(i);
			if (module instanceof RTLPortModule) {
				nPorts++;
				PortDir dir = ((RTLPortModule) module).getExternalPort().getDirection();
				if (dir == PortDir.IN) {
					nInputs++;
					if (nInputs < ioLimit) {
						visibleRTLModules.add(module);
						nVisiblePorts++;
					} else {
						visibleRTLModules.remove(module);
					}
				} else {
					nOutputs++;
					if (nOutputs < ioLimit) {
						visibleRTLModules.add(module);
						nVisiblePorts++;
					} else {
						visibleRTLModules.remove(module);
					}
				}
			}
		}

		int maxPorts = nInputs > nOutputs ? nInputs : nOutputs;
		if (ioLimit > maxPorts)
			ioLimit = maxPorts;

		ioText.setText("IOs: " + nVisiblePorts + "/" + nPorts);

		if (doPR_) {
			placeAndRoute();
			updateZoom(zoom.getFactor());
		}
	}

	public void setIOLimit(int ioLimit_) {
		ioLimit = ioLimit_;
		recalcVisibleIOs(true);
	}

	public int getIOLimit() {
		return ioLimit;
	}

	private void recalcVisibleBuiltins(boolean doPR_) {

		// first make all IOs invisible,
		// then make up to the limit IOs visible again

		int nSubs = rtlg.getNumSubs();
		int nBuiltins = 0;
		int nVisibleBuiltins = 0;
		for (int i = 0; i < nSubs; i++) {
			RTLModule module = rtlg.getSub(i);
			if (!(module instanceof RTLGraph) && !(module instanceof RTLPortModule)) {
				nBuiltins++;
				if (nBuiltins < builtinsLimit) {
					visibleRTLModules.add(module);
					nVisibleBuiltins++;
				} else {
					visibleRTLModules.remove(module);
				}
			}
		}

		builtinsText.setText("Builtins: " + nVisibleBuiltins + "/" + nBuiltins);

		if (doPR_) {
			placeAndRoute();
			updateZoom(zoom.getFactor());
		}
	}

	public void setBuiltinsLimit(int builtinsLimit_) {
		builtinsLimit = builtinsLimit_;
		recalcVisibleBuiltins(true);
	}

	public int getBuiltinsLimit() {
		return builtinsLimit;
	}

	public void setRTLGraph(RTLGraph rtlg_) {

		rtlg = rtlg_;

		offscreenValid = false;
		if (rtlg != null) {
			resetVisibleModules();

			location.setText(rtlg.getPath().toString());
		} else {
			location.setText("");
		}
		zoom.setFactor(1.0); // will call handleResize / canvas.redraw

		tree.setInput(rtlg_);

	}

	public RTLGraph getRTLGraph() {
		return rtlg;
	}

	private void doZoom(double factor_, double ax, double ay) {

		Rectangle visibleRect = canvas.getClientArea();
		visibleSize.x = visibleRect.width;
		visibleSize.y = visibleRect.height;

		// 100% zoom should mean display whole circuit

		double fx = (double) visibleSize.x / ((double) totalSize.x + LEFT_MARGIN + RIGHT_MARGIN);
		double fy = (double) visibleSize.y / ((double) totalSize.y + TOP_MARGIN + BOTTOM_MARGIN);

		double of = zoomFactor;

		if (fx > fy)
			zoomFactor = factor_ * fy;
		else
			zoomFactor = factor_ * fx;

		double df = zoomFactor / of;

		zoomedSize.x = tX(totalSize.x - 1);
		zoomedSize.y = tY(totalSize.y - 1);

		ScrollBar horizontal = canvas.getHorizontalBar();
		if (horizontal != null) {

			double off = horizontal.getSelection();

			double margin = visibleSize.x * ax;

			double mx = (off + margin) * df - margin;

			int ox = (int) mx;

			visibleOffset.x = ox;
			horizontal.setValues(visibleOffset.x, 0, zoomedSize.x, visibleSize.x, 8, visibleSize.x);

		}
		ScrollBar vertical = canvas.getVerticalBar();
		if (vertical != null) {

			double off = vertical.getSelection();

			double margin = visibleSize.y * ay;

			double my = (off + margin) * df - margin;

			int oy = (int) my;

			visibleOffset.y = oy;
			vertical.setValues(visibleOffset.y, 0, zoomedSize.y, visibleSize.y, 8, visibleSize.y);
		}

		offscreenValid = false;

		resizeFonts();

		handleResize();
		canvas.redraw();

	}

	private void resizeFonts() {
		if (smallFont != null) {
			smallFont.dispose();
			smallFont = null;
		}
		if (normalFont != null) {
			normalFont.dispose();
			normalFont = null;
		}
		if (bigFont != null) {
			bigFont.dispose();
			bigFont = null;
		}

		smallFont = new Font(display, FONT_NAME, tF(SMALL_FONT_SIZE), SWT.NONE);
		normalFont = new Font(display, FONT_NAME, tF(NORMAL_FONT_SIZE), SWT.NONE);
		bigFont = new Font(display, FONT_NAME, tF(BIG_FONT_SIZE), SWT.BOLD);
	}

	public int tF(double size_) {
		int d = tW(size_);
		if (d < 2)
			d = 2;
		return d;
	}

	public void updateZoom(double factor_) {
		control.update();

		doZoom(factor_, 0.5, 0.5);
	}

	public double getZoomFactor() {
		return zoomFactor;
	}

	// public void addHighlight(Signal signal_) {
	// selSignal = signal_;
	// if (selSignal != null) {
	// // FIXME: simulator connection
	// // try {
	// // valueLabel.setText("Value: " + sim.getValue(selSignal));
	// // } catch (SimException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	//
	// if (dynamicViewMode) {
	//
	// if (signal_ instanceof SignalBit) {
	//
	// SignalBit s = (SignalBit) signal_;
	//
	// int num = s.getNumConns();
	// for (int i = 0; i < num; i++) {
	// PortBit p = s.getConn(i);
	//
	// visibleRTLModules.add(p.getRTLModule());
	// }
	// }
	//
	// placeAndRoute();
	// }
	//
	// highlightSignals.add(signal_);
	//
	// } else {
	// searchText.setText("");
	// selectionLabel.setText("");
	// }
	// offscreenValid = false;
	// canvas.redraw();
	// }

	public void clearHighlight() {
		highlightModules = new HashSet<RTLModule>();
		highlightSignals = new HashSet<RTLSignal>();
		selectedRTLModule = null;
		selectedRTLSignal = null;
	}

	public void addHighlight(RTLModule module_) {
		// selectedRTLModule = gate_;
		if (module_ != null) {
			if (!visibleRTLModules.contains(module_)) {
				visibleRTLModules.add(module_);
				placeAndRoute();
				updateZoom(zoom.getFactor());
			}
			highlightModules.add(module_);
		}
		offscreenValid = false;
		canvas.redraw();
	}

	public void selectAndReveal(RTLModule module_) {
		clearHighlight();

		addHighlight(module_);

		// find position and size, zoom in

		VisualModule visualModule = par.getModule(module_);
		if (visualModule == null) {

			logger.error("RTLView: Module to reveal is not placed: %s", visualModule);
			return;
		}

		Rectangle rect = visualModule.getRect(this);

		int w = rect.width;
		if (w < 40)
			w = 40;
		int h = rect.height;
		if (h < 40)
			h = 40;

		double f1 = (totalSize.x / w) / 2.0;
		double f2 = (totalSize.y / h) / 2.0;

		double f = f1 < f2 ? f1 : f2;

		zoom.setFactor(f, false);
		doZoom(f, 0.0, 0.0);

		int offx = tX(rect.x) - visibleSize.x / 2;
		int offy = tY(rect.y) - visibleSize.y / 2;
		offx = offx < 0 ? 0 : offx;
		offy = offy < 0 ? 0 : offy;

		horizontal.setSelection(offx);
		vertical.setSelection(offy);

		doZoom(f, 0.0, 0.0);
	}

	void addHighlight(RTLSignal s_) {
		highlightSignals.add(s_);
		offscreenValid = false;
		canvas.redraw();
	}

	public void doSearch(String regexp_) {
		if (rtlg == null)
			return;

		clearHighlight();

		String regexps[] = regexp_.split(" ");

		for (int j = 0; j < regexps.length; j++) {

			String regexp = SimpleRegexp.convert(regexps[j]);

			if (regexp == null || regexp.length() < 1) {
				continue;
			}

			boolean inverse = false;
			char firstC = regexp.charAt(0);
			if (firstC == '-') {
				regexp = regexp.substring(1);
				inverse = true;
			} else if (firstC == '+') {
				regexp = regexp.substring(1);
			}

			int n = rtlg.getNumSubs();
			for (int i = 0; i < n; i++) {
				RTLModule sub = rtlg.getSub(i);
				if (sub.getInstanceName().matches(regexp)) {
					if (!inverse) {
						visibleRTLModules.add(sub);
						highlightModules.add(sub);
					} else {
						visibleRTLModules.remove(sub);
						highlightModules.remove(sub);
					}
				}
			}

			n = rtlg.getNumSignals();
			for (int i = 0; i < n; i++) {
				RTLSignal s = rtlg.getSignal(i);
				if (s.getId().matches(regexp)) {
					if (!inverse) {
						highlightSignals.add(s);
					} else {
						highlightSignals.remove(s);
					}
				}
			}
		}

		placeAndRoute();
		updateZoom(zoom.getFactor());

		offscreenValid = false;
		canvas.redraw();
	}

	public SimulatorView findSimulatorView() {

		if (simView == null) {
			IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

			IWorkbenchPage page = window.getActivePage();

			simView = (SimulatorView) page.findView("org.zamia.plugin.views.sim.SimulatorView");
		}

		return simView;
	}

	public ISimulator findSimulator() {
		// FIXME FIXME
		//		if (sim == null) {
		//			SimulatorView view = findSimulatorView();
		//			sim = view.getSim();
		//		}
		return sim;
	}

	public boolean handleMouseDown(int mx_, int my_, int button_) {
		if (rtlg == null) {
			return false;
		}

		// project coordinates, find out what has been hit
		int mx = mx_ + visibleOffset.x - offscreenOffset.x;
		int my = my_ + visibleOffset.y - offscreenOffset.y;

		clearHighlight();

		VisualPort p = par.getHitExpandButton(this, mx, my);
		if (p != null) {
			if (p.getDirection() != PortDir.IN) {
				doShowReceivers(p.getModule().getRTLModule());
			} else {
				doShowDrivers(p.getModule().getRTLModule());
			}
		}

		int n = par.getNumChannels();
		for (int i = 0; i < n; i++) {
			Channel c = par.getChannel(i);

			RTLSignal s = c.getSignalHit(this, mx, my);

			if (s != null) {

				String t = "Signal " + s.getId();

				// FIXME: implement
				//				Simulator sim = findSimulator();
				//				if (sim != null) {
				//					ZILValue v = sim.getValue(sim.getCurrentTime(), s, sim.getCurrentTime());
				//					t = t + ": " + v;
				//				}

				selectionLabel.setText(t);
				selectedRTLSignal = s;
				addHighlight(s);
				return button_ == 1;
			}
		}

		selectedRTLModule = null;
		n = rtlg.getNumSubs();
		for (int i = 0; i < n; i++) {
			RTLModule sub = rtlg.getSub(i);

			if (!visibleRTLModules.contains(sub))
				continue;

			VisualModule visualModule = par.getModule(sub);
			if ((visualModule != null) && visualModule.isHit(this, mx, my)) {
				RTLModule module = visualModule.getRTLModule();
				selectionLabel.setText("RTLModule " + module);
				selectedRTLModule = module;
				addHighlight(module);
				return button_ == 1;
			}
		}

		return button_ == 1;
	}

	public void handleMouseDoubleClick(int mx_, int my_) {
		// project coordinates, find out what has been hit
		int mx = mx_ + visibleOffset.x - offscreenOffset.x;
		int my = my_ + visibleOffset.y - offscreenOffset.y;

		int n = par.getNumChannels();
		for (int i = 0; i < n; i++) {
			Channel c = par.getChannel(i);

			RTLSignal s = c.getSignalHit(this, mx, my);

			if (s != null) {
				ASTObject io = s.getSource();
				if (io != null) {
					showSource(io);
				}

				return;
			}
		}

		n = rtlg.getNumSubs();

		for (int i = 0; i < n; i++) {
			RTLModule sub = rtlg.getSub(i);

			VisualModule visualModule = par.getModule(sub);
			if ((visualModule != null) && visualModule.isHit(this, mx, my)) {
				RTLModule module = visualModule.getRTLModule();
				if (module instanceof RTLGraph) {
					if (!busyElaborating) {
						final RTLGraph subGraph = (RTLGraph) module;

						setRTLGraph(subGraph);

						//						busyElaborating = true;
						//
						//						Cursor cursor = display.getSystemCursor(SWT.CURSOR_WAIT);
						//						getSite().getShell().setCursor(cursor);
						//
						//						final Job job = new Job("Elaborating subgraph...") {
						//
						//							protected IStatus run(IProgressMonitor monitor) {
						//
						//								// incremental elaboration, if neccessary
						//
						//								final ArchitectureEParms parms = subGraph.getArchitectureEParms();
						//								if (parms != null) {
						//
						//									long timer = System.currentTimeMillis();
						//									ZamiaPlugin.out.println("Incremental elaboration of " + parms.arch + " started.");
						//									monitor.beginTask("Elaborating...", 100);
						//
						//									try {
						//										parms.arch.elaborateStatements(subGraph, true);
						//									} catch (ZamiaException e) {
						//										e.printStackTrace();
						//										final SourceLocation location = e.getLocation();
						//										e.printStackTrace(ZamiaPlugin.out);
						//										ZamiaPlugin.out.println("location: " + location);
						//										ZamiaProject zprj = rtlg.getSource().getLibrary().getZamiaProject();
						//										final IProject prj = ZamiaProjectMap.getProject(zprj);
						//
						//										Display.getDefault().asyncExec(new Runnable() {
						//											public void run() {
						//												showSource(prj, location);
						//											}
						//										});
						//
						//									}
						//									long timer2 = System.currentTimeMillis();
						//									double t = timer2 - timer;
						//									ZamiaPlugin.out.println("Incremental elaboration of " + parms.arch + " done, took " + t / 1000.0 + "s.");
						//									monitor.worked(50);
						//
						//								}
						//								Display.getDefault().asyncExec(new Runnable() {
						//									public void run() {
						//										setRTLGraph(subGraph);
						//										busyElaborating = false;
						//										getSite().getShell().setCursor(null);
						//									}
						//								});
						//								return Status.OK_STATUS;
						//							}
						//						};
						//						job.schedule();
					}
					break;
				} else if (module instanceof RTLFSM) {
					try {
						final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

						String editorId = "org.zamia.plugin.views.fsm.ui.FSMEditor";

						FSM fsm = ((RTLFSM) module).getFSM();

						FSMEditorInput input = new FSMEditorInput(fsm);

						page.openEditor(input, editorId);

					} catch (PartInitException e) {
						e.printStackTrace();
					}

				} else {

					if (module instanceof ZILInterpreter) {
						ZILInterpreter interpreter = (ZILInterpreter) module;

						ZILInterpreterCode code = interpreter.getCode();

						ZamiaPlugin.out.println();
						ZamiaPlugin.out.println("Stack machine code dump of " + interpreter + ":");

						code.dump(ZamiaPlugin.out);
					}

					// show source

					ASTObject io = module.getSource();
					if (io != null) {
						showSource(io);
					}
				}
			}
		}
	}

	public RTLModule getSelectedModule() {
		return selectedRTLModule;
	}

	public RTLSignal getSelectedRTLSignal() {
		return selectedRTLSignal;
	}

	public void navigate(String path_) {
		// FIXME
		// RTLGraph newNL = project.findRTLGraph(path_);
		//
		// if (newNL == null) {
		// MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
		// box.setText("Invalid location");
		// box.setMessage("Sorry, location not found:\n" + path_);
		// box.open();
		// location.setText(nl.getPath());
		// } else
		// setRTLGraph(newNL);
	}

	// since gtk swt doesn't support printing,
	// we're implementing our own postscript printing
	// framework here
	public void doPrintPS() {

		Format format = getPrintFormat();

		if (format == null)
			return;

		FileDialog dialog = new FileDialog(control.getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.ps" });
		dialog.setText("Print to file...");
		dialog.setFileName("zamia.ps");
		String filename = dialog.open();

		if (filename == null)
			return;

		try {
			PrintStream outFile = new PrintStream(new FileOutputStream(filename));

			par.print(format, outFile);
			outFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Format format;

	private Format getPrintFormat() {
		format = null;
		String numPages[] = new String[] { "1", "2", "3", "4", "5", "6" };

		final Shell shell = new Shell(control.getDisplay());
		shell.setText("Choose print format");

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.makeColumnsEqualWidth = false;
		shell.setLayout(gridLayout);

		Label lX = new Label(shell, SWT.NONE);
		lX.setText("Number of pages: ");
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		lX.setLayoutData(gridData);

		final Combo comboX = new Combo(shell, SWT.SIMPLE);
		comboX.setItems(numPages);
		comboX.select(0);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		comboX.setLayoutData(gridData);

		Label lY = new Label(shell, SWT.NONE);
		lY.setText(" x ");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.CENTER;
		lY.setLayoutData(gridData);

		final Combo comboY = new Combo(shell, SWT.SIMPLE);
		comboY.setItems(numPages);
		comboY.select(0);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		comboY.setLayoutData(gridData);

		Label l = new Label(shell, SWT.NONE);
		l.setText(" ");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		l.setLayoutData(gridData);
		l = new Label(shell, SWT.NONE);
		l.setText(" ");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		l.setLayoutData(gridData);

		final Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		cancelButton.setLayoutData(gridData);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				format = null;
				shell.dispose();
			}
		});
		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		okButton.setLayoutData(gridData);
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int numX = comboX.getSelectionIndex() + 1;
				int numY = comboY.getSelectionIndex() + 1;
				// int numY = Integer.parseInt(listY.getSelection()[0]);
				System.out.println("selection " + numX + " x " + numY);
				format = new Format(numX, numY);
				shell.dispose();
			}
		});

		// shell.setSize(400, 400);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		shell.dispose();
		return format;
	}

	public void doPrint() {

		PrintDialog dialog = new PrintDialog(control.getShell());
		PrinterData data = dialog.open();
		if (data == null)
			return;

		// PrinterData data = Printer.getDefaultPrinterData();
		// if (data == null) {
		// System.out.println("Warning: No default printer.");
		// return;
		// }
		Printer printer = new Printer(data);
		if (printer.startJob("SWT Printing Snippet")) {
			Color black = printer.getSystemColor(SWT.COLOR_BLACK);
			Color white = printer.getSystemColor(SWT.COLOR_WHITE);
			//			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			//			int leftMargin = dpi.x + trim.x; // one inch from left side of
			// paper
			//			int topMargin = dpi.y / 2 + trim.y; // one-half inch from top edge
			// of paper
			GC gc = new GC(printer);
			// font
			if (printer.startPage()) {
				gc.setBackground(white);
				gc.setForeground(black);

				Point oldOffset = new Point(offscreenOffset.x, offscreenOffset.y);
				Point oldSize = new Point(offscreenSize.x, offscreenSize.y);
				double oldZoom = zoomFactor;

				// offscreenSize.x = trim.width ; // FIXME: SWT GTK seems to be
				// broken still
				// offscreenSize.y = trim.height ;
				// offscreenOffset.x = -trim.x;
				// offscreenOffset.y = -trim.y;
				offscreenOffset.x = -dpi.x;
				offscreenOffset.y = -dpi.y;
				offscreenSize.x = 7 * dpi.x;
				offscreenSize.y = 11 * dpi.y;

				double fx = (double) offscreenSize.x / (double) totalSize.x;
				double fy = (double) offscreenSize.y / (double) totalSize.y;

				if (fx > fy)
					zoomFactor = fy;
				else
					zoomFactor = fx;

				paintOffscreen(gc);

				offscreenOffset.x = oldOffset.x;
				offscreenOffset.y = oldOffset.y;
				offscreenSize.x = oldSize.x;
				offscreenSize.y = oldSize.y;
				zoomFactor = oldZoom;

				printer.endPage();
			}
			gc.dispose();
			printer.endJob();
		}
		printer.dispose();
	}

	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public Font getSmallFont() {
		return smallFont;
	}

	public Font getNormalFont() {
		return normalFont;
	}

	public Font getBigFont() {
		return bigFont;
	}

	public void showSource(IProject prj_, SourceLocation location_) {
		IWorkbenchPage page = getViewSite().getPage();
		ZamiaPlugin.showSource(page, prj_, location_, 0);
	}

	public void showSource(ASTObject io_) {

		ZamiaProject s = io_.getZPrj();
		IProject prj = ZamiaProjectMap.getProject(s);

		if (prj == null)
			return;

		SourceLocation location = io_.getLocation();
		showSource(prj, location);
	}

	public void setHighlight(RTLModule module_) {
		clearHighlight();
		addHighlight(module_);
	}

	public void setHighlight(RTLSignal s_) {
		clearHighlight();
		addHighlight(s_);
	}

	private void calcSize(CoolItem item) {
		Control control = item.getControl();
		Point pt = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pt = item.computeSize(pt.x, pt.y);
		item.setSize(pt);
	}

	public Display getDisplay() {
		return display;
	}

	public boolean isShowPins() {
		return showPins;
	}

	private void doShowSource() {
		ASTObject io = null;
		if (selectedRTLModule != null) {
			io = selectedRTLModule.getSource();
		}
		if (selectedRTLSignal != null) {
			io = selectedRTLSignal.getSource();
		}
		if (io == null) {
			io = rtlg.getArch();
		}
		showSource(io);
	}

	private void doShowReceivers(RTLModule module_) {
		if (module_ != null) {
			visibleRTLModules.add(module_);

			clearHighlight();

			ArrayList<RTLModule> receivers = rtlg.getReceivers(module_);

			int n = receivers.size();
			for (int i = 0; i < n; i++) {
				RTLModule r = (RTLModule) receivers.get(i);

				visibleRTLModules.add(r);
				highlightModules.add(r);
			}

			placeAndRoute();
			updateZoom(zoom.getFactor());

			// redraw
			offscreenValid = false;
			canvas.redraw();
			// update(project, Project.NOTIFY_STRUCTURE);
		}

	}

	private void doShowDrivers(RTLModule module_) {
		if (module_ != null) {
			visibleRTLModules.add(module_);
			clearHighlight();

			ArrayList<RTLModule> cone = rtlg.getDrivers(module_);

			int n = cone.size();
			for (int i = 0; i < n; i++) {
				RTLModule r = cone.get(i);

				visibleRTLModules.add(r);
				highlightModules.add(r);
			}

			placeAndRoute();
			updateZoom(zoom.getFactor());

			// redraw
			offscreenValid = false;
			canvas.redraw();
			// update(project, Project.NOTIFY_STRUCTURE);
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
