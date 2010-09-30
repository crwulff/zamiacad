/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.zil.synthesis;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashMapArray;
import org.zamia.zil.ZILClock;
import org.zamia.zil.ZILIReferable;
import org.zamia.zil.ZILTargetOperation;
import org.zamia.zil.ZILTargetOperationDestination;
import org.zamia.zil.ZILTargetOperationEMux;
import org.zamia.zil.ZILTargetOperationSource;
import org.zamia.zil.ZILVariable;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class Bindings {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private HashMapArray<ZILIReferable, Binding> fBindings;

	private HashMapArray<Integer, VariableBinding> fVariableBindings;

	private Bindings fParent;

	public Bindings(Bindings aParent) {
		fBindings = new HashMapArray<ZILIReferable, Binding>();
		fVariableBindings = new HashMapArray<Integer, VariableBinding>();
		fParent = aParent;
	}

	public Bindings() {
		this(null);
	}

	public void bindClocked(ZILTargetOperationDestination aValue, ZILClock aClk) throws ZamiaException {

		ZILIReferable aReferable = aValue.getObject();

		Binding b = fBindings.get(aReferable);
		if (b == null) {
			b = new Binding(this, aReferable);
			fBindings.put(aReferable, b);
		}

		ZILTargetOperationDestination oldValue = b.getSyncValue();
		if (oldValue != null) {
			ZILTargetOperation ov = oldValue.getSource();
			ZILTargetOperation nv = aValue.getSource();
			oldValue.setSource(new ZILTargetOperationEMux(ov, nv, oldValue.getContainer(), aValue.getSrc()));
		} else {
			b.setSyncValue(aValue);
		}

		b.setClock(aClk);
	}

	public void bind(ZILTargetOperationDestination aValue) throws ZamiaException {

		ZILIReferable aReferable = aValue.getObject();

		Binding b = fBindings.get(aReferable);
		if (b == null) {
			b = new Binding(this, aReferable);
			fBindings.put(aReferable, b);
		}

		ZILTargetOperationDestination oldValue = b.getASyncValue();
		if (oldValue != null) {
			ZILTargetOperation ov = oldValue.getSource();
			ZILTargetOperation nv = aValue.getSource();
			oldValue.setSource(new ZILTargetOperationEMux(ov, nv, oldValue.getContainer(), aValue.getSrc()));
		} else {
			b.setASyncValue(aValue);
		}
	}

	public int getNumBindings() {
		return fBindings.size();
	}

	public Binding getBinding(int aIdx) {
		return fBindings.get(aIdx);
	}

	public Binding getBinding(ZILIReferable aReferable) {
		Binding binding = fBindings.get(aReferable);
		return binding;
	}

	/******************************************
	 * Variable part
	 ******************************************/

	public int getNumVariableBindings() {
		return fVariableBindings.size();
	}

	public VariableBinding getVariableBinding(int i_) {
		return fVariableBindings.get(i_);
	}

	public void clearBinding(int aUID) {
		fVariableBindings.remove(new Integer(aUID));
	}

	public void bind(ZILVariable aVariable, ZILTargetOperation aValue, ZILClock aClk) throws ZamiaException {

		VariableBinding b = fVariableBindings.get(new Integer(aVariable.getUID()));
		if (b == null) {
			b = new VariableBinding(aVariable, aClk, aValue);
			fVariableBindings.put(aVariable.getUID(), b);
			return;
		}

		ZILClock clk = b.getClk();
		if (clk != aClk) {
			clk = aClk;
		}

		ZILTargetOperation value = b.getTO();
		if (aValue instanceof ZILTargetOperationSource) {
			value = aValue;
		} else {
			if (value != null) {
				value = new ZILTargetOperationEMux(value, aValue, value.getContainer(), aValue.getSrc());
			} else {
				value = aValue;
			}
		}

		b = new VariableBinding(aVariable, clk, value);
		fVariableBindings.put(aVariable.getUID(), b);
	}

	public VariableBinding get(ZILVariable aVariable) {
		VariableBinding b = fVariableBindings.get(new Integer(aVariable.getUID()));
		if (b != null)
			return b;
		if (fParent != null)
			return fParent.get(aVariable);
		return null;
	}

	public void merge(VariableBinding aBinding) throws ZamiaException {

		ZILTargetOperation value = aBinding.getTO();
		if (value != null) {
			bind(aBinding.getVar(), value, aBinding.getClk());
		}
	}

	/***********************************************
	 * common part
	 */

	public void merge(Bindings aBindings) throws ZamiaException {

		//		if (dump) {
		//			out_.println("Bindings: merge");
		//			dumpBindings(out_);
		//			out_.println("   merge with:");
		//			bindings_.dumpBindings(out_);
		//		}

		int n = aBindings.getNumBindings();
		for (int i = 0; i < n; i++) {
			Binding binding = aBindings.getBinding(i);

			merge(binding);
		}

		n = aBindings.getNumVariableBindings();
		for (int i = 0; i < n; i++) {
			VariableBinding vb = aBindings.getVariableBinding(i);
			merge(vb);
		}
		//		if (dump) {
		//			out_.println("merge result:");
		//			dumpBindings(out_);
		//		}

	}

	public void dumpBindings() {

		logger.debug("Bindings dump for Bindings@" + hashCode());
		int n = getNumBindings();
		for (int i = 0; i < n; i++) {
			Binding b = getBinding(i);
			logger.debug(b.toString());
		}
	}

	public void merge(Binding aBinding) throws ZamiaException {
		ZILTargetOperationDestination value = aBinding.getSyncValue();
		ZILClock clk = aBinding.getClock();
		if (value != null)
			bindClocked(value, clk);
		value = aBinding.getASyncValue();
		if (value != null)
			bind(value);
	}

	public VariableBinding getVariableBindingByUID(int aUid) {
		return fVariableBindings.get(new Integer(aUid));
	}
}