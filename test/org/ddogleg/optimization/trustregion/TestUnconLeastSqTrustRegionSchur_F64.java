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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquaresSchur_DDRM;
import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquaresSchur_DSCC;
import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		DMatrixSparseCSC J_t = CommonOps_DSCC.transpose(J,null,gw);
		CommonOps_DSCC.mult(J,J_t,H,gw,gx);

		H.sortIndices(null);
	}

	@Test
	public void cost() {
		double expected = 0.5*VectorVectorMult_DDRM.innerProd(residuals,residuals);

		TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> dogleg = new TrustRegionUpdateDogleg_F64<>();
		HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
		UnconLeastSqTrustRegionSchur_F64<DMatrixSparseCSC> alg = new UnconLeastSqTrustRegionSchur_F64<>(dogleg,hessian);
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],1e-6,1e-8);

		double found = alg.cost(new DMatrixRMaj(N,1));
		assertEquals(expected,found, UtilEjml.TEST_F64);
	}

	@Test
	public void functionGradientHessian() {
		TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> dogleg = new TrustRegionUpdateDogleg_F64<>();
		HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
		UnconLeastSqTrustRegionSchur_F64<DMatrixSparseCSC> alg = new UnconLeastSqTrustRegionSchur_F64<>(dogleg,hessian);
		alg.setFunction(new MockFunction(),new MockJacobian());
		alg.initialize(new double[N],1e-6,1e-8);

		DMatrixRMaj x = new DMatrixRMaj(1,1);

		DMatrixRMaj g = new DMatrixRMaj(N,1);
		alg.functionGradientHessian(x,false,g,hessian);
		// Only the gradient is computed and returned. The hessian is saved internally

		DMatrixRMaj exp_g = new DMatrixRMaj(N,1);
		CommonOps_DSCC.multTransA(J,residuals,exp_g,gx);

		assertTrue(MatrixFeatures_DDRM.isIdentical(exp_g,g,UtilEjml.TEST_F64));
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

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquaresSchur_DDRM {
		LeastSquaresDDRM() {
			// we can do fast convergence check here because dense supports robust solvers like SVD and it
			// won't get stuck
		}

		@Override
		protected UnconstrainedLeastSquaresSchur<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

//			config.regionInitial = 1;
			config.hessianScaling = true;

			return FactoryOptimization.doglegSchur(true,config);
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {

		public LeastSquaresDSCC() {
			// because of the matrix being nearly singular it gets stuck doing Cauchy steps.
			// If a rank revealing sparse solver this should be removed
			checkFastConvergence = false;
		}

		@Override
		protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

//			config.regionInitial = 1;
			config.hessianScaling = true;

			TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> dogleg = new TrustRegionUpdateDogleg_F64<>();
			HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
			UnconLeastSqTrustRegionSchur_F64<DMatrixSparseCSC> tr =
					new UnconLeastSqTrustRegionSchur_F64<>(dogleg,hessian);
			tr.configure(config);
//			tr.setVerbose(true);
			return tr;
		}
	}
}