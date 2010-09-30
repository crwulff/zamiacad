/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 3, 2008
 */
package org.zamia.plugin.views.rtl;

import java.util.ArrayList;

/**
 * @author guenter bartsch
 */
public class Produkt {

	private ArrayList<Baugruppe> baugruppen = new ArrayList<Baugruppe>();
	
	public void add(Baugruppe baugruppe_) {
		baugruppen.add(baugruppe_);
	}
	
	public ArrayList<Baugruppe> getBaugruppen() {
		return baugruppen;
	}

}
