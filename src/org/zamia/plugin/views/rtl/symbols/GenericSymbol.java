/*
 * Copyright 2004-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 * GenericSymbol.java created on 11.04.2004 by guenter
 */
package org.zamia.plugin.views.rtl.symbols;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.zamia.plugin.views.rtl.PSUtils;
import org.zamia.plugin.views.rtl.PlaceAndRoute;
import org.zamia.plugin.views.rtl.Position;
import org.zamia.plugin.views.rtl.RTLView;
import org.zamia.rtl.RTLArraySel;
import org.zamia.rtl.RTLComparator;
import org.zamia.rtl.RTLComponent;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLMux;
import org.zamia.rtl.RTLOperationLogic;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLRegister;
import org.zamia.rtl.RTLTargetArraySel;
import org.zamia.rtl.RTLTargetCond;
import org.zamia.rtl.RTLTargetEMux;
import org.zamia.rtl.RTLTargetRecordSel;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.vhdl.ast.OperationCompare.CompareOp;
import org.zamia.vhdl.ast.OperationLogic.LogicOp;
import org.zamia.zil.interpreter.ZILInterpreter;


/**
 * A generic symbol which represents any RTL module as a rectangular box,
 * Handles any number of input and output ports
 * 
 * 
 * @author Guenter Bartsch
 * 
 */
public class GenericSymbol extends Symbol {

	public final static int MIN_HEIGHT_CUSTOM = 660;

	public final static int MIN_WIDTH_CUSTOM = 400;

	public final static int MIN_HEIGHT_BUILTIN = 130;

	public final static int MIN_WIDTH_BUILTIN = 30;

	private int minHeight = MIN_HEIGHT_CUSTOM;

	private int minWidth = MIN_WIDTH_CUSTOM;

	public static final int PORT_DISTANCE_BUILTIN = 60;

	public static final int PORT_DISTANCE_CUSTOM = 60;

	private int portDistance = PORT_DISTANCE_CUSTOM;

	public static final int MAX_LABEL_LENGTH = 8;
	
	public final static int SHADOW_OFFSET = 20;

	static class HintedPort {
		RTLPort port;

		Position pos;

		int hint;

		public HintedPort(RTLPort p_, Position pos_) {
			port = p_;
			pos = pos_;
		}
	}

	private HashMap<RTLPort, Position> portPositions;

	private ArrayList<HintedPort> inputPorts;

	private ArrayList<HintedPort> outputPorts;

	private HashMap<RTLPort, HintedPort> hintMap;

	private int width, height;

	private String label;

	private RTLView control;

	private boolean builtin, showPins;

	private Font bigFont, normalFont;

	public GenericSymbol(RTLModule module_, RTLView control_) {
		super();

		builtin = !(module_ instanceof RTLGraph) && !(module_ instanceof RTLComponent);
		showPins = control_.isShowPins();

		portDistance = builtin ? PORT_DISTANCE_BUILTIN : PORT_DISTANCE_CUSTOM;
		minHeight = builtin ? MIN_HEIGHT_BUILTIN : MIN_HEIGHT_CUSTOM;
		minWidth = builtin ? MIN_WIDTH_BUILTIN : MIN_WIDTH_CUSTOM;

		width = minWidth;
		height = minHeight;
		control = control_;

		updateFonts();
		getLabel(module_);
		calcPortPositions(module_);
	}

