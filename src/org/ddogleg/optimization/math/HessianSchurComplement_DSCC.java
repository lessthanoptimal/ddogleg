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
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;

/**
 * Implementation of {@link HessianSchurComplement_Base} for {@link DMatrixSparseCSC}
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class HessianSchurComplement_DSCC
	extends HessianSchurComplement_Base<DMatrixSparseCSC>
{
	DMatrixSparseCSC transposed = new DMatrixSparseCSC(1,1);

	// Workspace variables
	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();

	// Two solvers are created so that the structure can be saved and not recomputed each iteration
	protected LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solverA, solverD;

	public HessianSchurComplement_DSCC() {
		this( LinearSolverFactory_DSCC.cholesky(FillReducing.NONE),
				LinearSolverFactory_DSCC.cholesky(FillReducing.NONE));
	}
	public HessianSchurComplement_DSCC(LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverA,
									   LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverD) {
		super(solverA,solverD);
	}

	/**
	 * Compuets the Hessian in block form
	 * @param jacLeft (Input) Left side of Jacobian
	 * @param jacRight (Input) Right side of Jacobian
	 */
	@Override
	public void computeHessian(DMatrixSparseCSC jacLeft , DMatrixSparseCSC jacRight) {
		A.reshape(jacLeft.numCols,jacLeft.numCols,1);
		B.reshape(jacLeft.numCols,jacRight.numCols,1);
		D.reshape(jacRight.numCols,jacRight.numCols,1);

		// A = L'*L
		CommonOps_DSCC.transpose(jacLeft, transposed,gw);
		CommonOps_DSCC.mult(transposed,jacLeft,A,gw,gx);

		// B = L'*R
		CommonOps_DSCC.mult(transposed,jacRight,B,gw,gx);

		// D = R'*R
		CommonOps_DSCC.transpose(jacRight, transposed,gw);
		CommonOps_DSCC.mult(transposed,jacRight,D,gw,gx);
	}

	@Override
	public DMatrixSparseCSC createMatrix() {
		return new DMatrixSparseCSC(1,1);
	}

	@Override
	protected double innerProduct(double[] a, int offsetA, DMatrixSparseCSC B, double[] c, int offsetC) {
		return MatrixVectorMult_DSCC.innerProduct(a,offsetA,B,c,offsetC);
	}

	@Override
	protected void extractDiag(DMatrixSparseCSC input, DMatrixRMaj output) {
		CommonOps_DSCC.extractDiag(input,output);
	}

	@Override
	protected void divideRowsCols(double[] diagA, int offsetA, DMatrixSparseCSC B, double[] diagC, int offsetC) {
		CommonOps_DSCC.divideRowsCols(diagA,offsetA,B,diagC,offsetC);
	}

	@Override
	protected void multTransA(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C){
		CommonOps_DSCC.transpose(A, transposed,gw);
		CommonOps_DSCC.mult(transposed,B,C,gw,gx);
	}

	@Override
	protected void multTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C) {
		CommonOps_DSCC.multTransA(A,B,C);
	}

	@Override
	protected void add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C) {
		CommonOps_DSCC.add(alpha,A,beta,B,C,gw,gx);
	}

	@Override
	protected void mult(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C) {
		CommonOps_DSCC.mult(A,B,C);
	}
}
