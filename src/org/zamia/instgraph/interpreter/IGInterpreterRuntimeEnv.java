/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.instgraph.interpreter;

import java.math.BigInteger;

import org.zamia.ErrorReport;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGStmt.ReturnStatus;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;

/**
 * Stack, PC and Variable contexts for executing InterpreterCode
 * 
 * 
 * @author Guenter Bartsch
 * 
 */
public class IGInterpreterRuntimeEnv {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	public final static boolean dump = false;

	protected ZStack<IGInterpreterContext> fContexts;

	private ZStack<IGStackFrame> fStack;

	private int fPC;

	private IGInterpreterCode fCode;

	static class CallStackEntry {
		IGInterpreterCode fCode;

		int fPC;

		public CallStackEntry(IGInterpreterCode aCode, int aPC) {
			fCode = aCode;
			fPC = aPC;
		}

		@Override
		public String toString() {
			return fCode + ":" + fPC;
		}
	}

	private ZStack<CallStackEntry> fCallStack;

	private ZamiaProject fZPrj;

	public IGInterpreterRuntimeEnv(IGInterpreterCode aCode, ZamiaProject aZPrj) {
		reset();
		fCode = aCode;
		fZPrj = aZPrj;
	}

	public void reset() {
		fPC = 0;
		fContexts = new ZStack<IGInterpreterContext>();
		fStack = new ZStack<IGStackFrame>();
		fCallStack = new ZStack<CallStackEntry>();
	}

	public ReturnStatus resume(ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		ReturnStatus status = null;

		if (dump) {
			logger.debug("Interpreter: resuming code for %s", fCode.getId());
		}

		while (fPC < fCode.size()) {
			IGStmt stmt = fCode.get(fPC++);
			if (dump) {
				logger.debug("Interpreter: executing %s:%3d %s (%s)", fCode.getId(), fPC - 1, stmt, stmt.computeSourceLocation());
			}

			status = stmt.execute(this, aErrorMode, aReport);

			switch (status) {
			case WAIT:
				return status;

			case ERROR:
				return status;

			case RETURN:
				rts();
				break;

			case CONTINUE:
				break;
			}
		}

		return status;
	}

	public void logInterpreterState() {

		logger.debug("Interpreter: ============== STATE LOG STARTS =================");

		int pc = fPC - 1;
		if (pc < 0)
			pc = fCode.size() - 1;

		logger.debug("Interpreter: Current stmt: PC=%3d %s (%s)", pc, fCode.get(pc), fCode.get(pc).computeSourceLocation());
		fCode.dumpToLogger(pc);

		int n = fCallStack.size();
		for (int i = n - 1; i >= 0; i--) {

			CallStackEntry entry = fCallStack.get(i);

			IGInterpreterCode code = entry.fCode;
			pc = entry.fPC - 1;

			if (code != null) {
				code.dumpToLogger(pc);
			} else {
				logger.debug("Interpreter: code == null.");
			}

			if (code == null || pc < 0 || pc >= code.getNumStmts()) {
				logger.debug("Interpreter: call stack %2d: INVALID", i);
				continue;
			}

			IGStmt stmt = code.get(pc);

			if (stmt != null) {
				logger.debug("Interpreter: call stack %2d: %s:%3d: %s (%s)", i, code.getId(), pc, stmt, stmt.computeSourceLocation());
			} else {
				logger.debug("Interpreter: call stack %2d: INVALID", i);
			}
		}

		n = fStack.size();
		for (int i = n - 1; i >= 0; i--) {
			logger.debug("Interpreter: stack %2d: %s", i, fStack.get(i));
		}

		n = fContexts.size();
		for (int i = n - 1; i >= 0; i--) {
			IGInterpreterContext context = fContexts.get(i);
			context.dump();
		}

		logger.debug("Interpreter: ============== STATE LOG ENDS   =================");

	}

	public ReturnStatus call(IGInterpreterCode aCode, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		if (aCode == null) {
			if (aErrorMode == ASTErrorMode.RETURN_NULL) {
				aReport.append("Call to subprogram without a body", null);
				return ReturnStatus.ERROR;
			}
			throw new ZamiaException("Call to subprogram without a body");
		}

		//System.out.println("Calling a subprogram:");
		//aCode.dump(System.out);

		fCallStack.push(new CallStackEntry(fCode, fPC));

		fCode = aCode;
		fPC = 0;

		return ReturnStatus.CONTINUE;
	}

	public void rts() {
		CallStackEntry entry = fCallStack.pop();
		fPC = entry.fPC;
		fCode = entry.fCode;
	}

	public int getPC() {
		return fPC;
	}

	public IGStackFrame pop() throws ZamiaException {
		if (fStack.isEmpty()) {
			throw new ZamiaException("Stack underflow error.");
		}
		IGStackFrame sf = fStack.pop();
		if (dump) {
			logger.debug("Interpreter: popped %s", sf);
		}
		return sf;
	}

	public IGStackFrame peek(int aOffset) throws ZamiaException {
		int n = fStack.size();
		if (n <= aOffset) {
			throw new ZamiaException("Stack underflow error.");
		}
		return fStack.get(n - 1 - aOffset);
	}

	public void pushContext(IGInterpreterContext aContext) {
		fContexts.push(aContext);
	}

	public IGInterpreterContext enterContext() {
		IGInterpreterContext context = new IGInterpreterContext();
		fContexts.push(context);
		return context;
	}

	public void exitContext() {
		fContexts.pop();
	}