	private void updateFonts() {
		if (builtin) {
			bigFont = control.getNormalFont();
			normalFont = control.getSmallFont();
		} else {
			bigFont = control.getBigFont();
			normalFont = control.getSmallFont();
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private void calcPortPositions(RTLModule module) {
		portPositions = new HashMap<RTLPort, Position>();
		inputPorts = new ArrayList<HintedPort>();
		outputPorts = new ArrayList<HintedPort>();
		hintMap = new HashMap<RTLPort, HintedPort>();
		int yi = portDistance * 1 / 4; // current input port position
		int yo = portDistance * 1 / 4; // current output port position
		int maxipw = 0;
		int maxopw = 0;

		double z = control.getZoomFactor();

		GC gc = new GC(control.getViewSite().getShell());

		gc.setFont(normalFont);

		for (int i = 0; i < module.getNumPorts(); i++) {
			RTLPort p = module.getPort(i);
			Position pos;
			String id = p.getId();
			int w = (int) (gc.textExtent(id).x / z);
			HintedPort hp;
			if (p.getDirection() != PortDir.OUT) {
				portPositions.put(p, pos = new Position(0, yi));
				if (showPins)
					yi += portDistance;
				hp = new HintedPort(p, pos);
				inputPorts.add(hp);
				if (w > maxipw)
					maxipw = w;
			} else {
				portPositions.put(p, pos = new Position(1, yo));
				if (showPins)
					yo += portDistance;
				hp = new HintedPort(p, pos);
				outputPorts.add(hp);
				if (w > maxopw)
					maxopw = w;
			}
			hintMap.put(p, hp);
		}

		gc.setFont(bigFont);

		int fontSize = (int) (gc.getFontMetrics().getHeight() / z);
		if (builtin)
			fontSize = 0;

		// System.out.println("Font size: "+fontSize);

		height = Math.max(yi - portDistance / 2, yo - portDistance / 2) + fontSize;
		width = height * 100 / 162;

		// System.out.println ("Size of "+module.getInstanceName()+":
		// "+gc.textExtent(module.getInstanceName()));

		int l = gc.textExtent(label).x;
		if (builtin)
			l = (int) (l / z) + maxopw + maxipw + 2*portDistance;
		else
			l = Math.max((int) (l / z), maxopw + maxipw + 2*portDistance);
		l = l + portDistance;
		if (width < l)
			width = l;

		if (width < minWidth)
			width = minWidth;
		if (height < minHeight)
			height = minHeight;

		for (Iterator<Position> i = portPositions.values().iterator(); i.hasNext();) {
			Position pos = i.next();
			if (pos.x > 0)
				pos.x = width - portDistance;
		}

		if (!builtin) {
			width += SHADOW_OFFSET;
			height += SHADOW_OFFSET;
		}
		
		gc.dispose();
	}

	public void setPinHint(RTLPort p_, int hint_) {
		if (!showPins)
			return;
		HintedPort hp = hintMap.get(p_);
		hp.hint = hint_;
	}

	public void sortPins() {
		Collections.sort(inputPorts, new Comparator<HintedPort>() {
			public int compare(HintedPort hp1, HintedPort hp2) {
				return hp1.hint - hp2.hint;
			}
		});
		int n = inputPorts.size();
		int yo = portDistance * 1 / 4;
		for (int i = 0; i < n; i++) {
			HintedPort hp = inputPorts.get(i);
			hp.pos.y = yo;
			yo += portDistance;
		}

		Collections.sort(outputPorts, new Comparator<HintedPort>() {
			public int compare(HintedPort hp1, HintedPort hp2) {
				return hp1.hint - hp2.hint;
			}
		});
		n = outputPorts.size();
		yo = portDistance * 1 / 4;
		for (int i = 0; i < n; i++) {
			HintedPort hp = outputPorts.get(i);
			hp.pos.y = yo;
			yo += portDistance;
		}
	}

	public Position getPortPosition(RTLPort p) {
		return portPositions.get(p);
	}

	public void paint(RTLView v_, GC gc, RTLModule module_, int xpos, int ypos, boolean hilight) {

		updateFonts();

		Font oldfont = gc.getFont();

		gc.setFont(normalFont);

		int w = tW(v_, getWidth());
		if (w < 1)
			w = 1;

		// System.out.println ("Width of "+module_+": "+w+" (scaled from
		// "+getWidth()+")");
		// System.out.println ("Height of "+module_+": "+getHeight());

		// gc.setXORMode(true);
		// gc.setBackground(v_.getColorScheme().getHilightColor());
		// gc.fillRectangle(tX(v_, 0, xpos), tY(v_, 0, ypos), tW(v_,
		// getWidth()),
		// tH(v_, getHeight()));
		// gc.setXORMode(false);

		if (hilight) {
			gc.setForeground(v_.getColorScheme().getHilightColor());
			gc.setLineWidth((int) (4 * v_.getZoomFactor()));
		} else {
			gc.setForeground(v_.getColorScheme().getModuleColor());
			gc.setLineWidth((int) (2 * v_.getZoomFactor()));
		}

		gc.setFont(bigFont);

		int th = gc.getFontMetrics().getHeight();
		if (builtin)
			th = 0;
		if (builtin) {
			gc.drawRectangle(tX(v_, portDistance / 2, xpos), tY(v_, 0, ypos), tW(v_, getWidth() - portDistance*2), tH(v_,
					getHeight())
					- th - 1);
		} else {
			gc.setBackground(v_.getColorScheme().getShadowColor());
			gc.setForeground(v_.getColorScheme().getShadowColor());
			gc.fillRectangle(tX(v_, portDistance / 2, xpos + SHADOW_OFFSET), tY(v_, 0, ypos + SHADOW_OFFSET),
					tW(v_, getWidth() - portDistance*2 - SHADOW_OFFSET) - 1, tH(v_, getHeight() - SHADOW_OFFSET) - th - 1);
			gc.setBackground(v_.getColorScheme().getBgColor());
			if (hilight) {
				gc.setForeground(v_.getColorScheme().getHilightColor());
			} else {
				gc.setForeground(v_.getColorScheme().getModuleColor());
			}
			gc.fillRectangle(tX(v_, portDistance / 2, xpos), tY(v_, 0, ypos), tW(v_, getWidth() - portDistance*2 - SHADOW_OFFSET),
					tH(v_, getHeight() - SHADOW_OFFSET) - th - 1);
			gc.drawRectangle(tX(v_, portDistance / 2, xpos), tY(v_, 0, ypos), tW(v_, getWidth() - portDistance*2 - SHADOW_OFFSET) - 1,
					tH(v_, getHeight() - SHADOW_OFFSET) - th - 2);
		}

		if (w < 20)
			return;

		// draw names for hierarchical subs
		if (!hilight) {
			gc.setForeground(v_.getColorScheme().getModuleLabelColor());
		}

		if (!builtin) {
			gc.drawText(label, tX(v_, portDistance / 2 + 4, xpos), tY(v_, getHeight(), ypos) - th, true);
		}

		gc.setFont(normalFont);
		th = gc.getFontMetrics().getHeight();

		if (showPins) {
			// draw ports
			for (Iterator<RTLPort> i = portPositions.keySet().iterator(); i.hasNext();) {
				RTLPort p = i.next();
				Position pos = portPositions.get(p);

				String id;
				id = p.getId();

				if (!p.getType().isBit())
					gc.setLineWidth((int) (4 * v_.getZoomFactor()));
				else
					gc.setLineWidth((int) v_.getZoomFactor());

				if (p.getDirection() != PortDir.OUT) {
					gc.drawLine(tX(v_, 0, xpos), tY(v_, pos.y, ypos), tX(v_, portDistance / 2, xpos), tY(v_, pos.y,
							ypos));
					gc.drawText(id, tX(v_, portDistance / 2 + 4, xpos), tY(v_, pos.y, ypos) - th / 2, true);
				} else {
					gc.drawLine(tX(v_, pos.x - portDistance / 2, xpos), tY(v_, pos.y, ypos), tX(v_, pos.x, xpos), tY(
							v_, pos.y, ypos));
					int ww = gc.textExtent(id).x;
					gc
							.drawText(id, tX(v_, pos.x - portDistance / 2 - 4, xpos) - ww,
									tY(v_, pos.y, ypos) - th / 2, true);
				}
			}
			// draw port values
			int fontSize = tW(v_, 4.0);

			if (fontSize > 3) {

				if (!hilight)
					gc.setForeground(v_.getColorScheme().getAnnotationColor());

				/*
				 * FIXME FIXME: sim disabled for now
				 * 
				 * Font font = new Font(v_.getDisplay(), "Sans", fontSize,
				 * SWT.NONE); gc.setFont(font);
				 * 
				 * 
				 * Simulator sim = v_.findSimulator();
				 * 
				 * for (Iterator<RTLPort> i =
				 * portPositions.keySet().iterator(); i .hasNext();) { RTLPort p =
				 * i.next(); Position pos = portPositions.get(p);
				 * 
				 * Value v; try { v = sim.getValue(p); String label =
				 * v.toString();
				 * 
				 * if (p.getDirection() == PortDir.OUT) { int ww =
				 * gc.textExtent(label).x; gc.drawText(label, tX(v_, pos.x,
				 * xpos)-ww, tY(v_, pos.y - 8, ypos), true); } else {
				 * gc.drawText(label, tX(v_, pos.x, xpos), tY(v_, pos.y - 8,
				 * ypos), true);
				 *  } } catch (ZamiaException e) { } } font.dispose();
				 */
			}
		} else {
			// draw a signgle input and output
			
			if (portPositions.size()>0) {
				int yi = portDistance * 1 / 4; // current input port position
			
				gc.drawLine(tX(v_, 0, xpos), tY(v_, yi, ypos), tX(v_, portDistance / 2, xpos), tY(v_, yi,
						ypos));
				gc.drawLine(tX(v_, width - portDistance / 2, xpos), tY(v_, yi, ypos), tX(v_, width, xpos), tY(
						v_, yi, ypos));
			}
			// draw port values
			int fontSize = tW(v_, 4.0);

			if (fontSize > 3) {

				if (!hilight)
					gc.setForeground(v_.getColorScheme().getAnnotationColor());

				/*
				 * FIXME FIXME: sim disabled for now
				 * 
				 * Font font = new Font(v_.getDisplay(), "Sans", fontSize,
				 * SWT.NONE); gc.setFont(font);
				 * 
				 * 
				 * Simulator sim = v_.findSimulator();
				 * 
				 * for (Iterator<RTLPort> i =
				 * portPositions.keySet().iterator(); i .hasNext();) { RTLPort p =
				 * i.next(); Position pos = portPositions.get(p);
				 * 
				 * Value v; try { v = sim.getValue(p); String label =
				 * v.toString();
				 * 
				 * if (p.getDirection() == PortDir.OUT) { int ww =
				 * gc.textExtent(label).x; gc.drawText(label, tX(v_, pos.x,
				 * xpos)-ww, tY(v_, pos.y - 8, ypos), true); } else {
				 * gc.drawText(label, tX(v_, pos.x, xpos), tY(v_, pos.y - 8,
				 * ypos), true);
				 *  } } catch (ZamiaException e) { } } font.dispose();
				 */
			}
			
		}

		if (label != null && builtin) {

			gc.setFont(bigFont);

			int fontSize = gc.getFontMetrics().getHeight();

			if (!hilight)
				gc.setForeground(v_.getColorScheme().getModuleLabelColor());

			int ww = gc.textExtent(label).x;
			gc.drawText(label, tX(v_, getWidth() / 2, xpos) - ww / 2, tY(v_, getHeight() / 2, ypos) - fontSize / 2,
					true);
		}

		gc.setFont(oldfont);
	}

	public void print(PlaceAndRoute par_, PrintWriter out_, RTLModule module_, int xpos, int ypos, boolean selected_)
			throws IOException {

		// double w = v_.tWPrint(getWidth());

		PSUtils.psDrawRectangle(par_, out_, xpos + portDistance / 2, ypos, getWidth() - portDistance,
				getHeight() - 10 - 1, selected_);

		// draw names for hierarchical subdesigns

		PSUtils.psDrawText(par_, out_, portDistance / 2 + 4 + xpos, getHeight() - 10 + ypos, module_.getInstanceName(),
				10);
		// PSUtils.psDrawText(par_, out_, 11 + xpos, getHeight() - 30 + ypos -
		// 10, module_.getClassName(), 10);

		// draw ports
		for (Iterator<RTLPort> i = portPositions.keySet().iterator(); i.hasNext();) {

			RTLPort p = i.next();
			Position pos = (Position) portPositions.get(p);

			String id = p.getId();

			double w = 4.0;

			if (p.getType().isBit())
				w = 1.0;

			if (p.getDirection() != PortDir.OUT) {
				PSUtils.psDrawLine(par_, out_, xpos, pos.y + ypos, portDistance / 2 + xpos, pos.y + ypos, w);
				PSUtils.psDrawText(par_, out_, portDistance / 2 + 4 + xpos, pos.y + ypos - 5, id, 10);
			} else {
				PSUtils.psDrawLine(par_, out_, pos.x - portDistance / 2 + xpos, pos.y + ypos, pos.x + xpos, pos.y
						+ ypos, w);
				// PSUtils.psDrawTextRight(par_, out_, pos.x - PORT_DISTANCE / 2
				// - 4 + xpos, pos.y + ypos - 5, id, 10);
				PSUtils.psDrawText(par_, out_, pos.x - portDistance / 2 - 4 + xpos - id.length() * 8, pos.y + ypos - 5,
						id, 10);
			}
		}
	}

	@Override
	public String isPortHit(int mx, int my, int xpos, int ypos) {
		for (Iterator<Entry<RTLPort, Position>> i = portPositions.entrySet().iterator(); i.hasNext();) {
			Entry<RTLPort, Position> e = i.next();
			Position pos = e.getValue();
			RTLPort pb = e.getKey();
			int size = 4;
			int x = pos.x;
			if (x == 0)
				x = 10;
			else
				x -= 10;
			int y = pos.y;

			if (Math.abs(mx - xpos - x) < size && Math.abs(my - ypos - y) < size)
				return pb.getId();

		}
		return null;
	}

	public void tweakPortPosition(RTLPort p_) {
		Position pos = portPositions.get(p_);
		pos.y += portDistance / 8;
		if (pos.y > getHeight() - (portDistance / 2)) {
			height += portDistance / 8;
		}
	}

	private void getLabel(RTLModule module_) {
		// special label for some RTL modules
		label = null;
		if (module_ instanceof RTLTargetCond) {
			label = "Cond";
		} else if (module_ instanceof RTLTargetEMux) {
			label = "Mux";
		} else if (module_ instanceof RTLMux) {
			label = "Mux";
		} else if (module_ instanceof RTLTargetArraySel) {
			label = "[ ]";
		} else if (module_ instanceof RTLArraySel) {
			label = "[ ]";
		} else if (module_ instanceof RTLRegister) {
			label = "Reg";
		} else if (module_ instanceof ZILInterpreter) {
			label = "VM";
		} else if (module_ instanceof RTLTargetRecordSel) {
			RTLTargetRecordSel sel = (RTLTargetRecordSel) module_;
			label = "." + sel.getId();
		} else if (module_ instanceof RTLOperationLogic) {

			RTLOperationLogic rtlol = (RTLOperationLogic) module_;
			LogicOp op = rtlol.getOp();
			switch (op) {
			case AND:
				label = "AND";
				break;
			case BUF:
				label = "BUF";
				break;
			case NAND:
				label = "NAND";
				break;
			case NOR:
				label = "NOR";
				break;
			case NOT:
				label = "NOT";
				break;
			case OR:
				label = "OR";
				break;
			case XNOR:
				label = "XNOR";
				break;
			case XOR:
				label = "XOR";
				break;
			}
		} else if (module_ instanceof RTLComparator) {
			RTLComparator cmp = (RTLComparator) module_;
			CompareOp op = cmp.getOp();
			switch (op) {
			case EQUAL:
				label = "=";
				break;
			case GREATER:
				label = ">";
				break;
			case GREATEREQ:
				label = ">=";
				break;
			case LESS:
				label = "<";
				break;
			case LESSEQ:
				label = "<=";
				break;
			case NEQUAL:
				label = "/=";
				break;
			}
		} else {
			label = module_.getInstanceName();
			if (label.length() > MAX_LABEL_LENGTH)
				label = label.substring(0, MAX_LABEL_LENGTH);
		}
	}
}
