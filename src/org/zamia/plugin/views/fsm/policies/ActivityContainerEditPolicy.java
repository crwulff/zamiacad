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
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.commands.OrphanChildCommand;


/**
 * ActivityContainerEditPolicy
 * 
 * @author Daniel Lee
 */
public class ActivityContainerEditPolicy extends ContainerEditPolicy {

	/**
	 * @see ContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getOrphanChildrenCommand(org.eclipse.gef.requests.GroupRequest)
	 */
	@SuppressWarnings("unchecked")
	protected Command getOrphanChildrenCommand(GroupRequest request) {
		List parts = request.getEditParts();
		CompoundCommand result = new CompoundCommand();
		for (int i = 0; i < parts.size(); i++) {
			OrphanChildCommand orphan = new OrphanChildCommand();
			orphan.setChild((FSMState) ((EditPart) parts.get(i)).getModel());
			orphan.setParent((StructuredFSMState) getHost().getModel());
			result.add(orphan);
		}
		return result.unwrap();
	}

}
