/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 2, 2008
 */
package org.zamia.vhdl.ast;

import java.io.Serializable;

import org.zamia.ZamiaException;


/**
 * DUUID: Design Unit ID, uniquely identifying a design/library unit. Consists of
 * 
 * - library id (e.g. "WORK")
 * - id 
 * - optionally an architecture id (architectures only)
 * 
 * type will specify whether this DUUID is an entity, architecture, package ... (LUType == Library Unit Type)
 * 
 * examples: 
 * - an entity       WORK.E1       would have fType==LUType.Entity,       fLibId=="WORK", fId=="E1", fArchId==null
 * - an architecture WORK.E1(ARCH) would have fType==LUType.Architecture, fLibId=="WORK", fId=="E1", fArchId=="ARCH"
 *  
 * fUID ("unique identifier") will have the same information but mangled into a single string (useful for hashing for example)
 * 
 * @author Guenter Bartsch
 * 
 */

@SuppressWarnings("serial")
public class DUUID implements Serializable {

	public final static String SEPARATOR = "_#_";

	public enum LUType {
		Entity, Architecture, Package, PackageBody, Configuration
	};

	private final LUType fType;

	private final String fLibId, fId, fArchId, fUID;

	public DUUID(LUType aType, String aLibId, String aId, String aArchId) {

		fType = aType;
		fLibId = aLibId;
		fId = aId;
		fArchId = aArchId;

		switch (fType) {
		case Entity:
			fUID = fLibId + SEPARATOR + "entity" + SEPARATOR + fId;
			break;
		case Architecture:
			fUID = fLibId + SEPARATOR + "arch" + SEPARATOR + fId + SEPARATOR + fArchId;
			break;
		case Configuration:
			fUID = fLibId + SEPARATOR + "conf" + SEPARATOR + fId;
			break;
		case Package:
			fUID = fLibId + SEPARATOR + "pkg" + SEPARATOR + fId;
			break;
		case PackageBody:
			fUID = fLibId + SEPARATOR + "pkgBody" + SEPARATOR + fId;
			break;
		default:
			fUID = "???";
		}
	}

	public static String getLibId(String aUID) {

		int pos = aUID.indexOf(SEPARATOR);
		if (pos < 0)
			return null;

		return aUID.substring(0, pos);
	}

	public String getUID() {
		return fUID;
	}

	public LUType getType() {
		return fType;
	}

	public String getLibId() {
		return fLibId;
	}

	public String getId() {
		return fId;
	}

	public String getArchId() {
		return fArchId;
	}

	@Override
	public int hashCode() {
		return fUID.hashCode();
	}

	@Override
	public boolean equals(Object aObject) {

		if (!(aObject instanceof DUUID))
			return false;

		DUUID duuid = (DUUID) aObject;

		return fUID.equals(duuid.getUID());
	}

	public String toCompactString() {
		if (fType == LUType.Architecture) {
			if (!fId.equals(fArchId))
				return fLibId + "." + fId + "(" + fArchId + ")";
			return fLibId + "." + fId;
		}
		return toString();
	}

	private enum Symbols {
		NONE, LPAREN, RPAREN, DOT, ID, EOF
	};

	public static DUUID parse(String aToplevel) throws ZamiaException {

		char ch = ' ';
		int pos = 0;
		int len = aToplevel.length();

		String libId = null;
		String archId = null;
		String entityId = null;

		Symbols sym = Symbols.NONE;
		boolean eof = false;

		while (!eof) {

			while (Character.isWhitespace(ch)) {
				if (pos >= len) {
					eof = true;
					break;
				}
				ch = aToplevel.charAt(pos);
				pos++;
			}

			if (eof) {
				break;
			}

			StringBuilder id = new StringBuilder();
			switch (ch) {
			case '(':
				sym = Symbols.LPAREN;
				if (pos < len) {
					ch = aToplevel.charAt(pos);
					pos++;
				} else {
					eof = true;
				}
				break;
			case ')':
				sym = Symbols.RPAREN;
				if (pos < len) {
					ch = aToplevel.charAt(pos);
					pos++;
				} else {
					eof = true;
				}
				break;
			case '.':
				sym = Symbols.DOT;
				if (pos < len) {
					ch = aToplevel.charAt(pos);
					pos++;
				} else {
					eof = true;
				}
				break;
			default:
				if (!Character.isJavaIdentifierPart(ch)) {
					throw new ZamiaException("Error parsing toplevel string: encountered " + ch);
				}
				while (Character.isJavaIdentifierPart(ch)) {
					id.append(ch);
					if (pos < len) {
						ch = aToplevel.charAt(pos);
						pos++;
					} else {
						eof = true;
						break;
					}
				}
				sym = Symbols.ID;
			}

			switch (sym) {
			case ID:

				if (entityId == null) {
					entityId = id.toString();
				} else if (archId == null) {
					archId = id.toString();
				} else {
					throw new ZamiaException("Unexpected identifier: " + id);
				}

				break;
			case DOT:
				if (libId == null) {
					libId = entityId;
					entityId = null;
				} else {
					throw new ZamiaException("Unexpected dot.");
				}
				break;
			case LPAREN:
				if (entityId == null) {
					throw new ZamiaException("( encountered. Please specify an entity first.");
				}
				break;
			case RPAREN:
				if (archId == null) {
					throw new ZamiaException(") encountered but no architecture specified.");
				}
			}
		}

		if (entityId == null) {
			throw new ZamiaException("No entity specified.");
		}

		LUType type = LUType.Architecture;
		if (archId == null) {
			type = LUType.Entity;
		}
		if (libId == null) {
			libId = "WORK";
		}

		return new DUUID(type, libId, entityId, archId);
	}

	@Override
	public String toString() {
		if (fType == LUType.Architecture) {
			return fLibId + "." + fId + "(" + fArchId + ")";
		}
		return fLibId + "." + fId;
	}

	public DUUID cloneDUUID() {
		return new DUUID(fType, fLibId, fId, fArchId);
	}

	public DUUID getEntityDUUID() {

		if (fType == LUType.Architecture) {
			return new DUUID(LUType.Entity, fLibId, fId, null);
		}

		return this;
	}

}
