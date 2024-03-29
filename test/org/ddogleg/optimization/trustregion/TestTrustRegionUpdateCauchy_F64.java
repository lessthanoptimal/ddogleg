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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.*;
import org.ddogleg.optimization.math.*;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestTrustRegionUpdateCauchy_F64 {

	Random rand = new Random(234);

	@Test void initializeUpdate() {
		var owner = new MockOwner(null);
		var alg = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();

		owner.gradient.set(new double[][]{{1}, {2}});
		owner.gradientNorm = NormOps_DDRM.normF(owner.gradient);
		owner.hessian().reshape(2, 2);
		RandomMatrices_DDRM.fillUniform(owner.hessian(), -1, 1, rand);
		alg.initialize(owner, 2, -1);
		alg.initializeUpdate();

		assertTrue(alg.gBg != 0);
	}

	@Test void initializeUpdate_catchNaN() {
		var owner = new MockOwner(null);
		var alg = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();

		owner.gradient.set(new double[][]{{1}, {2}});
		owner.gradientNorm = NormOps_DDRM.normF(owner.gradient);
		owner.hessian().reshape(2, 2);
		RandomMatrices_DDRM.fillUniform(owner.hessian(), -1, 1, rand);
		owner.hessian().data[1] = Double.NaN;
		alg.initialize(owner, 2, -1);

		try {
			alg.initializeUpdate();
			fail("Exception should have been thrown");
		} catch (OptimizationException ignore) {
		}
	}

	@Test void computeUpdate_positiveDefinite() {
		double radius = 2;
		var owner = new MockOwner(null);
		var alg = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();
		alg.initialize(owner, 2, -1);
		var p = new DMatrixRMaj(2, 1);

		// Unconstrained solution is way outside the region bounds. Make sure this contrains it
		owner.hessian().set(new double[][]{{2, 0.1}, {0.1, 1.5}});
		owner.gradientNorm = 1000;
		setGradient(owner.gradient, 0.1, 0.4, owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient, owner.gradientNorm, alg.direction);
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		alg.computeUpdate(p, radius);
		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);

		// should be inside the bounds
		owner.gradientNorm = 0.1;
		setGradient(owner.gradient, 0.1, 0.4, owner.gradientNorm);
		alg.computeUpdate(p, radius);
		double n = NormOps_DDRM.normF(p);
		assertTrue(n > 0 && n < radius);

		double expectedRadius = owner.gradientNorm/alg.gBg;
		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(expectedRadius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(expectedRadius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	public static void setGradient( DMatrixRMaj gradient,
									double dirX, double dirY, double norm ) {
		double r = Math.sqrt(dirX*dirX + dirY*dirY);
		gradient.set(new double[][]{{norm*dirX/r}, {norm*dirY/r}});
	}

	@Test void computeUpdate_negativeDefinite() {
		var owner = new MockOwner(null);
		var alg = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();
		alg.initialize(owner, 2, -1);
		owner.gradientNorm = 0.1;
		setGradient(owner.gradient, 0.1, 0.4, owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient, owner.gradientNorm, alg.direction);
		owner.hessian().set(new double[][]{{-2, 0.1}, {0.1, -1.5}});
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		var p = new DMatrixRMaj(2, 1);

		// should hit the boundary
		owner.fx = 1000;
		alg.computeUpdate(p, 2);
		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(2, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(2, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	@SuppressWarnings({"NullAway"})
	private static class MockOwner extends TrustRegionBase_F64<DMatrixRMaj, HessianMath> {

		public MockOwner( @Nullable ParameterUpdate<DMatrixRMaj> parameterUpdate ) {
			super(parameterUpdate, new HessianMath_DDRM());
		}

		public DMatrixRMaj hessian() {
			return ((HessianMath_DDRM)hessian).getHessian();
		}

		@Override protected boolean checkConvergenceFTest( double fx, double fx_prev ) {
			return false;
		}

		@Override protected double cost( DMatrixRMaj x ) {
			return 0;
		}

		@Override protected void functionGradientHessian( DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient,
														  HessianMath hessian ) {}
	}

	@Nested
	class UnconstrainedBFGS extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override protected UnconstrainedMinimization createSearch() {
			var config = new ConfigTrustRegion();
			var tr = new UnconMinTrustRegionBFGS_F64(new TrustRegionUpdateCauchy_F64<DMatrixRMaj>(),
					new HessianBFGS_DDRM(false));
			tr.configure(config);
			return tr;
		}

		@Override public void checkBadlyScaledBrown() {
			// it just can't handle this scenario
		}
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {
		public LeastSquaresDDRM() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();
			var cauchy = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();
			var tr = new UnconLeastSqTrustRegion_F64<DMatrixRMaj>(
					cauchy, new HessianLeastSquares_DDRM(), new MatrixMath_DDRM());
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

		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();
			config.hessianScaling = true;
			var cauchy = new TrustRegionUpdateCauchy_F64<DMatrixRMaj>();
			var tr = new UnconLeastSqTrustRegion_F64<DMatrixRMaj>(
					cauchy, new HessianLeastSquares_DDRM(), new MatrixMath_DDRM());
			tr.configure(config);
			return tr;
		}

		@Override public void checkPowell() {}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

		public LeastSquaresDSCC() {
			this.checkFastConvergence = false;
			this.maxIteration = 10000;
		}

		@Override protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();

			var cauchy = new TrustRegionUpdateCauchy_F64<DMatrixSparseCSC>();
			var tr = new UnconLeastSqTrustRegion_F64<DMatrixSparseCSC>(
					cauchy, new HessianLeastSquares_DSCC(), new MatrixMath_DSCC());
			tr.configure(config);
			return tr;
		}
	}
}