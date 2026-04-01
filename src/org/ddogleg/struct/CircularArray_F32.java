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

/**
 * A circular queue which can grow as needed.
 *
 * @author Peter Abeles
 */
public class CircularArray_F32 extends CircularArrayBase {
	//
	public float[] data;

	public CircularArray_F32() {
		this(10);
	}

	public CircularArray_F32( int dataSize ) {
		data = new float[dataSize];
	}

	/// Searches for the next element starting at index "offset" that the passed in condition is true for. Returns -1
	/// if no match was found
	public int indexOf( int offset, Match condition ) {
		if (offset < 0 || offset >= size)
			throw new IllegalArgumentException("Offset is out of bounds");

		for (int i = offset; i < size; i++) {
			if (condition.isMatch(data[(i + this.start)%data.length]))
				return i;
		}
		return -1;
	}

	/**
	 * Returns and removes the first element from the queue.
	 *
	 * @return first element in the queue
	 */
	public float popHead() {
		float r = data[start];
		removeHead();
		return r;
	}

	/**
	 * Returns and removes the last element from the queue.
	 *
	 * @return last element in the queue
	 */
	public float popTail() {
		float r = tail();
		removeTail();
		return r;
	}

	/**
	 * Value of the first element in the queue
	 */
	public float head() {
		return data[start];
	}

	/**
	 * Value of the last element in the queue
	 */
	public float tail() {
		return data[(start + size - 1)%data.length];
	}

	/**
	 * Returns the element in the queue at index.  No bounds check is performed and a garbage value might be returned.
	 *
	 * @param index Which element in the queue you wish to access
	 * @return the element's value
	 */
	public float get( int index ) {
		return data[(start + index)%data.length];
	}

	public void set( int index, float value ) {
		data[arrayIndex(index)] = value;
	}

	/**
	 * Adds a new element to the queue, but if the queue is full write over the oldest element.
	 *
	 * @param value Value which is to be added
	 */
	public void add( float value ) {
		// see if it needs to grow the queue
		if (size >= data.length) {
			data[start] = value;
			start = (start + 1)%data.length;
		} else {
			data[(start + size)%data.length] = value;
			size++;
		}
	}

	public CircularArray_F32 setTo( CircularArray_F32 original ) {
		if (this.data.length != original.data.length) {
			this.data = new float[original.data.length];
		}
		System.arraycopy(original.data, 0, this.data, 0, this.data.length);
		this.size = original.size;
		this.start = original.start;
		return this;
	}

	public CircularArray_F32 copy() {
		return new CircularArray_F32().setTo(this);
	}

	public void resizeQueue( int maxSize ) {
		if (this.data.length != maxSize) {
			this.data = new float[maxSize];
		}
	}

	@Override protected void shiftElements( int src0, int dst0, int length ) {
		for (int i = 0; i < length; i++) {
			data[arrayIndex(dst0 + i)] = data[arrayIndex(src0 + i)];
		}
	}

	@Override public int getMaxSize() {return data.length;}

	@Override public <T> T innerArray() {return (T)data;}

	public @FunctionalInterface interface Match {
		boolean isMatch( float value );
	}
}
