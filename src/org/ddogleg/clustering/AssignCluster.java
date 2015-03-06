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

package org.ddogleg.clustering;

/**
 * Used to assign a point to set of clusters.  Clusters are given labels from 0 to N-1, where N is the number of
 * clusters.
 *
 * @author Peter Abeles
 */
public interface AssignCluster<D> {

	/**
	 * Assigns the point to one of the clusters.
	 * @param point Point which is to be assigned
	 * @return Index of the cluster from 0 to N-1
	 */
	public int assign( D point );

	/**
	 * Total number of clusters.
	 * @return  The total number of clusters.
	 */
	public int getNumberOfClusters();

	/**
	 * Creates an exact copy of this class.
	 * @return Copy of class
	 */
	public AssignCluster<D> copy();
}
