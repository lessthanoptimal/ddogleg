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

package org.ddogleg.optimization.impl;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

/**
 * The approximate Hessian matrix (J'*J) is assumed to have the
 * following block triangle form: [A B;C D]. The system being solved for
 * is as follows: [A B;C D] [x_1;x_2] = [b_1;b_2]. Then the following steps
 * are followed to solve it:
 *
 * <ol>
 *     <li>D' = D - C*inv(A)*B and b_2' = b_2 - C*inv(A)*b_1</li>
 *     <li>D'*x_2 = b_2'</li>
 *     <li>A*x_1 = b_1 - B*x_2</li>
 * </ol>
 *
 * [1] Triggs, Bill, et al. "Bundle adjustment—a modern synthesis." International workshop on vision algorithms.
 * Springer, Berlin, Heidelberg, 1999.
 *
 * @author Peter Abeles
 */
public class SchurComplementMath {
	// Left and right side of the jacobian matrix
	DMatrixSparseCSC jacLeft = new DMatrixSparseCSC(1,1,1);
	DMatrixSparseCSC jacRight = new DMatrixSparseCSC(1,1,1);

	// Blocks inside the Hessian matrix
	DMatrixSparseCSC A = new DMatrixSparseCSC(1,1);
	DMatrixSparseCSC B = new DMatrixSparseCSC(1,1);
	DMatrixSparseCSC D = new DMatrixSparseCSC(1,1);

	// Workspace variables
	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();

	DMatrixRMaj b1 = new DMatrixRMaj(1,1);
	DMatrixRMaj b2 = new DMatrixRMaj(1,1);
	DMatrixRMaj b2_m = new DMatrixRMaj(1,1);
	DMatrixRMaj x = new DMatrixRMaj(1,1);
	DMatrixRMaj x1 = new DMatrixRMaj(1,1);
	DMatrixRMaj x2 = new DMatrixRMaj(1,1);
	DMatrixSparseCSC tmp0 = new DMatrixSparseCSC(1,1);
	DMatrixSparseCSC D_m = new DMatrixSparseCSC(1,1);

	// Two solvers are created so that the structure can be saved and not recomputed each iteration
	protected LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solverA, solverD;

	public SchurComplementMath() {
		this( LinearSolverFactory_DSCC.cholesky(FillReducing.NONE),
				LinearSolverFactory_DSCC.cholesky(FillReducing.NONE));
	}

	public SchurComplementMath(LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverA,
							   LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverD) {
		this.solverA = solverA;
		this.solverD = solverD;
	}


	/**
	 * Call when a new function is used and/ore the non-zero structure changes
	 */
	public void initialize() {
		// The structure will be computed in the first iteration then fixed from then on
		// saves a tiny bit of memory and time
		solverA.setStructureLocked(false);
		solverD.setStructureLocked(false);
	}

	/**
	 * Computes the gradient using Schur complement
	 *
	 * @param jacLeft (Input) Left side of Jacobian
	 * @param jacRight (Input) Right side of Jacobian
	 * @param residuals (Input) Residuals
	 * @param gradient (Output) Gradient
	 */
	public void computeGradient(DMatrixSparseCSC jacLeft , DMatrixSparseCSC jacRight ,
								DMatrixRMaj residuals, DMatrixRMaj gradient) {

		// Compute the Hessian in blocks
		A.reshape(jacLeft.numCols,jacLeft.numCols,1);
		B.reshape(jacLeft.numCols,jacRight.numCols,1);
		D.reshape(jacRight.numCols,jacRight.numCols,1);

		// take advantage of the inner product's symmetry when possible to reduce
		// the number of calculations
		CommonOps_DSCC.innerProductLower(jacLeft,tmp0,gw,gx);
		CommonOps_DSCC.symmLowerToFull(tmp0,A,gw);
		CommonOps_DSCC.multTransA(jacLeft,jacRight,B,gw,gx);
		CommonOps_DSCC.innerProductLower(jacRight,tmp0,gw,gx);
		CommonOps_DSCC.symmLowerToFull(tmp0,D,gw);

		// Find the gradient using the two matrices for Jacobian
		// g = J'*r = [L,R]'*r
		x1.reshape(jacLeft.numCols,1);
		x2.reshape(jacRight.numCols,1);
		CommonOps_DSCC.multTransA(jacLeft,residuals,x1);
		CommonOps_DSCC.multTransA(jacRight,residuals,x2);

		CommonOps_DDRM.insert(x1,gradient,0,0);
		CommonOps_DDRM.insert(x2,gradient,x1.numRows,0);
	}

