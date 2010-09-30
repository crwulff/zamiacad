/* 
 * Copyright 2008-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 6, 2008
 */
package org.zamia.zil;

import java.util.HashMap;

import org.zamia.ExceptionLogger;
import org.zamia.ZamiaLogger;
import org.zamia.vhdl.ast.ASTObject;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public abstract class ZILObject implements ZILIObject {

	public final static ExceptionLogger el = ExceptionLogger.getInstance();
	
	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	protected ASTObject fSrc;

	protected ZILIContainer fContainer;

	protected ZILType fType;

	private HashMap<String, ZILAttribute> fAttributes;

	protected String fId;
	
	public ZILObject(ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		fSrc = aSrc;
		fContainer = aContainer;
		fType = aType;
	}

	public ZILObject(String aId, ZILType aType, ZILIContainer aContainer, ASTObject aSrc) {
		fSrc = aSrc;
		fContainer = aContainer;
		fType = aType;
		fId = aId;
	}

	public ASTObject getSrc() {
		return fSrc;
	}

	public ZILIContainer getContainer() {
		return fContainer;
	}

	public final ZILType getType() {
		return fType;
	}

	// attribute stuff
	public void addAttribute(ZILAttribute aAttr) {
		if (fAttributes == null) {
			fAttributes = new HashMap<String, ZILAttribute>(1);
		}
		
		fAttributes.put(aAttr.getId(), aAttr);
	}

	public ZILAttribute getAttribute(String aId) {
		if (fAttributes == null)
			return null;
		return fAttributes.get(aId);
	}

	public String getId() {
		if (fId != null)
			return fId;
		return "Anonymous object #"+hashCode();
	}

	public void setId(String aId) {
		fId = aId;
	}
}
