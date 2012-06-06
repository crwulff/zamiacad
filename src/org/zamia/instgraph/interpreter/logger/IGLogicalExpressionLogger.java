package org.zamia.instgraph.interpreter.logger;

import org.zamia.SourceLocation;

/**
 * @author Anton Chepurov
 */
public class IGLogicalExpressionLogger extends IGCodeExecutionLogger {

	public IGLogicalExpressionLogger(String aId) {
		super(aId, true);
	}

	private IGLogicalExpressionLogger() {
		super(null, false);
	}

	@Override
	protected IGLogicalExpressionLogger createLogger() {
		return new IGLogicalExpressionLogger(null);
	}

	@Override
	protected IGLogicalExpressionLogger createLeafLogger() {
		return new IGLogicalExpressionLogger();
	}

	public void logExpr(SourceLocation aSourceLocation, boolean aHasTrueOccurred, boolean aHasFalseOccurred) {

		logItemComposite(new Expression(aSourceLocation, aHasTrueOccurred, aHasFalseOccurred));
	}

	static class Expression extends CodeItem {

		boolean fHasTrueOccurred;
		boolean fHasFalseOccurred;

		public Expression(SourceLocation aLocation, boolean aHasTrueOccurred, boolean aHasFalseOccurred) {
			super(aLocation);
			fHasTrueOccurred = aHasTrueOccurred;
			fHasFalseOccurred = aHasFalseOccurred;
		}

		@Override
		public void merge(CodeItem aOther) {

			if (!(aOther instanceof Expression)) {
				return;
			}

			Expression other = (Expression) aOther;

			fHasFalseOccurred |= other.fHasFalseOccurred;
			fHasTrueOccurred |= other.fHasTrueOccurred;
		}

		@Override
		public Expression subtract(CodeItem aOther) {

			if (!(aOther instanceof Expression)) {
				return this;
			}
			Expression other = (Expression) aOther;

			Expression clone = cloneThis();
			clone.fHasTrueOccurred &= !other.fHasTrueOccurred;
			clone.fHasFalseOccurred &= !other.fHasFalseOccurred;
			return clone;
		}

		Expression cloneThis() {
			return new Expression(fLoc, fHasTrueOccurred, fHasFalseOccurred);
		}

		@SuppressWarnings("RedundantIfStatement")
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			Expression that = (Expression) o;

			if (fHasFalseOccurred != that.fHasFalseOccurred) return false;
			if (fHasTrueOccurred != that.fHasTrueOccurred) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + (fHasTrueOccurred ? 1 : 0);
			result = 31 * result + (fHasFalseOccurred ? 1 : 0);
			return result;
		}

		@Override
		public String toString(int fCurLine, int fCurCol) {
			StringBuilder b = new StringBuilder(super.toString(fCurLine, fCurCol));
			b.append(getLogicValue());
			return b.toString();
		}

		LogicValue getLogicValue() {
			if (fHasTrueOccurred && !fHasFalseOccurred) {
				return LogicValue.TRUE;
			}
			if (!fHasTrueOccurred && fHasFalseOccurred) {
				return LogicValue.FALSE;
			}
			return LogicValue.NONE;
		}

		enum LogicValue {
			TRUE, FALSE, NONE;

			@Override
			public String toString() {
				switch (this) {
					case TRUE:
						return " T ";
					case FALSE:
						return " F ";
					default:
						return "   ";
				}
			}
		}
	}
}
