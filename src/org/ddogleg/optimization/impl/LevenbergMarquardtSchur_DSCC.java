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

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

/**
 * Sparse Levenberg-Marquardt implementation which uses the Schur Complement
 * to decompose a sparse matrix. The approxmate Hessian matrix (J'*J) is assumed to have the
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
public class LevenbergMarquardtSchur_DSCC extends LevenbergBase<DMatrixSparseCSC>
{
	// computes the function and its derivative
	FunctionNtoM function;
	SchurJacobian<DMatrixSparseCSC> jacobian;
	double[] input;

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

	/**
	 * Specifies termination condition and dampening parameter
	 *
	 * @param initialDampParam Initial value of the dampening parameter.  Tune.. try 1e-3;
	 */
	public LevenbergMarquardtSchur_DSCC(double initialDampParam) {
		super(initialDampParam);

		solverA = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
		solverD = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
	}

	@Override
	protected void setFunctionParameters(double[] param) {
		this.input = param;
		solverA.setStructureLocked(false);
		solverD.setStructureLocked(false);
	}

	@Override
	protected void computeResiduals(double[] output) {
		function.process(input,output);
	}

	@Override
	protected void computeJacobian(DMatrixRMaj residuals, DMatrixRMaj gradient) {
		// calculate the Jacobian values at the current sample point
		jacobian.process(input, jacLeft,jacRight);

		if( jacLeft.numCols+jacRight.numCols != N )
			throw new IllegalArgumentException("Unexpected number of jacobian columns");
		if( jacLeft.numRows!= M || jacRight.numRows != M )
			throw new IllegalArgumentException("Unexpected number of jacobian rows");

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

		// extract diagonal elements from Hessian matrix
		CommonOps_DSCC.extractDiag(A, x1);
		CommonOps_DSCC.extractDiag(D, x2);

		Bdiag.reshape(N,1);
		CommonOps_DDRM.insert(x1,Bdiag,0,0);
		CommonOps_DDRM.insert(x2,Bdiag,x1.numRows,0);
	}

	/**
	 *
	 * @param Y Negative of the gradient. DO NOT MODIFY. (Nx1)
	 * @param step Step for this iteration. (Nx1)
	 */
	@Override
	protected boolean computeStep(double lambda, DMatrixRMaj Y, DMatrixRMaj step) {
		// add dampening parameter
		for( int i = 0; i < A.numCols; i++ ) {
			A.set(i,i, Bdiag.data[i] *(1.0 + lambda));
		}
		for( int i = 0; i < D.numCols; i++ ) {
			D.set(i,i, Bdiag.data[A.numCols+i] *(1.0 + lambda));
		}

		// Don't use quality to reject a solution since it's meaning is too dependent on implementation
		if( !solverA.setA(A) )
			return false;
		solverA.setStructureLocked(true);

		// extract b1
		CommonOps_DDRM.extract(Y,0,A.numCols,0,Y.numCols, b1);
		CommonOps_DDRM.extract(Y,A.numCols,Y.numRows,0,Y.numCols, b2);

		// x=inv(A)*b1
		x.reshape(A.numRows,1);
		solverA.solve(b1,x);
		// b2_m = b_2 - C*inv(A)*b1 = b_2 - C*x
		// C = B'
		CommonOps_DSCC.multTransA(B,x,b2_m); // C*x
		CommonOps_DDRM.add(b2,-1,b2_m,b2_m); // b2_m = b_2 - C*x

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

		// copy into the output
		CommonOps_DDRM.insert(x1,step,0,0);
		CommonOps_DDRM.insert(x2,step,x1.numRows,0);

		return true;
	}

	@Override
	protected double predictedReduction(DMatrixRMaj param, DMatrixRMaj gradientNegative, double mu) {
		double p_dot_g = VectorVectorMult_DDRM.innerProd(param,gradientNegative);
		double p_JJ_p = 0;
		for( int i = 0; i < N; i++ )
			p_JJ_p += param.data[i]*Bdiag.data[i]*param.data[i];

		// The variable g is really the negative of g
		return 0.5*(p_dot_g + mu*p_JJ_p);
	}

	@Override
	protected double getMinimumDampening() {
		return CommonOps_DDRM.elementMax(Bdiag);
	}

	public void setFunction(FunctionNtoM function , SchurJacobian<DMatrixSparseCSC> jacobian )
	{
		this.function = function;
		this.jacobian = jacobian;
		internalInitialize(function.getNumOfInputsN(),function.getNumOfOutputsM());
	}
}
