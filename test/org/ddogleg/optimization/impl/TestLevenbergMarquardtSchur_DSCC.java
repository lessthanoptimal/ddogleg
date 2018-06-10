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

import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ddogleg.optimization.wrap.LevenbergSchur_to_UnconstrainedLeastSquares;
import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

/**
 * TODO add all the usual tests
 *
 * @author Peter Abeles
 */
public class TestLevenbergMarquardtSchur_DSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

	private Random rand = new Random(234);

	/**
	 * Compare the by blocks solution to the normal way
	 */
	@Test
	public void computeJacobian_computeStep() {

		double lambda = 1e-4;
		int rows = 20; // functions
		int cols = 15; // parameters
		int split = 4;

		HelperJacobian helper = new HelperJacobian(rows,cols,split);
		LevenbergMarquardtSchur_DSCC alg = new LevenbergMarquardtSchur_DSCC(1e-4);
		LevenbergMarquardtDampened_DSCC baseline = new LevenbergMarquardtDampened_DSCC(1e-4);

		// -------- computeJacobian
		alg.setFunction(helper);
		baseline.setFunction(helper);

		alg.internalInitialize(cols,rows);
		baseline.internalInitialize(cols,rows);

		DMatrixRMaj residuals = RandomMatrices_DDRM.rectangle(rows,1,rand);
		DMatrixRMaj foundGradient = new DMatrixRMaj(cols,1);
		DMatrixRMaj expectedGradient = new DMatrixRMaj(cols,1);

		baseline.computeJacobian(residuals,expectedGradient);
		alg.computeJacobian(residuals,foundGradient);

		EjmlUnitTests.assertEquals(expectedGradient,foundGradient);
		EjmlUnitTests.assertEquals(baseline.Bdiag,alg.Bdiag);

		// -------- computeStep
		DMatrixRMaj Y = RandomMatrices_DDRM.rectangle(cols,1,-1,1,rand);
		DMatrixRMaj expectedStep = new DMatrixRMaj(cols,1);
		DMatrixRMaj foundStep = new DMatrixRMaj(cols,1);

		baseline.computeStep(lambda,Y,expectedStep);
		alg.computeStep(lambda,Y,foundStep);

		EjmlUnitTests.assertEquals(expectedStep,foundStep);
	}

	@Override
	protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch(double minimumValue) {
		LevenbergMarquardtSchur_DSCC alg = new LevenbergMarquardtSchur_DSCC(1e-4);

		// just pick some location to split. Would be better if always in the middle...
		return new LevenbergSchur_to_UnconstrainedLeastSquares(alg,1);
	}

	public class HelperJacobian implements
			LevenbergMarquardtSchur_DSCC.FunctionJacobian,
			CoupledJacobian<DMatrixSparseCSC>
	{

		public DMatrixSparseCSC J;
		int split;

		public HelperJacobian( int rows , int cols , int split ) {
			int nz = rows*cols*2/3;
			J = RandomMatrices_DSCC.rectangle(rows,cols,nz,rand);
			this.split = split;
		}

		@Override
		public int getNumOfInputsN() {
			return J.numRows;
		}

		@Override
		public int getNumOfOutputsM() {
			return J.numCols;
		}

		@Override
		public void setInput(double[] x) {

		}

		@Override
		public void computeFunctions(double[] output) {

		}

		@Override
		public void computeJacobian(DMatrixSparseCSC jacobian) {
			jacobian.set(J);
		}

		@Override
		public void computeJacobian(DMatrixSparseCSC left, DMatrixSparseCSC right) {
			left.reshape(J.numRows,split);
			right.reshape(J.numRows, J.numCols-split);
			CommonOps_DDRM.extract(J,0, J.numRows,0,split,left,0,0);
			CommonOps_DDRM.extract(J,0, J.numRows,split, J.numCols,right,0,0);
		}
	}
}