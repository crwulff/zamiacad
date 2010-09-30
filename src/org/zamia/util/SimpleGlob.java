/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 24, 2010
 */
package org.zamia.util;

/**
 * 
 * @author Guenter Bartsch
 * 
 * original from: http://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
 *
 */

public class SimpleGlob {

	private SimpleGlob() {
	}

	public static boolean match(String aText, String aPattern) {
		return matchCharacter(aText, aPattern, 0, 0);
	}

	private static boolean matchCharacter(String aText, String aPattern, int aPatternIdx, int aTextIdx) {
		if (aPatternIdx >= aPattern.length()) {
			return false;
		}

		switch (aPattern.charAt(aPatternIdx)) {
		case '?':
			// Match any character
			if (aTextIdx >= aText.length()) {
				return false;
			}
			break;

		case '*':
			// * at the end of the pattern will match anything
			if (aPatternIdx + 1 >= aPattern.length() || aTextIdx >= aText.length()) {
				return true;
			}

			// Probe forward to see if we can get a match
			while (aTextIdx < aText.length()) {
				if (matchCharacter(aText, aPattern, aPatternIdx + 1, aTextIdx)) {
					return true;
				}
				aTextIdx++;
			}

			return false;

		default:
			if (aTextIdx >= aText.length()) {
				return false;
			}

			String textChar = aText.substring(aTextIdx, aTextIdx + 1);
			String patternChar = aPattern.substring(aPatternIdx, aPatternIdx + 1);

			// Note the match is case insensitive
			if (textChar.compareToIgnoreCase(patternChar) != 0) {
				return false;
			}
		}

		// End of pattern and text?
		if (aPatternIdx + 1 >= aPattern.length() && aTextIdx + 1 >= aText.length()) {
			return true;
		}

		// Go on to match the next character in the pattern
		return matchCharacter(aText, aPattern, aPatternIdx + 1, aTextIdx + 1);
	}

}
