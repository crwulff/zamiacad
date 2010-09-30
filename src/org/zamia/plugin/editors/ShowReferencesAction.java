/*
 * Copyright 2007-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.analysis.ast.ASTDeclarationSearch;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.DeclarativeItem;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.SignalDeclaration;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ShowReferencesAction extends StaticAnalysisAction {

	private ShowReferencesDialog fdlg;

	private Shell fShell;

	public void run(IAction a) {

		NewSearchUI.activateSearchResultView();

		try {
			processSelection();

			IWorkbenchWindow window = ZamiaPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
			if (window == null) {
				logger.error("ShowReferencesAction: Internal error: window == null.");
				return;
			}

			fShell = window.getShell();
			if (fLocation == null) {
				showError("Failed to determine source location.");
				return;
			}

			ASTObject nearest = SourceLocation2AST.findNearestASTObject(fLocation, true, fZPrj);
			if (nearest == null) {
				showError("Couldn't map caret position to syntax tree.");
				return;
			}
			
			DeclarativeItem decl = ASTDeclarationSearch.search(nearest, fZPrj);
			if (decl==null) {
				showError("Couldn't find declaration of\n"+nearest);
				return;
			}
			
			if (fdlg == null) {
				fdlg = new ShowReferencesDialog(window.getShell());
			}
			
			fdlg.setSearchJobText("Search for "+decl+"\nLocation: "+fLocation+"\nPath: "+fPath);
			
			if (decl instanceof SignalDeclaration || decl instanceof InterfaceDeclaration) {
				fdlg.setPath(fPath);
			} else {
				fdlg.setPath(null);
			}

			if (fdlg.open() == Window.OK) {
				NewSearchUI.runQueryInBackground(new ReferencesSearchQuery(this, fdlg.isSearchUp(), fdlg.isSearchDown(), false, fdlg.isUsePath(), fdlg.isWritersOnly(), fdlg.isReadersOnly()));
			}

		} catch (Exception e) {
			el.logException(e);
			showError("Catched exception:\n" + e + "\nSee log for details.");
		}
	}

	private void showError(String aMsg) {
		logger.error("ShowReferencesAction: Error: %s", aMsg);
		ZamiaPlugin.showError(fShell, "ShowReferencesAction: Error", aMsg, "");
	}

}
