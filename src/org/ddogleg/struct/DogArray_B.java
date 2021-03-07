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
 * Growable array composed of booleans.
 *
 * @author Peter Abeles
 */
public class DogArray_B implements DogArrayPrimitive<DogArray_B> {

	public boolean[] data;
	public int size;

	public DogArray_B( int maxSize ) {
		data = new boolean[ maxSize ];
		this.size = 0;
	}

	public DogArray_B() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with false
	 */
	public static DogArray_B zeros( int length ) {
		DogArray_B out = new DogArray_B(length);
		out.size = length;
		return out;
	}

	public static DogArray_B array( boolean ...values ) {
		DogArray_B out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i];
		}
		return out;
	}

	/**
	 * Non-zero values are set to true
	 */
	public static DogArray_B array( int ...values ) {
		DogArray_B out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i] != 0;
		}
		return out;
	}

	/**
	 * Counts the number of times the specified value occurs in the list
	 */
	public int count( boolean value ) {
		int total = 0;
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				total++;
		}
		return total;
	}

	/**
	 * Sees is the primitive array is equal to the values in this array
	 *
	 * @param values primitive array
	 * @return true if equal or false if not
	 */
	public boolean isEquals( int... values ) {
		if (size!=values.length)
			return false;
		for (int i = 0; i < size; i++) {
			boolean v = values[i]!=0;
			if (data[i] != v)
				return false;
		}
		return true;
	}

	public boolean isEquals( boolean... values ) {
		if (size!=values.length)
			return false;
		for (int i = 0; i < size; i++) {
			if (data[i] != values[i])
				return false;
		}
		return true;
	}

	@Override
	public void reset() {
		size = 0;
	}

	public void addAll( DogArray_B queue ) {
		if( size+queue.size > data.length ) {
			boolean[] temp = new boolean[ (size+queue.size) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(queue.data,0,data,size,queue.size);
		size += queue.size;
	}

	public void addAll( boolean[] array , int startIndex , int endIndex ) {
		if( endIndex > array.length )
			throw new IllegalAccessError("endIndex is larger than input array. "+endIndex+" > "+array.length);

		int arraySize = endIndex-startIndex;

		if( size+arraySize > data.length ) {
			boolean[] temp = new boolean[ (size+arraySize) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(array,startIndex,data,size,arraySize);
		size += arraySize;
	}

	public void add( boolean val ) {
		push(val);
	}

	public void push( boolean val ) {
		if( size == data.length ) {
			boolean[] temp;
			try {
				temp = new boolean[ size * 2+5];
			} catch( OutOfMemoryError e ) {
				System.gc();
				temp = new boolean[ 3*size/2];
			}
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = val;
	}

	/**
	 * Sets this array to be equal to the array segment
	 * @param array (Input) source array
	 * @param offset first index
	 * @param length number of elements to copy
	 */
	public void setTo( boolean[] array , int offset , int length ) {
		resize(length);
		System.arraycopy(array,offset,data,0,length);
	}

	/**
	 * Set's the value of this array to the passed in raw array.
	 * @param src (Input) The input array
	 * @return A reference to "this" to allow chaining of commands
	 */
	public DogArray_B setTo( boolean... src) {
		setTo(src, 0, src.length);
		return this;
	}

	/**
	 * Creates a new primitive array which is a copy.
	 */
	public boolean[] toArray() {
		boolean[] out = new boolean[size];
		System.arraycopy(data,0,out,0,size);
		return out;
	}

	public void remove( int index ) {
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		size--;
	}

	/**
	 * Removes elements from the list starting at 'first' and ending at 'last'
	 * @param first First index you wish to remove. Inclusive.
	 * @param last Last index you wish to remove. Inclusive.
	 */
	public void remove( int first , int last ) {
		if( last < first )
			throw new IllegalArgumentException("first <= last. first="+first+" last="+last );
		if( last >= size )
			throw new IllegalArgumentException("last must be less than the max size. last="+last+" size="+size);

		int delta = last-first+1;
		for( int i = last+1; i < size; i++ ) {
			data[i-delta] = data[i];
		}
		size -= delta;
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

	/**
	 * Removes the specified index from the array by swapping it with last element. Does not preserve order
	 * but has a runtime of O(1).
	 *
	 * @param index The index to be removed.
	 * @return The removed object
	 */
	public boolean removeSwap( int index ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		boolean ret = data[index];
		size -= 1;
		data[index] = data[size];
		return ret;
	}

	public boolean removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else {
			throw new RuntimeException("Size zero, no tail");
		}
	}

	public boolean get( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[index];
	}

	public boolean getTail() {
		if (size==0)
			throw new IndexOutOfBoundsException("Array is empty");
		return data[size-1];
	}

	/**
	 * Returns an element starting from the end of the list. 0 = size -1
	 */
	public boolean getTail( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[size-index-1];
	}

	public void setTail( int index, boolean value ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		data[size-index-1] = value;
	}

	public boolean unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index , boolean value ) {
		data[index] = value;
	}

	@Override public void setTo( DogArray_B original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	@Override public void resize( int size ) {
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

	public void fill( boolean value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void fill( int idx0, int idx1, boolean value ) {
		Arrays.fill(data, idx0, idx1, value);
	}

	public boolean contains( boolean value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return true;
		}
		return false;
	}

	@Override public void extend( int size ) {
		reserve(size);
		this.size = size;
	}

	@Override public void reserve( int amount ) {
		if (data.length >= amount)
			return;
		boolean []tmp = new boolean[amount];
		System.arraycopy(data,0,tmp,0,this.size);
		data = tmp;
	}

	@Override public int size() {
		return size;
	}

	@Override public void zero() {
		Arrays.fill(data,0,size,false);
	}

	@Override public DogArray_B copy() {
		var ret = new DogArray_B(size);
		ret.setTo(this);
		return ret;
	}

	@Override public void flip() {
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

	@Override public void sort() {
		throw new RuntimeException("Undefined for boolean");
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

	public void applyIdx(FunctionApplyIdx func) {
		for (int i = 0; i < size; i++) {
			data[i] = func.process(i, data[i]);
		}
	}

	@FunctionalInterface
	public interface FunctionEachIdx {
		void process( int index, boolean value );
	}

	@FunctionalInterface
	public interface FunctionEach {
		void process( boolean value );
	}

	@FunctionalInterface
	public interface FunctionApplyIdx {
		boolean process( int index, boolean value );
	}
}
