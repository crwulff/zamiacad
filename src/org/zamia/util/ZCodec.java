/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 7, 2009
 */
package org.zamia.util;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZCodec {

	private static final char code[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String encode(String aStr) {

		int l = aStr.length();

		StringBuilder buf = new StringBuilder(l * 4);

		for (int i = 0; i < l; i++) {

			int c = aStr.charAt(i);

			buf.append(code[(c >> 12) & 0xf]);
			buf.append(code[(c >> 8) & 0xf]);
			buf.append(code[(c >> 4) & 0xf]);
			buf.append(code[(c & 0xf)]);
		}

		return buf.toString();
	}

	private static int getC(String aStr, int aPos) {
		char c = aStr.charAt(aPos);

		switch (c) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		case 'A':
			return 10;
		case 'B':
			return 11;
		case 'C':
			return 12;
		case 'D':
			return 13;
		case 'E':
			return 14;
		case 'F':
			return 15;
		}

		return 0;
	}

	public static String decode(String aStr) {

		int l = aStr.length();

		StringBuilder buf = new StringBuilder(l / 4);

		for (int i = 0; i < l; i += 4) {

			int c = getC(aStr, i) << 12;
			c += getC(aStr, i + 1) << 8;
			c += getC(aStr, i + 2) << 4;
			c += getC(aStr, i + 3);

			buf.append((char) c);
		}

		return buf.toString();
	}

}
