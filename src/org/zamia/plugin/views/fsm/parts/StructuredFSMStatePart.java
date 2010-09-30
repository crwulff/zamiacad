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

import java.util.EventObject;
import java.util.List;
import java.util.Map;


import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.TextCellEditor;
import org.zamia.plugin.views.fsm.figures.SubgraphFigure;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.policies.ActivityContainerEditPolicy;
import org.zamia.plugin.views.fsm.policies.StructuredActivityLayoutEditPolicy;


/**
 * @author hudsonr Created on Jun 30, 2003
 */
public class StructuredFSMStatePart extends FSMStatePart implements NodeEditPart {

	static final Insets PADDING = new Insets(8, 6, 8, 6);

	static final Insets INNER_PADDING = new Insets(0);

	CommandStackListener stackListener = new CommandStackListener() {
        public void commandStackChanged(EventObject event) {
                if (!GraphAnimation.captureLayout(getFigure()))
                        return;
                while(GraphAnimation.step())
                        getFigure().getUpdateManager().performUpdate();
                GraphAnimation.end();
        }
	};

	
	@SuppressWarnings("unchecked")
	protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {
		for (int i = 0; i < getChildren().size(); i++) {
			FSMStatePart part = (FSMStatePart) getChildren().get(i);
			part.applyGraphResults(graph, map);
		}
	}

	@SuppressWarnings("unchecked")
	protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
		applyOwnResults(graph, map);
		applyChildrenResults(graph, map);
	}

	@SuppressWarnings("unchecked")
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s, Map map) {
		GraphAnimation.recordInitialState(getContentPane());
		Subgraph me = new Subgraph(this, s);
		// me.rowOrder = getActivity().getSortIndex();
		me.outgoingOffset = 5;
		me.incomingOffset = 5;
		IFigure fig = getFigure();
		if (fig instanceof SubgraphFigure) {
			me.width = fig.getPreferredSize(me.width, me.height).width;
			int tagHeight = ((SubgraphFigure) fig).getHeader().getPreferredSize().height;
			me.insets.top = tagHeight;
			me.insets.left = 0;
			me.insets.bottom = tagHeight;
		}
		me.innerPadding = INNER_PADDING;
		me.setPadding(PADDING);
		map.put(this, me);
		graph.nodes.add(me);
		for (int i = 0; i < getChildren().size(); i++) {
			FSMStatePart activity = (FSMStatePart) getChildren().get(i);
			activity.contributeNodesToGraph(graph, me, map);
		}
	}

	private boolean directEditHitTest(Point requestLoc) {
		IFigure header = ((SubgraphFigure) getFigure()).getHeader();
		header.translateToRelative(requestLoc);
		if (header.containsPoint(requestLoc))
			return true;
		return false;
	}

	/**
	 * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
	 */
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			if (request instanceof DirectEditRequest && !directEditHitTest(((DirectEditRequest) request).getLocation().getCopy()))
				return;
			performDirectEdit();
		}
	}

	int getAnchorOffset() {
		return -1;
	}

	public IFigure getContentPane() {
		if (getFigure() instanceof SubgraphFigure)
			return ((SubgraphFigure) getFigure()).getContents();
		return getFigure();
	}

	@SuppressWarnings("unchecked")
	protected List getModelChildren() {
		return getStructuredActivity().getChildren();
	}

	StructuredFSMState getStructuredActivity() {
		return (StructuredFSMState) getModel();
	}

	/**
	 * @see org.eclipse.gef.examples.flow.parts.FSMStatePart#performDirectEdit()
	 */
	protected void performDirectEdit() {
		if (manager == null) {
			Label l = ((Label) ((SubgraphFigure) getFigure()).getHeader());
			manager = new FSMStateDirectEditManager(this, TextCellEditor.class, new FSMStateCellEditorLocator(l), l);
		}
		manager.show();
	}

	@SuppressWarnings("unchecked")
	protected void applyOwnResults(CompoundDirectedGraph graph, Map map) { }

	/**
	 * @see org.eclipse.gef.examples.flow.parts.FSMStatePart#activate()
	 */
	public void activate() {
	        super.activate();
	        getViewer().getEditDomain().getCommandStack().addCommandStackListener(stackListener);
	}

	/**
	 * @see org.eclipse.gef.examples.flow.parts.FSMStatePart#createEditPolicies()
	 */
	protected void createEditPolicies() {
	        installEditPolicy(EditPolicy.NODE_ROLE, null);
	        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
	        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
	        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
	        installEditPolicy(EditPolicy.LAYOUT_ROLE, new StructuredActivityLayoutEditPolicy());
	        installEditPolicy(EditPolicy.CONTAINER_ROLE, new ActivityContainerEditPolicy());
	}

	protected IFigure createFigure() {
	        Figure f = new Figure() {
	                @Override
					public void setBounds(Rectangle rect) {
	                        int x = bounds.x,
	                            y = bounds.y;
	                
	                        boolean resize = (rect.width != bounds.width) || (rect.height != bounds.height),
	                                  translate = (rect.x != x) || (rect.y != y);
	                
	                        if (isVisible() && (resize || translate))
	                                erase();
	                        if (translate) {
	                                int dx = rect.x - x;
	                                int dy = rect.y - y;
	                                primTranslate(dx, dy);
	                        }
	                        bounds.width = rect.width;
	                        bounds.height = rect.height;
	                        if (resize || translate) {
	                                fireFigureMoved();
	                                repaint();
	                        }
	                }
	        };
	        f.setLayoutManager(new GraphLayoutManager(this));
	        return f;
	}
	/**
	 * @see org.eclipse.gef.examples.flow.parts.FSMStatePart#deactivate()
	 */
	public void deactivate() {
	        getViewer().getEditDomain().getCommandStack().removeCommandStackListener(stackListener);
	        super.deactivate();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
	 */
	public boolean isSelectable() {
	        return false;
	}

	/**
	 * @see org.eclipse.gef.examples.flow.parts.StructuredFSMStatePart#refreshVisuals()
	 */
	protected void refreshVisuals() {
	}

}
