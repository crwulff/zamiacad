/*
 * Copyright 2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/
package org.zamia.plugin.editors;

import java.util.ArrayList;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaOutlineFolder {
	
	public String id;
	@SuppressWarnings("unchecked")
	public ArrayList items;

	@SuppressWarnings("unchecked")
	public ZamiaOutlineFolder(String cat, ArrayList l) {
		id = cat;
		items = l;
	}

}
