/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.graphics.RGB;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.preferences.PreferenceConstants;


import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class VHDLScanner extends RuleBasedScanner {

	static class VHDLWordDetector implements IWordDetector {

		public boolean isVHDLIdentifierPart(char ch) {
			if (Character.isLetter(ch)) {
				return true;
			} else {
				if (Character.toString(ch).equals("_")) {
					return true;
				}
			}
			return false;
		}

		public boolean isVHDLIdentifierStart(char ch) {
			return Character.isLetterOrDigit(ch);
		}

		public boolean isWordPart(char character) {
			return this.isVHDLIdentifierPart(character);
		}

		public boolean isWordStart(char character) {
			return this.isVHDLIdentifierStart(character);
		}
	}

	public static String[] fgKeywords = { "abs", "access", "after", "alias", "and", "architecture", "assert", "attribute", "begin", "block", "body", "buffer", "bus", "case", "component",
			"configuration", "constant", "disconnect", "downto", "else", "elsif", "end", "entity", "exit", "file", "for", "function", "generate", "generic", "group", "guarded", "if", "impure", "in",
			"inertial", "inout", "is", "label", "library", "linkage", "literal", "loop", "map", "mod", "nand", "new", "next", "nor", "not", "null", "of", "on", "open", "or", "others", "out",
			"package", "port", "postponed", "procedural", "procedure", "process", "protected", "pure", "range", "record", "reference", "register", "reject", "rem", "report", "return", "rol", "ror",
			"select", "severity", "signal", "shared", "sla", "sll", "sra", "srl", "subtype", "then", "to", "transport", "type", "unaffected", "units", "until", "use", "variable", "wait", "when",
			"while", "with", "xnor", "xor" };

	public VHDLScanner() {

		ColorManager colorManager = ColorManager.getInstance();

		IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();

		RGB colorComment = PreferenceConverter.getColor(store, PreferenceConstants.P_COMMENT);
		RGB colorKeyword = PreferenceConverter.getColor(store, PreferenceConstants.P_KEYWORD);
		RGB colorString = PreferenceConverter.getColor(store, PreferenceConstants.P_STRING);
		RGB colorDefault = PreferenceConverter.getColor(store, PreferenceConstants.P_DEFAULT);
		//		RGB colorBackground = PreferenceConverter.getColor(store, PreferenceConstants.P_BACKGROUND);

		//IToken keyword = new Token(new TextAttribute(colorManager.getColor(colorKeyword),colorManager.getColor(colorBackground),0));
		//IToken string = new Token(new TextAttribute(colorManager.getColor(colorString),colorManager.getColor(colorBackground),0));
		//IToken other = new Token(new TextAttribute(colorManager.getColor(colorDefault),colorManager.getColor(colorBackground),0));
		//IToken comment = new Token(new TextAttribute(colorManager.getColor(colorComment),colorManager.getColor(colorBackground),0));

		IToken keyword = new Token(new TextAttribute(colorManager.getColor(colorKeyword)));
		IToken string = new Token(new TextAttribute(colorManager.getColor(colorString)));
		IToken other = new Token(new TextAttribute(colorManager.getColor(colorDefault)));
		IToken comment = new Token(new TextAttribute(colorManager.getColor(colorComment)));

		setDefaultReturnToken(other);
		List<IRule> rules = new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("--", comment));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		// FIXME between ' and ' should only one character to be scanned as string
		//rules.add(new SingleLineRule("\'", "\'", string, '\\')); 
		// Add word rule for keywords.
		// FIXME keyword following an underscore should be taken as normal text.
		WordRule wordRule = new WordRule(new VHDLWordDetector(), other, true);
		for (int i = 0; i < fgKeywords.length; i++) {
			wordRule.addWord(fgKeywords[i], keyword);
		}

		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
