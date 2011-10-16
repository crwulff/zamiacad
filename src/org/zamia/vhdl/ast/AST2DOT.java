/* 
 * Copyright 2011 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 08, 2011
 */
package org.zamia.vhdl.ast;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.zamia.IDesignModule;
import org.zamia.util.HashSetArray;
import org.zamia.util.ZStack;
import org.zamia.zdb.ZDB;

/**
 * Convert an AST to GraphViz' dot language
 * @author Guenter Bartsch
 *
 */

public class AST2DOT {

	private ZDB fZDB;

	private HashSet<String> fBlacklistedFields = new HashSet<String>();

	private IDesignModule fNode;

	public AST2DOT(IDesignModule aNode, ZDB aZDB) {
		fNode = aNode;
		fZDB = aZDB;
	}

	static class DotJob {

		private String fNID;

		private Object fObj;

		public DotJob(String aNID, Object aObj) {
			fNID = aNID;
			fObj = aObj;
		}

	}

	@SuppressWarnings("rawtypes")
	public void convert(PrintWriter aOut) {

		HashSet<String> done = new HashSet<String>();

		ZStack<DotJob> todo = new ZStack<DotJob>();

		HashMap<String, Integer> clazzCounter = new HashMap<String, Integer>();

		aOut.printf("digraph AST {\n  rankdir = LR;\n");

		todo.push(new DotJob("n" + fNode.getDBID(), fNode));

		while (!todo.isEmpty()) {

			DotJob job = todo.pop();

			if (done.contains(job.fNID)) {
				continue;
			}

			done.add(job.fNID);

			Class clazz = job.fObj.getClass();

			String clazzName = clazz.getName();
			int count = 1;
			Integer c = clazzCounter.get(clazzName);
			if (c != null) {
				count = c.intValue() + 1;
			}
			clazzCounter.put(clazzName, count);

			dump(job.fNID, job.fObj, aOut, todo);
		}

		aOut.printf("}\n");

		System.out.println("Statistics:\n=============\n");

		for (String key : clazzCounter.keySet()) {

			int count = clazzCounter.get(key);
			System.out.printf("%8d: %s\n", count, key);

		}

	}

