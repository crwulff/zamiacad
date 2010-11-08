/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Sep 2, 2009
 */
package org.zamia.instgraph.sim.ref;

import java.math.BigInteger;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.interpreter.IGObjectWriter;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class IGSignalChangeRequest extends IGSimRequest {

	private IGObjectWriter fObjectWriter;
	private long fSignalDBID;
	private SourceLocation fLocation;
	private BigInteger fTime;

	public IGSignalChangeRequest(IGSimProcess aRuntime, long aSignalDBID, BigInteger aTime, IGObjectWriter aObjectWriter, SourceLocation aLocation) {
		super(aRuntime);
		fSignalDBID = aSignalDBID;
		fTime = aTime;
		fObjectWriter = aObjectWriter;
		fLocation = aLocation;
	}

	@Override
	public void execute(IGSimRef aSim) throws ZamiaException {
		if (isCanceled()) {
			return;
		}
		fObjectWriter.execute(fProcess);
	}

	public BigInteger getTime() {
		return fTime;
	}

	@Override
	public String toString() {
		return "@ " + fTime + ": " + fObjectWriter; //fObjectWriter.getObject().getId() + " <= " + fObjectWriter.getValue();
	}

	public IGStaticValue getValue() {
		return fObjectWriter.getValue();
	}
}
