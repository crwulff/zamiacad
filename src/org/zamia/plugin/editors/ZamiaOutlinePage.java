/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.vhdl.ast.ASTObject;


/**
 * Content outline page for the VHDL editor.
 * 
 * @author Guenter Bartsch
 */

public class ZamiaOutlinePage extends ContentOutlinePage {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private ZamiaEditor fEditor;

	private Object fInput;

	private ZamiaOutlineLabelProvider fLabelProvider;

	private boolean fIsDisposed;

	private OutlineSearchDialog fSearchDlg;

	private TreeViewer fTreeViewer;

	public ZamiaOutlinePage(ZamiaEditor aEditor) {
		fEditor = aEditor;
		fIsDisposed = true;
	}

	public class OutlineSearchAction extends org.eclipse.jface.action.Action {

		private final ZamiaOutlineContentProvider fContentProvider;

		public OutlineSearchAction(final ZamiaOutlineContentProvider aContentProvider) {
			super("org.zamia.actions.OutlineSearchAction", AS_PUSH_BUTTON);
			fContentProvider = aContentProvider;
			final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(ZamiaPlugin.PLUGIN_ID, "share/images/search.gif");
			setImageDescriptor(desc);
			setToolTipText("Search...");
		}

		@Override
		public void run() {

			logger.info("ZamiaOutlinePage: Search.");

			fSearchDlg.connect(fContentProvider);

			fSearchDlg.open();
			Object sel[] = fSearchDlg.getResult();
			if (sel == null)
				return;
			for (int i = 0; i < sel.length; i++) {
				if (sel[i] instanceof StructuredSelection) {
					StructuredSelection s = (StructuredSelection) sel[i];

					try {

						// FIXME: doesn't work (tree needs to be expanded)
						//fTreeViewer.setSelection(s, true);

						int n = s.size();
						if (n < 1) {
							continue;
						}

						Object o = s.toArray()[n - 1];
						if (o instanceof ASTObject) {

							ASTObject ast = (ASTObject) o;

							ZamiaReconcilingStrategy strategy = fEditor.getReconcilingStrategy();

							ZamiaProject zprj = strategy.getZPrj();

							ZamiaPlugin.showSource(getSite().getPage(), ZamiaProjectMap.getProject(zprj), ast.getLocation(), 0);
						}

					} catch (Throwable e1) {
						el.logException(e1);
					}
				}
			}

			fSearchDlg.disconnect();
		}
	}

	public void createControl(Composite aParent) {
		super.createControl(aParent);

		fLabelProvider = new ZamiaOutlineLabelProvider(aParent);

		fSearchDlg = new OutlineSearchDialog(getSite().getShell());
		fSearchDlg.setTitle("Outline Search");

		fTreeViewer = getTreeViewer();

		ZamiaOutlineContentProvider contentProvider = new ZamiaOutlineContentProvider(fEditor);

		fTreeViewer.setContentProvider(contentProvider);
		fTreeViewer.setLabelProvider(fLabelProvider);
		fTreeViewer.addSelectionChangedListener(this);
		fTreeViewer.setAutoExpandLevel(2);

		// Adds button to viewer's toolbar
		final IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
		mgr.add(new OutlineSearchAction(contentProvider));

		if (fInput != null) {
			fTreeViewer.setInput(fInput);
		}

		fIsDisposed = false;

		update();
	}

	public void setInput(Object aInput) {
		fInput = aInput;
		update();
	}

	public void update() {
		TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			Control control = viewer.getControl();

			if ((control != null) && !control.isDisposed()) {
				viewer.removeSelectionChangedListener(this);
				control.setRedraw(false);
				viewer.setInput(fInput);

				// viewer.expandAll();
				control.setRedraw(true);
				// selectNode(fEditor.getCursorLine(), true);
				viewer.addSelectionChangedListener(this);
			}
		}
	}

	public boolean isDisposed() {
		return fIsDisposed;
	}

}
