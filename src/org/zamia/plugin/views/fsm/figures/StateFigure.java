/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.fsm.figures;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.parts.EllipseBorder;

/**
 * @author guenter bartsch
 */

public class StateFigure extends Figure {
	public final static Color classColor = new Color(null, 255, 255, 206);

	private FSMState state;

	private Label name, outputs;

	public StateFigure() {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new EllipseBorder());
		layout.setSpacing(3);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		setBackgroundColor(classColor);
		setOpaque(false);

		// Ellipse el = new Ellipse();

		name = new Label();
		name.setLabelAlignment(PositionConstants.LEFT);

		add(name);

		RectangleFigure line = new RectangleFigure();

		line.setOpaque(true);
		line.setSize(50, 1);
		line.setForegroundColor(ColorConstants.gray);
		add(line);

		outputs = new Label();
		outputs.setLabelAlignment(PositionConstants.LEFT);
		outputs.setText("Outputs");
		add(outputs);
	}

	public void setText(String t_) {
		name.setText(t_);
	}

	public void setState(FSMState state_) {
		state = state_;
		setText(state.getId());
	}

	// protected void paintFigure(Graphics graphics) {
	// super.paintFigure(graphics);
	// Rectangle bounds = getBounds();
	// graphics.translate(bounds.x, bounds.y);
	// //graphics.drawText("STATE", 10,10);
	// graphics.fillOval(0, 0, bounds.width, bounds.height);
	// graphics.drawOval(0, 0, bounds.width, bounds.height);
	// graphics.translate(-bounds.x, -bounds.y);
	// }

}
