/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jun 22, 2008
 */
package org.zamia.plugin.editors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.zamia.ZamiaLogger;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final ISearchQuery fQuery;

	// private final Map fElementsToParticipants;

	public ZamiaSearchResult(ISearchQuery aQuery) {
		fQuery = aQuery;
		// fElementsToParticipants= new HashMap();
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getLabel() {
		return fQuery.getLabel();
	}

	public String getTooltip() {
		return getLabel();
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {

		IEditorInput editorInput = editor.getEditorInput();

		logger.debug("ZamiaSearchResult: Editor input: " + editorInput);

		return computeContainedMatches(editorInput);
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		Object element = match.getElement();
		if (element instanceof IFile) {
			return element.equals(editor.getEditorInput().getAdapter(IFile.class));
		}
		return false;
	}

	public IFile getFile(Object element) {
		// if (element instanceof IJavaElement) {
		// IJavaElement javaElement = (IJavaElement) element;
		// ICompilationUnit cu = (ICompilationUnit)
		// javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
		// if (cu != null) {
		// return (IFile) cu.getResource();
		// } else {
		// IClassFile cf = (IClassFile)
		// javaElement.getAncestor(IJavaElement.CLASS_FILE);
		// if (cf != null)
		// return (IFile) cf.getResource();
		// }
		// return null;
		// }
		
		logger.debug("ZamiaSearchResult.getFile(%s)", element);
		
		if (element instanceof IFile)
			return (IFile) element;
		return null;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return computeContainedMatches(file);
	}

	@SuppressWarnings("unchecked")
	private Match[] computeContainedMatches(IAdaptable adaptable) {
		Set matches = new HashSet();

		IFile file = (IFile) adaptable.getAdapter(IFile.class);
		if (file != null) {
			Match[] m = getMatches(file);
			for (int i = 0; i < m.length; i++) {
				matches.add(m[i]);
			}
		}
		return (Match[]) matches.toArray(new Match[matches.size()]);
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

}
