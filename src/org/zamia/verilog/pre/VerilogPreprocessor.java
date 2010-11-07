/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 15, 2010
 */
package org.zamia.verilog.pre;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;
import org.zamia.util.ZStack;
import org.zamia.verilog.lexer.LexerException;
import org.zamia.verilog.pre.VPStackFrame.VPSFType;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VerilogPreprocessor implements IPreprocessor {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private final ZStack<VPStackFrame> fStack = new ZStack<VPStackFrame>();

	private VPStackFrame fCurFileSF = null;

	private final File fCurDir;

	private final HashMap<String, VPMacro> fMacros = new HashMap<String, VPMacro>();

	private boolean fEnabled = true;

	private final ArrayList<Character> fPushbackBuffer = new ArrayList<Character>();

	private final boolean fUseCache;

	public VerilogPreprocessor(SourceFile aSF, Reader aReader, boolean aUseCache) throws IOException, LexerException {
		fUseCache = aUseCache;

		File f = aSF.getFile();

		fCurDir = f != null ? f.getAbsoluteFile().getParentFile() : null;

		openFile(aSF, aReader);
		
	}

	private void openFile(SourceFile aSF, Reader aReader) throws IOException, LexerException {

		fCurFileSF = new VPStackFrame(aSF, aReader, fUseCache);

		fStack.push(fCurFileSF);

		nextCh();
	}

	private void closeFile() throws LexerException, IOException {

		VPStackFrame oldFileSF = fStack.pop();

		if (oldFileSF.getType() != VPSFType.FILE) {
			throw new LexerException("Unexpected end of file: " + oldFileSF.getSourceFile());
		}

		oldFileSF.close();

		fCurFileSF = null;

		int idx = fStack.size() - 1;

		while (idx >= 0) {
			VPStackFrame frame = fStack.get(idx);

			if (frame.getType() == VPSFType.FILE) {
				fCurFileSF = frame;
				break;
			}
		}
	}

	private char fCh = ' ';

	private boolean fEOF = false;

	private void nextCh() throws IOException, LexerException {
		if (fPushbackBuffer.isEmpty()) {

			while (true) {

				if (fCurFileSF == null) {
					fEOF = true;
					fCh = ' ';
					return;
				}

				if (fCurFileSF.isEOF()) {
					closeFile();
				} else {
					int ich = fCurFileSF.read();
					if (ich>=0) {
						fCh = (char) ich;
						return;
					}
				}
			}

		} else {
			fCh = fPushbackBuffer.remove(0);
		}
	}

	private void pushback(String aStr) {
		//System.out.printf("<<<<<'%s'<<<<<", aStr);
		for (int i = aStr.length() - 1; i >= 0; i--) {

			if (!fEOF) {
				fPushbackBuffer.add(0, fCh);
			}
			
			fCh = aStr.charAt(i);
			fEOF = false;
		}
	}

	private boolean isWhitespace() {
		return Character.isWhitespace(fCh);
	}

	private void skipWhitespace() throws IOException, LexerException {
		while (!fEOF && isWhitespace()) {
			nextCh();
		}
	}

	private String fetchString() throws LexerException, IOException {

		skipWhitespace();

		if (fCh != '"') {
			throw new LexerException("String expected.");
		}

		nextCh();

		StringBuilder buf = new StringBuilder();

		while (!fEOF && fCh != '"') {

			buf.append(fCh);

			nextCh();
		}

		if (fEOF || fCh != '"') {
			throw new LexerException("Unterminated string literal detected.");
		} else {
			nextCh();
		}

		return buf.toString();
	}

	private void processInclude() throws IOException, LexerException {

		String fileName = fetchString();

		logger.debug("include file detected: %s\n", fileName);

		if (fEnabled) {

			File includeFile = new File(fileName);

			if (!includeFile.isAbsolute()) {
				includeFile = new File(fCurDir.getAbsolutePath() + File.separator + includeFile.getPath());
			}

			SourceFile sf = new SourceFile(includeFile);

			FSCache fscache = FSCache.getInstance();

			Reader reader = fscache.openFile(sf, fUseCache);

			openFile(sf, reader);
		}
	}

	private int fetchInt() throws IOException, LexerException {

		skipWhitespace();

		StringBuilder buf = new StringBuilder();

		if (!Character.isDigit(fCh)) {
			throw new LexerException("Number expected.");
		}

		while (!fEOF && Character.isDigit(fCh)) {
			buf.append(fCh);
			nextCh();
		}

		return Integer.parseInt(buf.toString());
	}

	private String fetchId() throws IOException, LexerException {

		skipWhitespace();

		StringBuilder buf = new StringBuilder();

		if (!Character.isJavaIdentifierStart(fCh)) {
			throw new LexerException("Identifier expected.");
		}

		while (!fEOF && Character.isJavaIdentifierPart(fCh)) {
			buf.append(fCh);
			nextCh();
		}

		return buf.toString();
	}

	private void processTimescale() throws IOException, LexerException {

		int n = fetchInt();

		String id = fetchId();

		skipWhitespace();
		if (fCh != '/') {
			throw new LexerException("Timescale directive: / expected");
		} else {
			nextCh();
		}

		int n2 = fetchInt();

		String id2 = fetchId();

		logger.debug("\n\nTimescale detected: %d %s / %d %s\n\n", n, id, n2, id2);
	}

	private void updateEnabled() {

		fEnabled = true;

		int n = fStack.size();

		for (int i = n - 1; i >= 0; i--) {

			VPStackFrame sf = fStack.get(i);

			if (sf.getType() != VPSFType.CONDITION) {
				continue;
			}

			if ((!sf.isElse() && !sf.isCond()) || (sf.isElse() && sf.isCond())) {
				fEnabled = false;
				break;
			}
		}
	}

	private void processIfDef() throws IOException, LexerException {

		String id = fetchId();

		boolean b = fMacros.containsKey(id);

		VPStackFrame sf = new VPStackFrame(b);

		fStack.push(sf);

		updateEnabled();
	}

	private void processElse() throws IOException, LexerException {

		VPStackFrame sf = fStack.peek();

		if (sf.getType() != VPSFType.CONDITION) {
			throw new RuntimeException("else without if");
		}

		if (sf.isElse()) {
			throw new RuntimeException("more than one else");
		}

		sf.setElse(true);

		updateEnabled();
	}

	private void processEndIf() throws IOException, LexerException {

		VPStackFrame sf = fStack.pop();

		if (sf.getType() != VPSFType.CONDITION) {
			throw new RuntimeException("endif without if");
		}

		updateEnabled();
	}

	private void processDefine() throws IOException, LexerException {

		String id = fetchId();

		if (fCh == '(') {
			// FIXME:
			throw new RuntimeException("Sorry, macro arguments are not supported yet.");
		}

		StringBuilder macroText = new StringBuilder();

		char lastChar = ' ';
		boolean done = false;
		boolean commentArmed = false;
		boolean comment = false;
		while (!done && !fEOF) {

			char ch = fCh;
			nextCh();

			switch (ch) {
			case '/':
				if (commentArmed) {
					comment = true;
				} else {
					commentArmed = true;
				}
				break;
			case 10:
			case 13:
				if (lastChar != '\\') {
					done = true;
				} else {
					commentArmed = false;
					comment = false;
				}

				break;
			default:
				if (!comment) {
					macroText.append(ch);
				}
				commentArmed = false;
				lastChar = ch;
			}

		}

		if (fEnabled) {
			fMacros.put(id, new VPMacro(id, macroText.toString()));
		}
	}

	private void singleLineComment() throws IOException, LexerException {

		while (true) {

			char ch = fCh;

			nextCh();

			if (ch == 10 || fEOF) {
				return;
			}
		}
	}

	private void multiLineComment() throws IOException, LexerException {

		while (true) {

			char ch = fCh;
			//System.out.printf("%c", ch);

			nextCh();
			if (fEOF) {
				throw new LexerException("File ended in multi-line comment");
			}

			if (ch == '*') {
				ch = fCh;
				if (ch == '/') {
					nextCh();
					if (fEOF) {
						throw new LexerException("File ended in multi-line comment");
					}

					return;
				}
			}
		}
	}

	private int readNextChar() throws IOException, LexerException {

		while (true) {
			if (fEOF) {
				return -1;
			}

			/*
			 * filter out comments
			 */

			if (fCh == '/') {
				nextCh();
				if (fEOF) {
					return '/';
				}

				if (fCh == '/') {
					nextCh();
					singleLineComment();
					continue;
				} else if (fCh == '*') {
					nextCh();
					multiLineComment();
					continue;
				} else {
					return '/';
				}
			}

			if (fCh != '`') {

				char ch = fCh;
				
				nextCh();

				if (fEnabled) {
					//System.out.printf ("%c [%3d]", ch, (int) ch);
					return ch;
				}

			} else {

				nextCh();

				String dir = fetchId();

				logger.debug("\n\ndirective detected: %s\n", dir);

				if (dir.equals("include")) {

					processInclude();

				} else if (dir.equals("timescale")) {

					processTimescale();

				} else if (dir.equals("ifdef")) {

					processIfDef();

				} else if (dir.equals("else")) {

					processElse();

				} else if (dir.equals("endif")) {

					processEndIf();

				} else if (dir.equals("define")) {

					processDefine();

				} else {

					if (fEnabled) {
						VPMacro macro = fMacros.get(dir);

						if (macro != null) {

							logger.debug("\nMACRO BODY:'%s'\n", macro.getBody());

							pushback(macro.getBody());

						} else {

							throw new LexerException(getSourceFile() + ":" + getLine() + "," + getCol() + ": Unknown directive: " + dir);
						}
					}

				}
			}
		}
	}
	
	@Override
	public int read() throws IOException, LexerException {
		return readNextChar();
	}

	@Override
	public void close() throws IOException {
		try {
			closeFile();
		} catch (LexerException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public int getLine() {
		return fCurFileSF != null ? fCurFileSF.getLine() : 0;
	}

	@Override
	public int getCol() {
		return fCurFileSF != null ? fCurFileSF.getCol() : 0;
	}

	@Override
	public SourceFile getSourceFile() {
		return fCurFileSF != null ? fCurFileSF.getSourceFile() : null;
	}

	@Override
	public void unread(char aC) {
		pushback("" + aC);
	}
}
