/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Oct 29, 2010
 */
package org.zamia.verilog;

import org.zamia.DMManager;
import org.zamia.ExceptionLogger;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.util.HashSetArray;
import org.zamia.verilog.analysis.DepthFirstAdapter;
import org.zamia.verilog.node.AModuleDeclaration;
import org.zamia.verilog.node.PIdentifier;
import org.zamia.vhdl.ast.DMUID;

/**
 * 
 * @author Guenter Bartsch
 *
 */

class ModuleWrapperGenerator extends DepthFirstAdapter {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	private SourceFile fSF;

	private DMManager fDUM;

	private String fLibId;

	private boolean fUseFSCache;

	private int fPriority;

	private HashSetArray<DMUID> fDUs;

	public ModuleWrapperGenerator(HashSetArray<DMUID> aDUs, DMManager aDUM, SourceFile aSF, String aLibId, int aPriority, boolean aUseFSCache) {

		fSF = aSF;
		fDUM = aDUM;
		fDUs = aDUs;
		fLibId = aLibId;
		fPriority = aPriority;
		fUseFSCache = aUseFSCache;

	}

	@Override
	public void caseAModuleDeclaration(AModuleDeclaration aModuleDecl) {

		PIdentifier id = aModuleDecl.getIdentifier();

		System.out.printf("Found a module declaration: '%s'\n", id);

		ModuleWrapper wrapper = new ModuleWrapper(id.toString(), fLibId, aModuleDecl);

		try {
			fDUM.addDesignUnit(wrapper, fSF, fLibId, fPriority, fUseFSCache);
			fDUs.add(wrapper.getDMUID());
		} catch (ZamiaException e) {
			el.logException(e);
		}

	}

}
