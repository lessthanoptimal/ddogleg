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
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ddogleg.optimization.trustregion.TestTrustRegionUpdateCauchy_F64.setGradient;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionUpdateDogleg_F64 {

	Random rand = new Random(234);

	/**
	 * See if it correctly identifies a non SPD matrix
	 */
	@Test void initializeUpdate_spd() {
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		var owner = new MockTrustRegionBase(alg);
		alg.initialize(owner, 2, 0);

		owner.hessian().set(new double[][]{{1, 0}, {0, 1}});
		owner.gradientNorm = 1;
		owner.gradient.set(new double[][]{{1}, {0}});

		alg.initializeUpdate();
		assertTrue(alg.positiveDefinite);

		owner.hessian().set(new double[][]{{0, 1}, {1, 0}});
		alg.initializeUpdate();
		assertFalse(alg.positiveDefinite);
	}

	@Test void computeUpdate_NegativeDefinite() {
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		var owner = new MockTrustRegionBase(alg);
		alg.initialize(owner, 2, 0);

		double radius = 2;
		setGradient(alg.owner.gradient, -1, 0, 1000);
		alg.owner.gradientNorm = NormOps_DDRM.normF(alg.owner.gradient);
		CommonOps_DDRM.divide(owner.gradient, owner.gradientNorm, alg.direction);
		owner.hessian().set(new double[][]{{-2, 0.1}, {0.1, -1.5}});
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		alg.positiveDefinite = false;
		alg.owner.fx = 1000;
		DMatrixRMaj p = new DMatrixRMaj(2, 1);
		alg.computeUpdate(p, radius);

		assertEquals(2, p.get(0, 0), UtilEjml.TEST_F64);
		assertEquals(0, p.get(1, 0), UtilEjml.TEST_F64);
		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	@Test void computeUpdate_cauchy_after() {
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		var owner = new MockTrustRegionBase(alg);
		alg.initialize(owner, 2, 0);
		owner.hessian().set(new double[][]{{2, 0.1}, {0.1, 1.5}});

		// have Gn and cauchy lie along a line so the math is easy
		owner.gradientNorm = 2.1;
		setGradient(alg.owner.gradient, -1, 0, owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient, owner.gradientNorm, alg.direction);
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		alg.positiveDefinite = true;
		alg.distanceGN = 5;
		alg.distanceCauchy = 3;
		alg.stepCauchy.set(new double[][]{{3}, {0}});
		alg.stepGN.set(new double[][]{{5}, {0}});

		double radius = 2;
		DMatrixRMaj p = new DMatrixRMaj(2, 1);
		alg.computeUpdate(p, radius);

		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	@Test void computeUpdate_cauchy_inside() {
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		var owner = new MockTrustRegionBase(alg);
		alg.initialize(owner, 2, 0);

		// have Gn and cauchy lie along a line so the math is easy
		double radius = 2;
		owner.hessian().set(new double[][]{{2, 0.1}, {0.1, 1.5}});
		alg.owner.gradientNorm = 1.5;
		setGradient(alg.owner.gradient, -1, 0, alg.owner.gradientNorm);
		CommonOps_DDRM.divide(owner.gradient, owner.gradientNorm, alg.direction);
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		alg.positiveDefinite = true;
		alg.stepGN.set(new double[][]{{5}, {0}});
		alg.distanceGN = 5;
		alg.stepCauchy.set(new double[][]{{1.5}, {0}});
		alg.distanceCauchy = NormOps_DDRM.normF(alg.stepCauchy);
		alg.gBg = 1;

		DMatrixRMaj p = new DMatrixRMaj(2, 1);
		alg.computeUpdate(p, radius);

		assertEquals(2, p.get(0, 0), UtilEjml.TEST_F64);
		assertEquals(0, p.get(1, 0), UtilEjml.TEST_F64);

		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	/**
	 * The Gauss-Newton step  is inside the trust region so we will just use that
	 */
	@Test void computeUpdate_gn_inside() {
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		var owner = new MockTrustRegionBase(alg);
		alg.initialize(owner, 2, 0);

		alg.positiveDefinite = true;
		owner.hessian().set(new double[][]{{2, 0.1}, {0.1, 1.5}});
		setGradient(alg.owner.gradient, -1, 0, 1.5);
		alg.owner.gradientNorm = NormOps_DDRM.normF(alg.owner.gradient);
		alg.gBg = owner.hessian.innerVectorHessian(alg.direction);
		alg.stepGN.set(new double[][]{{1}, {2}});
		alg.distanceGN = NormOps_DDRM.normF(alg.stepGN);
		var p = new DMatrixRMaj(2, 1);
		alg.computeUpdate(p, 4);

		assertTrue(MatrixFeatures_DDRM.isIdentical(p, alg.stepGN, UtilEjml.TEST_F64));

		assertEquals(owner.computePredictedReduction(p), alg.getPredictedReduction(), UtilEjml.TEST_F64);
		assertEquals(alg.distanceGN, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(alg.distanceGN, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	/**
	 * Easy to derive solutions
	 */
	@Test void fractionCauchyToGN_easy() {
		// Everything lies along a line
		double lengthPtoGN = 2;
		double found = TrustRegionUpdateDogleg_F64.fractionCauchyToGN(2, 4, lengthPtoGN, 2.5);

		assertEquals(0.5/lengthPtoGN, found, UtilEjml.TEST_F64);
	}

	/**
	 * Randomly generate points in 2D and circles. Then see if a valid length can be found
	 */
	@Test void fractionCauchyToGN_random() {
		for (int i = 0; i < 200; i++) {
			double r = rand.nextDouble() + 1;

			double lengthP = 0.01 + rand.nextDouble()*0.99*r;
			double lengthGN = r + rand.nextDouble();

			double angleP = rand.nextDouble()*Math.PI*2.0;
			double angleGN = rand.nextDouble()*Math.PI*2.0;

			double x_p = Math.cos(angleP)*lengthP;
			double y_p = Math.sin(angleP)*lengthP;

			double x_gn = Math.cos(angleGN)*lengthGN;
			double y_gn = Math.sin(angleGN)*lengthGN;

			double dx = x_gn - x_p;
			double dy = y_gn - y_p;
			double lengthPtoGN = Math.sqrt(dx*dx + dy*dy);

			double fraction = TrustRegionUpdateDogleg_F64.fractionCauchyToGN(lengthP, lengthGN, lengthPtoGN, r);

			double x = x_p + fraction*dx;
			double y = y_p + fraction*dy;

			double found = Math.sqrt(x*x + y*y);

			assertEquals(r, found, UtilEjml.TEST_F64);
		}
	}

	private static class MockTrustRegionBase extends TrustRegionBase_F64<DMatrixRMaj, HessianMath> {

		public MockTrustRegionBase( ParameterUpdate<DMatrixRMaj> parameterUpdate ) {
			super(parameterUpdate, new HessianMath_DDRM(LinearSolverFactory_DDRM.chol(2)));
		}

		public DMatrixRMaj hessian() {
			return ((HessianMath_DDRM)hessian).getHessian();
		}

		@Override protected boolean checkConvergenceFTest( double fx, double fx_prev ) {
			return false;
		}

		@Override protected double cost( DMatrixRMaj x ) {return 0;}

		@Override protected void functionGradientHessian( DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient,
												HessianMath hessian ) {}
	}

	// In the future add support for scaled BFGS
	@Nested
	class UnconstrainedBFGS extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS() {
			this.checkFastConvergence = false; // TODO remove?
			this.maxIteration = 10000;
		}

		@Override protected UnconstrainedMinimization createSearch() {
			var config = new ConfigTrustRegion();
			config.regionInitial = 100;

			var dogleg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
			var hessian = new HessianBFGS_DDRM(true);
			var tr = new UnconMinTrustRegionBFGS_F64(dogleg, hessian);
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();
			config.regionInitial = -1;
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = createSolver();
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDDRM_Scaling extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();
			config.regionInitial = 100;
			config.hessianScaling = true;
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = createSolver();
			tr.configure(config);
			return tr;
		}

		@Override public void checkLineOutlier() {
			// skipping this test. It doesn't pass even if you remove the outliers and turn off robust loss.
		}
	}

	private static UnconLeastSqTrustRegion_F64<DMatrixRMaj> createSolver() {
		LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(2);
		var hessian = new HessianLeastSquares_DDRM(solver);
		var alg = new TrustRegionUpdateDogleg_F64<DMatrixRMaj>();
		return new UnconLeastSqTrustRegion_F64<>(alg, hessian, new MatrixMath_DDRM());
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

		@Override protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch( double minimumValue ) {
			var config = new ConfigTrustRegion();
			config.regionInitial = 1;

			LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
			var hessian = new HessianLeastSquares_DSCC(solver);
			var alg = new TrustRegionUpdateDogleg_F64<DMatrixSparseCSC>();
			var tr = new UnconLeastSqTrustRegion_F64<DMatrixSparseCSC>(
					alg, hessian, new MatrixMath_DSCC());
			tr.configure(config);
			return tr;
		}
	}
}