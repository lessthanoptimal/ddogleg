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

package org.ddogleg.optimization;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.loss.LossFunction;
import org.ddogleg.optimization.loss.LossFunctionGradient;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ejml.data.DMatrix;

/**
 * <p>
 *   A variant on {@link UnconstrainedLeastSquares} for solving large scale systems which can be simplified using the
 *   Schur Complement. The approximate Hessian matrix (J'*J) is assumed to have the
 *   following block triangle form: [A B;C D]. The system being solved for
 *   is as follows: [A B;C D] [x_1;x_2] = [b_1;b_2]. See {@link HessianSchurComplement_DSCC} for more details.

 * </p>
 *
 * @see HessianSchurComplement_DSCC
 * @see SchurJacobian
 *
 * @author Peter Abeles
 */
public interface UnconstrainedLeastSquaresSchur<S extends DMatrix>
		extends IterativeOptimization
{
	/**
	 * Specifies a set of functions and their Jacobian.  See class description for documentation
	 * on output data format.
	 *
	 * @param function Computes the output of M functions f<sub>i</sub>(x) which take in N fit parameters as input.
	 * @param jacobian Computes the Jacobian of the M functions and breaks it up into left and right components.
	 */
	void setFunction(FunctionNtoM function, SchurJacobian<S> jacobian);

	/**
	 * Specifies a specialized loss function, typically to improve robustness to outliers. Squared error is the default.
	 */
	void setLoss( LossFunction loss, LossFunctionGradient lossGradient );

	/**
	 * Specify the initial set of parameters from which to start from. Call after
	 * {@link #setFunction} has been called.
	 *
	 * @param initial Initial parameters or guess with N elements..
	 * @param ftol Relative threshold for change in function value between iterations. 0 &le; ftol &le; 1.  Try 1e-12
	 * @param gtol Absolute threshold for convergence based on the gradient's norm. 0 disables test.  0 &le; gtol.
	 *             Try 1e-12
	 */
	void initialize(double[] initial, double ftol, double gtol);
	// TODO consider adding scaling vector

	/**
	 * After each iteration this function can be called to get the current best
	 * set of parameters.
	 *
	 * @return parameters
	 */
	double[] getParameters();

	/**
	 * Returns the value of the objective function being evaluated at the current
	 * parameters value.  If not supported then an exception is thrown.
	 *
	 * @return Objective function's value.
	 */
	double getFunctionValue();
}
