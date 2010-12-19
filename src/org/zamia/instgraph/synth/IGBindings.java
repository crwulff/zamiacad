/*
 * Copyright 2005-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.instgraph.synth;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.instgraph.IGObject;
import org.zamia.util.HashMapArray;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class IGBindings {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private HashMapArray<IGObject, IGBinding> fBindings;

	private IGBindings fParent;

	public IGBindings(IGBindings aParent) {
		fBindings = new HashMapArray<IGObject, IGBinding>();
		fParent = aParent;
	}

	public IGBindings() {
		this(null);
	}

	public void bind(IGObject aObject, IGBindingNode aNode, IGClock aClk) throws ZamiaException {

		IGBinding binding = fBindings.get(aObject);

		if (binding == null) {
			binding = new IGBinding(aObject);
			fBindings.put(aObject, binding);
		}

		IGBindingNode node = aNode;

		if (aClk != null) {

			IGClock clk = binding.getClock();
			if (clk != null && !clk.equals(aClk)) {
				throw new ZamiaException("Multiple clocks detected.");
			}

			binding.setClock(aClk);

			IGBindingNode ob = binding.getSyncBinding();

			if (ob != null) {

				binding.setSyncBinding(new IGBindingNodeEMux(node, ob, aObject));

			} else {

				binding.setSyncBinding(node);

			}

		} else {
			IGBindingNode ob = binding.getASyncBinding();

			if (ob != null) {

				binding.setASyncBinding(new IGBindingNodeEMux(node, ob, aObject));

			} else {

				binding.setASyncBinding(node);

			}
		}

	}

	public int getNumBindings() {
		return fBindings.size();
	}

	public IGBinding getBinding(int aIdx) {
		return fBindings.get(aIdx);
	}

	public IGBinding getBinding(IGObject aObject) {
		IGBinding binding = fBindings.get(aObject);
		if (binding == null && fParent != null) {
			return fParent.getBinding(aObject);
		}
		return binding;
	}

	public void merge(IGBindings aBindings) throws ZamiaException {

		int n = aBindings.getNumBindings();
		for (int i = 0; i < n; i++) {
			IGBinding binding = aBindings.getBinding(i);

			IGObject obj = binding.getObject();

			IGBindingNode asyncb = binding.getASyncBinding();
			if (asyncb != null) {
				bind(obj, asyncb, null);
			}
			IGBindingNode syncb = binding.getSyncBinding();
			if (syncb != null) {
				bind(obj, syncb, binding.getClock());
			}
		}
	}

	public void dumpBindings() {

		logger.debug("Bindings dump for Bindings@" + hashCode());
		
		int n = getNumBindings();
		for (int i = 0; i < n; i++) {
			IGBinding b = getBinding(i);
			b.dump();
		}
	}

	public void elaborate(IGSynth aSynth) throws ZamiaException {
		int n = getNumBindings();
		for (int i = 0; i < n; i++) {
			IGBinding b = getBinding(i);
			b.synthesize(aSynth);
		}
	}

}