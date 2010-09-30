/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Mar 9, 2009
 */
package org.zamia.analysis.ast;

import java.util.HashMap;
import java.util.HashSet;

import org.zamia.util.HashSetArray;
import org.zamia.vhdl.ast.ASTObject;
import org.zamia.vhdl.ast.InterfaceDeclaration;
import org.zamia.vhdl.ast.Name;


/**
 * 
 * @author Guenter Bartsch
 *
 */

public class MappedInterfaces {

	private HashMap<InterfaceDeclaration,HashSetArray<String>> fMap;
	private HashMap<String,HashSetArray<String>> fIdMap;
	private HashMap<String, HashSetArray<MappedFormal>> fReverseMap;
	
	public MappedInterfaces() {
		fMap = new HashMap<InterfaceDeclaration, HashSetArray<String>>();
		fIdMap = new HashMap<String, HashSetArray<String>>();
		fReverseMap = new HashMap<String, HashSetArray<MappedFormal>>();
	}
	
	private void add(InterfaceDeclaration aFormal, String aActual, ASTObject aASTObject) {
		
		HashSetArray<String> l = fMap.get(aFormal);
		if (l == null) {
			l = new HashSetArray<String>();
			fMap.put(aFormal, l);
		}
		l.add(aActual);

		l = fIdMap.get(aFormal.getId());
		if (l == null) {
			l = new HashSetArray<String>();
			fIdMap.put(aFormal.getId(), l);
		}
		l.add(aActual);

		HashSetArray<MappedFormal> l2 = fReverseMap.get(aActual);
		if (l2 == null) {
			l2 = new HashSetArray<MappedFormal>();
			fReverseMap.put(aActual, l2);
		}
		l2.add(new MappedFormal(aFormal, aASTObject));
	}


	public void add(InterfaceDeclaration aFormal, ASTObject aObject, HashSet<ASTObject> aDone) {
		
		if (aObject == null || aDone.contains(aObject)) {
			return;
		}
		aDone.add(aObject);
		
		if (aObject instanceof Name) {
			Name name = (Name) aObject;
			add (aFormal, name.getId(), aObject);
		}
		
		int n = aObject.getNumChildren();
		for (int i = 0; i<n; i++) {
			ASTObject child = aObject.getChild(i);
			if (child == null) {
				continue;
			}
			add (aFormal, child, aDone);
		}
	}	
	
	public void add(InterfaceDeclaration aFormal, ASTObject aObject) {
		
		add (aFormal, aObject, new HashSet<ASTObject>());
		
	}

//	
//	
//	public void add(InterfaceDeclaration aFormal, Operation aOperation) {
//		
//		add (aFormal, aName.getId());
//		
//		int n = aName.getNumExtensions();
//		for (int i = 0; i<n; i++) {
//			
//			NameExtension ext = aName.getExtension(i);
//			
//			if (ext instanceof NameExtensionIndex) {
//				NameExtensionIndex nei = (NameExtensionIndex) ext;
//				
//				int m = nei.getNumIndices();
//				for (int j = 0; j<m; j++) {
//					Operation idx = nei.getIndex(j);
//					add (aFormal, idx);
//				}
//			} else if (ext instanceof NameExtensionFunctionCall) {
//				NameExtensionFunctionCall nefc = (NameExtensionFunctionCall) ext;
//				
//				int m = nefc.getNumParams();
//				for (int j = 0; j<m; j++) {
//					AssociationElement ae = nefc.getParam(j);
//					add (aFormal, ae.getActualPart());
//				}
//			} else if (ext instanceof NameExtensionRange) {
//				NameExtensionRange ner = (NameExtensionRange) ext;
//				
//				int m = ner.getNumRanges();
//				for (int j = 0; j<m; j++) {
//					Range r = ner.getRange(j);
//					
//					Name name = r.getName();
//					if (name != null) {
//						add (aFormal, name);
//					} else {
//						add (aFormal, r.getLeft();
//						add (aFormal, r.getRight());
//					}
//				}
//			} else if (ext instanceof NameExtensionQualifiedExpression) {
//				
//			}
//		}
//	}
	
	public HashSetArray<MappedFormal> getFormals(String aActual) {
		return fReverseMap.get(aActual);
	}

	public HashSetArray<String> getActuals(String aPortID) {
		return fIdMap.get(aPortID);
	}

	
}
