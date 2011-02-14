/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 11, 2010
 */
package org.zamia.vg;

/**
 * A graphics context to paint a graph onto
 * 
 * @author Guenter Bartsch
 *
 */

public interface VGGC {

	public enum VGFont {
		SMALL, NORMAL, LARGE
	};

	public enum VGColor {
		HIGHLIGHT, SIGNAL, MODULE, BACKGROUND, MODULE_LABEL
	};

	public void start(int aWidth, int aHeight);

	public void finish();

	public void setFont(VGFont aFont);

	public int getFontHeight();

	public int textWidth(String aText);

	public void setLineWidth(int aWidth);

	public void setForeground(VGColor aColor);

	public void setBackground(VGColor aColor);

	public void drawLine(int aX1, int aY1, int aX2, int aY2);

	public void drawOval(int aX, int aY, int aXR, int aYR);

	public void fillOval(int aX, int aY, int aXR, int aYR);

	public void drawText(String aLabel, int aX, int aY, boolean aTransparent);

	public void drawRectangle(int aX, int aY, int aW, int aH);

	public void fillRectangle(int aX, int aY, int aW, int aH);
}
