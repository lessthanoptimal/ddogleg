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
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * Hessian is represented as a dense matrix. Any dense linear solver can be used.
 *
 * @author Peter Abeles
 */
public class HessianMath_DDRM implements HessianMath {

	protected LinearSolver<DMatrixRMaj,DMatrixRMaj> solver;
	protected DMatrixRMaj hessian = new DMatrixRMaj(1,1);

	public HessianMath_DDRM() {
	}

	public HessianMath_DDRM(LinearSolverDense<DMatrixRMaj> solver) {
		this.solver = UtilEjml.safe(solver);
	}

	@Override
	public void init(int numParameters) {
		hessian.reshape(numParameters,numParameters);
	}

	@Override
	public double innerVectorHessian(DMatrixRMaj v) {
		return VectorVectorMult_DDRM.innerProdA(v, hessian, v);
	}

	@Override
	public void extractDiagonals(DMatrixRMaj diag) {
		CommonOps_DDRM.extractDiag(hessian,diag);
	}

	@Override
	public void setDiagonals(DMatrixRMaj diag) {
		for (int i = 0; i < hessian.numRows; i++) {
			hessian.set(i,i,diag.data[i]);
		}
	}

	@Override
	public void divideRowsCols(DMatrixRMaj scaling) {
		CommonOps_DDRM.divideCols(hessian,scaling.data);
		CommonOps_DDRM.divideRows(scaling.data, hessian);
	}

	@Override
	public boolean initializeSolver() {
		if( solver == null )
			throw new RuntimeException("Solver not set");

		if( !solver.setA(hessian) ) {
			hessian.print();
			return false;
		}
		return true;
	}

	@Override
	public boolean solve(DMatrixRMaj Y, DMatrixRMaj step) {
		solver.solve(Y,step);
		return true;
	}

	public DMatrixRMaj getHessian() {
		return hessian;
	}
}
