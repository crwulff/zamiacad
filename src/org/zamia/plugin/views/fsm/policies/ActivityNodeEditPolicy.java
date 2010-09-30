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
package org.zamia.plugin.views.fsm.policies;


import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.Transition;
import org.zamia.plugin.views.fsm.model.commands.ConnectionCreateCommand;
import org.zamia.plugin.views.fsm.model.commands.ReconnectSourceCommand;
import org.zamia.plugin.views.fsm.model.commands.ReconnectTargetCommand;
import org.zamia.plugin.views.fsm.parts.FSMStatePart;


/**
 * 
 * Created on Jul 17, 2003
 */
public class ActivityNodeEditPolicy extends GraphicalNodeEditPolicy {

/**
 * @see GraphicalNodeEditPolicy#getConnectionCompleteCommand(CreateConnectionRequest)
 */
protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
	ConnectionCreateCommand cmd = (ConnectionCreateCommand)request.getStartCommand();
	cmd.setTarget(getActivity());
	return cmd;
}

/**
 * @see GraphicalNodeEditPolicy#getConnectionCreateCommand(CreateConnectionRequest)
 */
protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
	ConnectionCreateCommand cmd = new ConnectionCreateCommand();
	cmd.setSource(getActivity());
	request.setStartCommand(cmd);
	return cmd;
}

/**
 * Returns the ActivityPart on which this EditPolicy is installed
 * @return the 
 */
protected FSMStatePart getActivityPart() {
	return (FSMStatePart) getHost();
}

/**
 * Returns the model associated with the EditPart on which this EditPolicy is installed
 * @return the model
 */
protected FSMState getActivity() {
	return (FSMState) getHost().getModel();
}

/**
 * @see GraphicalNodeEditPolicy#getReconnectSourceCommand(ReconnectRequest)
 */
protected Command getReconnectSourceCommand(ReconnectRequest request) {
	ReconnectSourceCommand cmd = new ReconnectSourceCommand();
	cmd.setTransition((Transition)request.getConnectionEditPart().getModel());
	cmd.setSource(getActivity());
	return cmd;
}

/**
 * @see GraphicalNodeEditPolicy#getReconnectTargetCommand(ReconnectRequest)
 */
protected Command getReconnectTargetCommand(ReconnectRequest request) {
	ReconnectTargetCommand cmd = new ReconnectTargetCommand();
	cmd.setTransition((Transition)request.getConnectionEditPart().getModel());
	cmd.setTarget(getActivity());
	return cmd;
}

}
