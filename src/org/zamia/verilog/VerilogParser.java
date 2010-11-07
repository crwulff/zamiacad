/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia.verilog;

import java.io.IOException;
import java.io.Reader;

import org.zamia.DMManager;
import org.zamia.ERManager;
import org.zamia.ExceptionLogger;
import org.zamia.IHDLParser;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.util.HashSetArray;
import org.zamia.verilog.lexer.Lexer;
import org.zamia.verilog.lexer.LexerException;
import org.zamia.verilog.node.Start;
import org.zamia.verilog.parser.Parser;
import org.zamia.verilog.parser.ParserException;
import org.zamia.verilog.pre.IPreprocessor;
import org.zamia.verilog.pre.VerilogPreprocessor;
import org.zamia.vhdl.ast.DMUID;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VerilogParser implements IHDLParser {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	@Override
	public HashSetArray<DMUID> parse(Reader aReader, String aLibId, SourceFile aSF, int aPriority, boolean aUseFSCache, boolean aBottomUp, ZamiaProject aZPrj) throws IOException {

		HashSetArray<DMUID> dus = new HashSetArray<DMUID>();

		DMManager dum = aZPrj.getDUM();
		ERManager erm = aZPrj.getERM();

		try {
			IPreprocessor pre = new VerilogPreprocessor(aSF, aReader, aUseFSCache);

			Parser p = new Parser(new Lexer(pre));

			Start ast = p.parse();

			ast.visit(new SourceLocationGenerator());

			ast.apply(new ModuleWrapperGenerator(dus, dum, aSF, aLibId, aPriority, aUseFSCache));

			aSF.setNumLines(pre.getLine());
			// FIXME: 
			aSF.setNumChars(0);

		} catch (ParserException e) {
			erm.addError(new ZamiaException(e.toString(), new SourceLocation(e.getToken().getSource(), e.getToken().getLine(), e.getToken().getPos())));
		} catch (LexerException e) {
			erm.addError(new ZamiaException(e.toString()));
		}

		return dus;
	}

}