	@SuppressWarnings({ "rawtypes" })
	private void dump(String aNID, Object aObj, PrintWriter aOut, ZStack<DotJob> aTODO) {

		Class cls = aObj.getClass();

		aOut.printf("  %s[shape=none,margin=0,label=<\n", aNID);
		aOut.printf("<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">\n");
		aOut.printf("<TR><TD COLSPAN=\"2\">%s</TD></TR>\n", cls.getName());

		int portnum = 0;
		String portId = "p0";

		StringBuilder linkBuffer = new StringBuilder();

		while (!Object.class.equals(cls)) {
			Field[] fields = cls.getDeclaredFields();

			for (Field field : fields) {
				field.setAccessible(true);

				int modifiers = field.getModifiers();

				if (Modifier.isStatic(modifiers))
					continue;
				else if (Modifier.isFinal(modifiers))
					continue;
				else if (Modifier.isTransient(modifiers))
					continue;

				Object obj;
				try {
					obj = field.get(aObj);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				String fieldName = field.getName();

				// blacklist certain fields
				if (fieldName.equals("fDBID")) {
					continue;
				}
				if (fBlacklistedFields.contains(fieldName)) {
					continue;
				}

				portId = "p" + portnum++;
				if (obj == null) {
					aOut.printf(" <TR><TD>%s</TD><TD PORT=\"%s\">%s</TD></TR>\n", fieldName, portId, "null");
				} else {
					if (isAPrimitiveType(obj)) {
						//printIndented(depth, field.getName() + ": " + obj);

						aOut.printf(" <TR><TD>%s</TD><TD PORT=\"%s\">%s</TD></TR>\n", fieldName, portId, forHTML(obj.toString()));

						handleChild(aNID, portId, obj, aTODO, linkBuffer);

					} else {
						aOut.printf(" <TR><TD>%s</TD><TD PORT=\"%s\">%s</TD></TR>\n", fieldName, portId, obj != null ? "obj" : "null");

						handleChild(aNID, portId, obj, aTODO, linkBuffer);
					}
				}
			}

			cls = cls.getSuperclass();

		}

		aOut.printf("  </TABLE>>];\n");

		if (aObj instanceof Object[]) {
			for (Object o2 : (Object[]) aObj) {
				handleChild(aNID, portId, o2, aTODO, linkBuffer);
			}
		} else if (aObj instanceof HashMap) {
			HashMap hm = (HashMap) aObj;
			for (Iterator it = hm.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object o2 = hm.get(key);
				handleChild(aNID, portId, o2, aTODO, linkBuffer);
			}
		} else if (aObj instanceof Array) {
			Array a = (Array) aObj;
			int m = Array.getLength(a);
			for (int j = 0; j < m; j++) {
				Object o2 = Array.get(a, j);
				handleChild(aNID, portId, o2, aTODO, linkBuffer);
			}
		} else if (aObj instanceof ArrayList) {
			ArrayList al = (ArrayList) aObj;
			int m = al.size();
			for (int j = 0; j < m; j++) {
				Object o2 = al.get(j);
				handleChild(aNID, portId, o2, aTODO, linkBuffer);
			}
		} else if (aObj instanceof HashSetArray) {
			HashSetArray hma = (HashSetArray) aObj;

			int m = hma.size();
			for (int j = 0; j < m; j++) {
				Object o2 = hma.get(j);
				handleChild(aNID, portId, o2, aTODO, linkBuffer);
			}
		}
		aOut.println(linkBuffer);
	}

	public void blacklistField(String aFieldID) {
		fBlacklistedFields.add(aFieldID);
	}

	private boolean isBlacklisted(Object aObj) {
		//return aObj instanceof IGSubProgram || aObj instanceof IGType || aObj instanceof IGStaticValue || aObj instanceof IGInterpreterContext || aObj instanceof IGOperation;
		//return aObj instanceof IGSubProgram || aObj instanceof IGStaticValue || aObj instanceof IGInterpreterContext || aObj instanceof IGOperation;
		return false;
	}

	private void handleChild(String aNID, String aPortId, Object aO2, ZStack<DotJob> aTODO, StringBuilder aLinkBuffer) {

		if (aO2 == null) {
			return;
		}

		if (aO2 instanceof Long) {
			// DBID ?

			long dbid = ((Long) aO2).longValue();
			Object o2 = fZDB.load(dbid);

			if (isBlacklisted(o2)) {
				return;
			}

			if (o2 != null) {
				aTODO.push(new DotJob("n" + dbid, o2));
				aLinkBuffer.append("  " + aNID + ":" + aPortId + " -> n" + dbid + ";\n");
				return;
			}
		}

		if (isAPrimitiveType(aO2)) {
			return;
		}

		if (isBlacklisted(aO2)) {
			return;
		}

		aTODO.push(new DotJob(objectLabel(aO2), aO2));
		aLinkBuffer.append("  " + aNID + ":" + aPortId + " -> " + objectLabel(aO2) + ";\n");
	}

	private String objectLabel(Object aObj) {
		long hash = aObj.hashCode();
		if (hash > 0)
			return "o" + hash;
		return "p" + (-hash);
	}

	private String forHTML(String aText) {
		final StringBuilder result = new StringBuilder();

		int l = aText.length();
		for (int i = 0; i < l; i++) {
			char c = aText.charAt(i);

			if (Character.isLetterOrDigit(c))
				result.append(c);
			else
				result.append('.');

		}
		return result.toString();
	}

	@SuppressWarnings("rawtypes")
	private static boolean isAPrimitiveType(Object aObj) {

		if (aObj instanceof String || aObj instanceof Long || aObj instanceof Integer || aObj instanceof Boolean || aObj instanceof File)
			return true;

		Class clazz = aObj.getClass();

		if (clazz == java.lang.Boolean.TYPE)
			return true;

		if (clazz == java.lang.Character.TYPE)
			return true;

		if (clazz == java.lang.Byte.TYPE)
			return true;

		if (clazz == java.lang.Short.TYPE)
			return true;

		if (clazz == java.lang.Integer.TYPE)
			return true;

		if (clazz == java.lang.Long.TYPE)
			return true;

		if (clazz == java.lang.Float.TYPE)
			return true;

		if (clazz == java.lang.Double.TYPE)
			return true;

		if (clazz == java.lang.Void.TYPE)
			return true;

		return false;
	}

}
