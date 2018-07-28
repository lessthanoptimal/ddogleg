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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * Modification of {@link TrustRegionUpdateDogleg_F64} which takes advantage of BFGS computing the inverse hessian
 * and avoids a matrix decomposition.
 *
 * @author Peter Abeles
 */
public class TrustRegionUpdateDoglegBFGS_F64 extends TrustRegionUpdateDogleg_F64<DMatrixRMaj> {

	// TODO do least-squares ftol test

	private UnconMinTrustRegionBFGS_F64 owner;


	@Override
	public void initialize(TrustRegionBase_F64<DMatrixRMaj> owner, int numberOfParameters, double minimumFunctionValue) {
		super.initialize(owner, numberOfParameters, minimumFunctionValue);
		this.owner = (UnconMinTrustRegionBFGS_F64)owner;
		this.owner .setComputeInverse(true);
	}

	@Override
	protected boolean solveGaussNewtonPoint(DMatrixRMaj pointGN) {
		CommonOps_DDRM.mult(owner.hessianInverse,owner.gradient,pointGN);
		return true;
	}
}
