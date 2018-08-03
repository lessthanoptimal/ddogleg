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

package org.ddogleg.optimization.trustregion;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

/**
 * @author Peter Abeles
 */
public interface OptimizationMath<S extends DMatrix> {
	/**
	 * Returns v^T*M*v
	 *
	 * @param v vector
	 * @param M square matrix
	 */
	double innerProductVectorMatrix(DMatrixRMaj v, S M);

	/**
	 * Sets the provided matrix to identity
	 */
	void setIdentity(S matrix);

	/**
	 * output = A'*A
	 */
	void innerMatrixProduct(S A, S output);

	void extractDiag(S A, double diag[]);

	void divideRows(double scaling[], S A);

	void divideColumns(double scaling[], S A);

	void scaleRows(double scaling[], S A);

	void scaleColumns(double scaling[], S A);

	void multTransA(S A, DMatrixRMaj B, DMatrixRMaj output);

	S createMatrix();
}
