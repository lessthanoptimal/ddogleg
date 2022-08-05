/*
 * Copyright (c) 2012-2022, Peter Abeles. All Rights Reserved.
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

/**
 * Interface for growable queues of primitive types
 *
 * @author Peter Abeles
 */
public interface DogArrayPrimitive<T extends DogArrayPrimitive<T>> {

	/**
	 * Sets the size to zero.
	 *
	 * @return Returns 'this' to allow chaining of operations.
	 */
	T reset();

	/**
	 * Returns true if the specified array index is outside the allowed value range
	 */
	default boolean isIndexOutOfBounds( int index ) {
		return index < 0 || index >= size();
	}

	/**
	 * Turns 'this' into a copy of 'original'
	 *
	 * @param original queue that is to be copied
	 * @return Returns 'this' to allow chaining of operations.
	 */
	T setTo( T original );

	/**
	 * <p>Ensures that the internal array is at least this size. Value of elements previously in the array will
	 * not be changed. If the size is increased then the value of new elements in undefined.</p>
	 *
	 * If you wish to resize the array and avoid copying over past values for performance reasons, then you must
	 * either resize(0) or call {@link #reset} first.
	 *
	 * @param size desired new size
	 */
	T resize( int size );

	/**
	 * Changes the array to the specified size. If there is not enough storage, a new internal array is created
	 * and the elements copied over. This is the same as: a.reserve(size);a.size = size;
	 *
	 * @param size desired new size
	 */
	void extend( int size );

	/**
	 * Ensures that the internal array's length is at least this size. Size is left unchanged. If the array
	 * is not empty and it needs to grow then the existing array is copied into the new array.
	 *
	 * @param amount minimum size of internal array
	 */
	void reserve( int amount );

	/**
	 * Flips the elements such that a[i] = a[N-i-1] where N is the number of elements.
	 */
	void flip();

	/**
	 * Number of elements in the queue
	 *
	 * @return size of queue
	 */
	int size();

	/**
	 * Sets all elements to 0 or False for binary queues
	 */
	void zero();

	T copy();

	/**
	 * Sorts the data from smallest to largest
	 */
	void sort();

	/** True if the container has no elements */
	default boolean isEmpty() {
		return size() == 0;
	}
}
