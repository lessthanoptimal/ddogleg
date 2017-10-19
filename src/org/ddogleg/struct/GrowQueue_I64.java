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


import java.util.Arrays;

/**
 * This is a queue that is composed of integers.  Elements are added and removed from the tail
 *
 * @author Peter Abeles
 */
public class GrowQueue_I64 implements GrowQueue<GrowQueue_I64> {

	public long data[];
	public int size;

	public GrowQueue_I64(int maxSize) {
		data = new long[ maxSize ];
		this.size = 0;
	}

	public GrowQueue_I64() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with all zeros
	 */
	public static GrowQueue_I64 zeros( int length ) {
		GrowQueue_I64 out = new GrowQueue_I64(length);
		out.size = length;
		return out;
	}

	@Override
	public void reset() {
		size = 0;
	}

	public void addAll( GrowQueue_I64 queue ) {
		if( size+queue.size > data.length ) {
			long temp[] = new long[ (size+queue.size) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(queue.data,0,data,size,queue.size);
		size += queue.size;
	}

	public void addAll( long[] array , int startIndex , int endIndex ) {
		if( endIndex > array.length )
			throw new IllegalAccessError("endIndex is larger than input array");

		int arraySize = endIndex-startIndex;

		if( size+arraySize > data.length ) {
			long temp[] = new long[ (size+arraySize) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(array,startIndex,data,size,arraySize);
		size += arraySize;
	}

	public void add(long value) {
		push(value);
	}

	public void push( long val ) {
		if( size == data.length ) {
			long temp[] = new long[ size * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = val;
	}

	public long get( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[index];
	}

	public long unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index , long value ) {
		data[index] = value;
	}

	@Override
	public void setTo( GrowQueue_I64 original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	public void remove( int index ) {
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		size--;
	}

	/**
	 * Inserts the value at the specified index and shifts all the other values down.
	 */
	public void insert( int index , long value ) {
		if( size == data.length ) {
			long temp[] = new long[ size * 2];
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

	public long removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else {
			throw new RuntimeException("Size zero, no tail");
		}
	}

	@Override
	public void resize( int size ) {
		if( data.length < size ) {
			data = new long[size];
		}
		this.size = size;
	}

	@Override
	public void extend( int size ) {
		if( data.length < size ) {
			long []tmp = new long[size];
			System.arraycopy(data,0,tmp,0,this.size);
			data = tmp;
		}
		this.size = size;
	}

	public void fill( long value ) {
		Arrays.fill(data, 0, size, value);
	}

	@Override
	public void setMaxSize( int size ) {
		if( data.length < size ) {
			data = new long[size];
		}
	}

	@Override
	public int size() {
		return size;
	}

	public long pop() {
		return data[--size];
	}

	@Override
	public void zero() {
		Arrays.fill(data,0,size,0);
	}

	@Override
	public GrowQueue_I64 copy() {
		GrowQueue_I64 ret = new GrowQueue_I64(size);
		ret.setTo(this);
		return ret;
	}

	/**
	 * Returns the index of the first element with the specified 'value'.  return -1 if it wasn't found
	 * @param value Value to search for
	 * @return index or -1 if it's not in the list
	 */
	public int indexOf( long value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return i;
		}
		return -1;
	}
}
