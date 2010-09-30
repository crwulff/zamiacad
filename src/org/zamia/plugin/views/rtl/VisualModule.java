/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Aug 20, 2008
 */
package org.zamia.plugin.views.rtl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.plugin.views.rtl.symbols.Symbol;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.util.HashMapArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class VisualModule {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private RTLModule module;

	protected HashMapArray<String,VisualPort> ports; 

	public final static int INVALID_POS = -100000;

	private int col;
	private Channel vc;
	private int yPos;

	protected Symbol symbol;
	
	public VisualModule(RTLModule module_, RTLView control_) {

		module = module_;

		int n = module.getNumPorts();
		ports = new HashMapArray<String, VisualPort>(n);
		
		for (int i = 0; i<n; i++) {
			RTLPort p = module.getPort(i);
			VisualPort vp = new VisualPort(this, p);
			ports.put(p.getId(), vp);
		}

		symbol = SymbolRegistry.getInstance().getSymbol(module, control_);

		yPos = INVALID_POS;
	}

	public int getNumPorts() {
		return ports.size();
	}
	
	public VisualPort getPort(int idx_) {
		return ports.get(idx_);
	}

	public RTLModule getRTLModule() {
		return module;
	}

	public ArrayList<VisualModule> getSuccessors() {
		
		ArrayList<VisualModule> successors = new ArrayList<VisualModule>();
		
		int n = ports.size();
		for (int i = 0; i<n; i++) {
			
			VisualPort port = ports.get(i);
			if (port.getDirection() == PortDir.IN) {
				continue;
			}
			
			int m = port.getNumConnections();
			for (int j = 0; j<m; j++) {
				
				successors.add(port.getConnection(j).getModule());
			}
		}
		
		return successors;
	}

	public ArrayList<VisualModule> getPredecessors() {
		ArrayList<VisualModule> successors = new ArrayList<VisualModule>();
		
		int n = ports.size();
		for (int i = 0; i<n; i++) {
			
			VisualPort port = ports.get(i);
			if (port.getDirection() != PortDir.IN) {
				continue;
			}
			
			int m = port.getNumConnections();
			for (int j = 0; j<m; j++) {
				
				successors.add(port.getConnection(j).getModule());
			}
		}
		
		return successors;
	}

	public Position getPortOffset (RTLPort p_) {
		return symbol.getPortPosition (p_);
	}
	
	public Position getPortPosition(RTLPort p) {
		Position p1 = symbol.getPortPosition(p);
		if (p1 == null)
			return new Position(getXPos(), getYPos());
		return new Position(p1.x + getXPos(), p1.y + getYPos());
	}

	private boolean isVisible(RTLView viewer_) {
		int x1 = viewer_.tX(getXPos());
		if (x1 > RTLView.OFFSCREEN_WIDTH)
			return false;
		int y1 = viewer_.tY(getYPos());
		if (y1 > RTLView.OFFSCREEN_HEIGHT)
			return false;
		int w = symbol.getWidth();
		int x2 = x1 + viewer_.tW(w);
		if (x2 < 0)
			return false;
		int h = symbol.getHeight();
		int y2 = y1 + viewer_.tH(h);
		if (y2 < 0)
			return false;
		return true;
	}

	public void paint(RTLView viewer_, GC gc_, boolean hilight_) {
		if (isVisible(viewer_))
			symbol.paint(viewer_, gc_, module, getXPos(), getYPos(), hilight_);
	}
	
	public void print (PlaceAndRoute par_, PrintWriter out_, boolean selected_) throws IOException {
		symbol.print(par_, out_, module, getXPos(), getYPos(), selected_);
	}

	public boolean isHit(RTLView rtlc_, int x_, int y_) {
		int xp = rtlc_.tX(getXPos());
		int yp = rtlc_.tY(getYPos());
		int w = rtlc_.tW(symbol.getWidth());
		int h = rtlc_.tH(symbol.getHeight());
		return (x_ >= xp) && (y_ >= yp) && (x_ <= (xp + w)) && (y_ <= (yp + h));
	}
	
	public Symbol getSymbol(){
		return symbol;
	}

	
	public int getWidth () {
		return symbol.getWidth();
	}
	public int getHeight() {
		return symbol.getHeight();
	}

	public int getXPos() {
		
		if (vc == null) {
			logger.error("Module %s has not been placed.", module);
			return 0;
		}
		
		return vc.getModulesPos();
	}
	public int getCol() {
		return col;
	}
	public void setCol(int i) {
		col = i;
	}
	
	public Channel getVChannel() {
		return vc;
	}
	public void setVChannel(Channel vc_) {
		vc = vc_;
	}

	public void setYPos (int yPos_) {
		yPos = yPos_;
	}
	public int getYPos () {
		return yPos;
	}

	public String isPortHit(int mx, int my) {
		return symbol.isPortHit(mx, my,getXPos(), getYPos());
	}

	public VisualPort getPort(String id) {
		return ports.get(id);
	}

	public int getNumInputs() {
		int n = ports.size();
		int count = 0;
		for (int i = 0; i<n; i++) {
			VisualPort p = getPort(i);
			if (p.getDirection() != PortDir.IN)
				continue;
			count ++;
		}
		
		return count;
	}

	public int getNumOutputs() {
		int n = ports.size();
		int count = 0;
		for (int i = 0; i<n; i++) {
			VisualPort p = getPort(i);
			if (p.getDirection() == PortDir.IN)
				continue;
			count ++;
		}
		
		return count;
	}

	public Rectangle getRect(RTLView rtlc_) {
		int xp = getXPos();
		int yp = getYPos();
		int w = symbol.getWidth();
		int h = symbol.getHeight();
		return new Rectangle(xp,yp,w,h);
	}

}
