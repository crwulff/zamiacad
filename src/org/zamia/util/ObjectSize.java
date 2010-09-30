/* 
 * Copyright 2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Jul 28, 2008
 */
package org.zamia.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.zamia.ZamiaLogger;


/**
 * 
 * @author Guenter Bartsch
 * 
 */

public class ObjectSize {

	public final static ZamiaLogger logger = ZamiaLogger.getInstance();

	private static boolean skipTransientFields = true;

	private static final boolean SKIP_STATIC_FIELD = true;

	private static final boolean SKIP_FINAL_FIELD = true;

	/**
	 * Calls java.lang.instrument.Instrument.getObjectSize(object).
	 * 
	 * @param object
	 *            the object to size
	 * @return an implementation-specific approximation of the amount of storage
	 *         consumed by the specified object.
	 * 
	 * @see java#lang#instrument#Instrument#Instrumentation#getObjectSize(Object
	 *      objectToSize)
	 */
	private static long sizeOf(Object object) {

//		Class clazz = object.getClass();

		if (object instanceof Boolean)
			return 4;

		if (object instanceof Character)
			return 4;

		if (object instanceof Byte)
			return 4;

		if (object instanceof Short)
			return 4;

		if (object instanceof Integer)
			return 4;

		if (object instanceof Long)
			return 8;

		if (object instanceof Float)
			return 4;

		if (object instanceof Double)
			return 8;

		if (object instanceof String)
			return 44 + ((String) object).length();

		//logger.error("This is not a primitive type: %s ", clazz);

		return 32;
	}

	public static long deepSizeOf(Object objectToSize) {
		//Set<Integer> doneObj = new HashSet<Integer>();
		Map<Object, Object> doneObj = new IdentityHashMap<Object, Object>();
		return deepSizeOf(objectToSize, doneObj, 0);
	}

	@SuppressWarnings("unchecked")
	private static long deepSizeOf(Object o, Map<Object, Object> doneObj, int depth) {
		if (o == null) {
			return 0;
		}

		long size = 0;

		if (doneObj.containsKey(o)) {
			return 0;
		}

		// doLog(o, depth);

		doneObj.put(o, null);
		size = sizeOf(o);

		if (o instanceof Object[]) {
			for (Object obj : (Object[]) o) {
				size += deepSizeOf(obj, doneObj, depth + 1);
			}
		} else if (o instanceof HashMap) {
			HashMap hm = (HashMap) o;
			size += 32 + 6*4;
			for (Iterator it = hm.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = hm.get(key);
				size += deepSizeOf(value, doneObj, depth + 1);
				size += deepSizeOf(key, doneObj, depth + 1);
			}
		} else if (o instanceof Array) {
			Array a = (Array) o;
			int m = Array.getLength(a);
			for (int j = 0; j < m; j++) {
				Object o2 = Array.get(a, j);
				size += deepSizeOf(o2, doneObj, depth + 1);
			}
		} else if (o instanceof ArrayList) {
			ArrayList al = (ArrayList) o;
			size += 32 + 2*4;
			int m = al.size();
			for (int j = 0; j < m; j++) {
				Object o2 = al.get(j);
				size += deepSizeOf(o2, doneObj, depth + 1);
			}
		} else if (o instanceof HashSetArray) {
			HashSetArray hma = (HashSetArray) o;

			// 32 + 4*4 : size of this object
			// 32 + 2*4 : size of embedded arraylist
			// 32 + 4 + 32 + 6*4 : size of embedded hashset  
			size+= 32 + 4*4 + 32 + 2*4 + 32 + 4 + 32 + 6*4;
			
			int m = hma.size();
			for (int j = 0; j < m; j++) {
				Object o2 = hma.get(j);
				size += deepSizeOf(o2, doneObj, depth + 1);
			}
		} else {

			Class cls = o.getClass();

			while (!Object.class.equals(cls)) {
				Field[] fields = cls.getDeclaredFields();

				for (Field field : fields) {
					field.setAccessible(true);
					Object obj;
					try {
						obj = field.get(o);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}

					if (isComputable(field)) {
						size += deepSizeOf(obj, doneObj, depth + 1);
					}
				}

				cls = cls.getSuperclass();
			}

		}

		return size;
	}

//	private static void doLog(Object o, int depth) {
//		StringBuilder buf = new StringBuilder();
//		for (int i = 0; i < depth; i++)
//			buf.append(" ");
//		int cnt = 0;
//		if (o instanceof IntermediateObject) {
//			cnt = ((IntermediateObject) o).getCnt();
//		}
//		//		buf.append(o.getClass() + "@" + Integer.toHexString(o.hashCode()));
//		buf.append(o.getClass() + "&" + cnt);
//		if (!(o instanceof HashMap)) {
//			buf.append(" : ");
//			buf.append(o.toString());
//		}
//		logger.debug("%s", buf.toString());
//	}

	/**
	 * Return true if the specified class is a primitive type
	 */
	@SuppressWarnings("unchecked")
	private static boolean isAPrimitiveType(Class clazz) {
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

	private static boolean isComputable(Field field) {
		int modifiers = field.getModifiers();

		if (isAPrimitiveType(field.getType()))
			return false;
		else if (SKIP_STATIC_FIELD && Modifier.isStatic(modifiers))
			return false;
		else if (SKIP_FINAL_FIELD && Modifier.isFinal(modifiers))
			return false;
		else if (skipTransientFields && Modifier.isTransient(modifiers))
			return false;
		else
			return true;
	}

	public static void setSkipTransientFields(boolean skipTransientFields_) {
		skipTransientFields = skipTransientFields_;
	}
}
