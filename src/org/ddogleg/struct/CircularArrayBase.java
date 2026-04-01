/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

/// Base class for Circular Arrays
public abstract class CircularArrayBase {
	/// index which is the start of the queue
	public int start;
	/// number of elements in the queue
	public int size;

	public final void reset() {
		start = size = 0;
	}

	/// Removes the first element. O(1) complexity
	public final void removeHead() {
		start = (start + 1)%getMaxSize();
		size--;
	}

	/// Removes the last element. O(1) complexity
	public final void removeTail() {
		size--;
	}

	/// Removes an element. O(N) complexity.
	public void remove( int index ) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException();
		if (index == 0) {
			removeHead();
		} else if (index == size - 1) {
			removeTail();
		} else {
			// Copy array and handle wrap around
			shiftElements(index + 1, index, size - index - 1);
			size -= 1;
		}
	}

	/// Number of elements used in the array
	public int size() {
		return size;
	}

	/// If no elements are used in the array
	public boolean isEmpty() {
		return size == 0;
	}

	/// If all available elements are used
	public boolean isFull() {return size == getMaxSize();}

	/// Copies the value in the src into dst inside the inner array. Needs to handle wrap around
	protected abstract void shiftElements( int src0, int dst0, int length );

	/// Converts circular index into the inner array index
	public int arrayIndex( int index ) {
		return (start + index)%getMaxSize();
	}

	/// Number of elements in the inner array
	public abstract int getMaxSize();

	/// Reference to the inner array
	public abstract <T>T innerArray();
}
