/*
 * Copyright 2005-2009 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on 27.06.2004
 */

package org.zamia.vhdl.ast;

import java.io.PrintStream;
import java.util.ArrayList;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.analysis.ReferenceSearchResult;
import org.zamia.analysis.ReferenceSite.RefType;
import org.zamia.analysis.ast.SearchJob;
import org.zamia.analysis.ast.ASTReferencesSearch.ObjectCat;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGElaborationEnv;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationCache;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGOperationBinary.BinOp;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class SequentialCase extends SequentialStatement {

	static class Alternative extends VHDLNode {
		public ArrayList<Range> choices;

		public SequenceOfStatements seq;

		public Alternative(ArrayList<Range> choices_, SequenceOfStatements seq_, VHDLNode parent_, long location_) {
			super(parent_, location_);
			choices = choices_;
			int n = choices.size();
			for (int i = 0; i < n; i++) {
				Range choice = choices.get(i);
				if (choice != null)
					choice.setParent(this);
			}
			seq = seq_;
			if (seq != null)
				seq.setParent(this);
		}

		@Override
		public int getNumChildren() {
			return choices.size() + 1;
		}

		@Override
		public VHDLNode getChild(int idx_) {
			switch (idx_) {
			case 0:
				return seq;
			}
			return choices.get(idx_ - 1);
		}

		@Override
		public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
				ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {
			int n = choices.size();
			for (int i = 0; i < n; i++) {
				Range choice = choices.get(i);
				if (choice != null) {
					choice.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
				}
			}
			if (seq != null)
				seq.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	private Operation expr;

	private ArrayList<Alternative> alternatives; // of Alternative

	public SequentialCase(Operation expr_, VHDLNode parent_, long location_) {
		super(parent_, location_);
		expr = expr_;
		expr.setParent(this);
		alternatives = new ArrayList<Alternative>();
	}

	/**
	 * add another alternative branch to this case statement
	 * 
	 * @param choices_
	 *            : ArrayList of Choice
	 * @param seq_
	 *            : Sequence of statments that is to be executed
	 */
	public void addAlternative(ArrayList<Range> choices_, SequenceOfStatements seq_, long location_) {
		Alternative alt = new Alternative(choices_, seq_, this, location_);
		alternatives.add(alt);
		alt.setParent(this);
	}

	public Operation getExpr() {
		return expr;
	}

	public int getNumAlternatives() {
		return alternatives.size();
	}

	public Alternative getAlternative(int idx_) {
		return alternatives.get(idx_);
	}

	@Override
	public int getNumChildren() {
		return alternatives.size() + 1;
	}

	@Override
	public VHDLNode getChild(int idx_) {
		if (idx_ == 0)
			return expr;
		return alternatives.get(idx_ - 1);
	}

	@Override
	public void dumpVHDL(int indent_, PrintStream out_) {
		printlnIndented("case " + expr + " is", indent_, out_);

		int n = alternatives.size();
		for (int i = 0; i < n; i++) {
			Alternative alt = alternatives.get(i);

			printIndented("when ", indent_ + 2, out_);
			int m = alt.choices.size();
			for (int j = 0; j < m; j++) {
				Range choice = alt.choices.get(j);
				if (choice != null) {
					out_.print("" + choice);
					if (j < m - 1) {
						out_.print(", ");
					}
				} else {
					out_.print("others");
				}
			}
			out_.println(" =>");
			alt.seq.dumpVHDL(indent_ + 4, out_);

		}
		printlnIndented("end case;", indent_, out_);
	}

	@Override
	public void findReferences(String id_, ObjectCat category_, RefType refType_, int depth_, ZamiaProject zprj_, IGContainer aContainer, IGElaborationEnv aCache,
			ReferenceSearchResult result_, ArrayList<SearchJob> aTODO) throws ZamiaException {

		expr.findReferences(id_, category_, RefType.Read, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);

		int n = getNumAlternatives();
		for (int i = 0; i < n; i++) {
			Alternative alt = getAlternative(i);
			alt.findReferences(id_, category_, refType_, depth_ + 1, zprj_, aContainer, aCache, result_, aTODO);
		}
	}

	@Override
	public void generateIG(IGSequenceOfStatements aSeq, IGContainer aContainer, IGElaborationEnv aEE) throws ZamiaException {
		IGOperation expr = this.expr.computeIGOperation(null, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);

		IGSequenceOfStatements seq = aSeq;

		IGType boolType = aContainer.findBoolType();

		int n = alternatives.size();
		for (int i = 0; i < n; i++) {
			Alternative alt = alternatives.get(i);

			// others?

			int m = alt.choices.size();
			if (m == 1 && alt.choices.get(0) == null) {
				// yes.

				if (i != n - 1) {
					throw new ZamiaException("SequentialCase: others statement must be the last choice.", alt.getLocation());
				}

				alt.seq.generateIG(seq, aContainer, aEE);

			} else {

				IGSequenceOfStatements ts = new IGSequenceOfStatements(null, alt.getLocation(), aEE.getZDB());

				IGOperation cond = null;
				for (int j = 0; j < m; j++) {

					Range r = alt.choices.get(j);

					if (r == null)
						throw new ZamiaException("SequentialCase: Illegal others choice.", alt);

					IGType exprT = expr.getType();

					IGItem item = r.computeIG(exprT, aContainer, aEE, new IGOperationCache(), ASTErrorMode.RETURN_NULL, new ErrorReport());
					if (item == null) {
						IGType exprRT = exprT.getRange().getType();
						item = r.computeIG(exprRT, aContainer, aEE, new IGOperationCache(), ASTErrorMode.EXCEPTION, null);
					}

					IGItem obj = tryToGenerateIGOperation(item, aContainer, r.getLocation());

					IGOperation o;
					if (obj instanceof IGOperation) {

						IGOperation op = (IGOperation) obj;

						IGType opT = op.getType();
						if (opT.isRange()) {

							// FIXME: Operation.optimize is broken for non-pure-constant expressions right now
							// see https://bugs.zamiacad.com/show_bug.cgi?id=78

							//							IGOperation rangeMin = op.getRangeMin(aContainer, alt.getLocation()).optimize(aEE.getInterpreterEnv());
							//							IGOperation rangeMax = op.getRangeMax(aContainer, alt.getLocation()).optimize(aEE.getInterpreterEnv());

							//							IGOperation ol = new IGOperationBinary(expr, rangeMin, BinOp.GREATEREQ, boolType, alt.getLocation(), aEE.getZDB()).optimize(aEE.getInterpreterEnv());
							//							IGOperation or = new IGOperationBinary(expr, rangeMax, BinOp.LESSEQ, boolType, alt.getLocation(), aEE.getZDB()).optimize(aEE.getInterpreterEnv());
							//							
							//							o = new IGOperationBinary(ol, or, BinOp.AND, boolType, alt.getLocation(), aEE.getZDB()).optimize(aEE.getInterpreterEnv());

							IGOperation rangeMin = op.getRangeMin(aContainer, alt.getLocation());
							IGOperation rangeMax = op.getRangeMax(aContainer, alt.getLocation());

							IGOperation ol = new IGOperationBinary(expr, rangeMin, BinOp.GREATEREQ, boolType, alt.getLocation(), aEE.getZDB());
							IGOperation or = new IGOperationBinary(expr, rangeMax, BinOp.LESSEQ, boolType, alt.getLocation(), aEE.getZDB());

							o = new IGOperationBinary(ol, or, BinOp.AND, boolType, alt.getLocation(), aEE.getZDB());

						} else {

							// FIXME: Operation.optimize is broken for non-pure-constant expressions right now
							// see https://bugs.zamiacad.com/show_bug.cgi?id=78
							//							o = new IGOperationBinary(expr, (IGOperation) obj, BinOp.EQUAL, boolType, alt.getLocation(), aEE.getZDB()).optimize(aEE.getInterpreterEnv());
							o = new IGOperationBinary(expr, (IGOperation) obj, BinOp.EQUAL, boolType, alt.getLocation(), aEE.getZDB());
						}
					} else {
						throw new ZamiaException("Range or operation expected here.", r);
					}

					if (cond == null) {
						cond = o;
					} else {
						// FIXME: Operation.optimize is broken for non-pure-constant expressions right now
						// see https://bugs.zamiacad.com/show_bug.cgi?id=78
						//						cond = new IGOperationBinary(cond, o, BinOp.OR, boolType, alt.getLocation(), aEE.getZDB()).optimize(aEE.getInterpreterEnv());
						cond = new IGOperationBinary(cond, o, BinOp.OR, boolType, alt.getLocation(), aEE.getZDB());
					}
				}

				IGSequentialIf si = new IGSequentialIf(cond, ts, null, alt.getLocation(), aEE.getZDB());
				seq.add(si);

				alt.seq.generateIG(ts, aContainer, aEE);

				seq = new IGSequenceOfStatements(null, alt.getLocation(), aEE.getZDB());
				si.setElse(seq);
			}
		}
	}

}