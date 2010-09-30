/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 8, 2009
 */
package org.zamia.vhdl.vhdl2002;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.zamia.ExceptionLogger;


/**
 * Attempt at a manual lexer implementation for speed
 * 
 * not used at the moment, just kept here for archiving purposes
 * 
 * @author Guenter Bartsch
 * 
 */

public class FastVHDLTokenManager implements VHDL2002ParserConstants {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static boolean dump = false;

	private Reader fReader;

	private int fLine;

	private int fCol;

	private int fNumChars;

	private boolean fEOF;

	private char fCh;

	private Token fToken;

	private StringBuilder fBuf;

	private int fLastTokenKind;

	private HashMap<String, Integer> fKeywords;

	public FastVHDLTokenManager() {
	}

	public FastVHDLTokenManager(Reader aReader) {
		fReader = aReader;

		fKeywords = new HashMap<String, Integer>();
		for (int i = 0; i < tokenImage.length; i++) {

			String image = tokenImage[i];

			int l = image.length();

			if (l < 3 || image.charAt(0) != '"')
				continue;

			if (!Character.isLetter(image.charAt(1))) {
				continue;
			}

			String kw = image.substring(1, l - 1).toLowerCase();

			fKeywords.put(kw, i);
		}

		fEOF = false;
		fLine = 1;
		fCol = 1;
		fNumChars = 0;
		fLastTokenKind = 0;
		try {
			getCh();
		} catch (IOException e) {
			el.logException(e);
			fEOF = true;
		}
	}

	private void getCh() throws IOException {
		if (fBuf != null) {
			fBuf.append(fCh);
		}

		if (fToken != null) {
			fToken.endColumn = fCol;
			fToken.endLine = fLine;
		}

		if (fReader != null) {
			int i = fReader.read();
			if (i < 0) {
				fEOF = true;
			} else {
				fCh = (char) i;

				if (dump) {
					System.out.print(fCh);
					System.out.flush();
				}

				fCol++;
				fNumChars++;
				if (fCh == 10) {
					fLine++;
					fCol = 1;
				}
			}
		} else {
			fEOF = true;
		}
	}

	private void beginToken() {
		fToken.beginLine = fLine;
		fToken.beginColumn = fCol;
		fToken.endLine = fLine;
		fToken.endColumn = fCol;
		fBuf = new StringBuilder();
	}

	public Token getNextToken() {

		fToken = new Token();
		fBuf = null;
		try {
			while (!fEOF && fToken.kind == 0) {
				while (!fEOF && Character.isWhitespace(fCh)) {
					getCh();
				}

				if (!fEOF) {

					beginToken();

					switch (fCh) {
					case '+':
						fToken.kind = ADD;
						getCh();
						break;
					case '-':
						getCh();

						if (fCh == '-') {
							comment();
							// no symbol set -> outer loop iterates
						} else
							fToken.kind = SUB;
						break;
					case '*':
						getCh();
						if (fCh == '*') {
							getCh();
							fToken.kind = EXP;
						} else
							fToken.kind = MUL;
						break;
					case '=':
						getCh();
						if (fCh == '>') {
							getCh();
							fToken.kind = FOLLOWS;
						} else
							fToken.kind = EQ;
						break;
					case '<':
						getCh();

						if (fCh == '=') {
							getCh();
							fToken.kind = LE;
						} else if (fCh == '>') {
							getCh();
							fToken.kind = BOX;
						} else
							fToken.kind = LO;
						break;
					case '>':
						getCh();

						if (fCh == '=') {
							getCh();
							fToken.kind = GE;
						} else
							fToken.kind = GT;
						break;
					case ';':
						fToken.kind = SEMICOLON;
						getCh();
						break;
					case ':':
						fToken.kind = COLON;
						getCh();
						if (fCh == '=') {
							getCh();
							fToken.kind = ASSIGN;
						}
						break;
					case '.':
						fToken.kind = PERIOD;
						getCh();
						break;
					case ',':
						fToken.kind = COMMA;
						getCh();
						break;
					case '|':
						fToken.kind = PIPE;
						getCh();
						break;
					case '(':
						fToken.kind = LPAREN;
						getCh();
						break;
					case ')':
						fToken.kind = RPAREN;
						getCh();
						break;
					case '[':
						fToken.kind = LBRACKET;
						getCh();
						break;
					case ']':
						fToken.kind = RBRACKET;
						getCh();
						break;
					case '&':
						fToken.kind = CONCAT;
						getCh();
						break;
					case '/':
						getCh();
						if (fCh == '=') {
							getCh();
							fToken.kind = NEQ;
						} else
							fToken.kind = DIV;
						break;
					case '\'':
						getCh();

						if (fLastTokenKind == basic_identifier || fLastTokenKind == extended_identifier || fLastTokenKind == RBRACKET || fLastTokenKind == RPAREN) {
							fToken.kind = APOSTROPHE;
						} else {

							fToken.kind = character_literal;

							getCh();
							if (fCh != '\'') {

								throw new TokenMgrError(false, 0, fLine, fCol, fBuf.toString(), fCh, 0);

							} else {
								getCh(); // consume '
							}
						}
						break;
					case '"':
						getCh();
						string();
						fToken.kind = string_literal;
						break;

					case '\\':
						getCh();
						fToken.kind = extended_identifier;
						extendedIdentifier();
						break;

					default:
						if (Character.isDigit(fCh)) {
							number();
						} else if (Character.isLetter(fCh)) {

							char base = fCh;

							getCh();

							// bit string ?
							if (base == 'b' || base == 'B' || base == 'o' || base == 'O' || base == 'x' || base == 'X') {
								if (fCh == '"') {
									fToken.kind = bit_string_literal;
									getCh();
									string();
								} else {
									identifier();
								}
							} else {
								identifier();
							}
						} else {
							getCh();
						}

					}
				}
			}

		} catch (IOException e) {
			el.logException(e);
			fEOF = true;
		}
		fLastTokenKind = fToken.kind;

		if (fToken.image == null && fBuf != null) {
			fToken.image = fBuf.toString();
		}

		if (dump) {
			System.out.print(" [" + fToken.kind + " " + fToken.image + "]");
			System.out.flush();
		}

		return fToken;
	}

