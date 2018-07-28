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

import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DDRM;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DSCC;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedOptimization;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.interfaces.linsol.LinearSolverDense;
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
	@Test
	public void initializeUpdate_spd() {
		LinearSolverDense<DMatrixRMaj> chol = LinearSolverFactory_DDRM.chol(2);
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> alg = new TrustRegionUpdateDogleg_F64<>(chol);
		alg.initialize(new MockTrustRegionBase(alg),2,0);

		alg.owner.hessian.set(new double[][]{{1,0},{0,1}});
		alg.owner.gradientNorm = 1;
		alg.owner.gradient.set(new double[][]{{1},{0}});

		alg.initializeUpdate();
		assertTrue(alg.positiveDefinite);

		alg.owner.hessian.set(new double[][]{{0,1},{1,0}});
		alg.initializeUpdate();
		assertFalse(alg.positiveDefinite);
	}

	@Test
	public void computeUpdate_NegativeDefinite() {
		TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDogleg_F64();
		MockTrustRegionBase owner = new MockTrustRegionBase(alg);
		alg.initialize(owner,2,0);

		double radius = 2;
		setGradient(alg.owner.gradient,-1,0,1000);
		alg.owner.gradientNorm = NormOps_DDRM.normF(alg.owner.gradient);
		owner.hessian.set(new double[][]{{-2,0.1},{0.1,-1.5}});
		alg.gBg = owner.math.innerProduct(owner.gradient,owner.hessian);
		alg.positiveDefinite = false;
		alg.owner.fx = 1000;
		DMatrixRMaj p = new DMatrixRMaj(2,1);
		alg.computeUpdate(p,radius);

		assertEquals(2,p.get(0,0), UtilEjml.TEST_F64);
		assertEquals(0,p.get(1,0), UtilEjml.TEST_F64);
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);

		alg.owner.fx = 0.5;
		alg.computeUpdate(p,radius);

		assertEquals(0.5,p.get(0,0), UtilEjml.TEST_F64);
		assertEquals(0,p.get(1,0), UtilEjml.TEST_F64);
		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(0.5, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(0.5, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	@Test
	public void computeUpdate_cauchy_after() {
		TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDogleg_F64();
		MockTrustRegionBase owner = new MockTrustRegionBase(alg);
		alg.initialize(owner,2,0);
		owner.hessian.set(new double[][]{{2,0.1},{0.1,1.5}});

		// have Gn and cauchy lie along a line so the math is easy
		owner.gradientNorm = 2.1;
		setGradient(alg.owner.gradient,-1,0,owner.gradientNorm);
		alg.gBg = owner.math.innerProduct(owner.gradient,owner.hessian);
		alg.positiveDefinite = true;
		alg.distanceGN = 5;
		alg.distanceCauchy = 3;
		alg.stepCauchy.set(new double[][]{{3},{0}});
		alg.stepGN.set(new double[][]{{5},{0}});

		double radius = 2;
		DMatrixRMaj p = new DMatrixRMaj(2,1);
		alg.computeUpdate(p,radius);

		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	@Test
	public void computeUpdate_cauchy_inside() {
		TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDogleg_F64();
		MockTrustRegionBase owner = new MockTrustRegionBase(alg);
		alg.initialize(owner,2,0);

		// have Gn and cauchy lie along a line so the math is easy
		double radius = 2;
		owner.hessian.set(new double[][]{{2,0.1},{0.1,1.5}});
		setGradient(alg.owner.gradient,-1,0,1.5);
		alg.owner.gradientNorm = NormOps_DDRM.normF(alg.owner.gradient);
		alg.gBg = owner.math.innerProduct(owner.gradient,owner.hessian);
		alg.positiveDefinite = true;
		alg.stepGN.set(new double[][]{{5},{0}});
		alg.distanceGN = 5;
		alg.stepCauchy.set(new double[][]{{1.5},{0}});
		alg.distanceCauchy = NormOps_DDRM.normF(alg.stepCauchy);
		alg.gBg = 1;

		DMatrixRMaj p = new DMatrixRMaj(2,1);
		alg.computeUpdate(p,radius);

		assertEquals(2,p.get(0,0), UtilEjml.TEST_F64);
		assertEquals(0,p.get(1,0), UtilEjml.TEST_F64);

		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(radius, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(radius, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	/**
	 * The Gauss-Newton step  is inside the trust region so we will just use that
	 */
	@Test
	public void computeUpdate_gn_inside() {
		TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDogleg_F64();
		MockTrustRegionBase owner = new MockTrustRegionBase(alg);
		alg.initialize(owner,2,0);

		alg.positiveDefinite = true;
		owner.hessian.set(new double[][]{{2,0.1},{0.1,1.5}});
		setGradient(alg.owner.gradient,-1,0,1.5);
		alg.owner.gradientNorm = NormOps_DDRM.normF(alg.owner.gradient);
		alg.gBg = owner.math.innerProduct(owner.gradient,owner.hessian);
		alg.stepGN.set(new double[][]{{1},{2}});
		alg.distanceGN = NormOps_DDRM.normF(alg.stepGN);
		DMatrixRMaj p = new DMatrixRMaj(2,1);
		alg.computeUpdate(p,4);

		assertTrue(MatrixFeatures_DDRM.isIdentical(p,alg.stepGN,UtilEjml.TEST_F64));

		assertEquals(owner.computePredictedReduction(p),alg.getPredictedReduction(),UtilEjml.TEST_F64);
		assertEquals(alg.distanceGN, alg.getStepLength(), UtilEjml.TEST_F64);
		assertEquals(alg.distanceGN, NormOps_DDRM.normF(p), UtilEjml.TEST_F64);
	}

	/**
	 * Easy to derive solutions
	 */
	@Test
	public void fractionCauchyToGN_easy() {
		// Everything lies along a line
		double lengthPtoGN = 2;
		double found = TrustRegionUpdateDogleg_F64.fractionCauchyToGN(2,4,lengthPtoGN,2.5);

		assertEquals(0.5/lengthPtoGN,found,UtilEjml.TEST_F64);
	}

	/**
	 * Randomly generate points in 2D and circles. Then see if a valid length can be found
	 */
	@Test
	public void fractionCauchyToGN_random() {

		for (int i = 0; i < 200; i++) {
			double r = rand.nextDouble()+1;

			double lengthP = 0.01+rand.nextDouble()*0.99*r;
			double lengthGN = r + rand.nextDouble();

			double angleP = rand.nextDouble()*Math.PI*2.0;
			double angleGN = rand.nextDouble()*Math.PI*2.0;

			double x_p = Math.cos(angleP)*lengthP;
			double y_p = Math.sin(angleP)*lengthP;

			double x_gn = Math.cos(angleGN)*lengthGN;
			double y_gn = Math.sin(angleGN)*lengthGN;

			double dx = x_gn-x_p;
			double dy = y_gn-y_p;
			double lengthPtoGN = Math.sqrt(dx*dx + dy*dy);

			double fraction = TrustRegionUpdateDogleg_F64.fractionCauchyToGN(lengthP,lengthGN,lengthPtoGN,r);

			double x = x_p + fraction*dx;
			double y = y_p + fraction*dy;

			double found = Math.sqrt(x*x + y*y);

			assertEquals(r,found, UtilEjml.TEST_F64);
		}
	}

	private static class MockTrustRegionBase extends TrustRegionBase_F64<DMatrixRMaj> {

		public MockTrustRegionBase(ParameterUpdate parameterUpdate) {
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

	@Nested
	class UnconstrainedBFGS extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS() {
			this.checkFastConvergence = false; // TODO remove?
			this.maxIteration = 10000;
		}

		@Override
		protected UnconstrainedMinimization createSearch() {
			ConfigTrustRegion config = new ConfigTrustRegion();
			config.scalingMinimum = 1e-4;
			config.scalingMaximum = 1e4;
//			config.regionMinimum = 0.0001;
			LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(2);
			solver = new LinearSolverSafe<>(solver);
			TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDogleg_F64(solver);

			UnconMinTrustRegionBFGS_F64 tr = new UnconMinTrustRegionBFGS_F64(alg);
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = createSolver();
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class LeastSquaresDDRM_Scaling extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();
			config.scalingMinimum = 1e-4;
			config.scalingMaximum = 1e5;
			UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr = createSolver();
			tr.configure(config);
			return tr;
		}
	}

	private static UnconLeastSqTrustRegion_F64<DMatrixRMaj> createSolver() {
		LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(2);
		solver = new LinearSolverSafe<>(solver);
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> alg = new TrustRegionUpdateDogleg_F64<>(solver);

		return new UnconLeastSqTrustRegion_F64<>(alg, new TrustRegionMath_DDRM());
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

		@Override
		protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

			LinearSolver<DMatrixSparseCSC,DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
			TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> alg = new TrustRegionUpdateDogleg_F64<>(solver);

			UnconLeastSqTrustRegion_F64<DMatrixSparseCSC> tr = new UnconLeastSqTrustRegion_F64<>(
					alg, new TrustRegionMath_DSCC());
			tr.configure(config);
			return tr;
		}
	}
}