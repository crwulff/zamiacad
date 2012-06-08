package org.zamia;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Utils {
		
	public static <T> T removeLast(List<T> list) {
		int i = list.size()-1;
		T res = list.get(i);
		list.remove(i);
		return res;
	}
	public static String concatenate(Object[] collection) {
		return concatenate(Arrays.asList(collection), ",");
	}
	
	public static String concatenate(Object[] collection, String separator) {
		return concatenate(Arrays.asList(collection), separator);
	}
	
	public static String concatenate(Object[] collection, String separator, boolean... keepLastDot) {
		return concatenate(Arrays.asList(collection), separator, keepLastDot);
	}
	
	public static String concatenate(Collection c) {
		return concatenate(c, ",");
	}
	public static String concatenate(Collection c, String separator, boolean... keepLastDot) {
		StringBuffer sb = new StringBuffer();
		for (Object o: c)
			sb.append(o.toString()).append(separator);
				
		if (!c.isEmpty() && keepLastDot.length == 0) // remove extra separator at the end
			sb.setLength(sb.length() - separator.length()); 
				
		return sb.toString();
	}

	public static class Counter<KeyT> extends HashMap<KeyT, Integer> {
		public int inc(KeyT key) {
			Integer cnt = get(key);
			cnt = (cnt == null) ? 1 : cnt +1;
			put(key, cnt);
			return cnt;
		}
	}
}

