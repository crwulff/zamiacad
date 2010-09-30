/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 18, 2009
 */
package org.zamia.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class LevelGZIPOutputStream extends GZIPOutputStream {
	public LevelGZIPOutputStream(OutputStream aOutputStream, int aCompressionLevel) throws IOException {
		super(aOutputStream);
		def.setLevel(aCompressionLevel);
	}
}
