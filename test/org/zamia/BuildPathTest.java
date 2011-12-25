/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 2, 2008
 */
package org.zamia;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


/**
 * @author Guenter Bartsch
 */

public class BuildPathTest {
	@Before
	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
	}

	@Test
	public void testBP() throws Exception {

		BuildPath bp = new BuildPath(new SourceFile(new File("examples/BuildPath.txt")));
		bp.parse(null, false, null);
	}

}
