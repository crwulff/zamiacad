/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

/*
 * Created on Apr 21, 2005
 *
 */
package org.zamia.plugin.views.rtl;

import java.io.PrintWriter;

/**
 * @author guenter bartsch
 * 
 * a few static utility functions to make postscript output generation a little
 * easier
 *  
 */
public class PSUtils {

	private PSUtils() {

	}

	public static void psDrawLine(PlaceAndRoute par_, PrintWriter out_, double sx_,
			double sy_, double dx_, double dy_, double width_) {

		out_.println("newpath");
		
		double w;
		w = par_.tWPrint (width_);
		
		out_.println(w+" setlinewidth");

		double x1, y1, x2, y2;
		x1 = par_.tXPrint(sx_);
		y1 = par_.tYPrint(sy_);
		x2 = par_.tXPrint(dx_);
		y2 = par_.tYPrint(dy_);

		out_.println("" + x1 + " " + y1 + " moveto");
		out_.println("" + x2 + " " + y2 + " lineto");
		out_.println("stroke");
	}

	public static void psDrawArc(PlaceAndRoute par_, PrintWriter out_, double x_,
			double y_, double r_, int startangle_, int stopangle_,
			boolean selected_) {

		out_.println("newpath");
		double w;
		if (selected_)
			w = par_.tWPrint (3.0);
		else
			w = par_.tWPrint (1.0);
		
		out_.println(w+" setlinewidth");

		double x, y;
		x = par_.tXPrint(x_);
		y = par_.tYPrint(y_);
		double r = par_.tWPrint(r_);

		out_.println("" + x + " " + y + " " + r + " " + startangle_ + " "
				+ stopangle_ + " arc");
		out_.println("stroke");
	}

	public static void psDrawTextRight(PlaceAndRoute par_, PrintWriter out_,
			double sx_, double sy_, String str_, double fontSize_) {

		double fontSize = par_.tHPrint(fontSize_);

		out_.println("/Helvetica findfont");
		out_.println("" + fontSize + " scalefont");
		out_.println("setfont");

		double x1, y1;
		x1 = par_.tXPrint(sx_);
		y1 = par_.tYPrint(sy_) - fontSize ;

		out_.println("" + x1 + " " + y1 + " moveto");
		out_.println("(" + encodeText(str_) + ") rightshow");
	}

	private static String encodeText(String str_) {
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < str_.length(); i++) {
			char c = str_.charAt(i);
			res.append('\\');
			//1100 0000
			//0011 1000
			//0000 0111
			char co = (char) ('0' + (char) ((c & 0xC0) >> 6));
			res.append(co);
			co = (char) ('0' + ((c & 0x38) >> 3));
			res.append(co);
			co = (char) ('0' + (c & 0x07));
			res.append(co);
		}

		return res.toString();
	}

	public static void psDrawText(PlaceAndRoute par_, PrintWriter out_, double sx_,
			double sy_, String str_, double fontSize_) {

		double fontSize = par_.tHPrint(fontSize_);

		out_.println("/Helvetica findfont");
		out_.println("" + fontSize + " scalefont");
		out_.println("setfont");

		double x1, y1;
		x1 = par_.tXPrint(sx_);
		y1 = par_.tYPrint(sy_) - fontSize;

		out_.println("" + x1 + " " + y1 + " moveto");
		out_.println("(" + encodeText(str_) + ") show");
	}

	public static void psNewPath(PlaceAndRoute par_, PrintWriter out_, boolean selected_) {
		
		out_.println("newpath");
		
		double w;
		if (selected_)
			w = par_.tWPrint (3.0);
		else
			w = par_.tWPrint (1.0);
		
		out_.println(w+" setlinewidth");
	}

	public static void psMoveTo(PlaceAndRoute par_, PrintWriter out_, double x_, double y_) {
		double x, y;
		x = par_.tXPrint(x_);
		y = par_.tYPrint(y_);

		out_.println("" + x + " " + y + " moveto");
	}
	
	public static void psLineTo(PlaceAndRoute par_, PrintWriter out_, double x_, double y_) {
		double x, y;
		x = par_.tXPrint(x_);
		y = par_.tYPrint(y_);

		out_.println("" + x + " " + y + " lineto");
	}

	public static void psStroke(PlaceAndRoute par_, PrintWriter out_) {
		out_.println("stroke");
	}
	
	public static void psDrawRectangle(PlaceAndRoute par_, PrintWriter out_, double x_,
			double y_, double w_, double h_, boolean selected_) {
		
		psNewPath (par_, out_, selected_);
		
		psMoveTo (par_, out_, x_, y_);
		psLineTo (par_, out_, x_+w_, y_);
		psLineTo (par_, out_, x_+w_, y_+h_);
		psLineTo (par_, out_, x_, y_+h_);
		psLineTo (par_, out_, x_, y_);
		
		psStroke (par_, out_);
	}	
}
