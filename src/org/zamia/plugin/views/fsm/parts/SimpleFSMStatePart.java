/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.zamia.plugin.views.fsm.parts;

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.jface.viewers.TextCellEditor;
import org.zamia.plugin.views.fsm.figures.StateFigure;


/**
 * @author hudsonr Created on Jul 17, 2003
 */
public class SimpleFSMStatePart extends FSMStatePart {

	@SuppressWarnings("unchecked")
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s, Map map) {
		Node n = new Node(this, s);
		n.outgoingOffset = getAnchorOffset();
		n.incomingOffset = getAnchorOffset();
		n.width = getFigure().getPreferredSize().width;
		n.height = getFigure().getPreferredSize().height;
		n.setPadding(new Insets(10, 8, 10, 12));
		map.put(this, n);
		graph.nodes.add(n);
	}


	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		return new StateFigure();
	}

	int getAnchorOffset() {
		return 9;
	}

	protected void performDirectEdit() {
		if (manager == null) {
			Label l = (Label) getFigure();
			manager = new FSMStateDirectEditManager(this, TextCellEditor.class, new FSMStateCellEditorLocator(l), l);
		}
		manager.show();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		((StateFigure) getFigure()).setState(getFSMState());
	}

}
