/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 22, 2008
 */
package org.zamia.plugin.editors;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * Used for accessing std libs source code
 * 
 * @author Guenter Bartsch
 *
 */

public class ReaderTextDocumentProvider implements
		org.eclipse.ui.texteditor.IDocumentProvider,
		org.eclipse.ui.texteditor.IDocumentProviderExtension,
		org.eclipse.ui.texteditor.IDocumentProviderExtension2,
		org.eclipse.ui.texteditor.IDocumentProviderExtension3,
		org.eclipse.ui.texteditor.IDocumentProviderExtension5,
		org.eclipse.ui.editors.text.IStorageDocumentProvider,
		org.eclipse.ui.texteditor.IDocumentProviderExtension4 {
	{

	}

	public void aboutToChange(Object arg0) {
		// TODO Auto-generated method stub

	}

	public void addElementStateListener(IElementStateListener arg0) {
		// TODO Auto-generated method stub

	}

	public boolean canSaveDocument(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void changed(Object arg0) {
		// TODO Auto-generated method stub

	}

	public void connect(Object arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void disconnect(Object arg0) {
		// TODO Auto-generated method stub

	}

	public IAnnotationModel getAnnotationModel(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public IDocument getDocument(Object arg0) {

		ExternalReaderEditorInput erei = (ExternalReaderEditorInput) arg0;
		
		
		StringBuilder buf = new StringBuilder();

		int c;
		try {
			Reader r = erei.getReader();
			while ( (c = r.read())>0) {
				buf.append((char) c);
			}
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Document doc = new Document(buf.toString());
		
		return doc;
	}

	public long getModificationStamp(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getSynchronizationStamp(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isDeleted(Object arg0) {
		return false;
	}

	public boolean mustSaveDocument(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeElementStateListener(IElementStateListener arg0) {
		// TODO Auto-generated method stub

	}

	public void resetDocument(Object arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void saveDocument(IProgressMonitor arg0, Object arg1,
			IDocument arg2, boolean arg3) throws CoreException {
		// TODO Auto-generated method stub

	}

	public IStatus getStatus(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isModifiable(Object arg0) {
		return false;
	}

	public boolean isReadOnly(Object arg0) {
		return true;
	}

	public boolean isStateValidated(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCanSaveDocument(Object arg0) {
		// TODO Auto-generated method stub

	}

	public void synchronize(Object arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void updateStateCache(Object arg0) throws CoreException {
		// TODO Auto-generated method stub

	}

	public void validateState(Object arg0, Object arg1) throws CoreException {
		// TODO Auto-generated method stub

	}

	public IProgressMonitor getProgressMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProgressMonitor(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	public boolean isSynchronized(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNotSynchronizedException(Object arg0, CoreException arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getDefaultEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEncoding(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setEncoding(Object arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	public IContentType getContentType(Object arg0) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
