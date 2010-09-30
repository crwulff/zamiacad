/* 
 * Copyright 2008,2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Dec 24, 2008
 */
package org.zamia.util;

import java.io.Serializable;

/**
 * 
 * @author Guenter Bartsch
 * 
 * @param <T>
 * @param <U>
 */
@SuppressWarnings("serial")
public class Pair<T, U> implements Serializable {
	private final T fFirst;

	private final U fSecond;

	public Pair(T aFirst, U aSecond) {
		fFirst = aFirst;
		fSecond = aSecond;
	}

	public T getFirst() {
		return fFirst;
	}

	public U getSecond() {
		return fSecond;
	}

	@Override
	public String toString() {
		return "("+fFirst+","+fSecond+")";
	}

}