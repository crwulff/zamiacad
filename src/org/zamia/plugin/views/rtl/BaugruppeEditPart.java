/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 3, 2008
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * @author guenter bartsch
 */

public class BaugruppeEditPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		Label label = new Label();
		
		label.setText("hello");
		
		label.setBounds(new Rectangle(10,10,50,15));
		
		return label;
		
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

}
