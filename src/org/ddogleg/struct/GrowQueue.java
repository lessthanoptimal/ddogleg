/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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
	 * Ensures that the internal array is at least this size and changes the size to be this.
	 * @param size desired new size
	 */
	void resize( int size );

	/**
	 * Ensures that the internal array's length is at least this size. Size is left unchanged
	 * @param size minimum size of internal array
	 */
	void setMaxSize( int size );

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
}
