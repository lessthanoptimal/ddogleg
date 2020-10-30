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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestHessianSchurComplement_DDRM extends StandardHessianMathChecks {

	int M = 10;
	int N = 6;
	int split = 4;
	Random rand = new Random(234);


	DMatrixRMaj jacLeft = RandomMatrices_DDRM.rectangle(M,split,-1,1,rand);
	DMatrixRMaj jacRight = RandomMatrices_DDRM.rectangle(M,N-split,-1,1,rand);

	DMatrixRMaj J = new DMatrixRMaj(M,N);
	DMatrixRMaj H = new DMatrixRMaj(N,N);

	DMatrixRMaj residuals = RandomMatrices_DDRM.rectangle(M,1,-1,1,rand);

	HessianSchurComplement_DDRM math = new HessianSchurComplement_DDRM();

	public TestHessianSchurComplement_DDRM() {
		super(new HessianSchurComplement_DSCC());
		CommonOps_DDRM.concatColumns(jacLeft,jacRight,J);
		CommonOps_DDRM.multTransA(J,J,H);
	}


	@Override
	protected void setHessian(HessianMath alg, DMatrixRMaj H) {

		int M = 3;
		int N = H.numCols-M;

		HessianSchurComplement_DSCC hm = (HessianSchurComplement_DSCC)alg;

		DMatrixSparseCSC SH = new DMatrixSparseCSC(1,1);
		DConvertMatrixStruct.convert(H,SH);

		hm.A.reshape(M,M);
		hm.B.reshape(M,N);
		hm.D.reshape(N,N);

		CommonOps_DSCC.extract(SH,0,M,0,M,hm.A,0,0);
		CommonOps_DSCC.extract(SH,0,M,M,M+N,hm.B,0,0);
		CommonOps_DSCC.extract(SH,M,M+N,M,M+N,hm.D,0,0);
	}

	@Test
	public void computeGradient() {
		DMatrixRMaj found = new DMatrixRMaj(N,1);
		DMatrixRMaj expected = new DMatrixRMaj(N,1);

		math.computeGradient(jacLeft,jacRight,residuals,found);

		CommonOps_DDRM.multTransA(J,residuals,expected);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}

	@Test
	public void computeHessian() {
		math.computeHessian(jacLeft,jacRight);

		DMatrixRMaj found = new DMatrixRMaj(1,1);
		math.extractDiagonals(found);

		DMatrixRMaj expected = new DMatrixRMaj(1,1);
		CommonOps_DDRM.extractDiag(H,expected);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}

//
//	@Test
//	public void innerVectorHessian() {
//		HessianSchurComplement_DSCC math = new HessianSchurComplement_DSCC();
//
//		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(N,1,-1,1,rand);
//
//		math.computeHessian(jacLeft,jacRight);
//		double found = math.innerVectorHessian(v);
//		double expected = MatrixVectorMult_DSCC.innerProduct(v.data,0,H,v.data,0);
//
//		assertEquals(expected,found,UtilEjml.TEST_F64);
//	}

	@Test
	public void computeStep() {
		// This is tested in the optimization algorithms
	}
}