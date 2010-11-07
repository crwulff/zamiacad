/*
 * Copyright 2007-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Mar 13, 2007
 */

package org.zamia.zil;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLSignalAE;
import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.VHDLNode;
import org.zamia.zil.interpreter.ZILInterpreterCode;
import org.zamia.zil.synthesis.Bindings;
import org.zamia.zil.synthesis.VariableRemapping;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public abstract class ZILTargetOperation extends ZILObject {

	public ZILTargetOperation(ZILType aType, ZILIContainer aContainer, VHDLNode aSrc) {
		super(aType, aContainer, aSrc);
	}

	public final RTLSignalAE elaborate(Bindings aLastBindings, RTLCache aCache) throws ZamiaException {
		RTLSignalAE result;

		//System.out.println ("Elaborating TargetOperation "+this);

		result = aCache.get(this);
		if (result != null) {
			//System.out.println ("  cached.");
			return result;
		}

		result = aCache.getGraph().createUnnamedSignalAE(getType(), getSrc());
		aCache.put(this, result);

		doElaborate(result, aLastBindings, aCache);

		aCache.put(this, result.getCurrent());

		return result;

	}

	protected abstract void doElaborate(RTLSignalAE aResult, Bindings aLastBindings, RTLCache aCache) throws ZamiaException;

	public abstract void generateCode(ZILInterpreterCode aCode, RTLCache aCache) throws ZamiaException;

	public abstract ZILType getSourceType();

	public abstract void setSource(ZILTargetOperation aSource) ;

	public abstract ZILTargetOperation getSource() ;

	public abstract ZILTargetOperation inlineSubprograms(VariableRemapping aVr, ZILSequenceOfStatements aSos, RTLCache aCache)
			throws ZamiaException;

	public abstract ZILTargetOperation resolveVariables(Bindings aVb, ZILSequenceOfStatements aSos, ZILClock aClk, RTLCache aCache) throws ZamiaException;

	public abstract void computeReadSignals(HashSetArray<ZILSignal> aReadSignals) ;

}
