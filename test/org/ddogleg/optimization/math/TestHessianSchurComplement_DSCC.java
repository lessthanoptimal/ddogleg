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
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestHessianSchurComplement_DSCC {

	int M = 10;
	int N = 6;
	int split = 4;
	Random rand = new Random(234);

	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();

	DMatrixSparseCSC jacLeft = RandomMatrices_DSCC.rectangle(M,split,30,-1,1,rand);
	DMatrixSparseCSC jacRight = RandomMatrices_DSCC.rectangle(M,N-split,14,-1,1,rand);

	DMatrixSparseCSC J = new DMatrixSparseCSC(M,N,1);
	DMatrixSparseCSC H = new DMatrixSparseCSC(N,N,1);

	DMatrixRMaj residuals = RandomMatrices_DDRM.rectangle(M,1,-1,1,rand);

	HessianSchurComplement_DSCC math = new HessianSchurComplement_DSCC();

	public TestHessianSchurComplement_DSCC() {
		CommonOps_DSCC.concatColumns(jacLeft,jacRight,J);
		CommonOps_DSCC.multTransA(J,J,H,gw,gx);
	}

	@Test
	public void computeGradient() {
		DMatrixRMaj found = new DMatrixRMaj(N,1);
		DMatrixRMaj expected = new DMatrixRMaj(N,1);

		math.computeGradient(jacLeft,jacRight,residuals,found);

		CommonOps_DSCC.multTransA(J,residuals,expected);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}

	@Test
	public void extractDiag() {
		math.computeHessian(jacLeft,jacRight);

		DMatrixRMaj found = new DMatrixRMaj(1,1);
		math.extractDiagonals(found);

		DMatrixRMaj expected = new DMatrixRMaj(1,1);
		CommonOps_DSCC.extractDiag(H,expected);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}

	@Test
	public void divideRowsCols() {
		DMatrixRMaj scale = RandomMatrices_DDRM.rectangle(M,1,0,1,rand);

		DMatrixSparseCSC H = this.H.copy();
		CommonOps_DSCC.divideRowsCols(scale.data,0,H,scale.data,0);

		HessianSchurComplement_DSCC math = new HessianSchurComplement_DSCC();
		math.computeHessian(jacLeft,jacRight);

		math.divideRowsCols(scale);

		// Reconstruct the original matrix
		DMatrixSparseCSC top = CommonOps_DSCC.concatColumns(math.A,math.B,null);
		DMatrixSparseCSC Bt = new DMatrixSparseCSC(1,1);
		CommonOps_DSCC.transpose(math.B,Bt,gw);
		DMatrixSparseCSC bottom = CommonOps_DSCC.concatColumns(Bt,math.D,null);
		DMatrixSparseCSC found = CommonOps_DSCC.concatRows(top,bottom,null);

		H.sortIndices(null);
		found.sortIndices(null);
		assertTrue(MatrixFeatures_DSCC.isEquals(H,found, UtilEjml.TEST_F64));
	}

	@Test
	public void innerVectorHessian() {
		HessianSchurComplement_DSCC math = new HessianSchurComplement_DSCC();

		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(N,1,-1,1,rand);

		math.computeHessian(jacLeft,jacRight);
		double found = math.innerVectorHessian(v);
		double expected = MatrixVectorMult_DSCC.innerProduct(v.data,0,H,v.data,0);

		assertEquals(expected,found,UtilEjml.TEST_F64);
	}

	@Test
	public void computeStep() {
		// This is tested in the optimization algorithms
	}
}