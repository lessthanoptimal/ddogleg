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

package org.ddogleg.optimization.math;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class StandardMatrixMathChecks<T extends DMatrix> {
	public abstract T convertA( DMatrixRMaj A );

	public abstract DMatrixRMaj convertB( T A );

	public abstract T create( int numRows, int numCols );

	Random rand = new Random(234);
	MatrixMath<T> alg;

	protected StandardMatrixMathChecks( MatrixMath<T> alg ) {
		this.alg = alg;
	}

	@Test void divideColumns() {
		DMatrixRMaj original = RandomMatrices_DDRM.rectangle(4, 4, -1, 1, rand);
		var v = new DMatrixRMaj(new double[][]{{0.5}, {0.75}, {2}, {0.9}});

		T _A = convertA(original);
		alg.divideColumns(v, _A);
		DMatrixRMaj A = convertB(_A);

		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				assertEquals(original.get(row, col)/v.data[col], A.get(row, col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test void multTransA() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6, 2, -1, 1, rand);
		DMatrixRMaj B = RandomMatrices_DDRM.rectangle(6, 4, -1, 1, rand);
		var expected = new DMatrixRMaj(1, 1);
		var found = new DMatrixRMaj(1, 1);

		CommonOps_DDRM.multTransA(A, B, expected);

		alg.multTransA(convertA(A), B, found);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, UtilEjml.TEST_F64));
	}

	@Test void createMatrix() {
		T a = alg.createMatrix();

		assertEquals(1, a.getNumCols());
		assertEquals(1, a.getNumRows());
	}
}
