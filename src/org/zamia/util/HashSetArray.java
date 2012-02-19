/* 
 * Copyright 2005-2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created on May 3, 2005
 */
package org.zamia.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Combination of HashSet and ArrayList.
 * 
 * @author Guenter Bartsch
 */

@SuppressWarnings("serial")
public class HashSetArray<V> implements Serializable, Cloneable, Iterable<V> {

	private ArrayList<V> array;

	private HashSet<V> set;

	// when removing elements, array is not updated,
	// so we're keeping this journal of deleted elements
	private HashSet<V> deletedElements;

	public boolean sorted = false;

	public HashSetArray() {
		this(5);
	}

	public HashSetArray(ArrayList<V> array_, HashSet<V> set_) {
		array = array_;
		set = set_;
		deletedElements = new HashSet<V>(1);
	}

	public HashSetArray(int loadFactor_) {
		set = new HashSet<V>(loadFactor_);
		array = new ArrayList<V>(loadFactor_);
		deletedElements = new HashSet<V>(1);
	}

	public HashSetArray(Collection<V> c) {
		array = new ArrayList<V>(c);
		set = new HashSet<V>(c);
		deletedElements = new HashSet<V>(1);
	}

	public Iterator<V> iterator() {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return array.iterator();
	}

//	private static final int THRESHOLD = 1000;

	private void cleanupArray() {
		int n = array.size();
		//boolean showProgress = deletedElements.size() > THRESHOLD;
		ArrayList<V> newarray = new ArrayList<V>(n - deletedElements.size());
//		int pold = -1;
//		int count = 0;
		for (int i = 0; i < n; i++) {
			V o = array.get(i);
			if (!deletedElements.contains(o))
				newarray.add(o);

//			if (showProgress) {
//				count++;
//				int p = (count * 100 / n);
//				if (p > pold) {
//					pold = p;
//					System.out.print("Cleaning up large HashSetArray " + p
//							+ "% done.\r");
//				}
//			}
		}
//		if (showProgress)
//			System.out.println("Cleaning up large HashSetArray 100% done.");
		deletedElements.clear();
		array = newarray;
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 * 
	 * @return the number of elements in this set (its cardinality).
	 */
	public int size() {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return array.size();
	}

	public V get(int idx_) {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return array.get(idx_);
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 * 
	 * @return <tt>true</tt> if this set contains no elements.
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.
	 * 
	 * @param o
	 *            element whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains the specified element.
	 */
	public boolean contains(V o) {
		return set.contains(o);
	}

	/**
	 * Returns <tt>true</tt> if this set contains any of the elements in
	 * specified HashSetArray.
	 * 
	 * @param o
	 *            hash with elements whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains any of the elements in
	 *         specified hash.
	 */
	public boolean containsAnyOf(HashSetArray<V> o) {
		int n = o.array.size();
		for (int i = 0; i < n; i++) {
			if (contains(o.array.get(i)))
				return true;
		}
		return false;
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 * 
	 * @param o
	 *            element to be added to this set.
	 * @return <tt>true</tt> if the set did not already contain the specified
	 *         element.
	 */
	public boolean add(V o) {
		if (set.add(o)) {
			if (deletedElements.contains(o))
				deletedElements.remove(o);
			else
				array.add(o);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * 
	 * @param o
	 *            object to be removed from this set, if present.
	 * @return <tt>true</tt> if the set contained the specified element.
	 */
	public boolean remove(V o) {
		if (set.remove(o)) {
			// array.remove(o);
			deletedElements.add((V) o);
			return true;
		} else {
			return false;
		}
	}

	public V remove(int i) {
		V o = get(i);
		remove(o);
		return o;
	}

	/**
	 * Removes all of the elements from this set.
	 */
	public void clear() {
		set.clear();
		array.clear();
		deletedElements.clear();
	}

	@SuppressWarnings("unchecked")
	public V[] toArray() {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return (V[]) array.toArray();
	}

//	public void writeObject(ObjectOutputStream out) throws IOException {
//		if (!deletedElements.isEmpty())
//			cleanupArray();
//
//		int size = array.size();
//		out.writeInt(size);
//		for (int j = 0; j < size; j++) {
//			out.writeObject(array.get(j));
//		}
//		out.writeBoolean(sorted);
//	}
//
//	@SuppressWarnings("unchecked")
//	public void readObject(ObjectInputStream in) throws IOException,
//			ClassNotFoundException {
//		int size = in.readInt();
//		array = new ArrayList<V>(size);
//		for (int j = 0; j < size; j++) {
//			array.add((V) in.readObject());
//		}
//		sorted = in.readBoolean();
//		set = new HashSet<V>(size);
//		set.addAll(array);
//		deletedElements = new HashSet<V>(1);
//	}

	@SuppressWarnings("unchecked")
	public HashSetArray<V> clone() {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return new HashSetArray<V>((ArrayList<V>) array.clone(),
				(HashSet<V>) set.clone());
	}

	public boolean addAll(HashSetArray<V> hsa_) {
		boolean changed = false;
		int n = hsa_.size();
		for (int i = 0; i < n; i++) {
			changed |= add(hsa_.get(i));
		}
		return changed;
	}

	public boolean removeAll(HashSetArray<V> hsa_) {
		if (!deletedElements.isEmpty())
			cleanupArray();
		if (!hsa_.deletedElements.isEmpty())
			hsa_.cleanupArray();

		return array.removeAll(hsa_.array) && set.removeAll(hsa_.set);
	}

	public boolean retainAll(HashSetArray<V> hsa_) {
		if (!deletedElements.isEmpty())
			cleanupArray();
		if (!hsa_.deletedElements.isEmpty())
			hsa_.cleanupArray();

		return array.retainAll(hsa_.array) && set.retainAll(hsa_.set);
	}
	
	public int indexOf(V elem) {
		if (!deletedElements.isEmpty())
			cleanupArray();
		return array.indexOf(elem);
	}
	
	@Override
	public String toString() {
		
		StringBuilder buf = new StringBuilder("[");
		
		int n = array.size();
		
		for (int i=0; i<n; i++) {
			buf.append(array.get(i));
			if (i<n-1) {
				buf.append(", ");
			}
		}
		
		buf.append("]");
		return buf.toString();
	}
}
