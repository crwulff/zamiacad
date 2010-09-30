/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * 
 */
package org.zamia.plugin.editors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.editors.completion.VHDLCompletionProcessor;
import org.zamia.plugin.preferences.PreferenceConstants;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaSourceViewerConfiguration extends SourceViewerConfiguration {

	final static String COMMENT_CONTENT_TYPE = "__comment_partition_content_type";

	//private final ISharedTextColors fColors;
	private ITextEditor fEditor;
	private ITokenScanner scanner;
	private ZamiaReconcilingStrategy strategy;


	private String[] defaultPrefixes;

	
	public ZamiaSourceViewerConfiguration(ITokenScanner scanner_, ZamiaReconcilingStrategy strategy_, String[] defaultPrefixes_, ITextEditor editor_) {
		fEditor= editor_;
		//fColors= colors;
		scanner = scanner_;
		strategy = strategy_;
		defaultPrefixes = defaultPrefixes_;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
	    IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();
		
		RGB colorComment = PreferenceConverter.getColor(store, PreferenceConstants.P_COMMENT);
		
		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(ColorManager.getInstance().getColor(colorComment)));
		reconciler.setDamager(ndr, COMMENT_CONTENT_TYPE);
		reconciler.setRepairer(ndr, COMMENT_CONTENT_TYPE);

		return reconciler;
	}

	private IInformationControlCreator getInformationPresenterControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
//				return new DefaultInformationControl(parent, true);
				return new DefaultInformationControl(parent);
			}
		};
	}

	
	@Override
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		InformationPresenter presenter= new InformationPresenter(getInformationPresenterControlCreator(sourceViewer));
		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		// Register information provider
		IInformationProvider provider= new VHDLInformationProvider(getEditor());
		String[] contentTypes= getConfiguredContentTypes(sourceViewer);
		for (int i= 0; i < contentTypes.length; i++)
			presenter.setInformationProvider(provider, contentTypes[i]);
		
		// sizes: see org.eclipse.jface.text.TextViewer.TEXT_HOVER_*_CHARS
		presenter.setSizeConstraints(100, 12, true, true);
		return presenter;
	}

	protected ITextEditor getEditor() {
		return fEditor;
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return defaultPrefixes;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		reconciler.setDelay(500);
        reconciler.setIsIncrementalReconciler(false);

		return reconciler;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new VHDLCompletionProcessor(getEditor()), IDocument.DEFAULT_CONTENT_TYPE);
	
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setContextInformationPopupBackground(ColorManager.getInstance().getColor(new RGB(150, 150, 0)));

		return assistant;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}
}
