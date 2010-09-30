/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * 
 */
package org.zamia.plugin.editors.buildpath;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.ITextEditor;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.editors.ColorManager;
import org.zamia.plugin.editors.NonRuleBasedDamagerRepairer;
import org.zamia.plugin.preferences.PreferenceConstants;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class BuildPathSourceViewerConfiguration extends SourceViewerConfiguration {

	final static String COMMENT_CONTENT_TYPE = "__comment_partition_content_type";

	//private final ISharedTextColors fColors;
	private ITextEditor fEditor;
	private ITokenScanner fScanner;
	private String[] fDefaultPrefixes;
	
	public BuildPathSourceViewerConfiguration(ITokenScanner aScanner, String[] aDefaultPrefixes, ITextEditor aEditor) {
		fEditor= aEditor;
		fScanner = aScanner;
		fDefaultPrefixes = aDefaultPrefixes;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer aSourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
	    IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();
		
		RGB colorComment = PreferenceConverter.getColor(store, PreferenceConstants.P_COMMENT);
		
		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(ColorManager.getInstance().getColor(colorComment)));
		reconciler.setDamager(ndr, COMMENT_CONTENT_TYPE);
		reconciler.setRepairer(ndr, COMMENT_CONTENT_TYPE);

		return reconciler;
	}

	protected ITextEditor getEditor() {
		return fEditor;
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer aSourceViewer, String aContentType) {
		return fDefaultPrefixes;
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}

}
