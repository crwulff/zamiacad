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
package org.zamia.plugin.views.fsm.model.commands;


import org.eclipse.gef.commands.Command;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;


/**
 * AddCommand
 * @author Daniel Lee
 */
public class AddCommand extends Command {

private FSMState child;
private StructuredFSMState parent;

/**
 * @see org.eclipse.gef.commands.Command#execute()
 */
public void execute() {
	parent.addChild(child);
}

/**
 * Returns the StructuredActivity that is the parent
 * @return the parent
 */
public StructuredFSMState getParent() {
	return parent;
}

/**
 * Sets the child to the passed Activity
 * @param subpart the child
 */
public void setChild(FSMState newChild) {
	child = newChild;
}

/**
 * Sets the parent to the passed StructuredActiivty
 * @param newParent the parent
 */
public void setParent(StructuredFSMState newParent) {
	parent = newParent;
}

/**
 * @see org.eclipse.gef.commands.Command#undo()
 */
public void undo() {
	parent.removeChild(child);
}

}
