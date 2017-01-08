/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;

/**
 * Base class for Levenberg solvers which use dense matrices.
 *
 * @author Peter Abeles
 */
public abstract class LevenbergDenseBase extends LevenbergBase {

	// jacobian at x
	protected RowMatrix_F64 jacobianVals = new RowMatrix_F64(1,1);

	// Jacobian inner product. Used to approximate Hessian
	// B=J'*J
	protected RowMatrix_F64 B = new RowMatrix_F64(1,1);
	// diagonal elements of JtJ
	protected RowMatrix_F64 Bdiag = new RowMatrix_F64(1,1);

	// Least-squares Function being optimized
	protected CoupledJacobian function;

	public LevenbergDenseBase(double initialDampParam) {
		super(initialDampParam);
	}

	@Override
	protected void setFunctionParameters(double[] param) {
		function.setInput(param);
	}

	@Override
	protected void computeResiduals(double[] output) {
		function.computeFunctions(output);
	}

	@Override
	protected double getMinimumDampening() {
		return CommonOps_R64.elementMax(Bdiag);
	}

	/**
	 * Specifies function being optimized.
	 *
	 * @param function Computes residuals and Jacobian.
	 */
	public void setFunction( CoupledJacobian function ) {
		internalInitialize(function.getN(),function.getM());
		this.function = function;

		jacobianVals.reshape(M,N);

		B.reshape(N, N);
		Bdiag.reshape(N,1);
	}
}
