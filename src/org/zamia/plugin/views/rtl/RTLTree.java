/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author guenter bartsch
 */
public class RTLTree extends TreeViewer {

	public RTLTree(Composite parent) {
		super(parent);
		
		setContentProvider(new RTLTreeContentProvider());
		setLabelProvider(new RTLTreeLabelProvider());
		
	}

}
