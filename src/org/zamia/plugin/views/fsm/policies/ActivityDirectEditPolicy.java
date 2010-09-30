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


import org.eclipse.draw2d.Label;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.commands.RenameActivityCommand;


/**
 * EditPolicy for the direct editing of Activity names.
 * @author Daniel Lee
 */
public class ActivityDirectEditPolicy extends DirectEditPolicy {

/**
 * @see DirectEditPolicy#getDirectEditCommand(org.eclipse.gef.requests.DirectEditRequest)
 */
protected Command getDirectEditCommand(DirectEditRequest request) {
	RenameActivityCommand cmd = new RenameActivityCommand();
	cmd.setSource((FSMState)getHost().getModel());
	cmd.setOldName(((FSMState)getHost().getModel()).getId());
	cmd.setName((String)request.getCellEditor().getValue());
	return cmd;
}

/**
 * @see DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
 */
protected void showCurrentEditValue(DirectEditRequest request) {
	String value = (String)request.getCellEditor().getValue();
	((Label)getHostFigure()).setText(value);
	//hack to prevent async layout from placing the cell editor twice.
//	getHostFigure().getUpdateManager().performUpdate();
}

}
