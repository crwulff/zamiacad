/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 15, 2009
 */
package org.zamia.rtl.sim.vcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zamia.ZamiaException;
import org.zamia.rtl.sim.SignalInfo;
import org.zamia.util.HashMapArray;
import org.zamia.util.PathName;
import org.zamia.util.SimpleRegexp;
import org.zamia.zil.ZILValue;


/**
 * Holds all the information that was extracted from a VCD file in memory
 * 
 * @author Guenter Bartsch
 * 
 */

public class VCDData {

	class VCDModule {

		PathName fName;

		HashMapArray<String, VCDModule> fModules;

		HashMapArray<PathName, SignalInfo> fSignals;

		public VCDModule(PathName aName) {
			fName = aName;

			fModules = new HashMapArray<String, VCDModule>();
			fSignals = new HashMapArray<PathName, SignalInfo>();
		}

	}

	private VCDModule fRoot;

	private HashMapArray<String, SignalInfo> fIDCSignalMap;

	private HashMapArray<PathName, SignalInfo> fPathSignalMap;

	private HashMap<PathName, VCDModule> fModuleMap;

	private ArrayList<PathName> fAllSignalPaths;

	private HashMap<PathName, Integer> fSignalIdxMap;

	private long fTimeScale, fStartTime, fEndTime;

	public VCDData() {

		fIDCSignalMap = new HashMapArray<String, SignalInfo>();
		fPathSignalMap = new HashMapArray<PathName, SignalInfo>();
		fAllSignalPaths = new ArrayList<PathName>();
		fSignalIdxMap = new HashMap<PathName, Integer>();

		PathName rootPath = new PathName(".");

		fRoot = new VCDModule(rootPath);

		fModuleMap = new HashMap<PathName, VCDModule>();

		fModuleMap.put(rootPath, fRoot);

	}

	public int findSignalIdx(PathName aPath) {
		Integer i = fSignalIdxMap.get(aPath);
		if (i == null)
			return 0;
		return i.intValue();
	}

	public void newSignal(String aIDC, SignalInfo aSignalInfo) {
		fIDCSignalMap.put(aIDC, aSignalInfo);

		PathName path = aSignalInfo.getPath();
		fSignalIdxMap.put(path, fIDCSignalMap.size() - 1);

		fPathSignalMap.put(path, aSignalInfo);

		int n = path.getNumSegments();

		VCDModule module = fRoot;

		PathName curPath = new PathName(".");

		for (int i = 0; i < n - 1; i++) {

			String segment = path.getSegment(i);

			curPath = curPath.append(segment);

			VCDModule childModule = module.fModules.get(segment);

			if (childModule == null) {
				childModule = new VCDModule(curPath);
				module.fModules.put(segment, childModule);
				fModuleMap.put(curPath, childModule);
			}

			module = childModule;
		}

		module.fSignals.put(path, aSignalInfo);

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

	public long getTimeScale() {
		return fTimeScale;
	}

	public void setTimeScale(long aTimeScale) {
		fTimeScale = aTimeScale;
		if (fTimeScale < 1)
			fTimeScale = 1;
	}

	public SignalInfo getSignalInfo(PathName aPath) {
		return fPathSignalMap.get(aPath);
	}

	public SignalInfo getSignalInfo(String aSymbol) {
		return fIDCSignalMap.get(aSymbol);
	}

	public void add(long aTime, String aIDC, ZILValue aValue) throws ZamiaException {
		SignalInfo info = fIDCSignalMap.get(aIDC);
		if (info == null) {
			throw new ZamiaException("Unknow signal idcode: " + aIDC);
		}
		info.add(aTime, aValue, true);

		if (fStartTime < 0 || aTime < fStartTime) {
			fStartTime = aTime;
		}

		if (aTime > fEndTime) {
			fEndTime = aTime;
		}
	}

	public long getEndTime() {
		return fEndTime;
	}

	public long getStartTime() {
		return fStartTime;
	}

	public int getNumTracedSignals() {
		return fIDCSignalMap.size();
	}

	public SignalInfo getSignalInfo(int aIdx) {
		return fIDCSignalMap.get(aIdx);
	}

	public int getNumAvailableModules(PathName aParentPath) throws ZamiaException {
		VCDModule module = fModuleMap.get(aParentPath);

		if (module == null) {
			throw new ZamiaException("Invalid path: " + aParentPath);
		}

		return module.fModules.size();
	}

	public int getNumAvailableSignals(PathName aParentPath) throws ZamiaException {
		VCDModule module = fModuleMap.get(aParentPath);

		if (module == null) {
			throw new ZamiaException("Invalid path: " + aParentPath);
		}

		return module.fSignals.size();
	}

	public PathName getAvailableSignal(PathName aParentPath, int aIdx) throws ZamiaException {
		VCDModule module = fModuleMap.get(aParentPath);

		if (module == null) {
			throw new ZamiaException("Invalid path: " + aParentPath);
		}

		return module.fSignals.get(aIdx).getPath();
	}

	public PathName getAvailableModule(PathName aParentPath, int aIdx) throws ZamiaException {
		VCDModule module = fModuleMap.get(aParentPath);

		if (module == null) {
			throw new ZamiaException("Invalid path: " + aParentPath);
		}

		return module.fModules.get(aIdx).fName;
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

}
