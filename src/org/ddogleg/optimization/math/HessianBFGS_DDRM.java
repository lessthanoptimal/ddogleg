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

package org.ddogleg.optimization.math;

import org.ddogleg.optimization.impl.EquationsBFGS;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * Uses DFP to estimate the Hessian and BFGS to estimate the inverse Hessian. See [1].
 * A more efficient way to do this would be to compute the Cholesky decomposition directly, then
 * use that for forward and inverse operations
 *
 * <p>[1] Jorge Nocedal,and Stephen J. Wright "Numerical Optimization" 2nd Ed. Springer 2006</p>
 *
 * @author Peter Abeles
 */
public class HessianBFGS_DDRM extends HessianMath_DDRM implements HessianBFGS {

	boolean computeInverse;
	DMatrixRMaj hessianInverse = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN1 = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN2 = new DMatrixRMaj(1,1);


	public HessianBFGS_DDRM(boolean computeInverse) {
		this.computeInverse = computeInverse;
	}

	@Override
	public void divideRowsCols(DMatrixRMaj scaling) {
		throw new IllegalArgumentException("Scaling with BFGS is currently not supported. A scaled and unscaled H and inv(H) need to be maintained");
	}

	@Override
	public void init(int numParameters) {
		super.init(numParameters);
		hessianInverse.reshape(numParameters,numParameters);
		CommonOps_DDRM.setIdentity(hessian);
		CommonOps_DDRM.setIdentity(hessianInverse);
	}

	@Override
	public void update(DMatrixRMaj s, DMatrixRMaj y) {
		EquationsBFGS.update(hessian, s, y, tmpN1, tmpN2);
		if( computeInverse ) {
			EquationsBFGS.inverseUpdate(hessianInverse,s,y,tmpN1,tmpN2);
		}
	}

	@Override
	public boolean initializeSolver() {
		return true;
	}

	@Override
	public boolean solve(DMatrixRMaj Y, DMatrixRMaj step) {
		if( !computeInverse )
			throw new RuntimeException("Can't solve since configured to not compute the inverse");

		CommonOps_DDRM.mult(hessianInverse,Y,step);
		return true;
	}
}
