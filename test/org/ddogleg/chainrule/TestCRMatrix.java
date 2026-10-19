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

public class TestCRMatrix extends DDoglegJUnit {
	/** Replaces the reflection check in the base class, which can't create a CRMatrix with data to copy */
	@Test public void setTo() {
		var src = CRMatrix.createD(2, 3);
		for (int i = 0; i < 6; i++) {
			src.data[i] = i + 1;
		}
		var dst = CRMatrix.createD(1, 1);
		assertSame(dst, dst.setTo(src));
		assertEquals(2, dst.rows);
		assertEquals(3, dst.cols);
		for (int i = 0; i < 6; i++) {
			assertEquals(i + 1, dst.d(i/3, i%3));
		}
	}

	@Test void wrap() {
		var alg = new CRMatrix<>(new double[7], 0, 0, 0);

		var array = new double[8];
		alg.wrap(array, 1, 2, 3);

		assertSame(array, alg.data);
		assertEquals(1, alg.offset);
		assertEquals(2, alg.rows);
		assertEquals(3, alg.cols);
	}

	@Test void write() {
		var dst = new double[10];

		var alg = new CRMatrix<>(sequence(10), 1, 2, 3);
		alg.write(dst, 2);

		assertArrayEquals(new double[]{0, 0, 2, 3, 4, 5, 6, 7, 0, 0}, dst);
	}

	@Test void read() {
		double[] src = sequence(8);

		var alg = new CRMatrix<>(new double[9], 1, 2, 3);
		alg.read(src, 2);

		assertArrayEquals(new double[]{0, 3, 4, 5, 6, 7, 8, 0, 0}, alg.data);
	}

	@Test void setOffset() {
		var alg = new CRMatrix<>(new double[7], 0, 0, 0);
		assertSame(alg, alg.setOffset(5));

		assertEquals(5, alg.offset);
	}

	@Test void d_read() {
		var alg = new CRMatrix<>(sequence(10), 1, 3, 2);

		assertEquals(2, alg.d(0, 0));
		assertEquals(3, alg.d(0, 1));
		assertEquals(4, alg.d(1, 0));
	}

	@Test void d_write() {
		var alg = new CRMatrix<>(sequence(10), 1, 3, 2);

		alg.d(0, 0, -2);
		alg.d(0, 1, -3);
		alg.d(1, 0, -4);

		assertArrayEquals(new double[]{1, -2, -3, -4, 5, 6, 7, 8, 9, 10}, alg.data);
	}

	@Test void isEquals() {
		var matrixA = new CRMatrix<>(sequence(10), 1, 2, 3);
		var matrixB = new CRMatrix<>(sequence(10), 1, 2, 3);

		matrixB.d(1, 2, 6);

		assertFalse(matrixA.isEquals(matrixB, 1e-8));
		assertTrue(matrixA.isEquals(matrixB, 1.001));

		// Make sure the sign on tolerance is used correctly
		matrixB.d(1, 2, 8);
		assertFalse(matrixA.isEquals(matrixB, 1e-8));
		assertTrue(matrixA.isEquals(matrixB, 1.001));
	}

	@Test void isNaN_double() {
		var matrix = new CRMatrix<>(sequence(10), 1, 2, 3);
		assertFalse(matrix.isNaN());

		// add NaN before it starts
		matrix.data[0] = Double.NaN;
		assertFalse(matrix.isNaN());

		// add a point in the matrix
		matrix.d(1, 0, Double.NaN);
		assertTrue(matrix.isNaN());
	}

	@Test void isNaN_float() {
		var matrix = new CRMatrix<>(sequenceF(10), 1, 2, 3);
		assertFalse(matrix.isNaN());

		// add NaN before it starts
		matrix.data[0] = Float.NaN;
		assertFalse(matrix.isNaN());

		// add a point in the matrix
		matrix.f(1, 0, Float.NaN);
		assertTrue(matrix.isNaN());
	}

	@Test void copy() {
		var src = new CRMatrix<>(sequence(10), 1, 2, 3);
		CRMatrix<double[]> dst = src.copy();

		assertEquals(src.offset, dst.offset);
		assertEquals(src.rows, dst.rows);
		assertEquals(src.cols, dst.cols);
		assertArrayEquals(src.data, dst.data);
	}

	@Test void elementCount() {
		var a = new CRMatrix<>(new double[5], 1, 2, 3);
		assertEquals(6, a.elementCount());
	}

	@Test void shape() {
		var a = new CRMatrix<>(new double[5], 1, 2, 3);
		a.shape(6, 4);
		assertEquals(6, a.rows);
		assertEquals(4, a.cols);
	}

	@Test void toStringD() {
		var alg = new CRMatrix<>(sequence(6), 0, 2, 3);
		assertFalse(alg.toStringD("%.2f").isBlank());
	}

	@Test void toStringM() {
		var alg = new CRMatrix<>(sequence(6), 0, 2, 3);
		alg.d(1, 1, 0.0); // exercise the zero -> dot branch
		assertFalse(alg.toStringM("%.2f").isBlank());
	}

	private double[] sequence( int length ) {
		var out = new double[length];
		for (int i = 0; i < length; i++) {
			out[i] = i + 1;
		}
		return out;
	}

	private float[] sequenceF( int length ) {
		var out = new float[length];
		for (int i = 0; i < length; i++) {
			out[i] = i + 1;
		}
		return out;
	}
}
