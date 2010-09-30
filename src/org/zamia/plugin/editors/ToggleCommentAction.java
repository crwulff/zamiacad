/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import java.util.HashMap;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * @author guenter bartsch
 */
public class ToggleCommentAction extends Action implements IUpdate, IEditorActionDelegate {
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	/** The document partitioning */
	private String fDocumentPartitioning = IDocumentExtension3.DEFAULT_PARTITIONING;
;
	/** The comment prefixes */
	private HashMap<String,String[]> fPrefixesMap;

	public ToggleCommentAction() {
		super();
	}

	/**
	 * Is the given selection single-line commented?
	 * 
	 * @param selection
	 *            Selection to check
	 * @return <code>true</code> iff all selected lines are commented
	 */
	private boolean isSelectionCommented(ISelection selection) {
		if (!(selection instanceof ITextSelection))
			return false;

		ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0)
			return false;

		IDocument document = getTextEditor().getDocumentProvider().getDocument(
				getTextEditor().getEditorInput());

		try {

			IRegion block = getTextBlockFromSelection(textSelection, document);
			ITypedRegion[] regions = TextUtilities.computePartitioning(
					document, fDocumentPartitioning, block.getOffset(), block
							.getLength(), false);

			int lineCount = 0;
			int[] lines = new int[regions.length * 2]; // [startline,
			// endline,
			// startline,
			// endline, ...]
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				// start line of region
				lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
				// end line of region
				int length = regions[i].getLength();
				int offset = regions[i].getOffset() + length;
				if (length > 0)
					offset--;
				lines[j + 1] = (lines[j] == -1 ? -1 : document
						.getLineOfOffset(offset));
				lineCount += lines[j + 1] - lines[j] + 1;
			}

