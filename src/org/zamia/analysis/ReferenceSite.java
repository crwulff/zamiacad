/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 25, 2008
 */
package org.zamia.analysis;

import java.io.PrintStream;

import org.zamia.ASTNode;
import org.zamia.ExceptionLogger;
import org.zamia.SourceLocation;
import org.zamia.ToplevelPath;
import org.zamia.instgraph.IGObject;
import org.zamia.vhdl.ast.ConcurrentStatement;
import org.zamia.vhdl.ast.DeclarativeItem;
import org.zamia.vhdl.ast.VHDLNode;

/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ReferenceSite extends ReferenceSearchResult {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();

	public enum RefType {
		Read, Write, Call, Declaration, ReadWrite, Unknown, Instantiation, Assignment
	};

	private final RefType fRefType;

	private final long fDBID;

	public ReferenceSite(VHDLNode aIO, RefType aRefType) {
		super("", aIO.getLocation(), 0);

		fRefType = aRefType;
		fDBID = 0l;

		String prefix = "";
		switch (getRefType()) {
		case Read:
			prefix = "READ";
			break;
		case Call:
			prefix = "CALL";
			break;
		case Write:
			prefix = "WRITE";
			break;
		case ReadWrite:
			prefix = "R/W";
			break;
		case Declaration:
			prefix = "DECLARATION";
			break;
		case Instantiation:
			prefix = "INSTANTIATION";
			break;
		case Assignment:
			prefix = "ASSIGN";
			break;
		default:
			prefix = "???";
			break;
		}

		ASTNode io = aIO;
		while (io != null && !(io instanceof ConcurrentStatement)) {
			io = io.getParent();
		}
		if (io == null) {
			io = aIO;
		}

		setTitle(prefix + " " + getLocation().toString() + " " + io.toString());

		if (aIO instanceof DeclarativeItem) {
			DeclarativeItem decl = (DeclarativeItem) aIO;
			String id = decl.getId();
			setLength(id.length());
		} else {
			setLength(aIO.toString().length());
		}

	}

	public ReferenceSite(String aTitle, SourceLocation aLocation, int aLength, RefType aRefType, ToplevelPath aPath, IGObject aObject) {
		super(aTitle, aLocation, aLength);
		fRefType = aRefType;
		fPath = aPath;
		fDBID = aObject != null ? aObject.getDBID() : 0l;
	}

	public RefType getRefType() {
		return fRefType;
	}

	public void dump(int aIndent, PrintStream aOut) {
		VHDLNode.printlnIndented("ReferenceSite: " + toString(), aIndent, aOut);
	}

	@Override
	public int countRefs() {
		return 1;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + (int) getDBID();
	}
	
	@Override
	/**DBID but not Path is not taken into account in addition to all things of base class.
	 * Comparison is particularly used in removing duplicate SearchAssignments made to the same object on the 
	 * same line and RootResults preventing repeated search (circular dependencies) in assignment-through search.
	 * */
	public boolean equals(Object that) {
		return that instanceof ReferenceSite && ((ReferenceSite) that).getDBID() == getDBID() && super.equals(that);
	}
	
	public long getDBID() {
		return fDBID;
	}

}
