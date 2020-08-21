/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.struct;

import java.io.Serializable;
import java.util.List;

/**
 * Base class for {@link FastArray} and {@link FastQueue}. Provides access to the data but does not provide
 * methods which add or grow the internal data structure.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public abstract class FastAccess<T> implements Serializable {
	public T[] data;
	public int size;
	public final Class<T> type;

	protected FastAccess(Class<T> type ) {
		this.type = type;
	}

	public T get( int index ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		return data[index];
	}

	/**
	 * The maximum number of elements before the 'data' array needs to grow
	 * @return length of 'data'
	 */
	public int getMaxSize() {
		return data.length;
	}

	/**
	 * Removes an element from the queue and preserves the order of all elements. This is done by shifting elements
	 * in the array down one and placing the removed element at the old end of the list. O(N) runtime.
	 *
	 * @param index Index of the element being removed
	 * @return The object removed.
	 */
	public abstract T remove( int index );

	/**
	 * Removes the specified index from the array by swapping it with last element. Does not preserve order
	 * but has a runtime of O(1).
	 *
	 * @param index The index to be removed.
	 * @return The removed object
	 */
	public abstract T removeSwap( int index );

	/**
	 * Number of elements in the array
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns a wrapper around FastQueue that allows it to act as a read only list.
	 * There is little overhead in using this interface.
	 *
	 * NOTE: The same instead of a list is returned each time.  Be careful when writing
	 * concurrent code and create a copy.
	 *
	 * @return List wrapper.
	 */
	public abstract List<T> toList();

	public T getTail() {
		return data[size-1];
	}

	/**
	 * Returns an element in the list relative to the tail
	 * @param index index relative to tail.  0 == the tail. size-1 = first element
	 * @return element
	 */
	public T getTail( int index ) {
		return data[size-1-index];
	}

	/**
	 * Reverse the item order in this queue.
	 */
	public void reverse() {
		for (int i = 0; i < size / 2; i++) {
			T tmp = data[i];
			data[i] = data[size - i - 1];
			data[size - i - 1] = tmp;
		}
	}

	/**
	 * The passed in function is called once for each element in the list
	 */
	public void forEach( FunctionEach<T> function ) {
		for (int i = 0; i < size; i++) {
			function.process(i,data[i]);
		}
	}

	/**
	 * For each with a range of values specified
	 * @param idx0 lower extent, inclusive
	 * @param idx1 upper extent, exclusive
	 */
	public void forEach( int idx0 , int idx1, FunctionEach<T> function ) {
		if( idx1 > size )
			throw new IllegalArgumentException("idx1 is out of range");

		for (int i = idx0; i < idx1; i++) {
			function.process(i,data[i]);
		}
	}

	public interface FunctionEach<T> {
		void process( int index, T o );
	}
}
