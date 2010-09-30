/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Feb 16, 2009
 */
package org.zamia.instgraph.sim.vcd;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.List;

import org.zamia.FSCache;
import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.ZamiaException;
import org.zamia.ZamiaProject;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.sim.IGISimCursor;
import org.zamia.instgraph.sim.IGISimObserver;
import org.zamia.instgraph.sim.IGISimulator;
import org.zamia.instgraph.sim.vcd.parser.VCDParser;
import org.zamia.util.PathName;


/**
 * A (at the moment proof-of-concept) simulator plugin that reads simulation
 * data from a VCD file on disk
 * 
 * @author Guenter Bartsch
 * 
 */
public class VCDImport implements IGISimulator {

	private static FSCache fsCache = FSCache.getInstance();

	private VCDData fData;

	public VCDImport() {
	}

	@Override
	public void open(ToplevelPath aToplevel, File aFile, PathName aPrefix, ZamiaProject aZPrj) throws IOException, ZamiaException {

		fData = new VCDData(aZPrj, aToplevel.getToplevel(), aPrefix);

		VCDParser parser = new VCDParser();

		SourceFile sf = new SourceFile(aFile);

		Reader reader = fsCache.openFile(sf, false);

		parser.parse(reader, sf, fData);

		reader.close();
	}

	@Override
	public IGISimCursor createCursor() {
		return new VCDSimCursor(this);
	}

	@Override
	public void shutdown() {
		// GC will take care of us.
	}

	@Override
	public BigInteger getStartTime() {
		return fData.getStartTime();
	}

	@Override
	public BigInteger getEndTime() {
		return fData.getEndTime();
	}

	@Override
	public boolean isSimulator() {
		return false;
	}

	@Override
	public int getInterfaceVersion() {
		return 3;
	}

	@Override
	public List<PathName> findSignalNamesRegexp(String aRegex, int aLimit) {
		return fData.findSignalPaths(aRegex, aLimit);
	}


	@Override
	public SourceLocation getCurrentSourceLocation() throws ZamiaException {
		// FIXME: not supported yet.
		return null;
	}

	@Override
	public void addObserver(IGISimObserver aO) {
	}

	@Override
	public void assign(PathName aSignalName, IGStaticValue aValue) throws ZamiaException {
	}

	@Override
	public void removeObserver(IGISimObserver aO) {
	}

	@Override
	public void reset() throws ZamiaException {
	}

	@Override
	public void run(BigInteger aTime) throws ZamiaException {
	}

	@Override
	public void trace(PathName aSignalName) throws ZamiaException {
	}

	@Override
	public void unTrace(PathName aSignalName) throws ZamiaException {
	}

	VCDData getData() {
		return fData;
	}

}
