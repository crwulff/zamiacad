/*
 * Copyright 2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.editors.completion;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.analysis.SourceLocation2AST;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.editors.VHDLScanner;
import org.zamia.plugin.editors.ZamiaEditor;
import org.zamia.plugin.editors.ZamiaReconcilingStrategy;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class VHDLCompletionProcessor implements IContentAssistProcessor {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5
	 * characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;

		/*
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		/*
		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset = offset;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected IContextInformationValidator fValidator = new Validator();

	private IDocument fDocument;

	private Image fTemplateImage;

	private IEditorPart fEditor;

	public VHDLCompletionProcessor(IEditorPart editor) {
		fTemplateImage = ZamiaPlugin.getImage("/share/images/library.gif");
		fEditor = editor;
	}

	public ITextSelection selectPrefix(IDocument doc, ITextSelection sel) {
		int caretPos = sel.getOffset();
		int startPos, endPos;
		try {
			int pos = caretPos - 1;
			char c;
			while (pos >= 0) {
				c = doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c) && c != '.')
					break;
				pos--;
			}
			startPos = pos + 1;
			//                pos = caretPos;
			//                int length = doc.getLength();
			//                while (pos < length) {
			//                        c = doc.getChar(pos);
			//                        if (!Character.isJavaIdentifierPart(c) && c != '.')
			//                                break;
			//                        pos++;
			//                }
			//                endPos = pos;
			endPos = caretPos;
			return new TextSelection(doc, startPos, endPos - startPos);
		} catch (BadLocationException x) {
			// Do nothing, except returning
		}
		return sel;
	}

	public String getPrefix(ITextViewer viewer) throws BadLocationException {

		fDocument = viewer.getDocument();

		ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		if (selection.getLength() <= 0) {
			selection = selectPrefix(fDocument, selection);
		}

		if (selection.isEmpty()) {
			return null;
		}

		String text = selection.getText();
		return text;
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {

		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

		String prefix = null;
		try {
			prefix = getPrefix(viewer).toLowerCase();
			logger.info("Completion: prefix is '%s'", prefix);
		} catch (BadLocationException e) {
			el.logException(e);
		}

		Region region;
		if (prefix != null) {
			region = new Region(documentOffset - prefix.length(), prefix.length());
		} else {
			region = new Region(documentOffset, 0);
		}

		String contextString = VHDLContext.CONTEXT_TYPE;
		TemplateContextType contextType = ZamiaPlugin.getDefault().getContextTypeRegistry().getContextType(contextString);

		if (fEditor instanceof ZamiaEditor) {
			ZamiaEditor editor = (ZamiaEditor) fEditor;

			ZamiaReconcilingStrategy strategy = editor.getReconcilingStrategy();
			ZamiaProject zprj = strategy.getZPrj();

			ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
			int caretPos = selection.getOffset();

			IEditorInput editorInput = editor.getEditorInput();
			SourceFile sf = null;

			IFile file = ResourceUtil.getFile(editorInput);
			if (file != null) {
				sf = ZamiaPlugin.getSourceFile(file);
			}

			try {
				int line = editor.getDocument().getLineOfOffset(caretPos);
				int col = caretPos - editor.getDocument().getLineOffset(line);

				line++;
				col++;

				logger.debug("VHDLCompletionProcessor: Line: " + line + ", col: " + col);

				ASTObject io = SourceLocation2AST.findNearestASTObject(new SourceLocation(sf, line, col), false, zprj);

				logger.debug("VHDLCompletionProcessor: nearest io is '%s'", io);

				if (io != null) {

					HashSetArray<String> identifiers = new HashSetArray<String>();

					io.collectIdentifiers(identifiers, zprj);
					int n = identifiers.size();
					logger.debug("VHDLCompletionProcessor: collected %d identifiers", n);
					for (int i = 0; i < n; i++) {

						String id = identifiers.get(i).toLowerCase();

						if (prefix != null) {
							if (id.startsWith(prefix)) {
								result.add(new CompletionProposal(id, region.getOffset(), region.getLength(), id.length()));
							}
						} else {
							result.add(new CompletionProposal(id, documentOffset, 0, id.length()));
						}
					}
				}

			} catch (BadLocationException e) {
				el.logException(e);
			} catch (IOException e) {
				el.logException(e);
			} catch (ZamiaException e) {
				el.logException(e);
			}
		}

		/*
		 * template proposals
		 */

		// compute indent ( = current column)
		String indentStr = "";

		try {
			int line = fDocument.getLineOfOffset(documentOffset);
			int lineOffset = fDocument.getLineOffset(line);

			StringBuilder buf = new StringBuilder();
			for (int i = lineOffset; i < documentOffset; i++) {
				char c = fDocument.getChar(i);
				if (Character.isWhitespace(c)) {
					buf.append(c);
				} else {
					break;
				}
			}
			indentStr = buf.toString();

			//			indent = documentOffset - lineOffset - prefix.length();
			//			if (indent <0) {
			//				indent = 0;
			//			}

		} catch (BadLocationException e) {
			el.logException(e);
		}

		DocumentTemplateContext documentTemplateContext = new DocumentTemplateContext(contextType, fDocument, region.getOffset(), region.getLength());

		Template[] templates = ZamiaPlugin.getDefault().getTemplateStore().getTemplates(contextType.getId());
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];

			if (prefix == null || template.getPattern().toLowerCase().startsWith(prefix) || template.getName().toLowerCase().startsWith(prefix)) {

				StringBuilder buf = new StringBuilder();

				String pattern = template.getPattern();
				int m = pattern.length();
				for (int j = 0; j < m; j++) {
					char c = pattern.charAt(j);
					buf.append(c);
					if (c == 10) {
						buf.append(indentStr);
					}
				}

				Template indentedTemplate = new Template(template.getName(), template.getDescription(), template.getContextTypeId(), buf.toString(), template.isAutoInsertable());

				result.add(new TemplateProposal(indentedTemplate, documentTemplateContext, region, fTemplateImage));
			}
		}

		for (int i = 0; i < VHDLScanner.fgKeywords.length; i++) {

			String kw = VHDLScanner.fgKeywords[i];

			if (prefix != null) {
				if (kw.toLowerCase().startsWith(prefix)) {
					result.add(new CompletionProposal(kw, region.getOffset(), region.getLength(), kw.length()));
				}
			} else {
				result.add(new CompletionProposal(kw, documentOffset, 0, kw.length()));
			}
		}

		ICompletionProposal[] res = new ICompletionProposal[result.size()];
		for (int i = 0; i < result.size(); i++) {
			res[i] = result.get(i);
		}

		return res;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result = new IContextInformation[5];
		for (int i = 0; i < result.length; i++)
			result[i] = new ContextInformation(MessageFormat
					.format("CompletionProcessor.ContextInfo.display.pattern", new Object[] { new Integer(i), new Integer(documentOffset) }), MessageFormat.format(
					"CompletionProcessor.ContextInfo.value.pattern", new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5) }));
		return result;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		//return new char[] { '.', '(' };
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		//return new char[] { '#' };
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	public String getErrorMessage() {
		return null;
	}
}
