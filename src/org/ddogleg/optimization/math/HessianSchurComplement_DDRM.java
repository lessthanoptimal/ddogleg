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

import org.ejml.LinearSolverToSparse;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.mult.MatrixMultProduct_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * Implementation of {@link HessianSchurComplement_Base} for {@link DMatrixRMaj}
 *
 * @author Peter Abeles
 */
public class HessianSchurComplement_DDRM
	extends HessianSchurComplement_Base<DMatrixRMaj>
{
	public HessianSchurComplement_DDRM() {
		this( LinearSolverFactory_DDRM.chol(100),
				LinearSolverFactory_DDRM.chol(100));
	}

	public HessianSchurComplement_DDRM(LinearSolverDense<DMatrixRMaj> solverA,
									   LinearSolverDense<DMatrixRMaj> solverD)
	{
		super(new LinearSolverToSparse<>(solverA),
				new LinearSolverToSparse<>(solverD));
	}

	/**
	 * Compuets the Hessian in block form
	 * @param jacLeft (Input) Left side of Jacobian
	 * @param jacRight (Input) Right side of Jacobian
	 */
	@Override
	public void computeHessian(DMatrixRMaj jacLeft , DMatrixRMaj jacRight) {
		A.reshape(jacLeft.numCols,jacLeft.numCols);
		B.reshape(jacLeft.numCols,jacRight.numCols);
		D.reshape(jacRight.numCols,jacRight.numCols);

		// take advantage of the inner product's symmetry when possible to reduce
		// the number of calculations
		MatrixMultProduct_DDRM.inner_reorder_lower(jacLeft,A);
		CommonOps_DDRM.symmLowerToFull(A);
		CommonOps_DDRM.multTransA(jacLeft,jacRight,B);
		MatrixMultProduct_DDRM.inner_reorder_lower(jacRight,D);
		CommonOps_DDRM.symmLowerToFull(D);
	}

	@Override
	public DMatrixRMaj createMatrix() {
		return new DMatrixRMaj(1,1);
	}

	@Override
	protected double innerProduct(double[] a, int offsetA, DMatrixRMaj B, double[] c, int offsetC) {
		return MatrixVectorMult_DDRM.innerProduct(a,0,B,c,offsetC);
	}

	@Override
	protected void extractDiag(DMatrixRMaj input, DMatrixRMaj output) {
		CommonOps_DDRM.extractDiag(input,output);
	}

	@Override
	protected void divideRowsCols(double[] diagA, int offsetA, DMatrixRMaj B, double[] diagC, int offsetC) {
		CommonOps_DDRM.divideRowsCols(diagA,offsetA,B,diagC,offsetC);
	}

	@Override
	protected void multTransA(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj C){
		CommonOps_DDRM.multTransA(A,B,C);
	}

	@Override
	protected void add(double alpha, DMatrixRMaj A, double beta, DMatrixRMaj B, DMatrixRMaj C) {
		CommonOps_DDRM.add(alpha,A,beta,B,C);
	}

	@Override
	protected void mult(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj C) {
		CommonOps_DDRM.mult(A,B,C);
	}
}
