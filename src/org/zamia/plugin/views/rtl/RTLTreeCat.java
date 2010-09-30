/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

import org.zamia.rtl.RTLModule;

/**
 * @author guenter bartsch
 */
public class RTLTreeCat {

	public enum TreeCat {SIGNALS, PORTS, SUBS, BUILTINS}

	private String id;
	private RTLModule module;
	private TreeCat cat;;
	
	public RTLTreeCat (String id_, RTLModule module_, TreeCat cat_) {
		
		id = id_;
		module = module_;
		cat = cat_;
	}

	public String getId() {
		return id;
	}

	public RTLModule getModule() {
		return module;
	}

	public TreeCat getCat() {
		return cat;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
}
