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
 * Command that handles the reconnection of source Activities.
 * 
 * @author Daniel Lee
 */
public class ReconnectSourceCommand extends Command {

	/** source Activity **/
	protected FSMState source;

	/** target Activity **/
	protected FSMState target;

	/** transition between source and target **/
	protected Transition transition;

	/** previous source prior to command execution **/
	protected FSMState oldSource;

	/**
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@SuppressWarnings("unchecked")
	public boolean canExecute() {
		if (transition.target.equals(source))
			return false;

		List transitions = source.getOutgoingTransitions();
		for (int i = 0; i < transitions.size(); i++) {
			Transition trans = ((Transition) (transitions.get(i)));
			if (trans.target.equals(target) && !trans.source.equals(oldSource))
				return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		if (source != null) {
			oldSource.removeOutput(transition);
			transition.source = source;
			source.addOutput(transition);
		}
	}

	/**
	 * Returns the source Activity associated with this command
	 * 
	 * @return the source Activity
	 */
	public FSMState getSource() {
		return source;
	}

	/**
	 * Returns the target Activity associated with this command
	 * 
	 * @return the target Activity
	 */
	public FSMState getTarget() {
		return target;
	}

	/**
	 * Returns the Transition associated with this command
	 * 
	 * @return the Transition
	 */
	public Transition getTransition() {
		return transition;
	}

	/**
	 * Sets the source Activity associated with this command
	 * 
	 * @param activity
	 *            the source Activity
	 */
	public void setSource(FSMState activity) {
		source = activity;
	}

	/**
	 * Sets the target Activity assoicated with this command
	 * 
	 * @param activity
	 *            the target Activity
	 */
	public void setTarget(FSMState activity) {
		target = activity;
	}

	/**
	 * Sets the transition associated with this
	 * 
	 * @param trans
	 *            the transition
	 */
	public void setTransition(Transition trans) {
		transition = trans;
		target = trans.target;
		oldSource = trans.source;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		source.removeOutput(transition);
		transition.source = oldSource;
		oldSource.addOutput(transition);
	}

}
