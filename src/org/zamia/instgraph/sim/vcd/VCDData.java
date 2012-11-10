/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.instgraph.sim.vcd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zamia.SourceLocation;
import org.zamia.Toplevel;
import org.zamia.ZamiaException;
import org.zamia.ZamiaLogger;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGItem;
import org.zamia.instgraph.IGManager;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.util.SimpleRegexp;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;

/**
 * Holds all the information that was extracted from a VCD file in memory
 * 
 * @author Guenter Bartsch
 * 
 */

public class VCDData {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final boolean dump = false;

	private HashMapArray<String, SignalInfo> fIDCSignalMap;

	private HashMapArray<PathName, SignalInfo> fPathSignalMap;

	private ArrayList<PathName> fAllSignalPaths;

	private BigInteger fTimeScale;

	private BigInteger fStartTime, fEndTime;

	private ZamiaProject fZPrj;

	private IGManager fIGM;

	private Toplevel fToplevel;

	private PathName fPrefix;

	public VCDData(ZamiaProject aZPrj, Toplevel aToplevel, PathName aPrefix) {
		fZPrj = aZPrj;
		fIGM = fZPrj.getIGM();
		fToplevel = aToplevel;
		fIDCSignalMap = new HashMapArray<String, SignalInfo>();
		fPathSignalMap = new HashMapArray<PathName, SignalInfo>();
		fAllSignalPaths = new ArrayList<PathName>();
		fPrefix = aPrefix;
	}

	private PathName removePrefix(PathName aPath) {
		if (fPrefix == null) {
			return aPath;
		}

		String prefix = fPrefix.toString();
		int l = prefix.length();
		if (prefix != null && l == 0) {
			return aPath;
		}

		String strPath = aPath.toString();
		if (strPath.length() <= l) {
			return aPath;
		}

		return new PathName(strPath.substring(l));
	}

	public void newSignal(VCDReference aReference, String aIDC, SourceLocation aLocation) throws ZamiaException {

		PathName path = removePrefix(aReference.getPathName());

		IGItem item = fIGM.findItem(fToplevel, path);

		if (!(item instanceof IGObject)) {
			throw new ZamiaException("VCD: Error: Not a VHDL object: " + path, aLocation);
		}

		IGObject obj = (IGObject) item;

		IGType type = obj.getType();

		IGTypeStatic stype = type.computeStaticType(null, ASTErrorMode.EXCEPTION, null);

		SignalInfo si = new SignalInfo(path, stype);

		fIDCSignalMap.put(aIDC, si);

		fPathSignalMap.put(path, si);

		fAllSignalPaths.add(path);
	}

	public ArrayList<PathName> findSignalPaths(String aRegex, int aLimit) {

		ArrayList<PathName> res = new ArrayList<PathName>();

		String regex = SimpleRegexp.convert(aRegex);
		Pattern p = Pattern.compile(regex);

		int n = fAllSignalPaths.size();
		for (int i = 0; i < n; i++) {

			PathName path = fAllSignalPaths.get(i);

			Matcher m = p.matcher(path.toString());

			if (m.matches()) {
				res.add(path);
				if (res.size() > aLimit) {
					return res;
				}
			}
		}

		return res;
	}

	public BigInteger getTimeScale() {
		return fTimeScale;
	}

	public void setTimeScale(BigInteger aTimeScale) {
		fTimeScale = aTimeScale;
	}

	public SignalInfo getSignalInfo(PathName aPath) {
		return fPathSignalMap.get(aPath);
	}

	public SignalInfo getSignalInfo(String aIDC) {
		return fIDCSignalMap.get(aIDC);
	}

	private IGStaticValue getBit(IGTypeStatic aType, char aV, SourceLocation aLocation) throws ZamiaException {
		IGStaticValue sv = aType.findEnumLiteral(Character.toUpperCase(aV));
		if (sv == null) {
			throw new ZamiaException("VCD: Error, failed to find enum literal " + aV, aLocation);
		}
		return sv;
	}

	public void addBit(BigInteger aTime, String aIDC, char aV, SourceLocation aLocation) throws ZamiaException {
		SignalInfo si = fIDCSignalMap.get(aIDC);

		if (si == null) {
			throw new ZamiaException("VCD: Error, unknown symbol: " + aIDC, aLocation);
		}

		IGTypeStatic type = si.getType();
		if (!type.isEnum()) {
			throw new ZamiaException("VCD: Expected bit type, but type is " + type, aLocation);
		}

		if (dump) {
			logger.info("VCD: Bit value change %s@%d => '%s' (type: %s)", si.getPath(), aTime, aV, type);
		}

		IGStaticValue sv = getBit(type, aV, aLocation);
		if (sv == null) {
			return;
		}

		if (dump) {
			logger.info("VCD:     => sv=%s", sv);
		}
		si.add(aTime, sv, true);
	}