	private void comment() throws IOException {
		while (!fEOF && (fCh != '\n') && (fCh != '\r')) {
			getCh();
		}
	}

	private void string() throws IOException {

		boolean endDetected = false;

		while (!endDetected && !fEOF) {

			while (!fEOF && (fCh != '"')) {
				getCh();
			}

			if (fEOF) {
				throw new TokenMgrError(true, 0, fLine, fCol, fBuf.toString(), fCh, 0);
			}
			getCh();
			if (fCh != '"') {
				endDetected = true;
			} else {
				getCh(); // VHDL way of quoting " in strings
			}

		}
	}

	private void extendedIdentifier() throws IOException {

		while (!fEOF && fCh != '\\') {
			getCh();
		}

		if (fEOF)
			throw new TokenMgrError(true, 0, fLine, fCol, fBuf.toString(), fCh, 0);

		// consume /
		getCh();
	}

	private void integer() throws IOException {

		if (!fEOF && (fCh >= '0') && (fCh <= '9')) {

			getCh();

			while (!fEOF && (((fCh >= '0') && (fCh <= '9')) || fCh == '_')) {
				getCh();
			}
		}
	}

	private void based_integer() throws IOException {
		//		  | <#based_integer:                   (("_")? <hex_digit>)*>

		while (!fEOF && (((fCh >= '0') && (fCh <= '9')) || ((fCh >= 'a') && (fCh <= 'f')) || ((fCh >= 'A') && (fCh <= 'F')) || fCh == '_')) {
			getCh();
		}

	}

	private void number() throws IOException {

		integer();

		fToken.kind = decimal_literal;

		// | <decimal_literal:                  <integer> ( "." <integer>)? ( <exponent> )?> : DEFAULT
		if (!fEOF && fCh == '.') {
			getCh();
			integer();

			// | <#exponent:                        ("E" ("+")? <integer>) | ("E" "-" <integer>)>

			if (!fEOF && (fCh == 'e' || fCh == 'E')) {
				getCh();

				if (!fEOF && (fCh == '+' || fCh == '-')) {
					getCh();
				}

				integer();
			}
		} else {

			// | <based_literal:                    <base> "#" <based_integer> ("." <based_integer>)? "#" (<exponent>)?> : DEFAULT
			if (!fEOF && fCh == '#') {
				getCh();

				based_integer();

				fToken.kind = based_literal;

				if (!fEOF && fCh == '.') {
					getCh();

					based_integer();
				}

				if (fEOF)
					throw new TokenMgrError(true, 0, fLine, fCol, fBuf.toString(), fCh, 0);

				if (fCh != '#') {
					throw new TokenMgrError(true, 0, fLine, fCol, fBuf.toString(), fCh, 0);
				}

				getCh();

				if (!fEOF && (fCh == 'e' || fCh == 'E')) {
					getCh();

					if (!fEOF && (fCh == '+' || fCh == '-')) {
						getCh();
					}

					integer();
				}
			}
		}
	}

	private void identifier() throws IOException {

		while (((fCh >= 'A') && (fCh <= 'Z')) || ((fCh >= 'a') && (fCh <= 'z')) || (fCh == '_') || ((fCh >= '0') && (fCh <= '9'))) {
			getCh();
		}

		fToken.kind = basic_identifier;
		fToken.image = fBuf.toString();

		/* keyword ? */
		String uid = fToken.image.toLowerCase();

		Integer n = fKeywords.get(uid);
		if (n != null) {
			fToken.kind = n.intValue();
		}

	}

	public int getLine() {
		return fLine;
	}

	public int getNumChars() {
		return fNumChars;
	}
}
