package org.zamia.util;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;

/**Extends Set with get(key) method, as explained in http://stackoverflow.com/a/8757856/1083704 */
public class RecordSet<E> extends java.util.HashSet<E>{

	final Map<E,E> map;
	{
		Field f;
		try {
			f = HashSet.class.getDeclaredField("map");
			f.setAccessible(true);
			Object o;
			o = f.get(this);
			map = (Map) o;
		} catch (Exception e) {
			throw new RuntimeException("Failed to obtain the underlying map", e);
		}
	}
	public boolean add(E e) {
		boolean exists = this.contains(e);
		map.put(e,e);
		return exists;
	}
	public void simpleAdd(E e) {
		map.put(e,e);
	}

	public E get(Object key) {
		E val = map.get(key); 
		return val;
	}
	@Override
	public boolean contains (Object key) {
		return get(key) != null;
	}

}
