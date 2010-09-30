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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.zamia.plugin.views.fsm.model.Transition;
import org.zamia.plugin.views.fsm.policies.TransitionEditPolicy;


/**
 * @author hudsonr Created on Jul 16, 2003
 */
public class TransitionPart extends AbstractConnectionEditPart {

	Font scaledFont = null;
	
	public TransitionPart() {
		if (scaledFont == null)
			scaledFont = new Font(Display.getCurrent(),"Sans", 6, SWT.NONE);
	
	}
	
	@SuppressWarnings("unchecked")
	protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
		Edge e = (Edge) map.get(this);
		NodeList nodes = e.vNodes;
		PolylineConnection conn = (PolylineConnection) getConnectionFigure();
		conn.setTargetDecoration(new PolygonDecoration());
		if (nodes != null) {
			List bends = new ArrayList();
			for (int i = 0; i < nodes.size(); i++) {
				Node vn = nodes.getNode(i);
				int x = vn.x;
				int y = vn.y;
				if (e.isFeedback()) {
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
					bends.add(new AbsoluteBendpoint(x, y));
				} else {
					bends.add(new AbsoluteBendpoint(x, y));
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
				}
			}
			conn.setRoutingConstraint(bends);
		} else {
			conn.setRoutingConstraint(Collections.EMPTY_LIST);
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new TransitionEditPolicy());
	}

	class LabeledPolylineConnection extends PolylineConnection {
		
		private Label conditionLabel;

		public LabeledPolylineConnection() {
			setConnectionRouter(new BendpointConnectionRouter() {
				public void route(Connection conn) {
					GraphAnimation.recordInitialState(conn);
					if (!GraphAnimation.playbackState(conn))
						super.route(conn);
				}
			});

			setTargetDecoration(new PolygonDecoration());
			
			ConnectionLocator relationshipLocator = 
	            new ConnectionLocator(this,ConnectionLocator.MIDDLE);
			conditionLabel = new Label("contains");
			conditionLabel.setFont(scaledFont);
			conditionLabel.setOpaque(true);
			add(conditionLabel,relationshipLocator);
			
		}
		
		public void setLabel(String l_) {
			
			String str = l_;
			int l = l_.length();
			if (l>5)
				str = l_.substring(0, 3) + "...";
			
			conditionLabel.setText(str);
			setToolTip(new Label (l_));
		}
		
	}
	
	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		LabeledPolylineConnection lpc = new LabeledPolylineConnection();
		return lpc;
	}

	/**
	 * @see org.eclipse.gef.EditPart#setSelected(int)
	 */
	public void setSelected(int value) {
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE)
			((PolylineConnection) getFigure()).setLineWidth(2);
		else
			((PolylineConnection) getFigure()).setLineWidth(1);
	}

	@SuppressWarnings("unchecked")
	public void contributeToGraph(CompoundDirectedGraph graph, Map map) {
		GraphAnimation.recordInitialState(getConnectionFigure());
		Node source = (Node) map.get(getSource());
		Node target = (Node) map.get(getTarget());
		Edge e = new Edge(this, source, target);
		e.weight = 2;
		graph.edges.add(e);
		map.put(this, e);
	}

	protected Transition getTransition() {
		return (Transition) getModel();
	}

	
	protected void refreshVisuals() {
		
		Transition trans = getTransition();
		
		
		LabeledPolylineConnection lpc = (LabeledPolylineConnection) getFigure();

		lpc.setLabel (trans.getConditionsAsString());
		
		//((StateFigure) getFigure()).setText(getFSMState().getName());
	}


}
