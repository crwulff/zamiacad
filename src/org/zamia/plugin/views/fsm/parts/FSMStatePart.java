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

/* based on GEF flow editor example, but heavily modified */ 

package org.zamia.plugin.views.fsm.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;


import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.zamia.plugin.views.fsm.model.FSMElement;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.policies.ActivityDirectEditPolicy;
import org.zamia.plugin.views.fsm.policies.ActivityEditPolicy;
import org.zamia.plugin.views.fsm.policies.ActivityNodeEditPolicy;
import org.zamia.plugin.views.fsm.policies.ActivitySourceEditPolicy;

/**
 * @author guenter bartsch
 */

public abstract class FSMStatePart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

	protected DirectEditManager manager;

	public void activate() {
		super.activate();
		getFSMState().addPropertyChangeListener(this);
	}

	@SuppressWarnings("unchecked")
	protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
		Node n = (Node) map.get(this);
		getFigure().setBounds(new Rectangle(n.x, n.y, n.width, n.height));

		for (int i = 0; i < getSourceConnections().size(); i++) {
			TransitionPart trans = (TransitionPart) getSourceConnections().get(i);
			trans.applyGraphResults(graph, map);
		}
	}

	@SuppressWarnings("unchecked")
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			TransitionPart part = (TransitionPart) getSourceConnections().get(i);
			part.contributeToGraph(graph, map);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			FSMStatePart child = (FSMStatePart) children.get(i);
			child.contributeEdgesToGraph(graph, map);
		}
	}

	@SuppressWarnings("unchecked")
	public abstract void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s, Map map);

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActivityNodeEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ActivitySourceEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ActivityEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ActivityDirectEditPolicy());
	}

	public void deactivate() {
		super.deactivate();
		getFSMState().removePropertyChangeListener(this);
	}

	protected FSMState getFSMState() {
		return (FSMState) getModel();
	}

	@SuppressWarnings("unchecked")
	protected List getModelSourceConnections() {
		return getFSMState().getOutgoingTransitions();
	}

	@SuppressWarnings("unchecked")
	protected List getModelTargetConnections() {
		return getFSMState().getIncomingTransitions();
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FSMStateAnchor(getFigure(), false);
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FSMStateAnchor(getFigure(), false);
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FSMStateAnchor(getFigure(), true);
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FSMStateAnchor(getFigure(), true);
	}

	protected void performDirectEdit() {
	}

	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT)
			performDirectEdit();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (FSMElement.CHILDREN.equals(prop))
			refreshChildren();
		else if (FSMElement.INPUTS.equals(prop))
			refreshTargetConnections();
		else if (FSMElement.OUTPUTS.equals(prop))
			refreshSourceConnections();
		else if (FSMState.NAME.equals(prop))
			refreshVisuals();

		// Causes Graph to re-layout
		((GraphicalEditPart) (getViewer().getContents())).getFigure().revalidate();
	}

	protected void setFigure(IFigure figure) {
		figure.getBounds().setSize(0, 0);
		super.setFigure(figure);
	}

	public String toString() {
		return getModel().toString();
	}
}
