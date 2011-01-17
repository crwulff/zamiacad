/* 
 * Copyright 2011 by the authors indicated in the @author tag. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 */

package org.zamia.instgraph.synth.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import jdd.bdd.BDD;

import org.apache.log4j.Level;
import org.junit.Test;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLValue;
import org.zamia.rtlng.RTLValue.BitValue;

/**
 * @author Guenter Bartsch
 */
public class ExprEngineTest {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private ZamiaProject fZPrj;

	public void setupTest(String aBasePath, String aBuildPath) throws Exception {
		ZamiaLogger.setup(Level.DEBUG);

		File f = new File(aBuildPath);

		assertTrue(f.exists());

		SourceFile sf = new SourceFile(f);

		fZPrj = new ZamiaProject("ExprEngine Test Tmp Project", aBasePath, sf, null);
		fZPrj.clean();
	}

	@Test
	public void testLogic() throws Exception {

		setupTest("examples/synth/combProcTest", "examples/synth/combProcTest/BuildPath.txt");

		IGSMExprEngine ee = IGSMExprEngine.getInstance();

		IGSynth synth = new IGSynth(fZPrj);

		RTLValue one = synth.getBitValue(BitValue.BV_1);

		RTLValue zero = synth.getBitValue(BitValue.BV_0);

		IGSMExprNode l1 = ee.literal(one, synth, null);

		RTLValue v = l1.getStaticValue();

		assertEquals(one, v);

		IGSMExprNode l0 = ee.literal(zero, synth, null);

		v = l0.getStaticValue();

		assertEquals(zero, v);

		IGSMExprNode e1 = ee.binary(BinOp.AND, l1, l1, null);

		v = e1.getStaticValue();

		assertEquals(one, v);

		IGSMExprNode e2 = ee.binary(BinOp.OR, l1, l0, null);

		v = e2.getStaticValue();

		assertEquals(one, v);

		IGSMExprNode e3 = ee.binary(BinOp.AND, l1, l0, null);

		v = e3.getStaticValue();

		assertEquals(zero, v);

		RTLSignal sa = new RTLSignal(false, null, null, "A", null, synth.getBitType(), null, fZPrj.getZDB());
		IGSMExprNodeSignal s1 = new IGSMExprNodeSignal(sa, null, synth);
		
		RTLSignal sb = new RTLSignal(false, null, null, "B", null, synth.getBitType(), null, fZPrj.getZDB());
		IGSMExprNodeSignal s2 = new IGSMExprNodeSignal(sb, null, synth);
		
		RTLSignal sc = new RTLSignal(false, null, null, "C", null, synth.getBitType(), null, fZPrj.getZDB());
		IGSMExprNodeSignal s3 = new IGSMExprNodeSignal(sc, null, synth);
		
		RTLSignal sd = new RTLSignal(false, null, null, "D", null, synth.getBitType(), null, fZPrj.getZDB());
		IGSMExprNodeSignal s4 = new IGSMExprNodeSignal(sd, null, synth);
		
		IGSMExprNode e4 = ee.binary(BinOp.AND, s1, l0, null);
		v = e4.getStaticValue();
		assertEquals(zero, v);

		IGSMExprNode e9 = ee.binary(BinOp.AND, ee.binary(BinOp.AND, ee.binary(BinOp.AND, s1, s2, null), s3, null), s4, null);
		v = e9.getStaticValue();
		assertEquals(null, v);
		
		System.out.printf ("e 9: %s\n", e9);

		IGSMExprNode e10 = ee.binary(BinOp.OR, ee.binary(BinOp.AND, s1, s2, null), ee.binary(BinOp.AND, s3, s4, null), null);
		v = e10.getStaticValue();
		assertEquals(null, v);
		
		System.out.printf ("e10: %s\n", e10);
		
		((IGSMExprNodeBDD)e10).printCubes();
		
		IGSMExprNode e5 = ee.binary(BinOp.AND, s1, s2, null);
		v = e5.getStaticValue();
		assertEquals(null, v);
		
		System.out.printf ("e 5: %s\n", e5);
		System.out.printf ("e10: %s\n", e10);
		System.out.printf ("e 9: %s\n", e9);

		IGSMExprNode e6 = ee.unary(UnaryOp.NOT, s1, null);
		v = e6.getStaticValue();
		assertEquals(null, v);

		IGSMExprNode e7 = ee.binary(BinOp.AND, s1, e6, null);
		v = e7.getStaticValue();
		assertEquals(zero, v);
		
		IGSMExprNode e8 = ee.binary(BinOp.OR, s1, e6, null);
		v = e8.getStaticValue();
		assertEquals(one, v);
	}

	@Test
	public void testBDD() throws Exception {

		BDD bdd = new BDD(5000, 5000);

		int a = bdd.createVar();
		int b = bdd.createVar();
		int c = bdd.createVar();

		int z = bdd.or(bdd.and(a, bdd.and(b, c)), bdd.and(a, bdd.and(bdd.not(b), bdd.not(c))));

		bdd.print(z);

		bdd.printSet(z);
	}

}
