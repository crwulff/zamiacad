/*
 * Copyright 2007 by the authors indicated in the @author tags.
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
 * @author guenter bartsch
 */
public class VerilogScanner extends RuleBasedScanner {

	static class VerilogWordDetector implements IWordDetector {
		
		public boolean isVerilogIdentifierPart(char ch) {
			if (Character.isLetter(ch)) {
				return true;
			} else {
				if (Character.toString(ch).equals("_")) {
					return true; 
				}
			}
			return false;
		}
		
		public boolean isVerilogIdentifierStart(char ch) {
		    return Character.isLetterOrDigit(ch);
		}

		public boolean isWordPart(char character) {
			return this.isVerilogIdentifierPart(character);
		}
		
		public boolean isWordStart(char character) {
			return this.isVerilogIdentifierStart(character);
		}
	}

	private String[] fgKeywords =
	{
			"always",
			"and",
			"assign",
			"attribute",
			"begin",
			"buf",
			"bufif0",
			"bufif1",
			"case",
			"casex",
			"casez",
			"cmos",
			"deassign",
			"default",
			"defparam",
			"disable",
			"edge",
			"else",
			"end",
			"endattribute",
			"endcase",
			"endfunction", 
			"endmodule",
			"endprimitive",
			"endspecify",
			"endtable",
			"endtask",
			"event",
			"for",
			"force",
			"forever",
			"fork",
			"function",
			"highz0",
			"highz1",
			"if",
			"ifnone",
			"initial",
			"inout",
			"input",
			"integer",
			"join",
			"medium",
			"module",
			"large",
			"macromodule",
			"nand",
			"negedge",
			"nmos",
			"nor",
			"not",
			"notif0",
			"notif1",
			"or",
			"output",
			"parameter",
			"pmos",
			"posedge",
			"primitive",
			"pull0",
			"pull1",
			"pulldown",
			"pullup",
			"rcmos",
			"real",
			"realtime",
			"reg",
			"release",
			"repeat",
			"rnmos",
			"rpmos",
			"rtran",
			"rtranif0",
			"rtranif1",
			"scalared",
			"signed",
			"small",
			"specify",
			"specparam",
			"strength",
			"strong0",
			"strong1",
			"supply0",
			"supply1",
			"table",
			"task",
			"time",
			"tran",
			"tranif0",
			"tranif1",
			"tri",
			"tri0",
			"tri1",
			"triand",
			"trior",
			"trireg",
			"unsigned",
			"vectored",
			"wait",
			"wand",
			"weak0",
			"weak1",
			"while",
			"wire",
			"wor",
			"xnor",
			"xor"
	};

	public VerilogScanner() {

		ColorManager colorManager = ColorManager.getInstance();

		IPreferenceStore store = ZamiaPlugin.getDefault().getPreferenceStore();

		RGB colorComment = PreferenceConverter.getColor(store, PreferenceConstants.P_COMMENT);
		RGB colorKeyword = PreferenceConverter.getColor(store, PreferenceConstants.P_KEYWORD);
		RGB colorString = PreferenceConverter.getColor(store, PreferenceConstants.P_STRING);
		RGB colorDefault = PreferenceConverter.getColor(store, PreferenceConstants.P_DEFAULT);

		IToken keyword = new Token(new TextAttribute(colorManager.getColor(colorKeyword)));
		IToken string = new Token(new TextAttribute(colorManager.getColor(colorString)));
		IToken other = new Token(new TextAttribute(colorManager.getColor(colorDefault)));
		IToken comment = new Token(new TextAttribute(colorManager.getColor(colorComment)));

        setDefaultReturnToken(other);
        List<IRule> rules = new ArrayList<IRule>();

        // Add rule for single line comments.
        rules.add(new EndOfLineRule("//", comment));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); 
		// FIXME between ' and ' should only one character to be scanned as string
		rules.add(new SingleLineRule("\'", "\'", string, '\\')); 
		// Add word rule for keywords.
		// FIXME keyword following an underscore should be taken as normal text.
		WordRule wordRule= new WordRule(new VerilogWordDetector(), other);
		for (int i= 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		rules.add(wordRule);
		
		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
