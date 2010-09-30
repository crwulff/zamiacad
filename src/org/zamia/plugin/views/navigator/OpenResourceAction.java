/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.navigator;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author guenter bartsch
 */
public class OpenResourceAction extends OpenFileAction {

    private IWorkbenchPage page;

    public OpenResourceAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        super(page, selectionProvider);
        this.page = page;
        this.setText("Open");
    }
    
    protected void openFiles(List<IFile> filesSelected) {
        for (IFile f : filesSelected) {
            try {
                IDE.openEditor(page, f);
            } catch (PartInitException e) {
            }
        }
    }

	
}
