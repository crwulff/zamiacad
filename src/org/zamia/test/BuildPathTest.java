/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 2, 2008
 */
package org.zamia.test;

import java.io.File;

import org.apache.log4j.Level;
import org.zamia.BuildPath;
import org.zamia.SourceFile;
import org.zamia.ZamiaLogger;

import junit.framework.TestCase;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class BuildPathTest extends TestCase {

	public void setupTest() {
		ZamiaLogger.setup(Level.DEBUG);
	}

	public void testBP() throws Exception {

		BuildPath bp = new BuildPath();
		
		bp.setSrc(new SourceFile(new File("test/BuildPath.txt")));
		bp.parse(null, false, null);
	}
	
}
