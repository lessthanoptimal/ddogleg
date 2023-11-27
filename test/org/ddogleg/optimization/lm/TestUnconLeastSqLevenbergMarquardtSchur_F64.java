/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquaresSchur_DDRM;
import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquaresSchur_DSCC;
import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement_DDRM;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ddogleg.optimization.math.MatrixMath_DDRM;
import org.ddogleg.optimization.math.MatrixMath_DSCC;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUnconLeastSqLevenbergMarquardtSchur_F64 {
	@Test void functionGradientHessian() {
		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm = declare();

		var residuals = new MockResiduals();
		var jacobian = new MockJacobian();
		lm.setFunction(residuals, jacobian);

		var x = new DMatrixRMaj(1, 1);
		var g = new DMatrixRMaj(1, 1);
		lm.functionGradientHessian(x, true, g, lm.hessian);
		assertFalse(residuals.called);

		MockHessian h = (MockHessian)lm.hessian;
		assertTrue(h.hessian);
		assertTrue(h.gradient);

		lm.functionGradientHessian(x, false, g, lm.hessian);
		assertTrue(residuals.called);
	}

	/**
	 * Heavily lifting is done elsewhere. this just make sure the correct function is called.
	 */
	@Test void computeResiduals() {
		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm = declare();

		var residuals = new MockResiduals();
		lm.setFunction(residuals, new MockJacobian());

		var x = new DMatrixRMaj(1, 1);
		var r = new DMatrixRMaj(1, 1);
		lm.computeResiduals(x, r);
		assertTrue(residuals.called);
	}

	private UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> declare() {
		var config = new ConfigLevenbergMarquardt();

		var hessian = new MockHessian();
		var lm = new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(), hessian);
		lm.configure(config);
		return lm;
	}

	public static class MockResiduals implements FunctionNtoM {
		boolean called = false;

		@Override public void process( double[] input, double[] output ) {
			called = true;
		}

		@Override public int getNumOfInputsN() {return 2;}

		@Override public int getNumOfOutputsM() {return 3;}
	}

	private class MockJacobian implements SchurJacobian<DMatrixSparseCSC> {
		boolean called = false;

		@Override public void process( double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right ) {
			called = true;
		}

		@Override public int getNumOfInputsN() {return 2;}

		@Override public int getNumOfOutputsM() {return 3;}
	}

	private class MockHessian extends HessianSchurComplement_DSCC {
		boolean hessian = false;
		boolean gradient = false;

		@Override public void computeHessian( DMatrixSparseCSC jacLeft, DMatrixSparseCSC jacRight ) {
			this.hessian = true;
		}

		@Override public void computeGradient( DMatrixSparseCSC jacLeft, DMatrixSparseCSC jacRight,
											   DMatrixRMaj residuals, DMatrixRMaj gradient ) {
			this.gradient = true;
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {
		@Override protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();

			var hessian = new HessianSchurComplement_DSCC();
			var lm = new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(), hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}

	@Nested
	class LeastSquaresDSCC_Scaling extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {
		@Override protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();

			config.dampeningInitial = 1e-8;
			config.hessianScaling = true;

			var hessian = new HessianSchurComplement_DSCC();
			var lm = new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(), hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquaresSchur_DDRM {
		public LeastSquaresDDRM() {
			maxIterationsFast = 60;
		}

		@Override protected UnconstrainedLeastSquaresSchur<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();
			return FactoryOptimization.levenbergMarquardtSchur(config, false);
		}
	}

	@Nested
	class LeastSquaresDDRM_Scaling extends CommonChecksUnconstrainedLeastSquaresSchur_DDRM {
		@Override protected UnconstrainedLeastSquaresSchur<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigLevenbergMarquardt();

			config.dampeningInitial = 1e-8;
			config.hessianScaling = true;

			var hessian = new HessianSchurComplement_DDRM();
			var lm = new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DDRM(), hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}
}