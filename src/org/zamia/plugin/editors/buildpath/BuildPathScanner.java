/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.editors.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.graphics.RGB;
import org.zamia.plugin.ZamiaPlugin;
import org.zamia.plugin.editors.ColorManager;
import org.zamia.plugin.preferences.PreferenceConstants;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
public class BuildPathScanner extends RuleBasedScanner {

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

	public static String[] fgKeywords = { "extern", "local", "toplevel", "readonly", "ignore", "include", "option", "true", "false", "topdown", "bottomup", "list", "none", "default", "exec", "recursive", "nonrecursive" };

	public BuildPathScanner() {

		ColorManager colorManager = ColorManager.getInstance();

		IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();

		RGB colorComment = PreferenceConverter.getColor(store, PreferenceConstants.P_COMMENT);
		RGB colorKeyword = PreferenceConverter.getColor(store, PreferenceConstants.P_KEYWORD);
		RGB colorString = PreferenceConverter.getColor(store, PreferenceConstants.P_STRING);
		RGB colorDefault = PreferenceConverter.getColor(store, PreferenceConstants.P_DEFAULT);
		//RGB colorBackground = PreferenceConverter.getColor(store, PreferenceConstants.P_BACKGROUND);

		//IToken keyword = new Token(new TextAttribute(colorManager.getColor(colorKeyword), colorManager.getColor(colorBackground), 0));
		//IToken string = new Token(new TextAttribute(colorManager.getColor(colorString), colorManager.getColor(colorBackground), 0));
		//IToken other = new Token(new TextAttribute(colorManager.getColor(colorDefault), colorManager.getColor(colorBackground), 0));
		//IToken comment = new Token(new TextAttribute(colorManager.getColor(colorComment), colorManager.getColor(colorBackground), 0));
		IToken keyword = new Token(new TextAttribute(colorManager.getColor(colorKeyword)));
		IToken string = new Token(new TextAttribute(colorManager.getColor(colorString)));
		IToken other = new Token(new TextAttribute(colorManager.getColor(colorDefault)));
		IToken comment = new Token(new TextAttribute(colorManager.getColor(colorComment)));

		setDefaultReturnToken(other);
		List<IRule> rules = new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", comment));

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
