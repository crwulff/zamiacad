/* 
 * Copyright 2010,2011 by the authors indicated in the @author tags. 
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
import org.zamia.ToplevelPath;
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
import org.zamia.instgraph.IGOperationUnary.UnaryOp;
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
import org.zamia.instgraph.synth.model.IGSMExprEngine;
import org.zamia.instgraph.synth.model.IGSMExprNode;
import org.zamia.instgraph.synth.model.IGSMExprNodeClockEdge;
import org.zamia.instgraph.synth.model.IGSMIf;
import org.zamia.instgraph.synth.model.IGSMSequenceOfStatements;
import org.zamia.rtl.RTLManager;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.RTLType;
import org.zamia.rtl.RTLValue;
import org.zamia.rtl.RTLValueBuilder;
import org.zamia.rtl.RTLPort.PortDir;
import org.zamia.rtl.RTLType.TypeCat;
import org.zamia.rtl.RTLValue.BitValue;
import org.zamia.rtl.nodes.RTLNBinaryOp.BinaryOp;
import org.zamia.util.Pair;
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

	private HashMap<Long, RTLSignal> fSignals;

	private HashMap<Long, RTLType> fTypeCache;

	// very crude implementation of common sub-expressions:
	// string-representation of operation -> resulting signal
	private HashMap<String, RTLSignal> fLogicCache;

	private final RTLValue fBitValue0;

	private final RTLValue fBitValue1;

	private final RTLValue fBitValueU;

	private final RTLValue fBitValueX;

	private IGStructure fStructure;

	public IGSynth(ZamiaProject aZPrj) throws ZamiaException {
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

		fTypeCache = new HashMap<Long, RTLType>();
		fLogicCache = new HashMap<String, RTLSignal>();

		fBitValue0 = new RTLValueBuilder(getBitType(), null, fZDB).setBit(BitValue.BV_0).buildValue();
		fBitValue1 = new RTLValueBuilder(getBitType(), null, fZDB).setBit(BitValue.BV_1).buildValue();
		fBitValueU = new RTLValueBuilder(getBitType(), null, fZDB).setBit(BitValue.BV_U).buildValue();
		fBitValueX = new RTLValueBuilder(getBitType(), null, fZDB).setBit(BitValue.BV_X).buildValue();

	}

	public IGStmtSynthAdapter getSynthAdapter(IGSequentialStatement aStmt) {
		Class<? extends IGSequentialStatement> c = aStmt.getClass();
		return fStmtSynthAdapters.get(c);
	}

	public IGOperationSynthAdapter getSynthAdapter(IGOperation aOp) {
		Class<? extends IGOperation> c = aOp.getClass();
		return fOperationSynthAdapters.get(c);
	}

	public RTLModule synthesizePorts(IGModule aModule) throws ZamiaException {

		DMUID dmuid = aModule.getDUUID();
		ToplevelPath tlp = aModule.getStructure().getPath();

		fSignals = new HashMap<Long, RTLSignal>();

		fModule = new RTLModule(tlp, dmuid, aModule.computeSourceLocation(), fZDB);

		fStructure = aModule.getStructure();

		synthesizeStructure(fStructure, true, false);

		return fModule;
	}

	public void synthesizeBody() throws ZamiaException {
		synthesizeStructure(fStructure, false, true);
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

	private void synthesizeStructure(IGStructure aIGS, boolean aSynthPorts, boolean aSynthBody) throws ZamiaException {

		IGContainer container = aIGS.getContainer();

		if (aSynthPorts) {
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
		}

		if (aSynthBody) {

			int n = aIGS.getNumStatements();
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
	}

	private void synthesizeProcess(IGProcess aProc) throws ZamiaException {

		IGSequenceOfStatements sos = aProc.getSequenceOfStatements();

		logger.debug("IGSynth: synthesizeProcess():  ***** computing bindings for process " + this + " *****");

		logger.debug("IGSynth: synthesizeProcess():  Original code:");

		sos.dump(0);

		/*
		 * Pass 1: Preprocessing
		 * 
		 * inline SubPrograms, unroll loops, remove wait stmts, detect global clock
		 */

		logger.debug("IGSynth: synthesizeProcess():  Pass 1: preprocessing");

		int n = sos.getNumStatements();

		IGSMSequenceOfStatements preprocessedSOS = new IGSMSequenceOfStatements(aProc.getLabel(), sos.computeSourceLocation(), this);

		IGSMSequenceOfStatements pSOS = preprocessedSOS;

		IGObjectRemapping or = new IGObjectRemapping(this);

		for (int i = 0; i < n; i++) {

			IGSequentialStatement stmt = sos.getStatement(i);

			if (stmt instanceof IGSequentialWait) {

				IGSequentialWait waitStmt = (IGSequentialWait) stmt;

				if (i == 0) {
					IGSMExprNode globalClock = findClock(waitStmt, pSOS);

					pSOS = new IGSMSequenceOfStatements(null, sos.computeSourceLocation(), this);

					IGSMIf ic = new IGSMIf(globalClock, pSOS, new IGSMSequenceOfStatements(null, sos.computeSourceLocation(), this), null, waitStmt.computeSourceLocation(), this);

					preprocessedSOS.add(ic);

				} else {

					// if this is not the implicit wait stmt at the
					// end of the process (location==null) => throw exception
					if (waitStmt.computeSourceLocation() != null) {
						throw new ZamiaException("Not synthesizable.");
					}
				}

			} else {
				getSynthAdapter(stmt).inline(stmt, or, pSOS, null, this);
			}
		}

		logger.debug("IGSynth: synthesizeProcess(): preprocessing done:");
		preprocessedSOS.dump(0);

		/*
		 * Pass 2: compute bindings
		 * 
		 */

		logger.debug("IGSynth: synthesizeProcess():  Pass 2: compute bindings");

		IGBindings bindings = new IGBindings();

		bindings = preprocessedSOS.computeBindings(bindings, this);

		logger.debug("IGSynth: synthesizeProcess():  " + bindings.getNumBindings() + " bindings computed.");

		bindings.dumpBindings();

		/*
		 * Phase 4: generate RTL graph
		 */

		bindings.synthesize(this);
	}

	public RTLType getBitType() {
		return fRTLM.getBitType();
	}

	private RTLType getCachedType(IGTypeStatic aType) {

		long dbid = aType.getDBID();

		return fTypeCache.get(dbid);
	}

	private void setCachedType(IGTypeStatic aType, RTLType aT) {
		fTypeCache.put(aType.getDBID(), aT);
	}

	public RTLType synthesizeType(IGType aType) throws ZamiaException {

		SourceLocation location = aType.computeSourceLocation();

		if (!(aType instanceof IGTypeStatic)) {
			throw new ZamiaException("Type " + aType + " is not static => not synthesizable", location);
		}

		IGTypeStatic type = (IGTypeStatic) aType;

		RTLType t = getCachedType(type);
		if (t != null) {
			return t;
		}

		switch (type.getCat()) {
		case ARRAY:

			RTLType et = synthesizeType(type.getStaticElementType(null));

			IGTypeStatic it = type.getStaticIndexType(null);

			t = new RTLType(TypeCat.ARRAY, location, fZDB);

			t.setArrayParams(et, (int) it.getStaticLeft(location).getOrd(), it.isAscending(), (int) it.getStaticRight(location).getOrd());

			break;

		case RECORD:
			t = new RTLType(TypeCat.RECORD, location, fZDB);

			int n = type.getNumRecordFields(location);
			for (int i = 0; i < n; i++) {

				IGRecordField rf = type.getRecordField(i, location);

				t.addField(rf.getId(), synthesizeType(rf.getType()));

			}

			break;

		case INTEGER:

			int w = 32;

			if (type.getRange() instanceof IGStaticValue) {
				long low = type.getStaticLow(location).getOrd();
				long high = type.getStaticHigh(location).getOrd();

				long card = high - low + 1;
				w = (int) Math.ceil(Math.log(card) / Math.log(2));
			}

			t = fRTLM.getBitVectorType(w);
			break;

		default:
			if (!type.isBit() && !type.isBool() && !isStdLogic(type)) {
				throw new ZamiaException("Type " + type + " is not synthesizable", location);
			}

			t = fRTLM.getBitType();
		}

		setCachedType(type, t);

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

	public IGSMExprNode findClock(IGSequentialWait aWaitStmt, IGSMSequenceOfStatements aPreprocessedSOS) throws ZamiaException {

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

		return getSynthAdapter(cond).preprocess(cond, null, aPreprocessedSOS, this);
	}

	private RTLSignal findSignal(IGOperation aOp) throws ZamiaException {

		if (!(aOp instanceof IGOperationObject)) {
			return null;
		}

		IGOperationObject oo = (IGOperationObject) aOp;

		IGObject signal = oo.getObject();
		if (signal.getCat() != IGObjectCat.SIGNAL) {
			return null;
		}

		return getOrCreateSignal(signal);
	}

	private Pair<IGOperation, IGOperation> findEquals(IGOperation aOp) {

		if (aOp instanceof IGOperationInvokeSubprogram) {

			IGOperationInvokeSubprogram inv = (IGOperationInvokeSubprogram) aOp;

			String opStr = inv.getSub().getId();

			BinOp bop = IGSAOperationInvokeSubprogram.identifyStdBinOp(opStr);
			if (bop != BinOp.EQUAL) {
				return null;
			}

			return new Pair<IGOperation, IGOperation>(inv.getMapping(0).getActual(), inv.getMapping(1).getActual());
		}

		if (!(aOp instanceof IGOperationBinary)) {
			return null;
		}

		IGOperationBinary bop = (IGOperationBinary) aOp;
		if (bop.getBinOp() != BinOp.EQUAL) {
			return null;
		}

		return new Pair<IGOperation, IGOperation>(bop.getA(), bop.getB());
	}

	private IGOperationAttribute findAttr(IGOperation aOp) {
		if (aOp instanceof IGOperationAttribute) {
			IGOperationAttribute opattr = (IGOperationAttribute) aOp;

			if (opattr.getAttrOp() != AttrOp.EVENT) {
				return null;
			}

			return opattr;
		}
		return null;
	}

	public IGSMExprNodeClockEdge findClock(IGOperation aOp) throws ZamiaException {
		SourceLocation location = aOp.computeSourceLocation();

		IGOperation a = null;
		IGOperation b = null;

		if (aOp instanceof IGOperationInvokeSubprogram) {

			IGOperationInvokeSubprogram inv = (IGOperationInvokeSubprogram) aOp;

			String opStr = inv.getSub().getId();

			if (opStr.equals("RISING_EDGE")) {

				a = inv.getMapping(0).getActual();

				RTLSignal rtls = findSignal(a);

				if (rtls == null) {
					return null;
				}

				return new IGSMExprNodeClockEdge(rtls, true, location, this);
			} else if (opStr.equals("FALLING_EDGE")) {

				a = inv.getMapping(0).getActual();

				RTLSignal rtls = findSignal(a);

				if (rtls == null) {
					return null;
				}

				return new IGSMExprNodeClockEdge(rtls, false, location, this);
			}

			BinOp bop = IGSAOperationInvokeSubprogram.identifyStdBinOp(opStr);
			if (bop != BinOp.AND) {
				return null;
			}

			a = inv.getMapping(0).getActual();
			b = inv.getMapping(1).getActual();

		} else if (aOp instanceof IGOperationBinary) {

			IGOperationBinary bop = (IGOperationBinary) aOp;

			if (bop.getBinOp() != BinOp.AND) {
				return null;
			}

			a = bop.getA();
			b = bop.getB();
		}

		IGOperationAttribute opattr = null;
		Pair<IGOperation, IGOperation> compare = findEquals(a);
		if (compare == null) {
			compare = findEquals(b);
			if (compare == null) {
				return null;
			}

			opattr = findAttr(a);

		} else {
			opattr = findAttr(b);
		}

		if (opattr == null) {
			return null;
		}

		IGOperationObject oo = null;
		IGStaticValue sv = null;
		a = compare.getFirst();
		b = compare.getSecond();
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

		RTLSignal rtls = findSignal(oo);

		return new IGSMExprNodeClockEdge(rtls, rising, location, this);
	}

	public RTLModule getRTLModule() {
		return fModule;
	}

	public RTLManager getRTLM() {
		return fRTLM;
	}

	public RTLValue getBitValue(BitValue aBV) {

		switch (aBV) {
		case BV_0:
			return fBitValue0;
		case BV_1:
			return fBitValue1;
		case BV_U:
			return fBitValueU;
		case BV_X:
			return fBitValueX;
		}

		return null;
	}

	public RTLSignal placeLiteral(RTLValue aValue, SourceLocation aLocation) throws ZamiaException {

		String signature = "L" + aValue;

		RTLSignal s = fLogicCache.get(signature);

		if (s == null) {
			s = fModule.createLiteral(aValue, aLocation);
			fLogicCache.put(signature, s);
		}

		return s;
	}

	public RTLSignal placeMUX(RTLSignal aS, RTLSignal aA, RTLSignal aB, SourceLocation aLocation) throws ZamiaException {

		String signature = "MUX" + aS.getId() + "###" + aA.getId() + "###" + aB.getId();

		RTLSignal s = fLogicCache.get(signature);

		if (s == null) {
			s = fModule.createComponentMUX(aS, aA, aB, aLocation);
			fLogicCache.put(signature, s);
		}

		return s;
	}

	private BinaryOp mapBinOp(BinOp aOp) {

		switch (aOp) {
		case ADD:
			return BinaryOp.ADD;
		case SUB:
			return BinaryOp.SUB;
		case MUL:
			return BinaryOp.MUL;
		case DIV:
			return BinaryOp.DIV;
		case MOD:
			return BinaryOp.MOD;
		case REM:
			return BinaryOp.REM;
		case POWER:
			return BinaryOp.POWER;
		case EQUAL:
			return BinaryOp.EQUAL;
		case LESSEQ:
			return BinaryOp.LESSEQ;
		case LESS:
			return BinaryOp.LESS;
		case GREATER:
			return BinaryOp.GREATER;
		case GREATEREQ:
			return BinaryOp.GREATEREQ;
		case NEQUAL:
			return BinaryOp.NEQUAL;
		case AND:
			return BinaryOp.AND;
		case NAND:
			return BinaryOp.NAND;
		case OR:
			return BinaryOp.OR;
		case NOR:
			return BinaryOp.NOR;
		case XOR:
			return BinaryOp.XOR;
		case XNOR:
			return BinaryOp.XNOR;
		case MIN:
			return BinaryOp.MIN;
		case MAX:
			return BinaryOp.MAX;
		case SLL:
			return BinaryOp.SLL;
		case SRL:
			return BinaryOp.SRL;
		case SLA:
			return BinaryOp.SLA;
		case SRA:
			return BinaryOp.SRA;
		case ROL:
			return BinaryOp.ROL;
		case ROR:
			return BinaryOp.ROR;
		case CONCAT:
			return BinaryOp.CONCAT;
		}

		return null;

	}

	public RTLSignal placeBinary(BinOp aOp, RTLSignal aA, RTLSignal aB, SourceLocation aLocation) throws ZamiaException {
		String signature = aOp.name() + aA.getId() + "###" + aB.getId();

		RTLSignal s = fLogicCache.get(signature);

		if (s == null) {
			s = fModule.createComponentBinary(mapBinOp(aOp), aA, aB, aLocation);
			fLogicCache.put(signature, s);
		}

		return s;
	}

	public RTLSignal placeUnary(UnaryOp aOp, RTLSignal aA, SourceLocation aLocation) throws ZamiaException {
		String signature = aOp.name() + aA.getId();

		RTLSignal s = fLogicCache.get(signature);

		if (s == null) {
			s = fModule.createComponentUnary(mapUnaryOp(aOp), aA, aLocation);
			fLogicCache.put(signature, s);
		}

		return s;
	}

	private org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp mapUnaryOp(UnaryOp aOp) {
		switch (aOp) {
		case ABS:
			return org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp.ABS;
		case BUF:
			return org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp.BUF;
		case NEG:
			return org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp.NEG;
		case NOT:
			return org.zamia.rtl.nodes.RTLNUnaryOp.UnaryOp.NOT;
		}
		return null;
	}

	public RTLSignal placeReg(RTLSignal aAE, RTLSignal aAD, RTLSignal aE, RTLSignal aD, RTLSignal aClk, SourceLocation aLocation) throws ZamiaException {
		return fModule.createComponentReg(aAE, aAD, aE, aD, aClk, aLocation);
	}

	public IGSMExprEngine getEE() {
		return IGSMExprEngine.getInstance();
	}

	public void sigjoin(RTLSignal aDriving, RTLSignal aReceiving, SourceLocation aLocation) throws ZamiaException {
		fModule.sigJoin(aDriving, aReceiving, aLocation);
	}

}
