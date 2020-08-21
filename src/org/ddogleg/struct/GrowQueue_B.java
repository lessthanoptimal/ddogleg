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


import java.util.Arrays;

/**
 * This is a queue that is composed of booleans.  Elements are added and removed from the tail
 *
 * @author Peter Abeles
 */
public class GrowQueue_B implements GrowQueue<GrowQueue_B> {

	public boolean[] data;
	public int size;

	public GrowQueue_B(int maxSize) {
		data = new boolean[ maxSize ];
		this.size = 0;
	}

	public GrowQueue_B() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with false
	 */
	public static GrowQueue_B zeros( int length ) {
		GrowQueue_B out = new GrowQueue_B(length);
		out.size = length;
		return out;
	}

	public static GrowQueue_B array( boolean ...values ) {
		GrowQueue_B out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i];
		}
		return out;
	}

	/**
	 * Non-zero values are set to true
	 */
	public static GrowQueue_B array( int ...values ) {
		GrowQueue_B out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i] != 0;
		}
		return out;
	}

	/**
	 * Counts the number of times the specified value occures in the list
	 */
	public int count( boolean value ) {
		int total = 0;
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				total++;
		}
		return total;
	}

	@Override
	public void reset() {
		size = 0;
	}

	public void add(boolean value) {
		push(value);
	}

	public void push( boolean val ) {
		if( size == data.length ) {
			boolean[] temp = new boolean[ size * 2+5];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = val;
	}

	public boolean get( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[index];
	}

	/**
	 * Returns an element starting from the end of the list. 0 = size -1
	 */
	public boolean getTail( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[size-index-1];
	}

	public boolean unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index, boolean value  ) {
		data[index] = value;
	}

	@Override
	public void setTo( GrowQueue_B original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	public void fill( boolean value ) {
		Arrays.fill(data, 0, size, value);
	}

	/**
	 * Inserts the value at the specified index and shifts all the other values down.
	 */
	public void insert( int index , boolean value ) {
		if( size == data.length ) {
			boolean[] temp = new boolean[ size * 2+5];
			System.arraycopy(data,0,temp,0,index);
			temp[index] = value;
			System.arraycopy(data,index,temp,index+1,size-index);
			this.data = temp;
			size++;
		} else {
			size++;
			for( int i = size-1; i > index; i-- ) {
				data[i] = data[i-1];
			}
			data[index] = value;
		}
	}

	public boolean removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else {
			throw new RuntimeException("Size zero, no tail");
		}
	}


	/**
	 * Removes elements from the list starting at 'first' and ending at 'last'
	 * @param first First index you wish to remove. Inclusive.
	 * @param last Last index you wish to remove. Inclusive.
	 */
	public void remove( int first , int last ) {
		if( last < first )
			throw new IllegalArgumentException("first <= last");
		if( last >= size )
			throw new IllegalArgumentException("last must be less than the max size");

		int delta = last-first+1;
		for( int i = last+1; i < size; i++ ) {
			data[i-delta] = data[i];
		}
		size -= delta;
	}

	@Override
	public void resize( int size ) {
		if( data.length < size ) {
			data = new boolean[size];
		}
		this.size = size;
	}

	/**
	 * Resizes the array and assigns the default value to every element.
	 * @param size New size
	 * @param value Default value
	 */
	public void resize( int size , boolean value ) {
		resize(size);
		fill(value);
	}

	@Override
	public void extend( int size ) {
		if( data.length < size ) {
			boolean []tmp = new boolean[size];
			System.arraycopy(data,0,tmp,0,this.size);
			data = tmp;
		}
		this.size = size;
	}

	@Override
	public void setMaxSize( int size ) {
		if( data.length < size ) {
			data = new boolean[size];
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void zero() {
		Arrays.fill(data,0,size,false);
	}

	@Override
	public GrowQueue_B copy() {
		GrowQueue_B ret = new GrowQueue_B(size);
		ret.setTo(this);
		return ret;
	}

	@Override
	public void flip() {
		if( size <= 1 )
			return;

		int D = size/2;
		for (int i = 0,j=size-1; i < D; i++,j--) {
			boolean tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
	}

	public boolean pop() {
		return data[--size];
	}

	/**
	 * Returns the index of the first element with the specified 'value'.  return -1 if it wasn't found
	 * @param value Value to search for
	 * @return index or -1 if it's not in the list
	 */
	public int indexOf( boolean value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return i;
		}
		return -1;
	}

	@Override
	public void sort() {
		throw new RuntimeException("Undefined for boolean");
	}
}
