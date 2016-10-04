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

import java.util.List;

/**
 * Selects the initial cluster positions for k-means
 *
 * @author Peter Abeles
 */
public interface InitializeKMeans_F64 {

	/**
	 * Initializes internal data structures.  Must be called first.
	 * @param pointDimension NUmber of degrees of freedom in each point.
	 * @param randomSeed Seed for any random number generators used internally.
	 */
	void init( int pointDimension, long randomSeed );

	/**
	 * <p>Given the set of points select reasonable seeds.</p>
	 *
	 * Duplicate Points: If there duplicate points in the input list it should not crash.  This
	 * is true even if the number of unique points is less than the number of requested seeds.  All the
	 * seeds will be filled but they do not need to be unique.
	 *
	 *
	 * @param points (Input) Set of points which is to be clustered.
	 * @param seeds (Output) List full of points which will act as the initial seed for k-means.  Results
	 *              are copied into this set.  Must be filled initially.
	 */
	void selectSeeds( List<double[]> points, List<double[]> seeds );
}
