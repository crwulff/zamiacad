/*
 * Copyright 2007,2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
*/

package org.zamia.fsm;

import java.util.HashMap;

import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.util.HashSetArray;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class CondNode {

	public enum CondNodeType {
		Literal, Variable, Op
	};

	public enum CondNodeOp {
		OpAnd, OpOr, OpNot, OpEq
	};

	private CondNode a, b;

	private CondNodeType type;

	private CondNodeOp op;

	private char literal;

	private String var;

	public CondNode(char literal_) {
		type = CondNodeType.Literal;
		literal = literal_;
	}

	public CondNode(String var_) {
		type = CondNodeType.Variable;
		var = var_;
	}

	public CondNode(CondNodeOp op_, CondNode a_, CondNode b_) {
		type = CondNodeType.Op;
		op = op_;
		a = a_;
		b = b_;
	}

	public char calc(HashMap<String, Character> bindings_) throws ZamiaException {

		switch (type) {
		case Literal:
			return literal;
		case Op:
			switch (op) {
			case OpAnd:
				char va = a.calc(bindings_);
				char vb = b.calc(bindings_);
				if ((va == IGStaticValue.BIT_DC) || (vb == IGStaticValue.BIT_DC))
					throw new ZamiaException("Don't care detected in an operation");

				if (va == IGStaticValue.BIT_1)
					return vb;
				return IGStaticValue.BIT_0;
			case OpOr:
				va = a.calc(bindings_);
				vb = b.calc(bindings_);

				if ((va == IGStaticValue.BIT_DC) || (vb == IGStaticValue.BIT_DC))
					throw new ZamiaException("Don't care detected in an operation");

				if (va == IGStaticValue.BIT_0)
					return vb;
				return IGStaticValue.BIT_1;
			case OpEq:
				va = a.calc(bindings_);
				vb = b.calc(bindings_);

				if (va == vb)
					return IGStaticValue.BIT_1;
				return IGStaticValue.BIT_0;
			case OpNot:
				va = a.calc(bindings_);

				if (va == IGStaticValue.BIT_DC)
					throw new ZamiaException("Don't care detected in an operation");

				if (va == IGStaticValue.BIT_1)
					return IGStaticValue.BIT_0;
				return IGStaticValue.BIT_1;
			}
		case Variable:
			return bindings_.get(var);
		}
		throw new ZamiaException("This didn't happen. Does not compute.");
	}

	@Override
	public String toString() {
		switch (type) {
		case Literal:
			return "" + literal;
		case Op:
			switch (op) {
			case OpAnd:

				return "(" + a + " and " + b + ")";

			case OpOr:
				return "(" + a + " or " + b + ")";

			case OpEq:
				return "(" + a + " == " + b + ")";

			case OpNot:
				return "( not " + a + ")";
			}
		case Variable:
			return var;
		}
		return super.toString();
	}

	public HashSetArray<String> getInputs() {

		HashSetArray<String> inputs = new HashSetArray<String>();

		switch (type) {
		case Literal:
			break;
		case Op:

			inputs.addAll(a.getInputs());
			if (b != null)
				inputs.addAll(b.getInputs());
			break;
		case Variable:
			inputs.add(var);
			break;
		}
		return inputs;
	}

	public String toHRString() {
		return toString();
	}
}
