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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.impl.LevenbergBase;
import org.ddogleg.optimization.impl.NumericalJacobianForward;
import org.ejml.data.DMatrix;

/**
 * Wrapper around {@link org.ddogleg.optimization.impl.LevenbergMarquardtDampened} for {@link org.ddogleg.optimization.UnconstrainedLeastSquares}
 *
 * @author Peter Abeles
 */
public class LevenbergDampened_to_UnconstrainedLeastSquares<S extends DMatrix>
		implements UnconstrainedLeastSquares<S> {

	private LevenbergBase<S> alg;

	public LevenbergDampened_to_UnconstrainedLeastSquares(LevenbergBase<S> alg) {
		this.alg = alg;
	}

	@Override
	public void setFunction(FunctionNtoM function, FunctionNtoMxN jacobian) {

		if( jacobian == null )
			jacobian = new NumericalJacobianForward(function);

		alg.setFunction(new Individual_to_CoupledJacobian(function,jacobian));
	}

	@Override
	public void initialize(double[] initial, double ftol , double gtol) {
		alg.setConvergence(ftol,gtol);
		alg.initialize(initial);
	}

	@Override
	public double[] getParameters() {
		return alg.getParameters();
	}

	@Override
	public boolean iterate() throws OptimizationException {
		return alg.iterate();
	}

	@Override
	public boolean isConverged() {
		return alg.isConverged();
	}

	@Override
	public String getWarning() {
		return alg.getMessage();
	}

	@Override
	public double getFunctionValue() {
		return alg.getFnorm();
	}

	@Override
	public boolean isUpdated() {
		return alg.isUpdatedParameters();
	}
}
