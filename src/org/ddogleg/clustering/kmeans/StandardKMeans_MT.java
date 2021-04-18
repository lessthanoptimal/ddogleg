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

package org.ddogleg.clustering.kmeans;

import lombok.Getter;
import lombok.Setter;
import org.ddogleg.DDoglegConcurrency;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.ComputeMeanClusters;
import org.ddogleg.clustering.PointDistance;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.DogLambdas;
import org.ddogleg.struct.LArrayAccessor;
import pabeles.concurrency.GrowArray;

/**
 * <p>Concurrent implementation of {@link StandardKMeans}</p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class StandardKMeans_MT<P> extends StandardKMeans<P> {
	GrowArray<MatchData> workspace;

	/**
	 * Minimum list size for it to use concurrent code. If a list is small it will run slower than the single
	 * thread version. By default this is zero since the optimal value is use case specific.
	 */
	@Getter @Setter int minimumForConcurrent = 0;

	/**
	 * Configures k-means parameters
	 *
	 * @param seedSelector Used to select initial seeds for the clusters
	 */
	public StandardKMeans_MT( ComputeMeanClusters<P> updateMeans,
							  InitializeKMeans<P> seedSelector,
							  PointDistance<P> distancer,
							  DogLambdas.NewInstance<P> factory ) {
		super(updateMeans, seedSelector, distancer, factory);

		workspace = new GrowArray<>(MatchData::new, MatchData::reset);
	}

	/**
	 * Finds the cluster which is the closest to each point.  The point is the added to the sum for the cluster
	 * and its member count incremented
	 */
	@Override protected void matchPointsToClusters( LArrayAccessor<P> points, DogArray<P> clusters ) {
		// see if it should run the single thread version instead
		if (points.size() < minimumForConcurrent) {
			super.matchPointsToClusters(points, clusters);
			return;
		}
		assignments.resize(points.size());

		DDoglegConcurrency.loopBlocks(0, points.size(), workspace, ( work, idx0, idx1 ) -> {
			final DogArray_I32 memberCount = work.memberCount;
			// reset the member counts to zero for each cluster
			memberCount.resetResize(clusters.size, 0);
			final P point = work.point;

			// Assign each point a single cluster
			for (int i = idx0; i < idx1; i++) {
				points.getCopy(i, point);

				// find the cluster which is closest to the point
				int assignment = findBestMatch(point, clusters, work);
				assignments.set(i, assignment); // threads won't modify the same elements
				// increment the number of points assigned to this cluster
				memberCount.data[assignment]++;
			}
		});

		// Stitch results back together from the threads
		memberCount.resetResize(clusters.size, 0);
		sumDistance = 0;

		for (int i = 0; i < workspace.size(); i++) {
			MatchData md = workspace.get(i);
			sumDistance += md.sumDistance;
			for (int clusterIdx = 0; clusterIdx < clusters.size; clusterIdx++) {
				memberCount.data[clusterIdx] += md.memberCount.data[clusterIdx];
			}
		}
	}

	/**
	 * Searches for this cluster which is the closest to p
	 */
	protected int findBestMatch( P p, DogArray<P> clusters, MatchData match ) {
		int bestCluster = -1;
		double bestDistance = Double.MAX_VALUE;

		for (int clusterIdx = 0; clusterIdx < clusters.size; clusterIdx++) {
			double d = distancer.distance(p, clusters.get(clusterIdx));
			if (d < bestDistance) {
				bestDistance = d;
				bestCluster = clusterIdx;
			}
		}
		match.sumDistance += bestDistance;
		return bestCluster;
	}

	@Override public ComputeClusters<P> newInstanceThread() {
		var spawn = new StandardKMeans_MT<>(
				updateMeans.newInstanceThread(),
				seedSelector.newInstanceThread(),
				distancer.newInstanceThread(), factory);

		spawn.convergeTol = convergeTol;
		spawn.maxIterations = maxIterations;
		spawn.reseedAfterIterations = reseedAfterIterations;
		spawn.verbose = verbose;

		return spawn;
	}

	private class MatchData {
		public double sumDistance;
		public P point;
		DogArray_I32 memberCount = new DogArray_I32();

		public MatchData() {
			point = factory.newInstance();
		}

		public void reset() {
			sumDistance = 0;
			memberCount.reset();
		}
	}
}
