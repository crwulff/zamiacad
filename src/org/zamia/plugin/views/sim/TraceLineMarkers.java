/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 3, 2009
 */
package org.zamia.plugin.views.sim;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.zamia.ZamiaException;
import org.zamia.instgraph.sim.IGISimCursor;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceLineMarkers extends TraceLine {

	public final static int MARKER_WIDTH = 320;

	private TreeMap<BigInteger, TraceLineMarker> fMarkers;

	private IProgressMonitor fMonitor;

	public TraceLineMarkers(String aLabel, int aColor) {
		super(aLabel, aColor, "TLM:" + aLabel);
		fMarkers = new TreeMap<BigInteger, TraceLineMarker>();
	}

	public void addMarker(BigInteger aTime, String aLabel) {
		fMarkers.put(aTime, new TraceLineMarker(aTime, aLabel));
	}

	@Override
	public void draw(IGISimCursor aCursor, GC aGC, int aXOffset, int aYOffset, int aVisibleWidth, int aHeadHeight, BigInteger aStartTime, BigInteger aTimeOffset,
			BigInteger aEndTime, SimulatorView aViewer, IProgressMonitor aMonitor) throws ZamiaException {

		fMonitor = aMonitor;

		// draw background

		Color gray = aViewer.getColor(7);

		aGC.setForeground(gray);
		int h = aGC.getFontMetrics().getHeight() + 3;

		for (int y = aYOffset; y <= aYOffset + h; y += 4) {

			for (int x = SimulatorView.BORDER_WIDTH; x < SimulatorView.BORDER_WIDTH + aVisibleWidth; x += 4) {
				aGC.drawPoint(x, y);
			}
		}

		// invalidate all marker positions

		for (TraceLineMarker marker : fMarkers.values()) {
			marker.setX(Integer.MIN_VALUE);
		}

		// draw visible markers

		Image minusIcon = aViewer.getMinusIcon();

		int minusIconWidth = minusIcon.getBounds().width;

		aGC.setForeground(aViewer.getColor(fColor));
		aGC.setBackground(aViewer.getBlack());

		BigInteger time = aTimeOffset.subtract(aViewer.tWI(SimulatorView.BORDER_WIDTH + MARKER_WIDTH));
		if (time.compareTo(aStartTime) < 0) {
			time = aStartTime;
		}

		Entry<BigInteger, TraceLineMarker> markerEntry = fMarkers.ceilingEntry(time);

		while (time.compareTo(aEndTime) < 0 && markerEntry != null) {

			TraceLineMarker marker = markerEntry.getValue();

			int x = aViewer.tX(marker.getTime().subtract(aTimeOffset));
			marker.setX(x);

			Point box = aGC.textExtent(marker.getLabel());

			int width = box.x + 2 + minusIconWidth;
			marker.setWidth(width);

			aGC.fillRectangle(x, aYOffset, width, box.y + 2);

			aGC.drawText(marker.getLabel(), x + 1 + minusIconWidth, aYOffset + 1);

			aGC.drawImage(minusIcon, x + 1, aYOffset);

			aGC.drawRectangle(x, aYOffset, width, box.y + 2);

			time = marker.getTime().add(BigInteger.ONE);

			markerEntry = fMarkers.ceilingEntry(time);

			if (isCanceled()) {
				return;
			}
		}
	}

	private boolean isCanceled() {
		if (fMonitor != null) {
			return fMonitor.isCanceled();
		}
		return false;
	}

	@Override
	public String getValueStr(IGISimCursor aCursor, BigInteger aCursorTime) {
		return "";
	}

	@Override
	public boolean isFullSignal() {
		return false;
	}

	@Override
	public void save(ArrayList<String> aTraces, int aNumChildren) {
		int n = fMarkers.size();
		String str = "markers:" + fLabel + ":" + fColor + ":" + aNumChildren + ":" + n;
		aTraces.add(str);
		for (TraceLineMarker marker : fMarkers.values()) {
			str = "marker:" + marker.getLabel() + ":" + marker.getTime();
			aTraces.add(str);
		}
	}

	public TraceLineMarker findNearestMarker(int aX) {

		for (TraceLineMarker marker : fMarkers.values()) {

			int x = marker.getX();
			int w = marker.getWidth();

			if (aX >= x && aX <= x + w) {
				return marker;
			}
		}

		return null;
	}

	public void delete(TraceLineMarker aMarker) {
		fMarkers.remove(aMarker.getTime());
	}

	@Override
	public BigInteger findPreviousTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException {
		BigInteger time = aTime.subtract(BigInteger.ONE);

		Entry<BigInteger, TraceLineMarker> entry = fMarkers.floorEntry(time);
		if (entry == null) {
			return aTimeLimit;
		}

		return entry.getKey();
	}

	public BigInteger findNextTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException {
		BigInteger time = aTime.add(BigInteger.ONE);

		Entry<BigInteger, TraceLineMarker> entry = fMarkers.ceilingEntry(time);
		if (entry == null) {
			return aTimeLimit;
		}

		return entry.getKey();
	}

}
