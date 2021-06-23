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

package org.ddogleg.util;

import org.ddogleg.struct.DogArray_I32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
class TestPrimitiveArrays {
	@Test void intersection_FindMinMax() {
		int[] setA = new int[]{8, 7, 6, 3, 1, 0, -2, -5};
		int[] setB = new int[]{3, 4, 5, -3, 1, 7, -2, -5};
		int[] expected = new int[]{-5, -2, 1, 3, 7};

		var results = new DogArray_I32();
		results.add(-99); // make sure it's cleared

		// intentionally give it a min/max that's larger than the actual
		PrimitiveArrays.intersection(setA, setA.length, setB, setB.length, results, null);

		assertEquals(expected.length, results.size);
		for (int i = 0; i < results.size; i++) {
			assertEquals(expected[i], results.get(i));
		}
	}

	@Test void intersection_GivenMinMax() {
		int[] setA = new int[]{8, 7, 6, 3, 1, 0, -2, -5};
		int[] setB = new int[]{3, 4, 5, -3, 1, 7, -2, -5};
		int[] expected = new int[]{-5, -2, 1, 3, 7};

		var results = new DogArray_I32();
		results.add(-99); // make sure it's cleared

		// intentionally give it a min/max that's larger than the actual
		PrimitiveArrays.intersection(setA, setA.length, setB, setB.length, -6, 20, results, null);

		assertEquals(expected.length, results.size);
		for (int i = 0; i < results.size; i++) {
			assertEquals(expected[i], results.get(i));
		}
	}

	@Test void union_GivenMinMax() {
		int[] setA = new int[]{8, 7, 6, 3, 1, 0, -2, -5};
		int[] setB = new int[]{3, 4, 5, -3, 1, 7, -2, -5};
		int[] expected = new int[]{-5, -3, -2, 0, 1, 3, 4, 5, 6, 7, 8};

		var results = new DogArray_I32();
		results.add(-99); // make sure it's cleared

		// intentionally give it a min/max that's larger than the actual
		PrimitiveArrays.union(setA, setA.length, setB, setB.length, -6, 20, results, null);

		assertEquals(expected.length, results.size);
		for (int i = 0; i < results.size; i++) {
			assertEquals(expected[i], results.get(i));
		}
	}

	@Test void fillCount_input() {
		int[] values = new int[100];
		PrimitiveArrays.fillCounting(values, 10, 40);
		for (int i = 0; i < values.length; i++) {
			if (i < 10 || i >= 50) {
				assertEquals(0, values[i]);
			} else {
				assertEquals(i - 10, values[i]);
			}
		}
	}

