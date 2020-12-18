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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * A growable array which provides access to the raw array but does not own the elements inside of the array. When
 * it is inexpensive to do so (O(1) operation) it will discard references to data when they are no longer needed.
 *
 * @author Peter Abeles
 */
public class FastArray<T> extends FastAccess<T> {

	// Wrapper around this class for lists
	private final FastArrayList<T> list = new FastArrayList<>(this);

	public FastArray( Class<T> type , int initialMaxSize ) {
		super(type);
		this.size = 0;
		data = (T[]) Array.newInstance(type, initialMaxSize);
	}

	public FastArray( Class<T> type ) {
		this(type,10);
	}

	public void set( int index, T value ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		data[index] = value;
	}

	public void add( T value ) {
		if( size >= data.length ) {
			reserve((data.length+1)*2);
		}
		data[size++] = value;
	}

	@Override
	public T remove( int index ) {
		T removed = data[index];
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		data[size-1] = null;
		size--;
		return removed;
	}

	@Override
	public T removeSwap( int index ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		T ret = data[index];
		size -= 1;
		data[index] = data[size];
		data[size] = null;
		return ret;
	}

	/**
	 * Searches for the object and removes it if it's contained in the list. O(N) operation.
	 * @param target Object to be removed
	 * @return true if it was removed or false if it was not found
	 */
	public boolean remove( T target ) {
		int index = indexOf(target);
		if( index < 0 )
			return false;
		remove(index);
		return true;
	}

	public T removeTail() {
		if( size <= 0 )
			throw new IllegalArgumentException("The array is empty");
		size -= 1;
		T ret = data[size];
		data[size] = null;
		return ret;
	}

	/**
	 * Sets the size of the list to zero. External references are not modified.
	 */
	public void reset() {
		size = 0;
	}

	/**
	 * Sets the size of the list to zero and removes all internal references inside the current array.
	 */
	public void clear() {
		Arrays.fill(data,0,size,null);
		size = 0;
	}

	/**
	 * Ensures that the internal array has at least `length` elements. If it does not then a new internal array
	 * is created with the specified length and elements from the old are copied into the new. The `size` does
	 * not change.
	 *
	 * @param length Requested minimum internal array length
	 */
	public void reserve(int length ) {
		reserve(length,true);
	}

	public void reserve(int length, boolean copy ) {
		// now need to grow since it is already larger
		if (this.data.length >= length)
			return;

		T []data = (T[])Array.newInstance(type, length);
		if (copy)
			System.arraycopy(this.data,0,data,0,size);
		this.data = data;
	}

	/**
	 * Changes the size to the specified length. Equivalent to calling {@link #reserve} and this.size = N.
	 * @param length The new size of the queue
	 */
	public void resize(int length) {
		reserve(length);
		this.size = length;
	}

	/**
	 * Changes the size and fills each element with this value
	 */
	public void resize(int length, T value) {
		reserve(length,false);
		Arrays.fill(data,0,length,value);
		this.size = length;
	}


	public void addAll( FastAccess<T> list ) {
		for( int i = 0; i < list.size; i++ ) {
			add( list.data[i]);
		}
	}

	public void add( T[] array , int first, int length ) {
		for( int i = 0; i < length; i++ ) {
			add( array[first+i]);
		}
	}

	public void addAll( final List<T> list ) {
		final int originalSize = this.size;
		resize(this.size+list.size());
		for (int i = 0; i < list.size(); i++) {
			data[originalSize+i] = list.get(i);
		}
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
	@Override
	public List<T> toList() {
		return list;
	}
}
