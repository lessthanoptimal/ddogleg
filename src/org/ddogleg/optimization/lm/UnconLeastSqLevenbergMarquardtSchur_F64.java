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

import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement;
import org.ddogleg.optimization.math.MatrixMath;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

/**
 * Implementation of {@link LevenbergMarquardt_F64} for {@link UnconstrainedLeastSquaresSchur}.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class UnconLeastSqLevenbergMarquardtSchur_F64<S extends DMatrix>
		extends LevenbergMarquardt_F64<S, HessianSchurComplement<S>>
		implements UnconstrainedLeastSquaresSchur<S> {
	// Left and right side of the jacobian matrix
	public S jacLeft;
	public S jacRight;

	public FunctionNtoM functionResiduals;
	public SchurJacobian<S> functionJacobian;

	public UnconLeastSqLevenbergMarquardtSchur_F64( MatrixMath<S> math,
													HessianSchurComplement<S> hessian ) {
		super(math, hessian);

		this.jacLeft = math.createMatrix();
		this.jacRight = math.createMatrix();
	}

	@Override public void setFunction( FunctionNtoM function, SchurJacobian<S> jacobian ) {
		this.functionResiduals = function;
		this.functionJacobian = jacobian;
	}

	@Override public void initialize( double[] initial, double ftol, double gtol ) {
		config.ftol = ftol;
		config.gtol = gtol;

		super.initialize(initial, functionResiduals.getNumOfInputsN(), functionResiduals.getNumOfOutputsM());
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
											DMatrixRMaj gradient, HessianSchurComplement<S> hessian ) {
		if (!sameStateAsResiduals)
			functionResiduals.process(x.data, residuals.data);
		functionJacobian.process(x.data, jacLeft, jacRight);
		hessian.computeHessian(jacLeft, jacRight);
		hessian.computeGradient(jacLeft, jacRight, residuals, gradient);
	}

	@Override
	protected void computeResiduals( DMatrixRMaj x, DMatrixRMaj residuals ) {
		functionResiduals.process(x.data, residuals.data);
	}
}