	@Test void shuffle_byte() {
		Random rand = new Random(234);
		byte[] values = new byte[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (byte)i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_byte_zeroLength() {
		var values = new byte[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (byte)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < 5; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void shuffle_short() {
		Random rand = new Random(234);
		short[] values = new short[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (short)i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_short_zeroLength() {
		var values = new short[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (short)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < 5; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void shuffle_int() {
		Random rand = new Random(234);
		int[] values = new int[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_int_zeroLength() {
		var values = new int[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (int)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < 5; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void shuffle_long() {
		Random rand = new Random(234);
		long[] values = new long[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_long_zeroLength() {
		var values = new long[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (long)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < values.length; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void shuffle_float() {
		Random rand = new Random(234);
		float[] values = new float[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_float_zeroLength() {
		var values = new float[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (float)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < 5; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void shuffle_double() {
		Random rand = new Random(234);
		double[] values = new double[100];
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		PrimitiveArrays.shuffle(values, 10, 90, rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if (Math.abs(values[j + 10] - i) == 0) {
					count++;
				}
			}
			assertEquals(1, count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if (Math.abs(values[i + 10] - i) == 0) {
				matches++;
			}
		}
		assertTrue(matches < 10);
	}

	@Test void shuffle_double_zeroLength() {
		var values = new double[5];
		for (int i = 0; i < values.length; i++) {
			values[i] = (double)(5-i);
		}

		// nothing should change if it shuffles with a length of 1
		PrimitiveArrays.shuffle(values, 1, 0, new Random(234));
		for (int i = 0; i < 5; i++) {
			assertEquals(5-i, values[i]);
		}
	}

	@Test void min_byte() {
		byte[] values = new byte[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (byte)(i + 2);
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_byte_extremes() {
		assertEquals(Byte.MIN_VALUE, PrimitiveArrays.min(new byte[]{Byte.MIN_VALUE}, 0, 1));
		assertEquals(Byte.MAX_VALUE, PrimitiveArrays.min(new byte[]{Byte.MAX_VALUE}, 0, 1));
	}

	@Test void min_short() {
		short[] values = new short[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (short)(i + 2);
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_short_extremes() {
		assertEquals(Short.MIN_VALUE, PrimitiveArrays.min(new short[]{Short.MIN_VALUE}, 0, 1));
		assertEquals(Short.MAX_VALUE, PrimitiveArrays.min(new short[]{Short.MAX_VALUE}, 0, 1));
	}

	@Test void min_int() {
		int[] values = new int[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_int_extremes() {
		assertEquals(Integer.MIN_VALUE, PrimitiveArrays.min(new int[]{Integer.MIN_VALUE}, 0, 1));
		assertEquals(Integer.MAX_VALUE, PrimitiveArrays.min(new int[]{Integer.MAX_VALUE}, 0, 1));
	}

	@Test void min_long() {
		long[] values = new long[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_long_extremes() {
		assertEquals(Long.MIN_VALUE, PrimitiveArrays.min(new long[]{Long.MIN_VALUE}, 0, 1));
		assertEquals(Long.MAX_VALUE, PrimitiveArrays.min(new long[]{Long.MAX_VALUE}, 0, 1));
	}

	@Test void min_float() {
		float[] values = new float[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_float_extremes() {
		assertEquals(-Float.MAX_VALUE, PrimitiveArrays.min(new float[]{-Float.MAX_VALUE}, 0, 1));
		assertEquals(Float.MAX_VALUE, PrimitiveArrays.min(new float[]{Float.MAX_VALUE}, 0, 1));
	}

	@Test void min_double() {
		double[] values = new double[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(2, PrimitiveArrays.min(values, 10, 90));
	}

	@Test void min_double_extremes() {
		assertEquals(-Double.MAX_VALUE, PrimitiveArrays.min(new double[]{-Double.MAX_VALUE}, 0, 1));
		assertEquals(Double.MAX_VALUE, PrimitiveArrays.min(new double[]{Double.MAX_VALUE}, 0, 1));
	}

	@Test void minIdx_byte() {
		byte[] values = new byte[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (byte)(i + 2);
		}
		assertEquals(10, PrimitiveArrays.minIdx(values, 10, 90));
	}

	@Test void minIdx_byte_extremes() {
		assertEquals(0, PrimitiveArrays.minIdx(new byte[]{Byte.MIN_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.minIdx(new byte[]{Byte.MAX_VALUE}, 0, 1));
	}

	@Test void minIdx_int() {
		int[] values = new int[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(values, 10, 90));
	}

	@Test void minIdx_int_extremes() {
		assertEquals(0, PrimitiveArrays.minIdx(new int[]{Integer.MIN_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.minIdx(new int[]{Integer.MAX_VALUE}, 0, 1));
	}

	@Test void minIdx_float() {
		float[] values = new float[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(values, 10, 90));
	}

	@Test void minIdx_float_extremes() {
		assertEquals(0, PrimitiveArrays.minIdx(new float[]{-Float.MAX_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.minIdx(new float[]{Float.MAX_VALUE}, 0, 1));
	}

	@Test void minIdx_double() {
		double[] values = new double[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i + 2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(values, 10, 90));
	}

	@Test void minIdx_double_extremes() {
		assertEquals(0, PrimitiveArrays.minIdx(new double[]{-Double.MAX_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.minIdx(new double[]{Double.MAX_VALUE}, 0, 1));
	}

	@Test void max_byte() {
		byte[] values = new byte[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (byte)i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_byte_extremes() {
		assertEquals(Byte.MIN_VALUE, PrimitiveArrays.max(new byte[]{Byte.MIN_VALUE}, 0, 1));
		assertEquals(Byte.MAX_VALUE, PrimitiveArrays.max(new byte[]{Byte.MAX_VALUE}, 0, 1));
	}

	@Test void max_short() {
		short[] values = new short[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (short)i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_short_extremes() {
		assertEquals(Short.MIN_VALUE, PrimitiveArrays.max(new short[]{Short.MIN_VALUE}, 0, 1));
		assertEquals(Short.MAX_VALUE, PrimitiveArrays.max(new short[]{Short.MAX_VALUE}, 0, 1));
	}

	@Test void max_int() {
		int[] values = new int[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_int_extremes() {
		assertEquals(Integer.MIN_VALUE, PrimitiveArrays.max(new int[]{Integer.MIN_VALUE}, 0, 1));
		assertEquals(Integer.MAX_VALUE, PrimitiveArrays.max(new int[]{Integer.MAX_VALUE}, 0, 1));
	}

	@Test void max_long() {
		long[] values = new long[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (long)i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_long_extremes() {
		assertEquals(Long.MIN_VALUE, PrimitiveArrays.max(new long[]{Long.MIN_VALUE}, 0, 1));
		assertEquals(Long.MAX_VALUE, PrimitiveArrays.max(new long[]{Long.MAX_VALUE}, 0, 1));
	}

	@Test void max_float() {
		float[] values = new float[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_float_extremes() {
		float[] values = {-Float.MAX_VALUE, Float.MAX_VALUE};
		assertEquals(-Float.MAX_VALUE, PrimitiveArrays.min(values, 0, values.length));
	}

	@Test void max_double() {
		double[] values = new double[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(values, 10, 90));
	}

	@Test void max_double_extremes() {
		double[] values = {-Double.MAX_VALUE, Double.MAX_VALUE};
		assertEquals(-Double.MAX_VALUE, PrimitiveArrays.min(values, 0, values.length));
	}

	@Test void maxIdx_byte() {
		byte[] values = new byte[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = (byte)i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(values, 10, 90));
	}

	@Test void maxIdx_byte_extremes() {
		assertEquals(0, PrimitiveArrays.maxIdx(new byte[]{Byte.MIN_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.maxIdx(new byte[]{Byte.MAX_VALUE}, 0, 1));
	}

	@Test void maxIdx_int() {
		int[] values = new int[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(values, 10, 90));
	}

	@Test void maxIdx_int_extremes() {
		assertEquals(0, PrimitiveArrays.maxIdx(new int[]{Integer.MIN_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.maxIdx(new int[]{Integer.MAX_VALUE}, 0, 1));
	}

	@Test void maxIdx_float() {
		float[] values = new float[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(values, 10, 90));
	}

	@Test void maxIdx_float_extremes() {
		assertEquals(0, PrimitiveArrays.maxIdx(new float[]{-Float.MAX_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.maxIdx(new float[]{Float.MAX_VALUE}, 0, 1));
	}

	@Test void maxIdx_double() {
		double[] values = new double[100];
		values[1] = 120;
		for (int i = 0; i < 90; i++) {
			values[i + 10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(values, 10, 90));
	}

	@Test void maxIdx_double_extremes() {
		assertEquals(0, PrimitiveArrays.maxIdx(new double[]{-Double.MAX_VALUE}, 0, 1));
		assertEquals(0, PrimitiveArrays.maxIdx(new double[]{Double.MAX_VALUE}, 0, 1));
	}

	@Test void lowerBoundU_byte() {
		byte[] values = new byte[100];
		values[0] = 124;
		for (int i = 1; i < 100; i++) {
			values[i] = (byte)(i + 100);
		}
		assertEquals(51, PrimitiveArrays.lowerBoundU(values, 1, 99, 151));
		assertEquals(50, PrimitiveArrays.lowerBoundU(values, 1, 99, 150));
		assertEquals(49, PrimitiveArrays.lowerBoundU(values, 1, 99, 149));
		assertEquals(1, PrimitiveArrays.lowerBoundU(values, 1, 99, 0));
		assertEquals(100, PrimitiveArrays.lowerBoundU(values, 1, 99, 300));
	}

	@Test void lowerBound_byte() {
		byte[] values = new byte[100];
		values[0] = 124;
		for (int i = 1; i < 100; i++) {
			values[i] = (byte)(i - 50);
		}
		assertEquals(51, PrimitiveArrays.lowerBound(values, 1, 99, 1));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 0));
		assertEquals(49, PrimitiveArrays.lowerBound(values, 1, 99, -1));
		assertEquals(1, PrimitiveArrays.lowerBound(values, 1, 99, -100));
		assertEquals(100, PrimitiveArrays.lowerBound(values, 1, 99, 200));
	}

	@Test void lowerBound_short() {
		short[] values = new short[100];
		values[0] = 9999;
		for (int i = 1; i < 100; i++) {
			values[i] = (short)(i - 50);
		}
		assertEquals(51, PrimitiveArrays.lowerBound(values, 1, 99, 1));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 0));
		assertEquals(49, PrimitiveArrays.lowerBound(values, 1, 99, -1));
		assertEquals(1, PrimitiveArrays.lowerBound(values, 1, 99, -100));
		assertEquals(100, PrimitiveArrays.lowerBound(values, 1, 99, 200));
	}

	@Test void lowerBound_int() {
		int[] values = new int[100];
		values[0] = 99999;
		for (int i = 1; i < 100; i++) {
			values[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(values, 1, 99, 51));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 50));
		assertEquals(49, PrimitiveArrays.lowerBound(values, 1, 99, 49));
		assertEquals(1, PrimitiveArrays.lowerBound(values, 1, 99, -1));
		assertEquals(100, PrimitiveArrays.lowerBound(values, 1, 99, 200));
	}

	@Test void lowerBound_float() {
		float[] values = new float[100];
		values[0] = 99999;
		for (int i = 1; i < 100; i++) {
			values[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(values, 1, 99, 50.1f));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 50f));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 49.9f));
		assertEquals(1, PrimitiveArrays.lowerBound(values, 1, 99, -1f));
		assertEquals(100, PrimitiveArrays.lowerBound(values, 1, 99, 200f));
	}

	@Test void lowerBound_double() {
		double[] values = new double[100];
		values[0] = 99999;
		for (int i = 1; i < 100; i++) {
			values[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(values, 1, 99, 50.1));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 50));
		assertEquals(50, PrimitiveArrays.lowerBound(values, 1, 99, 49.9));
		assertEquals(1, PrimitiveArrays.lowerBound(values, 1, 99, -1));
		assertEquals(100, PrimitiveArrays.lowerBound(values, 1, 99, 200));
	}

	@Test void sumD_byte() {
		var array = new byte[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void sumD_short() {
		var array = new short[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void sumD_int() {
		var array = new int[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void sumD_long() {
		var array = new long[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void sumD_float() {
		var array = new float[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void sumD_double() {
		var array = new double[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.sumD(array, 0, 4));
		assertEquals(128.0, PrimitiveArrays.sumD(array, 1, 3));
	}

	@Test void feedbackIdxDOp_byte() {
		var array = new byte[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}

	@Test void feedbackIdxDOp_short() {
		var array = new short[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}

	@Test void feedbackIdxDOp_int() {
		var array = new int[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}

	@Test void feedbackIdxDOp_long() {
		var array = new long[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}

	@Test void feedbackIdxDOp_float() {
		var array = new float[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}

	@Test void feedbackIdxDOp_double() {
		var array = new double[]{5, -2, 10, 120};

		assertEquals(133.0, PrimitiveArrays.feedbackIdxDOp(array, 0, 4, ( idx, value, prior ) -> prior + value));
		assertEquals(128.0, PrimitiveArrays.feedbackIdxDOp(array, 1, 3, ( idx, value, prior ) -> prior + value));
	}
}