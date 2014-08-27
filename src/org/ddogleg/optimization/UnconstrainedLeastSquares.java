/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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
import org.ddogleg.optimization.functions.FunctionNtoMxN;

/**
 * <p>
 * Non-linear least squares problems have a special structure which can be taken advantage of for optimization.
 * The least squares problem is defined below:<br>
 * F(x) = 0.5*sum( i=1:M ; f<sub>i</sub>(x)<sup>2</sup> )<br>
 * where f<sub>i</sub>(x) is a function from &real;<sup>N</sup> to &real;. M is number of functions, and N
 * is number of fit parameters. M &ge; N<br>
 * f<sub>i</sub>(x) = observed - predicted, which is known as the residual error.
 * </p>
 *
 * <p>
 * Convergence is tested using the g-test, which is based off the gradient's norm.<br>
 * G-test:    gtol &le; ||g(x)||<sub>inf</sub><br>
 * A relative f-test is not provided since that test breaks down when the expected function output
 * is zero. An absolute f-test can be done by checking the value of {@link #getFunctionValue} in each iteration.
 * </p>
 *
 * <p>
 * NOTE: The function computes the M outputs of the f<sub>i</sub>(x), residual error functions, NOT
 * [f<sub>i</sub>(x)]<sup>2</sup>
 * </p>
 *
 * <p>
 * FORMATS:<br>
 * Input functions are specified using {@link org.ddogleg.optimization.functions.FunctionNtoM} for the set of M
 * functions, and {@link FunctionNtoMxN} for the Jacobian.  The function's output is a vector of length M,
 * where element i correspond to function i's output. The Jacobian is an array containing the partial
 * derivatives of each function.  Element J(i,j) corresponds to the partial of function i and parameter j.
 * The array is stored in a row major format.  The partial for F(i,j) would be stored at index = i*N+j in the data array.
 * </p>
 *
 * <p>
 * NOTE: If you need to modify the optimization parameters this can be done inside the 'function'.
 * </p>
 *
 * @author Peter Abeles
 */
public interface UnconstrainedLeastSquares extends IterativeOptimization {

	/**
	 * Specifies a set of functions and their Jacobian.  See class description for documentation
	 * on output data format.
	 *
	 * @param function Computes the output of M functions f<sub>i</sub>(x) which take in N fit parameters as input.
	 * @param jacobian Computes the Jacobian of the M functions.  If null a numerical Jacobian will be used.
	 */
	public void setFunction( FunctionNtoM function , FunctionNtoMxN jacobian );

	/**
	 * Specify the initial set of parameters from which to start from. Call after
	 * {@link #setFunction} has been called.
	 *
	 * @param initial Initial parameters or guess with N elements..
	 * @param ftol Relative threshold for change in function value between iterations. 0 &le; ftol &le; 1.  Try 1e-12
	 * @param gtol Absolute threshold for convergence based on the gradient's norm. 0 disables test.  0 &le; gtol.
	 *             Try 1e-12
	 */
	public void initialize( double initial[] , double ftol , double gtol );

	/**
	 * After each iteration this function can be called to get the current best
	 * set of parameters.
	 */
	public double[] getParameters();

	/**
	 * Returns the value of the objective function being evaluated at the current
	 * parameters value.  If not supported then an exception is thrown.
	 *
	 * @return Objective function's value.
	 */
	public double getFunctionValue();
}
