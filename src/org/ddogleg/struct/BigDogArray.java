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

import java.lang.reflect.Array;

/**
 * Implementation of {@link BigDogArrayBase} for any Object[].
 *
 * @author Peter Abeles
 */
@SuppressWarnings("unchecked")
public class BigDogArray<T> extends BigDogArrayBase<T[]> {
	/** new instances are created using this. If null then no new instances are created automatically. */
	private @Getter final Factory<T> factory;

	/** function that's called to reset a returned instance */
	private @Getter @Setter DProcess<T> reset;

	/** function that's called to initialize a new instance */
	private @Getter @Setter DProcess<T> initialize = new DProcess.DoNothing<>();

	/** Type of object */
	private @Getter final Class<T> type;

	protected BigDogArray( int initialAllocation,
						   int blockSize,
						   BigDogGrowth growth,
						   Factory<T> factory, DProcess<T> reset ) {
		super(initialAllocation, blockSize, growth,
				new NewObjectArray<T>((Class<T>)factory.newInstance().getClass()),
				( array, startIndex ) -> {
					for (int i = startIndex; i < array.length; i++) {
						array[i] = factory.newInstance();
						reset.process(array[i]);
					}
				});
		this.factory = factory;
		this.reset = reset;
		this.type = (Class<T>)factory.newInstance().getClass();
	}

	protected BigDogArray( Factory<T> factory, DProcess<T> reset ) {
		this(8, DEFAULT_BLOCK_SIZE, BigDogGrowth.GROW_FIRST, factory, reset);
	}

	protected BigDogArray( Factory<T> factory ) {
		this(8, DEFAULT_BLOCK_SIZE, BigDogGrowth.GROW_FIRST, factory, new DProcess.DoNothing<>());
	}

	private static class NewObjectArray<T> implements NewArray<T[]> {
		Class<T> type;

		public NewObjectArray( Class<T> type ) {
			this.type = type;
		}

		@Override public T[] create( int length ) {
			return (T[])Array.newInstance(type, length);
		}
	}

	@Override protected int arrayLength( T[] ts ) {
		return ts.length;
	}

	/** Modify reserve so that it doesn't discard the already allocated objects */
	@Override public void reserve( int desiredSize ) {
		allocate(desiredSize, true, false);
	}

	/**
	 * Adds a new element to the tail and returns it.
	 */
	public T grow() {
		allocate(this.size + 1, true, true);
		this.size++;
		return get(size - 1);
	}

	/**
	 * Removes an element in O(1) time by swapping the specified index with the last index and resizing to size -1.
	 */
	@Override public void removeSwap( int index ) {
		int indexTail = size - 1;

		T target = get(index);
		T tail = get(indexTail);

		blocks.data[indexTail/blockSize][indexTail%blockSize] = target;
		blocks.data[index/blockSize][index%blockSize] = tail;
		size--;
	}

	/**
	 * Resizes the array and fills all new elements with the specified value
	 *
	 * @param desiredSize New array size
	 * @param configure Operator that the "new" element is passed in to.
	 */
	public void resize( int desiredSize, DProcess<T> configure ) {
		allocate(desiredSize, true, false);
		int originalSize = size;
		this.size = desiredSize;
		fill(originalSize, desiredSize, configure);
	}

	/**
	 * Fills the elements in the specified range with the specified value.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) last index, exclusive.
	 * @param configure Operator that the "new" element is passed in to.
	 */
	public void fill( int idx0, int idx1, DProcess<T> configure ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			for (int i = block0; i < block1; i++) {
				configure.process(block[i]);
			}
		});
	}

	/**
	 * Returns the value in the array at the specified index
	 *
	 * @param index (Input) Index in the array
	 * @return value at index
	 */
	public T get( int index ) {
		return blocks.data[index/blockSize][index%blockSize];
	}

	/**
	 * Returns the value in the array at the specified index, counting from the end of the array.
	 *
	 * @param index (Input) Index relative to the end counting in reverse order. 0 = get(size-1)
	 */
	public T getTail( int index ) {
		return get(size - index - 1);
	}

	/**
	 * Simulates a for-each loop. Passes in element values to 'op' from the specified range.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) Last index, exclusive.
	 * @param op The operator which processes the values
	 */
	public void forEach( int idx0, int idx1, DProcess<T> op ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			for (int i = block0; i < block1; i++) {
				op.process(block[i]);
			}
		});
	}

	/**
	 * Simulates a for-each loop. Passes in array indexes and element values to 'op' from the specified range.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) Last index, exclusive.
	 * @param op The operator which processes the values
	 */
	public void forIdx( int idx0, int idx1, DProcessIdx<T> op ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			int index = idx0 + offset;
			for (int i = block0; i < block1; i++) {
				op.process(index++, block[i]);
			}
		});
	}
}
