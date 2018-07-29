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

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ReshapeMatrix;
import org.ejml.dense.row.SpecializedOps_DDRM;

/**
 * Implementations of {@link TrustRegionUpdateCauchy_F64} for {@link UnconstrainedLeastSquares}.
 *
 * @author Peter Abeles
 */
public class UnconLeastSqTrustRegion_F64<S extends DMatrix>
		extends TrustRegionBase_F64<S>
		implements UnconstrainedLeastSquares<S>
{
	protected DMatrixRMaj tmpM0 = new DMatrixRMaj(1,1);
	protected DMatrixRMaj residuals = new DMatrixRMaj(1,1);
	protected S jacobian;
	protected DMatrixRMaj gradientPrevious = new DMatrixRMaj(1,1);

	protected FunctionNtoM functionResiduals;
	protected FunctionNtoMxN<S> functionJacobian;

	public UnconLeastSqTrustRegion_F64(ParameterUpdate<S> parameterUpdate, MatrixMath<S> math) {
		super(parameterUpdate, math);
		jacobian = math.createMatrix();
	}

	@Override
	public void setFunction(FunctionNtoM function, FunctionNtoMxN<S> jacobian) {
		this.functionResiduals = function;
		this.functionJacobian = jacobian;
	}

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * f-test : ftol &le; || r(x+p) || infinity
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	@Override
	protected boolean checkConvergenceFTest(double fx, double fx_prev ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		for (int i = 0; i < residuals.numRows; i++) {
			if( Math.abs(residuals.data[i]) > config.ftol )
				return false;
		}
		return true;
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		this.initialize(initial,functionResiduals.getNumOfInputsN(),0);
		config.ftol = ftol;
		config.gtol = gtol;
	}

	@Override
	public void initialize(double[] initial, int numberOfParameters, double minimumFunctionValue) {
		int M = functionResiduals.getNumOfOutputsM();
		int N = functionResiduals.getNumOfInputsN();
		tmpM0.reshape(M,1);
		residuals.reshape(M,1);

		// Set the hessian to identity. There are other potentially better methods
		((ReshapeMatrix)hessian).reshape(numberOfParameters,numberOfParameters);
		math.setIdentity(hessian);
		// set the previous gradient to zero
		gradientPrevious.reshape(numberOfParameters,1);
		gradientPrevious.zero();
		((ReshapeMatrix)jacobian).reshape(M,N);

		super.initialize(initial, numberOfParameters, minimumFunctionValue);
	}

	@Override
	protected double cost(DMatrixRMaj x) {
		functionResiduals.process(x.data,residuals.data);
		return 0.5*SpecializedOps_DDRM.elementSumSq(residuals);
	}

	@Override
	protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient, S hessian) {
		if( !sameStateAsCost )
			functionResiduals.process(x.data,residuals.data);
		functionJacobian.process(x.data,jacobian);
		math.innerMatrixProduct(jacobian,hessian);
		math.multTransA(jacobian, residuals, gradient);
	}

	@Override
	protected void applyScaling() {
		super.applyScaling();
		// Apply scaling to the Jacobian matrix
		math.divideColumns(scaling.data,jacobian);
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


	public DMatrixRMaj getResiduals() {
		return residuals;
	}

	public S getJacobian() {
		return jacobian;
	}
}
