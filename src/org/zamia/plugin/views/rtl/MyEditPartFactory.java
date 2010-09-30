/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 3, 2008
 */
package org.zamia.plugin.views.rtl;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * @author guenter bartsch
 */
public class MyEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart result = null;
		
		if (model instanceof Produkt) {
			result = new ProduktEditPart();
		} else if (model instanceof Baugruppe) {
			result = new BaugruppeEditPart();
		}
		
		result.setModel(model);
		return result;
	}

}
