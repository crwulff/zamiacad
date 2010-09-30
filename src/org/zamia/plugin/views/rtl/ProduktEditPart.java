/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 3, 2008
 */
package org.zamia.plugin.views.rtl;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * @author guenter bartsch
 */
public class ProduktEditPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		Figure f = new Figure();
		f.setOpaque(true);
		f.setBackgroundColor(ColorConstants.white);
		return f;
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	protected List getModelChildren() {
		return getCastedModel().getBaugruppen();
	}
	
	private Produkt getCastedModel() {
		return (Produkt) getModel();
	}
	
}
