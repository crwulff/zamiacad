/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Jul 2, 2007
 */

package org.zamia.plugin.views.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			Object adapted = adaptable.getAdapter(IFile.class);
			if (adapted instanceof IFile) {
				IFile resource = (IFile) adapted;
				if (resource.getName().endsWith(".du")) {
					return false;
				}
			} else {

				adapted = adaptable.getAdapter(IFolder.class);

				if (adapted instanceof IFolder) {
					IFolder folder = (IFolder) adapted;
					if (folder.getName().equals("ZDB"))
						return false;
				}
			}

		}
		return true;
	}

}