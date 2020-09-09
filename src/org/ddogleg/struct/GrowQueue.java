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

/**
 * Interface for growable queues of primitive types
 *
 * @author Peter Abeles
 */
public interface GrowQueue<T extends GrowQueue<T>> {

	/**
	 * Sets the size to zero.
	 */
	void reset();

	/**
	 * Turns 'this' into a copy of 'original'
	 * @param original queue that is to be copied
	 */
	void setTo( T original );

	/**
	 * Ensures that the internal array is at least this size and changes the size to be this. Array
	 * data is not saved
	 * @param size desired new size
	 */
	void resize( int size );

	/**
	 * Ensures that the internal array this size. If a new array needs to be declared the old data
	 * is saved
	 * @param size desired new size
	 */
	void extend( int size );

	/**
	 * Ensures that the internal array's length is at least this size. Size is left unchanged
	 * @param size minimum size of internal array
	 */
	void setMaxSize( int size );

	/**
	 * Flips the elements such that a[i] = a[N-i-1] where N is the number of elements.
	 */
	void flip();

	/**
	 * Number of elements in the queue
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
