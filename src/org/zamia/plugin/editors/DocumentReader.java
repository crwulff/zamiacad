/*
 * Copyright 2005 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Dec 12, 2005
 */

package org.zamia.plugin.editors;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author guenter bartsch
 */
public class DocumentReader extends Reader {

	private IDocument doc;
	private int off;
	
	public DocumentReader (IDocument doc_) {
		doc = doc_;
		off = 0;
	}
	
	@Override
	public int read(char[] cbuf_, int off_, int len_) throws IOException {
		try {
			cbuf_[off_] = doc.getChar(off++);
		} catch (BadLocationException e) {
			return -1;
		}
		return 1;
	}

	@Override
	public void close() throws IOException {
	}
}
