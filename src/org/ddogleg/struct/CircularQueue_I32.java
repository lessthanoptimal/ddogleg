/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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
public class CircularQueue_I32 {
	//
	public int data[];

	// index which is the start of the queue
	public int start;
	// number of elements in the queue
	public int size;

	public CircularQueue_I32() {
		this(10);
	}

	public CircularQueue_I32( int dataSize ) {
		data = new int[dataSize];
	}

	public void reset() {
		start = size = 0;
	}

	/**
	 * Returns and removes the first element from the queue.
	 * @return first element in the queue
	 */
	public int popHead() {
		int r = data[start];
		removeHead();
		return r;
	}

	/**
	 * Returns and removes the last element from the queue.
	 * @return last element in the queue
	 */
	public int popTail() {
		int r = tail();
		removeTail();
		return r;
	}

	/**
	 * Value of the first element in the queue
	 */
	public int head() {
		return data[start];
	}

	/**
	 * Value of the last element in the queue
	 */
	public int tail() {
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
	public int get( int index ) {
		return data[(start+index)%data.length];
	}

	/**
	 * Adds a new element to the queue.  If the queue isn't large enough to store this value then its internal data
	 * array will grow
	 * @param value Value which is to be added
	 */
	public void add( int value ) {
		// see if it needs to grow the queue
		if( size >= data.length) {
			int a[] = new int[ nextDataSize() ];

			System.arraycopy(data,start,a,0,data.length-start);
			System.arraycopy(data,0,a,data.length-start,start);
			start = 0;
			data = a;
		}
		data[(start+size)%data.length] = value;
		size++;
	}

	/**
	 * Adds a new element to the queue, but if the queue is full write over the oldest element.
	 *
	 * @param value Value which is to be added
	 */
	public void addW( int value ) {
		// see if it needs to grow the queue
		if( size >= data.length) {
			data[start] = value;
			start = (start+1)%data.length;
		} else {
			data[(start+size)%data.length] = value;
			size++;
		}
	}

	private int nextDataSize() {
		if( data.length < 1000 )
			return data.length*2;
		else if( data.length < 10000 )
			return data.length*3/2;
		else
			return data.length*6/5;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean isFull(){ return size == data.length;}
}
