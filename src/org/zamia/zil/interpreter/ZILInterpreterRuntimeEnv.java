/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 25, 2008
 */
package org.zamia.zil.interpreter;

import java.util.Stack;

import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.sim.PortVarWriter;
import org.zamia.rtl.sim.Simulator;
import org.zamia.util.ZStack;
import org.zamia.zil.ZILIReferable;
import org.zamia.zil.ZILValue;
import org.zamia.zil.interpreter.ZILStmt.ReturnStatus;


/**
 * Stack, PC and Variable contexts for executing InterpreterCode
 * 
 * 
 * @author Guenter Bartsch
 * 
 */
public class ZILInterpreterRuntimeEnv {

	public RTLPort getCurrentEventPort() {
		return fCurrentEventPort;
	}

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private Stack<ZILVariableContext> fContexts;

	private ZStack<ZILStackFrame> fStack;

	private int fPC;

	private RTLPort fCurrentEventPort = null;

	private int wakeupCode = 1;

	private boolean fIsWakeup = false;

	private ZILInterpreter fInterpreter;

	private ZILInterpreterCode fCode;
	
	class CallStackEntry {
		ZILInterpreterCode fCode;
		int fPC;
		public CallStackEntry(ZILInterpreterCode aCode, int aPC) {
			fCode = aCode;
			fPC = aPC;
		}
	}

	private ZStack<CallStackEntry> fCallStack;

	public ZILInterpreterRuntimeEnv(ZILInterpreterCode aCode, ZILInterpreter aInterpreter) {
		reset();
		fInterpreter = aInterpreter;
		fCode = aCode;
	}

	public void reset() {
		fPC = 0;
		fContexts = new Stack<ZILVariableContext>();
		fStack = new ZStack<ZILStackFrame>();
		fCallStack = new ZStack<CallStackEntry>();
		fCurrentEventPort = null;
	}

	public void resume(Simulator aSimulator, RTLPort aPort, int aWakeupCode) throws ZamiaException {

		fCurrentEventPort = aPort;

		fIsWakeup = aWakeupCode > 0 && aWakeupCode == wakeupCode;

		logger.debug("Interpreter: resuming code for %s, port=%s, isWakeup=%b", fCode.getId(), aPort, fIsWakeup);

		while (true) {
			ZILStmt stmt = fCode.get(fPC++);
			logger.debug("Interpreter: executing: %s:%d %s", fCode.getId(), fPC - 1, stmt);
			ReturnStatus status = stmt.execute(aSimulator, this, fInterpreter);

			if (status == ReturnStatus.WAIT) {
				break;
			} else if (status == ReturnStatus.RETURN) {
				
				CallStackEntry entry = fCallStack.pop();
				fPC = entry.fPC;
				fCode = entry.fCode;
			}
			
			if (fPC >= fCode.size())
				break;
		}
	}

	public void call(ZILInterpreterCode aCode, Simulator aSim, ZILInterpreter aInterpreter) throws ZamiaException {

		fCallStack.push(new CallStackEntry(fCode, fPC));
		
		fCode = aCode;
		fPC = 0;
	}

	public int getPC() {
		return fPC;
	}

	public void push(ZILStackFrame aStackFrame) {
		logger.debug("Interpreter: pushing %s", aStackFrame);
		fStack.push(aStackFrame);
	}

	public ZILStackFrame pop() throws ZamiaException {
		if (fStack.isEmpty())
			throw new ZamiaException("Stack underflow error.");
		ZILStackFrame sf = fStack.pop();
		logger.debug("Interpreter: popped %s", sf);
		return sf;
	}

	public void enterContext() {
		fContexts.push(new ZILVariableContext());
	}

	public void exitContext() {
		fContexts.pop();
	}

	public void newObject(ZILIReferable aObj) throws ZamiaException {
		ZILVariableContext vc = fContexts.peek();
		vc.objects.put(aObj, ZILValue.generateZ(aObj.getType(), null, null));
	}

	public ZILValue getObjectValue(ZILIReferable aObject) {
		int n = fContexts.size();
		for (int i = n - 1; i >= 0; i--) {
			ZILVariableContext vc = fContexts.get(i);
			ZILValue iv = vc.objects.get(aObject);
			if (iv != null)
				return iv;
		}
		return null;
	}

	public void setObjectValue(ZILIReferable aObject, ZILValue aValue) throws ZamiaException {
		int n = fContexts.size();
		for (int i = n - 1; i >= 0; i--) {
			ZILVariableContext vc = fContexts.get(i);
			ZILValue iv = vc.objects.get(aObject);
			if (iv != null) {
				vc.objects.put(aObject, aValue);
				return;
			}
		}
		throw new ZamiaException("Error: variable not found: " + aObject);
	}

	public void setPC(int aAdr) {
		fPC = aAdr;
	}

	public void push(ZILValue aValue) {
		push(new ZILStackFrame(aValue));
	}

	public void push(PortVarWriter aSw) {
		push(new ZILStackFrame(aSw));
	}


	public int popInt(Simulator aSimulator) throws ZamiaException {
		ZILStackFrame sf = pop();
		return sf.getInt(aSimulator);
	}

	public void requestWakeup(long aT, Simulator aSimulator) {
		wakeupCode++;
		aSimulator.scheduleWakeup(aT, wakeupCode, this);
	}

	public boolean isTimeout() {
		return fIsWakeup;
	}

	public void waitDone() {
		fCurrentEventPort = null;
		fIsWakeup = false;
	}

}
