/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */

package org.zamia.plugin.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class ZamiaActionProvider extends CommonActionProvider{
    
    private OpenFileAction openAction;
    private OpenResourceAction openResourceAction;
	private ISelectionProvider selectionProvider;

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        ICommonViewerSite viewSite = aSite.getViewSite();
        if(viewSite instanceof ICommonViewerWorkbenchSite){
            ICommonViewerWorkbenchSite site = (ICommonViewerWorkbenchSite) viewSite;
            selectionProvider = site.getSelectionProvider();
            openAction = new OpenFileAction(site.getPage(), selectionProvider);
            openResourceAction = new OpenResourceAction(site.getPage(), selectionProvider);
        }
    }
    
    public void fillActionBars(IActionBars actionBars) { 
        if(openResourceAction.isEnabled()){
            actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openResourceAction);
        }
    }
    
    public void fillContextMenu(IMenuManager menu) {
        if(openAction.isEnabled()){
            menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);        
        }
    }

}
