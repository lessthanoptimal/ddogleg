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

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.loss.LossFunction;
import org.ddogleg.optimization.loss.LossFunctionGradient;
import org.ddogleg.optimization.loss.LossSquared;
import org.ddogleg.optimization.math.HessianMath;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all least squares trust region implementations.
 */
public abstract class TrustRegionLeastSqBase_F64<S extends DMatrix, HM extends HessianMath>
		extends TrustRegionBase_F64<S, HM> {

	protected FunctionNtoM functionResiduals;

	// difference between observations and estimate value from model
	protected DMatrixRMaj residuals = new DMatrixRMaj(1, 1);

	/** Given the residuals it computes the "Loss" or cost */
	protected LossFunction lossFunc = new LossSquared();

	/** Gradient of the loss function. If null then squared error is assumed and this step can be skipped. */
	protected @Nullable LossFunctionGradient lossFuncGradient;

	// Storage for the loss gradient
	protected DMatrixRMaj storageLossGradient = new DMatrixRMaj();

	protected TrustRegionLeastSqBase_F64( ParameterUpdate<S> parameterUpdate, HM hessian ) {
		super(parameterUpdate, hessian);
	}

	/**
	 * Specifies the loss function.
	 */
	public void setLoss( LossFunction loss, LossFunctionGradient lossGradient ) {
		this.lossFunc = loss;
		this.lossFuncGradient = lossGradient;
	}

	@Override protected double computeCostAtInitialization() {
		// need residuals to fixate and compute the cost
		functionResiduals.process(x.data, residuals.data);

		int numberOfFunctions = residuals.numRows;
		storageLossGradient.reshape(numberOfFunctions, 1);
		lossFunc.setNumberOfFunctions(numberOfFunctions);
		if (lossFuncGradient != null)
			lossFuncGradient.setNumberOfFunctions(numberOfFunctions);
		lossFunc.fixate(residuals.data);
		return lossFunc.process(residuals.data);
	}

	@Override protected boolean acceptNewState( boolean converged, double fx_candidate ) {
		// If the loss function is dynamic then update the cost function and the cost it will try to beat next time
		if (lossFunc.fixate(residuals.data)) {
			fx_candidate = lossFunc.process(residuals.data);
		}
		return super.acceptNewState(converged, fx_candidate);
	}

	@Override
	protected double cost( DMatrixRMaj x ) {
		functionResiduals.process(x.data, residuals.data);
		return lossFunc.process(residuals.data);
	}
}