	public IGObjectDriver newObject(IGObject aObj, ASTErrorMode aErrorMode, ErrorReport aReport, SourceLocation aLocation) throws ZamiaException {
		IGInterpreterContext context = fContexts.peek();

		IGTypeStatic type = aObj.getType().computeStaticType(this, aErrorMode, aReport);

		if (type == null || type.isError()) {
			return null;
		}
		
		IGInterpreterObject intObject = new IGInterpreterObject(aObj, type);
		
		IGObjectDriver driver = context.createObject(intObject, aLocation);
		
		IGOperation iv = aObj.getInitialValue();
		if (iv != null) {
			IGStaticValue value = iv.computeStaticValue(this, aErrorMode, aReport);
			
			if (value == null) {
				ZamiaException e = new ZamiaException("Interpreter: Failed to compute initial value for "+aObj, aLocation);
				if (aErrorMode == ASTErrorMode.RETURN_NULL) {
					if (aReport != null) {
						aReport.append(e);
					}
					return null;
				}
				throw e;
			}
			
			driver.setValue(value, aLocation);
		}

		return driver;
	}

	public IGObjectDriver getDriver(IGObject aObj, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {
		int n = fContexts.size();
		long dbid = aObj.getDBID();
		for (int i = n - 1; i >= 0; i--) {
			IGInterpreterContext context = fContexts.get(i);
			IGObjectDriver driver = context.getObjectDriver(dbid);
			if (driver != null)
				return driver;
		}
		return null;
	}
	
	public IGStaticValue getObjectValue(IGObject aObj) throws ZamiaException {
		
		IGObjectDriver driver = getDriver(aObj, ASTErrorMode.EXCEPTION, null);
		if (driver == null) {
			return null;
		}
		
		return driver.getValue(null);
	}

	public IGInterpreterContext findContext(long aDBID) throws ZamiaException {
		int n = fContexts.size();

		// if not found => create in latest context
		IGInterpreterContext context = fContexts.get(n - 1);

		for (int i = n - 1; i >= 0; i--) {
			IGInterpreterContext c = fContexts.get(i);
			IGStaticValue iv = c.getObjectValue(aDBID);
			if (iv != null) {
				context = c;
				break;
			}
		}

		return context;
	}

	public void setObjectValue(IGObject aObject, IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		IGTypeStatic type = aObject.getType().computeStaticType(this, ASTErrorMode.EXCEPTION, null);

		IGInterpreterObject io = new IGInterpreterObject(aObject, type);

		long dbid = aObject.getDBID();
		IGInterpreterContext context = findContext(dbid);
		context.setObjectValue(io, aValue, aLocation);
	}

	public void setPC(int aAdr) {
		fPC = aAdr;
	}

	public void push(IGStaticValue aValue) {
		if (dump) {
			logger.debug("Interpreter: pushing value %s type is %s", aValue, aValue.getType());
		}
		fStack.push(new IGStackFrame(aValue));
	}

	public void push(IGTypeStatic aType) {
		if (dump) {
			logger.debug("Interpreter: pushing %s", aType);
		}
		fStack.push(new IGStackFrame(aType));
	}

	public void push(IGObjectDriver aDriver) {
		if (dump) {
			logger.debug("Interpreter: pushing %s", aDriver);
		}
		fStack.push(new IGStackFrame(aDriver));
	}
	
	public ZDB getZDB() {
		return fZPrj.getZDB();
	}

	public ZamiaProject getZPrj() {
		return fZPrj;
	}

	private IGInterpreterContext getCurrentContext() {
		int n = fContexts.size();
		if (n == 0) {
			return null;
		}
		return fContexts.get(n - 1);
	}

	public IGTypeStatic getCachedActualType(long aDBID) {

		IGInterpreterContext context = getCurrentContext();
		if (context == null) {
			logger.error("IGInterpreterRuntime: getCurrentContext(): no current context!");
			return null;
		}

		return context.getCachedType(aDBID);
	}

	public void putCachedActualType(long aDBID, IGTypeStatic aType) {
		IGInterpreterContext context = getCurrentContext();
		if (context == null) {
			return;
		}

		context.putCachedType(aDBID, aType);
	}

	public IGStaticValue getCachedLiteralActualConstant(IGOperationLiteral aLiteral) {

		IGInterpreterContext context = getCurrentContext();
		if (context == null) {
			logger.error("IGInterpreterRuntime: getCachedLiteralActualConstant(): no current context!");
			return null;
		}

		return context.getCachedLiteralActualConstant(aLiteral);
	}

	public void putCachedLiteralActualConstant(IGOperationLiteral aLiteral, IGStaticValue aAC) {
		IGInterpreterContext context = getCurrentContext();
		if (context == null) {
			return;
		}

		context.putCachedLiteralActualConstant(aLiteral, aAC);
	}

	@Override
	public String toString() {

		if (fCode != null) {
			return "IGInterpreterRuntime(" + fCode + ":" + fPC + ", stack=" + fCallStack + ")";
		}

		return "IGInterpreterRuntime";
	}

	public void scheduleWakeup(BigInteger aT, SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support wakeup requests.", aLocation);
	}

	public void scheduleWakeup(IGObjectDriver aDriver, SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support wakeup requests.", aLocation);
	}

	@Deprecated
	public void scheduleSignalChange(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGObjectWriter aObjectWriter, SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support signals.", aLocation);
	}

	public void cancelAllWakeups(SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support wakeup requests.", aLocation);
	}

	public BigInteger getCurrentTime(SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support simulation time retrieval.", aLocation);
	}

	public void scheduleSignalChange(boolean aInertial, IGStaticValue aDelay, IGStaticValue aReject, IGStaticValue aValue, IGObjectDriver aIgObjectDriver, SourceLocation aLocation) throws ZamiaException {
		throw new ZamiaException("Error: this environment doesn't support signals.", aLocation);
	}

}
