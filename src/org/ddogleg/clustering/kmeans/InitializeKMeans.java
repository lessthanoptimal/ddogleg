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

import org.ddogleg.clustering.PointDistance;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.LArrayAccessor;

/**
 * Selects the initial cluster positions for k-means
 *
 * @author Peter Abeles
 */
public interface InitializeKMeans<P> {

	/**
	 * Initializes internal data structures.  Must be called first.
	 * @param distance Distance function between two points
	 * @param randomSeed Seed for any random number generators used internally.
	 */
	void initialize(PointDistance<P> distance, long randomSeed );

	/**
	 * <p>Given the set of points select reasonable seeds.</p>
	 *
	 * Duplicate Points: If there duplicate points in the input list it should not crash.  This
	 * is true even if the number of unique points is less than the number of requested seeds.  All the
	 * seeds will be filled but they do not need to be unique.
	 *
	 *
	 * @param points (Input) Set of points which is to be clustered.
	 * @param totalSeeds (Input) Number of seeds it will select
	 * @param selectedSeeds (Output) Storage for selected seeds. They will be copied into it.
	 */
	void selectSeeds( LArrayAccessor<P> points, int totalSeeds, DogArray<P> selectedSeeds);

	/**
	 * Creates a new instance which has the same configuration and can be run in parallel. Some components
	 * can be shared as long as they are read only and thread safe.
	 */
	InitializeKMeans<P> newInstanceThread();
}
