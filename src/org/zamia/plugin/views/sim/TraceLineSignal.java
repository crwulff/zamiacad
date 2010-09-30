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
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.plugin.views.sim.SimulatorView.TraceDisplayMode;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.ASTObject.ASTErrorMode;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class TraceLineSignal extends TraceLine {

	public final static boolean ENABLE_SLOWDOWN = false;

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected PathName fSignalPath;

	protected TraceDisplayMode fTDM;

	protected IGTypeStatic fType;

	private IProgressMonitor fMonitor;

	private SimulatorView fViewer;

	private int fXOffset;

	private int fVisibleWidth;

	private int fHeadHeight;

	protected TraceLineSignal() {
	}

	public TraceLineSignal(PathName aSignalPath, TraceDisplayMode aTDM, int aColor, IGTypeStatic aType) {
		super(aSignalPath.getSegment(aSignalPath.getNumSegments()-1).toString(), aColor, "TLS:" + aSignalPath.toString());

		fSignalPath = aSignalPath;
		fTDM = aTDM;
		setType(aType);

		if (fType.isArray()) {
			try {
				fLabel = fLabel + " (" + fType.getStaticIndexType(null) + ")";
			} catch (ZamiaException e) {
				el.logException(e);
			}
		}
	}

	public PathName getSignalPath() {
		return fSignalPath;
	}

	public void setSignalPath(PathName aSignalPath) {
		fSignalPath = aSignalPath;
	}

	public TraceDisplayMode getTDM() {
		return fTDM;
	}

	public void setTDM(TraceDisplayMode aTdm) {
		fTDM = aTdm;
	}

	IGStaticValue getValue(IGISimCursor aCursor, BigInteger aCursorTime) {
		try {
			if (aCursor.gotoTransition(fSignalPath, aCursorTime)) {
				return aCursor.getCurrentValue();
			}
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public String getValueStr(IGISimCursor aCursor, BigInteger aCursorTime) {
		IGStaticValue value = getValue(aCursor, aCursorTime);
		if (value != null) {
			return formatSignalValue(value, fTDM);
		}
		return "???";
	}

	public static String formatSignalValue(IGStaticValue aValue, TraceDisplayMode aTDM) {
		try {
			IGTypeStatic t = aValue.getStaticType();
			if (t.isLogic() && !t.isArray()) {
				return aValue.toHRString();
			}

			try {
				if (t.isEnum() && !t.isCharEnum()) {

					long ord = aValue.getOrd();

					IGStaticValue l = t.getEnumLiteral((int) ord, null, ASTErrorMode.EXCEPTION, null);

					if (l != null && l.getId() != null) {
						return l.getId();
					}
				}
			} catch (Throwable e) {
			}

			String str;
			switch (aTDM) {
			case BIN:
				str = aValue.toBinString();
				break;
			case HEX:
				str = aValue.toHexString();
				break;
			case DEC:
				str = aValue.toDecString();
				break;
			case OCT:
				str = aValue.toOctString();
				break;
			default:
				str = aValue.toHexString();
				break;
			}

			return str;
		} catch (Throwable t) {
			return "???";
		}
	}

	public void setType(IGTypeStatic type) {
		fType = type;
	}

	public IGTypeStatic getType() {
		return fType;
	}

	@Override
	public void save(ArrayList<String> aTraces, int aNumChildren) {
		String str = "signal:" + fSignalPath + ":" + fColor + ":" + fTDM.name() + ":" + aNumChildren;
		aTraces.add(str);
	}

	@Override
	public boolean isFullSignal() {
		return true;
	}

	public BigInteger gotoPreviousTransition(IGISimCursor aCursor, BigInteger aTimeLimit) throws ZamiaException {
		return aCursor.gotoPreviousTransition(aTimeLimit);
	}

	public BigInteger gotoNextTransition(IGISimCursor aCursor, BigInteger aTimeLimit) throws ZamiaException {
		return aCursor.gotoNextTransition(aTimeLimit);
	}

	@Override
	public BigInteger findPreviousTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException {
		if (!aCursor.gotoTransition(getSignalPath(), aTime)) {
			return aTimeLimit;
		}
		
		BigInteger cursorTime = aCursor.getCurrentTime();
		if (cursorTime.compareTo(aTime)<0) {
			return cursorTime;
		}
		
		return gotoPreviousTransition(aCursor, aTimeLimit);
	}

	public BigInteger findNextTransition(IGISimCursor aCursor, BigInteger aTime, BigInteger aTimeLimit) throws ZamiaException {
		if (!aCursor.gotoTransition(getSignalPath(), aTime)) {
			return aTimeLimit;
		}
		return gotoNextTransition(aCursor, aTimeLimit);
	}

	public IGStaticValue getCurrentValue(IGISimCursor aCursor) throws ZamiaException {
		return aCursor.getCurrentValue();
	}

	@Override
	public void draw(IGISimCursor aCursor, GC aGC, int aXOffset, int aYOffset, int aVisibleWidth, int aHeadHeight, BigInteger aStartTime, BigInteger aTimeOffset,
			BigInteger aEndTime, SimulatorView aViewer, IProgressMonitor aMonitor) throws ZamiaException {

		fMonitor = aMonitor;
		fViewer = aViewer;
		fXOffset = aXOffset;
		fVisibleWidth = aVisibleWidth;
		fHeadHeight = aHeadHeight;

		aGC.setForeground(fViewer.getColor(fColor));

		BigInteger time = aTimeOffset.subtract(aViewer.tWI(SimulatorView.BORDER_WIDTH));
		if (time.compareTo(aStartTime) < 0) {
			time = aStartTime;
		}

		if (!aCursor.gotoTransition(fSignalPath, time)) {
			logger.error("Failed to find transition for signal '%s'", fSignalPath);
			return;
		}
		IGStaticValue cv = getCurrentValue(aCursor);

		BigInteger nextTime = gotoNextTransition(aCursor, aEndTime);
		IGStaticValue nv = getCurrentValue(aCursor);

		int y = -1;

		while (time.compareTo(aEndTime) < 0) {

			if (!nv.equals(cv) || nextTime.compareTo(aEndTime) >= 0) {
				y = drawValueBlock(aGC, cv, fTDM, time, nextTime, aYOffset, y);

				time = nextTime;
				cv = nv;
			}
			nextTime = gotoNextTransition(aCursor, aEndTime);
			nv = getCurrentValue(aCursor);

			if (ENABLE_SLOWDOWN) {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
				}
			}
			if (isCanceled()) {
				return;
			}
		}
		if (!isCanceled()) {
			drawValueBlock(aGC, cv, fTDM, time, aEndTime, aYOffset, y);
		}
	}

	private boolean isCanceled() {
		if (fMonitor != null) {
			return fMonitor.isCanceled();
		}
		return false;
	}

	private int drawValueBlock(GC aGC, IGStaticValue aValue, TraceDisplayMode aDisplayMode, BigInteger aStartTime, BigInteger aStopTime, int aYPos, int aOldYPos) {

		IGTypeStatic t = aValue.getStaticType();
		if (t.isLogic() && !t.isArray()) {
			return drawValueBlock(aGC, aValue.getCharLiteral(), aStartTime, aStopTime, aYPos, aOldYPos);
		} else {

			String str = formatSignalValue(aValue, aDisplayMode);
			drawValueBlock(aGC, str, aStartTime, aStopTime, aYPos);
			
		}
		return 0;
	}

	private int drawValueBlock(GC aGC, char aValue, BigInteger aStartTime, BigInteger aStopTime, int aYPos, int aOldYPos) {

		int oldYPos = aOldYPos;

		int x1, x2, h;
		x1 = fViewer.tX(aStartTime) - fXOffset;
		x2 = fViewer.tX(aStopTime) - fXOffset;

		int xmax = fVisibleWidth + SimulatorView.BORDER_WIDTH;

		if (x2 > xmax) {
			x2 = xmax;
		}

		h = aGC.getFontMetrics().getHeight();

		int cx = x1;
		if (cx < SimulatorView.BORDER_WIDTH)
			cx = SimulatorView.BORDER_WIDTH;

		int clipY = aYPos;
		int clipH = h + 1;

		if (clipY < fHeadHeight) {
			clipH -= (fHeadHeight - clipY);
			clipY = fHeadHeight;
			if (clipH < 0) {
				clipH = 0;
			}
		}

		aGC.setClipping(cx, clipY, x2 - x1, clipH);

		switch (aValue) {
		case IGStaticValue.BIT_0:
			if (oldYPos >= 0) {
				aGC.drawLine(x1, aYPos + oldYPos, x1, aYPos + h);
			}
			aGC.drawLine(x1, aYPos + h, x2, aYPos + h);
			oldYPos = h - 1;
			break;
		case IGStaticValue.BIT_1:
			if (oldYPos >= 0) {
				aGC.drawLine(x1, aYPos + oldYPos, x1, aYPos);
			}
			aGC.drawLine(x1, aYPos, x2, aYPos);
			oldYPos = 1;
			break;
		default:
			drawValueBlock(aGC, "" + aValue, aStartTime, aStopTime, aYPos);
			oldYPos = 1;
			break;
		}
		return oldYPos;
	}

	private void drawValueBlock(GC aGC, String aValue, BigInteger aStartTime, BigInteger aStopTime, int aYPos) {

		int x1, x2;
		x1 = fViewer.tX(aStartTime) - fXOffset;
		x2 = fViewer.tX(aStopTime) - fXOffset;

		boolean drawLeftHMark = true;
		boolean drawRightHMark = true;

		int xmax = fVisibleWidth + SimulatorView.BORDER_WIDTH;
		if (x2 > xmax + 3) {
			drawRightHMark = false;
			x2 = xmax;
		}
		int h = aGC.getFontMetrics().getHeight();

		if (x1 < SimulatorView.BORDER_WIDTH - 3) {
			x1 = SimulatorView.BORDER_WIDTH - 3;
			drawLeftHMark = false;
		}

		int w = x2 - x1;
		if (w < 1) {
			w = 1;
		}

		int clipY = aYPos;
		int clipH = h + 1;

		if (clipY < fHeadHeight) {
			clipH -= (fHeadHeight - clipY);
			clipY = fHeadHeight;
			if (clipH < 0) {
				clipH = 0;
			}
		}

		aGC.setClipping(x1 + 5, clipY, w - 10, clipH);

		int textXPos = x1 + 5;
		if (textXPos < SimulatorView.BORDER_WIDTH + 5) {
			textXPos = SimulatorView.BORDER_WIDTH + 5;
		}
		aGC.drawText(aValue, textXPos, aYPos);

		aGC.setClipping(x1, clipY, w + 1, clipH);

		aGC.drawLine(x1 + 3, aYPos, x2 - 3, aYPos);

		if (drawLeftHMark) {
			aGC.drawLine(x1 + 1, aYPos + h / 2, x1 + 3, aYPos);
			aGC.drawLine(x1 + 1, aYPos + h / 2, x1 + 3, aYPos + h);
		}

		aGC.drawLine(x1 + 3, aYPos + h, x2 - 3, aYPos + h);

		if (drawRightHMark) {
			aGC.drawLine(x2, aYPos + h / 2, x2 - 2, aYPos);
			aGC.drawLine(x2, aYPos + h / 2, x2 - 2, aYPos + h);
		}

		// aGC.drawRectangle(x, aYPos, w, h);
	}

	@Override
	public String getToolTip() {
		return fSignalPath.toString();
	}
}
