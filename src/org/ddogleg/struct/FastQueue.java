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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Growable array designed for fast access.  It can be configured to declare new instances
 * or just grow the array.
 *
 * @author Peter Abeles
 */
public class FastQueue<T> implements Serializable {
	public T []data;
	public int size;
	public Class<T> type;

	// new instances are created using this. If null then no new instances are created automatically.
	private Factory<T> factory;

	// Wrapper around this class for lists
	private FastQueueList<T> list = new FastQueueList<T>(this);

	public FastQueue(int initialMaxSize, Class<T> type, boolean declareInstances) {
		init(initialMaxSize, type, declareInstances ? new FactoryClass(type):null);
	}

	public FastQueue(Class<T> type, boolean declareInstances ) {
		init(10, type, declareInstances ? new FactoryClass(type):null);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public FastQueue(Class<T> type, Factory<T> factory ) {
		init(10, type, factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda and determines the class by
	 * creating a new instance.
	 */
	public FastQueue( Factory<T> factory ) {
		T tmp = factory.newInstance();
		init(10, (Class<T>)tmp.getClass(), factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public FastQueue( int initialMaxSize, Class<T> type, Factory<T> factory ) {
		init(initialMaxSize, type, factory);
	}

	protected FastQueue() {
	}

	/**
	 * Data structure initialization is done here so that child classes can declay initialization until they are ready
	 */
	protected void init(int initialMaxSize, Class<T> type, Factory<T> factory) {
		this.size = 0;
		this.type = type;
		this.factory = factory;

		data = (T[]) Array.newInstance(type, initialMaxSize);

		if( factory != null ) {
			for( int i = 0; i < initialMaxSize; i++ ) {
				try {
					data[i] = createInstance();
				} catch( RuntimeException e ) {
					throw new RuntimeException("declareInstances is true, but createInstance() can't create a new instance.  Maybe override createInstance()?");
				}
			}
		}
	}

	/**
	 * Returns a wrapper around FastQueue that allows it to act as a read only list.
	 * There is little overhead in using this interface.
	 *
	 * NOTE: The same instead of a list is returned each time.  Be careful when writing
	 * concurrent code and create a copy.
	 *
	 * @return List wrapper.
	 */
	public List<T> toList() {
		return list;
	}

	/**
	 * Removes the indexes from the queue. This is done by swapping removed elements with the last element. O(N) copies.
	 *
	 * @param indexes Index of elements which are to be removed. This will be modified
	 * @param fromIndex the index of the first element, inclusive, to be sorted
	 * @param toIndex the index of the last element, exclusive, to be sorted
	 * @param workSpace Optional internal workspace. Can be set to null.
	 */
	public void remove( int[] indexes , int fromIndex, int toIndex, @Nullable List<T> workSpace ) {
		if( toIndex <= fromIndex )
			return;
		if( workSpace == null ) {
			workSpace = new ArrayList<>();
		} else {
			workSpace.clear();
		}
		// sort indexes from lowest to highest
		Arrays.sort(indexes,fromIndex,toIndex);

		// the next index wihch should be skipped
		int target = indexes[fromIndex];
		// how many indexes have been removed so far
		int count = 0;
		for ( int i = indexes[fromIndex]; i < size; i++ ) {
			if( i == target ) {
				workSpace.add( data[i] );
				count++;
				if( count < toIndex-fromIndex ) {
					target = indexes[fromIndex+count];
				} else {
					target = -1;
				}
			} else {
				data[i-count] = data[i];
			}
		}

		// push removed objects to the end
		for (int i = 0; i < workSpace.size(); i++) {
			data[size-i-1] = workSpace.get(i);
		}
		size -= workSpace.size();
	}

	/**
	 * Shrinks the size of the array by one and returns the element stored at the former last element.
	 *
	 * @return The last element in the list that was removed.
	 */
	public T removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else
			throw new IllegalArgumentException("Size is already zero");
	}

	public T getTail() {
		return data[size-1];
	}

	/**
	 * Returns an element in the list relative to the tail
	 * @param index index relative to tail.  0 == the tail. size-1 = first element
	 * @return element
	 */
	public T getTail( int index ) {
		return data[size-1-index];
	}

	public void reset() {
		size = 0;
	}

	/**
	 * The maximum number of elements before the 'data' array needs to grow
	 * @return length of 'data'
	 */
	public int getMaxSize() {
		return data.length;
	}

	/**
	 * Number of elements
	 */
	public int size() {
		return size;
	}

	/**
	 * Reverse the item order in this queue.
	 */
	public void reverse() {
		for (int i = 0; i < size / 2; i++) {
			T tmp = data[i];
			data[i] = data[size - i - 1];
			data[size - i - 1] = tmp;
		}
	}

	/**
	 * Returns the element at the specified index.  Bounds checking is performed.
	 * @param index Index of the element being retrieved
	 * @return The retrieved element
	 */
	public T get( int index ) {
		if( index >= size )
			throw new IllegalArgumentException("Index out of bounds: index "+index+" size "+size);
		return data[index];
	}

	/**
	 * Returns a new element of data.  If there are new data elements available then array will
	 * automatically grow.
	 *
	 * @return A new instance.
	 */
	public T grow() {
		if( size < data.length ) {
			return data[size++];
		} else {
			growArray((data.length+1)*2);
			return data[size++];
		}
	}

	/**
	 * Grows the array and adds all the items in list. Values are copied using the provided function
	 */
	public void copyAll(List<T> list , Set<T> setter ) {
		growArray(size()+list.size());
		for (int i = 0; i < list.size(); i++) {
			T dst = grow();
			setter.set(list.get(i),dst);
		}
	}

	/**
	 * Removes an element from the queue by shifting elements in the array down one and placing the removed element
	 * at the old end of the list.
	 *
	 * @param index Index of the element being removed
	 */
	public void remove( int index ) { // TODO return the removed object. Wait until Geo is updated
		T removed = data[index];
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		data[size-1] = removed;
		size--;
	}

	public void add( T object ) {
		if( size >= data.length ) {
			growArray((size+1)*2);
		}
		data[size++] = object;
	}

	public void addAll( FastQueue<T> list ) {
		for( int i = 0; i < list.size; i++ ) {
			add( list.data[i]);
		}
	}

	public void add( T[] array , int first, int length ) {
		for( int i = 0; i < length; i++ ) {
			add( array[first+i]);
		}
	}

	/**
	 * Increases the size of the internal array without changing the shape's size. If the array
	 * is already larger than the specified length then nothing is done.  Elements previously
	 * stored in the array are copied over is a new internal array is declared.
	 *
	 * @param length Requested size of internal array.
	 */
	public void growArray( int length) {
		// now need to grow since it is already larger
		if( this.data.length >= length)
			return;

		T []data = (T[])Array.newInstance(type, length);
		System.arraycopy(this.data,0,data,0,this.data.length);

		if( factory != null ) {
			for( int i = this.data.length; i < length; i++ ) {
				data[i] = createInstance();
			}
		}
		this.data = data;
	}

	/**
	 * Changes the size to the specified length. Equivalent to calling {@link #growArray} and this.size = N.
	 * @param length The new size of the queue
	 */
	public void resize(int length) {
		growArray(length);
		this.size = length;
	}

	public boolean contains(Object o) {
		for( int i = 0; i < size; i++ ) {
			if( data[i].equals(o) )
				return true;
		}

		return false;
	}

	public void shuffle( Random rand ) {
		for (int i = 0; i < size; i++) {
			int selected = rand.nextInt(size-i);
			T tmp = data[selected];
			data[selected] = data[size-i-1];
			data[size-i-1] = tmp;
		}
	}

	/**
	 * This function will be removed eventually and the factory used directly. DO NOT USE IN NEW CODE
	 */
	@Deprecated
	protected T createInstance() {
		return factory.newInstance();
	}

	public List<T> copyIntoList(List<T> ret) {
		if( ret == null )
			ret = new ArrayList<T>(size);
		for( int i = 0; i < size; i++ ) {
			ret.add(data[i]);
		}
		return ret;
	}

	/**
	 * Returns the first index which equals() obj. -1 is there is no match
	 *
	 * @param obj The object being searched for
	 * @return index or -1 if not found
	 */
	public int indexOf( T obj ) {
		for (int i = 0; i < size; i++) {
			if( data[i].equals(obj) ) {
				return i;
			}
		}
		return -1;
	}

	public void flip() {
		if( size <= 1 )
			return;

		int D = size/2;
		for (int i = 0,j=size-1; i < D; i++,j--) {
			T tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
	}

	public void swap( int idx0 , int idx1 ) {
		T tmp = data[idx0];
		data[idx0] = data[idx1];
		data[idx1] = tmp;
	}

	// -------- These are only around so that it can be a java bean
	public T[] getData() {
		return data;
	}

	public void setData(T[] data) {
		this.data = data;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isDeclareInstances() {
		return factory != null;
	}

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}

	public interface Factory<T> {
		T newInstance();
	}

	public interface Set<T> {
		void set( T src , T dst );
	}

	public class FactoryClass implements Factory<T> {
		Class<T> type;

		public FactoryClass(Class<T> type) {
			this.type = type;
		}

		@Override
		public T newInstance() {
			try {
				return type.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
