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

import org.ddogleg.clustering.FactoryClustering;
import org.ddogleg.clustering.kmeans.StandardKMeans;
import org.ddogleg.clustering.kmeans.TestStandardKMeans;
import org.ddogleg.clustering.misc.ListAccessor;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSeedFromKMeans_F64 {

	Random rand = new Random(234);

	@Test
	public void selectSeeds() {
		int dof = 2;
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
		SeedFromKMeans_F64 alg = new SeedFromKMeans_F64(createKMeans(dof));
		alg.init(2,234234);

		List<GaussianGmm_F64> seeds = new ArrayList<GaussianGmm_F64>();
		seeds.add( new GaussianGmm_F64(2));
		seeds.add( new GaussianGmm_F64(2));
		ListAccessor<double[]> accessor = new ListAccessor<>(points,
				(src, dst) -> System.arraycopy(src, 0, dst, 0, dof), double[].class);

		alg.selectSeeds(accessor,seeds);

		GaussianGmm_F64 a = seeds.get(0);
		GaussianGmm_F64 b = seeds.get(1);

		assertEquals(0.5,a.weight,0.91);
		assertEquals(0.5,b.weight,0.01);

		GaussianGmm_F64 expectedA = computeGaussian(0,points);
		GaussianGmm_F64 expectedB = computeGaussian(1,points);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedA.mean,a.mean,1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedB.mean,b.mean,1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedA.covariance,a.covariance,1e-8));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expectedB.covariance,b.covariance,1e-8));
	}

	private GaussianGmm_F64 computeGaussian( int offset , List<double[]> points ) {

		GaussianGmm_F64 out = new GaussianGmm_F64(2);

		// compute the mean
		for (int i = offset; i < points.size(); i += 2) {
			double[] p = points.get(i);

			out.mean.data[0] += p[0];
			out.mean.data[1] += p[1];
		}
		CommonOps_DDRM.divide(out.mean,points.size()/2);

		// compute the covariance
		Equation eq = new Equation();
		eq.alias(out.mean, "mu", out.covariance, "Q");
		for (int i = offset; i < points.size(); i += 2) {
			double[] p = points.get(i);
			DMatrixRMaj x = DMatrixRMaj.wrap(2,1,p);
			eq.alias(x,"x");
			eq.process("Q = Q + (x-mu)*(x-mu)'");
		}
		CommonOps_DDRM.divide(out.covariance,points.size()/2-1);
		return out;
	}

	private StandardKMeans<double[]> createKMeans(int dof) {
		var alg = FactoryClustering.kMeans(null,dof, double[].class);
		alg.maxIterations = 200;
		alg.reseedAfterIterations = 200;
		alg.convergeTol = 1e-6;
		alg.seedSelector = new TestStandardKMeans.FixedSeeds();
		return alg;
	}

}