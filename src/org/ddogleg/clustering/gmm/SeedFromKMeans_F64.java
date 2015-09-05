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

import org.ddogleg.clustering.kmeans.StandardKMeans_F64;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.List;

/**
 * Initializes the mixture models by applying K-Means first.  The mean
 * will be the center of the clusters, variance computed from its members, and weight based on
 * the total number of points assigned.
 *
 * @author Peter Abeles
 */
public class SeedFromKMeans_F64 implements InitializeGmm_F64 {

	StandardKMeans_F64 kmeans;
	GrowQueue_I32 totals = new GrowQueue_I32();

	double dx[] = new double[1];
	int N;

	public SeedFromKMeans_F64(StandardKMeans_F64 kmeans) {
		this.kmeans = kmeans;
	}

	@Override
	public void init(int pointDimension, long randomSeed) {
		this.N = pointDimension;
		kmeans.init(N,randomSeed);
		if( dx.length < N ) {
			dx = new double[N];
		}
	}

	@Override
	public void selectSeeds(List<double[]> points, List<GaussianGmm_F64> seeds) {

		totals.resize(seeds.size());
		totals.fill(0);

		// initial cluster
		kmeans.process(points,seeds.size());

		GrowQueue_I32 labels = kmeans.getPointLabels();
		FastQueue<double[]> means = kmeans.getClusterMeans();

		// compute mixture models
		for (int i = 0; i < seeds.size(); i++) {
			GaussianGmm_F64 g = seeds.get(i);
			g.setMean(means.get(i));
			CommonOps.fill(g.covariance, 0);
		}

		// Perform the summation part of the covariance calculation and tally how many points are
		// in each cluster
		for (int i = 0; i < points.size(); i++) {
			double[] p = points.get(i);
			int label = labels.get(i);

			totals.data[label]++;
			double[] m = means.get(label);

			// compute the difference between the mean and the point
			for (int j = 0; j < N; j++) {
				dx[j] = m[j]-p[j];
			}

			// add to the covariance while taking advantage of symmetry
			DenseMatrix64F cov = seeds.get(label).covariance;

			for (int j = 0; j < N; j++) {
				for (int k = j; k < N; k++) {
					cov.data[k*N+j] += dx[j]*dx[k];
				}
			}
		}

		// fill in the lower half
		for (int i = 0; i < seeds.size(); i++) {
			DenseMatrix64F cov = seeds.get(i).covariance;
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < j; k++) {
					cov.data[k*N+j] = cov.data[j*N+k];
				}
			}
		}

		// Perform the division part of covariance calculation and compute the weight
		for (int i = 0; i < seeds.size(); i++) {
			DenseMatrix64F cov = seeds.get(i).covariance;

			int M = totals.get(i)-1;
			if( M <= 0 ) {
				// will this is a bit distressing. The covariance is already zero so that's what
				// it should be in this pathological case
			} else {
				CommonOps.divide(cov, M);

				// compute the weights now
				seeds.get(i).weight = totals.get(i) / (double) points.size();
			}
		}
	}

	@Override
	public void setVerbose(boolean verbose) {
		kmeans.setVerbose(verbose);
	}
}
