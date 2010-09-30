/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 25, 2009
 */
package org.zamia.plugin.views.sim;

import java.math.BigInteger;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.zamia.ZamiaException;
import org.zamia.instgraph.sim.IGISimCursor;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public abstract class TraceLine {

	protected String fLabel;

	protected int fColor;
	
	protected String fUID;
	
	protected TraceLine() {
	}
	
	public TraceLine(String aLabel, int aColor, String aUID) {
		setLabel(aLabel);
		setColor(aColor);
		fUID = aUID;
	}

	public void setLabel(String label) {
		fLabel = label;
	}

	public String getLabel() {
		return fLabel;
	}

	public void setColor(int color) {
		fColor = color;
	}

	public int getColor() {
		return fColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fUID == null) ? 0 : fUID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraceLine other = (TraceLine) obj;
		if (fUID == null) {
			if (other.fUID != null)
				return false;
		} else if (!fUID.equals(other.fUID))
			return false;
		return true;
	}

	public abstract String getValueStr(IGISimCursor aCursor, BigInteger aCursorTime);

	public String getUID() {
		return fUID;
	}

	public abstract void save(ArrayList<String> aTraces, int aNumChildren) ;

	public abstract boolean isFullSignal();

	public abstract void draw(IGISimCursor aCursor, GC aGC, int aXOffset, int aYOffset, int aVisibleWidth, int aHeadHeight, BigInteger aStartTime, BigInteger aTimeOffset, BigInteger aEndTime, SimulatorView aViewer, IProgressMonitor aMonitor) throws ZamiaException ;
	
	public abstract BigInteger findPreviousTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException;
	
	public abstract BigInteger findNextTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException;

	public String getToolTip() {
		return getLabel();
	}

}
