/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.views.rtl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.zamia.ZamiaException;
import org.zamia.plugin.views.rtl.RTLTreeCat.TreeCat;
import org.zamia.rtl.RTLGraph;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLPortModule;
import org.zamia.rtl.RTLSignal;
import org.zamia.vhdl.ast.Architecture;


/**
 * 
 * @author guenter bartsch
 *
 */

public class RTLTreeContentProvider implements ITreeContentProvider  {

	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object node_) {
		
		if (node_ instanceof RTLModule) {
			
			RTLModule module = (RTLModule) node_;

			RTLTreeCat cats[] = new RTLTreeCat[4];
			cats[0] = new RTLTreeCat("Ports", module, TreeCat.PORTS);
			if (module instanceof RTLGraph) {
				cats[1] = new RTLTreeCat("Subs", module, TreeCat.SUBS);
				cats[2] = new RTLTreeCat("Builtins", module, TreeCat.BUILTINS);
				cats[3] = new RTLTreeCat("Signals", module, TreeCat.SIGNALS);
			}
			
			return cats;
		}
		
		if (node_ instanceof RTLTreeCat) {
			
			RTLTreeCat cat = (RTLTreeCat) node_;

			ArrayList res = new ArrayList();
			
			switch (cat.getCat()) {
			case PORTS:
				RTLModule module = cat.getModule();

				int n = module.getNumPorts();
				for (int i = 0; i<n; i++) {
					res.add(module.getPort(i));
				}
				
				break;
			case SIGNALS:
				RTLGraph graph = getGraph(cat);
				
				n = graph.getNumSignals();
				for (int i = 0; i<n; i++) {
					res.add(graph.getSignal(i));
				}
				break;
				
			case SUBS:
				graph = getGraph(cat);
				
				n = graph.getNumSubs();
				for (int i = 0; i<n; i++) {
					RTLModule m = graph.getSub(i);
					if (m instanceof RTLGraph) {
						res.add(graph.getSub(i));
					}
				}
				break;

			case BUILTINS:
				graph = getGraph(cat);
				
				n = graph.getNumSubs();
				for (int i = 0; i<n; i++) {
					RTLModule m = graph.getSub(i);
					if (!(m instanceof RTLGraph) && !(m instanceof RTLPortModule)) {
						res.add(graph.getSub(i));
					}
				}
				break;
			}
			
			Collections.sort(res,new Comparator() {

				public int compare(Object o1, Object o2) {

					if ((o1 instanceof RTLSignal) && (o2 instanceof RTLSignal)) {
						
						RTLSignal s1 = (RTLSignal) o1;
						RTLSignal s2 = (RTLSignal) o2;
						
						return s1.getId().compareTo(s2.getId());
					}
					
					if ((o1 instanceof RTLModule) && (o2 instanceof RTLModule)) {
						
						RTLModule m1 = (RTLModule) o1;
						RTLModule m2 = (RTLModule) o2;
						
						return m1.getInstanceName().compareTo(m2.getInstanceName());
					}
					
					if ((o1 instanceof RTLPort) && (o2 instanceof RTLPort)) {
						
						RTLPort p1 = (RTLPort) o1;
						RTLPort p2 = (RTLPort) o2;
						
						return p1.getId().compareTo(p2.getId());
					}
					
					return 0;
				}});
			
			return res.toArray();
		}
		
		return null;
	}

	private RTLGraph getGraph(RTLTreeCat cat) {
		
		RTLModule m = cat.getModule();
		if (!(m instanceof RTLGraph))
			return null;
		
		RTLGraph graph = (RTLGraph) m;
		
//		Architecture arch = graph.getArch();
//		try {
//			arch.elaborateStatements(graph, true);
//		} catch (ZamiaException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return graph;
	}

	public Object getParent(Object node_) {
		
		if (node_ instanceof RTLTreeCat) {
			RTLTreeCat cat = (RTLTreeCat) node_;
			return cat.getModule();
		}
		
		return null;
	}

	public boolean hasChildren(Object node_) {
		return node_ instanceof RTLModule || node_ instanceof RTLTreeCat;
	}

	public Object[] getElements(Object node_) {
		return getChildren(node_);
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}
}
