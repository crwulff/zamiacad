/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 22, 2009
 */
package org.zamia.instgraph;

import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGObject.IGObjectCat;
import org.zamia.instgraph.IGObject.OIDir;
import org.zamia.instgraph.interpreter.IGInterpreterCode;
import org.zamia.instgraph.interpreter.IGMapStmt;
import org.zamia.instgraph.interpreter.IGPopStmt;
import org.zamia.zdb.ZDB;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGMapping extends IGItem {

	public static final String SYNCHRO_IN_ID = "_synchro_IN";

	public static final String SYNCHRO_OUT_ID = "_synchro_OUT";

	private IGOperation fFormal;

	private IGOperation fActual;

	public IGMapping(IGOperation aFormal, IGOperation aActual, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fFormal = aFormal;
		fActual = aActual;
	}

	public IGOperation getFormal() {
		return fFormal;
	}

	public IGOperation getActual() {
		return fActual;
	}

	/*
	 * used for fake processes that handle instantiation mappings in simulator
	 */
	public void generateInstantiationCode(boolean aIgnoreDir, IGInterpreterCode aIc, SourceLocation aSrc) throws ZamiaException {

		OIDir dir = fFormal.getDirection();

		if (aIgnoreDir || dir == OIDir.IN || dir == OIDir.INOUT) {
			fFormal.generateCodeRef(false, !aIgnoreDir, aIc);
			fActual.generateCode(true, aIc);
			aIc.add(new IGPopStmt(false, false, false, aSrc, getZDB()));
		}
	}

	/*
	 * next two methods generate code which for subprogram entry and exit
	 */

	public void generateEntryCode(IGInterpreterCode aIc, SourceLocation aSrc) throws ZamiaException {

		OIDir dir = fFormal.getDirection();

		if (dir == OIDir.IN || dir == OIDir.INOUT) {
			IGObject obj = fFormal.generateCodeRef(false, true, aIc);

			fActual.generateCode(true, aIc);
			
			// take special care of signal parameters:
			if (obj != null && obj.getCat() == IGObjectCat.SIGNAL) {
				
				aIc.add(new IGMapStmt(aSrc, getZDB()));
			} else {
				aIc.add(new IGPopStmt(false, false, false, aSrc, getZDB()));
			}
			
		}
	}

	public void generateExitCode(IGInterpreterCode aCode, SourceLocation aSrc) throws ZamiaException {

		OIDir dir = fFormal.getDirection();

		if (dir == OIDir.OUT || dir == OIDir.INOUT) {
			fActual.generateCodeRef(true, true, aCode);
			fFormal.generateCode(false, aCode);
			aCode.add(new IGPopStmt(false, false, false, aSrc, getZDB()));
		}
	}

	@Override
	public String toString() {
		return "IGMapping(formal=" + fFormal + ", actual=" + fActual + ")";
	}

	@Override
	public IGItem getChild(int aIdx) {
		return aIdx == 0 ? fActual : fFormal;
	}

	@Override
	public int getNumChildren() {
		return 2;
	}

	public Collection<IGInterpreterCode> generateSynchroCodes(String aLabel, SourceLocation aSrc) throws ZamiaException {
		Collection<IGInterpreterCode> processCodes = new ArrayList<IGInterpreterCode>();
		IGSequenceOfStatements seq;
		IGInterpreterCode code;

		OIDir dir = fFormal.getDirection();

		if (dir == OIDir.IN || dir == OIDir.INOUT) {

			// create artificial process
			seq = new IGSequenceOfStatements(null, computeSourceLocation(), getZDB());
			seq.add(new IGSequentialAssignment(fActual, fFormal, false, null, null, computeSourceLocation(), getZDB()));

			IGProcess igProcess = new IGProcess(false, 0, null, computeSourceLocation(), getZDB());
			igProcess.setStatementSequence(seq);
			igProcess.appendFinalWait(null);

			// generate code
			seq = igProcess.getSequenceOfStatements();
			int nStmts = seq.getNumStatements();
			code = new IGInterpreterCode(aLabel + SYNCHRO_IN_ID, aSrc);
			// for signal assignment generate code manually, because IGSequentialAssignment
			// will generate code that will not allow to write into IN port.
			fFormal.generateCodeRef(false, true, code);
			fActual.generateCode(false, code);
			code.add(new IGPopStmt(false, false, false, computeSourceLocation(), getZDB()));
			// now process automatically
			for (int i = 1; i < nStmts; i++) {
				IGSequentialStatement statement = seq.getStatement(i);
				statement.generateCode(code);
			}

			processCodes.add(code);
		}

		if (dir == OIDir.OUT || dir == OIDir.INOUT) {

			// create artificial process
			seq = new IGSequenceOfStatements(null, computeSourceLocation(), getZDB());
			seq.add(new IGSequentialAssignment(fFormal, fActual, false, null, null, computeSourceLocation(), getZDB()));

			IGProcess igProcess = new IGProcess(false, 0, null, computeSourceLocation(), getZDB());
			igProcess.setStatementSequence(seq);
			igProcess.appendFinalWait(null);

			// generate code
			seq = igProcess.getSequenceOfStatements();
			code = new IGInterpreterCode(aLabel + SYNCHRO_OUT_ID, aSrc);
			seq.generateCode(code);

			processCodes.add(code);
		}

		return processCodes;
	}

}
