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

package org.ddogleg.optimization.math;

import org.ejml.data.DMatrixRMaj;

/**
 * Abstraction layer for operations related to hessian matrix. This allows different internal representations
 * of the Hessian matrix to be used by the same code.
 *
 * @author Peter Abeles
 */
public interface HessianMath {

	/**
	 * Initialize Hessian to be in its initial state with the specified dimensions
	 * @param numParameters Number of optimization parameters. Hessian will be N by N
	 */
	void init( int numParameters );

	/**
	 * Returns v^T*M*v
	 *
	 * @param v vector
	 */
	double innerVectorHessian(DMatrixRMaj v );

	/**
	 * Extracts diagonal elements from the hessian and stores them in the vector diag
	 * @param diag vector
	 */
	void extractDiag( DMatrixRMaj diag);

	/**
	 * Applies row and column division using the scaling vector.
	 *
	 * B = inv(diag(s))*B*inv(diag(s))
	 *
	 * @param scaling
	 */
	void divideRowsCols(DMatrixRMaj scaling);

	/**
	 * Initializes the solver
	 * @return true if successful
	 */
	boolean initializeSolver();

	/**
	 * Solves the system
	 *
	 * step = inv(B)*Y
	 *
	 * @param Y (Input) vector
	 * @param step (output) vector
	 */
	boolean solve(DMatrixRMaj Y , DMatrixRMaj step );
}
