/*
 * Copyright 2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 */
package org.zamia.plugin.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.zamia.vhdl.ast.ComponentDeclaration;
import org.zamia.vhdl.ast.ConstantDeclaration;
import org.zamia.vhdl.ast.InstantiatedUnit;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.SequentialProcess;
import org.zamia.vhdl.ast.SignalDeclaration;
import org.zamia.vhdl.ast.SubProgram;
import org.zamia.vhdl.ast.TypeDeclaration;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class ZamiaOutlineHierarchyGen {
	
	@SuppressWarnings("unchecked")
	private HashMap<String,ArrayList> map;
	@SuppressWarnings("unchecked")
	private ArrayList list;
	
	@SuppressWarnings("unchecked")
	public ZamiaOutlineHierarchyGen() {
		map = new HashMap<String, ArrayList>();
		list = new ArrayList();
	}
	
	private String getCategory(Object o_) {
		if (o_ instanceof InterfaceDeclaration)
			return "Interfaces" ;
		if (o_ instanceof SignalDeclaration)
			return "Signals";
		if (o_ instanceof InstantiatedUnit)
			return "Units";
		if (o_ instanceof ComponentDeclaration)
			return "Units";
		if (o_ instanceof TypeDeclaration)
			return "Types";
		if (o_ instanceof ConstantDeclaration)
			return "Constants";
		if (o_ instanceof SequentialProcess)
			return "Processes";
		if (o_ instanceof InterfaceDeclaration) {
			InterfaceDeclaration id = (InterfaceDeclaration) o_;
			switch (id.getDir()) {
			case IN:
				return "Input Ports";
			case OUT:
				return "Output Ports";
			default:
				return "Other Ports";
			}
		}
		if (o_ instanceof SubProgram) {
			return "Subprograms";
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void add(Object o_) {
		String cat = getCategory(o_);
		if (cat != null) {
			ArrayList l = map.get(cat);
			if (l==null) {
				l = new ArrayList();
				map.put(cat, l);
				list.add (new ZamiaOutlineFolder(cat, l));
			}
			l.add(o_);
		} else {
			list.add(o_);
		}
	}

	@SuppressWarnings("unchecked")
	private void sort (ArrayList l_) {
		Collections.sort(l_, new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.toString().compareToIgnoreCase(o2.toString());
			}});
	}

	public Object[] toArray() {
		
		for (String key: map.keySet()) {
			sort (map.get(key));
		}
		
		return list.toArray();
	}
}
