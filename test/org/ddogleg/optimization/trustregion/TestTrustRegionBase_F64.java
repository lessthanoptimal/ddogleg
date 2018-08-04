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

import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.HessianMath_DDRM;
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

		// commented out because Hessian is initialized by a derived class
//		assertEquals(2,alg.hessian.numRows);
//		assertEquals(2,alg.hessian.numCols);

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
		DMatrixRMaj H = ((HessianMath_DDRM)alg.hessian).getHessian();
		H.reshape(2,2);

		double x[] = new double[]{1,2};
		alg.initialize(x,2,0);

		RandomMatrices_DDRM.fillUniform(alg.gradient,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(H,-1,1,rand);

		Equation eq = new Equation();
		eq.alias(alg.p,"p",alg.gradient,"g",H,"H");

		eq.process("reduction = -g'*p - 0.5*p'*H*p");
		double expected = eq.lookupDouble("reduction");
		double found = alg.computePredictedReduction(alg.p);

		assertEquals(expected, found, UtilEjml.TEST_F64);
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
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);

		DMatrixRMaj orig = new DMatrixRMaj(new double[][]{{1},{0.1},{-4}});

		// no clamping
		DMatrixRMaj scaling = orig.copy();
		alg.computeScaling(scaling,-1000,1000);
		assertEquals(1,scaling.get(0));
		assertEquals(Math.sqrt(0.1),scaling.get(1));
		assertEquals(2,scaling.get(2));


		// Minimum
		scaling = orig.copy();
		alg.computeScaling(scaling,1.1,1000);
		assertEquals(1.1,scaling.get(0));
		assertEquals(1.1,scaling.get(1));
		assertEquals(2,scaling.get(2));

		// Maximum
		scaling = orig.copy();
		alg.computeScaling(scaling,-1000,1.5);
		assertEquals(1,scaling.get(0));
		assertEquals(Math.sqrt(0.1),scaling.get(1));
		assertEquals(1.5,scaling.get(2));
	}

	@Test
	public void applyScaling() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);

		DMatrixRMaj H = ((HessianMath_DDRM)alg.hessian).getHessian();
		H.reshape(4,4);
		alg.gradient = RandomMatrices_DDRM.rectangle(4,1,-1,2,rand);
		RandomMatrices_DDRM.fillUniform(H,rand);

		DMatrixRMaj _g = alg.gradient.copy();
		DMatrixRMaj _H = H.copy();

		alg.scaling = new DMatrixRMaj(new double[][]{{1},{0.1},{2},{0.6}});

		alg.applyScaling();

		for (int row = 0; row < 4; row++) {
			double expected = _g.get(row,0)/alg.scaling.get(row);
			assertEquals(expected, alg.gradient.get(row), UtilEjml.TEST_F64);

			for (int col = 0; col < 4; col++) {
				expected = _H.get(row,col)/(alg.scaling.get(row)*alg.scaling.get(col));
				assertEquals(expected, H.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void undoScalingOnParameters() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);
		alg.scaling = new DMatrixRMaj(new double[][]{{1},{0.1},{2},{0.6}});

		DMatrixRMaj p = RandomMatrices_DDRM.rectangle(4,1,-1,2,rand);
		DMatrixRMaj o = p.copy();

		alg.undoScalingOnParameters(p);

		for (int row = 0; row < 4; row++) {
			double expected = o.get(row, 0) / alg.scaling.get(row);
			assertEquals(expected, p.get(row), UtilEjml.TEST_F64);
		}
	}

	@Test
	public void isScaling() {
		MockTrustRegionBase_F64 alg = new MockTrustRegionBase_F64(null);
		alg.config.scalingMaximum = -1;
		alg.config.scalingMinimum = 1;
		assertFalse(alg.isScaling());
		alg.config.scalingMaximum = 2;
		alg.config.scalingMinimum = 2;
		assertFalse(alg.isScaling());
		alg.config.scalingMaximum = 1;
		alg.config.scalingMinimum = -1;
		assertTrue(alg.isScaling());
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
		public void setVerbose(boolean verbose) {

		}
	}

	private static class MockTrustRegionBase_F64 extends TrustRegionBase_F64<DMatrixRMaj,HessianMath> {

		public MockTrustRegionBase_F64(ParameterUpdate<DMatrixRMaj> parameterUpdate) {
			super(parameterUpdate, new HessianMath_DDRM());
		}

		@Override
		protected boolean checkConvergenceFTest(double fx, double fx_prev) {
			return false;
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