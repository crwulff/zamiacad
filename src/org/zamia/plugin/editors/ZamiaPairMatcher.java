/*
 * Copyright 2006 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 25, 2006
 * 
 * Based on IBM's JavaPairMatcher
 */

package org.zamia.plugin.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

/**
 * @author guenter bartsch
 */
public class ZamiaPairMatcher implements ICharacterPairMatcher {

	protected char[] fPairs;
	protected IDocument fDocument;
	protected int fOffset;

	protected int fStartPos;
	protected int fEndPos;
	protected int fAnchor;

	public ZamiaPairMatcher(char[] pairs) {
		fPairs = pairs;
	}

	public IRegion match(IDocument document, int offset) {

		fOffset = offset;

		if (fOffset < 0)
			return null;

		fDocument = document;

		if (fDocument != null && matchPairsAt() && fStartPos != fEndPos)
			return new Region(fStartPos, fEndPos - fStartPos + 1);

		return null;
	}

	public int getAnchor() {
		return fAnchor;
	}

	public void dispose() {
		clear();
		fDocument = null;
	}

	public void clear() {
	}

	protected boolean matchPairsAt() {

		int i;
		int pairIndex1 = fPairs.length;
		int pairIndex2 = fPairs.length;

		fStartPos = -1;
		fEndPos = -1;

		// get the char preceding the start position
		try {

			char prevChar = fDocument.getChar(Math.max(fOffset - 1, 0));
			// search for opening peer character next to the activation point
			for (i = 0; i < fPairs.length; i = i + 2) {
				if (prevChar == fPairs[i]) {
					fStartPos = fOffset - 1;
					pairIndex1 = i;
				}
			}

			// search for closing peer character next to the activation point
			for (i = 1; i < fPairs.length; i = i + 2) {
				if (prevChar == fPairs[i]) {
					fEndPos = fOffset - 1;
					pairIndex2 = i;
				}
			}

			if (fEndPos > -1) {
				fAnchor = RIGHT;
				fStartPos = searchForOpeningPeer(fEndPos,
						fPairs[pairIndex2 - 1], fPairs[pairIndex2], fDocument);
				if (fStartPos > -1)
					return true;
				else
					fEndPos = -1;
			} else if (fStartPos > -1) {
				fAnchor = LEFT;
				fEndPos = searchForClosingPeer(fStartPos, fPairs[pairIndex1],
						fPairs[pairIndex1 + 1], fDocument);
				if (fEndPos > -1)
					return true;
				else
					fStartPos = -1;
			}

		} catch (BadLocationException x) {
		}

		return false;
	}

	protected int searchForClosingPeer(int offset, char openingPeer,
			char closingPeer, IDocument document) throws BadLocationException {

		int depth = 1;
		//offset -= 1;
		while (true) {
			offset++;

			if (fDocument.getChar(offset) == openingPeer)
				depth++;
			else if (fDocument.getChar(offset) == closingPeer)
				depth--;

			if (depth == 0)
				return offset;
		}
	}

	protected int searchForOpeningPeer(int offset, char openingPeer,
			char closingPeer, IDocument document) throws BadLocationException {

		int depth = -1;
		while (true) {
			offset--;

			if (fDocument.getChar(offset) == openingPeer)
				depth++;
			else if (fDocument.getChar(offset) == closingPeer)
				depth--;

			if (depth == 0)
				return offset;
		}
	}
}
