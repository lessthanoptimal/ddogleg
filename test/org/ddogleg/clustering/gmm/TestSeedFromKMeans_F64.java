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

import org.ddogleg.clustering.kmeans.InitializeKMeans_F64;
import org.ddogleg.clustering.kmeans.StandardKMeans_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestSeedFromKMeans_F64 {

	Random rand = new Random(234);


	@Test
	public void selectSeeds() {

		// first 2 points will act as the initial seed for K-Means
		// rest will be in a known Gaussian distribution
		double sigmaX = 1;
		double sigmaY = 0.5;
		double x0 = 10,y0 = 50,x1 = -30, y1 = 20;

		List<double[]> points = new ArrayList<double[]>();
		for (int i = 0; i < 1000; i++) {
			double[] a = new double[2];
			double[] b = new double[2];

			a[0] = x0 + rand.nextGaussian()*sigmaX;
			a[1] = y0 + rand.nextGaussian()*sigmaY;

			b[0] = x1 + rand.nextGaussian()*sigmaX;
			b[1] = y1 + rand.nextGaussian()*sigmaY;

			points.add(a);
			points.add(b);
		}

		// recompute original distributions
		SeedFromKMeans_F64 alg = new SeedFromKMeans_F64(createKMeans());
		alg.init(2,234234);

		List<GaussianGmm_F64> seeds = new ArrayList<GaussianGmm_F64>();
		seeds.add( new GaussianGmm_F64(2));
		seeds.add( new GaussianGmm_F64(2));

		alg.selectSeeds(points,seeds);

		GaussianGmm_F64 a = seeds.get(0);
		GaussianGmm_F64 b = seeds.get(1);

		assertEquals(0.5,a.weight,0.91);
		assertEquals(0.5,b.weight,0.01);

		assertEquals(x0,a.mean.get(0,0),0.1);
		assertEquals(y0,a.mean.get(1,0),0.1);
		assertEquals(x1,b.mean.get(0,0),0.1);
		assertEquals(y1,b.mean.get(1,0),0.1);

		assertEquals(sigmaX*sigmaX,a.covariance.get(0,0),0.1);
		assertEquals(0,a.covariance.get(0,1),0.1);
		assertEquals(0,a.covariance.get(1,0),0.1);
		assertEquals(sigmaY*sigmaY,a.covariance.get(1,1),0.05);

		assertEquals(sigmaX*sigmaX,b.covariance.get(0,0),0.1);
		assertEquals(0,b.covariance.get(0,1),0.1);
		assertEquals(0,b.covariance.get(1,0),0.1);
		assertEquals(sigmaY*sigmaY,b.covariance.get(1,1),0.05);
	}

	private StandardKMeans_F64 createKMeans() {
		return new StandardKMeans_F64(200,1e-6,new FixedSeeds());
	}

	public static class FixedSeeds implements InitializeKMeans_F64 {

		@Override
		public void init(int pointDimension, long randomSeed) {}

		@Override
		public void selectSeeds(List<double[]> points, List<double[]> seeds) {
			int N = seeds.get(0).length;
			for (int i = 0; i < 2; i++) {
				System.arraycopy(points.get(i), 0, seeds.get(i), 0, N);
			}
		}
	}
}