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


import org.ddogleg.sorting.QuickSort_F32;

import java.util.Arrays;

/**
 * This is a queue that is composed of integers.  Elements are added and removed from the tail
 *
 * @author Peter Abeles
 */
public class DogArray_F32 implements DogArrayPrimitive<DogArray_F32> {

	public float[] data;
	public int size;

	public DogArray_F32( int maxSize ) {
		data = new float[ maxSize ];
		this.size = 0;
	}

	public DogArray_F32() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with all zeros
	 */
	public static DogArray_F32 zeros( int length ) {
		DogArray_F32 out = new DogArray_F32(length);
		out.size = length;
		return out;
	}

	public static DogArray_F32 array( float ...values ) {
		DogArray_F32 out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i];
		}
		return out;
	}

	/**
	 * Counts the number of times the specified value occures in the list
	 */
	public int count( float value ) {
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

	public void addAll( DogArray_F32 queue ) {
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
			float temp[] = new float[ size * 2+5];
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
	 * Sets this array to be equal to the array segment
	 * @param array (Input) source array
	 * @param offset first index
	 * @param length number of elements to copy
	 */
	public void setTo( float[] array , int offset , int length ) {
		resize(length);
		System.arraycopy(array,offset,data,0,length);
	}

	/**
	 * Set's the value of this array to the passed in raw array.
	 * @param src (Input) The input array
	 * @return A reference to "this" to allow chaining of commands
	 */
	public DogArray_F32 setTo( float... src ) {
		setTo(src, 0, src.length);
		return this;
	}

	/**
	 * Creates a new primitive array which is a copy.
	 */
	public float[] toArray() {
		float[] out = new float[size];
		System.arraycopy(data,0,out,0,size);
		return out;
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

	/**
	 * Inserts the value at the specified index and shifts all the other values down.
	 */
	public void insert( int index , float value ) {
		if( size == data.length ) {
			float temp[] = new float[ size * 2+5];
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

	/**
	 * Removes the specified index from the array by swapping it with last element. Does not preserve order
	 * but has a runtime of O(1).
	 *
	 * @param index The index to be removed.
	 * @return The removed object
	 */
	public float removeSwap( int index ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		float ret = data[index];
		size -= 1;
		data[index] = data[size];
		return ret;
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

	/**
	 * Returns an element starting from the end of the list. 0 = size -1
	 */
	public float getTail( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[size-index-1];
	}

	/**
	 * Gets the value at the index which corresponds to the specified fraction
	 * @param fraction 0 to 1 inclusive
	 * @return value at fraction
	 */
	public float getFraction( double fraction ) {
		return get( (int)((size-1)*fraction) );
	}

	public float unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index, float value  ) {
		data[index] = value;
	}

	@Override
	public void setTo( DogArray_F32 original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	public void fill( float value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void fill( int idx0, int idx1, float value ) {
		Arrays.fill(data, idx0, idx1, value);
	}

	public boolean contains( float value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return true;
		}
		return false;
	}

	@Override
	public void resize( int size ) {
		if( data.length < size ) {
			data = new float[size];
		}
		this.size = size;
	}

	/**
	 * Resizes the array and assigns the default value to every element.
	 * @param size New size
	 * @param value Default value
	 */
	public void resize( int size , float value ) {
		resize(size);
		fill(value);
	}

	@Override
	public void extend( int size ) {
		if( data.length < size ) {
			float []tmp = new float[size];
			System.arraycopy(data,0,tmp,0,this.size);
			data = tmp;
		}
		this.size = size;
	}

	@Override
	public void reserve(int amount ) {
		if (data.length >= amount)
			return;
		extend(amount-size);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void zero() {
		Arrays.fill(data,0,size,0);
	}

	@Override
	public DogArray_F32 copy() {
		DogArray_F32 ret = new DogArray_F32(size);
		ret.setTo(this);
		return ret;
	}

	@Override
	public void flip() {
		if( size <= 1 )
			return;

		int D = size/2;
		for (int i = 0,j=size-1; i < D; i++,j--) {
			float tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
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

	public int indexOfGreatest() {
		if( size <=- 0 )
			return -1;

		int selected = 0;
		float best = data[0];

		for (int i = 1; i < size; i++) {
			if( data[i] > best ) {
				best = data[i];
				selected = i;
			}
		}

		return selected	;
	}

	public int indexOfLeast() {
		if( size <=- 0 )
			return -1;

		int selected = 0;
		float best = data[0];

		for (int i = 1; i < size; i++) {
			if( data[i] < best ) {
				best = data[i];
				selected = i;
			}
		}

		return selected;
	}

	@Override
	public void sort() {
		Arrays.sort(data,0,size);
	}

	/**
	 * Sort but with a re-usable sorter to avoid declaring new memory
	 */
	public void sort(QuickSort_F32 sorter) {
		sorter.sort(data,size);
	}

	public void forIdx(FunctionEachIdx func) {
		for (int i = 0; i < size; i++) {
			func.process(i,data[i]);
		}
	}

	public void forEach(FunctionEach func) {
		for (int i = 0; i < size; i++) {
			func.process(data[i]);
		}
	}

	public interface FunctionEachIdx {
		void process( int index, float value );
	}

	public interface FunctionEach {
		void process( float value );
	}
}
