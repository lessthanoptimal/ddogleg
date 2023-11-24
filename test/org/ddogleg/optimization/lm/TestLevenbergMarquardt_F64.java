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

import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.HessianMath_DDRM;
import org.ddogleg.optimization.math.MatrixMath_DDRM;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestLevenbergMarquardt_F64 {
	@Test void computeStep_solve_failed() {
		var alg = new MockLevenbergMarquardt() {
			@Override protected boolean computeStep( double lambda, DMatrixRMaj gradient, DMatrixRMaj step ) {
				return false;
			}
		};

		alg.lambda = 2;

		assertFalse(alg.computeStep());
		assertEquals(8, alg.lambda, UtilEjml.TEST_F64);
	}

	/**
	 * Makes sure hessian scaling is correctly handled
	 */
	@Test void computeStep_hessianScaling() {

		var alg = new MockLevenbergMarquardt() {
			@Override protected boolean computeStep( double lambda, DMatrixRMaj gradient, DMatrixRMaj step ) {
				return true;
			}

			@Override protected void undoHessianScalingOnParameters( DMatrixRMaj p ) {
				undoCalled = true;
			}
		};

		assertTrue(alg.computeStep());
		assertFalse(alg.undoCalled);

		alg.config.hessianScaling = true;

		assertTrue(alg.computeStep());
		assertTrue(alg.undoCalled);
	}

	@Test void checkConvergenceFTest() {
		var alg = new MockLevenbergMarquardt();

		alg.config.ftol = 1e-4;

		assertTrue(alg.checkConvergenceFTest(2, 2));
		assertTrue(alg.checkConvergenceFTest(2, 2*(1 + 1e-5)));
		assertFalse(alg.checkConvergenceFTest(2, 2*(1 + 9e-3)));
	}

	@Test void cost() {
		var alg = new MockLevenbergMarquardt();

		var eq = new Equation();
		eq.process("r = rand(10,1)");
		eq.process("cost = 0.5*r'*r");
		DMatrixRMaj residuals = eq.lookupDDRM("r");
		double expected = eq.lookupDouble("cost");
		double found = alg.costFromResiduals(residuals);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	private class MockLevenbergMarquardt extends LevenbergMarquardt_F64<DMatrixRMaj, HessianMath> {
		boolean undoCalled = false;

		public MockLevenbergMarquardt() {
			super(new MatrixMath_DDRM(), new HessianMath_DDRM());
		}

		@Override protected void functionGradientHessian( DMatrixRMaj x, boolean sameStateAsResiduals,
														  DMatrixRMaj gradient, HessianMath hessian ) {}

		@Override protected void computeResiduals( DMatrixRMaj x, DMatrixRMaj residuals ) {}
	}
}