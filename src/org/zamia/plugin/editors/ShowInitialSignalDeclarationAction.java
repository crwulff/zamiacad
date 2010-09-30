/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */

package org.zamia.plugin.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.search.ui.NewSearchUI;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ShowInitialSignalDeclarationAction extends StaticAnalysisAction {

	
	public void run(IAction a) {

		NewSearchUI.activateSearchResultView();

		try {
			processSelection();
			
			NewSearchUI.runQueryInBackground(new ReferencesSearchQuery(this, true, false, true, false, false, false));
			
		} catch (BadLocationException e) {
			
			el.logException(e);
		}

	}

//	public void run(IAction a) {
//
//		NewSearchUI.activateSearchResultView();
//
//		try {
//			processSelection();
//
//			
//			ZamiaProject zprj = getZamiaProject();
//			
//			DeclarationSearch ds = new DeclarationSearch(zprj);
//
//			DeclarativeItem declaration = ds.search(sf, line, col);
//
//			if (declaration != null) {
//
//				ReferencesSearch rs = new ReferencesSearch(zprj);
//
//				ArrayList<ReferenceSearchResult> results = rs.search(declaration, true, false);
//
//				int n = results.size();
//
//				for (int i = 0; i < n; i++) {
//
//					ReferenceSearchResult res = results.get(i);
//
//					int m = res.getNumRefs();
//					for (int j = 0; j<m; j++) {
//						ReferenceSite referenceSite = res.getRef(j);
//					if (referenceSite.getRefType() == RefType.Declaration) {
//						
//						IWorkbenchPage page = editor.getEditorSite().getPage();
//
//						ZamiaPlugin.showSource(page, prj, referenceSite.getLocation(), referenceSite.getLength());
//						
//						break;
//					}
//					}
//				}
//			} else {
//				logger.error("SA: Failed to find declaration for selection.");
//			}
//			
//		} catch (BadLocationException e) {
//			el.logException(e);
//		} catch (ZamiaException e) {
//			el.logException(e);
//		} catch (IOException e) {
//			el.logException(e);
//		}
//	}
}