	/**
	 * will parse the string and transform it into a IGStaticValue of the
	 * signals' type.
	 * 
	 * @param aTime
	 * @param aIDC
	 * @param aValue
	 * @throws ZamiaException
	 */

	public void addBinaryVector(BigInteger aTime, String aIDC, String aValue, SourceLocation aLocation) throws ZamiaException {
		SignalInfo si = fIDCSignalMap.get(aIDC);

		if (si == null) {
			throw new ZamiaException("VCD: Error, unknown symbol: " + aIDC, aLocation);
		}

		IGTypeStatic type = si.getType();

		if (dump) {
			logger.info("VCD: Value change %s@%d => '%s' (type: %s)", si.getPath(), aTime, aValue, type);
		}

		if (!type.isArray()) {
			throw new ZamiaException("VCD: Expected bit vector type, found " + type, aLocation);
		}

		IGTypeStatic et = type.getStaticElementType(null);
		if (!et.isEnum()) {
			throw new ZamiaException("VCD: Expected bit vector type, but element type is " + et, aLocation);
		}

		String vs = aValue.substring(1);

		IGStaticValueBuilder b = new IGStaticValueBuilder(type, null, null);

		int off = b.getArrayOffset();
		int w = b.getNumArrayElements();

		int l = vs.length();
		char expansionC = '0';
		switch (vs.charAt(0)) {
		case '1':
			expansionC = '1';
			break;
		case '0':
			expansionC = '0';
			break;
		case 'Z':
			expansionC = 'Z';
			break;
		case 'X':
			expansionC = 'X';
			break;
		case 'U':
			expansionC = 'U';
			break;
		case 'L':
			expansionC = 'L';
			break;
		case 'H':
			expansionC = 'H';
			break;
		case '-':
			expansionC = '-';
			break;
		default:
			throw new ZamiaException("VCD: unknown value :" + vs.charAt(0), aLocation);
		}

		for (int i = 0; i < w; i++) {

			int bIdx = off + i;

			int vsIdx = l - i - 1;

			char c = vsIdx >= 0 ? vs.charAt(vsIdx) : expansionC;

			IGStaticValue sv = getBit(et, c, aLocation);
			if (sv == null) {
				return;
			}

			b.set(bIdx, sv, null);

		}

		IGStaticValue value = b.buildConstant();

		si.add(aTime, value, true);
	}

	public void add(BigInteger aTime, String aIDC, IGStaticValue aValue) throws ZamiaException {
		SignalInfo info = fIDCSignalMap.get(aIDC);
		if (info == null) {
			throw new ZamiaException("Unknow signal idcode: " + aIDC);
		}
		info.add(aTime, aValue, true);

		if (fStartTime.signum() < 0 || aTime.compareTo(fStartTime) < 0) {
			fStartTime = aTime;
		}

		if (aTime.compareTo(fEndTime) > 0) {
			fEndTime = aTime;
		}
	}

	public BigInteger getEndTime() {
		return fEndTime;
	}

	public void setEndTime(BigInteger aEndTime) {
		fEndTime = aEndTime;
	}

	public BigInteger getStartTime() {
		return fStartTime;
	}

	public void setStartTime(BigInteger aStartTime) {
		fStartTime = aStartTime;
	}

	public int getNumTracedSignals() {
		return fIDCSignalMap.size();
	}

	public SignalInfo getSignalInfo(int aIdx) {
		return fIDCSignalMap.get(aIdx);
	}

	public PathName getSignalName(int aIdx) {
		SignalInfo aSI = getSignalInfo(aIdx);
		if (aSI == null)
			return null;
		return aSI.getPath();
	}

	public int getNumSignals() {
		return fIDCSignalMap.size();
	}

	static Pattern fSegmentPattern;

	static {
		fSegmentPattern = Pattern.compile("^([^\\(]*)\\(([0-9]*)\\)$");
	}

	public static String convertSegmentId(String aSegment) {
		String segment = aSegment.toUpperCase();

		Matcher m = fSegmentPattern.matcher(segment);

		if (m.matches()) {
			String id = m.group(1);
			String num = m.group(2);

			segment = id + "#" + num;
		}

		return segment;
	}

}
