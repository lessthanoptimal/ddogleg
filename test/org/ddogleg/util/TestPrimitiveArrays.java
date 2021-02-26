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

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
class TestPrimitiveArrays {
	@Test void fillCount_input() {
		int[] orig = new int[100];
		PrimitiveArrays.fillCounting(orig,10,40);
		for (int i = 0; i < orig.length; i++) {
			if( i < 10 || i >= 50 ) {
				assertEquals(0,orig[i]);
			} else {
				assertEquals(i-10,orig[i]);
			}
		}
	}

	@Test void shuffle_byte() {
		Random rand = new Random(234);
		byte[] orig = new byte[100];
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)i;
		}
		PrimitiveArrays.shuffle(orig,10,90,rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if( Math.abs(orig[j+10]-i) == 0 ) {
					count++;
				}
			}
			assertEquals(1,count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if( Math.abs(orig[i+10]-i) == 0 ) {
				matches++;
			}
		}
		assertTrue(matches<10);
	}

	@Test void shuffle_short() {
		Random rand = new Random(234);
		short[] orig = new short[100];
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (short)i;
		}
		PrimitiveArrays.shuffle(orig,10,90,rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if( Math.abs(orig[j+10]-i) == 0 ) {
					count++;
				}
			}
			assertEquals(1,count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if( Math.abs(orig[i+10]-i) == 0 ) {
				matches++;
			}
		}
		assertTrue(matches<10);
	}

	@Test void shuffle_int() {
		Random rand = new Random(234);
		int[] orig = new int[100];
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		PrimitiveArrays.shuffle(orig,10,90,rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if( Math.abs(orig[j+10]-i) == 0 ) {
					count++;
				}
			}
			assertEquals(1,count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if( Math.abs(orig[i+10]-i) == 0 ) {
				matches++;
			}
		}
		assertTrue(matches<10);
	}

	@Test void shuffle_float() {
		Random rand = new Random(234);
		float[] orig = new float[100];
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		PrimitiveArrays.shuffle(orig,10,90,rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if( Math.abs(orig[j+10]-i) == 0 ) {
					count++;
				}
			}
			assertEquals(1,count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if( Math.abs(orig[i+10]-i) == 0 ) {
				matches++;
			}
		}
		assertTrue(matches<10);
	}

	@Test void shuffle_double() {
		Random rand = new Random(234);
		double[] orig = new double[100];
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		PrimitiveArrays.shuffle(orig,10,90,rand);

		// make sure each element still only appears once
		for (int i = 0; i < 90; i++) {
			int count = 0;
			for (int j = 0; j < 90; j++) {
				if( Math.abs(orig[j+10]-i) == 0 ) {
					count++;
				}
			}
			assertEquals(1,count);
		}

		// see if elements were shuffled around. If too many match
		// then something is wrong
		int matches = 0;
		for (int i = 0; i < 90; i++) {
			if( Math.abs(orig[i+10]-i) == 0 ) {
				matches++;
			}
		}
		assertTrue(matches<10);
	}

	@Test void min_byte() {
		byte[] orig = new byte[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)(i+2);
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void min_short() {
		short[] orig = new short[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (short)(i+2);
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void min_int() {
		int[] orig = new int[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void min_long() {
		long[] orig = new long[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void min_float() {
		float[] orig = new float[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void min_double() {
		double[] orig = new double[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(2, PrimitiveArrays.min(orig,10,90));
	}

	@Test void minIdx_byte() {
		byte[] orig = new byte[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)(i+2);
		}
		assertEquals(10, PrimitiveArrays.minIdx(orig,10,90));
	}

	@Test void minIdx_int() {
		int[] orig = new int[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(orig,10,90));
	}

	@Test void minIdx_float() {
		float[] orig = new float[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(orig,10,90));
	}

	@Test void minIdx_double() {
		double[] orig = new double[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i+2;
		}
		assertEquals(10, PrimitiveArrays.minIdx(orig,10,90));
	}

	@Test void max_byte() {
		byte[] orig = new byte[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void max_short() {
		short[] orig = new short[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (short)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void max_int() {
		int[] orig = new int[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void max_long() {
		long[] orig = new long[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (long)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void max_float() {
		float[] orig = new float[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void max_double() {
		double[] orig = new double[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test void maxIdx_byte() {
		byte[] orig = new byte[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(orig,10,90));
	}

	@Test void maxIdx_int() {
		int[] orig = new int[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(orig,10,90));
	}

	@Test void maxIdx_float() {
		float[] orig = new float[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(orig,10,90));
	}

	@Test void maxIdx_double() {
		double[] orig = new double[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(99, PrimitiveArrays.maxIdx(orig,10,90));
	}

	@Test void lowerBoundU_byte() {
		byte[] orig = new byte[100];
		orig[0] = 124;
		for (int i = 1; i < 100; i++) {
			orig[i] = (byte)(i+100);
		}
		assertEquals(51, PrimitiveArrays.lowerBoundU(orig,1,99,151));
		assertEquals(50, PrimitiveArrays.lowerBoundU(orig,1,99,150));
		assertEquals(49, PrimitiveArrays.lowerBoundU(orig,1,99,149));
		assertEquals(1, PrimitiveArrays.lowerBoundU(orig,1,99,0));
		assertEquals(100, PrimitiveArrays.lowerBoundU(orig,1,99,300));
	}

	@Test void lowerBound_byte() {
		byte[] orig = new byte[100];
		orig[0] = 124;
		for (int i = 1; i < 100; i++) {
			orig[i] = (byte)(i-50);
		}
		assertEquals(51, PrimitiveArrays.lowerBound(orig,1,99,1));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,0));
		assertEquals(49, PrimitiveArrays.lowerBound(orig,1,99,-1));
		assertEquals(1, PrimitiveArrays.lowerBound(orig,1,99,-100));
		assertEquals(100, PrimitiveArrays.lowerBound(orig,1,99,200));
	}

	@Test void lowerBound_short() {
		short[] orig = new short[100];
		orig[0] = 9999;
		for (int i = 1; i < 100; i++) {
			orig[i] = (short)(i-50);
		}
		assertEquals(51, PrimitiveArrays.lowerBound(orig,1,99,1));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,0));
		assertEquals(49, PrimitiveArrays.lowerBound(orig,1,99,-1));
		assertEquals(1, PrimitiveArrays.lowerBound(orig,1,99,-100));
		assertEquals(100, PrimitiveArrays.lowerBound(orig,1,99,200));
	}

	@Test void lowerBound_int() {
		int[] orig = new int[100];
		orig[0] = 99999;
		for (int i = 1; i < 100; i++) {
			orig[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(orig,1,99,51));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,50));
		assertEquals(49, PrimitiveArrays.lowerBound(orig,1,99,49));
		assertEquals(1, PrimitiveArrays.lowerBound(orig,1,99,-1));
		assertEquals(100, PrimitiveArrays.lowerBound(orig,1,99,200));
	}

	@Test void lowerBound_float() {
		float[] orig = new float[100];
		orig[0] = 99999;
		for (int i = 1; i < 100; i++) {
			orig[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(orig,1,99,50.1f));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,50f));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,49.9f));
		assertEquals(1, PrimitiveArrays.lowerBound(orig,1,99,-1f));
		assertEquals(100, PrimitiveArrays.lowerBound(orig,1,99,200f));
	}

	@Test void lowerBound_double() {
		double[] orig = new double[100];
		orig[0] = 99999;
		for (int i = 1; i < 100; i++) {
			orig[i] = i;
		}
		assertEquals(51, PrimitiveArrays.lowerBound(orig,1,99,50.1));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,50));
		assertEquals(50, PrimitiveArrays.lowerBound(orig,1,99,49.9));
		assertEquals(1, PrimitiveArrays.lowerBound(orig,1,99,-1));
		assertEquals(100, PrimitiveArrays.lowerBound(orig,1,99,200));
	}
}