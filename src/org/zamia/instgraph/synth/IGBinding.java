/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 28, 2010
 */
package org.zamia.instgraph.synth;


import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLSignalAE;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.nodes.RTLNUnaryOp.UnaryOp;
import org.zamia.rtlng.nodes.RTLNRegister;

/**
 * 
 * @author Guenter Bartsch
 *
 */
public class IGBinding {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private final IGObject fObject;

	private IGClock fClock;

	private IGBindingNode fSyncBinding;

	private IGBindingNode fASyncBinding;

	public IGBinding(IGObject aObject) {
		fObject = aObject;
	}

	public IGClock getClock() {
		return fClock;
	}

	public void setClock(IGClock aClock) {
		fClock = aClock;
	}

	public IGObject getObject() {
		return fObject;
	}

	public void setSyncBinding(IGBindingNode syncBinding) {
		fSyncBinding = syncBinding;
	}

	public IGBindingNode getSyncBinding() {
		return fSyncBinding;
	}

	public void setASyncBinding(IGBindingNode aSyncBinding) {
		fASyncBinding = aSyncBinding;
	}

	public IGBindingNode getASyncBinding() {
		return fASyncBinding;
	}

	public void dump() {
		logger.debug("  IGBinding obj=%s clock=%s", fObject, fClock);
		logger.debug("    sync binding=");
		if (fSyncBinding != null) {
			fSyncBinding.dump(6);
		}

		logger.debug("    async binding=");
		if (fASyncBinding != null) {
			fASyncBinding.dump(6);
		}
	}

	public RTLSignal synthesize(IGSynth aSynth) throws ZamiaException {

		RTLSignal dest = aSynth.getCachedBindingSynth(this);
		if (dest != null) {
			return dest;
		}
		
		
		SourceLocation location = fObject.computeSourceLocation();
		
		RTLModule module = aSynth.getRTLModule();
		
		RTLType type = aSynth.synthesizeType(fObject.getType()); 
		
		if (fObject.getCat() == IGObjectCat.VARIABLE) {
			dest = module.createUnnamedSignal(type, location);
		} else {
			dest = aSynth.getOrCreateSignal(fObject);
		}
		
		RTLNRegister reg = module.createRegister(type, location);
		
		RTLPort z = reg.getZ();
		z.setSignal(dest);

		if (fASyncBinding != null) {
			RTLSignalAE asyncSAE = fASyncBinding.synthesize(aSynth);
			
			RTLPort data = reg.getASyncData();
			data.setSignal(asyncSAE.getSignal());
			
			RTLPort enable = reg.getASyncEnable();
			enable.setSignal(asyncSAE.getEnable());
		}
		
		if (fSyncBinding != null) {
			RTLSignalAE syncSAE = fSyncBinding.synthesize(aSynth);
			
			RTLPort data = reg.getSyncData();
			data.setSignal(syncSAE.getSignal());
			
			RTLPort enable = reg.getSyncEnable();
			enable.setSignal(syncSAE.getEnable());
		}
		
		RTLSignal clock = null;
		
		if (fClock != null) {
			clock = aSynth.getOrCreateSignal(fClock.getSignal());
			
			if (!fClock.isRisingEdge()) {
				clock = module.createComponentUnary(UnaryOp.NOT, clock, clock.computeSourceLocation());
			}
			
			RTLPort cp = reg.getClk();
			cp.setSignal(clock);
		}
		
		aSynth.setCachedBindingSynth(this, dest);
		
		return dest;
	}

}
