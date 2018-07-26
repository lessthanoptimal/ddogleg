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

import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.impl.EquationsBFGS;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * Implementations of {@link TrustRegionUpdateCauchy_F64} for {@link UnconstrainedMinimization}. The hessian is approximated
 * using the {@link EquationsBFGS BFGS} method.
 *
 * TODO Update so that it can handle sparse system. BFGS
 *
 * @author Peter Abeles
 */
public class UnconMinTrustRegionBFGS_F64
		extends TrustRegionBase_F64<DMatrixRMaj>
		implements UnconstrainedMinimization
{
	// temp variable of length N
	private DMatrixRMaj tmpN0 = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN1 = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN2 = new DMatrixRMaj(1,1);
	private DMatrixRMaj gradientPrevious = new DMatrixRMaj(1,1);

	private FunctionNtoS functionCost;
	private FunctionNtoN functionGradient;

	private double minimumFunctionValue;

	// true if it's the first iteration
	private boolean firstIteration;

	public UnconMinTrustRegionBFGS_F64(ParameterUpdate parameterUpdate) {
		super(parameterUpdate, new TrustRegionMath_DDRM());
	}

	@Override
	public void setFunction(FunctionNtoS function, FunctionNtoN gradient, double minFunctionValue) {
		this.functionCost = function;
		this.functionGradient = gradient;
		this.minimumFunctionValue = minFunctionValue;
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		this.initialize(initial,functionCost.getNumOfInputsN(), minimumFunctionValue);
		config.ftol = ftol;
		config.gtol = gtol;
	}

	/**
	 * Override parent to initialize matrices
	 */
	@Override
	public void initialize(double[] initial, int numberOfParameters, double minimumFunctionValue) {
		super.initialize(initial, numberOfParameters,minimumFunctionValue);
		tmpN0.reshape(numberOfParameters,1);
		tmpN1.reshape(numberOfParameters,1);
		tmpN2.reshape(numberOfParameters,1);

		// Set the hessian to identity. There are other potentially better methods
		math.setIdentity(hessian);

		// set the previous gradient to zero
		gradientPrevious.reshape(numberOfParameters,1);
		gradientPrevious.zero();

		firstIteration = true;
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
		functionGradient.process(x.data, gradient.data);

		if( !firstIteration ) {
			if( isScaling() ) {
				// undo the scaling which was previous applied to the hessian
				// The gradient was just computed so it's not scaled yet
				math.scaleColumns(scaling.data, hessian);
				math.scaleRows(scaling.data, hessian);
			}

			// compute the change in Gradient
			CommonOps_DDRM.subtract(gradient, gradientPrevious, tmpN0);

			// Apply DFP equation and update H
			EquationsBFGS.update(hessian, p, tmpN0, tmpN1, tmpN2);
		} else {
			firstIteration = false;
		}

		// save the new gradient
		gradientPrevious.set(gradient);
	}

	@Override
	protected double costFunction(DMatrixRMaj x) {
		return functionCost.process(x.data);
	}

}
