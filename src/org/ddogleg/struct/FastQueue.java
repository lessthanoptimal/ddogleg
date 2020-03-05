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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Growable array designed for fast access which creates, recycles and in general owns all of its elements.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("unchecked")
public class FastQueue<T> extends FastAccess<T> {
	// new instances are created using this. If null then no new instances are created automatically.
	private Factory<T> factory;
	// function that's called to reset a returned instance
	private Process<T> reset;

	// Wrapper around this class for lists
	private FastQueueList<T> list = new FastQueueList<T>(this);

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public FastQueue(Class<T> type, Factory<T> factory ) {
		super(type);
		init(10, factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda and determines the class by
	 * creating a new instance.
	 */
	public FastQueue( Factory<T> factory ) {
		super((Class<T>)factory.newInstance().getClass());
		init(10, factory);
	}

	/**
	 * User provided factory function and reset function.
	 * @param factory Creates new instances
	 * @param reset Called whenever an element is recycled and needs to be reset
	 */
	public FastQueue( Factory<T> factory , Process<T> reset ) {
		super((Class<T>)factory.newInstance().getClass());
		this.reset = reset;
		init(10, factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public FastQueue( int initialMaxSize, Factory<T> factory ) {
		super((Class<T>)factory.newInstance().getClass());
		init(initialMaxSize, factory);
	}

	/**
	 * Data structure initialization is done here so that child classes can declay initialization until they are ready
	 */
	protected void init(int initialMaxSize, Factory<T> factory) {
		this.size = 0;
		this.factory = factory;
		if( this.reset == null )
			this.reset = new Process.DoNothing<>();

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
	@Override
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

	public void reset() {
		size = 0;
	}

	/**
	 * Returns a new element of data.  If there are new data elements available then array will
	 * automatically grow.
	 *
	 * @return A new instance.
	 */
	public T grow() {
		if( size < data.length ) {
			T ret = data[size++];
			reset.process(ret);
			return ret;
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
	 * Removes an element from the queue and preserves the order of all elements. This is done by shifting elements
	 * in the array down one and placing the removed element at the old end of the list. O(N) runtime.
	 *
	 * @param index Index of the element being removed
	 * @return The object removed.
	 */
	@Override
	public T remove( int index ) {
		T removed = data[index];
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		data[size-1] = removed;
		size--;
		return removed;
	}

	/**
	 * Removes the specified index from the array by swapping it with last element. Does not preserve order
	 * but has a runtime of O(1).
	 *
	 * @param index The index to be removed.
	 * @return The removed object
	 */
	@Override
	public T removeSwap( int index ) {
		T removed = data[index];
		data[index] = data[size-1];
		data[size-1] = removed;
		size--;
		return removed;
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
		for (int i = size; i < length; i++) {
			reset.process(data[i]);
		}
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

	/**
	 * Checks to see if the object is in the unused list.
	 */
	public boolean isUnused( T object ) {
		final T[] data = this.data;
		for (int i = size; i < data.length; i++) {
			if( data[i] == object )
				return true;
		}
		return false;
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

	public final boolean isDeclare() {
		return factory != null;
	}

	public Class<T> getType() {
		return type;
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
