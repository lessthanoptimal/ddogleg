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
 * Implementation of {@link BigDogArray} for byte[].
 *
 * @author Peter Abeles
 */
public class BigDogArray_I8 extends BigDogArray<byte[]> {

	// WARNING: BigDogArray_I8 is a template for all the other BigDogArray classes. Only modify BigDogArray_I8.

	public BigDogArray_I8() {
		this(8, DEFAULT_BLOCK_SIZE, Growth.GROW_FIRST);
	}

	public BigDogArray_I8( int initialAllocation ) {
		this(initialAllocation, DEFAULT_BLOCK_SIZE, Growth.GROW_FIRST);
	}

	public BigDogArray_I8( int initialAllocation, int blockSize, Growth growth ) {
		super(initialAllocation, blockSize, growth);
	}

	@Override protected byte[] newArrayInstance( int size ) {
		return new byte[size];
	}

	@Override protected int arrayLength( byte[] array ) {
		return array.length;
	}

	/**
	 * Appends a single value to the end of the array
	 *
	 * @param value (Input) The new value which is to be added
	 */
	public void append( byte value ) {
		// Current block that it's in
		int blockIdx = size/blockSize;
		// The new element in the block it will be at
		int indexInBlock = (size%blockSize);

		appendGrowthLogic(blockIdx, indexInBlock);

		blocks.data[blockIdx][indexInBlock] = value;
		size++;
	}

	/**
	 * Resizes the array and fills all new elements with the specified value
	 *
	 * @param desiredSize New array size
	 * @param initialValue The value of new elements
	 */
	public void resize( int desiredSize, byte initialValue ) {
		allocate(desiredSize, true, false);
		int originalSize = size;
		this.size = desiredSize;
		fill(originalSize, desiredSize, initialValue);
	}

	/**
	 * Fills the elements in the specified range with the specified value.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) last index, exclusive.
	 * @param value (Input) Fill value
	 */
	public void fill( int idx0, int idx1, byte value ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			Arrays.fill(block, block0, block1, value);
		});
	}

	/**
	 * Assigns an element a new value
	 *
	 * @param index (Input) Which element to modify
	 * @param value (Input) The element's new value
	 */
	public void set( int index, byte value ) {
		blocks.data[index/blockSize][index%blockSize] = value;
	}

	/**
	 * Copies a sub-array into the passed in array
	 *
	 * @param index Start index in this array
	 * @param array (Output) destination array
	 * @param offset Offset from start of destination array
	 * @param length Number of elements to copy
	 */
	public void getArray( int index, byte[] array, int offset, int length ) {
		processByBlock(index, index + length, ( block, block0, block1, arrayLoc ) -> {
			System.arraycopy(block, block0, array, offset + arrayLoc, block1 - block0);
		});
	}

	/**
	 * Returns the value in the array at the specified index
	 *
	 * @param index Index in the array
	 * @return value at index
	 */
	public byte get( int index ) {
		return blocks.data[index/blockSize][index%blockSize];
	}

	/**
	 * Simulates a for-each loop. Passes in element values to 'op' from the specified range.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) Last index, exclusive.
	 * @param op The operator which processes the values
	 */
	public void forEach( int idx0, int idx1, DogArray_I8.FunctionEach op ) {
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
	public void forIdx( int idx0, int idx1, DogArray_I8.FunctionEachIdx op ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			int index = idx0 + offset;
			for (int i = block0; i < block1; i++) {
				op.process(index++, block[i]);
			}
		});
	}

	/**
	 * Simulates a for-each loop. Passes in array indexes and element values to 'op' from the specified range.
	 * After calling op, the array is modified by the return value
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) Last index, exclusive.
	 * @param op The operator which processes the values
	 */
	public void applyIdx( int idx0, int idx1, DogArray_I8.FunctionApplyIdx op ) {
		processByBlock(idx0, idx1, ( block, block0, block1, offset ) -> {
			int index = idx0 + offset;
			for (int i = block0; i < block1; i++) {
				block[i] = op.process(index++, block[i]);
			}
		});
	}
}
