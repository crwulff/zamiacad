/* 
 * Copyright 2005-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Dec 30, 2005
 */
package org.zamia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.zamia.instgraph.IGManager;
import org.zamia.util.ZHash;
import org.zamia.util.ZamiaTmpDir;
import org.zamia.vhdl.VHDLIndexer;
import org.zamia.vhdl.vhdl2002.VHDL2002Parser;
import org.zamia.vhdl.vhdl2008.VHDL2008Parser;
import org.zamia.zdb.ZDB;
import org.zamia.zdb.ZDBException;

/**
 * The class ZamiaProject is the central point holding together all zamia
 * classes, objects and structures related to a project.
 * 
 * It takes care of IG and (todo) RTLGraph generation and persistence. Also
 * anchors the central compiler invocation and DU storage facility DUManager and
 * the build path.
 * 
 * @author Guenter Bartsch
 */

public class ZamiaProject {

	protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected final static ExceptionLogger el = ExceptionLogger.getInstance();

	//	private static final String TCL_CLEAN_CMD = "zamiaBuildClean";

	public enum VHDLLanguageSupport {
		VHDL2002, VHDL2008
	};

	private String fId;

	private String fBasePath;

	private String fDataPath;

	private ZDB fZDB;

	private BuildPath fBuildPath;

	private VHDLLanguageSupport fVHDLLanguageSupport;

	private IHDLParser fVHDLParser;

	private VHDLIndexer fVHDLIndexer;

	private ZamiaProjectBuilder fBuilder;

	private DUManager fDUM;

	private IGManager fIGM;

	// error manager
	private ERManager fERM;

	//private ZamiaTclInterpreter fZTI;

	private static final String BUILDPATH_OBJ_NAME = "ZPRJ_BuildPath";

	public ZamiaProject(String aId, String aBasePath, SourceFile aBuildPath, String aDataPath, VHDLLanguageSupport aVHDLLanguageSupport) throws IOException, ZamiaException,
			ZDBException {
		fId = aId;
		fBasePath = aBasePath;
		fDataPath = aDataPath != null ? aDataPath : ZamiaTmpDir.getTmpDir().getAbsolutePath();
		fVHDLLanguageSupport = aVHDLLanguageSupport;

		registerProject(this);

		logger.debug("ZamiaProject: project %s: Using data directory: %s", fId, fDataPath);

		File dbDir = new File(fDataPath + File.separator + "db" + File.separator + ZHash.encodeZ(fId));

		logger.debug("ZamiaProject: project %s: Using db directory: %s", fId, dbDir.getAbsolutePath());

		fZDB = new ZDB(dbDir, this);

		fERM = new ERManager(this);

		switch (fVHDLLanguageSupport) {
		case VHDL2002:
			fVHDLParser = new VHDL2002Parser();
			break;
		default:
			fVHDLParser = new VHDL2008Parser();
		}

		fVHDLIndexer = new VHDLIndexer();

		fBuildPath = (BuildPath) fZDB.getNamedObject(BUILDPATH_OBJ_NAME);
		if (fBuildPath == null) {
			BuildPath bp = new BuildPath();
			bp.setSrc(aBuildPath);
			setBuildPath(bp);
		} else {
			SourceFile sf1 = fBuildPath.getSourceFile();
			if (sf1 == null && aBuildPath != null || sf1 != null && aBuildPath == null || sf1 != null && !sf1.equals(aBuildPath)) {
				BuildPath bp = new BuildPath();
				bp.setSrc(aBuildPath);
				setBuildPath(bp);
			}
		}

		fDUM = new DUManager(this);

		fBuilder = new ZamiaProjectBuilder(this);

		fIGM = new IGManager(this);

		initTclInterpreter();
	}

	public void initTclInterpreter() {
		//		try {
		//			if (fZTI != null) {
		//				fZTI.dispose();
		//			}
		//			fZTI = new ZamiaTclInterpreter(this);
		//
		//			int n = fBuildPath.getNumScripts();
		//			for (int i = 0; i < n; i++) {
		//				String script = fBuildPath.getScript(i);
		//				fZTI.evalFile(script);
		//			}
		//
		//		} catch (TclException e) {
		//			el.logException(e);
		//		}
	}

	public ZamiaProject(String aId, String aBasePath, SourceFile aBuildPath) throws IOException, ZamiaException, ZDBException {
		this(aId, aBasePath, aBuildPath, null, VHDLLanguageSupport.VHDL2008);
	}

	public ZamiaProject() throws IOException, ZamiaException, ZDBException {
		this("unnamed project", ".", null);
	}

	public ZDB getZDB() {
		return fZDB;
	}

	public void clean() throws IOException, ZamiaException {
		logger.info("Cleaning project '%s'", fBasePath);
		ZamiaProfiler.getInstance().startTimer("Cleaning");
		fZDB.clear();
		BuildPath bp = new BuildPath();
		bp.setSrc(fBuildPath.getSourceFile());
		setBuildPath(bp);
		fDUM.clean();
		fERM.clean();
		fBuilder.clean();

		//		if (fZTI.hasCommand(TCL_CLEAN_CMD)) {
		//			try {
		//				fZTI.eval(TCL_CLEAN_CMD);
		//			} catch (Throwable e) {
		//				el.logException(e);
		//			}
		//		}

		initTclInterpreter();

		ZamiaProfiler.getInstance().stopTimer("Cleaning");
	}

	public void zdbChanged() {
		BuildPath bp = new BuildPath();
		bp.setSrc(fBuildPath.getSourceFile());
		setBuildPath(bp);

		fDUM.zdbChanged();
		fERM.zdbChanged();
		fBuilder.zdbChanged();
	}

	public DUManager getDUM() {
		return fDUM;
	}

	public void shutdown() {
		fZDB.shutdown();
	}

	public ZamiaProjectBuilder getBuilder() {
		return fBuilder;
	}

	public String getBasePath() {
		return fBasePath;
	}

	public String getId() {
		return fId;
	}

	public IGManager getIGM() {
		return fIGM;
	}

	public ERManager getERM() {
		return fERM;
	}

	public VHDLIndexer getVHDLIndexer() {
		return fVHDLIndexer;
	}

	public IHDLParser getVHDLParser() {
		return fVHDLParser;
	}

	@Override
	public String toString() {
		return "ZamiaProject " + fId;
	}

	public void setBuildPath(BuildPath aBP) {
		fZDB.createNamedObject(BUILDPATH_OBJ_NAME, aBP);
		fBuildPath = aBP;
	}

	public BuildPath getBuildPath() {
		return fBuildPath;
	}

	public String getDataPath() {
		return fDataPath;
	}

	//	public ZamiaTclInterpreter getZTI() {
	//		return fZTI;
	//	}

	/*
	 * editor path storage
	 */

	public void storeEditorPath(String aFilename, String aPath) {

		long id = fZDB.store(aPath);

		fZDB.putIdx("EPIdx", aFilename, id);
	}

	public String lookupEditorPath(String aFilename) {

		long id = fZDB.getIdx("EPIdx", aFilename);
		if (id == 0)
			return null;

		return (String) fZDB.load(id);
	}

	/*
	 * 
	 * static project registry
	 * 
	 */

	private static HashMap<String, ZamiaProject> projectMap = new HashMap<String, ZamiaProject>();

	private static void registerProject(ZamiaProject aPrj) {
		projectMap.put(aPrj.getId(), aPrj);
	}

	public static ZamiaProject lookupProject(String aId) {
		return projectMap.get(aId);
	}

}
