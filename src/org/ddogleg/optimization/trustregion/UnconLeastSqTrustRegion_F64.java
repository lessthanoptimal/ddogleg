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

import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ReshapeMatrix;
import org.ejml.dense.row.NormOps_DDRM;

/**
 * Implementations of {@link TrustRegionUpdateCauchy_F64} for {@link UnconstrainedLeastSquares}.
 *
 * @author Peter Abeles
 */
public abstract class UnconLeastSqTrustRegion_F64<S extends DMatrix>
		extends TrustRegionBase_F64<S>
		implements UnconstrainedLeastSquares<S>
{
	protected DMatrixRMaj tmpM0 = new DMatrixRMaj(1,1);
	protected DMatrixRMaj residuals = new DMatrixRMaj(1,1);
	protected S jacobian;
	protected DMatrixRMaj gradientPrevious = new DMatrixRMaj(1,1);

	protected FunctionNtoM functionResiduals;
	protected FunctionNtoMxN<S> functionJacobian;

	public UnconLeastSqTrustRegion_F64(ParameterUpdate parameterUpdate, MatrixMath<S> math) {
		super(parameterUpdate, math);
		jacobian = math.createMatrix();
	}

	@Override
	public void setFunction(FunctionNtoM function, FunctionNtoMxN<S> jacobian) {
		this.functionResiduals = function;
		this.functionJacobian = jacobian;
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		super.initialize(initial,ftol,gtol, functionResiduals.getNumOfInputsN(),0);

		int M = functionResiduals.getNumOfOutputsM();
		int N = functionResiduals.getNumOfInputsN();

		tmpM0.reshape(M,1);
		residuals.reshape(M,1);
		((ReshapeMatrix)jacobian).reshape(M,N);
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol,
						   int numberOfParameters, double minFunctionValue) {
		super.initialize(initial, ftol, gtol, numberOfParameters,minFunctionValue);

		// Set the hessian to identity. There are other potentially better methods
//		hessian.reshape(numberOfParameters,numberOfParameters);
//		CommonOps_DDRM.setIdentity(hessian);

		// set the previous gradient to zero
		gradientPrevious.reshape(numberOfParameters,1);
		gradientPrevious.zero();
	}


	@Override
	public double[] getParameters() {
		return x.data;
	}

	@Override
	public double getFunctionValue() {
		return fx;
	}

	@Override
	public boolean isUpdated() {
		return mode == Mode.FULL_STEP;
	}

	@Override
	public boolean isConverged() {
		return mode == Mode.CONVERGED;
	}

	@Override
	public String getWarning() {
		return null;
	}

	@Override
	protected void updateDerivedState(DMatrixRMaj x) {
		functionResiduals.process(x.data,residuals.data);
		functionJacobian.process(x.data,jacobian);
		math.innerMatrixProduct(jacobian,hessian);
	}

	@Override
	protected double costFunction(DMatrixRMaj x) {
		functionResiduals.process(x.data,tmpM0.data);
		return NormOps_DDRM.normF(tmpM0);
	}

	public DMatrixRMaj getResiduals() {
		return residuals;
	}

	public S getJacobian() {
		return jacobian;
	}
}
