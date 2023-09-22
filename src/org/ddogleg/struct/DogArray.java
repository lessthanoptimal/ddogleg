/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Growable array which automatically creates, recycles, and resets its elements. Access to internal variables
 * is provided for high performant code. If a reset function is provided then when an object is created or recycled
 * the reset function is called with the objective of giving the object a repeatable initial state.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked", "NullAway.Init", "ForLoopReplaceableByForEach", "ManualArrayToCollectionCopy"})
public class DogArray<T> extends FastAccess<T> {
	/** new instances are created using this. If null then no new instances are created automatically. */
	private @Getter Factory<T> factory;
	/** function that's called to reset a returned instance */
	private @Getter @Setter DProcess<T> reset;
	/** function that's called to initialize a new instance */
	private @Getter @Setter DProcess<T> initialize = new DProcess.DoNothing<>();

	// Wrapper around this class for lists
	private final DogArrayList<T> list = new DogArrayList<>(this);

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public DogArray( Class<T> type, Factory<T> factory ) {
		super(type);
		init(10, factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda and determines the class by
	 * creating a new instance.
	 */
	public DogArray( Factory<T> factory ) {
		super((Class<T>)factory.newInstance().getClass());
		init(10, factory);
	}

	/**
	 * User provided factory function and reset function.
	 *
	 * @param factory Creates new instances
	 * @param reset Called whenever an element is recycled and needs to be reset
	 */
	public DogArray( Factory<T> factory, DProcess<T> reset ) {
		super((Class<T>)factory.newInstance().getClass());
		this.reset = reset;
		init(10, factory);
	}

	/**
	 * User provided factory function and reset function.
	 *
	 * @param factory Creates new instances
	 * @param reset Called whenever an element is recycled and needs to be reset
	 * @param initialize Called after a new instance is created
	 */
	public DogArray( Factory<T> factory, DProcess<T> reset, DProcess<T> initialize ) {
		super((Class<T>)factory.newInstance().getClass());
		this.reset = reset;
		this.initialize = initialize;
		init(10, factory);
	}

	/**
	 * Constructor which allows new instances to be created using a lambda
	 */
	public DogArray( int initialMaxSize, Factory<T> factory ) {
		super((Class<T>)factory.newInstance().getClass());
		init(initialMaxSize, factory);
	}

	/**
	 * Data structure initialization is done here so that child classes can declay initialization until they are ready
	 */
	protected void init( int initialMaxSize, Factory<T> factory ) {
		this.size = 0;
		this.factory = factory;
		if (this.reset == null)
			this.reset = new DProcess.DoNothing<>();

		data = (T[])Array.newInstance(type, initialMaxSize);

		if (factory != null) {
			for (int i = 0; i < initialMaxSize; i++) {
				try {
					data[i] = createInstance();
				} catch (RuntimeException e) {
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
	 * Removes the indexes from the array. This is done by swapping removed elements with the last element. O(N) copies.
	 *
	 * @param indexes Index of elements which are to be removed. This will be modified
	 * @param fromIndex the index of the first element, inclusive, to be sorted
	 * @param toIndex the index of the last element, exclusive, to be sorted
	 * @param workSpace Optional internal workspace. Can be set to null.
	 */
	public void remove( int[] indexes, int fromIndex, int toIndex, @Nullable List<T> workSpace ) {
		if (toIndex <= fromIndex)
			return;
		if (workSpace == null) {
			workSpace = new ArrayList<>();
		} else {
			workSpace.clear();
		}
		// sort indexes from lowest to highest
		Arrays.sort(indexes, fromIndex, toIndex);

		// the next index wihch should be skipped
		int target = indexes[fromIndex];
		// how many indexes have been removed so far
		int count = 0;
		for (int i = indexes[fromIndex]; i < size; i++) {
			if (i == target) {
				workSpace.add(data[i]);
				count++;
				if (count < toIndex - fromIndex) {
					target = indexes[fromIndex + count];
				} else {
					target = -1;
				}
			} else {
				data[i - count] = data[i];
			}
		}

		// push removed objects to the end
		for (int i = 0; i < workSpace.size(); i++) {
			data[size - i - 1] = workSpace.get(i);
		}
		size -= workSpace.size();
	}

	/**
	 * Shrinks the size of the array by one and returns the element stored at the former last element.
	 *
	 * @return The last element in the list that was removed.
	 */
	public T removeTail() {
		if (size > 0) {
			size--;
			return data[size];
		} else
			throw new IllegalArgumentException("Size is already zero");
	}

	public DogArray<T> reset() {
		size = 0;
		return this;
	}

	/**
	 * Returns a new element of data.  If there are new data elements available then array will
	 * automatically grow.
	 *
	 * @return A new instance.
	 */
	public T grow() {
		if (size < data.length) {
			T ret = data[size++];
			reset.process(ret);
			return ret;
		} else {
			reserve((data.length + 1)*2);
			return data[size++];
		}
	}

	/**
	 * Grows the array and adds all the items in list. Values are copied using the provided function
	 */
	public <S> void copyAll( List<S> list, Set<S, T> setter ) {
		reserve(size() + list.size());
		for (int i = 0; i < list.size(); i++) {
			T dst = grow();
			setter.set(list.get(i), dst);
		}
	}

	/**
	 * Removes an element from the array and preserves the order of all elements. This is done by shifting elements
	 * in the array down one and placing the removed element at the old end of the list. O(N) runtime.
	 *
	 * @param index Index of the element being removed
	 * @return The object removed.
	 */
	@Override
	public T remove( int index ) {
		T removed = data[index];
		for (int i = index + 1; i < size; i++) {
			data[i - 1] = data[i];
		}
		data[size - 1] = removed;
		size--;
		return removed;
	}

	/**
	 * Searches for and removes the 'target' from the list. Returns true if the target was found. If false
	 * then the target was never found and no change has been made. This is an O(N) operation.
	 *
	 * @param target Object to remove from the list
	 * @return true if the target was found and removed
	 */
	public boolean remove( T target ) {
		int index = indexOf(target);
		if (index < 0)
			return false;
		remove(index);
		return true;
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
		data[index] = data[size - 1];
		data[size - 1] = removed;
		size--;
		return removed;
	}

	/**
	 * Ensures that the internal array has at least `length` elements. If it does not then a new internal array
	 * is created with the specified length and elements from the old are copied into the new. The `size` does
	 * not change.
	 *
	 * @param length Requested minimum internal array length
	 */
	public void reserve( int length ) {
		// now need to grow since it is already larger
		if (this.data.length >= length)
			return;

		T[] data = (T[])Array.newInstance(type, length);
		System.arraycopy(this.data, 0, data, 0, this.data.length);

		if (factory != null) {
			for (int i = this.data.length; i < length; i++) {
				data[i] = createInstance();
			}
		}
		this.data = data;
	}

	/**
	 * Ensures that the reserve is at lease the current {@link #size} plus the specified amount. This is
	 * exactly the same as doing `reserve(size + amount)`
	 *
	 * @param amount How much you wish the ensure the size is increased by
	 */
	public void reserveIncrease( int amount ) {
		reserve(size + amount);
	}

	/**
	 * Resize with a configuration operator. Equivalent to calling {@link #reserve} and this.size = N, then
	 * applying the 'configure' operator to each new element.
	 *
	 * NOTE: The 'reset' operator is applied before the 'configure' operator.
	 *
	 * @param length The new size of the array
	 * @param configure Operator that the "new" element is passed in to.
	 */
	public DogArray<T> resize( int length, DProcess<T> configure ) {
		reserve(length);
		for (int i = size; i < length; i++) {
			reset.process(data[i]);
			configure.process(data[i]);
		}
		this.size = length;
		return this;
	}

	/**
	 * Resize with a configuration operator. Equivalent to calling {@link #reserve} and this.size = N, then
	 * applying the 'configure' operator to each new element.
	 *
	 * NOTE: The 'reset' operator is applied before the 'configure' operator.
	 *
	 * @param length The new size of the array
	 * @param configure Operator that the "new" element is passed in to along with the index of the element.
	 */
	public DogArray<T> resize( int length, DProcessIdx<T> configure ) {
		reserve(length);
		for (int i = size; i < length; i++) {
			reset.process(data[i]);
			configure.process(i, data[i]);
		}
		this.size = length;
		return this;
	}

	/**
	 * Changes the size to the specified length. Equivalent to calling {@link #reserve} and this.size = N.
	 *
	 * All new elements will be passed in to {@link #reset}.
	 *
	 * @param newSize New array size
	 */
	public DogArray<T> resize( int newSize ) {
		reserve(newSize);
		for (int i = size; i < newSize; i++) {
			reset.process(data[i]);
		}
		this.size = newSize;
		return this;
	}

	/**
	 * Convenience functions that calls {@link #reset} first before {@link #resize}.
	 *
	 * @param newSize New array size
	 */
	@Deprecated
	public void resetResize( int newSize ) {
		reset();
		resize(newSize);
	}

	/**
	 * Convenience functions that calls {@link #reset} first before {@link #resize}, then applies the
	 * configure function for each element..
	 *
	 * @param newSize New array size
	 * @param configure Operator that the "new" element is passed in to along with the index of the element.
	 */
	@Deprecated
	public void resetResize( int newSize, DProcessIdx<T> configure ) {
		reset();
		resize(newSize, configure);
	}

	/**
	 * Randomly shuffles elements in the list. O(N) complexity.
	 *
	 * @param rand random seed.
	 */
	public void shuffle( Random rand ) {
		shuffle(rand, size);
	}

	/**
	 * Shuffle where it will only shuffle up to the specified number of elements. This is useful
	 * when you want to randomly select up to N elements in the list. When shuffling, The first
	 * i < N elements is randomly selected out from an element from i+1 to N-1.
	 *
	 * @param numShuffle The maximum number of elements that will be shuffled
	 * @param rand random seed.
	 */
	public void shuffle( Random rand, int numShuffle ) {
		int N = Math.min(numShuffle, size);
		for (int i = 0; i < N; i++) {
			int selected = rand.nextInt(size - i);
			T tmp = data[selected];
			data[selected] = data[size - i - 1];
			data[size - i - 1] = tmp;
		}
	}

	/**
	 * Creates a new instance of elements stored in this array
	 */
	protected T createInstance() {
		T instance = factory.newInstance();
		initialize.process(instance);
		reset.process(instance);
		return instance;
	}

	public List<T> copyIntoList( List<T> ret ) {
		if (ret == null)
			ret = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			ret.add(data[i]);
		}
		return ret;
	}

	/**
	 * Checks to see if the object is in the unused list.
	 */
	public boolean isUnused( T object ) {
		final T[] data = this.data;
		for (int i = size; i < data.length; i++) {
			if (data[i] == object)
				return true;
		}
		return false;
	}

	// -------- These are only around so that it can be a java bean
	public T[] getData() {
		return data;
	}

	public void setData( T[] data ) {
		this.data = data;
	}

	public int getSize() {
		return size;
	}

	public void setSize( int size ) {
		this.size = size;
	}

	public final boolean isDeclare() {
		return factory != null;
	}

	public Class<T> getType() {
		return type;
	}

	public interface Set<S, D> {
		void set( S src, D dst );
	}
}
