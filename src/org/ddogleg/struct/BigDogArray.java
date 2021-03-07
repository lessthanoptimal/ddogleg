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

import lombok.Getter;

/**
 * A growable array that is composed of internal blocks. This is intended to reduce overhead when growing a very large
 * array. Contrast this with {@link DogArray} for which its entire internal array needs to be copied when growing.
 * While more complex and in some classes slightly slower, the approach employed here is much more memory and
 * speed efficient while growing. See {@link Growth} for a description of different growth strategies.
 *
 * When operations are used which add/append to the end of the array then extra room is typically added, if a grow
 * strategy is employed. This is done to avoid excessive amount of memory copy operations.
 *
 * @author Peter Abeles
 */
public abstract class BigDogArray<Array> {
	/**
	 * Default block size. It's assumed that this is used in fairly large arrays.
	 */
	public static final int DEFAULT_BLOCK_SIZE = 32768;
	/**
	 * Storage for blocks. Note that the size is always the number of elements with non-null values.
	 */
	protected @Getter final FastArray<Array> blocks;
	/**
	 * Number of elements in a full block
	 */
	protected @Getter final int blockSize;
	/**
	 * Number of elements in the array being used. Know what you're doing before modifying.
	 */
	public @Getter int size;

	/**
	 * If a grow strategy is employed, this is the initial size of a block. Do not set this to be
	 * larger than {@link #blockSize}.
	 */
	private @Getter int initialBlockSize = 8;

	/**
	 * Approach used for growth. See enum for a description
	 */
	protected @Getter final BigDogArray.Growth growth;

	protected BigDogArray( int initialAllocation, int blockSize, BigDogArray.Growth growth ) {
		// initializing with a size of zero causes all sorts of edge cases. So just make it illegal.
		if (initialAllocation <= 0)
			throw new IllegalArgumentException("initialAllocation size must be a positive value");
		if (blockSize <= 0)
			throw new IllegalArgumentException("Block size must be a positive value");
		this.blockSize = blockSize;
		this.growth = growth;

		Class<Array> type = (Class)newArrayInstance(0).getClass();
		blocks = new FastArray<>(type, getDesiredBlocks(initialAllocation));
		blocks.size = blocks.data.length; // hack to enable the code below to work

		// Declare all full blocks
		for (int i = 0; i < blocks.size - 1; i++) {
			blocks.set(i, newArrayInstance(blockSize));
		}

		// The last block might be a partial block
		if (blocks.size > 0) {
			if ((blocks.size == 1 && growth == BigDogArray.Growth.GROW_FIRST) || growth == BigDogArray.Growth.GROW) {
				blocks.set(blocks.size - 1, newArrayInstance(initialAllocation%blockSize));
			} else {
				blocks.set(blocks.size - 1, newArrayInstance(blockSize));
			}
		}
	}

	/**
	 * Sets the array size to zero. No memory is freed.
	 */
	public void reset() {
		this.size = 0;
	}

	/**
	 * Ensures that the internal data can store up to this number of elements before needing to allocate more memory.
	 * No extra data is added and this function is only recommended when the array has a known max size.
	 */
	public void reserve( int desiredSize ) {
		allocate(desiredSize, false, false);
	}

	/**
	 * Allocate more memory so that an array of the specified desiredSize can be stored. Optionally copy old
	 * values into new arrays when growing
	 *
	 * @param desiredSize New size of internal array, not just a single block.
	 * @param saveValues If old values should be copied.
	 * @param addExtra If using a grow strategy, is this a case where it should add extra elements or do the
	 * exact request?
	 */
	protected void allocate( int desiredSize, boolean saveValues, boolean addExtra ) {
		int desiredNumBlocks = getDesiredBlocks(desiredSize);

		// See if the current allocation in blocks is larger than what's requested
		if (blocks.size() > desiredNumBlocks) {
			return;
		}

		// Add more blocks
		int priorNumBlocks = blocks.size;
		blocks.resize(desiredNumBlocks);

		// If the number of blocks is increasing, make sure the prior last block is the correct size
		if (priorNumBlocks > 0 && priorNumBlocks < desiredNumBlocks) {
			// all growth methods require the non last block to be blockSize
			Array old = blocks.data[priorNumBlocks - 1];
			if (arrayLength(old) != blockSize) {
				Array replacement = newArrayInstance(blockSize);
				if (saveValues)
					System.arraycopy(old, 0, replacement, 0, arrayLength(old));
				blocks.data[priorNumBlocks - 1] = replacement;
			}
		}

		// All new blocks, but the last one, must be blockSize in length
		for (int i = priorNumBlocks; i < desiredNumBlocks - 1; i++) {
			blocks.data[i] = newArrayInstance(blockSize);
		}

		// The last block might not be a full block
		int desiredLastBlockSize = computeLastBlockSize(desiredSize, desiredNumBlocks);

		if (priorNumBlocks == desiredNumBlocks && priorNumBlocks > 0) {
			// If requested/allowed, increase the size the last array. This will effectively double its size.
			// Adding a bit of extra when growing significantly reduces number of array copies
			Array old = blocks.data[priorNumBlocks - 1];
			if (addExtra)
				desiredLastBlockSize =
						Math.min(blockSize, initialBlockSize + arrayLength(old)*2 + desiredLastBlockSize);

			if (arrayLength(old) < desiredLastBlockSize) {
				Array replacement = newArrayInstance(desiredLastBlockSize);
				if (saveValues)
					System.arraycopy(old, 0, replacement, 0, arrayLength(old));
				blocks.data[desiredNumBlocks - 1] = replacement;
			}
		} else if (priorNumBlocks < desiredNumBlocks) {
			if (addExtra)
				desiredLastBlockSize = Math.min(blockSize, initialBlockSize + 2*desiredLastBlockSize);

			blocks.data[desiredNumBlocks - 1] = newArrayInstance(desiredLastBlockSize);
		}
	}

