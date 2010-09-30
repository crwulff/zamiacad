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


import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.commands.DeleteCommand;


/**
 * @author Daniel Lee
 */
public class ActivityEditPolicy extends ComponentEditPolicy {

/**
 * @see ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
 */
protected Command createDeleteCommand(GroupRequest deleteRequest) {
	StructuredFSMState parent = (StructuredFSMState)(getHost().getParent().getModel());
	DeleteCommand deleteCmd = new DeleteCommand();
	deleteCmd.setParent(parent);
	deleteCmd.setChild((FSMState)(getHost().getModel()));
	return deleteCmd;
}

}
