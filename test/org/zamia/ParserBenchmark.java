/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 8, 2009
 */
package org.zamia;

import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.vhdl2008.VHDL2008Parser;

import java.io.File;
import java.io.Reader;


/**
 * @author Guenter Bartsch
 */

public class ParserBenchmark {

	private static FSCache fsCache = FSCache.getInstance();

	private void runBenchmark(IHDLParser aParser) {
		ZamiaProject zprj;
		try {
			zprj = new ZamiaProject();
			SourceFile sf = new SourceFile(new File("examples/leonSOC/lib/tech/axcelerator/components/axcelerator_full.vhd"));

			Reader rd = fsCache.openFile(sf, false);

			long startTime = System.currentTimeMillis();

			HashSetArray<DMUID> dus = aParser.parse(rd, "WORK", sf, 1, false, true, zprj);

			long stopTime = System.currentTimeMillis();

			int n = dus.size();
			int nErrors = zprj.getERM().getNumErrors();
			System.out.println("Parser 1 : #DUs    : " + n);

			System.out.println("Parser 1 : #Errors : " + nErrors);

			double seconds = (stopTime - startTime) / 1000.0;

			System.out.println("Parser 1 : Runtime : " + seconds);

			System.out.println("");


		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public void run() {

		VHDL2008Parser parser = new VHDL2008Parser();

		runBenchmark(parser);
	}

	public static void main(String[] args) {

		ParserBenchmark pb = new ParserBenchmark();

		pb.run();

	}

}
