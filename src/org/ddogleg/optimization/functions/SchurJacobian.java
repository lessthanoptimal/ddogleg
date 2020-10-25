/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrix;

/**
 * Jacobian calculation for use in Schur Complement.
 *
 * @see org.ddogleg.optimization.UnconstrainedLeastSquaresSchur
 *
 * @author Peter Abeles
 */
public interface SchurJacobian<S extends DMatrix> extends FunctionInOut {
	/**
	 * <p>
	 * Processes the input vector to outputs two matrices. The matrices represent
	 * the jacobian split along one of its columns
	 * </p>
	 *
	 * <p>
	 * The user can modify the input parameters here and the optimizer must use those changes.
	 * </p>
	 *
	 * @param input Vector with input parameters.
	 * @param left (Output) left side of jacobian. Will be resized to fit.
	 * @param right (Output) right side of jacobian. Will be resized to fit.
	 */
	void process(double[] input, S left , S right );
}
