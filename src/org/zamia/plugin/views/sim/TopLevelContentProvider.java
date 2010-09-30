/*
 * Copyright 2006-2008,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.views.sim;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.zamia.BuildPath;
import org.zamia.Toplevel;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaProjectMap;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class TopLevelContentProvider implements ITreeContentProvider {

	private IProject fProject;;

	public TopLevelContentProvider() {
	}

	public TopLevelContentProvider(IProject aProject) {
		fProject = aProject;
	}

	public Object[] getChildren(Object aParentElement) {
		return null;
	}

	public Object getParent(Object aElement) {
		return null;
	}

	public boolean hasChildren(Object aElement) {
		return false;
	}

	public Object[] getElements(Object aInputeElement) {

		ZamiaProject zPrj = ZamiaProjectMap.getZamiaProject(fProject);
		if (zPrj == null) {
			return null;
		}

		BuildPath bp = zPrj.getBuildPath();
		if (bp==null) {
			return null;
		}
		
		ArrayList<Toplevel> toplevels = new ArrayList<Toplevel>();
		int n = bp.getNumToplevels();
		for (int i = 0; i<n; i++) {
			Toplevel tl = bp.getToplevel(i);
			
			toplevels.add(tl);
		}
		
		return toplevels.toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer aViewer, Object aOldInput, Object aNewInput) {
	}

}
