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

import org.ddogleg.struct.BigDogArray.Growth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class ChecksBigDogArray<Array> {

	public abstract BigDogArray<Array> createBigDog(int initialAllocation, int blockSize, Growth growth);

	/**
	 * Ensures that the initial array allocation is done correctly by the constructor
	 */
	@Test void constructor() {
		checkConstructor(5, 10, Growth.GROW_FIRST, 5);
		checkConstructor(85, 10, Growth.GROW_FIRST, 10);
		checkConstructor(5, 10, Growth.GROW, 5);
		checkConstructor(85, 10, Growth.GROW, 5);
		checkConstructor(5, 10, Growth.FIXED, 10);
		checkConstructor(85, 10, Growth.FIXED, 10);
	}

	private void checkConstructor( int initialAllocation, int blockSize, Growth growth, int expectLastBlock ) {
		int expectedBlocks = initialAllocation/blockSize + (initialAllocation%blockSize != 0 ? 1 : 0);

		BigDogArray<Array> alg = createBigDog(initialAllocation, blockSize, growth);
		assertEquals(0, alg.size);
		assertEquals(growth, alg.growth);
		assertEquals(10, alg.blockSize);
		assertEquals(expectedBlocks, alg.blocks.size);
		if (expectedBlocks > 0)
			assertEquals(expectLastBlock, alg.blockArrayLength(expectedBlocks - 1));
		assertTrue(alg.isValidStructure());
	}

	@Test void reserve() {
		checkReserve(5, Growth.GROW_FIRST, 5);
		checkReserve(10, Growth.GROW_FIRST, 10);
		checkReserve(11, Growth.GROW_FIRST, 10);
		checkReserve(45, Growth.GROW_FIRST, 10);
		checkReserve(50, Growth.GROW_FIRST, 10);
		checkReserve(5, Growth.GROW, 5);
		checkReserve(10, Growth.GROW, 10);
		checkReserve(45, Growth.GROW, 5);
		checkReserve(50, Growth.GROW, 10);
		checkReserve(5, Growth.FIXED, 10);
		checkReserve(10, Growth.FIXED, 10);
		checkReserve(45, Growth.FIXED, 10);
		checkReserve(50, Growth.FIXED, 10);
	}

	private void checkReserve( int allocation, Growth growth, int expectLastBlock ) {
		int blockSize = 10;
		int expectedBlocks = allocation/blockSize + (allocation%blockSize != 0 ? 1 : 0);

		// Allocate the memory
		BigDogArray<Array> alg = createBigDog(1, blockSize, growth);
		alg.reserve(allocation);
		assertEquals(0, alg.size); // make sure the size didn't change
		assertTrue(alg.isValidStructure());
		// verify the blocks have the expected structure
		assertEquals(expectedBlocks, alg.blocks.size);
		for (int i = 0; i < alg.blocks.size - 1; i++) {
			assertEquals(blockSize, alg.blockArrayLength(i));
		}

		// See how well it handles reserve after being reset
		alg.reset();
		alg.reserve(allocation);
		assertEquals(0, alg.size);
		assertTrue(alg.isValidStructure());
		assertEquals(expectedBlocks, alg.blocks.size);
		for (int i = 0; i < alg.blocks.size - 1; i++) {
			assertEquals(blockSize, alg.blockArrayLength(i));
		}
	}

	@Test void getDesiredBlocks() {
		BigDogArray<Array> alg = createBigDog(1, 10, Growth.GROW_FIRST);

		assertEquals(1, alg.getDesiredBlocks(1));
		assertEquals(1, alg.getDesiredBlocks(9));
		assertEquals(1, alg.getDesiredBlocks(10));
		assertEquals(2, alg.getDesiredBlocks(11));
		assertEquals(2, alg.getDesiredBlocks(19));
		assertEquals(2, alg.getDesiredBlocks(20));
	}

	@Test void allocate_GROW_FIRST() {
		BigDogArray<Array> alg = createBigDog(1, 10, Growth.GROW_FIRST);

		assertEquals(1, alg.getTotalAllocation());
		alg.allocate(2,false,false);
		assertEquals(2, alg.getTotalAllocation());
		alg.allocate(3,false,true);
		assertTrue(3 <= alg.getTotalAllocation() && 10 >= alg.getTotalAllocation());

		alg.allocate(10,false,false);
		assertEquals(10, alg.getTotalAllocation());

		alg.allocate(12,false,false);
		assertEquals(20, alg.getTotalAllocation());

		alg.allocate(13,false,true);
		assertEquals(20, alg.getTotalAllocation());
	}

	@Test void allocate_GROW() {
		BigDogArray<Array> alg = createBigDog(1, 10, Growth.GROW);

		assertEquals(1, alg.getTotalAllocation());
		alg.allocate(2,false,false);
		assertEquals(2, alg.getTotalAllocation());
		alg.allocate(3,false,true);
		assertTrue(3 <= alg.getTotalAllocation() && 10 >= alg.getTotalAllocation());

		alg.allocate(10,false,false);
		assertEquals(10, alg.getTotalAllocation());

		alg.allocate(12,false,false);
		assertEquals(12, alg.getTotalAllocation());

		alg.allocate(13,false,true);
		assertTrue(13 <= alg.getTotalAllocation() && 20 >= alg.getTotalAllocation());
	}

	@Test void allocate_FIXED() {
		BigDogArray<Array> alg = createBigDog(1, 10, Growth.FIXED);

		assertEquals(10, alg.getTotalAllocation());
		alg.allocate(2,false,false);
		assertEquals(10, alg.getTotalAllocation());
		alg.allocate(3,false,true);
		assertEquals(10, alg.getTotalAllocation());

		alg.allocate(10,false,false);
		assertEquals(10, alg.getTotalAllocation());

		alg.allocate(12,false,false);
		assertEquals(20, alg.getTotalAllocation());

		alg.allocate(13,false,true);
		assertEquals(20, alg.getTotalAllocation());
	}
}
