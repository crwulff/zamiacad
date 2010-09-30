/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 3, 2009
 */
package org.zamia.plugin.views.sim;

import java.math.BigInteger;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.util.Native;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class WaveformPaintJob extends Job {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private static final BigInteger TWENTYFIVE = BigInteger.valueOf(25);

	private static final BigInteger TEN = BigInteger.valueOf(10);

	private static final BigInteger TWO = BigInteger.valueOf(2);

	private static final long REPAINT_THRESHOLD = 1000; // milliseconds

	private SimulatorView fViewer;

	private IGISimulator fSim;

	private int fXOffset;

	private Display fDisplay;

	private BigInteger fStartTime;

	private BigInteger fEndTime;

	private Rectangle fClientArea;

	private int fVisibleWidth;

	private Color fBlack;

	private Color fWhite;

	private Color fGreen;

	private double fFSPerUnit;

	static class TreeItemInfo {

		private final int fYOffset, fHeight;

		private final TraceLine fTL;

		public TreeItemInfo(int aYOffset, int aHeight, TraceLine aTL) {
			fYOffset = aYOffset;
			fHeight = aHeight;
			fTL = aTL;
		}

		public int getYOffset() {
			return fYOffset;
		}

		public TraceLine getTL() {
			return fTL;
		}

		public int getHeight() {
			return fHeight;
		}

	}

	private ArrayList<TreeItemInfo> fTreeItems = new ArrayList<TreeItemInfo>();

	private IProgressMonitor fMonitor;

	private IGISimCursor fCursor;

	private int fHeadHeight;

	private int fOldYPos;

	private long fLastRepaint;

	// bad hack to fix d&d gtk redraw problem
	private static int tableCellHeight = -1;

	public WaveformPaintJob(SimulatorView aViewer, Rectangle aClientArea) {
		super("Paint waveform");

		fViewer = aViewer;
		fSim = fViewer.getSim();
		fXOffset = fViewer.getXOffset();
		fDisplay = fViewer.getDisplay();
		fStartTime = fViewer.getStartTime();
		fEndTime = fViewer.getEndTime();
		fClientArea = aClientArea;
		fVisibleWidth = fViewer.getVisibleWidth();
		fFSPerUnit = fViewer.getFSPerUnit();

		if (fSim != null) {
			fCursor = fSim.createCursor();
		}

		fLastRepaint = System.currentTimeMillis();
		
		Tree tree = fViewer.getTree();

		TreeItem items[] = tree.getItems();
		fHeadHeight = tree.getHeaderHeight();
		fOldYPos = Integer.MIN_VALUE;
		for (int i = 0; i < items.length; i++) {
			collectTreeItemsRek(items[i]);
		}

		fBlack = fDisplay.getSystemColor(SWT.COLOR_BLACK);
		fWhite = fDisplay.getSystemColor(SWT.COLOR_WHITE);
		fGreen = fDisplay.getSystemColor(SWT.COLOR_GREEN);
	}

	private void collectTreeItemsRek(TreeItem aTreeItem) {
		Rectangle r = aTreeItem.getBounds(0);

		int ypos = Native.isAIX() ? r.y : r.y + fHeadHeight;

		// bad hack to fix d&d gtk redraw problem

		if (ypos <= fOldYPos) {
			ypos = fOldYPos + tableCellHeight;
		} else if (fOldYPos >= 0) {
			int h = ypos - fOldYPos;
			if (tableCellHeight < h) {
				tableCellHeight = h;
			}
		}
		fOldYPos = ypos;

		if (ypos >= 0 && ypos <= fClientArea.height) {

			TraceLine tl = (TraceLine) aTreeItem.getData();

			fTreeItems.add(new TreeItemInfo(ypos, r.height, tl));
		}

		if (aTreeItem.getExpanded()) {
			int n = aTreeItem.getItemCount();
			for (int i = 0; i < n; i++) {
				TreeItem child = aTreeItem.getItem(i);
				collectTreeItemsRek(child);
			}
		}
	}

	private boolean isCanceled() {
		if (fMonitor == null)
			return false;
		return fMonitor.isCanceled();
	}

	@Override
	protected IStatus run(IProgressMonitor aMonitor) {

		fMonitor = aMonitor;

		GC gc = fViewer.resizeOffscreenImage(fClientArea);

		gc.setClipping(fClientArea);

		gc.setBackground(fBlack);
		gc.setForeground(fGreen);
		gc.fillRectangle(0, 0, fClientArea.width, fClientArea.height);

		if (fSim == null) {
			gc.drawText("No Data", 20, fClientArea.height / 2);
			return Status.OK_STATUS;
		}

		BigInteger fsPerUnit = BigInteger.valueOf((long) fFSPerUnit);

		BigInteger timeOffset = fViewer.tXI(fXOffset);
		BigInteger endTimeOffset = fViewer.tXI(fXOffset + fVisibleWidth);

		if (endTimeOffset.compareTo(fStartTime) < 0) {
			endTimeOffset = fStartTime;
		}
		if (timeOffset.compareTo(fStartTime) < 0) {
			timeOffset = fStartTime;
		}
		if (timeOffset.compareTo(fEndTime) > 0) {
			timeOffset = fEndTime;
		}

		// draw timeline
		gc.setClipping(fClientArea);
		gc.setBackground(fBlack);
		gc.fillRectangle(0, 0, fClientArea.width, 20);
		gc.setForeground(fWhite);

		// choose scale
		BigInteger majorScale = fsPerUnit;
		while (true) {

			int w = fViewer.tW(majorScale);
			if (w > 100) {
				break;
			}
			majorScale = majorScale.multiply(TWENTYFIVE).divide(TEN);
			w = fViewer.tW(majorScale);
			if (w > 100) {
				break;
			}
			majorScale = majorScale.multiply(TWO);
			w = fViewer.tW(majorScale);
			if (w > 100) {
				break;
			}
			majorScale = majorScale.multiply(TWO);
		}

		BigInteger minorScale = majorScale.divide(BigInteger.valueOf(5));

		BigInteger time = timeOffset.subtract(timeOffset.mod(majorScale));
		while (time.compareTo(endTimeOffset) <= 0) {
			int x = fViewer.tX(time) - fXOffset;

			if (time.mod(majorScale).signum() == 0) {
				gc.drawLine(x, 14, x, 19);
				BigInteger scaledTime = time.divide(fsPerUnit);
				String s = "" + scaledTime;
				gc.drawText(s, x - gc.textExtent(s).x / 2, 0);
			} else
				gc.drawLine(x, 19, x, 19);
			time = time.add(minorScale);
		}

		repaintViewer(false);

		/*
		 * repaint waveforms
		 */

		if (endTimeOffset.compareTo(fEndTime) > 0) {
			endTimeOffset = fEndTime;
		}

		int n = fTreeItems.size();
		for (int i = 0; i < n; i++) {

			TreeItemInfo tii = fTreeItems.get(i);

			TraceLine tl = tii.getTL();

			gc.setClipping(fClientArea.x, fClientArea.y + fHeadHeight, fClientArea.width, fClientArea.height - fHeadHeight);

			gc.setForeground(fGreen);
			gc.setBackground(fBlack);

			gc.fillRectangle(0, tii.getYOffset(), fClientArea.width, tii.getHeight());

			try {
				tl.draw(fCursor, gc, fXOffset, tii.getYOffset() + 2, fVisibleWidth, fHeadHeight, fStartTime, timeOffset, endTimeOffset, fViewer, aMonitor);
			} catch (Throwable t) {
				el.logException(t);
			}

			if (isCanceled()) {
				break;
			}

			repaintViewer(false);
		}
		
		repaintViewer(!isCanceled());
		
		fCursor.dispose();
		fCursor = null;

		return Status.OK_STATUS;
	}

	private void repaintViewer(boolean aForce) {
		
		if (!aForce) {
			long currentTime = System.currentTimeMillis();
			long diff = currentTime - fLastRepaint;
			if (diff < REPAINT_THRESHOLD) {
				return;
			}
			fLastRepaint = currentTime;
		}
		
		fViewer.syncedCanvasRedraw();
	}

}
