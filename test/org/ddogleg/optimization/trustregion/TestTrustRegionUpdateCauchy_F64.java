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
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DDRM;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DSCC;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedOptimization;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionUpdateCauchy_F64 {

	Random rand = new Random(234);

	@Test
	public void initializeUpdate() {
		MockOwner owner = new MockOwner(null);
		TrustRegionUpdateCauchy_F64 alg = new TrustRegionUpdateCauchy_F64();

		owner.gradient.set(new double[][]{{1},{2}});
		owner.gradientNorm = NormOps_DDRM.normF(owner.gradient);
		owner.hessian.reshape(2,2);
		RandomMatrices_DDRM.fillUniform(owner.hessian,-1,1,rand);
		alg.initialize(owner,2,-1);
		alg.initializeUpdate();

		assertTrue( alg.gBg != 0 );
	}

	@Test
	public void initializeUpdate_catchNaN() {
		MockOwner owner = new MockOwner(null);
		TrustRegionUpdateCauchy_F64 alg = new TrustRegionUpdateCauchy_F64();

		owner.gradient.set(new double[][]{{1},{2}});
		owner.gradientNorm = NormOps_DDRM.normF(owner.gradient);
		owner.hessian.reshape(2,2);
		RandomMatrices_DDRM.fillUniform(owner.hessian,-1,1,rand);
		owner.hessian.data[1] = Double.NaN;
		alg.initialize(owner,2,-1);

		try {
			alg.initializeUpdate();
			fail("Exception should have been thrown");
		} catch( OptimizationException ignore){}
	}


	@Test
	public void computeUpdate_positiveDefinite() {
		double radius = 2;
		MockOwner owner = new MockOwner(null);
		TrustRegionUpdateCauchy_F64 alg = new TrustRegionUpdateCauchy_F64();
		alg.initialize(owner,2,-1);
		DMatrixRMaj p = new DMatrixRMaj(2,1);

		// Unconstrained solution is way outside the region bounds. Make sure this contrains it
		owner.hessian.set(new double[][]{{2,0.1},{0.1,1.5}});
		owner.gradientNorm = 1000;
		setGradient(owner.gradient,0.1,0.4,owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient,owner.gradientNorm,alg.direction);
		alg.gBg = owner.math.innerProduct(alg.direction,owner.hessian);
		alg.computeUpdate(p,radius);
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);

		// should be inside the bounds
		owner.gradientNorm = 0.1;
		setGradient(owner.gradient,0.1,0.4,owner.gradientNorm);
		alg.computeUpdate(p,radius);
		double n = NormOps_DDRM.normF(p);
		assertTrue(n > 0 && n < radius);

		double expectedRadius = owner.gradientNorm/alg.gBg;
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(expectedRadius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(expectedRadius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	public static void setGradient( DMatrixRMaj gradient ,
									double dirX , double dirY , double norm ) {
		double r = Math.sqrt(dirX*dirX + dirY*dirY);
		gradient.set(new double[][]{{norm*dirX/r},{norm*dirY/r}});
	}

	@Test
	public void computeUpdate_negativeDefinite() {
		MockOwner owner = new MockOwner(null);
		TrustRegionUpdateCauchy_F64 alg = new TrustRegionUpdateCauchy_F64();
		alg.initialize(owner,2,-1);
		owner.gradientNorm = 0.1;
		setGradient(owner.gradient,0.1,0.4,owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient,owner.gradientNorm,alg.direction);
		owner.hessian.set(new double[][]{{-2,0.1},{0.1,-1.5}});
		alg.gBg = owner.math.innerProduct(alg.direction,owner.hessian);
		DMatrixRMaj p = new DMatrixRMaj(2,1);

		// should hit the boundary
		owner.fx = 1000;
		alg.computeUpdate(p,2);
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(2, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(2, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);

		// shouldn't hit the boundary because -1 is the minimum function value
		owner.fx = 0;
		alg.computeUpdate(p,2);
		double n = NormOps_DDRM.normF(p);
		assertTrue(n > 0 && n < 2);
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(NormOps_DDRM.normF(p), alg.getStepLength(), UtilEjml.TEST_F64);
	}

	private static class MockOwner extends TrustRegionBase_F64<DMatrixRMaj> {

		public MockOwner(ParameterUpdate parameterUpdate) {
			super(parameterUpdate, new TrustRegionMath_DDRM());
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
		protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient, DMatrixRMaj hessian) {

		}
	}

	@Nested
	class UnconstrainedBFGS extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override
		protected UnconstrainedMinimization createSearch() {
			ConfigTrustRegion config = new ConfigTrustRegion();
			UnconMinTrustRegionBFGS_F64 tr = new UnconMinTrustRegionBFGS_F64(new TrustRegionUpdateCauchy_F64());
			tr.configure(config);
			return tr;
		}

		@Override
		public void checkBadlyScaledBrown() {
			// it just can't handle this scenario
		}
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {

		public LeastSquaresDDRM() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = new UnconLeastSqTrustRegion_F64<>(
					new TrustRegionUpdateCauchy_F64<>(), new TrustRegionMath_DDRM());
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDDRM_Scaling extends CommonChecksUnconstrainedLeastSquares_DDRM {

		public LeastSquaresDDRM_Scaling() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();
			config.scalingMinimum = 0.1; // sensitive to this parameter
			config.scalingMaximum = 1e6;
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = new UnconLeastSqTrustRegion_F64<>(
					new TrustRegionUpdateCauchy_F64<>(), new TrustRegionMath_DDRM());
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

		public LeastSquaresDSCC() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override
		protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

			UnconLeastSqTrustRegion_F64<DMatrixSparseCSC> tr = new UnconLeastSqTrustRegion_F64<>(
					new TrustRegionUpdateCauchy_F64<>(), new TrustRegionMath_DSCC());
			tr.configure(config);
			return tr;
		}
	}
}