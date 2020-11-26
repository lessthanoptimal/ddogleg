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
 * A circular queue which can grow as needed.
 *
 * @author Peter Abeles
 */
public class CircularArray_F64 {
	//
	public double[] data;

	// index which is the start of the queue
	public int start;
	// number of elements in the queue
	public int size;

	public CircularArray_F64() {
		this(10);
	}

	public CircularArray_F64( int dataSize ) {
		data = new double[dataSize];
	}

	public void reset() {
		start = size = 0;
	}

	/**
	 * Returns and removes the first element from the queue.
	 * @return first element in the queue
	 */
	public double popHead() {
		double r = data[start];
		removeHead();
		return r;
	}

	/**
	 * Returns and removes the last element from the queue.
	 * @return last element in the queue
	 */
	public double popTail() {
		double r = tail();
		removeTail();
		return r;
	}

	/**
	 * Value of the first element in the queue
	 */
	public double head() {
		return data[start];
	}

	/**
	 * Value of the last element in the queue
	 */
	public double tail() {
		return data[(start+size-1)%data.length];
	}

	/**
	 * Removes the first element
	 */
	public void removeHead() {
		start = (start+1)%data.length;
		size--;
	}

	/**
	 * Removes the last element
	 */
	public void removeTail() {
		size--;
	}

	/**
	 * Returns the element in the queue at index.  No bounds check is performed and a garbage value might be returned.
	 * @param index Which element in the queue you wish to access
	 * @return the element's value
	 */
	public double get( int index ) {
		return data[(start+index)%data.length];
	}

	/**
	 * Adds a new element to the queue, but if the queue is full write over the oldest element.
	 *
	 * @param value Value which is to be added
	 */
	public void add( double value ) {
		// see if it needs to grow the queue
		if( size >= data.length) {
			data[start] = value;
			start = (start+1)%data.length;
		} else {
			data[(start+size)%data.length] = value;
			size++;
		}
	}

	public void set( CircularArray_F64 original ) {
		if( this.data.length != original.data.length) {
			this.data = new double[original.data.length];
		}
		System.arraycopy(original.data,0,this.data,0,this.data.length);
		this.size = original.size;
		this.start = original.start;
	}

	public CircularArray_F64 copy() {
		CircularArray_F64 r = new CircularArray_F64();
		r.set(this);
		return r;
	}

	public void resizeQueue( int maxSize ) {
		if( this.data.length != maxSize) {
			this.data = new double[maxSize];
		}
	}

	public int queueSize() {
		return data.length;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean isFull(){ return size == data.length;}
}