	/**
	 * Declare an array for the last block
	 *
	 * @param desiredSize (Input) The new desired size of this array
	 * @return A new array
	 */
	private int computeLastBlockSize( int desiredSize, int numBlocks ) {
		if (growth == BigDogArray.Growth.FIXED || (numBlocks > 1 && growth == BigDogArray.Growth.GROW_FIRST)) {
			return blockSize;
		} else {
			return desiredSize%blockSize == 0 ? blockSize : desiredSize%blockSize;
		}
	}

	/**
	 * Either increased or decreases the array size. If it's increased then the new elements will be filled with
	 * undefined values, depending on their previous state.
	 *
	 * @param desiredSize (Input) New array size
	 */
	public void resize( int desiredSize ) {
		allocate(desiredSize, true, false);
		this.size = desiredSize;
	}

	/**
	 * Adds the input array to the end of this array.
	 *
	 * @param array (Input) Array which is to be copied
	 * @param offset (Input) First element in the array which is to be copied
	 * @param length (Input) Number of elements which are to be copied
	 */
	public void append( Array array, int offset, int length ) {
		// make sure enough memory has been allocated
		allocate(this.size + length, true, true);
		this.size = this.size + length;
		setArray(this.size - length, array, offset, length);
	}

	protected void appendGrowthLogic( int blockIdx, int indexInBlock ) {
		// see if it needs to grow by one block
		if (indexInBlock == 0) {
			// Is there a block in the queue it can recycle?
			if (blocks.size <= blockIdx) {
				// Nope, need to add a new block
				int desiredBlockSize = computeLastBlockSize(size + 1, blocks.size + 1);
				desiredBlockSize = Math.min(blockSize, desiredBlockSize + initialBlockSize);
				blocks.add(newArrayInstance(desiredBlockSize));
			} else {
				// there is an existing block, but we need to make it bigger
				growLastBlock(1);
			}
		} else if (indexInBlock >= arrayLength(blocks.data[blockIdx])) {
			growLastBlock(1);
		}
	}

	/**
	 * Copies the passed in array into this array at the specified location
	 *
	 * @param location (Input) First element that the array is to be inserted at
	 * @param array (Input) Array which is to be copied in
	 * @param offset (Input) Offset inside of array that it should be copied from
	 * @param length (Input) Number of elements in array to copy
	 */
	public void setArray( int location, Array array, int offset, int length ) {
		int idx0 = location;
		int idx1 = location + length;

		// First block is a special case. Fill up the block and make sure idx0 is aligned to the blocks
		if (idx0%blockSize != 0) {
			int blockIdx0 = idx0%blockSize;
			int remainingInBlock = blockSize - blockIdx0;
			int lengthInBlock = Math.min(remainingInBlock, idx1 - idx0);
			System.arraycopy(array, offset, blocks.data[idx0/blockSize], blockIdx0, lengthInBlock);
			offset += lengthInBlock;
			idx0 += lengthInBlock;
		}

		// idx0 should be at the start of a block now. Just mindlessly copy until it hits the end of a block
		while (idx0 + blockSize < idx1) {
			System.arraycopy(array, offset, blocks.data[idx0/blockSize], 0, blockSize);
			offset += blockSize;
			idx0 += blockSize;
		}

		// fill in the last block
		if (idx0 != idx1) {
			System.arraycopy(array, offset, blocks.data[idx0/blockSize], 0, idx1 - idx0);
		}
	}

	/**
	 * Passes in array elements to the operator one block at a time. What's given to the operator
	 * is the first index in the block it should process, the last (exclusive) index in the block,
	 * and the number of elements offset from the original range requested.
	 *
	 * @param idx0 (Input) First index, inclusive.
	 * @param idx1 (Input) Last index, exclusive.
	 * @param op The operator which processes the values
	 */
	public void processByBlock( int idx0, int idx1, FunctionEachRange<Array> op ) {
		int origIdx0 = idx0;

		// If not initially aligned at the block boundaries, process it until it hits the first boundary
		if (idx0%blockSize != 0) {
			int blockIdx0 = idx0%blockSize;
			int remainingInBlock = blockSize - blockIdx0;
			int lengthInBlock = Math.min(remainingInBlock, idx1 - idx0);
			int blockIdx1 = blockIdx0 + lengthInBlock;

			op.process(blocks.data[idx0/blockSize], blockIdx0, blockIdx1, 0);

			idx0 += lengthInBlock;
		}

		// We can now process it one block at a time
		while (idx0 + blockSize < idx1) {
			op.process(blocks.data[idx0/blockSize], 0, blockSize, idx0 - origIdx0);
			idx0 += blockSize;
		}

		// The end is another special case. It might end before a block boundary
		if (idx0 != idx1) {
			op.process(blocks.data[idx0/blockSize], 0, idx1 - idx0, idx0 - origIdx0);
		}
	}

