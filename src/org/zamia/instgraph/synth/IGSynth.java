/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Nov 20, 2010
 */
package org.zamia.instgraph.synth;

import java.util.ArrayList;
import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGConcurrentStatement;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGContainerItem;
import org.zamia.instgraph.IGInstantiation;
import org.zamia.instgraph.IGModule;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAttribute;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.IGOperationBinary;
import org.zamia.instgraph.IGOperationBinary.BinOp;
import org.zamia.instgraph.IGOperationIndex;
import org.zamia.instgraph.IGOperationInvokeSubprogram;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGOperationObject;
import org.zamia.instgraph.IGOperationRange;
import org.zamia.instgraph.IGProcess;
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGRecordField;
import org.zamia.instgraph.IGSequenceOfStatements;
import org.zamia.instgraph.IGSequentialAssignment;
import org.zamia.instgraph.IGSequentialIf;
import org.zamia.instgraph.IGSequentialRestart;
import org.zamia.instgraph.IGSequentialStatement;
import org.zamia.instgraph.IGSequentialWait;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStructure;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.synth.adapters.IGSAOperationAttribute;
import org.zamia.instgraph.synth.adapters.IGSAOperationBinary;
import org.zamia.instgraph.synth.adapters.IGSAOperationIndex;
import org.zamia.instgraph.synth.adapters.IGSAOperationInvokeSubprogram;
import org.zamia.instgraph.synth.adapters.IGSAOperationLiteral;
import org.zamia.instgraph.synth.adapters.IGSAOperationObject;
import org.zamia.instgraph.synth.adapters.IGSAOperationRange;
import org.zamia.instgraph.synth.adapters.IGSARange;
import org.zamia.instgraph.synth.adapters.IGSASequenceOfStatements;
import org.zamia.instgraph.synth.adapters.IGSASequentialAssignment;
import org.zamia.instgraph.synth.adapters.IGSASequentialIf;
import org.zamia.instgraph.synth.adapters.IGSASequentialRestart;
import org.zamia.instgraph.synth.adapters.IGSAStaticValue;
import org.zamia.rtlng.RTLManager;
import org.zamia.rtlng.RTLModule;
import org.zamia.rtlng.RTLPort;
import org.zamia.rtlng.RTLPort.PortDir;
import org.zamia.rtlng.RTLSignal;
import org.zamia.rtlng.RTLType;
import org.zamia.rtlng.RTLType.TypeCat;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.zdb.ZDB;

