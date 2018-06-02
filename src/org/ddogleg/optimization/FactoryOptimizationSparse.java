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

package org.ddogleg.optimization;

import org.ddogleg.optimization.impl.LevenbergDampened_DSCC;
import org.ddogleg.optimization.impl.LevenbergMarquardtDampened_DSCC;
import org.ddogleg.optimization.wrap.LevenbergDampened_to_UnconstrainedLeastSquares;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

/**
 * Factory for sparse optimization algorithms.  These implementations/interfaces
 * are designed to be easy to use and effective for most tasks.  If more control is needed then
 * create an implementation directly.
 *
 * @author Peter Abeles
 */
public class FactoryOptimizationSparse {

	/**
	 * <p>
	 * Unconstrained least squares Levenberg-Marquardt (LM) optimizer for dense problems.  There are many
	 * different variants of LM and this function provides an easy to use interface for selecting and
	 * configuring them.  Scaling of function parameters and output might be needed to ensure good results.
	 * </p>
	 *
	 * @param dampInit Initial value of dampening parameter.  Tune.  Start at around 1e-3.
	 * @param robust If true a slower, more robust algorithm that can handle more degenerate cases will be used.
	 * @return UnconstrainedLeastSquares
	 */
	public static UnconstrainedLeastSquares<DMatrixSparseCSC> leastSquaresLM(double dampInit ,
																			 boolean robust )
	{
		LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solver;

		if( robust ) {
			throw new IllegalArgumentException("Currently no sparse SVD in EJML. Maybe you can help?");
		} else {
			solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);
		}

		LevenbergMarquardtDampened_DSCC alg = new LevenbergMarquardtDampened_DSCC(solver,dampInit);
		return new LevenbergDampened_to_UnconstrainedLeastSquares<>(alg);
	}

	/**
	 * <p>
	 * Unconstrained least squares Levenberg optimizer for dense problems.
	 * </p>
	 *
	 * @param dampInit Initial value of dampening parameter.  Tune.  Start at around 1e-3.
	 * @return UnconstrainedLeastSquares
	 */
	public static UnconstrainedLeastSquares<DMatrixSparseCSC> leastSquareLevenberg( double dampInit )
	{
		LevenbergDampened_DSCC alg = new LevenbergDampened_DSCC(dampInit);
		return new LevenbergDampened_to_UnconstrainedLeastSquares<>(alg);
	}
}
