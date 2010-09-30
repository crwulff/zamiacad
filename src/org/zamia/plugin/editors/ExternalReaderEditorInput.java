/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 21, 2008
 */
package org.zamia.plugin.editors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.zamia.ZamiaProject;

/**
 * @author guenter bartsch
 */

/**
 * EditorInput for external files. Copied from
 * org.eclipse.ui.internal.editors.text.JavaFileEditorInput
 */
public class ExternalReaderEditorInput implements IEditorInput, ILocationProvider {
	// copies of this class exist in:
	// org.eclipse.wst.xml.ui.internal.hyperlink
	// org.eclipse.wst.html.ui.internal.hyperlink
	// org.eclipse.jst.jsp.ui.internal.hyperlink

	/**
	 * The workbench adapter which simply provides the label.
	 * 
	 * @see Eclipse 3.1
	 */
	static private class WorkbenchAdapter implements IWorkbenchAdapter {
		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object o) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		public String getLabel(Object o) {
			return ((ExternalReaderEditorInput) o).getName();
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		public Object getParent(Object o) {
			return null;
		}
	}

	private String name, uri;

	private WorkbenchAdapter fWorkbenchAdapter = new WorkbenchAdapter();

	private IProject prj;

	public ExternalReaderEditorInput(Reader reader_, String name_, String uri_, IProject prj_) {
		super();
		name = name_;
		uri = uri_;
		prj = prj_;
		fWorkbenchAdapter = new WorkbenchAdapter();
	}

	public IProject getProject() {
		return prj;
	}
	
	/*
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return name;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (ILocationProvider.class.equals(adapter))
			return this;
		if (IWorkbenchAdapter.class.equals(adapter))
			return fWorkbenchAdapter;
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/*
	 * @see org.eclipse.ui.editors.text.ILocationProvider#getPath(java.lang.Object)
	 */
	public IPath getPath(Object element) {
		if (element instanceof ExternalReaderEditorInput) {
//			ExternalReaderEditorInput input = (ExternalReaderEditorInput) element;
			return Path.fromOSString(uri);
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.IPathEditorInput#getPath()
	 * @since 3.1
	 */
	public IPath getPath() {
		return Path.fromOSString(uri);
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof ExternalReaderEditorInput) {
			ExternalReaderEditorInput input = (ExternalReaderEditorInput) o;
			return uri.equals(input.uri);
		}

		if (o instanceof IPathEditorInput) {
			IPathEditorInput input = (IPathEditorInput) o;
			return getPath().equals(input.getPath());
		}

		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return uri.hashCode();
	}

	public Reader getReader() {
		return new BufferedReader(new InputStreamReader(ZamiaProject.class
				.getResourceAsStream(uri)));
	}

	public String getURI() {
		return uri;
	}
	
//	public File getFile() {
//		return fFile;
//	}
}
