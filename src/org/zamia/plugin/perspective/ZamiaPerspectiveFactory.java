/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * 
 * Created on 06.04.2005
 */
package org.zamia.plugin.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Creates the zamia eclipse perspective with a default set of views
 * 
 * @author Guenter Bartsch
 */
public class ZamiaPerspectiveFactory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.2, editorArea); 
		folder.addView("org.zamia.plugin.views.navigator.ZamiaNavigator");
		folder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout consoleArea = layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.6, editorArea); 
        consoleArea.addView(IPageLayout.ID_PROBLEM_VIEW);
		//consoleArea.addView("org.zamia.plugin.views.rtl.RTLView");
		consoleArea.addView("org.zamia.plugin.views.sim.SimulatorView");
		//consoleArea.addView("org.zamia.plugin.views.console.ZamiaConsole");
		consoleArea.addView("org.eclipse.ui.console.ConsoleView");
		consoleArea.addView("org.eclipse.search.ui.views.SearchView");
        //consoleArea.addView(IPageLayout.ID_TASK_LIST);
		consoleArea.addView(IProgressConstants.PROGRESS_VIEW_ID);
		consoleArea.addView("org.eclipse.pde.runtime.LogView");
		
	
		
		folder= layout.createFolder("right", IPageLayout.RIGHT, (float)0.8, editorArea); //$NON-NLS-1$
		folder.addView("org.eclipse.ui.views.ContentOutline");
		
        // new actions - zamia wizards
        layout.addNewWizardShortcut("org.zamia.plugin.ui.NewZamiaProjectWizard"); 
        layout.addNewWizardShortcut("org.zamia.plugin.ui.NewFileWizard"); 
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");
        
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
        layout.addActionSet("org.zamia.plugin.ZamiaActionSet");
	}
}
