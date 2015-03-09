/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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
import org.ddogleg.clustering.GenericClusterChecks_F64;
import org.ddogleg.clustering.gmm.ExpectationMaximizationGmm_F64.PointInfo;
import org.ddogleg.clustering.kmeans.StandardKMeans_F64;
import org.ddogleg.clustering.kmeans.TestStandardKMeans_F64;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import java.util.Random;

import static org.ddogleg.clustering.gmm.TestGaussianLikelihoodManager.computeLikelihood;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestExpectationMaximizationGmm_F64 extends GenericClusterChecks_F64 {

	Random rand = new Random(234);

	StandardKMeans_F64 kmeans = new StandardKMeans_F64(1000,1e-8,new TestStandardKMeans_F64.FixedSeeds());;
	SeedFromKMeans_F64 seeds = new SeedFromKMeans_F64(kmeans);

	/**
	 * Computes the expectation for several points and a variable number of Gaussians.  Sees if points which
	 * are at the mean of the Gaussians have a peak at the expected location
	 */
	@Test
	public void expectation() {

		int DOF = 3;

		ExpectationMaximizationGmm_F64 alg = new ExpectationMaximizationGmm_F64(100,1e-8,seeds);

		alg.init(DOF,34535);

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
			CommonOps.setIdentity(a.covariance);

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

	@Test
	public void maximization() {
		int DOF = 2;

		ExpectationMaximizationGmm_F64 alg = new ExpectationMaximizationGmm_F64(100,1e-8,seeds);
		alg.init(DOF,34535);

		GaussianGmm_F64 a = alg.mixture.grow();
		a.setMean(new double[]{1,0.5});
		CommonOps.diag(a.covariance, 2, 0.75, 1);
		a.weight = 0.25;

		GaussianGmm_F64 b = alg.mixture.grow();
		b.setMean(new double[]{4,8});
		CommonOps.diag(b.covariance, 2, 0.5, 0.75);
		b.weight = 0.75;


		// uniform generate a bunch of points
		createPointsAround(1,0.5,alg);
		createPointsAround(2,3  ,alg);


		// discard the mixture parameters
		for (int i = 0; i < alg.mixture.size; i++) {
			CommonOps.fill(alg.mixture.get(i).mean,0);
			CommonOps.fill(alg.mixture.get(i).covariance,0);
			alg.mixture.get(i).weight = 0;
		}

		// compute the density and compare against ground truth
		alg.maximization();

		assertEquals(1,a.mean.get(0,0),0.1);
		assertEquals(0.5,a.mean.get(1,0),0.1);

		// TODO check mean and covariance

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

	@Override
	public ComputeClusters<double[]> createClustersAlg() {

		return new ExpectationMaximizationGmm_F64(1000,1e-8,seeds);
	}
}