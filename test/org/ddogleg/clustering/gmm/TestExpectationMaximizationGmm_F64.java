/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.clustering.gmm;

import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.ConfigKMeans;
import org.ddogleg.clustering.FactoryClustering;
import org.ddogleg.clustering.GenericClusterChecks_F64;
import org.ddogleg.clustering.gmm.ExpectationMaximizationGmm_F64.PointInfo;
import org.ddogleg.clustering.kmeans.StandardKMeans;
import org.ddogleg.clustering.kmeans.TestStandardKMeans;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.ddogleg.clustering.gmm.TestGaussianLikelihoodManager.computeLikelihood;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestExpectationMaximizationGmm_F64 extends GenericClusterChecks_F64 {

	Random rand = new Random(234);
	
	/**
	 * Computes the expectation for several points and a variable number of Gaussians.  Sees if points which
	 * are at the mean of the Gaussians have a peak at the expected location
	 */
	@Test void expectation() {
		int DOF = 3;

		var alg = new ExpectationMaximizationGmm_F64(100,1e-8,DOF,createSeeds(DOF));
		alg.initialize(34535);

		// randomly create a few points
		for (int i = 0; i < 20; i++) {
			PointInfo p = alg.info.grow();
			p.point = new double[DOF];
			for (int j = 0; j < DOF; j++) {
				p.point[j] = rand.nextGaussian()*5;
			}
		}

		// try different number of gaussians in the mixture to be more exhaustive in the testing
		for (int i = 0; i < 3; i++) {
			// adjust for the number of clusters
			for (int j = 0; j < alg.info.size; j++) {
				PointInfo p = alg.info.get(j);
				p.weights.resize(i+1);
			}

			// set the mean of the gaussians to be the first N points
			GaussianGmm_F64 a = alg.mixture.grow();
			a.setMean(alg.info.get(i).point);
			a.weight = 2;
			CommonOps_DDRM.setIdentity(a.covariance);

			// compute expectation.  The peak for the first i points is known
			alg.likelihoodManager.precomputeAll();
			alg.expectation();

			for (int j = 0; j <= i; j++) {
				PointInfo p = alg.info.get(j);

				double expectedMax = p.weights.get(j);

				double total = 0;
				for (int k = 0; k <= i; k++) {
					double w = p.weights.get(k);
					total += w;
					if( k != j ) {
						assertTrue(w<expectedMax);
					}
				}
				// should sum up to one
				assertEquals(1,total,1e-8);
			}
		}
	}

	@Test void maximization() {
		int DOF = 2;

		ExpectationMaximizationGmm_F64 alg = new ExpectationMaximizationGmm_F64(100,1e-8,DOF,createSeeds(DOF));
		alg.initialize(34535);

		GaussianGmm_F64 a = alg.mixture.grow();
		a.setMean(new double[]{1,0.5});
		CommonOps_DDRM.diag(a.covariance, 2, 0.75, 1);
		a.weight = 0.25;

		GaussianGmm_F64 b = alg.mixture.grow();
		b.setMean(new double[]{4,8});
		CommonOps_DDRM.diag(b.covariance, 2, 0.5, 0.75);
		b.weight = 0.75;


		// uniform generate a bunch of points
		createPointsAround(1,0.5,alg);
		createPointsAround(2,3  ,alg);


		// discard the mixture parameters
		for (int i = 0; i < alg.mixture.size; i++) {
			CommonOps_DDRM.fill(alg.mixture.get(i).mean,0);
			CommonOps_DDRM.fill(alg.mixture.get(i).covariance,0);
			alg.mixture.get(i).weight = 0;
		}

		// compute the density and compare against ground truth
		alg.maximization();

		GaussianGmm_F64 expectedA = computeGaussian(0,alg.info.toList());
		GaussianGmm_F64 expectedB = computeGaussian(1,alg.info.toList());

		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedA.mean, a.mean, 1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedB.mean, b.mean, 1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedA.covariance,a.covariance,1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedB.covariance,b.covariance,1e-8));

	}

	private void createPointsAround( double cx , double cy , ExpectationMaximizationGmm_F64 alg ) {
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				double x = cx + i*0.1 - 2.5;
				double y = cy + i*0.1 - 2.5;

				PointInfo p = alg.info.grow();
				p.point = new double[]{x,y};
				p.weights.resize(2);

				// assign their weight based on their likelihood
				double total = 0;
				for (int k = 0; k < alg.mixture.size; k++) {
					total += p.weights.data[k] = computeLikelihood(alg.mixture.get(k), p.point);
				}
				for (int k = 0; k < alg.mixture.size; k++) {
					p.weights.data[k] /= total;
				}
			}
		}
	}

	private GaussianGmm_F64 computeGaussian( int which , List<PointInfo> points ) {
		int N = points.get(0).point.length;
		GaussianGmm_F64 out = new GaussianGmm_F64(N);

		// compute the mean
		double total = 0;
		for (int i = 0; i < points.size(); i++) {
			PointInfo p = points.get(i);
			double w = p.weights.data[which];
			total += w;

			for (int j = 0; j < N; j++) {
				out.mean.data[j] += w*p.point[j];
			}
		}
		CommonOps_DDRM.divide(out.mean,total);

		// compute the covariance
		Equation eq = new Equation();
		eq.alias(out.mean, "mu", out.covariance, "Q");
		for (int i = 0; i < points.size(); i++) {
			PointInfo p = points.get(i);
			double w = p.weights.data[which];

			DMatrixRMaj x = DMatrixRMaj.wrap(N,1,p.point);
			eq.alias(x,"x",w,"w");
			eq.process("Q = Q + w*(x-mu)*(x-mu)'");
		}
		CommonOps_DDRM.divide(out.covariance,total);
		return out;
	}

	@Override
	public ComputeClusters<double[]> createClustersAlg( boolean hint, int dof ) {
		if( hint ) {
			return new ExpectationMaximizationGmm_F64(1000, 1e-8, dof, createSeeds(dof));
		} else {
			return FactoryClustering.gaussianMixtureModelEM_F64(1000,1000,1e-8,dof);
		}
	}

	private SeedFromKMeans_F64 createSeeds(int DOF) {
		ConfigKMeans config = new ConfigKMeans();
		config.convergeTol = 1e-8;
		config.maxIterations = 1000;
		config.reseedAfterIterations = 1000;

		StandardKMeans<double[]> kmeans = FactoryClustering.kMeans(config,DOF, double[].class);
		kmeans.seedSelector = new TestStandardKMeans.FixedSeeds();
		return new SeedFromKMeans_F64(kmeans);
	}
}