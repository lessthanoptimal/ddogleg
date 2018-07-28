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
		MockTrustRegionBase_F64 alg = createFixedCost(1,0.5);

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

	/**
	 * Prediction is low accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate() {
		// Everything goes well.
		considerUpdate(1,1.1,0.1,0.1, TrustRegionBase_F64.Convergence.ACCEPT);

		// Score improved by a very small amount relative to distance traveled
		considerUpdate(1,1.1,0.1,1e10, TrustRegionBase_F64.Convergence.ACCEPT);
		// poor prediction causes noise
		considerUpdate(1,1.1,0.001,0.1, TrustRegionBase_F64.Convergence.ACCEPT);

		// the model predicted a much larger gain than there was
		considerUpdate(1,1.1,2.1,0.1, TrustRegionBase_F64.Convergence.ACCEPT);

		// cost increased
		considerUpdate(1.0001,1,0.1,0.1, TrustRegionBase_F64.Convergence.REJECT);
	}

	protected void considerUpdate(double fx_candiate, double fx, double predictedReduction, double stepLength,
								  TrustRegionBase_F64.Convergence expected )
	{
		ConfigTrustRegion config = new ConfigTrustRegion();

		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);
		alg.configure(config);

		alg.regionRadius = 2;

		assertEquals(expected, alg.considerCandidate(fx_candiate,fx,predictedReduction,stepLength));

		double ratio = (fx-fx_candiate)/predictedReduction;

		switch( expected ) {
			case REJECT:
				assertEquals(2*0.5,alg.regionRadius);
				break;

			default:
				if( ratio <= 0.5 ) {
					assertEquals(2*0.5,alg.regionRadius);
				} else {
					assertEquals(Math.max(3*stepLength,2),alg.regionRadius);
				}
		}
	}

	@Test
	public void computePredictedReduction() {
		MockParameterUpdate update = new MockParameterUpdate();
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(update);

		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);

		RandomMatrices_DDRM.fillUniform(alg.gradient,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(alg.hessian,-1,1,rand);

		Equation eq = new Equation();
		eq.alias(alg.p,"p",alg.gradient,"g",alg.hessian,"H");

		eq.process("reduction = -g'*p - 0.5*p'*H*p");
		double expected = eq.lookupDouble("reduction");
		double found = alg.computePredictedReduction(alg.p);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	/**
	 * If noise is applied sameStateAsCost should be false
	 */
	@Test
	public void computeAndConsiderNew_sameStateAsCost() {
		fail("implement");
	}

	@Test
	public void checkConvergenceFTest() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);

		alg.getConfig().ftol = 1e-4;
		alg.getConfig().gtol = -1;
		alg.regionRadius = 1; // can't be zero
		alg.numberOfParameters = 0; // turns off gtol test
		assertFalse(alg.checkConvergenceFTest(2,2.5));
		assertTrue(alg.checkConvergenceFTest(2,2*1.00000001));
	}

	@Test
	public void checkConvergenceGTest() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);

		alg.config.gtol = 1e-5;

		DMatrixRMaj g = new DMatrixRMaj(2,1);
		g.data = new double[]{-0.1,0.1};
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[0] = -2e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[1] = 2e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[1] = 1e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[0] = 1e-5;
		assertTrue(alg.checkConvergenceGTest(g));
	}

	@Test
	public void computeScaling() {
		fail("Implement");
	}

	@Test
	public void applyScaling() {
		fail("Implement");
	}

	@Test
	public void undoScalingOnParameters() {
		fail("Implement");
	}

	@Test
	public void isScaling() {
		fail("Implement");
	}

	private MockTrustRegionBase_F64 createFixedCost( double cost , double predictedReduction ) {
		MockParameterUpdate update = new MockParameterUpdate() {
			@Override
			public double getPredictedReduction() {
				return predictedReduction;
			}
		};
		return new MockTrustRegionBase_F64(update) {
			@Override
			protected double cost(DMatrixRMaj x) {
				return cost;
			}
		};
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
		public void computeUpdate(DMatrixRMaj p, double regionRadius) {

		}

		@Override
		public double getPredictedReduction() {
			return 0;
		}

		@Override
		public double getStepLength() {
			return 0;
		}
	}

	private static class MockTrustRegionBase_F64 extends TrustRegionBase_F64<DMatrixRMaj> {

		public MockTrustRegionBase_F64(ParameterUpdate parameterUpdate) {
			super(parameterUpdate, new TrustRegionMath_DDRM());
		}

		@Override
		protected double cost(DMatrixRMaj x) {
			return 0;
		}

		@Override
		protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient, DMatrixRMaj hessian) {

		}
	}
}