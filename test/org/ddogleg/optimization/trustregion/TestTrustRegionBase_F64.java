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
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionBase_F64 {
	Random rand = new Random(234);

	@Test
	public void initialize() {
		MockTrustRegionBase_F64 alg = createFixedCost(1,2);

		alg.totalFullSteps = 10;
		alg.totalRetries = 11;

		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);

		assertEquals(1,alg.fx, UtilEjml.TEST_F64);
		assertEquals(2,alg.x.numRows);
		assertEquals(2,alg.x_next.numRows);
		assertEquals(2,alg.gradient.numRows);
		assertEquals(2,alg.hessian.numRows);
		assertEquals(2,alg.hessian.numCols);

		assertEquals(alg.regionRadius,alg.config.regionInitial, UtilEjml.TEST_F64);
		assertEquals(0,alg.totalFullSteps);
		assertEquals(0,alg.totalRetries);
		assertEquals(2,alg.numberOfParameters);

		assertEquals(TrustRegionBase_F64.Mode.FULL_STEP,alg.mode);

	}

	@Test
	public void swapOldAndNewParameters() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		DMatrixRMaj next = alg.x_next;
		alg.swapOldAndNewParameters();
		assertTrue(next==alg.x);
		assertTrue(next!=alg.x_next);
	}

	/**
	 * Prediction is low accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate() {
		// low accuracy prediction and decreasing score. don't accept and reduce region size
		considerUpdate(2.5, 0.01, 0.25, false);
		// medium accuracy prediction and decreasing score.  keep region size and accept
		considerUpdate(2.5, 0.4, 1.0, true);
		// high accuracy. Increase region size
		considerUpdate(2.5, 1.0, 2.0, true);

		// high accuracy but decreased
		considerUpdate(3.1, 1.0, 0.25, false);
	}

	protected void considerUpdate(double cost, double predAcc, double radiusFrac, boolean update) {
		MockTrustRegionBase_F64 alg = createFixedCost(cost,predAcc);
		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);

		alg.fx_prev = 3;

		// it should reduce the region size
		assertEquals(update, alg.considerUpdate(alg.p, predAcc==1.0));
		assertEquals(radiusFrac*alg.config.regionInitial,alg.regionRadius, UtilEjml.TEST_F64);
	}

	private MockTrustRegionBase_F64 createFixedCost( double cost , double predAcc ) {
		MockParameterUpdate update = new MockParameterUpdate();
		return new MockTrustRegionBase_F64(update) {
			@Override
			protected double costFunction(DMatrixRMaj x) {
				return cost;
			}

			@Override
			protected double computePredictionAccuracy(DMatrixRMaj p) {
				return predAcc;
			}
		};
	}

	@Test
	public void computePredictionAccuracy() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);

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
		alg.initialize(x,2,0);

		alg.hessian.zero();
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
		alg.getConfig().ftol = 1e-4;
		alg.getConfig().gtol = -1;
		alg.regionRadius = 1; // can't be zero
		alg.fx = 2;
		alg.fx_prev = 2.5;
		alg.numberOfParameters = 0; // turns off gtol test
		assertFalse(alg.checkConvergence());
		alg.fx_prev = 2*1.0000001;
		assertTrue(alg.checkConvergence());

		// gtol only
		alg.getConfig().ftol = -1;
		alg.getConfig().gtol = 1e-4;
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