	/**
	 * Extracts the diagonal elements from the hessian
	 * @param diag
	 */
	public void extractHessianDiagonal( DMatrixRMaj diag ) {
		// extract diagonal elements from Hessian matrix
		CommonOps_DSCC.extractDiag(A, x1);
		CommonOps_DSCC.extractDiag(D, x2);

		diag.reshape(jacLeft.numCols+jacRight.numCols,1);
		CommonOps_DDRM.insert(x1,diag,0,0);
		CommonOps_DDRM.insert(x2,diag,x1.numRows,0);
	}

	/**
	 * Applies the inverted scaling matrix S to the blocks of H=J'*J=[A B;C D] inplace.
	 * [A B;C D] = inv(S)*J'*J*inv(S).
	 * @param scale
	 */
	public void applyInvScalingToBlocks( DMatrixRMaj scale ) {

	}

	/**
	 * Vector matrix inner product of Hessian in block format
	 *
	 * @param v row vector
	 * @return v'*H*v = v'*[A B;C D]*v
	 */
	public double innerProductHessian( DMatrixRMaj v ) {
		return 0;
	}

	/**
	 *
	 * @param gradient (Input) The gradient. DO NOT MODIFY. (Nx1)
	 * @param step (Output) Step for this iteration. (Nx1)
	 */
	public boolean computeStep( DMatrixRMaj gradient, DMatrixRMaj step) {

		// Don't use quality to reject a solution since it's meaning is too dependent on implementation
		if( !solverA.setA(A) )
			return false;
		solverA.setStructureLocked(true);

		// extract b1
		CommonOps_DDRM.extract(gradient,0,A.numCols,0,gradient.numCols, b1);
		CommonOps_DDRM.extract(gradient,A.numCols,gradient.numRows,0,gradient.numCols, b2);

		// x=-inv(A)*b1
		x.reshape(A.numRows,1);
		solverA.solve(b1,x);
		CommonOps_DDRM.scale(-1,x);
		// b2_m = -b_2 - C*inv(A)*b1 = -b_2 - C*x
		// C = B'
		CommonOps_DSCC.multTransA(B,x,b2_m); // C*x
		CommonOps_DDRM.add(-1,b2,-1,b2_m,b2_m); // b2_m = -b_2 - C*x

		// D_m = D - C*inv(A)*B = D - B'*inv(A)*B (thus symmetric)
		D_m.reshape(A.numRows,B.numCols);
		solverA.solveSparse(B,D_m); // D_m = inv(A)*B
		CommonOps_DSCC.multTransA(B,D_m,tmp0,gw,gx); // tmp0 = C*D_m = C*inv(A)*B
		CommonOps_DSCC.add(1,D,-1,tmp0,D_m,gw,gx);

		// Reduced System
		// D_m*x_2 = b_2
		if( !solverD.setA(D_m) )
			return false;

		solverD.setStructureLocked(true);
		x2.reshape(D_m.numRows,b2_m.numCols);
		solverD.solve(b2_m,x2);

		// back-substitution
		// A*x1 = b1-B*x2
		CommonOps_DSCC.mult(B,x2,x1);
		CommonOps_DDRM.subtract(b1,x1,b1);
		solverA.solve(b1,x1);

//		x1.print();
		CommonOps_DDRM.insert(x1,step,0,0);
		CommonOps_DDRM.insert(x2,step,x1.numRows,0);

		return true;
	}

}
