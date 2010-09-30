/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 3, 2008
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author guenter bartsch
 */
public class RTLViewGEF extends ViewPart {

	private ScrollingGraphicalViewer sgv;

	private Produkt produkt = new Produkt();

	public RTLViewGEF() {
		//sgv.setEditDomain(new DefaultEditDomain(null));
	}

	@Override
	public void createPartControl(Composite parent) {
		sgv = new ScrollingGraphicalViewer();
		sgv.createControl(parent);

		produkt.add(new Baugruppe());
		
		sgv.setEditPartFactory(new MyEditPartFactory());
		sgv.setContents(produkt);

	}

	@Override
	public void setFocus() {
		// sgv.setFocus(part);
	}

}
