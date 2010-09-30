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

import java.util.List;


import org.eclipse.gef.commands.Command;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.Transition;


/**
 * @author Daniel Lee
 */
public class ConnectionCreateCommand extends Command {

/** The Transistion between source and target Activities **/
protected Transition transition;
/** The source Activity **/
protected FSMState source;
/** The target Activity **/
protected FSMState target;

/**
 * @see org.eclipse.gef.commands.Command#canExecute()
 */
@SuppressWarnings("unchecked")
public boolean canExecute() {
	if (source.equals(target))
		return false;
	
	// Check for existence of connection already
	List transistions = source.getOutgoingTransitions();
	for (int i = 0; i < transistions.size(); i++) {
		if (((Transition)transistions.get(i)).target.equals(target))
			return false;
	}
	return true;
}


/**
 * @see org.eclipse.gef.commands.Command#execute()
 */
public void execute() {
	transition = new Transition(source, target, null, false);
}

/**
 * Returns the source Activity
 * @return the source
 */
public FSMState getSource() {
	return source;
}

/**
 * Returns the target Activity
 * @return the target
 */
public FSMState getTarget() {
	return target;
}

/**
 * Returns the Transistion between the source and target Activities
 * @return the transistion
 */
public Transition getTransition() {
	return transition;
}

/**
 * @see org.eclipse.gef.commands.Command#redo()
 */
public void redo() {
	source.addOutput(transition);
	target.addInput(transition);
}

/**
 * Sets the source Activity
 * @param activity the source Activity
 */
public void setSource(FSMState activity) {
	source = activity;
}

/**
 * Sets the Transistion between the source and target Activities
 * @param transition the transistion
 */
public void setTransition(Transition transition) {
	this.transition = transition;
}

/**
 * Sets the target Activity
 * @param activity the target
 */
public void setTarget(FSMState activity) {
	target = activity;
}

/**
 * @see org.eclipse.gef.commands.Command#undo()
 */
public void undo() {
	source.removeOutput(transition);
	target.removeInput(transition);
}

}
