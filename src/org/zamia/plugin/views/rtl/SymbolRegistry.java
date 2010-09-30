/*
 * Copyright 2004-2008 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.plugin.views.rtl;

import java.util.HashMap;

import org.zamia.plugin.views.rtl.symbols.ArrayCSelSymbol;
import org.zamia.plugin.views.rtl.symbols.ArrayRangeSelSymbol;
import org.zamia.plugin.views.rtl.symbols.CESymbol;
import org.zamia.plugin.views.rtl.symbols.GenericSymbol;
import org.zamia.plugin.views.rtl.symbols.InoutPortSymbol;
import org.zamia.plugin.views.rtl.symbols.InputPortSymbol;
import org.zamia.plugin.views.rtl.symbols.LiteralSymbol;
import org.zamia.plugin.views.rtl.symbols.MathOpSymbol;
import org.zamia.plugin.views.rtl.symbols.OutputPortSymbol;
import org.zamia.plugin.views.rtl.symbols.RecordSelSymbol;
import org.zamia.plugin.views.rtl.symbols.Symbol;
import org.zamia.plugin.views.rtl.symbols.TargetArrayCSelSymbol;
import org.zamia.plugin.views.rtl.symbols.TargetArraySymbol;
import org.zamia.plugin.views.rtl.symbols.TargetRecordCSelSymbol;
import org.zamia.plugin.views.rtl.symbols.TargetRecordSymbol;
import org.zamia.rtl.RTLArrayCSel;
import org.zamia.rtl.RTLInoutPort;
import org.zamia.rtl.RTLInputPort;
import org.zamia.rtl.RTLLiteral;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLOutputPort;
import org.zamia.rtl.RTLRecordSel;
import org.zamia.rtl.RTLTargetArray;
import org.zamia.rtl.RTLTargetArrayCSel;
import org.zamia.rtl.RTLTargetRecord;
import org.zamia.rtl.RTLTargetRecordCSel;


// Singleton

/**
 * 
 * @author Guenter Bartsch
 */

public class SymbolRegistry {

	private HashMap<String,Symbol> syms;

	private static SymbolRegistry instance = null;
	
	public static SymbolRegistry getInstance() {
		if (instance == null)
			instance = new SymbolRegistry();
		return instance;
	}
	
	private SymbolRegistry() {

		syms = new HashMap<String,Symbol>();
		syms.put("RTLOpMath", new MathOpSymbol());
		syms.put("RTLCE", new CESymbol());
		syms.put("RTLArrayRangeSel", new ArrayRangeSelSymbol());
	}

	public Symbol getSymbol(RTLModule module_, RTLView control_) {
		Symbol sym = (Symbol) syms.get(module_.getClassName());
		if (sym != null)
			return sym;
		if (module_ instanceof RTLInputPort)
			return new InputPortSymbol (module_, control_);
		else if (module_ instanceof RTLInoutPort)
			return new InoutPortSymbol (module_, control_);
		else if (module_ instanceof RTLOutputPort)
			return new OutputPortSymbol (module_, control_);
		else if (module_ instanceof RTLLiteral)
			return new LiteralSymbol ((RTLLiteral)module_, control_);
		else if (module_ instanceof RTLTargetArrayCSel)
			return new TargetArrayCSelSymbol ((RTLTargetArrayCSel)module_);
		else if (module_ instanceof RTLTargetArray)
			return new TargetArraySymbol ((RTLTargetArray)module_);
		else if (module_ instanceof RTLArrayCSel)
			return new ArrayCSelSymbol ((RTLArrayCSel)module_);
		else if (module_ instanceof RTLRecordSel)
			return new RecordSelSymbol ((RTLRecordSel)module_);
		else if (module_ instanceof RTLTargetRecordCSel)
			return new TargetRecordCSelSymbol ((RTLTargetRecordCSel)module_, control_);
		else if (module_ instanceof RTLTargetRecord)
			return new TargetRecordSymbol ((RTLTargetRecord)module_, control_);
		return new GenericSymbol(module_, control_);
	}
}
