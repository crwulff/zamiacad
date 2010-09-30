/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 29, 2008
 */
package org.zamia.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All tests relevant for the current stable ZamiaCAD release
 * 
 * @author Guenter Bartsch
 *
 */

public class TestSuiteStable {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.zamia.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(FSCacheTest.class);
		suite.addTestSuite(BuildPathTest.class);
		suite.addTestSuite(DG2ETest.class);
		suite.addTestSuite(VHDLIndexerTest.class);
		suite.addTestSuite(IGTest.class);
		suite.addTestSuite(SATest.class);
		suite.addTestSuite(ParserTest.class);
		suite.addTestSuite(ZDBTest.class);
		suite.addTestSuite(VCDTest.class);
		//$JUnit-END$
		return suite;
	}

}
