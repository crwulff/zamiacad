/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 18, 2009
 */
package org.zamia.plugin;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.zamia.DUManager;
import org.zamia.DesignUnitStub;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGModule;
import org.zamia.plugin.editors.ZamiaEditor;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DesignUnit;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class OpenUnitAction implements IWorkbenchWindowActionDelegate {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IWorkbenchWindow fWindow;

	public void run(IAction action) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IWorkspaceRoot root = workspace.getRoot();

		IProject[] prjs = root.getProjects();

		if (prjs == null) {
			logger.error("No projects found.");
		}

		ArrayList<UnitWrapper> units = new ArrayList<UnitWrapper>();

		for (int i = 0; i < prjs.length; i++) {

			IProject prj = prjs[i];

			if (!prj.isAccessible())
				continue;

			logger.debug("OpenUnitAction: collecting units from project %s", prj);

			ZamiaProject zprj = ZamiaProjectMap.getZamiaProject(prj);

			if (zprj == null) {
				logger.error("OpenUnitAction: no corresponding Zamia project found for eclipse project '%s'", prj);
				continue;
			}

			DUManager dum = zprj.getDUM();

			int m = dum.getNumStubs();
			for (int j = 0; j < m; j++) {
				DesignUnitStub stub = dum.getStub(j);

				//				System.out.println("Stub in prj: " + stub);

				units.add(new UnitWrapper(stub.getDUUID(), zprj, prj));
			}

		}

		UnitSelectionDialog dlg = new UnitSelectionDialog(fWindow.getShell(), units);

		dlg.setInitialPattern("WORK.*");
		if (dlg.open() == Window.OK) {

			UnitWrapper stub = (UnitWrapper) dlg.getFirstResult();

			IProject prj = stub.getPrj();
			ZamiaProject zprj = stub.getZPrj();
			DUUID duuid = stub.getDUUID();

			DUManager dum = zprj.getDUM();

			DesignUnit du;
			try {
				du = dum.getDU(duuid);
				if (du == null) {
					logger.error("OpenUnitAction: failed to load DU %s", duuid);
					return;
				}

				IWorkbenchPage page = fWindow.getActivePage();

				/*
				 * try to find a valid path
				 * 
				 *  either we have a stored path in our database (then use that one)
				 *  or we use the default path of the IGModule corresponding to our DUUID
				 *  
				 */

				SourceLocation location = du.getLocation();
				String filename = location != null ? location.fSF.getAbsolutePath() : null;
				ToplevelPath path = null;

				if (filename != null) {
					String pathStr = zprj.lookupEditorPath(filename);
					if (pathStr != null) {
						path = new ToplevelPath(pathStr);
					}
				}

				if (path == null) {
					IGManager igm = zprj.getIGM();

					DUUID archDUUID = dum.getArchDUUID(duuid);

					if (archDUUID != null) {

						String signature = IGInstantiation.computeSignature(archDUUID, null);

						IGModule module = igm.findModule(signature);
						if (module != null) {
							path = module.getStructure().getPath();
						}
					}
				}

				IEditorPart editor = ZamiaPlugin.showSource(page, prj, du.getLocation(), 0);
				if (path != null && editor instanceof ZamiaEditor) {
					ZamiaEditor ze = (ZamiaEditor) editor;
					ze.setPath(path);
				}
			} catch (ZamiaException e) {
				el.logException(e);
			}

		}
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow aWindow) {
		fWindow = aWindow;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
