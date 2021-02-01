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

import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.FastAccess;
import org.ddogleg.struct.LArrayAccessor;

/**
 * Abstract way to update the cluster mean values from a set of points which have been assigned to a single
 * cluster.
 *
 * @author Peter Abeles
 */
public interface ComputeMeanClusters<P> {

	/**
	 * Updates cluster means
	 *
	 * @param points (Input) access to point values
	 * @param assignments (Input) which cluster each point has been assigned to
	 * @param clusters (Output) Cluster means which are to be updated.
	 */
	void process( LArrayAccessor<P> points, DogArray_I32 assignments, FastAccess<P> clusters);

	/**
	 * Creates a new instance which has the same configuration and can be run in parallel. Some components
	 * can be shared as long as they are read only and thread safe.
	 */
	ComputeMeanClusters<P> newInstanceThread();
}
