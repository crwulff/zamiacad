/*
 * Copyright 2008-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public abstract class StaticAnalysisAction implements IEditorActionDelegate {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	static ZamiaEditor fEditor;

	// results of processSelection:

	protected IProject fPrj;

	protected SourceLocation fLocation;

	protected ZamiaProject fZPrj;

	protected ToplevelPath fPath;

	public void setActiveEditor(IAction aAction, IEditorPart aTargetEditor) {
		if (aTargetEditor == null) {
			return;
		}
		if (!(aTargetEditor instanceof ZamiaEditor)) {
			return;
		}
		fEditor = (ZamiaEditor) aTargetEditor;
	}

	public void selectionChanged(IAction aAction, ISelection aSelection) {
		aAction.setEnabled(true);
	}

	public ToplevelPath getPath() {
		return fPath;
	}

	public void processSelection() throws BadLocationException {
		ITextSelection selection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		int caretPos = selection.getOffset();

		IEditorInput editorInput = fEditor.getEditorInput();
		fPrj = null;
		SourceFile sf = null;
		fPath = fEditor.getPath();

		IFile file = ResourceUtil.getFile(editorInput);
		if (file != null) {
			fPrj = file.getProject();
			sf = ZamiaPlugin.getSourceFile(file);
		} else {
			if (editorInput instanceof ExternalReaderEditorInput) {
				ExternalReaderEditorInput erei = (ExternalReaderEditorInput) editorInput;
				
				fPrj = erei.getProject();
				sf = new SourceFile(erei.getURI());
				
			}
		}

		int line = 0;
		int col = 0;

		if (fPrj != null) {
			line = fEditor.getDocument().getLineOfOffset(caretPos);
			col = caretPos - fEditor.getDocument().getLineOffset(line);

			line++;
			col++;

			if (fEditor.isAnnotated()) {
				line /=2;
			}
			
			logger.debug("Line: " + line + ", col: " + col);

			fZPrj = ZamiaProjectMap.getZamiaProject(fPrj);

		} else {
			logger.error("Failed to find project for '%s'", editorInput);
		}

		if (sf != null) {
			fLocation = new SourceLocation(sf, line, col);
		}
	}

	public ZamiaProject getZamiaProject() {
		return fZPrj;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

}
