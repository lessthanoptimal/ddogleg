/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule;

import org.ddogleg.DDoglegJUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCRArray extends DDoglegJUnit {
	@Test void wrap() {
		var alg = new CRArray<>(new double[5], 1);

		var array = new double[6];
		alg.wrap(array, 3);

		assertSame(array, alg.a);
		assertEquals(3, alg.offset);
	}

	@Test void write() {
		var dest = new double[4];

		var alg = new CRArray<>(new double[]{1, 2, 3, 4, 5}, 1);
		alg.write(dest, 1, 2);

		assertArrayEquals(new double[]{0, 2, 3, 0}, dest);
	}

	@Test void read() {
		var src = new double[]{1, 2, 3, 4, 5};

		var alg = new CRArray<>(new double[4], 1);

		alg.read(src, 2, 3);

		assertArrayEquals(new double[]{0, 3, 4, 5}, alg.a);
	}

	@Test void setOffset() {
		var alg = new CRArray<>(new double[5], 1);
		assertSame(alg, alg.setOffset(5));

		assertEquals(5, alg.offset);
	}

	@Test void d_read() {
		var alg = new CRArray<>(new double[]{1, 2, 3, 4, 5}, 1);

		assertEquals(2, alg.d(0));
		assertEquals(3, alg.d(1));
	}

	@Test void d_write() {
		var alg = new CRArray<>(new double[]{1, 2, 3, 4, 5}, 1);

		alg.d(0, -1);
		alg.d(1, -2);

		assertArrayEquals(new double[]{1, -1, -2, 4, 5}, alg.a);
	}

	@Test void isEquals_double() {
		var arrayA = new CRArray<>(new double[]{1, 2, 3, 4, 5}, 1);
		var arrayB = new CRArray<>(new double[]{2, 3, 4, 6, 7}, 0);

		assertFalse(arrayA.isEquals(arrayB, 4, 1e-8));
		assertTrue(arrayA.isEquals(arrayB, 4, 1.001));
		assertTrue(arrayA.isEquals(arrayB, 3, 1e-8));

		// Make sure the sign on tolerance is used correctly
		arrayB.d(3, 4);
		assertFalse(arrayA.isEquals(arrayB, 4, 1e-8));
		assertTrue(arrayA.isEquals(arrayB, 4, 1.001));
	}

	@Test void copy() {
		var src = new CRArray<>(new double[]{1, 2, 3, 4, 5}, 1);
		CRArray<double[]> dst = src.copy();

		assertEquals(src.offset, dst.offset);
		assertArrayEquals(src.a, dst.a);
	}

	@Test void toStringD() {
		var alg = new CRArray<>(new double[]{1.5, -2.25, 7.0}, 0);
		assertFalse(alg.toStringD("%.2f", 3).isBlank());
	}
}
