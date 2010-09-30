/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.util;

import java.util.ArrayList;

/**
 * 
 * @author Guenter Bartsch
 *
 * @param <T>
 */

public class ZStack<T> {
	
	private ArrayList<T> stack;

	public ZStack() {
		stack = new ArrayList<T>();
	}
	
	public void push(T o_) {
		stack.add(o_);
	}
	
	public T pop() {
		return stack.remove(stack.size()-1);
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public int size() {
		return stack.size();
	}

	public T peek() {
		return stack.get(size()-1);
	}

	public T get(int aI) {
		return stack.get(aI);
	}
	
	@Override
	public String toString() {
		return stack.toString();
	}

}