			// Perform the check
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				String[] prefixes = (String[]) fPrefixesMap.get(regions[i]
						.getType());
				if (prefixes != null && prefixes.length > 0 && lines[j] >= 0
						&& lines[j + 1] >= 0)
					if (!isBlockCommented(lines[j], lines[j + 1], prefixes,
							document))
						return false;
			}

			return true;

		} catch (BadLocationException x) {
		}

		return false;
	}

	/**
	 * Creates a region describing the text block (something that starts at the
	 * beginning of a line) completely containing the current selection.
	 * 
	 * @param selection
	 *            The selection to use
	 * @param document
	 *            The document
	 * @return the region describing the text block comprising the given
	 *         selection
	 */
	private IRegion getTextBlockFromSelection(ITextSelection selection,
			IDocument document) {

		try {
			IRegion line = document.getLineInformationOfOffset(selection
					.getOffset());
			int length = selection.getLength() == 0
					? line.getLength()
					: selection.getLength()
							+ (selection.getOffset() - line.getOffset());
			return new Region(line.getOffset(), length);

		} catch (BadLocationException x) {
		}

		return null;
	}

	private int getFirstCompleteLineOfRegion(IRegion region, IDocument document) {

		try {

			int startLine = document.getLineOfOffset(region.getOffset());

			int offset = document.getLineOffset(startLine);
			if (offset >= region.getOffset())
				return startLine;

			offset = document.getLineOffset(startLine + 1);
			return (offset > region.getOffset() + region.getLength()
					? -1
					: startLine + 1);

		} catch (BadLocationException x) {
		}

		return -1;
	}

	/**
	 * Determines whether each line is prefixed by one of the prefixes.
	 * 
	 * @param startLine
	 *            Start line in document
	 * @param endLine
	 *            End line in document
	 * @param prefixes
	 *            Possible comment prefixes
	 * @param document
	 *            The document
	 * @return <code>true</code> iff each line from <code>startLine</code>
	 *         to and including <code>endLine</code> is prepended by one of
	 *         the <code>prefixes</code>, ignoring whitespace at the begin of
	 *         line
	 */
	private boolean isBlockCommented(int startLine, int endLine,
			String[] prefixes, IDocument document) {

		try {

			// check for occurrences of prefixes in the given lines
			for (int i = startLine; i <= endLine; i++) {

				IRegion line = document.getLineInformation(i);
				String text = document.get(line.getOffset(), line.getLength());

				int[] found = TextUtilities.indexOf(prefixes, text, 0);

				if (found[0] == -1)
					// found a line which is not commented
					return false;

				String s = document.get(line.getOffset(), found[0]);
				s = s.trim();
				if (s.length() != 0)
					// found a line which is not commented
					return false;

			}

			return true;

		} catch (BadLocationException x) {
		}

		return false;
	}


	public void configure(ISourceViewer sourceViewer,
			SourceViewerConfiguration configuration) {
		fPrefixesMap = null;

		String[] types = configuration.getConfiguredContentTypes(sourceViewer);
		HashMap<String,String[]> prefixesMap = new HashMap<String,String[]>(types.length);
		for (int i = 0; i < types.length; i++) {
			String type = types[i];
			String[] prefixes = configuration.getDefaultPrefixes(sourceViewer,
					type);
			if (prefixes != null && prefixes.length > 0) {
				int emptyPrefixes = 0;
				for (int j = 0; j < prefixes.length; j++)
					if (prefixes[j].length() == 0)
						emptyPrefixes++;

				if (emptyPrefixes > 0) {
					String[] nonemptyPrefixes = new String[prefixes.length
							- emptyPrefixes];
					for (int j = 0, k = 0; j < prefixes.length; j++) {
						String prefix = prefixes[j];
						if (prefix.length() != 0) {
							nonemptyPrefixes[k] = prefix;
							k++;
						}
					}
					prefixes = nonemptyPrefixes;
				}

				prefixesMap.put(type, prefixes);
			}
		}
		fDocumentPartitioning = configuration
				.getConfiguredDocumentPartitioning(sourceViewer);
		fPrefixesMap = prefixesMap;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {

		if (fOperationTarget == null || fDocumentPartitioning == null
				|| fPrefixesMap == null)
			return;

		ITextEditor editor = getTextEditor();
		if (editor == null)
			return;

		if (!validateEditorInputState())
			return;

		final int operationCode;
		if (isSelectionCommented(editor.getSelectionProvider().getSelection()))
			operationCode = ITextOperationTarget.STRIP_PREFIX;
		else
			operationCode = ITextOperationTarget.PREFIX;

		Shell shell = editor.getSite().getShell();
		if (!fOperationTarget.canDoOperation(operationCode)) {
			return;
		}

		Display display = null;
		if (shell != null && !shell.isDisposed())
			display = shell.getDisplay();

		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				fOperationTarget.doOperation(operationCode);
			}
		});

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		if (! (targetEditor instanceof ITextEditor)) {
			fTextEditor = null;
			return;
		}
		
		fTextEditor = (ITextEditor) targetEditor;
		fOperationTarget = null;
		update();
		
		if (fTextEditor instanceof ZamiaEditor) {
			ZamiaEditor editor = (ZamiaEditor) fTextEditor;
			configure(editor.getMySourceViewer(), editor.getSourceViewerCfg());
		}
	}

	/** The action's editor */
	private ITextEditor fTextEditor;

	/**
	 * Returns the action's text editor.
	 * 
	 * @return the action's text editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	public void update() {
		setEnabled(getTextEditor() != null);

		if (!canModifyEditor()) {
			setEnabled(false);
			return;
		}

		ITextEditor editor = getTextEditor();
		if (fOperationTarget == null && editor != null)
			fOperationTarget = (ITextOperationTarget) editor
					.getAdapter(ITextOperationTarget.class);

		boolean isEnabled = (fOperationTarget != null
				&& fOperationTarget.canDoOperation(ITextOperationTarget.PREFIX) && fOperationTarget
				.canDoOperation(ITextOperationTarget.STRIP_PREFIX));
		setEnabled(isEnabled);
	}

	/**
	 * Checks the editor's modifiable state. Returns <code>true</code> if the
	 * editor can be modified, taking in account the possible editor extensions.
	 * 
	 * <p>
	 * If the editor implements <code>ITextEditorExtension2</code>, this
	 * method returns {@link ITextEditorExtension2#isEditorInputModifiable()};<br>
	 * else if the editor implements <code>ITextEditorExtension2</code>, it
	 * returns {@link ITextEditorExtension#isEditorInputReadOnly()};<br>
	 * else, {@link ITextEditor#isEditable()} is returned, or <code>false</code>
	 * if the editor is <code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * There is only a difference to {@link #validateEditorInputState()} if the
	 * editor implements <code>ITextEditorExtension2</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if a modifying action should be enabled,
	 *         <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean canModifyEditor() {
		ITextEditor editor = getTextEditor();
		if (editor instanceof ITextEditorExtension2)
			return ((ITextEditorExtension2) editor).isEditorInputModifiable();
		else if (editor instanceof ITextEditorExtension)
			return !((ITextEditorExtension) editor).isEditorInputReadOnly();
		else if (editor != null)
			return editor.isEditable();
		else
			return false;
	}

	/**
	 * Checks and validates the editor's modifiable state. Returns
	 * <code>true</code> if an action can proceed modifying the editor's
	 * input, <code>false</code> if it should not.
	 * 
	 * <p>
	 * If the editor implements <code>ITextEditorExtension2</code>, this
	 * method returns {@link ITextEditorExtension2#validateEditorInputState()};<br>
	 * else if the editor implements <code>ITextEditorExtension</code>, it
	 * returns {@link ITextEditorExtension#isEditorInputReadOnly()};<br>
	 * else, {@link ITextEditor#isEditable()} is returned, or <code>false</code>
	 * if the editor is <code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * There is only a difference to {@link #canModifyEditor()} if the editor
	 * implements <code>ITextEditorExtension2</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if a modifying action can proceed to modify
	 *         the underlying document, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean validateEditorInputState() {
		ITextEditor editor = getTextEditor();
		if (editor instanceof ITextEditorExtension2)
			return ((ITextEditorExtension2) editor).validateEditorInputState();
		else if (editor instanceof ITextEditorExtension)
			return !((ITextEditorExtension) editor).isEditorInputReadOnly();
		else if (editor != null)
			return editor.isEditable();
		else
			return false;
	}

}
