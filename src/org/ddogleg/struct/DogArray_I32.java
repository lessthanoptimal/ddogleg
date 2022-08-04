/*
 * Copyright (c) 2012-2022, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.sorting.QuickSort_S32;

import java.util.Arrays;
import java.util.Random;

/**
 * Growable array composed of ints.
 *
 * @author Peter Abeles
 */
public class DogArray_I32 implements DogArrayPrimitive<DogArray_I32> {

	public int[] data;
	public int size;

	public DogArray_I32( int reserve ) {
		data = new int[reserve];
		this.size = 0;
	}

	public DogArray_I32() {
		this(10);
	}

	/**
	 * Creates a queue with the specified length as its size filled with all zeros
	 */
	public static DogArray_I32 zeros( int length ) {
		var out = new DogArray_I32(length);
		out.size = length;
		return out;
	}

	public static DogArray_I32 array( int... values ) {
		DogArray_I32 out = zeros(values.length);
		for (int i = 0; i < values.length; i++) {
			out.data[i] = values[i];
		}
		return out;
	}

	/**
	 * Returns a new array with values containing range of integer numbers from idx0 to idx1-1.
	 *
	 * @param idx0 Lower extent, inclusive.
	 * @param idx1 Upper extent, exclusive.
	 * @return new array.
	 */
	public static DogArray_I32 range( int idx0, int idx1 ) {
		DogArray_I32 out = zeros(idx1 - idx0);
		for (int i = idx0; i < idx1; i++) {
			out.data[i - idx0] = (int)i;
		}
		return out;
	}

