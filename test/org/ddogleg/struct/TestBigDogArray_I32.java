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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestBigDogArray_I32 extends ChecksBigDogArray<int[]> {

	/** resize with a set value */
	@Test void resizeValue() {
		checkResizeValue(BigDogGrowth.GROW_FIRST);
		checkResizeValue(BigDogGrowth.GROW);
		checkResizeValue(BigDogGrowth.FIXED);
	}

	/** Check resize by seeing if the size changed then checking the values */
	private void checkResizeValue( BigDogGrowth growth ) {
		int blockSize = 10;

		var alg = new BigDogArray_I32(1, blockSize, growth);

		// give it some values that we know and this is within the first block
		alg.resize(blockSize - 4, (int)4);
		alg.resize(blockSize - 1, (int)3);
		assertEquals(blockSize - 1, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize - 4 ? 4 : 3, alg.get(i));
		}

		// Resize past the first block
		alg.resize(blockSize + 3, (int)3);
		assertEquals(blockSize + 3, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize - 4 ? 4 : 3, alg.get(i));
		}

		// Fill this all in with a known value
		alg.fill(0, alg.size, (int)5);
		// Resize past the second block
		alg.resize(blockSize*2 + 1, (int)6);
		assertEquals(blockSize*2 + 1, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize + 3 ? 5 : 6, alg.get(i));
		}
	}

	/** resize only */
	@Test void resize() {
		checkResize(BigDogGrowth.GROW_FIRST);
		checkResize(BigDogGrowth.GROW);
		checkResize(BigDogGrowth.FIXED);
	}

	/** Check resize by seeing if the size changed then checking the values */
	private void checkResize( BigDogGrowth growth ) {
		int blockSize = 10;

		var alg = new BigDogArray_I32(1, blockSize, growth);

		// give it some values that we know and this is within the first block
		alg.resize(blockSize - 4, (int)4);
		alg.resize(blockSize - 1);
		assertEquals(blockSize - 1, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize - 4 ? 4 : 0, alg.get(i));
		}

		// Resize past the first block
		alg.resize(blockSize + 3);
		assertEquals(blockSize + 3, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize - 4 ? 4 : 0, alg.get(i));
		}

		// Fill this all in with a known value
		alg.fill(0, alg.size, (int)5);
		// Resize past the second block
		alg.resize(blockSize*2 + 1);
		assertEquals(blockSize*2 + 1, alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(i < blockSize + 3 ? 5 : 0, alg.get(i));
		}
	}

	@Test void appendArray() {
		for (BigDogGrowth growth : BigDogGrowth.values()) {
			checkAppendArray(1, growth, 5);
			checkAppendArray(1, growth, 12);
			checkAppendArray(4, growth, 8);
		}
	}

	private void checkAppendArray( int initialSize, BigDogGrowth growth, int arraySize ) {
		int[] array = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			array[i] = (int)(i + 1);
		}

		var alg = new BigDogArray_I32(initialSize, 10, growth);
		alg.resize(initialSize);
		alg.append(array, 1, arraySize - 1);
		assertTrue(alg.isValidStructure());
		assertEquals(initialSize + arraySize - 1, alg.size);
		for (int i = 0; i < initialSize; i++) {
			assertEquals(0, alg.get(i));
		}
		for (int i = initialSize; i < alg.size; i++) {
			assertEquals(array[i - initialSize + 1], alg.get(i));
		}
	}

	@Test void appendValue() {
		for (BigDogGrowth growth : BigDogGrowth.values()) {
			checkAppendValue(1, growth);
			checkAppendValue(4, growth);
		}
	}

	private void checkAppendValue( int initialSize, BigDogGrowth growth ) {
		var alg = new BigDogArray_I32(initialSize, 10, growth);
		alg.resize(initialSize);

		for (int i = 0; i < 21; i++) {
			alg.append((int)(i + 1));
			assertEquals(initialSize + i + 1, alg.size);
			assertTrue(alg.isValidStructure());
		}
		assertEquals(3, alg.blocks.size);

		for (int i = 0; i < initialSize; i++) {
			assertEquals(0, alg.get(i));
		}
		for (int i = initialSize; i < alg.size; i++) {
			assertEquals(i + 1 - initialSize, alg.get(i));
		}

		// Do it over again with a predeclared array
		alg.reserve(45);
		alg.resize(initialSize);

		for (int i = 0; i < 10; i++) {
			alg.append((int)(i + 1));
			assertEquals(initialSize + i + 1, alg.size);
			assertTrue(alg.isValidStructure());
		}

		for (int i = 0; i < initialSize; i++) {
			assertEquals(0, alg.get(i));
		}
		for (int i = initialSize; i < alg.size; i++) {
			assertEquals(i + 1 - initialSize, alg.get(i));
		}
	}

	@Test void fill() {
		for (BigDogGrowth growth : BigDogGrowth.values()) {
			checkFill(growth);
		}
	}

	private void checkFill( BigDogGrowth growth ) {
		var alg = new BigDogArray_I32(1, 10, growth);
		alg.resize(4, (int)2);
		alg.resize(11, (int)10);
		alg.fill(0, 3, (int)1);
		alg.fill(3, 9, (int)2);

		for (int i = 0; i < 3; i++) {
			assertEquals(1, alg.get(i));
		}
		for (int i = 3; i < 9; i++) {
			assertEquals(2, alg.get(i));
		}
		assertEquals(10, alg.get(9));
		assertEquals(10, alg.get(10));

		// fill across block boundary
		alg.fill(0, alg.size, (int)4);
		for (int i = 3; i < alg.size; i++) {
			assertEquals(4, alg.get(i));
		}
	}

	@Test void setArray() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(25);

		int[] array = new int[21];
		for (int i = 0; i < array.length; i++) {
			array[i] = (int)(i + 1);
		}

		alg.setArray(1, array, 1, array.length - 1);
		for (int i = 1; i < array.length; i++) {
			assertEquals(array[i], alg.get(i));
		}
	}

	@Test void set_get() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(25);

		for (int i = 0; i < 25; i++) {
			alg.set(i, (int)(i + 1));
			assertEquals(i + 1, alg.get(i));
		}
	}

	@Test void setTail_getTail() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(25);

		for (int i = 0; i < 25; i++) {
			alg.setTail(i, (int)(i + 1));
			assertEquals(i + 1, alg.getTail(i));
		}

		assertEquals(alg.get(alg.size-1), alg.getTail(0));
		assertEquals(alg.get(alg.size-2), alg.getTail(1));
	}

	@Test void getArray() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(22);
		for (int i = 0; i < alg.size; i++) {
			alg.set(i, (int)(i + 1));
		}
		int[] array = new int[15];
		alg.getArray(2, array, 1, 12);
		for (int i = 0; i < array.length; i++) {
			if (i >= 1 && i < 13)
				assertEquals(2 + i, array[i]);
			else
				assertEquals(0, array[i]);
		}
	}

	@Test void forEach() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(22);
		for (int i = 0; i < alg.size; i++) {
			alg.set(i, (int)(i + 1));
		}

		DogArray_I32 results = new DogArray_I32();
		alg.forEach(1, 12, results::add);
		assertEquals(11, results.size);
		for (int i = 0; i < 11; i++) {
			assertEquals(i + 2, results.get(i));
		}
	}

	@Test void forIdx() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(22);
		for (int i = 0; i < alg.size; i++) {
			alg.set(i, (int)(i + 1));
		}

		DogArray_I32 results = new DogArray_I32(12);
		results.resize(12);
		alg.forIdx(1, 12, results::set);
		assertEquals(0, results.get(0));
		for (int i = 1; i < 12; i++) {
			assertEquals(i + 1, results.get(i));
		}
	}

	@Test void applyIdx() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(22);
		for (int i = 0; i < alg.size; i++) {
			alg.set(i, (int)(i + 1));
		}

		alg.applyIdx(1, 12, ( idx, val ) -> (int)(idx*2));
		for (int i = 0; i < alg.size; i++) {
			if (i >= 1 && i < 12) {
				assertEquals(i*2, alg.get(i));
			} else {
				assertEquals(i + 1, alg.get(i));
			}
		}
	}

	@Test void processByBlock() {
		var alg = new BigDogArray_I32(2, 10, BigDogGrowth.GROW);
		alg.resize(26);
		alg.processByBlock(1, alg.size - 1, ( block, idx0, idx1, offset ) -> {
			for (int i = idx0; i < idx1; i++) {
				block[i] = (int)(i - idx0 + offset);
			}
		});
		for (int i = 0; i < alg.size; i++) {
			if (i >= 1 && i < alg.size - 1) {
				assertEquals((int)(i - 1), alg.get(i));
			} else {
				assertEquals(0, alg.get(i));
			}
		}
	}

	@Test void isEquivalent() {
		// both arrays will have different block sizes
		var a = new BigDogArray_I32(10, 5, BigDogGrowth.GROW);
		var b = new BigDogArray_I32(10, 4, BigDogGrowth.GROW);

		// see if it handles an empty array correctly
		assertTrue(a.isEquivalent(b));

		// They will have the same values
		for (int i = 0; i < 21; i++) {
			a.append(i);
			b.append(i);
		}

		assertTrue(a.isEquivalent(b));

		// adjust an element so that it's out of spec
		a.set(4, 5);
		assertFalse(a.isEquivalent(b));
		assertFalse(b.isEquivalent(a));

		// make the shapes not match
		b.append(4);
		assertFalse(a.isEquivalent(b));
		assertFalse(b.isEquivalent(a));
	}

	@Override public BigDogArrayBase<int[]> createBigDog( int initialAllocation, int blockSize, BigDogGrowth growth ) {
		return new BigDogArray_I32(initialAllocation, blockSize, growth);
	}

	@Override public void fillRandom( BigDogArrayBase<int[]> _array ) {
		var array = (BigDogArray_I32)_array;
		array.applyIdx(0, array.size, ( idx, v ) -> rand.nextInt());
	}

	@Override public Object get( BigDogArrayBase<int[]> _array, int index ) {
		var array = (BigDogArray_I32)_array;
		return array.get(index);
	}

	@Override public void copy( BigDogArrayBase<int[]> _src, BigDogArrayBase<int[]> _dst ) {
		var src = (BigDogArray_I32)_src;
		var dst = (BigDogArray_I32)_dst;

		dst.resize(src.size);
		dst.applyIdx(0, src.size, ( idx, v ) -> src.get(idx));
	}
}
