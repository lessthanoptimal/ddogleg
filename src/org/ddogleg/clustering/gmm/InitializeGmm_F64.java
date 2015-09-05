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

import java.util.List;

/**
 * Selects initial Gaussians for EM algorithm.
 *
 * @author Peter Abeles
 */
public interface InitializeGmm_F64 {

	/**
	 * Initializes internal data structures.  Must be called first.
	 * @param pointDimension Number of degrees of freedom in each point.
	 * @param randomSeed Seed for any random number generators used internally.
	 */
	public void init( int pointDimension, long randomSeed );

	/**
	 *
	 * @param points (input) Set of points which is to be clustered.
	 * @param seeds (output) List containing storage for the initial Gaussians.
	 */
	public void selectSeeds( List<double[]> points, List<GaussianGmm_F64> seeds );

	/**
	 * Turn on verbose output to standard out
	 */
	void setVerbose( boolean verbose );
}
