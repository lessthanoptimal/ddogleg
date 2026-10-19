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

import org.ejml.UtilEjml;

import java.util.Arrays;
import java.util.Random;

/// Various math operations that can be formed on [CRMatrix]
public class MathCRMatrixD implements MathCRMatrix<double[]> {
	/// Performs a matrix multiplication between a and b. Results are stored in 'out'. The shape of 'out' is
	/// dynamically computed and adjusted.
	@Override public void mult( CRMatrix<double[]> a, CRMatrix<double[]> b, CRMatrix<double[]> out ) {
		if (a.cols != b.rows)
			throw new IllegalArgumentException("Incompatible matrices. (" + a.rows + "x" + a.cols + ") and (" + b.rows + "x" + b.cols + ")");

		// Reshape the output matrix
		if (out.offset == 0) {
			out.reshape(a.rows, b.cols);
		} else {
			// can't allocate more memory if offset isn't 0
			out.rows = a.rows;
			out.cols = b.cols;
		}

		int offA = a.offset;
		int offB = b.offset;
		int offOut = out.offset;

		for (int i = 0; i < a.rows; i++) {
			for (int j = 0; j < b.cols; j++) {
				double sum = 0.0;
//                for (int k = 0; k < a.cols; k++) {
//                    sum += a.data[offA + i*a.cols + k]*b.data[offB + k*b.cols + j];
//                }
				// Ugly code below is functionally the same as above. Reduces number of integer multiplications
				// This modification reduced runtime in one scenario from 182s to 156s
				// Unrolling for specific matrix sizes is probably next step in optimizing
				int idxA = offA + i*a.cols;
				int idxAEnd = idxA + a.cols;
				int idxB = offB + j;
				while (idxA < idxAEnd) {
					sum += a.data[idxA++]*b.data[idxB];
					idxB += b.cols;
				}
				out.data[offOut++] = sum;
			}
		}
	}

	@Override public void addTo( CRMatrix<double[]> dst, CRMatrix<double[]> src ) {
		if (dst.rows != src.rows || dst.cols != src.cols)
			throw new IllegalArgumentException("Shape mismatch. (" + dst.rows + "x" + dst.cols
					+ ") and (" + src.rows + "x" + src.cols + ")");

		int total = dst.rows*dst.cols;
		for (int i = 0; i < total; i++) {
			dst.data[dst.offset + i] += src.data[src.offset + i];
		}
	}

	@Override public void diag( double value, CRMatrix<double[]> m ) {
		for (int i = 0; i < Math.min(m.rows, m.cols); i++) {
			m.data[i*m.cols + i] = value;
		}
	}

	@Override public void fill( CRMatrix<double[]> m, double value ) {
		Arrays.fill(m.data, m.offset, m.offset + m.elementCount(), value);
	}

	@Override public void zero( CRArray<double[]> m, int length ) {
		Arrays.fill(m.a, m.offset, m.offset + length, 0);
	}

	/// Fills the matrix with uniform random values.
	///
	/// @param min Minimum value from RNG
	/// @param max Maximum value from RNG
	/// @param rand The random number generate
	/// @param output Matrix to fill in with random values
	@Override public void random( Random rand, double min, double max, CRMatrix<double[]> output ) {
		int index = output.offset;
		int N = output.rows*output.cols;

		for (int i = 0; i < N; i++) {
			output.data[index + i] = min + rand.nextDouble()*(max - min);
		}
	}

	/// Creates a new random matrix
	@Override public CRMatrix<double[]> random( Random rand, double min, double max, int rows, int cols ) {
		CRMatrix<double[]> m = CRMatrix.createD(rows, cols);
		random(rand, min, max, m);
		return m;
	}

	@Override public CRArray<double[]> random( Random rand, double min, double max, int length ) {
		CRArray<double[]> m = new CRArray<>(new double[length], 0);
		for (int i = 0; i < length; i++) {
			m.a[i] = rand.nextDouble()*(max - min) + min;
		}
		return m;
	}

	@Override public boolean isUncountable( CRMatrix<double[]> m ) {
		for (int row = 0; row < m.rows; row++) {
			for (int col = 0; col < m.cols; col++) {
				if (UtilEjml.isUncountable(m.d(row, col)))
					return true;
			}
		}
		return false;
	}

	@Override public CRArray<double[]> createArray( int length ) {
		return new CRArray<>(new double[length], 0);
	}

	@Override public CRMatrix<double[]> createMatrix( int rows, int cols ) {
		return new CRMatrix<>(new double[rows*cols], 0, rows, cols);
	}
}
