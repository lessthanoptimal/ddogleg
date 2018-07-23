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
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.linsol.LinearSolver;

/**
 * Modification of {@link TrustRegionUpdateDogleg_F64} specifically for the least-squares problem. Instead of
 * solving using the Hessian it uses the Jacobian. This can have better numerical properties.  Less prone
 * to overflow issues and can handle nearly singular systems better.
 *
 * @author Peter Abeles
 */
public class TrustRegionUpdateDoglegLS_F64<S extends DMatrix> extends TrustRegionUpdateDogleg_F64<S> {

	private UnconLeastSqTrustRegion_F64<S> owner;

	/**
	 * Specifies internal algorithms
	 *
	 * @param solver Solver for tall matrices
	 */
	public TrustRegionUpdateDoglegLS_F64(LinearSolver<S, DMatrixRMaj> solver) {
		super(solver);
	}

	@Override
	public void initialize(TrustRegionBase_F64<S> owner, int numberOfParameters, double minimumFunctionValue) {
		super.initialize(owner, numberOfParameters, minimumFunctionValue);
		this.owner = (UnconLeastSqTrustRegion_F64)owner;
	}

	@Override
	protected void solveGaussNewtonPoint(DMatrixRMaj pointGN) {
		// Compute Gauss-Newton step
		if( !solver.setA(owner.getJacobian()) ) {
			throw new OptimizationException("Solver failed!");
		}
		solver.solve(owner.gradient, pointGN);
	}
}