	public Array getBlock( int index ) {
		return blocks.get(index/blockSize);
	}

	/**
	 * Changes the initial block size. This only matters when blocks can grow. If the value is out of bounds
	 * it is assigned the closest valid value.
	 *
	 * @param initialBlockSize initial block size
	 */
	public void setInitialBlockSize( int initialBlockSize ) {
		this.initialBlockSize = Math.max(1, Math.min(blockSize, initialBlockSize));
	}

	/**
	 * If a growth strategy is employed on the last block this will
	 *
	 * @param requiredNewSpace (Input) How much new space is needed. It's assumed that this value won't bleed into the next block
	 */
	protected void growLastBlock( int requiredNewSpace ) {
		Array block = blocks.data[blocks.size - 1];
		if (arrayLength(block) == blockSize)
			return;

		// The minimum size of the block to fit the new data
		int minimum = (size + requiredNewSpace)%blockSize;
		if (minimum == 0)
			minimum = blockSize;
		if (arrayLength(block) >= minimum)
			return;

		// Need to declare a larger block to hold everything. Let's add some extra room
		// to avoid doing this process too often
		int desired = Math.min(blockSize, arrayLength(block)*2 + requiredNewSpace + initialBlockSize);

		// declare the new array and copy the old array into it
		Array work = newArrayInstance(desired);
		System.arraycopy(block, 0, work, 0, size%blockSize);

		// replace the old array with the new array
		blocks.data[blocks.size - 1] = work;
	}

	private int getDesiredBlocks( int desiredSize ) {
		return desiredSize/blockSize + (desiredSize%blockSize > 0 ? 1 : 0);
	}

	/**
	 * Performs an internal check to make sure all data structures and values are internally consistent. Used for
	 * debugging and paranoia.
	 */
	public boolean isValidStructure() {
		// there has to be one block. the minimum allocation size is 1
		if (blocks.size == 0)
			return false;

		// make sure the size isn't impossibly large
		int maxStorage = arrayLength(blocks.getTail()) + (blocks.size - 1)*blockSize;
		if (size > maxStorage)
			return false;

		// Make sure internal blocks are correct size
		switch (growth) {
			case FIXED: {
				for (int i = 0; i < blocks.size; i++) {
					if (arrayLength(blocks.get(i)) != blockSize)
						return false;
				}
			}
			break;

			case GROW_FIRST: {
				for (int i = 1; i < blocks.size; i++) {
					if (arrayLength(blocks.get(i)) != blockSize)
						return false;
				}
				if (blocks.size == 1) {
					if (arrayLength(blocks.get(0)) > blockSize)
						return false;
				} else {
					if (arrayLength(blocks.get(0)) != blockSize)
						return false;
				}
			}
			break;

			case GROW: {
				for (int i = 0; i < blocks.size - 1; i++) {
					if (arrayLength(blocks.get(i)) != blockSize)
						return false;
				}
				if (arrayLength(blocks.getTail()) > blockSize)
					return false;
			}
			break;
		}

		return true;
	}

	/**
	 * Returns the number of elements which have been allocated. This array size has to be less than
	 * or equal to this number\
	 */
	public int getTotalAllocation() {
		return (blocks.size-1)*blockSize + arrayLength(blocks.data[blocks.size-1]);
	}

	protected int blockArrayLength( int block ) {
		return arrayLength(blocks.get(block));
	}

	protected abstract Array newArrayInstance( int size );

	protected abstract int arrayLength( Array array );

	@FunctionalInterface
	public interface FunctionEachRange<Array> {
		/**
		 * Passes in internal blocks to be processed in chunks
		 *
		 * @param block Array block to process
		 * @param idx0 First valid index in the array, inclusive
		 * @param idx1 Last index in the array, exclusive
		 * @param offset The number of elements offset from the first element it requested
		 */
		void process( Array block, int idx0, int idx1, int offset );
	}

	/**
	 * Specifies which strategy is used to select internal array size when adding new blocks. If a fixed sized
	 * block is always used this can be memory inefficient, but is faster as array creation and copy can be avoided.
	 */
	public enum Growth {
		/**
		 * Blocks always have a fixed size.
		 */
		FIXED,
		/**
		 * The first block will grow as needed but all blocks later on are fixed. The idea being that initially
		 * a block can be much bigger than needed initially, but after the first block the amount of memory wasted
		 * is relatively small.
		 */
		GROW_FIRST,
		/**
		 * Every new block will start out as small as possible. Most memory efficient approach but will be
		 * wasteful as smaller arrays will be continuously created and copied as the array grows.
		 */
		GROW
	}
}
