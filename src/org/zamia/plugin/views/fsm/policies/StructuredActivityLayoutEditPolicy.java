/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.zamia.plugin.views.fsm.policies;

import java.util.List;


import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.commands.AddCommand;
import org.zamia.plugin.views.fsm.model.commands.CreateCommand;


/**
 * @author Daniel Lee
 */
public class StructuredActivityLayoutEditPolicy extends LayoutEditPolicy {

	protected Command createAddCommand(EditPart child) {
		FSMState activity = (FSMState) child.getModel();
		AddCommand add = new AddCommand();
		add.setParent((StructuredFSMState) getHost().getModel());
		add.setChild(activity);
		return add;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new NonResizableEditPolicy();
	}

	protected Command createMoveChildCommand(EditPart child, EditPart after) {
		return null;
	}

	@SuppressWarnings("unchecked")
	protected Command getAddCommand(Request req) {
		ChangeBoundsRequest request = (ChangeBoundsRequest) req;
		List editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		for (int i = 0; i < editParts.size(); i++) {
			EditPart child = (EditPart) editParts.get(i);
			command.add(createAddCommand(child));
		}
		return command.unwrap();
	}

	/**
	 * @see LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		CreateCommand command = new CreateCommand();
		command.setParent((StructuredFSMState) getHost().getModel());
		command.setChild((FSMState) request.getNewObject());
		return command;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getMoveChildrenCommand(org.eclipse.gef.Request)
	 */
	protected Command getMoveChildrenCommand(Request request) {
		return null;
	}

}
