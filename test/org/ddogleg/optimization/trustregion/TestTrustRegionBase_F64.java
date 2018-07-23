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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.equation.Equation;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionBase_F64 {
	Random rand = new Random(234);

	@Test
	public void updateState() {
		fail("Implement");
	}

	@Test
	public void swapOldAndNewParameters() {
		fail("Implement");
	}

	/**
	 * Prediction is low accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate_lowacc_decrease() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		double x[] = new double[]{1,2};
		alg.initialize(x,1e-4,1e-6,2,0);

//		alg.considerUpdate();
	}

	/**
	 * Prediction is medium accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate_mediumAcc_decrease() {

	}

	/**
	 * Prediction is high accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate_highAcc_decrease() {

	}

	/**
	 * Prediction is high accuracy but the function increased in value
	 */
	@Test
	public void considerUpdate_highAcc_increase() {

	}

	@Test
	public void computePredictionAccuracy() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		double x[] = new double[]{1,2};
		alg.initialize(x,1e-4,1e-6,2,0);

		alg.fx = 1.2;
		alg.fx_prev = 1.5;
		RandomMatrices_DDRM.fillUniform(alg.gradient,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(alg.hessian,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(alg.p,-1,1,rand);

		Equation eq = new Equation();
		eq.alias(alg.p,"p",alg.gradient,"g",alg.hessian,"H",alg.fx_prev,"fx_p");

		eq.process("q=fx_p + g'*p + 0.5*p'*H*p");
		double q = eq.lookupDouble("q");
		double expected = (alg.fx_prev-alg.fx)/(alg.fx_prev - q);
		double found = alg.computePredictionAccuracy(alg.p);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	/**
	 * Test a pathological case with zeros for gradient and hessian
	 */
	@Test
	public void computePredictionAccuracy_pathological() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		double x[] = new double[]{1,2};
		alg.initialize(x,1e-4,1e-6,2,0);

		alg.fx = 1.2;
		alg.fx_prev = 1.5;
		RandomMatrices_DDRM.fillUniform(alg.p,-1,1,rand);

		double found = alg.computePredictionAccuracy(alg.p);

		assertEquals(0, found, UtilEjml.TEST_F64);
	}

	@Test
	public void checkConvergence() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);

		// just check ftol only
		alg.setFtol(1e-4);
		alg.setGtol(-1);
		alg.regionRadius = 1; // can't be zero
		alg.fx = 2;
		alg.fx_prev = 2.5;
		alg.numberOfParameters = 0; // turns off gtol test
		assertFalse(alg.checkConvergence());
		alg.fx_prev = 2*1.0000001;
		assertTrue(alg.checkConvergence());

		// gtol only
		alg.setFtol(-1);
		alg.setGtol(1e-4);
		alg.numberOfParameters = 2;
		alg.gradient.data = new double[]{-0.1,0.1};
		assertFalse(alg.checkConvergence());
		alg.gradient.data[0] = -1e-5;
		assertFalse(alg.checkConvergence());
		alg.gradient.data[1] = 1e-5;
		assertTrue(alg.checkConvergence());
	}

	private static class MockParameterUpdate implements TrustRegionBase_F64.ParameterUpdate<DMatrixRMaj> {

		@Override
		public void initialize(TrustRegionBase_F64<DMatrixRMaj> base,
							   int numberOfParameters,
							   double minimumFunctionValue) {

		}

		@Override
		public void initializeUpdate() {

		}

		@Override
		public boolean computeUpdate(DMatrixRMaj p, double regionRadius) {
			return false;
		}
	}

	private static class MockTrustRegionBase_F64 extends TrustRegionBase_F64<DMatrixRMaj> {

		public MockTrustRegionBase_F64(ParameterUpdate parameterUpdate) {
			super(parameterUpdate, new TrustRegionMath_DDRM());
		}

		@Override
		protected void updateDerivedState(DMatrixRMaj x) {

		}

		@Override
		protected double costFunction(DMatrixRMaj x) {
			return 0;
		}
	}
}