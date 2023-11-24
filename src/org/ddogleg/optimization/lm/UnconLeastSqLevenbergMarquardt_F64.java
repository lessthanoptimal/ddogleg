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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.FactoryNumericalDerivative;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.math.HessianLeastSquares;
import org.ddogleg.optimization.math.MatrixMath;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ReshapeMatrix;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link LevenbergMarquardt_F64} for {@link UnconstrainedLeastSquares}.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class UnconLeastSqLevenbergMarquardt_F64<S extends DMatrix>
		extends LevenbergMarquardt_F64<S, HessianLeastSquares<S>>
		implements UnconstrainedLeastSquares<S> {
	S jacobian;

	protected FunctionNtoM functionResiduals;
	protected FunctionNtoMxN<S> functionJacobian;

	public UnconLeastSqLevenbergMarquardt_F64( MatrixMath<S> math,
											   HessianLeastSquares<S> hessian) {
		super(math, hessian);
		this.jacobian = math.createMatrix();
	}

	@Override public void setFunction( FunctionNtoM function, @Nullable FunctionNtoMxN<S> jacobian ) {
		this.functionResiduals = function;
		if (jacobian == null)
			this.functionJacobian = FactoryNumericalDerivative.jacobianForwards(function, (Class)this.jacobian.getClass());
		else
			this.functionJacobian = jacobian;
		int M = functionResiduals.getNumOfOutputsM();
		int N = functionResiduals.getNumOfInputsN();

		((ReshapeMatrix)this.jacobian).reshape(M, N);
	}

	@Override public void initialize( double[] initial, double ftol, double gtol ) {
		config.ftol = ftol;
		config.gtol = gtol;

		super.initialize(initial,
				functionResiduals.getNumOfInputsN(),
				functionResiduals.getNumOfOutputsM());
	}

	@Override public double[] getParameters() {
		return x.data;
	}

	@Override public double getFunctionValue() {
		return fx;
	}

	@Override public boolean isUpdated() {
		return mode == Mode.COMPUTE_DERIVATIVES;
	}

	@Override public boolean isConverged() {
		return mode == Mode.CONVERGED;
	}

	@Override
	protected void functionGradientHessian( DMatrixRMaj x, boolean sameStateAsResiduals,
											DMatrixRMaj gradient, HessianLeastSquares<S> hessian ) {
		if (!sameStateAsResiduals)
			functionResiduals.process(x.data, residuals.data);
		functionJacobian.process(x.data, jacobian);

		hessian.updateHessian(jacobian);
		if (lossFuncGradient != null) {
			lossFuncGradient.process(residuals.data, storageLossGradient.data);
			math.multTransA(jacobian, storageLossGradient, gradient);
		} else {
			// Note: The residuals are the gradient of the squared error loss function
			math.multTransA(jacobian, residuals, gradient);
		}
	}

	@Override
	protected void computeResiduals( DMatrixRMaj x, DMatrixRMaj residuals ) {
		functionResiduals.process(x.data, residuals.data);
	}
}
