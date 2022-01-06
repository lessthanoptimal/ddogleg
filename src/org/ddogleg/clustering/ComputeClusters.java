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

package org.ddogleg.clustering;

import org.ddogleg.struct.LArrayAccessor;

/**
 * Given a set of points in N-dimensional space, compute a set of unique assignment for each point to a cluster.
 * The clusters will be designed to minimize some distance function between each cluster and the points assigned
 * to it.
 *
 * @author Peter Abeles
 */
public interface ComputeClusters<P> {

	/**
	 * Must be called first to initializes internal data structures.  Only needs to be called once.
	 *
	 * @param randomSeed Seed for any random number generators used internally.
	 */
	void initialize(long randomSeed);

	/**
	 * Computes a set of clusters which segment the points into numCluster sets. The number
	 * of clusters and points must be 1 or more. If this is not true then the behavior is undefined.
	 *
	 * @param points Set of points which are to be clustered. Not modified.
	 * @param numCluster Number of clusters it will use to split the points.
	 */
	void process( LArrayAccessor<P> points, int numCluster);

	/**
	 * <p>Returns a class which is used to assign a point to a cluster.  Only invoked after
	 * {@link #process} has been called.</p>
	 *
	 * <p>
	 * WARNING: The returned data structure is recycled each time compute clusters is called.  Create a copy
	 * if you wish to avoid having it modified.
	 * </p>
	 *
	 * @return Instance of {@link org.ddogleg.clustering.AssignCluster}.
	 */
	AssignCluster<P> getAssignment();

	/**
	 * <p>
	 * Returns the sum of all the distances between each point in the set.  Can be used to evaluate
	 * the quality of fit for all the clusters.  Can only be used to compare when the same number of clusters
	 * is uesd.
	 * </p>
	 *
	 * NOTE: The specific distance measure is not specified and is application specific.
	 *
	 * @return sum of distance between each point and their respective clusters.
	 */
	double getDistanceMeasure();

	/**
	 * If set to true then information about status will be printed to standard out.  By default verbose is off
	 * @param verbose true for versbose mode.  False for quite mode.
	 */
	void setVerbose(boolean verbose);

	/**
	 * Creates a new instance which has the same configuration and can be run in parallel. Some components
	 * can be shared as long as they are read only and thread safe.
	 */
	ComputeClusters<P> newInstanceThread();
}
