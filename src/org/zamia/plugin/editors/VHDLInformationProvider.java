/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 18, 2008
 */
package org.zamia.plugin.editors;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.widgets.Shell;
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
import org.zamia.analysis.ast.ASTDeclarationSearch;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.ZamiaProjectMap;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class VHDLInformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private IEditorPart fEditor;

	private IProject fPrj;

	private SourceFile fSF;

	private int fLine;

	private int fCol;

	private ZamiaProject fZPrj;

	public VHDLInformationProvider(IEditorPart aEditor) {
		fEditor = aEditor;
	}

	public String getInformation(ITextViewer textViewer, IRegion subject) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRegion getSubject(ITextViewer aTextViewer, int aOffset) {

		IDocument doc = aTextViewer.getDocument();

		int start = aOffset;
		int end = aOffset;
		try {
			while (start >= 0) {
				char c = doc.getChar(start);
				if (!Character.isJavaIdentifierPart(c) && c != '.')
					break;
				start--;
			}
			while (true) {
				char c = doc.getChar(end);
				if (!Character.isJavaIdentifierPart(c) && c != '.')
					break;
				end++;
			}

			IEditorInput editorInput = fEditor.getEditorInput();
			fPrj = null;
			fSF = null;

			IFile file = ResourceUtil.getFile(editorInput);
			if (file != null) {
				fPrj = file.getProject();
				fSF = ZamiaPlugin.getSourceFile(file);
			}

			if (fPrj != null) {
				fLine = doc.getLineOfOffset(aOffset);
				fCol = aOffset - doc.getLineOffset(fLine);

				fLine++;
				fCol++;

				logger.debug("Line: " + fLine + ", col: " + fCol);

				fZPrj = ZamiaProjectMap.getZamiaProject(fPrj);

			} else {
				fLine = 0;
				fCol = 0;
				logger.error("Failed to find project for '%s'", editorInput);
			}

		} catch (BadLocationException e) {
			// probably hit EOF
		}

		return new Region(start, end - start);
	}

	public Object getInformation2(ITextViewer textViewer, IRegion subject) {

		try {
			SourceLocation location = new SourceLocation(fSF, fLine, fCol);

			ASTObject nearest = SourceLocation2AST.findNearestASTObject(location, true, fZPrj);

			if (nearest != null) {
				ASTObject declaration = ASTDeclarationSearch.search(nearest, fZPrj);
				if (declaration != null) {
					return declaration.toString();
				}
			}
		} catch (ZamiaException e) {
			el.logException(e);
		} catch (IOException e) {
			el.logException(e);
		}

		return null;
	}

	public IInformationControlCreator getInformationPresenterControlCreator() {

		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				//				return new DefaultInformationControl(parent, "Result of declaration search.");
				return new DefaultInformationControl(parent);
			}
		};
	}
}
