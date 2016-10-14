/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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
public class GrowQueue_F32 {

	public float data[];
	public int size;

	public GrowQueue_F32( int maxSize ) {
		data = new float[ maxSize ];
		this.size = 0;
	}

	public GrowQueue_F32() {
		this(10);
	}

	public void reset() {
		size = 0;
	}

	public void addAll( GrowQueue_F32 queue ) {
		if( size+queue.size > data.length ) {
			float temp[] = new float[ (size+queue.size) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(queue.data,0,data,size,queue.size);
		size += queue.size;
	}

	public void addAll( float[] array , int startIndex , int endIndex ) {
		if( endIndex > array.length )
			throw new IllegalAccessError("endIndex is larger than input array");

		int arraySize = endIndex-startIndex;

		if( size+arraySize > data.length ) {
			float temp[] = new float[ (size+arraySize) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(array,startIndex,data,size,arraySize);
		size += arraySize;
	}

	public void add( float val ) {
		push(val);
	}

	public void push( float val ) {
		if( size == data.length ) {
			float temp[] = new float[ size * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = val;
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
	public void insert( int index , float value ) {
		if( size == data.length ) {
			float temp[] = new float[ size * 2];
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

	public float removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else {
			throw new RuntimeException("Size zero, no tail");
		}
	}

	public float get( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[index];
	}

	public float unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index, float value  ) {
		data[index] = value;
	}

	public void setTo( GrowQueue_F32 original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	public void fill( float value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void resize( int size ) {
		if( data.length < size ) {
			data = new float[size];
		}
		this.size = size;
	}

	public void setMaxSize( int size ) {
		if( data.length < size ) {
			data = new float[size];
		}
	}

	public int size() {
		return size;
	}

    public float pop() {
        return data[--size];
    }

	/**
	 * Returns the index of the first element with the specified 'value'.  return -1 if it wasn't found
	 * @param value Value to search for
	 * @return index or -1 if it's not in the list
	 */
	public int indexOf( float value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return i;
		}
		return -1;
	}
}