/**
 * Visit IG, generate RTL if it is synthesizable
 * 
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("rawtypes")
public class IGSynth {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	private final ZamiaProject fZPrj;

	private final RTLManager fRTLM;

	private final ZDB fZDB;

	private HashMap<Class, IGStmtSynthAdapter> fStmtSynthAdapters;

	private HashMap<Class, IGOperationSynthAdapter> fOperationSynthAdapters;

	private IGInterpreterRuntimeEnv fEnv;

	private RTLModule fModule;

	private HashMap<IGOperationObject, IGBinding> fVariableOperationBindings; // for variables

	private HashMap<IGBinding, RTLSignal> fBindingSynthCache;

	private HashMap<Long, RTLSignal> fSignals;

	public IGSynth(ZamiaProject aZPrj) {
		fZPrj = aZPrj;
		fZDB = fZPrj.getZDB();

		fRTLM = fZPrj.getRTLM();

		fEnv = new IGInterpreterRuntimeEnv(null, fZPrj);
		fEnv.enterContext();

		fStmtSynthAdapters = new HashMap<Class, IGStmtSynthAdapter>();
		fStmtSynthAdapters.put(IGSequentialIf.class, new IGSASequentialIf());
		fStmtSynthAdapters.put(IGSequenceOfStatements.class, new IGSASequenceOfStatements());
		fStmtSynthAdapters.put(IGSequentialAssignment.class, new IGSASequentialAssignment());
		fStmtSynthAdapters.put(IGSequentialRestart.class, new IGSASequentialRestart());

		fOperationSynthAdapters = new HashMap<Class, IGOperationSynthAdapter>();
		fOperationSynthAdapters.put(IGOperationBinary.class, new IGSAOperationBinary());
		fOperationSynthAdapters.put(IGStaticValue.class, new IGSAStaticValue());
		fOperationSynthAdapters.put(IGOperationObject.class, new IGSAOperationObject());
		fOperationSynthAdapters.put(IGOperationInvokeSubprogram.class, new IGSAOperationInvokeSubprogram());
		fOperationSynthAdapters.put(IGOperationIndex.class, new IGSAOperationIndex());
		fOperationSynthAdapters.put(IGOperationLiteral.class, new IGSAOperationLiteral());
		fOperationSynthAdapters.put(IGOperationRange.class, new IGSAOperationRange());
		fOperationSynthAdapters.put(IGOperationAttribute.class, new IGSAOperationAttribute());
		fOperationSynthAdapters.put(IGRange.class, new IGSARange());

	}

	public IGStmtSynthAdapter getSynthAdapter(IGSequentialStatement aStmt) {
		Class<? extends IGSequentialStatement> c = aStmt.getClass();
		return fStmtSynthAdapters.get(c);
	}

	public IGOperationSynthAdapter getSynthAdapter(IGOperation aOp) {
		Class<? extends IGOperation> c = aOp.getClass();
		return fOperationSynthAdapters.get(c);
	}

	public RTLModule synthesize(IGModule aModule) throws ZamiaException {

		DMUID dmuid = aModule.getDUUID();

		fVariableOperationBindings = new HashMap<IGOperationObject, IGBinding>();
		fSignals = new HashMap<Long, RTLSignal>();
		fBindingSynthCache = new HashMap<IGBinding, RTLSignal>();

		fModule = new RTLModule(dmuid.getUID(), "", aModule.computeSourceLocation(), fZDB);

		IGStructure igs = aModule.getStructure();

		synthesizeStructure(igs);

		return fModule;
	}

	private PortDir mapPortDir(OIDir aDir) {

		switch (aDir) {
		case IN:
			return PortDir.IN;
		case OUT:
			return PortDir.OUT;
		case INOUT:
			return PortDir.INOUT;
		case BUFFER:
			return PortDir.BUFFER;
		}
		return PortDir.LINKAGE;
	}

	public RTLSignal getOrCreateSignal(IGObject aObject) throws ZamiaException {

		long id = aObject.store();

		RTLSignal s = fSignals.get(id);
		if (s == null) {

			String uid = findUID(aObject.getId());

			RTLType type = synthesizeType(aObject.getType());

			s = fModule.createSignal(uid, type, aObject.computeSourceLocation());

			fSignals.put(id, s);
		}

		return s;
	}

	private String findUID(String aId) {
		String uid = aId != null ? aId : "us";
		int idx = 0;
		while (fModule.findSignal(uid) != null) {
			idx++;
			uid = aId + "_" + idx;
		}
		return uid;
	}

	private void synthesizeStructure(IGStructure aIGS) throws ZamiaException {

		IGContainer container = aIGS.getContainer();

		int n = container.getNumLocalItems();
		for (int i = 0; i < n; i++) {

			IGContainerItem item = container.getLocalItem(i);

			if (!(item instanceof IGObject)) {
				continue;
			}

			IGObject igo = (IGObject) item;

			IGType type = igo.getType();

			RTLType t = synthesizeType(type);

			String uid = findUID(item.getId());

			OIDir direction = igo.getDirection();

			RTLSignal s = null;

			if (direction != OIDir.NONE) {
				PortDir dir = mapPortDir(igo.getDirection());
				RTLPort p = fModule.createPort(uid, t, dir, igo.computeSourceLocation());
				s = p.getSignal();
			} else {
				s = fModule.createSignal(uid, t, igo.computeSourceLocation());
			}
			long dbid = item.store();
			fSignals.put(dbid, s);

			logger.info("IGSynth: Generated %s => %s", igo, s);
		}

		n = aIGS.getNumStatements();
		for (int i = 0; i < n; i++) {

			IGConcurrentStatement stmt = aIGS.getStatement(i);

			if (stmt instanceof IGProcess) {

				IGProcess proc = (IGProcess) stmt;

				logger.info("IGSynth: synthesizing process %s", proc);

				synthesizeProcess(proc);

			} else if (stmt instanceof IGInstantiation) {

				logger.info("IGSynth: synthesizing instantiation %s", stmt);

				// FIXME: implement

				throw new ZamiaException("Sorry, not implemented.");

			} else if (stmt instanceof IGStructure) {

				logger.info("IGSynth: synthesizing nested structure %s", stmt);

				// FIXME: implement

				throw new ZamiaException("Sorry, not implemented.");

			} else {
				throw new ZamiaException("IGSynth: Unknown statement: " + stmt, stmt.computeSourceLocation());
			}
		}
	}

	private void synthesizeProcess(IGProcess aProc) throws ZamiaException {

		IGSequenceOfStatements sos = aProc.getSequenceOfStatements();

		logger.debug("IGSynth: synthesizeProcess():  ***** computing bindings for process " + this + " *****");

		logger.debug("IGSynth: synthesizeProcess():  Original code:");

		sos.dump(0);

		/*
		 * Phase 1: inline SubPrograms, remove wait stmts, detect global clock
		 */

		logger.debug("IGSynth: synthesizeProcess():  Phase 1: inline SubPrograms, remove wait stmts, detect global clock");

		int n = sos.getNumStatements();
		IGClock globalClock = null;
		IGSequenceOfStatements inlinedSOS = new IGSequenceOfStatements(aProc.getLabel(), sos.computeSourceLocation(), fZDB);
		IGObjectRemapping or = new IGObjectRemapping(this);

		for (int i = 0; i < n; i++) {

			IGSequentialStatement stmt = sos.getStatement(i);

			if (stmt instanceof IGSequentialWait) {

				IGSequentialWait waitStmt = (IGSequentialWait) stmt;

				if (i == 0) {
					globalClock = findClock(waitStmt);
				}

			} else {
				getSynthAdapter(stmt).inlineSubprograms(stmt, or, inlinedSOS, null, this);
			}
		}

		logger.debug("IGSynth: synthesizeProcess():  Subprograms inlined:");
		inlinedSOS.dump(0);

		/*
		 * Phase 2: resolve variables
		 */

		logger.debug("IGSynth: synthesizeProcess():  Phase 2: resolve variables");
		IGBindings vb = new IGBindings();

		IGSequenceOfStatements resolvedSOS = new IGSequenceOfStatements(aProc.getLabel(), sos.computeSourceLocation(), fZDB);

		IGBindings lastBindings = getSynthAdapter(inlinedSOS).resolveVariables(inlinedSOS, resolvedSOS, vb, resolvedSOS, null, or, this);

		logger.debug("IGSynth: synthesizeProcess():  Variables resolved:");
		resolvedSOS.dump(0);

		logger.debug("IGSynth: synthesizeProcess():  Variable bindings:");

		lastBindings.dumpBindings();

		/*
		 * Phase 3: compute bindings
		 */

		logger.debug("IGSynth: synthesizeProcess():  Phase 3: compute bindings");

		IGBindings bindings = getSynthAdapter(resolvedSOS).computeBindings(resolvedSOS, resolvedSOS, lastBindings, globalClock, this);

		logger.debug("IGSynth: synthesizeProcess():  " + bindings.getNumBindings() + " bindings computed.");

		bindings.dumpBindings();

		/*
		 * Phase 4: generate RTL graph
		 */

		bindings.elaborate(this);
	}

	public RTLType synthesizeType(IGType aType) throws ZamiaException {

		SourceLocation location = aType.computeSourceLocation();

		if (!(aType instanceof IGTypeStatic)) {
			throw new ZamiaException("Type " + aType + " is not static => not synthesizable", location);
		}

		IGTypeStatic type = (IGTypeStatic) aType;

		RTLType t = fRTLM.getCachedType(type);
		if (t != null) {
			return t;
		}

		switch (type.getCat()) {
		case ARRAY:

			RTLType et = synthesizeType(type.getStaticElementType(null));

			IGTypeStatic it = type.getStaticIndexType(null);

			t = new RTLType(TypeCat.ARRAY, location, fZDB);

			t.setArrayParams(et, (int) it.getStaticLeft(location).getOrd(), it.isAscending(), (int) it.getStaticLeft(location).getOrd());

			break;

		case RECORD:
			t = new RTLType(TypeCat.RECORD, location, fZDB);

			int n = type.getNumRecordFields(location);
			for (int i = 0; i < n; i++) {

				IGRecordField rf = type.getRecordField(i, location);

				t.addField(rf.getId(), synthesizeType(rf.getType()));

			}

			break;

		default:
			if (!type.isBit() && !type.isBool() && !isStdLogic(type)) {
				throw new ZamiaException("Type " + type + " is not synthesizable", location);
			}

			t = fRTLM.getBitType();
		}

		fRTLM.setCachedType(type, t);

		return t;
	}

	private boolean isStdLogic(IGTypeStatic aType) {
		// FIXME: check values ?
		return aType.isCharEnum();
	}

	public ZDB getZDB() {
		return fZDB;
	}

	public IGInterpreterRuntimeEnv getRuntimeEnv() {
		return fEnv;
	}

	public void setVariableOperationBinding(IGOperationObject aOp, IGBinding aBinding) {
		fVariableOperationBindings.put(aOp, aBinding);
	}

	public IGBinding getVariableOperationBinding(IGOperationObject aOp) {
		return fVariableOperationBindings.get(aOp);
	}

	public IGClock findClock(IGSequentialWait aWaitStmt) throws ZamiaException {

		ArrayList<IGOperation> sens = aWaitStmt.getSensitivityList();
		if (sens != null) {
			return null;
		}
		IGOperation timeout = aWaitStmt.getTimeoutClause();
		if (timeout != null) {
			return null;
		}

		IGOperation cond = aWaitStmt.getConditionClause();
		if (cond == null) {
			return null;
		}
		return findClock(cond);

	}

	public IGClock findClock(IGOperation aOp) throws ZamiaException {

		// clk'event and clk='1'

		if (aOp instanceof IGOperationBinary) {

			IGOperationBinary bop = (IGOperationBinary) aOp;

			if (bop.getBinOp() != BinOp.AND) {
				return null;
			}

			IGOperation a = bop.getA();
			IGOperation b = bop.getB();

			IGOperationAttribute opattr = null;
			IGOperationBinary compare = null;

			if ((a instanceof IGOperationAttribute) && (b instanceof IGOperationBinary)) {

				opattr = (IGOperationAttribute) a;

				IGOperationBinary bop2 = (IGOperationBinary) b;
				if (bop2.getBinOp() != BinOp.EQUAL) {
					return null;
				}
				compare = bop2;
			} else if ((b instanceof IGOperationAttribute) && (a instanceof IGOperationBinary)) {

				opattr = (IGOperationAttribute) b;

				IGOperationBinary bop2 = (IGOperationBinary) a;
				if (bop2.getBinOp() != BinOp.EQUAL) {
					return null;
				}
				compare = bop2;
			} else {
				return null;
			}

			if (opattr.getAttrOp() != AttrOp.EVENT) {
				return null;
			}

			IGOperationObject oo = null;
			IGStaticValue sv = null;
			a = compare.getA();
			b = compare.getB();
			if ((a instanceof IGOperationObject) && (b instanceof IGStaticValue)) {
				oo = (IGOperationObject) a;
				sv = (IGStaticValue) b;
			} else if ((b instanceof IGOperationObject) && (a instanceof IGStaticValue)) {
				oo = (IGOperationObject) b;
				sv = (IGStaticValue) a;
			} else {
				return null;
			}

			IGTypeStatic t = sv.getStaticType();
			if (!t.isLogic()) {
				return null;
			}

			boolean rising = sv.isLogicOne();

			IGObject signal = oo.getObject();
			if (signal.getCat() != IGObjectCat.SIGNAL) {
				return null;
			}

			return new IGClock(signal, rising);

		}

		// FIXME: implement RISING_EDGE...

		return null;

	}

	public RTLModule getRTLModule() {
		return fModule;
	}

	public void setCachedBindingSynth(IGBinding aBinding, RTLSignal aDest) {
		fBindingSynthCache.put(aBinding, aDest);
	}

	public RTLSignal getCachedBindingSynth(IGBinding aBinding) {
		return fBindingSynthCache.get(aBinding);
	}

}
