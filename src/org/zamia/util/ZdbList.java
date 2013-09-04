package org.zamia.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.zamia.Utils;
import org.zamia.instgraph.IGObject;
import org.zamia.zdb.ZDB;

/**
 * http://stackoverflow.com/questions/13534985/abstract-iterator-for-indexable-colletions
 * */
public class ZdbList<T> extends ArrayList<Long> {
	
	public ZdbList(int size) {
		super(size);
	}
	
	public ZdbList() {
		super();
	}
	
	public Iterable<T> zdbIterator(ZDB zdb) {
		return new ZdbIterator<T>(zdb);
	}
	
	/**
	 * Implements both iterator and iterable to avoid creating too many instances.
	 * */
	public class ZdbIterator<T> implements Iterator<T>, Iterable<T> {
	    private int i = 0; 
	    private final int size = size();
	    private boolean created = false; // just in case somebody wants to reuse the iterable
	    private final ZDB zdb;
	    ZdbIterator(ZDB zdb) { this.zdb = zdb; }
	    public T next() { 
	        if (!hasNext()) throw new NoSuchElementException(); 
	        return (T) zdb.load(get(i++));
	    }

	    public boolean hasNext() { return i != size;}

	    public void remove() {
	        throw new UnsupportedOperationException("Remove is not implemented");
	    }
		
		public Iterator<T> iterator() {
			if (created) throw new IllegalAccessError("Can be iterated only once");
			created = true;
			return this;
		}
	}

	public ArrayList<T> toList(ZDB zdb) {
		return Utils.createArrayList(zdbIterator(zdb), size());
	}

}


