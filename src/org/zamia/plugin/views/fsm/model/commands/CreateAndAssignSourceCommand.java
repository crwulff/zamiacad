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
package org.zamia.plugin.views.fsm.model.commands;


import org.eclipse.gef.commands.Command;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.Transition;


/**
 * @author Daniel Lee
 */
public class CreateAndAssignSourceCommand extends Command {

private StructuredFSMState parent;
private FSMState child;
private FSMState source;
private Transition transition;

/**
 * @see org.eclipse.gef.commands.Command#execute()
 */
public void execute() {
	parent.addChild(child);
	transition = new Transition(source, child, null, false);
}


/**
 * @see org.eclipse.gef.commands.Command#redo()
 */
public void redo() {
	source.addOutput(transition);
	child.addInput(transition);
	parent.addChild(child);
}

/**
 * Sets the parent ActivityDiagram
 * @param sa the parent
 */
public void setParent(StructuredFSMState sa) {
	parent = sa;
}

/**
 * Sets the Activity to create
 * @param activity the Activity to create
 */
public void setChild(FSMState activity) {
	child = activity;
	child.setName("a " + (parent.getChildren().size() + 1));
}

/**
 * Sets the source to the passed activity
 * @param activity the source
 */
public void setSource(FSMState activity) {
	source = activity;
}

/**
 * @see org.eclipse.gef.commands.Command#undo()
 */
public void undo() {
	source.removeOutput(transition);
	child.removeInput(transition);
	parent.removeChild(child);
}

}
