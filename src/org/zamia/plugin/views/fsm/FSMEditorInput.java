/*
 * Copyright 2007-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 11, 2007
 */

package org.zamia.plugin.views.fsm;

import java.util.HashMap;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.zamia.fsm.CondTransition;
import org.zamia.fsm.FSM;
import org.zamia.fsm.State;
import org.zamia.plugin.views.fsm.model.FSMState;
import org.zamia.plugin.views.fsm.model.StructuredFSMState;
import org.zamia.plugin.views.fsm.model.Transition;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class FSMEditorInput implements IEditorInput {

	private StructuredFSMState diagram;
	private FSM fsm;

	public FSMEditorInput(FSM fsm_) {
    	diagram = new StructuredFSMState();
        
    	
    	fsm = fsm_;
    	int n = fsm.getNumStates();
    	HashMap<State, FSMState> amap = new HashMap<State,FSMState>(n);
    	for (int i = 0; i<n; i++) {
    		State state = fsm.getState(i);
    		
    		FSMState activity = new FSMState (state.getId());
    		diagram.addChild(activity);
    		amap.put(state,activity);
    	}
    	
    	for (int i = 0; i<n; i++) {
    		State state = fsm.getState(i);
    		
    		FSMState source = amap.get(state);
    		int m = state.getNumCondTransitions();
    		for (int j = 0; j<m; j++) {
    			CondTransition ct = state.getCondTransition(j);
    			
    			State nextState = ct.getNextState();
    			System.out.println ("Transition from "+state+" to "+nextState);
    			
        		FSMState dest = amap.get(ct.getNextState());
    			
        		if (source == dest)
        			continue;
        		
    			new Transition(source, dest, ct.getCondition(), ct.isDefault());
    		}
    	}    	
	}
	
	public StructuredFSMState getDiagram() {
		return diagram;
	}
	
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Flow Chart";
	}

	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		return "Finit State Machine";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
