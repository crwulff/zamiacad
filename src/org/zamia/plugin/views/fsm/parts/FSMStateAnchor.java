/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.fsm.parts;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author guenter bartsch
 */
class FSMStateAnchor extends AbstractConnectionAnchor {

	private boolean top;
	
	FSMStateAnchor(IFigure source, boolean top_) {
		super(source);
		top = top_;
	}

	public Point getLocation(Point reference) {
		Rectangle r = getOwner().getBounds().getCopy();
		getOwner().translateToAbsolute(r);
		int off = r.width / 2;
        
		boolean top = this.top;
		
		if (!top) {
			if (r.contains(reference) || r.bottom() > reference.y)
				top = true;
		} else {
			 if (r.contains(reference) || r.y < reference.y)
				 top = false;
		}

		
		if (!top)
			return r.getBottomLeft().translate(off, -1);
		else
			return r.getTopLeft().translate(off, 0);
	}

}
