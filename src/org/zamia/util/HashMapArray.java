/* 
 * Copyright 2005-2008 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by guenter on Apr 17, 2005
 * 
 */

package org.zamia.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Combination of a HashMap and an HashSetArray, provides: 
 * - O(1) access to elements using a key 
 * - O(1) access to elements using their index 
 * - preserves order of added unique elements (ordered set semantics)
 * 
 * @author Guenter Bartsch
 * 
 * @param <K>
 * @param <V>
 */

@SuppressWarnings("serial")
public class HashMapArray<K, V> implements Serializable, Cloneable {

	private HashSetArray<V> set;

	private HashMap<K, V> hashmap;

	public HashMapArray() {
		set = new HashSetArray<V>();
		hashmap = new HashMap<K, V>();
	}

	public HashMapArray(HashSetArray<V> set_, HashMap<K, V> hashmap_) {
		set = set_;
		hashmap = hashmap_;
	}

	public HashMapArray(int initialSize_) {
		set = new HashSetArray<V>(initialSize_);
		hashmap = new HashMap<K, V>(initialSize_);
	}

	public void put(K key_, V o_) {
		if (hashmap.containsKey(key_)) {
			V old = hashmap.get(key_);
			set.remove(old);
		}
		hashmap.put(key_, o_);
		set.add(o_);
	}

	public int size() {
		return set.size();
	}

	public V get(int index_) {
		return set.get(index_);
	}

	public V get(K key_) {
		return hashmap.get(key_);
	}

	public void remove(K key_) {
		if (hashmap.containsKey(key_)) {
			V o = hashmap.get(key_);
			hashmap.remove(key_);
			set.remove(o);
		}
	}

	public boolean containsKey(K key_) {
		return hashmap.containsKey(key_);
	}

	public boolean containsValue(V o_) {
		return hashmap.containsValue(o_);
	}

	public V[] toArray() {
		return (V[]) set.toArray();
	}

	public Set<K> keySet() {
		return hashmap.keySet();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return hashmap.entrySet();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		return new HashMapArray<K, V>((HashSetArray<V>) set.clone(),
				(HashMap<K, V>) hashmap.clone());
	}

	public void clear() {
		set.clear();
		hashmap.clear();

	}

	public void verify() {
		if (set.size() != hashmap.size())
			System.out.println("Ouch!");
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("{");
		int count = 0;
		for (Iterator<K> i = keySet().iterator(); i.hasNext();) {
			
			K key = i.next();
			
			V value = get(key);

			buf.append(key);
			buf.append("=>");
			buf.append(value);
			
			if(i.hasNext())
				buf.append(", ");
			
			count ++;
			if(count>10) {
				buf.append(" ... ");
				break;
			}
		}
		
		buf.append("}");
		return buf.toString();
	}
}
