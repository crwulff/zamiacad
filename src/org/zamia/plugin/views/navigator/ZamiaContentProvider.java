/*
 * Copyright 2006-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 23, 2006
 */

package org.zamia.plugin.views.navigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.zamia.BuildPath;
import org.zamia.DUManager;
import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.SFDUInfo;
import org.zamia.SourceFile;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.ZamiaProjectBuilder;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DesignUnit;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZamiaContentProvider extends BaseWorkbenchContentProvider implements IResourceChangeListener {

	public final static boolean dump = false;

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static FSCache fsCache = FSCache.getInstance();

	private Viewer viewer;

	// caching of wrappers is necessary to keep items expanded during navigator refresh
	private HashMap<ZamiaProject, IGModuleWrapperCache> fCaches = new HashMap<ZamiaProject, IGModuleWrapperCache>();

	private IGModuleWrapperCache getCache(ZamiaProject aZPrj) {
		IGModuleWrapperCache cache = fCaches.get(aZPrj);
		if (cache == null) {
			cache = new IGModuleWrapperCache(aZPrj);
			fCaches.put(aZPrj, cache);
		}
		return cache;
	}

	@Override
	public Object[] getChildren(Object element) {

		// logger.debug("NAVIGATOR: looking for children of " + element);

		// long startTime = System.currentTimeMillis();

		if (element instanceof IProject) {
			IProject prj = (IProject) element;

			if (prj.isAccessible()) {

				ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

				IGModuleWrapperCache cache = getCache(zprj);

				ArrayList<Object> res = new ArrayList<Object>();

				DUManager dum = zprj.getDUM();
				BuildPath bp = zprj.getBuildPath();
				if (bp != null) {

					int n = bp.getNumToplevels();

					for (int i = 0; i < n; i++) {

						Toplevel tl = bp.getToplevel(i);

						DUUID duuid = dum.getArchDUUID(tl);

						if (duuid != null) {
							res.add(cache.getRedWrapper(tl, duuid));
							res.add(cache.getBlueWrapper(tl, duuid));
						}
					}
				}

				Object[] sc = super.getChildren(prj);

				int n = sc.length;
				for (int i = 0; i < n; i++)
					res.add(sc[i]);

				// long d = System.currentTimeMillis() - startTime;
				// logger.debug("NAVIAGTOR: getting children of %s took %d ms",
				// element.toString(), d);

				return res.toArray();
			}

		} else if (element instanceof IGModuleWrapper) {

			IGModuleWrapper wrapper = (IGModuleWrapper) element;

			return wrapper.getChildren();

		} else if (element instanceof IFile) {
			IFile file = (IFile) element;

			// System.out.println("Looking for children of " + file.getName());

			IProject prj = file.getProject();
			ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);
			DUManager dum = zprj.getDUM();

			try {

				SourceFile sf = ZamiaPlugin.getSourceFile(file);

				Object[] res = getDUs(dum, sf);

				// long d = System.currentTimeMillis() - startTime;
				// logger.debug("NAVIAGTOR: getting children of %s took %d ms",
				// element.toString(), d);

				return res;

			} catch (IOException e) {
				el.logException(e);
			}
		}
		return super.getChildren(element);
	}

	private Object[] getDUs(DUManager aDUM, SourceFile aSF) throws IOException {
		SFDUInfo info = null;
		try {
			info = aDUM.compileFile(aSF, null);
		} catch (ZamiaException e) {
			el.logException(e);
		}

		if (info != null) {

			int n = info.getNumDUUIDs();

			ArrayList<DesignUnit> dus = new ArrayList<DesignUnit>();

			for (int i = 0; i < n; i++) {

				DUUID duuid = info.getDUUID(i);

				try {
					DesignUnit du = aDUM.getDU(duuid);
					if (du != null) {
						dus.add(du);
					}
				} catch (Throwable t) {
					el.logException(t);
				}
			}

			return dus.toArray();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IFile) {

			IFile file = (IFile) element;
			String name = file.getName();
			return ZamiaProjectBuilder.fileNameAcceptable(name);
		} else if (element instanceof IGModuleWrapper) {

			if (dump)
				logger.info("ZamiaContentProvider: hasChildren(%s)...", element);

			IGModuleWrapper wrapper = (IGModuleWrapper) element;

			return wrapper.hasChildren();

		}
		return super.hasChildren(element);
	}

	/*
	 * (non-Javadoc) Method declared on IContentProvider.
	 */
	public void dispose() {
		if (viewer != null) {
			IWorkspace workspace = null;
			Object obj = viewer.getInput();
			if (obj instanceof IWorkspace) {
				workspace = (IWorkspace) obj;
			} else if (obj instanceof IContainer) {
				workspace = ((IContainer) obj).getWorkspace();
			}
			if (workspace != null) {
				workspace.removeResourceChangeListener(this);
			}
		}

		super.dispose();
	}

	/*
	 * (non-Javadoc) Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);

		this.viewer = viewer;
		IWorkspace oldWorkspace = null;
		IWorkspace newWorkspace = null;

		if (oldInput instanceof IWorkspace) {
			oldWorkspace = (IWorkspace) oldInput;
		} else if (oldInput instanceof IContainer) {
			oldWorkspace = ((IContainer) oldInput).getWorkspace();
		}

		if (newInput instanceof IWorkspace) {
			newWorkspace = (IWorkspace) newInput;
		} else if (newInput instanceof IContainer) {
			newWorkspace = ((IContainer) newInput).getWorkspace();
		}

		if (oldWorkspace != newWorkspace) {
			if (oldWorkspace != null) {
				oldWorkspace.removeResourceChangeListener(this);
			}
			if (newWorkspace != null) {
				newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IResourceChangeListener.
	 */
	public final void resourceChanged(final IResourceChangeEvent event) {

		processDelta(event.getDelta());

	}

	/**
	 * Process the resource delta.
	 * 
	 * @param delta
	 */
	@SuppressWarnings("unchecked")
	protected void processDelta(IResourceDelta delta) {

		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		final Collection runnables = new ArrayList();
		processDelta(delta, runnables);

		if (runnables.isEmpty()) {
			return;
		}

		// Are we in the UIThread? If so spin it until we are done
		if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
			runUpdates(runnables);
		} else {
			ctrl.getDisplay().asyncExec(new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					// Abort if this happens after disposes
					Control ctrl = viewer.getControl();
					if (ctrl == null || ctrl.isDisposed()) {
						return;
					}

					runUpdates(runnables);
				}
			});
		}

	}

	/**
	 * Run all of the runnables that are the widget updates
	 * 
	 * @param runnables
	 */
	@SuppressWarnings("unchecked")
	private void runUpdates(Collection runnables) {
		Iterator runnableIterator = runnables.iterator();
		while (runnableIterator.hasNext()) {
			((Runnable) runnableIterator.next()).run();
		}

	}

	/**
	 * Process a resource delta. Add any runnables
	 */
	@SuppressWarnings("unchecked")
	private void processDelta(IResourceDelta delta, Collection runnables) {
		// he widget may have been destroyed
		// by the time this is run. Check for this and do nothing if so.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		// Get the affected resource
		final IResource resource = delta.getResource();

		// If any children have changed type, just do a full refresh of this
		// parent,
		// since a simple update on such children won't work,
		// and trying to map the change to a remove and add is too dicey.
		// The case is: folder A renamed to existing file B, answering yes to
		// overwrite B.
		IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);
		for (int i = 0; i < affectedChildren.length; i++) {
			if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
				runnables.add(getRefreshRunnable(resource));
				return;
			}
		}

		// Opening a project just affects icon, but we need to refresh when
		// a project is closed because if child items have not yet been created
		// in the tree we still need to update the item's children
		int changeFlags = delta.getFlags();
		if ((changeFlags & IResourceDelta.OPEN) != 0) {
			if (resource.isAccessible()) {
				runnables.add(getUpdateRunnable(resource));
			} else {
				runnables.add(getRefreshRunnable(resource));
				return;
			}
		}
		// Check the flags for changes the Navigator cares about.
		// See ResourceLabelProvider for the aspects it cares about.
		// Notice we don't care about F_CONTENT or F_MARKERS currently.
		if ((changeFlags & (IResourceDelta.SYNC | IResourceDelta.TYPE | IResourceDelta.DESCRIPTION)) != 0) {
			runnables.add(getUpdateRunnable(resource));
		}
		// Replacing a resource may affect its label and its children
		if ((changeFlags & IResourceDelta.REPLACED) != 0) {
			runnables.add(getRefreshRunnable(resource));
			return;
		}

		/*
		 * re-parse file if source file was changed
		 */

		if ((changeFlags & (IResourceDelta.CHANGED | IResourceDelta.CONTENT)) != 0) {
			if (resource instanceof IFile) {
				// IFile file = (IFile) resource;
				// if(PythonPathHelper.isValidSourceFile(file)){
				runnables.add(getRefreshRunnable(resource));
				// }
			}
			return;
		}

		// Handle changed children .
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i], runnables);
		}

		// @issue several problems here:
		// - should process removals before additions, to avoid multiple equal
		// elements in viewer
		// - Kim: processing removals before additions was the indirect cause of
		// 44081 and its varients
		// - Nick: no delta should have an add and a remove on the same element,
		// so processing adds first is probably OK
		// - using setRedraw will cause extra flashiness
		// - setRedraw is used even for simple changes
		// - to avoid seeing a rename in two stages, should turn redraw on/off
		// around combined removal and addition
		// - Kim: done, and only in the case of a rename (both remove and add
		// changes in one delta).

		IResourceDelta[] addedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
		IResourceDelta[] removedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);

		if (addedChildren.length == 0 && removedChildren.length == 0) {
			return;
		}

		final Object[] addedObjects;
		final Object[] removedObjects;

		// Process additions before removals as to not cause selection
		// preservation prior to new objects being added
		// Handle added children. Issue one update for all insertions.
		int numMovedFrom = 0;
		int numMovedTo = 0;
		if (addedChildren.length > 0) {
			addedObjects = new Object[addedChildren.length];
			for (int i = 0; i < addedChildren.length; i++) {
				addedObjects[i] = addedChildren[i].getResource();
				if ((addedChildren[i].getFlags() & IResourceDelta.MOVED_FROM) != 0) {
					++numMovedFrom;
				}
			}
		} else {
			addedObjects = new Object[0];
		}

		// Handle removed children. Issue one update for all removals.
		if (removedChildren.length > 0) {
			removedObjects = new Object[removedChildren.length];
			for (int i = 0; i < removedChildren.length; i++) {
				removedObjects[i] = removedChildren[i].getResource();
				if ((removedChildren[i].getFlags() & IResourceDelta.MOVED_TO) != 0) {
					++numMovedTo;
				}
			}
		} else {
			removedObjects = new Object[0];
		}
		// heuristic test for items moving within same folder (i.e. renames)
		final boolean hasRename = numMovedFrom > 0 && numMovedTo > 0;

		Runnable addAndRemove = new Runnable() {
			public void run() {
				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					// Disable redraw until the operation is finished so we
					// don't
					// get a flash of both the new and old item (in the case of
					// rename)
					// Only do this if we're both adding and removing files (the
					// rename case)
					if (hasRename) {
						treeViewer.getControl().setRedraw(false);
					}
					try {
						if (addedObjects.length > 0) {
							treeViewer.add(resource, addedObjects);
						}
						if (removedObjects.length > 0) {
							treeViewer.remove(removedObjects);
						}
					} finally {
						if (hasRename) {
							treeViewer.getControl().setRedraw(true);
						}
					}
				} else {
					((StructuredViewer) viewer).refresh(resource);
				}
			}
		};
		runnables.add(addAndRemove);
	}

	/**
	 * Return a runnable for refreshing a resource.
	 * 
	 * @param resource
	 * @return Runnable
	 */
	private Runnable getRefreshRunnable(final IResource resource) {
		return new Runnable() {
			public void run() {
				((StructuredViewer) viewer).refresh(resource);
			}
		};
	}

	/**
	 * Return a runnable for refreshing a resource.
	 * 
	 * @param resource
	 * @return Runnable
	 */
	private Runnable getUpdateRunnable(final IResource resource) {
		return new Runnable() {
			public void run() {
				((StructuredViewer) viewer).update(resource, null);
			}
		};
	}

	public void resetUsageCounters() {
		// FIXME: -> cache
	}

	public void cleanup() {
		// FIXME: -> cache
	}
}
