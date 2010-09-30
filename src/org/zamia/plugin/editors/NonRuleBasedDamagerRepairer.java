/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class NonRuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

	protected IDocument fDocument;

	protected TextAttribute fDefaultTextAttribute;

	public NonRuleBasedDamagerRepairer(TextAttribute defaultTextAttribute) {
		fDefaultTextAttribute = defaultTextAttribute;
	}

	public void setDocument(IDocument document) {
		fDocument = document;
	}

	protected int endOfLineOf(int offset) throws BadLocationException {

		IRegion info = fDocument.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength())
			return info.getOffset() + info.getLength();

		int line = fDocument.getLineOfOffset(offset);
		try {
			info = fDocument.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException x) {
			return fDocument.getLength();
		}
	}

	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged) {
			try {

				IRegion info = fDocument.getLineInformationOfOffset(event.getOffset());
				int start = Math.max(partition.getOffset(), info.getOffset());

				int end = event.getOffset() + (event.getText() == null ? event.getLength() : event.getText().length());

				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					// optimize the case of the same line
					end = info.getOffset() + info.getLength();
				} else
					end = endOfLineOf(end);

				end = Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);

			} catch (BadLocationException x) {
			}
		}

		return partition;
	}

	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
	}

	protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null)
			presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr.getStyle()));
	}
}