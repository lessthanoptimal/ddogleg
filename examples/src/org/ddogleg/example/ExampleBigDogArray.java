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

package org.ddogleg.example;

import org.ddogleg.struct.BigDogArray_I32;

/**
 * BigDogArrays are designed to handle very large arrays at the cost of additional complexity and a small hit
 * on read/write performance. As you start to hit the limits of a java array, growing a DogArray can get expensive
 * as it will need to allocate a new array, then copy the old one over. This could mean two very large
 * chunks of continuous memory. BigDogArrays are broken up into multiple chunks/blocks and at worst the cost
 * to grow the array is limited by the size of a chunk.
 *
 * It's recommended that you first look at ExampleDogArray, as we will only cover new topics here.
 *
 * @author Peter Abeles
 */
public class ExampleBigDogArray {
	public static void main( String[] args ) {
		// Unless you really know what you are doing, use the default constructor
		var array = new BigDogArray_I32();

		// It is possible to customize the block size and how the array is grown.
//		var array = new BigDogArray_I32(10_000, 50_0000, BigDogGrowth.GROW_FIRST);

		// Let's initialize it after pre-allocating memory
		array.reserve(50);
		for (int i = 0; i < 50; i++) {
			// Set is actually making two array access calls
			array.set(i, i*2);
		}

		// Here's an alternative to do the same thing. It will be smart enough toe process it by blocks
		// reducing the number of array accesses
		array.applyIdx(0, 50, ( i, value ) -> i*2);

		// if you need to process a range of values it's recommended you use forEach or forIdx and it will
		// handle the internal complexity for you
		array.forEach(10, 15, v -> System.out.print(v + ","));
		System.out.println();

		// If you for some reason need to process a range of values (10 to 20) but need to access the raw
		// block array, then processByBlock is your friend
		array.processByBlock(10, 20, ( block, idx0, idx1, offset ) -> {
			// block is the raw array that composes the block
			// idx0 is the first element in the block that you should process
			// idx1 is the upper extent, exclusive
			// offset is the offset to the array's indexing. So block[idx0 + 1] = array[offset + 1]
			for (int i = idx0; i < idx1; i++) {
				block[i] = offset + i;
			}
		});

		// Let's see what happened
		array.forEach(0, 20, v -> System.out.print(v + ","));
		System.out.println();
	}
}
