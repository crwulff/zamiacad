/* 
 * Copyright 2008-2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 4, 2008
 */
package org.zamia.vhdl;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.zamia.DUManager;
import org.zamia.SourceFile;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.vhdl.ast.DUUID;
import org.zamia.vhdl.ast.DUUID.LUType;


/**
 * We use a simple (and fast!) lexer to figure out which library units are being
 * defined in a given VHDL source file
 * 
 * @author Guenter Bartsch
 * 
 */

public class VHDLIndexer {

	public enum Symbol {
		NONE, STRING, IDENTIFIER, EOF, ARCHITECTURE, OF, IS, PACKAGE, BODY, ENTITY, CONFIGURATION
	};

	private final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static boolean dump = false;

	private Reader fSrc;

	private Symbol fSym;

	private StringBuilder fIdBuilder;

	private String fId;

	private int fLine, fCol;

	private HashMap<String, Symbol> fKW;

	private char fCh;

	private int fICh;

	private SourceFile fSF;

	private String fTargetLib;

	private DUManager fDUM;

	private int fPriority;

	private int fNumChars;

	private String fPath;

	private boolean fBottomUp;

	private boolean fUseFSCache;

	public VHDLIndexer() {

		fKW = new HashMap<String, Symbol>();

		fKW.put("ARCHITECTURE", Symbol.ARCHITECTURE);
		fKW.put("OF", Symbol.OF);
		fKW.put("IS", Symbol.IS);
		fKW.put("PACKAGE", Symbol.PACKAGE);
		fKW.put("BODY", Symbol.BODY);
		fKW.put("ENTITY", Symbol.ENTITY);
		fKW.put("CONFIGURATION", Symbol.CONFIGURATION);

		fIdBuilder = new StringBuilder();

		clean();
	}

	public void clean() {
	}

	private boolean checkFileName(String entityId_) {
		String fileNameHAL = entityId_ + ".vhdl";
		String fileName = fSF.getFileName();

		if (!fileNameHAL.equalsIgnoreCase(fileName)) {
			logger.debug("VHDLIndexer: IGNORING '%s' because file name doesn't match and we are in top-down mode.", entityId_);
			return false;
		}
		return true;
	}

	private void architectureDeclaration() throws IOException, ZamiaException {
		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			return;
		String archId = fId;
		nextSym();
		if (fSym != Symbol.OF)
			return;
		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			return;
		String entityId = fId;
		nextSym();
		if (fSym != Symbol.IS)
			return;

		if (dump) {
			logger.debug("VHDLIndexer: Architecture %s.%s(%s) found in '%s'", fTargetLib, entityId, archId, fPath);
		}

		if (fBottomUp || checkFileName(entityId)) {
			DUUID duuid = new DUUID(LUType.Architecture, fTargetLib, entityId, archId);
			fDUM.addDesignUnitSource(duuid, fSF, fPriority, fUseFSCache);
		}
	}

	private void configurationDeclaration() throws IOException, ZamiaException {
		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			return;
		String cfgId = fId;
		nextSym();
		if (fSym != Symbol.OF)
			return;
		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			return;
		String entityId = fId;
		nextSym();
		if (fSym != Symbol.IS)
			return;

		if (dump)
			logger.debug("VHDLIndexer: Configuration %s.%s(%s) found in '%s'", fTargetLib, entityId, cfgId, fPath);

		DUUID duuid = new DUUID(LUType.Configuration, fTargetLib, entityId, null);

		fDUM.addDesignUnitSource(duuid, fSF, fPriority, fUseFSCache);
	}

	private void packageDeclaration() throws IOException, ZamiaException {
		nextSym();

		boolean isBody = false;

		if (fSym == Symbol.BODY) {
			nextSym();
			isBody = true;
		}
		if (fSym != Symbol.IDENTIFIER)
			return;
		String pkgId = fId;

		nextSym();
		if (fSym != Symbol.IS)
			return;

		if (isBody) {

			if (dump)
				logger.debug("VHDLIndexer: Package body %s.%s found in '%s'", fTargetLib, pkgId, fPath);

			if (fBottomUp || checkFileName(pkgId)) {
				DUUID duuid = new DUUID(LUType.PackageBody, fTargetLib, pkgId, null);
				fDUM.addDesignUnitSource(duuid, fSF, fPriority, fUseFSCache);
			}

		} else {

			if (dump)
				logger.debug("VHDLIndexer: Package %s.%s found in '%s'", fTargetLib, pkgId, fPath);
			
			if (fBottomUp || checkFileName(pkgId)) {
				DUUID duuid = new DUUID(LUType.Package, fTargetLib, pkgId, null);
				fDUM.addDesignUnitSource(duuid, fSF, fPriority, fUseFSCache);
			}
		}
	}