	/**
	 * Counts the number of times the specified value occurs in the list
	 */
	public int count( int value ) {
		int total = 0;
		for (int i = 0; i < size; i++) {
			if (data[i] == value)
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
		if (size != values.length)
			return false;
		for (int i = 0; i < size; i++) {
			if (data[i] != values[i])
				return false;
		}
		return true;
	}

	public boolean isEquals( DogArray_I32 values ) {
		if (size != values.size)
			return false;
		for (int i = 0; i < size; i++) {
			if (data[i] != values.data[i])
				return false;
		}
		return true;
	}

	@Override
	public void reset() {
		size = 0;
	}

	public void addAll( DogArray_I32 queue ) {
		if (size + queue.size > data.length) {
			int[] temp = new int[(size + queue.size)*2];
			System.arraycopy(data, 0, temp, 0, size);
			data = temp;
		}
		System.arraycopy(queue.data, 0, data, size, queue.size);
		size += queue.size;
	}

	public void addAll( int[] array, int startIndex, int endIndex ) {
		if (endIndex > array.length)
			throw new IllegalAccessError("endIndex is larger than input array. " + endIndex + " > " + array.length);

		int arraySize = endIndex - startIndex;

		if (size + arraySize > data.length) {
			int[] temp = new int[(size + arraySize)*2];
			System.arraycopy(data, 0, temp, 0, size);
			data = temp;
		}
		System.arraycopy(array, startIndex, data, size, arraySize);
		size += arraySize;
	}

	public void add( int val ) {
		push(val);
	}

	public void push( int val ) {
		if (size == data.length) {
			int[] temp;
			try {
				temp = new int[size*2 + 5];
			} catch (OutOfMemoryError e) {
				System.gc();
				temp = new int[3*size/2];
			}
			System.arraycopy(data, 0, temp, 0, size);
			data = temp;
		}
		data[size++] = (int)val;
	}

	/**
	 * Sets this array to be equal to the array segment
	 *
	 * @param array (Input) source array
	 * @param offset first index
	 * @param length number of elements to copy
	 */
	public void setTo( int[] array, int offset, int length ) {
		resize(length);
		System.arraycopy(array, offset, data, 0, length);
	}

	/**
	 * Set's the value of this array to the passed in raw array.
	 *
	 * @param src (Input) The input array
	 * @return A reference to "this" to allow chaining of commands
	 */
	public DogArray_I32 setTo( int... src ) {
		setTo(src, 0, src.length);
		return this;
	}

	/**
	 * Creates a new primitive array which is a copy.
	 */
	public int[] toArray() {
		int[] out = new int[size];
		System.arraycopy(data, 0, out, 0, size);
		return out;
	}

	public void remove( int index ) {
		for (int i = index + 1; i < size; i++) {
			data[i - 1] = data[i];
		}
		size--;
	}

	/**
	 * Removes elements from the list starting at 'first' and ending at 'last'
	 *
	 * @param first First index you wish to remove. Inclusive.
	 * @param last Last index you wish to remove. Inclusive.
	 */
	public void remove( int first, int last ) {
		if (last < first)
			throw new IllegalArgumentException("first <= last. first=" + first + " last=" + last);
		if (last >= size)
			throw new IllegalArgumentException("last must be less than the max size. last=" + last + " size=" + size);

		int delta = last - first + 1;
		for (int i = last + 1; i < size; i++) {
			data[i - delta] = data[i];
		}
		size -= delta;
	}

	/**
	 * Inserts the value at the specified index and shifts all the other values down.
	 */
	public void insert( int index, int value ) {
		if (size == data.length) {
			int[] temp = new int[size*2 + 5];
			System.arraycopy(data, 0, temp, 0, index);
			temp[index] = value;
			System.arraycopy(data, index, temp, index + 1, size - index);
			this.data = temp;
			size++;
		} else {
			size++;
			for (int i = size - 1; i > index; i--) {
				data[i] = data[i - 1];
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
	public int removeSwap( int index ) {
		if (index < 0 || index >= size)
			throw new IllegalArgumentException("Out of bounds. index=" + index + " max size " + size);
		int ret = data[index];
		size -= 1;
		data[index] = data[size];
		return ret;
	}

	public int removeTail() {
		if (size > 0) {
			size--;
			return data[size];
		} else {
			throw new RuntimeException("Size zero, no tail");
		}
	}

	public int get( int index ) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = " + index + "  size = " + size);
		return data[index];
	}

	public int getTail() {
		if (size == 0)
			throw new IndexOutOfBoundsException("Array is empty");
		return data[size - 1];
	}

	/**
	 * Returns an element starting from the end of the list. 0 = size -1
	 */
	public int getTail( int index ) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = " + index + "  size = " + size);
		return data[size - index - 1];
	}

	public void setTail( int index, int value ) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = " + index + "  size = " + size);
		data[size - index - 1] = value;
	}

	/**
	 * Gets the value at the index which corresponds to the specified fraction
	 *
	 * @param fraction 0 to 1 inclusive
	 * @return value at fraction
	 */
	public int getFraction( double fraction ) {
		return get((int)((size - 1)*fraction));
	}

	public int unsafe_get( int index ) {
		return data[index];
	}

	public void set( int index, int value ) {
		data[index] = value;
	}

	@Override public void setTo( DogArray_I32 original ) {
		resize(original.size);
		System.arraycopy(original.data, 0, data, 0, size());
	}

	@Override public void resize( int size ) {
		reserve(size);
		this.size = size;
	}

	/**
	 * Resizes the array and assigns the default value to every new element.
	 *
	 * @param size New size
	 * @param value Default value
	 */
	public void resize( int size, int value ) {
		int priorSize = this.size;
		resize(size);
		if (priorSize >= size )
			return;
		fill(priorSize, size, value);
	}

	/**
	 * Convenience function that will first call {@link #reset} then {@link #resize(int, int)}, ensuring
	 * that every element in the array will have the specified value
	 *
	 * @param size New size
	 * @param value New value of every element
	 */
	public void resetResize( int size, int value ) {
		reset();
		resize(size, value);
	}

	/**
	 * Resizes and assigns the new elements (if any) to the value specified by the lambda
	 *
	 * @param size New sie
	 * @param op Assigns default values
	 */
	public void resize( int size, DogLambdas.AssignIdx_I32 op ) {
		int priorSize = this.size;
		resize(size);
		for (int i = priorSize; i < size; i++) {
			data[i] = op.assign(i);
		}
	}

	public void fill( int value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void fill( int idx0, int idx1, int value ) {
		Arrays.fill(data, idx0, idx1, value);
	}

	public boolean contains( int value ) {
		for (int i = 0; i < size; i++) {
			if (data[i] == value)
				return true;
		}
		return false;
	}

	@Override public void extend( int size ) {
		reserve(size);
		this.size = size;
	}

	@SuppressWarnings("NullAway")
	@Override public void reserve( int amount ) {
		if (data.length >= amount)
			return;
		if (size == 0) {
			// In this special case we can dereference the old array and this might allow the GC to free up memory
			// before declaring the new array. Could be useful if the arrays are very large.
			this.data = null;
			this.data = new int[amount];
		} else {
			var tmp = new int[amount];
			System.arraycopy(data, 0, tmp, 0, this.size);
			data = tmp;
		}
	}

	@Override public int size() {
		return size;
	}

	@Override public void zero() {
		Arrays.fill(data, 0, size, (int)0);
	}

	@Override public DogArray_I32 copy() {
		var ret = new DogArray_I32(size);
		ret.setTo(this);
		return ret;
	}

	@Override public void flip() {
		if (size <= 1)
			return;

		int D = size/2;
		for (int i = 0, j = size - 1; i < D; i++, j--) {
			int tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
	}

	public static DogArray_I32 parseHex( String message ) {
		message = message.replaceAll("\\[","");
		message = message.replaceAll("\\]","");
		message = message.replaceAll(" ","");

		String[] words = message.split(",");

		var out = new DogArray_I32(words.length);
		out.size = words.length;

		for (int i = 0; i < words.length; i++) {
			out.data[i] = Integer.decode(words[i]).byteValue();
		}
		return out;
	}

	public int pop() {
		return data[--size];
	}

	/**
	 * Returns the index of the first element with the specified 'value'.  return -1 if it wasn't found
	 *
	 * @param value Value to search for
	 * @return index or -1 if it's not in the list
	 */
	public int indexOf( int value ) {
		for (int i = 0; i < size; i++) {
			if (data[i] == value)
				return i;
		}
		return -1;
	}

	public int indexOfGreatest() {
		if (size <= -0)
			return -1;

		int selected = 0;
		int best = data[0];

		for (int i = 1; i < size; i++) {
			if (data[i] > best) {
				best = data[i];
				selected = i;
			}
		}

		return selected;
	}

	public int indexOfLeast() {
		if (size <= -0)
			return -1;

		int selected = 0;
		int best = data[0];

		for (int i = 1; i < size; i++) {
			if (data[i] < best) {
				best = data[i];
				selected = i;
			}
		}

		return selected;
	}

	@Override public void sort() {
		Arrays.sort(data, 0, size);
	}

	/**
	 * Sort but with a re-usable sorter to avoid declaring new memory
	 */
	public void sort( QuickSort_S32 sorter) {
		sorter.sort(data,size);
	}

	/** Shuffle elements by randomly swapping them */
	public void shuffle( Random rand ) {
		for (int i = 0; i < size; i++) {
			int src = rand.nextInt(size-i);
			int tmp = data[i];
			data[i] = data[src];
			data[src] = tmp;
		}
	}

	public void forIdx( FunctionEachIdx func ) {
		for (int i = 0; i < size; i++) {
			func.process(i, data[i]);
		}
	}

	public void forEach( FunctionEach func ) {
		for (int i = 0; i < size; i++) {
			func.process(data[i]);
		}
	}

	public void applyIdx( FunctionApplyIdx func ) {
		for (int i = 0; i < size; i++) {
			data[i] = func.process(i, data[i]);
		}
	}

	public int count( Filter filter ) {
		int total = 0;
		for (int i = 0; i < size; i++) {
			if (filter.include(data[i]))
				total++;
		}
		return total;
	}

	@FunctionalInterface
	public interface FunctionEachIdx {
		void process( int index, int value );
	}

	@FunctionalInterface
	public interface FunctionEach {
		void process( int value );
	}

	@FunctionalInterface
	public interface FunctionApplyIdx {
		int process( int index, int value );
	}

	@FunctionalInterface
	public interface Filter {
		boolean include( int value );
	}
}
