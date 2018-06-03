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

import org.ddogleg.optimization.impl.*;
import org.ddogleg.optimization.wrap.LevenbergDampened_to_UnconstrainedLeastSquares;
import org.ddogleg.optimization.wrap.QuasiNewtonBFGS_to_UnconstrainedMinimization;
import org.ddogleg.optimization.wrap.TrustRegionLeastSquares_to_UnconstrainedLeastSquares;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * Creates optimization algorithms using easy to use interfaces.  These implementations/interfaces
 * are designed to be easy to use and effective for most tasks.  If more control is needed then
 * create an implementation directly.
 *
 * @author Peter Abeles
 */
public class FactoryOptimization {
	/**
	 * <p>
	 * Creates a solver for the unconstrained minimization problem.  Here a function has N parameters
	 * and a single output.  The goal is the minimize the output given the function and its derivative.
	 * </p>
	 *
	 * @return UnconstrainedMinimization
	 */
	public static UnconstrainedMinimization unconstrained()
	{
		return new QuasiNewtonBFGS_to_UnconstrainedMinimization();
	}

	/**
	 * Returns an implementation of {@link QuasiNewtonBFGS} with {@link LineSearchMore94} for the internal line search.
	 * This is a specific implementation of {@link UnconstrainedMinimization} and allows full access to all
	 * tuning parameters.
	 *
	 * @return UnconstrainedMinimization
	 */
	public static QuasiNewtonBFGS_to_UnconstrainedMinimization createBfgsWithMore94() {
		return new QuasiNewtonBFGS_to_UnconstrainedMinimization();
	}

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
	public static UnconstrainedLeastSquares<DMatrixRMaj> leastSquaresLM( double dampInit ,
																		 boolean robust )
	{
		LinearSolverDense<DMatrixRMaj> solver;

		if( robust ) {
			solver = LinearSolverFactory_DDRM.pseudoInverse(true);
		} else {
			solver = LinearSolverFactory_DDRM.symmPosDef(10);
		}

		LevenbergMarquardtDampened_DDRM alg = new LevenbergMarquardtDampened_DDRM(solver,dampInit);
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
	public static UnconstrainedLeastSquares leastSquareLevenberg( double dampInit )
	{
		LevenbergDampened_DDRM alg = new LevenbergDampened_DDRM(dampInit);
		return new LevenbergDampened_to_UnconstrainedLeastSquares<>(alg);
	}

	/**
	 * Creates a trust region based optimization algorithm for least squares problem.
	 *
	 * @see org.ddogleg.optimization.impl.TrustRegionLeastSquares
	 *
	 * @param regionSize Maximum radius of the trust region.
	 * @param type The type of trust region
	 * @param robustSolver If true then a lower but more robust solver which can handle singularities, otherwise
	 *                     a much faster one if used.
	 * @return UnconstrainedLeastSquares
	 */
	public static UnconstrainedLeastSquares leastSquaresTrustRegion( double regionSize ,
																	 RegionStepType type ,
																	 boolean robustSolver )
	{
		TrustRegionStep stepAlg;

		switch( type ) {
			case CAUCHY:
				stepAlg = new CauchyStep();
				break;

			case DOG_LEG_F:
				if( robustSolver )
					stepAlg = new DoglegStepF(LinearSolverFactory_DDRM.pseudoInverse(true));
				else
					stepAlg = new DoglegStepF(LinearSolverFactory_DDRM.leastSquaresQrPivot(true, false));
				break;

			case DOG_LEG_FTF:
				if( robustSolver )
					stepAlg = new DoglegStepFtF(LinearSolverFactory_DDRM.pseudoInverse(true));
				else
					stepAlg = new DoglegStepFtF(LinearSolverFactory_DDRM.leastSquaresQrPivot(true, false));
				break;

			default:
				throw new IllegalArgumentException("Unknown type = "+type);
		}
		
		TrustRegionLeastSquares alg = new TrustRegionLeastSquares(regionSize,stepAlg);

		return new TrustRegionLeastSquares_to_UnconstrainedLeastSquares(alg);
	}
}
