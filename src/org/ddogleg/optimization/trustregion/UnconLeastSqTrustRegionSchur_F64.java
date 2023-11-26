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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

/**
 * Implementations of {@link UnconstrainedLeastSquaresSchur}. Uses {@link HessianSchurComplement_DSCC}
 * to compute the Schur complement and perform all math related to the Hessian. The Hessian
 * is stored in a custom block format and the Hessian referenced in the parent class is ignored.
 * All functions which reference the original Hessian are overriden.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class UnconLeastSqTrustRegionSchur_F64<S extends DMatrix>
		extends TrustRegionLeastSqBase_F64<S, HessianSchurComplement<S>>
		implements UnconstrainedLeastSquaresSchur<S> {

	protected SchurJacobian<S> functionJacobian;

	// Left and right side of the jacobian matrix
	protected S jacLeft;
	protected S jacRight;

	public UnconLeastSqTrustRegionSchur_F64( ParameterUpdate<S> update,
											 HessianSchurComplement<S> hessian ) {
		super(update, hessian);

		jacLeft = hessian.createMatrix();
		jacRight = hessian.createMatrix();
	}

	@Override
	public void setFunction( FunctionNtoM function, SchurJacobian<S> jacobian ) {
		this.functionResiduals = function;
		this.functionJacobian = jacobian;
		residuals.reshape(jacobian.getNumOfOutputsM(), 1);
	}

	@Override
	public void initialize( double[] initial, double ftol, double gtol ) {
		this.initialize(initial, functionResiduals.getNumOfInputsN(), 0);
		config.ftol = ftol;
		config.gtol = gtol;
	}

	@Override
	protected void functionGradientHessian( DMatrixRMaj x, boolean sameStateAsCost,
											DMatrixRMaj gradient, HessianSchurComplement<S> hessian ) {
		if (!sameStateAsCost)
			functionResiduals.process(x.data, residuals.data);
		functionJacobian.process(x.data, jacLeft, jacRight);
		hessian.computeHessian(jacLeft, jacRight);

		if (lossFuncGradient != null) {
			lossFuncGradient.process(residuals.data, storageLossGradient.data);
			hessian.computeGradient(jacLeft, jacRight, storageLossGradient, gradient);
		} else {
			// Note: The residuals are the gradient of the squared error loss function
			hessian.computeGradient(jacLeft, jacRight, residuals, gradient);
		}
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
		return mode == Mode.COMPUTE_DERIVATIVES;
	}

	@Override
	public boolean isConverged() {
		return mode == Mode.CONVERGED;
	}

	public DMatrixRMaj getResiduals() {
		return residuals;
	}
}
