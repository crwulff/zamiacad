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
package org.zamia.plugin.views.fsm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A Structured activity is an activity whose execution is determined by some
 * internal structure.
 * 
 * @author hudsonr
 */
public class StructuredFSMState extends FSMState {

	static final long serialVersionUID = 1;

	private static int count;

	@SuppressWarnings("unchecked")
	protected List children = new ArrayList();

	public StructuredFSMState() {
	}

	public void addChild(FSMState child) {
		addChild(child, -1);
	}

	@SuppressWarnings("unchecked")
	public void addChild(FSMState child, int index) {
		if (index >= 0)
			children.add(index, child);
		else
			children.add(child);
		fireStructureChange(CHILDREN, child);
	}

	@SuppressWarnings("unchecked")
	public List getChildren() {
		return children;
	}

	public String getNewID() {
		return Integer.toString(count++);
	}

	public void removeChild(FSMElement child) {
		children.remove(child);
		fireStructureChange(CHILDREN, child);
	}

}