	private void entityDeclaration() throws IOException, ZamiaException {
		nextSym();

		if (fSym != Symbol.IDENTIFIER)
			return;
		String entityId = fId;
		nextSym();
		if (fSym != Symbol.IS)
			return;

		if (dump)
			logger.debug("VHDLIndexer: Entity found: %s.%s", fTargetLib, entityId);

		if (fBottomUp || checkFileName(entityId)) {
			DUUID duuid = new DUUID(LUType.Entity, fTargetLib, entityId, null);
			fDUM.addDesignUnitSource(duuid, fSF, fPriority, fUseFSCache);
		}
	}

	public void parse(Reader aReader, String aTargetLib, SourceFile aSF, int aPriority, boolean aUseFSCache, boolean aBottomUp, DUManager aDUM) throws IOException, ZamiaException {

		//if (dump)
			logger.debug("VHDLIndexer: indexing '%s', target lib : '%s'", aSF.getAbsolutePath(), aTargetLib);

		if (aReader == null) {
			logger.error("VHDLIndexer: reader == null!");
		}

		fSF = aSF;
		fTargetLib = aTargetLib;
		fDUM = aDUM;
		fSrc = aReader;
		fLine = 1;
		fCol = 1;
		fNumChars = 0;
		fPriority = aPriority;
		fUseFSCache = aUseFSCache;
		fBottomUp = aBottomUp;
		fPath = fSF.getAbsolutePath();

		fDUM.removeStubs(fSF);

		clean();

		getCh();
		nextSym();

		// parser goes here
		while (fSym != Symbol.EOF) {

			switch (fSym) {
			case ARCHITECTURE:
				architectureDeclaration();
				break;
			case PACKAGE:
				packageDeclaration();
				break;
			case ENTITY:
				entityDeclaration();
				break;
			case CONFIGURATION:
				configurationDeclaration();
				break;
			default:
				// ignore
				nextSym();
			}
		}

		aSF.setNumLines(fLine);
		aSF.setNumChars(fNumChars);
	}

	private void getCh() throws IOException {
		fICh = fSrc.read();
		fCol++;
		if (fICh != -1)
			fCh = (char) fICh;
		if (fCh == '\n') {
			fLine++;
			fCol = 1;
		}
		fNumChars++;
	}

	private Symbol nextSym() throws IOException, ZamiaException {

		fSym = Symbol.NONE;

		while (fSym == Symbol.NONE) {

			while ((fICh != -1) && Character.isWhitespace(fCh))
				getCh();

			if (fICh == -1) {
				fSym = Symbol.EOF;
				return fSym;
			}

			switch (fCh) {
			case '-':
				getCh();
				if (fCh == '-')
					comment();
				break;
			case '"':
				getCh();
				quotedString();
				fSym = Symbol.STRING;
				break;
			default:
				if (Character.isLetter(fCh)) {
					identifier();
				} else {
					// ignore
					getCh();
				}

			}
		}

		// print_sym();

		return fSym;
	}

	private void comment() throws IOException {
		while ((fICh != -1) && (fCh != '\n') && (fCh != '\r'))
			getCh();
	}

	private void quotedString() throws IOException, ZamiaException {

		fIdBuilder.setLength(0);
		while (fICh != -1 && fCh != '"') {
			fIdBuilder.append(fCh);
			getCh();
		}

		if (fCh == '"') {
			getCh();
		}
		fSym = Symbol.STRING;
	}

	private void identifier() throws IOException, ZamiaException {

		fIdBuilder.setLength(0);
		while (fICh != -1 && (Character.isLetter(fCh) || Character.isDigit(fCh) || fCh == '_')) {
			fIdBuilder.append(fCh);
			getCh();
		}

		fSym = Symbol.IDENTIFIER;
		fId = fIdBuilder.toString().toUpperCase();
		if (fKW.containsKey(fId)) {
			fSym = fKW.get(fId);
		}
	}

}
