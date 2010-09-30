/*
 * Copyright 2006 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 5, 2006
 */

package org.zamia.util;

/**
 * @author guenter bartsch
 */
public class SimpleRegexp {

	/**
	 * 
	 * Convert bash-style patterns ("*.exe") to java style (".*\.exe") regexps
	 * 
	 * @param regexp_
	 *            bash-style pattern
	 * @return java-style regexp
	 */

	public static String convert(String regexp_) {

		StringBuffer res = new StringBuffer();

		int i = 0;
		int l = regexp_.length();
		while (i < l) {
			char c = regexp_.charAt(i);

			switch (c) {
				case '\\' :
					i++;
					if (i >= l)
						break;
					res.append(regexp_.charAt(i));
					i++;
					break;
				case '*' :
					res.append(".*");
					i++;
					break;
				case '?' :
					res.append(".");
					i++;
					break;
				case '.' :
					res.append("\\.");
					i++;
					break;
				default :
					res.append(regexp_.charAt(i));
					i++;
			}
		}

		//System.out.println (regexp_+" converted to "+res);
		
		return res.toString();
	}

}
