/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.fsm.parts;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;

/**
 * @author guenter bartsch
 */
public class EllipseBorder extends AbstractBorder {
	public Insets getInsets(IFigure figure) {
		return new Insets(20);
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		graphics.setForegroundColor(ColorConstants.black);
		graphics.setLineWidth(2);
		graphics.drawOval(figure.getBounds().x, figure.getBounds().y, figure.getBounds().width - 1, figure.getBounds().height - 1);
	}
}
