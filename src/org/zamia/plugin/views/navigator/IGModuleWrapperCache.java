/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 16, 2009
 */
package org.zamia.plugin.views.navigator;

import java.util.HashMap;

import org.zamia.Toplevel;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGStructure;
import org.zamia.plugin.views.navigator.IGModuleWrapper.IGMWOp;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.DMUID;


/**
 * 	caching of wrappers is necessary to keep items expanded during navigator refresh
 *
 * @author Guenter Bartsch
 *
 */

public class IGModuleWrapperCache {

	private ZamiaProject fZPrj;
	
	private HashMap<DMUID, IGModuleWrapper> fBlueDUUIDWrappers = new HashMap<DMUID, IGModuleWrapper>();
	private HashMap<DMUID, IGModuleWrapper> fRedDUUIDWrappers = new HashMap<DMUID, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fLocalsWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fGlobalsWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fInstantiationWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fStructWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fProcessWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();
	private HashMap<ToplevelPath, IGModuleWrapper> fBlueWrappers = new HashMap<ToplevelPath, IGModuleWrapper>();

	public IGModuleWrapperCache(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
	}

	public IGModuleWrapper getRedWrapper(Toplevel aTL, DMUID aDUUID) {

		IGModuleWrapper wrapper = fRedDUUIDWrappers.get(aDUUID);
		if (wrapper != null) {
			return wrapper;
		}
		
		String signature = IGInstantiation.computeSignature(aDUUID, null);

		PathName path = new PathName("" + PathName.separator);
		ToplevelPath tlp = new ToplevelPath (aTL, path);

		wrapper = new IGModuleWrapper(IGMWOp.TOPRED, signature, aDUUID, tlp, this);
		
		fRedDUUIDWrappers.put(aDUUID, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getBlueWrapper(Toplevel aTL, DMUID aDUUID) {

		IGModuleWrapper wrapper = fBlueDUUIDWrappers.get(aDUUID);
		if (wrapper != null) {
			return wrapper;
		}
		
		String signature = IGInstantiation.computeSignature(aDUUID, null);

		PathName path = new PathName("" + PathName.separator);
		ToplevelPath tlp = new ToplevelPath (aTL, path);

		wrapper = new IGModuleWrapper(IGMWOp.BLUEIG, signature, aDUUID, tlp, this);
		
		fBlueDUUIDWrappers.put(aDUUID, wrapper);
		
		return wrapper;
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	public IGModuleWrapper getLocalsWrapper(IGModule aModule, ToplevelPath aPath) {

		IGModuleWrapper wrapper = fLocalsWrappers.get(aPath);
		if (wrapper != null && wrapper.getModule().getDBID() == aModule.getDBID()) {
			return wrapper;
		}
		
		wrapper = new IGModuleWrapper(IGMWOp.LOCALS, aModule, aPath, this);
		fLocalsWrappers.put(aPath, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getGlobalsWrapper(IGModule aModule, ToplevelPath aPath) {

		IGModuleWrapper wrapper = fGlobalsWrappers.get(aPath);
		if (wrapper != null && wrapper.getModule().getDBID() == aModule.getDBID()) {
			return wrapper;
		}
		
		wrapper = new IGModuleWrapper(IGMWOp.GLOBALS, aModule, aPath, this);
		fGlobalsWrappers.put(aPath, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getInstantiationWrapper(IGInstantiation aInst, ToplevelPath aTP) {
		
		IGModuleWrapper wrapper = fInstantiationWrappers.get(aTP);
		if (wrapper != null && wrapper.getInstantiation().getDBID() == aInst.getDBID()) {
			return wrapper;
		}
		
		wrapper = new IGModuleWrapper(aInst, aTP, this);
		
		fInstantiationWrappers.put(aTP, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getStructureWrapper(IGStructure aStruct, ToplevelPath aTP) {
		
		IGModuleWrapper wrapper = fStructWrappers.get(aTP);
		if (wrapper != null && wrapper.getStruct().getDBID() == aStruct.getDBID()) {
			return wrapper;
		}
		
		wrapper = new IGModuleWrapper(aStruct, aTP, this);
		
		fStructWrappers.put(aTP, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getProcessWrapper(IGProcess aProcess, ToplevelPath aTP) {
		
		IGModuleWrapper wrapper = fProcessWrappers.get(aTP);
		if (wrapper != null && wrapper.getProcess().getDBID() == aProcess.getDBID()) {
			return wrapper;
		}
		
		wrapper = new IGModuleWrapper(aProcess, aTP, this);
		
		fProcessWrappers.put(aTP, wrapper);
		
		return wrapper;
	}

	public IGModuleWrapper getBlueWrapper(IGInstantiation aInst, ToplevelPath aTP) {
		
		IGModuleWrapper wrapper = fBlueWrappers.get(aTP);

//		if (wrapper != null && wrapper.getInstantiation().getDBID() == aInst.getDBID()) {
//			return wrapper;
//		}
		
		DMUID duuid = aInst.getChildDUUID();

		wrapper = new IGModuleWrapper(IGMWOp.BLUEIG, aInst.getSignature(), duuid, aTP, this);
		fBlueWrappers.put(aTP, wrapper);

		return wrapper;
	}

}
