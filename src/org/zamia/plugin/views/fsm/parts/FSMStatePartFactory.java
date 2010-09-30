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


import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.Transition;


/**
 * @author hudsonr Created on Jul 16, 2003
 */
public class FSMStatePartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof StructuredFSMState)
			part = new StructuredFSMStatePart();
		else if (model instanceof FSMState)
			part = new SimpleFSMStatePart();
		else if (model instanceof Transition)
			part = new TransitionPart();
		part.setModel(model);
		return part;
	}

}
