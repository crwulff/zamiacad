/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;

/**
 * @author guenter bartsch
 */

public class RTLTreeLabelProvider implements ILabelProvider {

	public Image getImage(Object arg0) {
		return null;
	}

	public String getText(Object node_) {
		
		if (node_ instanceof RTLModule) {
			RTLModule m = (RTLModule) node_;
			return m.getInstanceName();
		}
		
		if (node_ instanceof RTLSignal) {
			RTLSignal s = (RTLSignal) node_;
			return s.getId();
		}
		
		if (node_ instanceof RTLPort) {
			RTLPort p = (RTLPort) node_;
			return p.getId();
		}
		
		return node_.toString();
	}

	public void addListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
	}


}
