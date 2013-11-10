/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.functions;

/**
 * @author Peter Abeles
 */
public interface CoupledJacobian {

	/**
	 * Number of input parameters being optimized.
	 */
	public int getN();

	/**
	 * Number of functions.
	 */
	public int getM();

	/**
	 * Specifies the input parameters.  The user can modify these values and they will be modified inside the
	 * optimization function too.
	 *
	 * @param x Optimization parameters.
	 */
	public void setInput(double[] x);
	
	public void computeFunctions( double[] output );

	/**
	 * <p>
	 * Processes the input parameters into the 2D Jacobian matrix.  The matrix has a dimension of M rows and N columns
	 * and is formatted as a row major 1D-array.  EJML can be used to provide a matrix wrapper around
	 * the output array: DenseMatrix J = DenseMatrix.wrap(m,n,output);
	 * </p>
	 *
	 * <p>
	 * The user can modify the input parameters here and the optimizer must use those changes.
	 * </p>
	 *
	 * @param jacobian Row major array with M rows and N columns.
	 */
	public void computeJacobian( double[] jacobian );
}
