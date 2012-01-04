/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jan 2, 2008
 */
package org.zamia;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.SimpleRegexp;
import org.zamia.vhdl.ast.DMUID;
import org.zamia.vhdl.ast.DMUID.LUType;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class BuildPath implements Serializable {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public final static FSCache fsCache = FSCache.getInstance();

	private static final int NUM_THREADS = 32;

	static class BPVar implements Serializable {
		public BPVar(String aVarId, String aValue) {
			fId = aVarId;
			fValue = aValue;
		}

		String fId;

		String fValue;
	}

	private HashMapArray<String, BPVar> fVars;

	private HashSetArray<String> fIgnorePatterns;

	private HashSetArray<String> fIncludes;

	private ArrayList<BuildPathEntry> fEntries;

	private BuildPathEntry fDefaultEntry; // basically lib WORK, max priority

	private ArrayList<Toplevel> fToplevels, fSynthTLs;

	private ArrayList<String> fScripts;

	public enum Symbol {
		NOSYMBOL, IDENTIFIER, STRING, EOF, EXTERN, LOCAL, EQUALS, TOPLEVEL, PERIOD, LPAREN, RPAREN, READONLY, IGNORE, INCLUDE, OPTION, TRUE, FALSE, TOPDOWN, BOTTOMUP, LIST, DEFAULT, NONE, EXEC, RECURSIVE, NONRECURSIVE, SYNTHESIZE
	};

	private transient Reader fSrc;

	private Symbol fSym;

	private StringBuilder fBuf;

	private int fLine, fCol;

	private HashMap<String, Symbol> fKeyWords;

	private char fCh;

	private int fICh;

	private SourceFile fSF;

	private int fPriority;

	private boolean fHALMode;

	private boolean fUseFSCache;

	private transient ZamiaProject fZPrj;

	public BuildPath(SourceFile sourceFile) {
		setSrc(sourceFile);
		fKeyWords = new HashMap<String, Symbol>();

		fKeyWords.put("extern", Symbol.EXTERN);
		fKeyWords.put("local", Symbol.LOCAL);
		fKeyWords.put("toplevel", Symbol.TOPLEVEL);
		fKeyWords.put("readonly", Symbol.READONLY);
		fKeyWords.put("ignore", Symbol.IGNORE);
		fKeyWords.put("include", Symbol.INCLUDE);
		fKeyWords.put("option", Symbol.OPTION);
		fKeyWords.put("true", Symbol.TRUE);
		fKeyWords.put("false", Symbol.FALSE);
		fKeyWords.put("topdown", Symbol.TOPDOWN);
		fKeyWords.put("bottomup", Symbol.BOTTOMUP);
		fKeyWords.put("list", Symbol.LIST);
		fKeyWords.put("none", Symbol.NONE);
		fKeyWords.put("default", Symbol.DEFAULT);
		fKeyWords.put("exec", Symbol.EXEC);
		fKeyWords.put("recursive", Symbol.RECURSIVE);
		fKeyWords.put("nonrecursive", Symbol.NONRECURSIVE);
		fKeyWords.put("synthesize", Symbol.SYNTHESIZE);

		fBuf = new StringBuilder();

		fDefaultEntry = new BuildPathEntry(null, "WORK", false, Integer.MAX_VALUE, false, true, true, true, null);

		clean();
	}

	public void clean() {
		fEntries = new ArrayList<BuildPathEntry>();
		fVars = new HashMapArray<String, BPVar>();
		fToplevels = new ArrayList<Toplevel>();
		fSynthTLs = new ArrayList<Toplevel>();
		fIgnorePatterns = new HashSetArray<String>();
		fIncludes = new HashSetArray<String>();
		fScripts = new ArrayList<String>();
	}

	// "include" fileName

	private void includeDeclaration() throws ZamiaException, IOException {

		SourceLocation loc = getLocation();

		nextSym();

		if (fSym != Symbol.STRING)
			throw new ZamiaException("fileName expected.", getLocation());
		String fileName = evalString(fBuf.toString());
		nextSym();

		File f = new File(fileName);

		if (!f.isAbsolute()) {

			File f2 = fSF.getFile();
			if (f2 != null) {
				fileName = f2.getParent() + File.separator + fileName;
				f = new File(fileName);
			}
		}

		if (!f.exists()) {
			throw new ZamiaException("Include file '" + f.getAbsolutePath() + "' does not exist.", loc);
		}

		fIncludes.add(f.getAbsolutePath());

		BuildPath bp2 = new BuildPath(new SourceFile(f));
		bp2.parse(fVars, fUseFSCache, fZPrj);

		int n = bp2.getNumEntries();
		for (int i = 0; i < n; i++) {
			BuildPathEntry entry = bp2.getEntry(i);
			fEntries.add(entry);
		}

		n = bp2.getNumToplevels();
		for (int i = 0; i < n; i++) {
			Toplevel toplevel = bp2.getToplevel(i);
			fToplevels.add(toplevel);
		}

		n = bp2.getNumVars();
		for (int i = 0; i < n; i++) {
			BPVar var = bp2.getVar(i);
			fVars.put(var.fId, var);
		}

		n = bp2.getNumIgnorePatterns();
		for (int i = 0; i < n; i++) {
			String pattern = bp2.getIgnorePattern(i);
			addIgnorePattern(pattern);
		}

		n = bp2.getNumIncludes();
		for (int i = 0; i < n; i++) {
			String include = bp2.getInclude(i);
			addInclude(include);
		}

		n = bp2.getNumScripts();
		for (int i = 0; i < n; i++) {
			String script = bp2.getScript(i);
			addScript(script);
		}
	}

	// "option" string value

	private void optionDeclaration() throws ZamiaException, IOException {

		nextSym();

		if (fSym != Symbol.STRING)
			throw new ZamiaException("string expected.", getLocation());
		String optionName = fBuf.toString();
		nextSym();

		boolean value = false;

		switch (fSym) {
		case TRUE:
			value = true;
			break;
		case FALSE:
			value = false;
			break;
		default:
			throw new ZamiaException("Value expected (e.g. true/false)", getLocation());
		}
		nextSym();

		if (optionName.equals("HAL")) {

			fHALMode = value;

		} else {
			throw new ZamiaException("Unknown option " + optionName, getLocation());
		}
	}

	// "default" ( string | "none")

	private void defaultDeclaration() throws ZamiaException, IOException {

		nextSym();

		if (fSym == Symbol.STRING) {

			fDefaultEntry.fLibId = fBuf.toString();
			nextSym();

		} else if (fSym == Symbol.NONE) {

			fDefaultEntry.fLibId = null;
			nextSym();

		} else {
			throw new ZamiaException("Library id or \"none\" expected.", getLocation());
		}
	}

	// "ignore" pattern

	private void ignoreDeclaration() throws ZamiaException, IOException {

		nextSym();

		if (fSym != Symbol.STRING)
			throw new ZamiaException("pattern expected.", getLocation());
		String pattern = SimpleRegexp.convert(fBuf.toString());
		nextSym();

		addIgnorePattern(pattern);
	}

	// "exec" script

	private void execDeclaration() throws ZamiaException, IOException {

		nextSym();

		if (fSym != Symbol.STRING)
			throw new ZamiaException("filename/uri expected.", getLocation());
		String script = fBuf.toString();
		nextSym();

		addScript(script);
	}

	// "extern" ["readonly"] [ "topdown" | "bottomup" ] [ "recursive" | "nonrecursive" ] ["list"] ( libId | "none" ) pathPrefix

	private void externDeclaration() throws ZamiaException, IOException {

		SourceLocation location = getLocation();

		nextSym();

		boolean readonly = false;
		boolean topdown = false;
		boolean explicitMode = false;
		boolean list = false;
		boolean recursive = true;

		while (fSym == Symbol.READONLY || fSym == Symbol.TOPDOWN || fSym == Symbol.BOTTOMUP || fSym == Symbol.LIST || fSym == Symbol.NONRECURSIVE || fSym == Symbol.RECURSIVE) {

			switch (fSym) {
			case READONLY:
				readonly = true;
				break;
			case TOPDOWN:
				if (explicitMode) {
					throw new ZamiaException("Cannot specify topdown/bottomup multiple times", getLocation());
				}
				topdown = true;
				explicitMode = true;
				break;
			case BOTTOMUP:
				if (explicitMode) {
					throw new ZamiaException("Cannot specify topdown/bottomup multiple times", getLocation());
				}
				topdown = false;
				explicitMode = true;
				break;
			case LIST:
				list = true;
				break;
			case RECURSIVE:
				recursive = true;
				break;
			case NONRECURSIVE:
				recursive = false;
				break;
			}

			nextSym();
		}

		String libId = null;
		if (fSym == Symbol.IDENTIFIER) {
			libId = fBuf.toString();
		} else if (fSym == Symbol.NOSYMBOL) {
			libId = null;
		} else {
			throw new ZamiaException("Library id / 'none' expected.", getLocation());
		}
		nextSym();

		if (!explicitMode && fHALMode) {
			topdown = libId != null && libId.equals("WORK");
		}

		if (fSym != Symbol.STRING)
			throw new ZamiaException("pathPrefix expected.", getLocation());
		String pathPrefix = evalString(fBuf.toString());
		nextSym();

		if (!list) {

			logger.debug("extern declaration: " + pathPrefix + "** goes into library " + libId);

			fEntries.add(new BuildPathEntry(pathPrefix, libId, true, fPriority--, readonly, !topdown, true, recursive, location));

		} else {
			readList(pathPrefix, libId, true, readonly, topdown, location);
		}
	}

	private void readList(String aPathPrefix, String aLibId, boolean aExtern, boolean aReadonly, boolean aTopdown, SourceLocation aLocation) {

		if (aExtern) {
			logger.debug("extern declaration: list of files " + aPathPrefix);
		} else {
			logger.debug("local declaration: list of files " + aPathPrefix);
		}

		BufferedReader listReader = null;
		try {
			listReader = new BufferedReader(new InputStreamReader(fsCache.openFileStream(aPathPrefix, aReadonly)));

			String line;
			while ((line = listReader.readLine()) != null) {
				String path = evalString(line);

				if (aExtern) {
					logger.debug("extern declaration: file " + path + " goes into library " + aLibId);
				} else {
					logger.debug("local declaration: file " + path + " goes into library " + aLibId);
				}

				fEntries.add(new BuildPathEntry(path, aLibId, aExtern, fPriority--, aReadonly, !aTopdown, false, false, aLocation));
			}
		} catch (IOException e) {
			el.logException(e);
		} finally {
			if (listReader != null) {
				try {
					listReader.close();
				} catch (IOException e) {
					el.logException(e);
				}
			}
		}
	}

	// "toplevel" [libId "."] entityId [ "(" archId ")" ]

	private void toplevelDeclaration() throws ZamiaException, IOException {

		int line = this.fLine;
		int col = this.fCol;

		String libId = "WORK";

		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			throw new ZamiaException("Library/Entity id expected.", getLocation());
		String entityId = fBuf.toString();
		nextSym();

		if (fSym == Symbol.PERIOD) {
			nextSym();
			libId = entityId;

			if (fSym != Symbol.IDENTIFIER)
				throw new ZamiaException("Entity id expected.", getLocation());

			entityId = fBuf.toString();
			nextSym();
		}

		String archId = null;

		if (fSym == Symbol.LPAREN) {
			nextSym();
			if (fSym != Symbol.IDENTIFIER)
				throw new ZamiaException("Architecture id expected.", getLocation());
			archId = fBuf.toString();
			nextSym();

			if (fSym != Symbol.RPAREN)
				throw new ZamiaException(") id expected.", getLocation());

			nextSym();
		}

		logger.debug("toplevel declaration: " + entityId + "(" + archId + ") from library " + libId);

		LUType type = archId != null ? LUType.Architecture : LUType.Entity;

		DMUID duuid = new DMUID(type, libId, entityId, archId);

		fToplevels.add(new Toplevel(duuid, new SourceLocation(fSF, line, col)));

	}

	// "synthesize" [libId "."] entityId [ "(" archId ")" ]

	private void synthesizeDeclaration() throws ZamiaException, IOException {

		int line = this.fLine;
		int col = this.fCol;

		String libId = "WORK";

		nextSym();
		if (fSym != Symbol.IDENTIFIER)
			throw new ZamiaException("Library/Entity id expected.", getLocation());
		String entityId = fBuf.toString();
		nextSym();

		if (fSym == Symbol.PERIOD) {
			nextSym();
			libId = entityId;

			if (fSym != Symbol.IDENTIFIER)
				throw new ZamiaException("Entity id expected.", getLocation());

			entityId = fBuf.toString();
			nextSym();
		}

		String archId = null;

		if (fSym == Symbol.LPAREN) {
			nextSym();
			if (fSym != Symbol.IDENTIFIER)
				throw new ZamiaException("Architecture id expected.", getLocation());
			archId = fBuf.toString();
			nextSym();

			if (fSym != Symbol.RPAREN)
				throw new ZamiaException(") id expected.", getLocation());

			nextSym();
		}

		logger.debug("synthesize declaration: " + entityId + "(" + archId + ") from library " + libId);

		LUType type = archId != null ? LUType.Architecture : LUType.Entity;

		DMUID duuid = new DMUID(type, libId, entityId, archId);

		fSynthTLs.add(new Toplevel(duuid, new SourceLocation(fSF, line, col)));

	}

	// "local" [ "topdown" | "bottomup" ] [ "list"] (libId | "none") pathPrefix

	private void localDeclaration() throws ZamiaException, IOException {
		SourceLocation location = getLocation();

		nextSym();

		boolean topdown = false;
		boolean explicitMode = false;
		boolean list = false;

		while (fSym == Symbol.TOPDOWN || fSym == Symbol.BOTTOMUP || fSym == Symbol.LIST) {

			switch (fSym) {
			case TOPDOWN:
				if (explicitMode) {
					throw new ZamiaException("Cannot specify topdown/bottomup multiple times", getLocation());
				}
				topdown = true;
				explicitMode = true;
				break;
			case BOTTOMUP:
				if (explicitMode) {
					throw new ZamiaException("Cannot specify topdown/bottomup multiple times", getLocation());
				}
				topdown = false;
				explicitMode = true;
				break;
			case LIST:
				list = true;
				break;
			}

			nextSym();
		}

		String libId = null;

		if (fSym == Symbol.IDENTIFIER) {
			libId = fBuf.toString();
		} else if (fSym == Symbol.NONE) {
			libId = null;
		} else {
			throw new ZamiaException("Library id / none expected.", getLocation());
		}
		nextSym();

		if (!explicitMode && fHALMode) {
			topdown = libId != null && libId.equals("WORK");
		}

		if (fSym != Symbol.STRING)
			throw new ZamiaException("pathPrefix expected.", getLocation());
		String pathPrefix = evalString(fBuf.toString()).replace('/', File.separatorChar).replace('\\', File.separatorChar);
		nextSym();

		//FIXME: check the case when resource is a file rather than dir. 
		//It should not end with separator then. Get rid of new File() in the findEntry().
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix = pathPrefix + File.separator;
		}

		if (!list) {

			logger.debug("local declaration: " + pathPrefix + "** goes into library " + libId);

			fEntries.add(new BuildPathEntry(pathPrefix, libId, false, Integer.MAX_VALUE, false, !topdown, true, true, location));

		} else {
			String absPath = fZPrj.fBasePath + File.separator + pathPrefix;
			readList(absPath, libId, false, false, topdown, location);
		}
	}

	public String getVar(String aVarId) {
		BPVar res = fVars.get(aVarId);

		if (res == null) {
			return System.getenv(aVarId);
		}
		return res.fValue;
	}

	public String evalString(String aStr) {

		StringBuffer res = new StringBuffer();

		int i = 0;
		boolean inVar = false;
		StringBuffer varId = new StringBuffer();
		while (i < aStr.length()) {

			char c = aStr.charAt(i);

			if (c == '$') {
				if (inVar) {

					String v = getVar(varId.toString());
					if (v != null)
						res.append(v);

				}

				varId = new StringBuffer();
				inVar = true;
			} else {

				if (inVar) {
					if (Character.isJavaIdentifierPart(c)) {
						varId.append(c);
					} else {
						inVar = false;
						String v = getVar(varId.toString());
						if (v != null)
							res.append(v);
						res.append(c);
					}
				} else {
					res.append(c);
				}
			}
			i++;
		}

		if (inVar) {
			String v = getVar(varId.toString());
			if (v != null)
				res.append(v);
		}

		return res.toString();
	}

	private void varAssignment() throws IOException, ZamiaException {
		String varId = fBuf.toString();
		nextSym();
		if (fSym != Symbol.EQUALS)
			throw new ZamiaException("= expected.", getLocation());
		nextSym();
		if (fSym != Symbol.STRING)
			throw new ZamiaException("String expected.", getLocation());
		String value = evalString(fBuf.toString());
		nextSym();

		logger.debug("variable assignment: " + varId + " := " + value);
		fVars.put(varId, new BPVar(varId, value));
	}

	public void parse(HashMapArray<String, BPVar> aVariables, boolean aUseFSCache, ZamiaProject aZPrj) throws IOException, ZamiaException {

		if (fSF == null || !fsCache.exists(fSF, false))
			return;

		logger.debug("BuildPath, SF exists. ");

		fSrc = fsCache.openFile(fSF, false);
		fLine = 1;
		fCol = 1;
		fPriority = Integer.MAX_VALUE - 1;
		fUseFSCache = aUseFSCache;
		fZPrj = aZPrj;

		clean();

		logger.debug("BuildPath, clean done. ");

		if (aVariables != null) {
			int n = aVariables.size();
			for (int i = 0; i < n; i++) {
				BPVar var = aVariables.get(i);
				fVars.put(var.fId, var);
			}
		}

		getCh();
		nextSym();

		// parser goes here
		while (fSym != Symbol.EOF) {

			// logger.debug ("BuildPath, got symbol: "+sym);

			switch (fSym) {
			case EXTERN:
				externDeclaration();
				break;
			case LOCAL:
				localDeclaration();
				break;
			case IDENTIFIER:
				varAssignment();
				break;
			case TOPLEVEL:
				toplevelDeclaration();
				break;
			case INCLUDE:
				includeDeclaration();
				break;
			case IGNORE:
				ignoreDeclaration();
				break;
			case OPTION:
				optionDeclaration();
				break;
			case DEFAULT:
				defaultDeclaration();
				break;
			case EXEC:
				execDeclaration();
				break;
			case SYNTHESIZE:
				synthesizeDeclaration();
				break;
			default:
				throw new ZamiaException("Syntax error.", getLocation());
			}
		}

		logger.debug("BuildPath, parsing done, closing. ");
		fSrc.close();

		computeCanonicalPaths();

		checkIsValid();
	}

	class MakeCanonicalJob implements Runnable {

		private BuildPathEntry fEntry;

		public MakeCanonicalJob(BuildPathEntry aEntry) {
			fEntry = aEntry;
		}

		public void run() {

			logger.info("BuildPath: making path canonical: %s...", fEntry.fPrefix);

			File fext = new File(fEntry.fPrefix);

			String absPath = fext.getAbsolutePath();

			if (!fsCache.exists(absPath, fUseFSCache)) {
				if (fZPrj != null) {
					ERManager erm = fZPrj.getERM();
					if (erm != null) {
						erm.addError(new ZamiaException("File or directory doesn't exist: " + fEntry.fPrefix, fEntry.fLocation));
					} else {
						logger.error("File or directory doesn't exist: %s", fEntry.fPrefix);
					}
				} else {
					logger.error("File or directory doesn't exist: %s", fEntry.fPrefix);
				}
			}

			String canonicalPrefix = fsCache.getCanonicalPath(absPath, fUseFSCache);

			if (fEntry.fIsDirectory) {
				// make sure path ends with a '/' so prefix detection will work
				if (!canonicalPrefix.endsWith(File.separator)) {
					canonicalPrefix = canonicalPrefix + File.separator;
				}
			}

			fEntry.fPrefix = canonicalPrefix;

			logger.debug("BuildPath: canonical prefix: %s...", fEntry.fPrefix);
		}
	}

	private void computeCanonicalPaths() {

		logger.info("BuildPath: Computing canonical paths...");

		int n = getNumEntries();

		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

		for (int i = 0; i < n; i++) {

			BuildPathEntry entry = getEntry(i);

			if (!entry.fExtern) {
				continue;
			}

			pool.execute(new MakeCanonicalJob(entry));
		}

		pool.shutdown();
		try {
			pool.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			el.logException(e);
		}
	}

	private void checkIsValid() throws ZamiaException {

		// no extern entry is allowed to be a prefix of another one

		int n = getNumEntries();
		for (int i = 0; i < n; i++) {

			BuildPathEntry entry = getEntry(i);

			if (!entry.fIsDirectory) {
				continue;
			}

			for (int j = i + 1; j < n; j++) {

				BuildPathEntry entry2 = getEntry(j);
				if (!entry2.fIsDirectory) {
					continue;
				}

				if (entry.fPrefix.startsWith(entry2.fPrefix)) {
					throw new ZamiaException("Common prefix among build path entries detected: " + entry2.fPrefix + " is a prefix of " + entry.fPrefix, entry2.fLocation);
				}
				if (entry2.fPrefix.startsWith(entry.fPrefix)) {
					throw new ZamiaException("Common prefix among build path entries detected: " + entry.fPrefix + " is a prefix of " + entry2.fPrefix, entry.fLocation);
				}
			}
		}

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
	}

	private Symbol nextSym() throws IOException, ZamiaException {

		fSym = Symbol.NOSYMBOL;

		while (fSym == Symbol.NOSYMBOL) {

			while ((fICh != -1) && Character.isWhitespace(fCh))
				getCh();

			if (fICh == -1) {
				fSym = Symbol.EOF;
				return fSym;
			}

			switch (fCh) {
			case '=':
				fSym = Symbol.EQUALS;
				getCh();
				break;
			case '(':
				fSym = Symbol.LPAREN;
				getCh();
				break;
			case '.':
				fSym = Symbol.PERIOD;
				getCh();
				break;
			case ')':
				fSym = Symbol.RPAREN;
				getCh();
				break;
			case '#':
				comment();
				break;
			case '"':
				getCh();
				quotedString();
				fSym = Symbol.STRING;
				break;
			default:
				if (Character.isJavaIdentifierPart(fCh)) {
					identifier();
				} else {
					logger.error("BuildPath: unknown character '%c' skipped.", fCh);
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

		fBuf.setLength(0);
		while (fICh != -1 && fCh != '"') {
			fBuf.append(fCh);
			getCh();
		}

		if (fICh == -1)
			throw new ZamiaException("\" expected.", getLocation());

		if (fCh == '"') {
			getCh();
		}
	}

	private void identifier() throws IOException, ZamiaException {

		fBuf.setLength(0);
		while (fICh != -1 && Character.isJavaIdentifierPart(fCh)) {
			fBuf.append(fCh);
			getCh();
		}

		fSym = Symbol.IDENTIFIER;
		String ids = fBuf.toString();
		if (fKeyWords.containsKey(ids)) {
			fSym = fKeyWords.get(ids);
		}
	}

	private SourceLocation getLocation() {
		return new SourceLocation(fSF, fLine, fCol);
	}

	public int getNumEntries() {
		return fEntries.size();
	}

	public BuildPathEntry getEntry(int aIdx) {
		return fEntries.get(aIdx);
	}

	public BuildPathEntry findEntry(SourceFile aSF) {

		String path = aSF.getLocalPath();
		if (path != null) {
			// local
			int n = getNumEntries();
			for (int i = 0; i < n; i++) {
				BuildPathEntry entry = getEntry(i);
				if (!entry.fExtern) {
					String p1 =new File(path).getPath();
					String p2 = new File(entry.fPrefix).getPath();
					if (p1.startsWith(p2))
						return entry;
				}
			}
		} else {
			// FIXME: canonical path?
			path = aSF.getAbsolutePath();

			int n = getNumEntries();
			for (int i = 0; i < n; i++) {
				BuildPathEntry entry = getEntry(i);
				if (entry.fExtern) {
					if (path.startsWith(entry.fPrefix))
						return entry;
				}
			}
		}

		return fDefaultEntry;
	}

	public int getNumToplevels() {
		return fToplevels.size();
	}

	public Toplevel getToplevel(int aIdx) {
		return fToplevels.get(aIdx);
	}

	public int getNumSynthTLs() {
		return fSynthTLs.size();
	}

	public Toplevel getSynthTL(int aIdx) {
		return fSynthTLs.get(aIdx);
	}

	public void setSrc(SourceFile aSF) {
		fSF = aSF;
	}

	public SourceFile getSourceFile() {
		return fSF;
	}

	private int getNumVars() {
		return fVars.size();
	}

	private BPVar getVar(int aIdx) {
		return fVars.get(aIdx);
	}

	public int getNumIgnorePatterns() {
		return fIgnorePatterns.size();
	}

	public String getIgnorePattern(int aIdx) {
		return fIgnorePatterns.get(aIdx);
	}

	private void addIgnorePattern(String aPattern) {
		fIgnorePatterns.add(aPattern);
	}

	public int getNumIncludes() {
		return fIncludes.size();
	}

	public String getInclude(int aIdx) {
		return fIncludes.get(aIdx);
	}

	public void addInclude(String aIncludePath) {
		fIncludes.add(aIncludePath);
	}

	public int getNumScripts() {
		return fScripts.size();
	}

	public String getScript(int aIdx) {
		return fScripts.get(aIdx);
	}

	private void addScript(String aScript) {
		fScripts.add(aScript);
	}

	/**
	 * does this buildpath differ in anything besides toplevels from the given one?
	 */

	public boolean sigDiff(BuildPath aOldBP) {

		int n = aOldBP.getNumEntries();
		if (getNumEntries() != n) {
			return true;
		}

		for (int i = 0; i < n; i++) {

			BuildPathEntry oldEntry = aOldBP.getEntry(i);
			BuildPathEntry entry = getEntry(i);

			if (!oldEntry.equals(entry)) {
				return true;
			}

		}

		n = aOldBP.getNumIgnorePatterns();
		if (getNumIgnorePatterns() != n) {
			return true;
		}
		for (int i = 0; i < n; i++) {
			if (!aOldBP.getIgnorePattern(i).equals(getIgnorePattern(i))) {
				return true;
			}
		}

		n = aOldBP.getNumScripts();
		if (getNumScripts() != n) {
			return true;
		}
		for (int i = 0; i < n; i++) {
			if (!aOldBP.getScript(i).equals(getScript(i))) {
				return true;
			}
		}

		return false;
	}

}
