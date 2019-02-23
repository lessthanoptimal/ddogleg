/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

	@Test
	void fillCount_input() {
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

	@Test
	void shuffle_byte() {
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

	@Test
	void shuffle_short() {
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

	@Test
	void shuffle_int() {
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

	@Test
	void shuffle_float() {
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

	@Test
	void shuffle_double() {
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

	@Test
	void max_byte() {
		byte[] orig = new byte[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (byte)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test
	void max_short() {
		short[] orig = new short[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (short)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test
	void max_int() {
		int[] orig = new int[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test
	void max_long() {
		long[] orig = new long[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = (long)i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test
	void max_float() {
		float[] orig = new float[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}

	@Test
	void max_double() {
		double[] orig = new double[100];
		orig[1] = 120;
		for (int i = 0; i < 90; i++) {
			orig[i+10] = i;
		}
		assertEquals(89, PrimitiveArrays.max(orig,10,90));
	}
}