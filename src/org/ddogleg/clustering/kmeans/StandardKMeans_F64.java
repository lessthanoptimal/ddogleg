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

package org.ddogleg.clustering.kmeans;

import org.ddogleg.clustering.AssignCluster;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Standard implementation of k-means [1], summary is provided below:
 * <ol>
 * <li> The initial seeds for each cluster is selected by the provided
 * {@link org.ddogleg.clustering.kmeans.InitialSeedsKMeans_F64}.
 * <li> Each point is assigned to a cluster which minimizes the euclidean distance squared.
 * <li> New cluster centers are computed from the average of all points assigned to it.
 * </ol>
 * This will find a locally optimal solution which minimizes the sum of the distance-squared of each point
 * to the cluster they are assigned to.
 * </p>
 * <p>
 * [1] Lloyd, S. P. (1957). "Least square quantization in PCM". Bell Telephone Laboratories Paper.
 * Published in journal much later: Lloyd., S. P. (1982)
 * </p>
 *
 * @author Peter Abeles
 */
public class StandardKMeans_F64 implements ComputeClusters<double[]> {

	// number of elements in each point
	int N;

	// maximum number of iterations
	int maxIterations;

	// If all point centers move less than this distance squared then it is
	// considered to have converged
	double minimumChangeSq;

	// selects the initial locations of each seed
	InitialSeedsKMeans_F64 seedSelector;
	// Storage for the seeds
	FastQueue<double[]> clusters;

	// work space for computing the new cluster centers.  The sum for points in a cluster is computed on the fly
	// instead of labeling each point and computing it later.  Should save memory and maybe slightly faster.
	FastQueue<double[]> workClusters;
	GrowQueue_I32 memberCount = new GrowQueue_I32();

	AssignKMeans_F64 assign;

	@Override
	public void init(final int pointDimension, long randomSeed) {
		seedSelector.init(pointDimension,randomSeed);
		this.N = pointDimension;

		clusters = createQueue(pointDimension);
		workClusters = createQueue(pointDimension);
		memberCount.resize(pointDimension);

		assign = new AssignKMeans_F64(clusters.toList());
	}

	private FastQueue<double[]> createQueue( final int pointDimension ) {
		return new FastQueue<double[]>(double[].class,true) {
			@Override
			protected double[] createInstance() {
				return new double[pointDimension];
			}
		};
	}

	@Override
	public void process(List<double[]> points, int numCluster) {
		// declare data
		clusters.resize(numCluster);

		// select the initial seeds
		seedSelector.selectSeeds(points, clusters.toList());

		// run standard k-means
		for (int iteration = 0; iteration < maxIterations; iteration++) {
			// zero the work seeds.  These will be used
			for (int i = 0; i < workClusters.size(); i++) {
				Arrays.fill(workClusters.data[i],0);
			}
			memberCount.fill(0);

			matchPointsToClusters(points);

			if( !updateClusterCenters() )
				break; // exit if the centers haven't moved
		}

		// TODO create cluster assignment

		// TODO code up potential function
	}

	/**
	 * Finds the cluster which is the closest to each point.  The point is the added to the sum for the cluster
	 * and its member count incremented
	 */
	protected void matchPointsToClusters(List<double[]> points) {
		for (int i = 0; i < points.size(); i++) {
			double[]p = points.get(i);

			// find the cluster which is closest to the point
			int bestCluster = -1;
			double bestDistance = Double.MAX_VALUE;

			for (int j = 0; j < clusters.size; j++) {
				double d = distanceSq(p,clusters.get(i));
				if( d < bestDistance ) {
					bestDistance = d;
					bestCluster = j;
				}
			}

			// sum up all the points which are members of this cluster
			double[] c = workClusters.get(bestCluster);
			for (int j = 0; j < c.length; j++) {
				c[j] += p[j];
			}
			memberCount.data[bestCluster]++;
		}
	}

	/**
	 * Sets the location of each cluster to the average location of all its members.
	 *
	 * @return true if the cluster centers have moved significantly.
	 */
	protected boolean updateClusterCenters() {
		// compute the new centers of each cluster
		for (int i = 0; i < clusters.size; i++) {
			double mc = memberCount.get(i);
			double[] c = workClusters.get(i);

			for (int j = 0; j < c.length; j++) {
				c[j] /= mc;
			}
		}

		// see if the centers have changed significantly and update their values
		boolean changed = false;
		for (int i = 0; i < clusters.size; i++) {
			double[] c = clusters.get(i);
			double[] w = workClusters.get(i);
			if( !changed ) {
				if( distanceSq(c,w) > minimumChangeSq ) {
					changed = true;
				}
			}
			System.arraycopy(w,0,c,0,c.length);
		}

		return changed;
	}

	/**
	 * Returns the euclidean distance squared between the two poits
	 */
	protected static double distanceSq(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			double d = a[i]-b[i];
			sum += d*d;
		}
		return sum;
	}

	@Override
	public AssignCluster<double[]> getAssignment() {
		return assign;
	}

	/**
	 * Computes the potential function.  The sum of distance for each point from their cluster centers.\
	 */
	@Override
	public double computeDistance() {
		return 0;  // TODO
	}
}
