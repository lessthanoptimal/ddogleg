/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * @author Peter Abeles
 */
public class HessianLeastSquares_DSCC extends HessianMath_DSCC
		implements HessianLeastSquares<DMatrixSparseCSC>
{
	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();
	DMatrixSparseCSC transpose = new DMatrixSparseCSC(1,1);
	public HessianLeastSquares_DSCC() {
	}

	public HessianLeastSquares_DSCC(LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver) {
		super(solver);
	}

	@Override
	public void updateHessian(DMatrixSparseCSC jacobian) {
		CommonOps_DSCC.transpose(jacobian,transpose,gw);
		CommonOps_DSCC.mult(transpose,jacobian, hessian,gw,gx);
	}
}
