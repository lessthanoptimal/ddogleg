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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * Base class for Levenberg solvers which use dense matrices.
 *
 * @author Peter Abeles
 */
public abstract class LevenbergBase_DDRM extends LevenbergFuncBase<DMatrixRMaj> {

	// jacobian at x
	protected DMatrixRMaj jacobianVals = new DMatrixRMaj(1,1);

	// Jacobian inner product. Used to approximate Hessian
	// B=J'*J
	protected DMatrixRMaj B = new DMatrixRMaj(1,1);

	public LevenbergBase_DDRM(double initialDampParam) {
		super(initialDampParam);
	}

	@Override
	protected double getMinimumDampening() {
		return CommonOps_DDRM.elementMax(Bdiag);
	}

	/**
	 * Specifies function being optimized.
	 *
	 * @param function Computes residuals and Jacobian.
	 */
	@Override
	public void setFunction( CoupledJacobian<DMatrixRMaj> function ) {
		internalInitialize(function.getNumOfInputsN(),function.getNumOfOutputsM());
		this.function = function;

		jacobianVals.reshape(M,N);

		B.reshape(N, N);
		Bdiag.reshape(N,1);
	}
}
