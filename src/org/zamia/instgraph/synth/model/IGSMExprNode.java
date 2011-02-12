/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 8, 2011
 */
package org.zamia.instgraph.synth.model;

import java.util.Set;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.synth.IGSynth;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLValue;

/**
 * @author Guenter Bartsch
 *
 */

public abstract class IGSMExprNode {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	protected static final IGSMExprEngine ee = IGSMExprEngine.getInstance();

	protected final RTLType fType;

	protected final SourceLocation fLocation;

	protected final IGSynth fSynth;

	protected IGSMExprNode(RTLType aType, SourceLocation aLocation, IGSynth aSynth) {
		fType = aType;
		fLocation = aLocation;
		fSynth = aSynth;
	}

	public RTLType getType() {
		return fType;
	}

	public SourceLocation getLocation() {
		return fLocation;
	}

	public IGSynth getSynth() {
		return fSynth;
	}

	public abstract RTLValue getStaticValue();

	public abstract IGSMExprNode replaceClockEdge(RTLSignal aClockSignal, RTLValue aValue, IGSynth aSynth) throws ZamiaException;

	public abstract void findClockEdges(Set<IGSMExprNodeClockEdge> aClockEdges) throws ZamiaException;

}
