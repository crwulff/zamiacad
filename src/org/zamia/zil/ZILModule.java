/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import java.util.ArrayList;

import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.rtl.Optimizer;
import org.zamia.rtl.RTLGraph;
import org.zamia.util.HashMapArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.zil.synthesis.Binding;
import org.zamia.zil.synthesis.Bindings;


/**
 * An architecture will generate this when asked to produce ZIL
 * 
 * @author Guenter Bartsch
 * 
 */

public class ZILModule extends ZILDesignUnit {

	private HashMapArray<String, ZILInterfaceValue> fGenerics;
	private HashMapArray<String, ZILInterfaceSignal> fInterfaces;

	private ArrayList<ZILConcurrentStatement> fStatements;

	public ZILModule(String aId, ASTObject aSrc) {
		super(aId, aSrc);
		fGenerics = new HashMapArray<String, ZILInterfaceValue>();
		fInterfaces = new HashMapArray<String, ZILInterfaceSignal>();
		fStatements = new ArrayList<ZILConcurrentStatement>();
	}

	public void addGeneric(ZILInterfaceValue aInterface, ASTObject aSrc) throws ZamiaException {
		fGenerics.put(aInterface.getId(), aInterface);
		add(aInterface, aSrc);
	}

	public void addPort(ZILInterfaceSignal aInterface, ASTObject aSrc) throws ZamiaException {
		fInterfaces.put(aInterface.getId(), aInterface);
		add(aInterface, aSrc);
	}

	public void addStatement(ZILConcurrentStatement aObj) {
		fStatements.add(aObj);
	}

	public void dump(int aIndent) {
		logger.debug(aIndent, "module id=%s {", getId());
		logger.debug(aIndent + 1, "interfaces");
		int n = getNumInterfaces();
		for (int i = 0; i < n; i++) {
			ZILInterfaceSignal intf = getInterface(i);
			intf.dump(aIndent + 2);
		}
		logger.debug(aIndent + 1, "generics");
		n = getNumGenerics();
		for (int i = 0; i < n; i++) {
			ZILInterfaceValue g = getGeneric(i);
			g.dump(aIndent + 2);
		}
		logger.debug(aIndent + 1, "declarations");
		n = getNumItems();
		for (int i = 0; i < n; i++) {
			ZILIObject item = getItem(i);
			item.dump(aIndent + 2);
		}
		logger.debug(aIndent + 1, "statements");
		n = getNumStatements();
		for (int i = 0; i < n; i++) {
			ZILIObject statement = getStatement(i);
			statement.dump(aIndent + 2);
		}
		logger.debug(aIndent, "}", getId());
	}

	public ZILInterfaceValue getGeneric(int aIndex) {
		return fGenerics.get(aIndex);
	}

	public int getNumGenerics() {
		return fGenerics.size();
	}

	public int getNumInterfaces() {
		return fInterfaces.size();
	}

	public ZILInterfaceSignal getInterface(int aIndex) {
		return fInterfaces.get(aIndex);
	}

	public int getNumStatements() {
		return fStatements.size();
	}

	public ZILIObject getStatement(int aIndex) {
		return fStatements.get(aIndex);
	}

	public RTLGraph elaborate(ZamiaProject aZPrj) throws ZamiaException {

		String id = getId();

		logger.info("Elaborating: Module '%s'", id);

		RTLGraph rtlg = new RTLGraph(id, "Module", getSrc(), /* FIXME */null);

		RTLCache cache = new RTLCache(rtlg, aZPrj);

		int n = fInterfaces.size();
		for (int i = 0; i < n; i++) {
			ZILInterfaceSignal intf = fInterfaces.get(i);

			intf.elaborateAsPortModule(rtlg, cache);
		}

		// resolvers.add(parms_.resolver);
		// entity.pushResolver(parms_.entityResolver);

		Bindings bindings = new Bindings();

		n = fStatements.size();
		// int p = 0;
		for (int i = 0; i < n; i++) {
			ZILConcurrentStatement cs = fStatements.get(i);
			if (cs == null)
				continue;

			if (cs.isBindingsProducer(cache)) {
				Bindings tmpBindings = cs.computeBindings(cache);

				bindings.merge(tmpBindings);
			} else {
				cs.elaborate(cache);
			}

		}

		// FIXME: FSM extraction

		//		FSMExtract ext = new FSMExtract();
		//		ArrayList<FSM> fsms = new ArrayList<FSM>();
		//		HashSet<RTLSignal> handledSignals = new HashSet<RTLSignal>();
		//		ext.extractFSMs(bindings, handledSignals, fsms, cache);
		//
		//		n = fsms.size();
		//		for (int i = 0; i < n; i++) {
		//			FSM fsm = fsms.get(i);
		//
		//			RTLFSM module = new RTLFSM(fsm, rtlg_, null, null);
		//
		//			rtlg_.add(module);
		//		}

		//
		// synthesize bindings
		//

		n = bindings.getNumBindings();
		for (int i = 0; i < n; i++) {
			Binding binding = bindings.getBinding(i);
			ZILIReferable referable = binding.getReferable();
			//				if (handledSignals.contains(s)) {
			//					continue;
			//				}
			logger.debug("\nReferable '%s'\n------------------------------\nbound to: %s", referable.toString(), binding.toString());

			binding.elaborate(bindings, cache);
		}

		rtlg.cleanup();

		//
		// run optimizer (constant propagation, simplifications)
		//
		
		Optimizer.optimize(rtlg);

		return rtlg;
	}

	@Override
	public String toString() {
		return "module (id=" + getId() + ")";
	}

}
