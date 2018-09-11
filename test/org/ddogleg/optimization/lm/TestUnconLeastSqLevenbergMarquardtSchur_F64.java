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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.CommonChecksUnconstrainedLeastSquaresSchur_DSCC;
import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ddogleg.optimization.math.MatrixMath_DSCC;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUnconLeastSqLevenbergMarquardtSchur_F64 {
	@Test
	public void functionGradientHessian() {
		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm = declare();

		MockResiduals residuals = new MockResiduals();
		MockJacobian jacobian = new MockJacobian();
		lm.setFunction(residuals,jacobian);

		DMatrixRMaj x = new DMatrixRMaj(1,1);
		DMatrixRMaj g = new DMatrixRMaj(1,1);
		lm.functionGradientHessian(x,true,g,lm.hessian);
		assertFalse(residuals.called);

		MockHessian h = (MockHessian)lm.hessian;
		assertTrue(h.hessian);
		assertTrue(h.gradient);

		lm.functionGradientHessian(x,false,g,lm.hessian);
		assertTrue(residuals.called);
	}

	/**
	 * Heavily lifting is done elsewhere. this just make sure the correct function is called.
	 */
	@Test
	public void computeResiduals() {
		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm = declare();

		MockResiduals residuals = new MockResiduals();
		lm.setFunction(residuals,new MockJacobian());

		DMatrixRMaj x = new DMatrixRMaj(1,1);
		DMatrixRMaj r = new DMatrixRMaj(1,1);
		lm.computeResiduals(x,r);
		assertTrue(residuals.called);
	}

	private UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> declare() {
		ConfigLevenbergMarquardt config = new ConfigLevenbergMarquardt();

		HessianSchurComplement_DSCC hessian = new MockHessian();
		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm =
				new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(), hessian);
		lm.configure(config);
		return lm;
	}

	public static class MockResiduals implements FunctionNtoM {

		boolean called = false;
		@Override
		public void process(double[] input, double[] output) {
			called = true;
		}

		@Override
		public int getNumOfInputsN() {
			return 2;
		}

		@Override
		public int getNumOfOutputsM() {
			return 3;
		}
	}

	private class MockJacobian implements SchurJacobian<DMatrixSparseCSC> {
		boolean called = false;
		@Override
		public void process(double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right) {
			called = true;
		}

		@Override
		public int getNumOfInputsN() {
			return 2;
		}

		@Override
		public int getNumOfOutputsM() {
			return 3;
		}
	}

	private class MockHessian extends HessianSchurComplement_DSCC {
		boolean hessian = false;
		boolean gradient = false;

		@Override
		public void computeHessian(DMatrixSparseCSC jacLeft, DMatrixSparseCSC jacRight) {
			this.hessian = true;
		}

		@Override
		public void computeGradient(DMatrixSparseCSC jacLeft, DMatrixSparseCSC jacRight, DMatrixRMaj residuals, DMatrixRMaj gradient) {
			this.gradient = true;
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {
		@Override
		protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigLevenbergMarquardt config = new ConfigLevenbergMarquardt();

			HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
			UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm =
					new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(),hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}

	@Nested
	class LeastSquaresDSCC_Scaling extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {
		@Override
		protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigLevenbergMarquardt config = new ConfigLevenbergMarquardt();

			config.dampeningInitial = 1e-8;
			config.hessianScaling = true;

			HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
			UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm =
					new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(),hessian);
			lm.configure(config);
//			lm.setVerbose(System.out,0);
			return lm;
		}
	}
}