/*
 * Copyright 2006-2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Apr 14, 2006
 */

package org.zamia.plugin.views.sim;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.rtl.sim.ISimulator;
import org.zamia.util.PathName;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class TraceContentProvider implements ITreeContentProvider {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	//	private ISimulator fSimulator;

	public TraceContentProvider() {
	}

	public Object[] getChildren(Object aParentElement) {

		if (!(aParentElement instanceof TraceDialogItem)) {
			return null;
		}

		TraceDialogItem parent = (TraceDialogItem) aParentElement;

		try {
			return getChildrenInternal(parent.fName);
		} catch (ZamiaException e) {
			el.logException(e);
		}
		return null;
	}

	public Object getParent(Object aElement) {
		return null;
	}

	public boolean hasChildren(Object aElement) {
		return (aElement instanceof TraceDialogItem) && ((TraceDialogItem) aElement).fIsModule;
	}

	public Object[] getElements(Object aInputeElement) {

		if (aInputeElement instanceof ISimulator) {

			//			fSimulator = (ISimulator) aInputeElement;
			try {

				PathName rootPath = new PathName("/");

				return getChildrenInternal(rootPath);

			} catch (ZamiaException e) {
				el.logException(e);
			}
		}

		return null;
	}

	private Object[] getChildrenInternal(PathName aRootPath) throws ZamiaException {
		//		int nModules = fSimulator.getNumAvailableModules(aRootPath);
		//
		//		int nSignals = fSimulator.getNumAvailableSignals(aRootPath);
		//
		//		ArrayList<TraceDialogItem> res = new ArrayList<TraceDialogItem>(nModules + nSignals);
		//
		//		for (int i = 0; i < nModules; i++) {
		//			res.add(new TraceDialogItem(fSimulator.getAvailableModule(aRootPath, i), true));
		//		}
		//
		//		for (int i = 0; i < nSignals; i++) {
		//			res.add(new TraceDialogItem(fSimulator.getAvailableSignal(aRootPath, i), false));
		//		}
		//
		//		return res.toArray();

		// FIXME

		return null;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
