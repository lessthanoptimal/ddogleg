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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquares_DDRM;
import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquares_DSCC;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.lm.TestUnconLeastSqLevenbergMarquardtSchur_F64.MockResiduals;
import org.ddogleg.optimization.math.HessianLeastSquares_DDRM;
import org.ddogleg.optimization.math.HessianLeastSquares_DSCC;
import org.ddogleg.optimization.math.MatrixMath_DDRM;
import org.ddogleg.optimization.math.MatrixMath_DSCC;
import org.ddogleg.optimization.wrap.GenericUnconstrainedLeastSquaresTests_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public class TestUnconLeastSqLevenbergMarquardt_F64 extends GenericUnconstrainedLeastSquaresTests_F64 {
	@Test void computeGradientHessian() {
		UnconLeastSqLevenbergMarquardt_F64<DMatrixRMaj> lm = createLM();

		var residuals = new MockResiduals();
		var jacobian = new MockJacobian();
		lm.setFunction(residuals, jacobian);
		lm.residuals.reshape(2, 1);
		lm.hessian = new MockHessian();

		var x = new DMatrixRMaj(2, 1);
		var g = new DMatrixRMaj(2, 1);
		lm.functionGradientHessian(x, true, g, lm.hessian);
		assertFalse(residuals.called);

		var h = (MockHessian)lm.hessian;
		assertTrue(h.hessian);

		lm.functionGradientHessian(x, false, g, lm.hessian);
		assertTrue(residuals.called);
	}

	@Test void computeResiduals() {
		UnconLeastSqLevenbergMarquardt_F64<DMatrixRMaj> lm = createLM();

		var residuals = new MockResiduals();
		lm.setFunction(residuals, new MockJacobian());

		var x = new DMatrixRMaj(1, 1);
		var r = new DMatrixRMaj(1, 1);
		lm.computeResiduals(x, r);
		assertTrue(residuals.called);
	}

	@Override
	public UnconstrainedLeastSquares<DMatrixRMaj> createAlgorithm() {
		return createLM();
	}

	private UnconLeastSqLevenbergMarquardt_F64<DMatrixRMaj> createLM() {
		var config = new ConfigLevenbergMarquardt();

		LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(2);
		var hessian = new HessianLeastSquares_DDRM(solver);
		var lm = new UnconLeastSqLevenbergMarquardt_F64<>(new MatrixMath_DDRM(), hessian);
		lm.configure(config);
		return lm;
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {
		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			return createLM();
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {
		@Override protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();

			LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
			HessianLeastSquares_DSCC hessian = new HessianLeastSquares_DSCC(solver);
			UnconLeastSqLevenbergMarquardt_F64<DMatrixSparseCSC> lm = new UnconLeastSqLevenbergMarquardt_F64<>(new MatrixMath_DSCC(),hessian);
			lm.configure(config);
//			lm.setVerbose(true);
			return lm;
		}
	}

	@Nested
	class LeastSquaresDDRM_scaling extends CommonChecksUnconstrainedLeastSquares_DDRM {
		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();

			config.dampeningInitial = 0.1;
			config.hessianScaling = true;

			LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(2);
			HessianLeastSquares_DDRM hessian = new HessianLeastSquares_DDRM(solver);
			UnconLeastSqLevenbergMarquardt_F64<DMatrixRMaj> lm = new UnconLeastSqLevenbergMarquardt_F64<>(new MatrixMath_DDRM(),hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}

	public class MockJacobian implements FunctionNtoMxN<DMatrixRMaj> {
		boolean called = false;

		@Override public void process( double[] input, DMatrixRMaj output ) {
			called = true;
			output.reshape(2,3);
		}

		@Override public DMatrixRMaj declareMatrixMxN() {return null;}
		@Override public int getNumOfInputsN() {return 0;}
		@Override public int getNumOfOutputsM() {return 0;}
	}

	private class MockHessian extends HessianLeastSquares_DDRM {
		boolean hessian = false;

		@Override public void updateHessian( DMatrixRMaj jacobian ) {
			this.hessian = true;
		}
	}
}