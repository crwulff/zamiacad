package org.zamia.instgraph.sim.ref;

import java.util.Collection;

import org.zamia.ErrorReport;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.util.PathName;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

public class IGSimPostponedProcess extends IGSimProcess {

	public IGSimPostponedProcess(IGSimRef aSim, PathName aPath, PathName aParentPath, ZamiaProject aZPrj) {
		super(aSim, aPath, aParentPath, aZPrj);
	}
	
	@Override
	public void resume(Collection<IGSimPostponedProcess> aPostponed, ASTErrorMode aErrorMode, ErrorReport aReport) 
			throws ZamiaException {
		aPostponed.add(this);
	}

}
