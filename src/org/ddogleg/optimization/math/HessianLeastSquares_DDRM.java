/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.DDoglegConcurrency;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CommonOps_MT_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * @author Peter Abeles
 */
public class HessianLeastSquares_DDRM extends HessianMath_DDRM
		implements HessianLeastSquares<DMatrixRMaj> {
	public HessianLeastSquares_DDRM() {super(LinearSolverFactory_DDRM.cholInner(true));}

	public HessianLeastSquares_DDRM( LinearSolverDense<DMatrixRMaj> solver ) {super(solver);}

	@Override
	public void updateHessian( DMatrixRMaj jacobian ) {
		if (DDoglegConcurrency.isUseConcurrent()) {
			CommonOps_MT_DDRM.multInner(jacobian, hessian);
		} else {
			CommonOps_DDRM.multInner(jacobian, hessian);
		}
	}
}
