/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite;
import org.zamia.analysis.SourceLocation2IG;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ig.IGReferencesSearch;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.views.sim.SimulatorView;
import org.zamia.util.HashSetArray;
import org.zamia.util.Pair;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ShowInSimAction extends StaticAnalysisAction {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	static ZamiaEditor fEditor;

	static Shell fShell;

	static Display fDisplay;

	class ShowInSimJob extends Job {

		private final ToplevelPath fPath;

		public ShowInSimJob() {
			super("Show in Simulator View");
			fPath = getPath();
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {

				if (fPath == null) {
					ZamiaPlugin.showError(fShell, "No Path", "No path was given.", "Editor location field empty?");
				}

				SourceLocation location = getLocation();

				Pair<IGItem, ToplevelPath> nearest = SourceLocation2IG.findNearestItem(location, fPath, getZamiaProject());

				if (nearest != null) {

					IGItem item = nearest.getFirst();
					ToplevelPath path = nearest.getSecond();

					logger.info("ShowInSimJob: nearest item: %s, path: %s", item, path);

					if (item != null) {

						IGObject obj = null;

						if (item instanceof IGObject) {
							obj = (IGObject) item;
						} else if (item instanceof IGOperationObject) {
							obj = ((IGOperationObject) item).getObject();
						}

						if (obj != null) {

							// find local declaration, see if sim has a waveform for that, try global decl otherwise
							IGReferencesSearch rs = new IGReferencesSearch(getZamiaProject());

							ReferenceSearchResult res = rs.search(obj, path, false, false, false, false);

							boolean success = false;
							
							if (res != null) {
								logger.debug("ShowInSimJob: local search result: %s", res);

								HashSetArray<ToplevelPath> originalPaths = new HashSetArray<ToplevelPath>();

								traverseResult(res, originalPaths);

								if (!originalPaths.isEmpty()) {
									int n = originalPaths.size();
									for (int i = 0; i < n; i++) {
										ToplevelPath signalPath = originalPaths.get(i);
										
										logger.debug ("ShowInSimJob: trying local declaration %s", signalPath);

										if (fView.hasSignal(signalPath.getPath())) {
											logger.debug ("ShowInSimJob: trying local declaration %s succeeded!", signalPath);
											showInSim(signalPath);
											success = true;
											break;
										}
									}
								}
							}


							if (!success) {

								logger.debug("ShowInSimJob: Didn't find waveform for %s, trying to find original declarations...", item);

								// find original declarations

								res = rs.search(obj, path, true, false, false, false);

								if (res != null) {
									logger.debug("ShowInSimJob: Search result: %s", res);

									HashSetArray<ToplevelPath> originalPaths = new HashSetArray<ToplevelPath>();

									traverseResult(res, originalPaths);

									if (!originalPaths.isEmpty()) {

										int n = originalPaths.size();
										StringBuilder buf = new StringBuilder("Will try to trace these original declarations:\n");

										for (int i = 0; i < n; i++) {
											buf.append("\n");
											buf.append(originalPaths.get(i));
										}

										ZamiaPlugin.showInfo(fShell, "Path corrected", "Simulator cannot provide waveform information for\n\n" + obj + "\n\n" + buf,
												"interface signal?");

										for (int i = 0; i < n; i++) {
											showInSim(originalPaths.get(i));
										}

									} else {
										ZamiaPlugin.showError(fShell, "Unknown Object", "Simulator cannot provide waveform information for\n\n" + obj
												+ "\n\nAlso couldn't find any declarations for this object.", "Unknown interface? Only driven by literals?");
									}

								} else {
									ZamiaPlugin.showError(fShell, "Unknown Object", "Simulator cannot provide waveform information for\n\n" + obj,
											"Not a signal / unknown interface ?");
								}

							}
						} else {
							ZamiaPlugin.showError(fShell, "Not a signal.", "Cursor location mapped to \n\n" + item + "\n\nWhich is not a signal.", "Not a signal.");
						}
					} else {
						ZamiaPlugin.showError(fShell, "No Intermediate Object", "Current source location does not correspond to any intermediate object.",
								"IG Item search didn't return an item (path only).");
					}
				} else {
					ZamiaPlugin.showError(fShell, "No Intermediate Object", "Current source location does not correspond to any intermediate object.", "IG Item search failed.");
				}
			} catch (Throwable e) {
				ZamiaPlugin.showError(fShell, "Zamia Exception Caught", e.getMessage(), "");
				el.logException(e);
			}

			return Status.OK_STATUS;
		}

		private void traverseResult(ReferenceSearchResult aRes, HashSetArray<ToplevelPath> aOriginalPaths) {

			if (aRes == null) {
				return;
			}

			if (aRes instanceof ReferenceSite) {

				ReferenceSite site = (ReferenceSite) aRes;

				if (site.getRefType() == RefType.Declaration) {
					long dbid = site.getDBID();

					if (dbid != 0) {
						ZDB zdb = getZamiaProject().getZDB();

						IGObject obj = (IGObject) zdb.load(dbid);
						if (obj != null) {
							aOriginalPaths.add(site.getPath().append(obj.getId()));
						}
					}
				}
			}

			int n = aRes.getNumChildren();
			for (int i = 0; i < n; i++) {
				traverseResult(aRes.getChild(i), aOriginalPaths);
			}
		}
	}

	private IWorkbenchPage fPage;

	private SimulatorView fView;

	private ToplevelPath fSignalPath;

	// private SimulatorView fSimView;

	public void setActiveEditor(IAction aAction, IEditorPart aTargetEditor) {
		if (aTargetEditor == null) {
			return;
		}
		if (!(aTargetEditor instanceof ZamiaEditor)) {
			return;
		}
		fEditor = (ZamiaEditor) aTargetEditor;
		fShell = fEditor.getSite().getShell();
		fDisplay = fShell.getDisplay();
	}

	public void selectionChanged(IAction aAction, ISelection aSelection) {
		aAction.setEnabled(true);
	}

	public void run(IAction aAction) {

		try {

			ToplevelPath path = fEditor.getPath();

			if (path == null) {
				ZamiaPlugin.showError(fEditor.getSite().getShell(), "No Path Information", "No path information found.", "Editor has no design path information.");
				return;
			}

			//fPath = path.getPath();

			processSelection();

			IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

			fPage = window.getActivePage();

			fView = (SimulatorView) fPage.showView("org.zamia.plugin.views.sim.SimulatorView");

			if (fView != null) {

				ShowInSimJob job = new ShowInSimJob();
				job.setPriority(Job.SHORT);
				job.schedule();
			}

		} catch (BadLocationException e) {
			el.logException(e);
		} catch (PartInitException e) {
			el.logException(e);
		}

	}

	private void showInSim(ToplevelPath aPath) {

		logger.info("ShowInSimAction: About to show %s", aPath);

		fSignalPath = aPath;

		fDisplay.asyncExec(new Runnable() {
			public void run() {

				fView.trace(fSignalPath.getPath());
			}
		});
	}

}
