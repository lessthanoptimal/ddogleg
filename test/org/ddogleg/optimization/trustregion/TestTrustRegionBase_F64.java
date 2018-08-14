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

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.HessianMath_DDRM;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionBase_F64 {

	@Test
	public void initialize() {
		MockTrustRegionBase_F64 alg = createFixedCost(1,0.5);

		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);
		assertEquals(1,alg.fx, UtilEjml.TEST_F64);

		assertEquals(alg.regionRadius,alg.config.regionInitial, UtilEjml.TEST_F64);
	}

	/**
	 * sees if it's checking the region radius for problems
	 */
	@Test
	public void checkConvergenceFTest_radius() {
		MockTrustRegionBase_F64 alg = createFixedCost(1,0.5);

		alg.regionRadius = 0;
		try {
			alg.checkConvergenceFTest(-1,-1);
			fail("Should have thrown an exception");
		} catch( OptimizationException ignore){}

		alg.regionRadius = Double.NaN;
		try {
			alg.checkConvergenceFTest(-1,-1);
			fail("Should have thrown an exception");
		} catch( OptimizationException ignore){}
	}

	@Test
	public void checkConvergenceFTest() {
		MockTrustRegionBase_F64 alg = createFixedCost(1,0.5);

		alg.regionRadius = 1;
		alg.config.ftol = 1e-4;

		assertTrue(alg.checkConvergenceFTest(2,2));
		assertTrue(alg.checkConvergenceFTest(2,2*(1+1e-5)));
		assertFalse(alg.checkConvergenceFTest(2,2*(1+9e-3)));
	}

	/**
	 * Prediction is low accuracy but the function decreased in value
	 */
	@Test
	public void considerUpdate() {
		// Everything goes well.
		considerUpdate(1,1.1,0.1,0.1, true);

		// Score improved by a very small amount relative to distance traveled
		considerUpdate(1,1.1,0.1,1e10, true);
		// poor prediction causes noise
		considerUpdate(1,1.1,0.001,0.1, true);

		// the model predicted a much larger gain than there was
		considerUpdate(1,1.1,2.1,0.1, true);

		// cost increased
		considerUpdate(1.0001,1,0.1,0.1, false);
	}

	protected void considerUpdate(double fx_candiate, double fx, double predictedReduction, double stepLength,
								  boolean expected )
	{
		ConfigTrustRegion config = new ConfigTrustRegion();

		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);
		alg.configure(config);

		alg.regionRadius = 2;

		assertEquals(false, alg.considerCandidate(fx_candiate,fx,predictedReduction,stepLength));

		double ratio = (fx-fx_candiate)/predictedReduction;

		if( expected ) {
			assertEquals(2*0.5,alg.regionRadius);
		} else {
			if (ratio <= 0.5) {
				assertEquals(2 * 0.5, alg.regionRadius);
			} else {
				assertEquals(Math.max(3 * stepLength, 2), alg.regionRadius);
			}
		}
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
		public void initialize(TrustRegionBase_F64<DMatrixRMaj,?> base,
							   int numberOfParameters, double minimumFunctionValue) {

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

		@Override
		public void setVerbose(PrintStream out, int level) {

		}
	}

	private static class MockTrustRegionBase_F64 extends TrustRegionBase_F64<DMatrixRMaj,HessianMath> {

		public MockTrustRegionBase_F64(ParameterUpdate<DMatrixRMaj> parameterUpdate) {
			super(parameterUpdate, new HessianMath_DDRM());
		}

		@Override
		protected double cost(DMatrixRMaj x) {
			return 0;
		}

		@Override
		protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost,
											   DMatrixRMaj gradient, HessianMath hessian) {

		}

	}
}