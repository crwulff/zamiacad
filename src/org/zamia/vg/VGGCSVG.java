/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2010
 */
package org.zamia.vg;

import java.io.PrintWriter;

/**
 * VGGC implementation that produces an SVG file
 * @author Guenter Bartsch
 *
 */

public class VGGCSVG implements VGGC {

	private VGFont fFont;

	private final PrintWriter fOut;

	private int fLineWidth = 1;

	private VGColor fForeground = VGColor.SIGNAL;

	private VGColor fBackground = VGColor.BACKGROUND;

	public VGGCSVG(PrintWriter aOut) {
		fOut = aOut;
	}

	@Override
	public void start(int aWidth, int aHeight) {
		fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		fOut.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");

		fOut.println("<svg xmlns=\"http://www.w3.org/2000/svg\"");
		fOut.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ev=\"http://www.w3.org/2001/xml-events\"");
		fOut.println("     version=\"1.1\" baseProfile=\"full\"");
		fOut.println("     width=\"" + aWidth + "\" height=\"" + aHeight + "\">");
	}

	@Override
	public void finish() {
		fOut.println("</svg>");
	}

	@Override
	public void setFont(VGFont aFont) {
		fFont = aFont;
	}

	@Override
	public int getFontHeight() {

		switch (fFont) {
		case LARGE:
			return 18;
		case NORMAL:
			return 10;
		case SMALL:
			return 6;
		}

		return 10;
	}

	private int getFontBaseline() {

		switch (fFont) {
		case LARGE:
			return 5;
		case NORMAL:
			return 2;
		case SMALL:
			return 1;
		}

		return 2;
	}

	@Override
	public int textWidth(String aText) {

		// FIXME?

		int xw = 10;
		switch (fFont) {
		case LARGE:
			xw = 18;
		case NORMAL:
			xw = 10;
		case SMALL:
			xw = 6;
		}

		return aText.length() * xw;
	}

	@Override
	public void setLineWidth(int aWidth) {
		fLineWidth = aWidth;
	}

	@Override
	public void setForeground(VGColor aColor) {
		fForeground = aColor;
	}

	@Override
	public void setBackground(VGColor aColor) {
		fBackground = aColor;
	}

	private String getRGB(VGColor aColor) {
		switch (aColor) {
		case HIGHLIGHT:
			return "rgb(255,0,0)";

		case SIGNAL:
			return "rgb(0,0,0)";

		case BACKGROUND:
			return "rgb(255,255,255)";

		case MODULE:
			return "rgb(0,0,0)";

		case MODULE_LABEL:
			return "rgb(0,0,0)";

		}

		return "rgb(0,0,0)";
	}

	@Override
	public void drawLine(int aX1, int aY1, int aX2, int aY2) {

		String style = "stroke:" + getRGB(fForeground) + ";stroke-width:" + fLineWidth + ";";

		fOut.println("  <line x1=\"" + aX1 + "\" y1=\"" + aY1 + "\" x2=\"" + aX2 + "\" y2=\"" + aY2 + "\" style=\"" + style + "\" />");
	}

	@Override
	public void fillOval(int aX, int aY, int aXr, int aYr) {
		String style = "fill-opacity:1;" + "fill:" + getRGB(fBackground) + ";stroke:" + getRGB(fForeground) + ";stroke-width:" + fLineWidth + ";";
		fOut.println("  <ellipse cx=\"" + aX + "\" cy=\"" + aY + "\" rx=\"" + aXr + "\" ry=\"" + aYr + "\" style=\"" + style + "\" />");
	}

	@Override
	public void drawOval(int aX, int aY, int aXr, int aYr) {
		String style = "fill-opacity:0;" + "fill:" + getRGB(fBackground) + ";stroke:" + getRGB(fForeground) + ";stroke-width:" + fLineWidth + ";";
		fOut.println("  <ellipse cx=\"" + aX + "\" cy=\"" + aY + "\" rx=\"" + aXr + "\" ry=\"" + aYr + "\" style=\"" + style + "\" />");
	}

	private String encode(String aStr) {
		StringBuilder buf = new StringBuilder();

		int l = aStr.length();
		for (int i = 0; i < l; i++) {

			char c = aStr.charAt(i);

			switch (c) {
			case '<':
				buf.append("&lt;");
				break;
			case '>':
				buf.append("&gt;");
				break;
			case '"':
				buf.append("&quot;");
				break;
			case '\'':
				buf.append("&apos;");
				break;
			case '&':
				buf.append("&amp;");
				break;
			default:
				buf.append(c);
			}

		}
		
		return buf.toString();
	}

	@Override
	public void drawText(String aLabel, int aX, int aY, boolean aTransparent) {
		String style = "font-family:monospace;" + "font-size:" + getFontSize(fFont) + "px;fill-opacity:1;" + "fill:" + getRGB(fForeground) + ";stroke:" + getRGB(fForeground)
				+ ";stroke-width:0;";

		int y = aY - getFontBaseline();

		fOut.println("  <text x=\"" + aX + "\" y=\"" + y + "\" style=\"" + style + "\" >");

		String encoded = encode(aLabel);

		fOut.println("    " + encoded);
		fOut.println("  </text>");

		//drawRectangle(aX, aY-getFontHeight(), 200, getFontHeight());
	}

	private String getFontSize(VGFont aFont) {

		switch (fFont) {
		case LARGE:
			return "18";
		case NORMAL:
			return "10";
		case SMALL:
			return "6";
		}

		return "10";
	}

	@Override
	public void drawRectangle(int aX, int aY, int aW, int aH) {
		String style = "fill-opacity:0;" + "fill:" + getRGB(fBackground) + ";stroke:" + getRGB(fForeground) + ";stroke-width:" + fLineWidth + ";";
		fOut.println("  <rect x=\"" + aX + "\" y=\"" + aY + "\" width=\"" + aW + "\" height=\"" + aH + "\" style=\"" + style + "\" />");
	}

	@Override
	public void fillRectangle(int aX, int aY, int aW, int aH) {
		String style = "fill-opacity:1;" + "fill:" + getRGB(fBackground) + ";stroke:" + getRGB(fForeground) + ";stroke-width:" + fLineWidth + ";";
		fOut.println("  <rect x=\"" + aX + "\" y=\"" + aY + "\" width=\"" + aW + "\" height=\"" + aH + "\" style=\"" + style + "\" />");
	}

}
