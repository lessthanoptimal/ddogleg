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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;

/**
 * Hessian is represented as a sparse compact column matrix.
 *
 * @author Peter Abeles
 */
public class HessianMath_DSCC implements HessianMath {

	LinearSolver<DMatrixSparseCSC,DMatrixRMaj> solver;
	DMatrixSparseCSC hessian = new DMatrixSparseCSC(1,1);

	public HessianMath_DSCC() {
	}

	public HessianMath_DSCC(LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solver) {
		this.solver = UtilEjml.safe(solver);
	}

	@Override
	public void init(int numParameters) {
		hessian.reshape(numParameters,numParameters);
	}

	@Override
	public double innerVectorHessian(DMatrixRMaj v) {
		return MatrixVectorMult_DSCC.innerProduct(v.data,0, hessian, v.data,0);
	}

	@Override
	public void extractDiagonals(DMatrixRMaj diag) {
		CommonOps_DSCC.extractDiag(hessian,diag);
	}

	@Override
	public void divideRowsCols(DMatrixRMaj scaling) {
		CommonOps_DSCC.divideRowsCols(scaling.data,0,hessian,scaling.data,0);
	}

	@Override
	public boolean initializeSolver() {
		if( solver == null )
			throw new RuntimeException("Solver not set");

		return solver.setA(hessian);
	}

	@Override
	public boolean solve(DMatrixRMaj Y, DMatrixRMaj step) {
		solver.solve(Y,step);
		return true;
	}
}
