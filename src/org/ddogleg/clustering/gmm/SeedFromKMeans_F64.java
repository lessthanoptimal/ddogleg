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

import org.ddogleg.clustering.kmeans.StandardKMeans;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.LArrayAccessor;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import java.util.List;

/**
 * Initializes the mixture models by applying K-Means first.  The mean
 * will be the center of the clusters, variance computed from its members, and weight based on
 * the total number of points assigned.
 *
 * @author Peter Abeles
 */
public class SeedFromKMeans_F64 implements InitializeGmm_F64 {

	StandardKMeans<double[]> kmeans;
	DogArray_I32 totals = new DogArray_I32();

	double[] dx = new double[1];
	// degrees-of-freedom in the points
	int dof;

	public SeedFromKMeans_F64(StandardKMeans<double[]> kmeans) {
		this.kmeans = kmeans;
	}

	@Override
	public void init(int pointDimension, long randomSeed) {
		this.dof = pointDimension;
		kmeans.initialize(randomSeed);
		if( dx.length < dof) {
			dx = new double[dof];
		}
	}

	@Override
	public void selectSeeds( LArrayAccessor<double[]> points, List<GaussianGmm_F64> seeds) {
		totals.resize(seeds.size(), 0);

		// initial cluster
		kmeans.process(points,seeds.size());

		DogArray_I32 labels = kmeans.getAssignments();
		DogArray<double[]> means = kmeans.getBestClusters();

		// compute mixture models
		for (int i = 0; i < seeds.size(); i++) {
			GaussianGmm_F64 g = seeds.get(i);
			g.setMean(means.get(i));
			CommonOps_DDRM.fill(g.covariance, 0);
		}

		// Perform the summation part of the covariance calculation and tally how many points are
		// in each cluster
		for (int i = 0; i < points.size(); i++) {
			double[] point = points.getTemp(i);
			int label = labels.get(i);

			totals.data[label]++;
			double[] m = means.get(label);

			// compute the difference between the mean and the point
			for (int j = 0; j < dof; j++) {
				dx[j] = m[j]-point[j];
			}

			// add to the covariance while taking advantage of symmetry
			DMatrixRMaj cov = seeds.get(label).covariance;

			for (int j = 0; j < dof; j++) {
				for (int k = j; k < dof; k++) {
					cov.data[k*dof +j] += dx[j]*dx[k];
				}
			}
		}

		// fill in the lower half
		for (int i = 0; i < seeds.size(); i++) {
			DMatrixRMaj cov = seeds.get(i).covariance;
			for (int j = 0; j < dof; j++) {
				for (int k = 0; k < j; k++) {
					cov.data[k*dof +j] = cov.data[j*dof +k];
				}
			}
		}

		// Perform the division part of covariance calculation and compute the weight
		for (int i = 0; i < seeds.size(); i++) {
			DMatrixRMaj cov = seeds.get(i).covariance;

			int M = totals.get(i)-1;
			if( M <= 0 ) {
				// will this is a bit distressing. The covariance is already zero so that's what
				// it should be in this pathological case
			} else {
				CommonOps_DDRM.divide(cov, M);

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
