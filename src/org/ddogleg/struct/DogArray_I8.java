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
 * This is a queue that is composed of bytes.  Elements are added and removed from the tail
 *
 * @author Peter Abeles
 */
public class DogArray_I8 implements DogArrayPrimitive<DogArray_I8> {

	public byte[] data;
	public int size;

	public DogArray_I8( int maxSize) {
		data = new byte[ maxSize ];
		this.size = 0;
	}

	public DogArray_I8() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with all zeros
	 */
	public static DogArray_I8 zeros( int length ) {
		DogArray_I8 out = new DogArray_I8(length);
		out.size = length;
		return out;
	}

	public static DogArray_I8 array( int ...values ) {
		DogArray_I8 out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = (byte)values[i];
		}
		return out;
	}

	/**
	 * Counts the number of times the specified value occures in the list
	 */
	public int count( int value ) {
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
	public boolean isEquals( byte... values ) {
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

	public void addAll( DogArray_I8 queue ) {
		if( size+queue.size > data.length ) {
			byte[] temp = new byte[ (size+queue.size) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(queue.data,0,data,size,queue.size);
		size += queue.size;
	}

	public void addAll( byte[] array , int startIndex , int endIndex ) {
		if( endIndex > array.length )
			throw new IllegalAccessError("endIndex is larger than input array");

		int arraySize = endIndex-startIndex;

		if( size+arraySize > data.length ) {
			byte[] temp = new byte[ (size+arraySize) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(array,startIndex,data,size,arraySize);
		size += arraySize;
	}

	public void add(int value) {
		push(value);
	}

	public void push( int val ) {
		if( size == data.length ) {
			byte[] temp = new byte[ size * 2+5];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = (byte)val;
	}

	public int get( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[index];
	}

	/**
	 * Returns an element starting from the end of the list. 0 = size -1
	 */
	public int getTail( int index ) {
		if( index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = "+index+"  size = "+size);
		return data[size-index-1];
	}

	/**
	 * Gets the value at the index which corresponds to the specified fraction
	 * @param fraction 0 to 1 inclusive
	 * @return value at fraction
	 */
	public int getFraction( double fraction ) {
		return get( (int)((size-1)*fraction) );
	}

	public int unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index , int value ) {
		data[index] = (byte)value;
	}

	@Override
	public void setTo( DogArray_I8 original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	/**
	 * Sets this array to be equal to the array segment
	 * @param array (Input) source array
	 * @param offset first index
	 * @param length number of elements to copy
	 */
	public void setTo( byte[] array , int offset , int length ) {
		resize(length);
		System.arraycopy(array,offset,data,0,length);
	}

	/**
	 * Set's the value of this array to the passed in raw array.
	 * @param src (Input) The input array
	 * @return A reference to "this" to allow chaining of commands
	 */
	public DogArray_I8 setTo( byte... src) {
		setTo(src, 0, src.length);
		return this;
	}

	/**
	 * Creates a new primitive array which is a copy.
	 */
	public byte[] toArray() {
		byte[] out = new byte[size];
		System.arraycopy(data,0,out,0,size);
		return out;
	}

	/**
	 * Inserts the value at the specified index and shifts all the other values down.
	 */
	public void insert( int index , int value ) {
		if( size == data.length ) {
			byte[] temp = new byte[ size * 2+5];
			System.arraycopy(data,0,temp,0,index);
			temp[index] = (byte)value;
			System.arraycopy(data,index,temp,index+1,size-index);
			this.data = temp;
			size++;
		} else {
			size++;
			for( int i = size-1; i > index; i-- ) {
				data[i] = data[i-1];
			}
			data[index] = (byte)value;
		}
	}

	/**
	 * Removes the specified index from the array by swapping it with last element. Does not preserve order
	 * but has a runtime of O(1).
	 *
	 * @param index The index to be removed.
	 * @return The removed object
	 */
	public byte removeSwap( int index ) {
		if( index < 0 || index >= size )
			throw new IllegalArgumentException("Out of bounds. index="+index+" max size "+size);
		byte ret = data[index];
		size -= 1;
		data[index] = data[size];
		return ret;
	}

	public byte removeTail() {
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

	public void fill( byte value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void fill( int idx0, int idx1, byte value ) {
		Arrays.fill(data, idx0, idx1, value);
	}

	public boolean contains( byte value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return true;
		}
		return false;
	}

	@Override
	public void resize( int size ) {
		if( data.length < size ) {
			data = new byte[size];
		}
		this.size = size;
	}

	/**
	 * Resizes the array and assigns the default value to every element.
	 * @param size New size
	 * @param value Default value
	 */
	public void resize( int size , byte value ) {
		resize(size);
		fill(value);
	}

	@Override public void extend( int size ) {
		reserve(size);
		this.size = size;
	}

	@Override public void reserve( int amount ) {
		if (data.length >= amount)
			return;
		byte []tmp = new byte[amount];
		System.arraycopy(data,0,tmp,0,this.size);
		data = tmp;
	}

	@Override
	public int size() {
		return size;
	}

	public int pop() {
		return data[--size];
	}

	@Override
	public void zero() {
		Arrays.fill(data,0,size,(byte)0);
	}

	@Override
	public DogArray_I8 copy() {
		DogArray_I8 ret = new DogArray_I8(size);
		ret.setTo(this);
		return ret;
	}

	@Override
	public void flip() {
		if( size <= 1 )
			return;

		int D = size/2;
		for (int i = 0,j=size-1; i < D; i++,j--) {
			byte tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
	}

	/**
	 * Prints the queue to stdout as a hex array
	 */
	public void printHex() {
		System.out.print("[ ");
		for (int i = 0; i < size; i++) {
			System.out.printf("0x%02X ",data[i]);
		}
		System.out.print("]");
	}

	public static DogArray_I8 parseHex( String message ) {
		message = message.replaceAll("\\[","");
		message = message.replaceAll("\\]","");
		message = message.replaceAll(" ","");

		String[] words = message.split(",");

		DogArray_I8 out = new DogArray_I8(words.length);
		out.size = words.length;

		for (int i = 0; i < words.length; i++) {
			out.data[i] = Integer.decode(words[i]).byteValue();
		}
		return out;
	}

	/**
	 * Returns the index of the first element with the specified 'value'.  return -1 if it wasn't found
	 * @param value Value to search for
	 * @return index or -1 if it's not in the list
	 */
	public int indexOf( byte value ) {
		for (int i = 0; i < size; i++) {
			if( data[i] == value )
				return i;
		}
		return -1;
	}

	@Override
	public void sort() {
		Arrays.sort(data,0,size);
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
		void process( int index, byte value );
	}

	@FunctionalInterface
	public interface FunctionEach {
		void process( byte value );
	}

	@FunctionalInterface
	public interface FunctionApplyIdx {
		byte process( int index, byte value );
	}
}
