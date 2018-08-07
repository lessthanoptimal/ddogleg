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

import org.ddogleg.optimization.GaussNewtonBase_F64;
import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.HessianMath_DDRM;
import org.ddogleg.optimization.math.MatrixMath_DDRM;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestLevenbergMarquardt_F64 {

	Random rand = new Random(234);

	/**
	 * If the initial state meets the convergence criterial it should be put into the appropriate mode
	 */
	@Test
	public void initialize_check_converged() {

		final double _residuals[] = new double[]{1,0,0,0};

		MockLevenbergMarquardt alg = new MockLevenbergMarquardt() {
			@Override
			protected void computeResiduals(DMatrixRMaj x, DMatrixRMaj residuals) {
				System.arraycopy(_residuals,0,residuals.data,0,4);
			}
		};
		alg.config.ftol = 1e-6;
		double x[] = new double[]{1,2};
		alg.initialize(x,2,4);
		assertEquals(GaussNewtonBase_F64.Mode.FULL_STEP,alg.mode());

		_residuals[0] = 1e-7;
		alg.initialize(x,2,4);
		assertEquals(GaussNewtonBase_F64.Mode.CONVERGED,alg.mode());
	}

	@Test
	public void computeAndConsiderNew_solve_failed() {
		MockLevenbergMarquardt alg = new MockLevenbergMarquardt() {
			@Override
			protected boolean computeStep( double lambda, DMatrixRMaj gradient , DMatrixRMaj step ) {
				return false;
			}
		};

		alg.lambda = 2;

		assertFalse(alg.computeAndConsiderNew());
		assertEquals(8,alg.lambda, UtilEjml.TEST_F64);
	}

	@Test
	public void checkConvergenceFTest() {
		MockLevenbergMarquardt alg = new MockLevenbergMarquardt();

		alg.config.ftol = 1e-5;

		DMatrixRMaj r = new DMatrixRMaj(2,1);
		r.data = new double[]{-0.1,0.1};
		assertFalse(alg.checkConvergenceFTest(r));
		r.data[0] = -2e-5;
		assertFalse(alg.checkConvergenceFTest(r));
		r.data[1] = 2e-5;
		assertFalse(alg.checkConvergenceFTest(r));
		r.data[1] = 1e-5;
		assertFalse(alg.checkConvergenceFTest(r));
		r.data[0] = 1e-5;
		assertTrue(alg.checkConvergenceFTest(r));
	}

	@Test
	public void cost() {
		MockLevenbergMarquardt alg = new MockLevenbergMarquardt();

		Equation eq = new Equation();
		eq.process("r = rand(10,1)");
		eq.process("cost = 0.5*r'*r");
		DMatrixRMaj residuals = eq.lookupDDRM("r");
		double expected = eq.lookupDouble("cost");
		double found = alg.costFromResiduals(residuals);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	/**
	 * Checks to see if the diagonal elements are computed as expected
	 */
	@Test
	public void computeStep() {
		fail("Implement");
	}


	private class MockLevenbergMarquardt extends LevenbergMarquardt_F64<DMatrixRMaj, HessianMath> {

		public MockLevenbergMarquardt() {
			super(new MatrixMath_DDRM(), new HessianMath_DDRM());
		}

		@Override
		protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsResiduals, DMatrixRMaj gradient, HessianMath hessian) {

		}

		@Override
		protected void computeResiduals(DMatrixRMaj x, DMatrixRMaj residuals) {

		}
	}

}