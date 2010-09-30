/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/


package org.zamia.plugin.editors;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class VHDLEditor extends ZamiaEditor {

	public VHDLEditor() {
		super(new VHDLScanner(), new String[] {"--", ""});
	}

}
