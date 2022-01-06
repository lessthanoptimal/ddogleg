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
import org.ddogleg.clustering.AssignCluster;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.ComputeMeanClusters;
import org.ddogleg.clustering.PointDistance;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.DogLambdas;
import org.ddogleg.struct.LArrayAccessor;

/**
 * <p>
 * Standard implementation of k-means [1], summary is provided below:
 * </p>
 * <ol>
 * <li> The initial seeds for each cluster is selected by the provided
 * {@link InitializeKMeans}.
 * <li> Each point is assigned to a cluster which minimizes the euclidean distance squared.
 * <li> New cluster centers are computed from the average of all points assigned to it.
 * </ol>
 * <p>
 * This will find a locally optimal solution which minimizes the sum of the distance-squared of each point
 * to the cluster they are assigned to.
 * </p>
 *
 * <p>
 * Converged if, {@code(D[i] - D[i-1])/D[i] <= tol}, where D is the sum of point from cluster distance at iteration 'i',
 * and tol is the convergence tolerance threshold.
 * </p>
 *
 * <p>
 * [1] Lloyd, S. P. (1957). "Least square quantization in PCM". Bell Telephone Laboratories Paper.
 * Published in journal much later: Lloyd., S. P. (1982)
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class StandardKMeans<P> implements ComputeClusters<P> {

	// flag for verbose mode
	boolean verbose = false;

	/** maximum number of iterations */
	public @Getter @Setter int maxIterations = 100;
	/** max iterations before it will reseed */
	public @Getter @Setter int reseedAfterIterations = 20;
	/** max number of times it will re-seed */
	public @Getter @Setter int maxReSeed = 5;

	/** It is considered to be converged when the change in sum score is <= than this amount. */
	public @Getter @Setter double convergeTol = 1e-8;

	// Used to update the mean after assignments have been made
	public ComputeMeanClusters<P> updateMeans;
	// selects the initial locations of each seed
	public InitializeKMeans<P> seedSelector;
	// Storage for the seeds
	DogArray<P> workClusters;

	// Computes the distance between two points
	PointDistance<P> distancer;

	// Creates new instances of points
	DogLambdas.NewInstance<P> factory;

	/** labels for all the points */
	final @Getter DogArray_I32 assignments = new DogArray_I32();

	/** Number of points assigned to each cluster */
	final @Getter DogArray_I32 memberCount = new DogArray_I32();

	// sum of distances for all the points
	double sumDistance;

	// the best cluster centers
	final @Getter DogArray<P> bestClusters;
	/** Distance from cluster score for best cluster */
	@Getter double bestClusterScore;
	final DogArray_I32 bestMemberCount = new DogArray_I32();


	/**
	 * Configures k-means parameters
	 *
	 * @param seedSelector Used to select initial seeds for the clusters
	 */
	public StandardKMeans( ComputeMeanClusters<P> updateMeans,
						   InitializeKMeans<P> seedSelector,
						   PointDistance<P> distancer,
						   DogLambdas.NewInstance<P> factory) {
		this.updateMeans = updateMeans;
		this.seedSelector = seedSelector;
		this.distancer = distancer;
		this.factory = factory;

		workClusters = new DogArray<>(factory::newInstance);
		bestClusters = new DogArray<>(factory::newInstance);
	}

	@Override
	public void initialize( long randomSeed ) {
		if (convergeTol >= 1.0 || convergeTol < 0.0)
			throw new IllegalArgumentException("convergeTol must be 0 <= tol < 1.0, not "+convergeTol);
		seedSelector.initialize(distancer, randomSeed);
	}

	@Override
	public void process( LArrayAccessor<P> points, int numCluster ) {
		if (numCluster <= 0)
			throw new IllegalArgumentException("There must be at least one cluster");
		if (points.size() == 0)
			throw new IllegalArgumentException("There must be at least one point");

		if (verbose)
			System.out.println("ENTER standard kmeans process");
		// declare data
		workClusters.resize(numCluster);
		bestClusters.resize(numCluster);

		// select the initial seeds
		seedSelector.selectSeeds(points, numCluster, workClusters);

		// un standard k-means
		bestClusterScore = Double.MAX_VALUE;
		sumDistance = Double.MAX_VALUE;
		double previousSum = Double.MAX_VALUE;
		int lastConverge = 0;

		// Abort if it re-seeded many times.
		int numReSeeded = 0;
		int maxReSeed = this.maxReSeed <= 0 ? Integer.MAX_VALUE : this.maxReSeed;
		for (int iteration = 0; iteration < maxIterations && numReSeeded < maxReSeed; iteration++) {
			// match points to the means
			matchPointsToClusters(points, workClusters);

			// see if its taking too long
			boolean reseed = iteration - lastConverge >= reseedAfterIterations;

			// check for convergence
			double fractionalChange = 1.0 - sumDistance/previousSum;
			reseed |= fractionalChange >= 0 && fractionalChange <= convergeTol;

			if (reseed) {
				// see if a better solution has been found and save it
				if (sumDistance < bestClusterScore) {
					saveBestCluster(points);
				}
				if (verbose)
					System.out.println(iteration + "  Reseeding, distance = " + sumDistance);
				// try from a new random seed
				seedSelector.selectSeeds(points, numCluster, workClusters);
				previousSum = Double.MAX_VALUE;
				lastConverge = iteration;
				numReSeeded++;
			} else {
				if (verbose && previousSum == Double.MAX_VALUE) {
					System.out.println(iteration + "  first iteration: " + sumDistance);
				}
				previousSum = sumDistance;

				// Given the current assignments, update the cluster centers
				updateMeans.process(points, assignments, workClusters);
			}
		}

		// see if a better solution has been found and save it
		if (sumDistance < bestClusterScore) {
			saveBestCluster(points);
		}

		// Make sure the points are assigned to the best cluster
		matchPointsToClusters(points, bestClusters);

		// copy the best into the output member count
		memberCount.setTo(bestMemberCount);

		if (verbose)
			System.out.println("EXIT standard kmeans process");
	}

	/**
	 * Copies the current cluster into the best cluster
	 */
	private void saveBestCluster( LArrayAccessor<P> points ) {
		bestClusterScore = sumDistance;

		// copy current cluster into best cluster
		bestClusters.reserve(workClusters.size);
		bestClusters.reset();
		for (int i = 0; i < workClusters.size; i++) {
			points.copy(workClusters.get(i), bestClusters.grow());
		}
		bestMemberCount.setTo(memberCount);

		if (verbose)
			System.out.println(" better clusters score: " + bestClusterScore);
	}

	@Override
	public AssignCluster<P> getAssignment() {
		return new AssignKMeans<>(bestClusters, distancer);
	}

	/**
	 * Finds the cluster which is the closest to each point.  The point is the added to the sum for the cluster
	 * and its member count incremented
	 */
	protected void matchPointsToClusters( LArrayAccessor<P> points, DogArray<P> clusters ) {
		// reset the member counts to zero for each cluster
		memberCount.resetResize(clusters.size, 0);

		// updated inside the call to findBestMatch
		sumDistance = 0;

		// NOTE: This is a good candidate for optimizing
		// Maybe reverse loop order by having outer loop go through clusters
		// instead of doing points.getTemp() compute the distance directly on the internal array

		// Assign each point a single cluster
		assignments.resize(points.size());
		for (int i = 0; i < points.size(); i++) {
			P point = points.getTemp(i);

			// find the cluster which is closest to the point
			int assignment = findBestMatch(point, clusters);
			assignments.set(i, assignment);
			// increment the number of points assigned to this cluster
			memberCount.data[assignment]++;
		}
	}

	/**
	 * Searches for this cluster which is the closest to p
	 */
	protected int findBestMatch( final P p, final DogArray<P> clusters ) {
		int bestCluster = -1;
		double bestDistance = Double.MAX_VALUE;

		for (int clusterIdx = 0; clusterIdx < clusters.size; clusterIdx++) {
			double d = distancer.distance(p, clusters.get(clusterIdx));
			if (d < bestDistance) {
				bestDistance = d;
				bestCluster = clusterIdx;
			}
		}
		sumDistance += bestDistance;
		return bestCluster;
	}

	/**
	 * Computes the potential function.  The sum of distance for each point from their cluster centers.\
	 */
	@Override
	public double getDistanceMeasure() {
		return sumDistance;
	}

	@Override
	public void setVerbose( boolean verbose ) {
		this.verbose = verbose;
	}

	@Override public ComputeClusters<P> newInstanceThread() {
		var spawn = new StandardKMeans<>(
				updateMeans.newInstanceThread(),
				seedSelector.newInstanceThread(),
				distancer.newInstanceThread(),factory);

		spawn.convergeTol = convergeTol;
		spawn.maxIterations = maxIterations;
		spawn.reseedAfterIterations = reseedAfterIterations;
		spawn.verbose = verbose;

		return spawn;
	}
}
