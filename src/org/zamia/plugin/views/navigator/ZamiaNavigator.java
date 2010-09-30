/*
 * Copyright 2006-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 18, 2006
 */

package org.zamia.plugin.views.navigator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.ShowInContext;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.plugin.ZamiaPlugin;


/**
 * Customized navigator view for HDL design navigation
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZamiaNavigator extends CommonNavigator {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public static final String VIEW_ID = "org.zamia.plugin.views.navigator.ZamiaNavigator";

	/**
	 * This is the memento to be used.
	 */
	@Override
	public boolean show(ShowInContext context) {

		logger.debug("NAVIGATOR: ZamiaNavigator.show(): " + context);

		logger.error("NAVIGATOR: ZamiaNavigator.show(): currently broken.");

		// Object input = context.getInput();
		// if (input instanceof IEditorInput) {
		//
		// IEditorInput fei = (IEditorInput) input;
		//
		// IFile file = ResourceUtil.getFile(fei);
		// if (file == null) {
		// logger.error("NAVIGATOR: Failed to find file for editor input '%s'",
		// input);
		// return false;
		// }
		//
		// CommonViewer commonViewer = getCommonViewer();
		// if (commonViewer == null) {
		// logger.error("NAVIGATOR: commonViewer is NULL");
		// return false;
		// }
		//
		// IProject prj = file.getProject();
		// SourceFile sf = ZamiaPlugin.getSourceFile(file);
		//
		// ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);
		//
		// DUManager dum = zprj.getDUManager();
		// IGManager igm = zprj.getIGM();
		//
		// try {
		// HashSetArray<DUUID> duuids;
		// duuids = dum.compileFile(sf, null, null, Integer.MAX_VALUE, true,
		// true, true, true);
		// int n = duuids.size();
		//
		// HashSet<IGModule> modules = new HashSet<IGModule>();
		//
		// HashSet<IGModuleWrapper> selectedNodes = new
		// HashSet<IGModuleWrapper>();
		//
		// IContentProvider contentProvider =
		// getCommonViewer().getContentProvider();
		//
		// if (!(contentProvider instanceof ZamiaContentProvider)) {
		// return false;
		// }
		//				
		// ZamiaContentProvider zcp = (ZamiaContentProvider) contentProvider;
		//				
		// for (int i = 0; i < n; i++) {
		// DUUID duuid = duuids.get(i);
		//
		// IGModule module = igm.findModule(duuid.getUID(), false);
		//
		// modules.add(module);
		// selectedNodes.add(zcp.findWrapper(module));
		// }
		//
		// Object[] selectedNodesArray = selectedNodes.toArray();
		//
		// StructuredSelection selection = new
		// StructuredSelection(selectedNodesArray);
		// commonViewer.setSelection(selection, true);
		//
		// // also expand parent nodes and the project
		// HashSet expanded = new HashSet();
		// expanded.add(prj);
		//
		// ZStack<InstNode> todo = new ZStack<InstNode>();
		// n = selectedNodes.size();
		// for (int i = 0; i < n; i++) {
		// todo.push((InstNode) selectedNodesArray[i]);
		// }
		//
		// HashSet<InstNode> done = new HashSet<InstNode>();
		//
		// while (!todo.isEmpty()) {
		//
		// InstNode node = todo.pop();
		//
		// if (done.contains(node))
		// continue;
		//
		// done.add(node);
		//
		// if (!selectedNodes.contains(node)) {
		// expanded.add(node);
		// }
		//
		// n = node.getNumInstantiatiors();
		// for (int i = 0; i < n; i++) {
		// InstNode n2 = node.getInstantiator(i);
		// todo.push(n2);
		// }
		// }
		//
		// Object[] expandedArray = expanded.toArray();
		//
		// Object[] expandedElements = commonViewer.getExpandedElements();
		// Object[] newExpandedElements = new Object[expandedArray.length +
		// expandedElements.length];
		// System.arraycopy(expandedElements, 0, newExpandedElements, 0,
		// expandedElements.length);
		// System.arraycopy(expandedArray, 0, newExpandedElements,
		// expandedElements.length, expandedArray.length);
		// commonViewer.setExpandedElements(newExpandedElements);
		//
		// logger.debug("NAVIGATOR: show prj=%s, sf='%s', zprj=%s", prj, sf,
		// zprj);
		//
		// return true;
		//
		// } catch (IOException e) {
		// el.logException(e);
		// } catch (ZamiaException e) {
		// el.logException(e);
		// }
		// }
		return super.show(context);
	}

	// @Override
	// public void init(IViewSite aSite, IMemento aMemento) throws
	// PartInitException {
	// super.init(aSite, aMemento);
	// // memento = aMemento;
	// }

	@Override
	protected CommonViewer createCommonViewer(Composite aParent) {
		CommonViewer cv = super.createCommonViewer(aParent);
		cv.setComparator(null);
		cv.setSorter(null);
		return cv;
	}

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		CommonViewer cv = getCommonViewer();
		cv.setComparator(null);
	}
	/**
	 * Returns the element contained in the EditorInput
	 */
	Object getElementOfInput(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	public void refresh() {
		CommonViewer commonViewer = getCommonViewer();

		//ZamiaContentProvider cp = (ZamiaContentProvider) commonViewer.getContentProvider();
		//cp.resetUsageCounters();
		
		commonViewer.refresh();
		
		//cp.cleanup();
	}
	
	static class NavigatorRefreshJob extends Job {
		
		private long fDelay;
		
		public NavigatorRefreshJob(long aDelay) {
			super("Refresh ZamiaCAD Navigator");
			fDelay = aDelay;
		}

		protected IStatus run(IProgressMonitor monitor) {
			
			try {
				Thread.sleep(fDelay);
				Display d = Display.getDefault();
				
				d.asyncExec(new Runnable() {
					public void run() {
						IWorkbenchPage page = ZamiaPlugin.getWorkbenchWindow().getActivePage();

						ZamiaNavigator zamiaNavigator = (ZamiaNavigator) page.findView(ZamiaNavigator.VIEW_ID);
						zamiaNavigator.refresh();
					}
				});
			} catch (Throwable t) {
				el.logException(t);
			}

			return Status.OK_STATUS;
		}
	}

	public static void refresh(long aDelay) {
		NavigatorRefreshJob job = new NavigatorRefreshJob(aDelay);
		job.setPriority(Job.SHORT);
		job.schedule();
	}
}
