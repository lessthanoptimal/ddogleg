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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestUnconLeastSqTrustRegionSchur_F64 {

	int M = 20;
	int N = 8;
	int split = 5;
	Random rand = new Random(234);

	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();

	DMatrixSparseCSC jacLeft = RandomMatrices_DSCC.rectangle(M,split,50,-1,1,rand);
	DMatrixSparseCSC jacRight = RandomMatrices_DSCC.rectangle(M,N-split,30,-1,1,rand);

	DMatrixSparseCSC J = new DMatrixSparseCSC(M,N,1);
	DMatrixSparseCSC H = new DMatrixSparseCSC(N,N,1);

	DMatrixRMaj residuals = RandomMatrices_DDRM.rectangle(M,1,-1,1,rand);

	public TestUnconLeastSqTrustRegionSchur_F64() {
		CommonOps_DSCC.concatColumns(jacLeft,jacRight,J);
		CommonOps_DSCC.multTransA(J,J,H,gw,gx);

		H.sortIndices(null);
	}

	@Test
	public void cost() {
		double expected = 0.5*VectorVectorMult_DDRM.innerProd(residuals,residuals);

		UnconLeastSqTrustRegionSchur_F64 alg = new UnconLeastSqTrustRegionSchur_F64();
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],1e-6,1e-8);

		double found = alg.cost(new DMatrixRMaj(N,1));
		assertEquals(expected,found, UtilEjml.TEST_F64);
	}

	@Test
	public void functionGradientHessian() {
		UnconLeastSqTrustRegionSchur_F64 alg = new UnconLeastSqTrustRegionSchur_F64();
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],1e-6,1e-8);

		DMatrixRMaj x = new DMatrixRMaj(1,1);

		DMatrixRMaj g = new DMatrixRMaj(N,1);
		alg.functionGradientHessian(x,false,g,null);
		// Only the gradient is computed and returned. The hessian is saved internally

		DMatrixRMaj exp_g = new DMatrixRMaj(N,1);
		CommonOps_DSCC.multTransA(J,residuals,exp_g);

		assertTrue(MatrixFeatures_DDRM.isIdentical(exp_g,g,UtilEjml.TEST_F64));
	}

	@Test
	public void checkConvergenceFTest() {
		double tol = 1e-4;
		UnconLeastSqTrustRegionSchur_F64 alg = new UnconLeastSqTrustRegionSchur_F64();
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],tol,0);
		alg.regionRadius = 100; // so it doesn't complain

		// give it random matrix where everything is in bounds
		RandomMatrices_DDRM.fillUniform(alg.residuals,-tol/10,tol/10,rand);

		// these inputs are ignored because a different formula is used with least-squares
		assertTrue(alg.checkConvergenceFTest(100,1000));

		// one element is not in tolerance
		alg.residuals.data[3] = tol*1.0001;
		assertFalse(alg.checkConvergenceFTest(100,1000));
		alg.residuals.data[3] = -tol*1.0001;
		assertFalse(alg.checkConvergenceFTest(100,1000));
	}

	@Test
	public void computeScaling() {
		UnconLeastSqTrustRegionSchur_F64 alg = new UnconLeastSqTrustRegionSchur_F64();
		alg.config.scalingMinimum = -10000;
		alg.config.scalingMaximum = 10000;

		alg.schur.computeHessian(jacLeft,jacRight);
		alg.computeScaling();

		for (int i = 0; i < alg.scaling.numRows; i++) {
			assertEquals( Math.sqrt(H.get(i,i)),alg.scaling.data[i], UtilEjml.TEST_F64);
		}
	}

	@Test
	public void computePredictedReduction() {
		UnconLeastSqTrustRegionSchur_F64 alg = new UnconLeastSqTrustRegionSchur_F64();
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],1e-8,1e-8);

		RandomMatrices_DDRM.fillUniform(alg.gradient,-1,1,rand);
		alg.schur.computeHessian(jacLeft,jacRight);

		DMatrixRMaj p = RandomMatrices_DDRM.rectangle(N,1,rand);
		double found = alg.computePredictedReduction(p);

		double expected = -VectorVectorMult_DDRM.innerProd(alg.gradient,p);
		expected -= 0.5*MatrixVectorMult_DSCC.innerProduct(p.data,0,H,p.data,0);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	public class MockFunction implements FunctionNtoM {

		@Override
		public void process(double[] input, double[] output) {
			System.arraycopy(residuals.data,0,output,0,M);
		}

		@Override
		public int getNumOfInputsN() {
			return N;
		}

		@Override
		public int getNumOfOutputsM() {
			return M;
		}
	}

	public class MockJacobian implements SchurJacobian<DMatrixSparseCSC> {

		@Override
		public void process(double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right) {
			left.set(jacLeft);
			right.set(jacRight);
		}

		@Override
		public int getNumOfInputsN() {
			return N;
		}

		@Override
		public int getNumOfOutputsM() {
			return M;
		}
	}

	@Test
	public void runFullAlg() {
		fail("implement");
	}
}