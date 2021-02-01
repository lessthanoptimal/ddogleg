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

/**
 * Configuration for K-Means clustering
 *
 * @author Peter Abeles
 */
public class ConfigKMeans {
	/** Which initialization algorithm */
	public KMeansInitializers initializer = KMeansInitializers.PLUS_PLUS;

	/** Maximum number of iterations, across all seeds combined */
	public int maxIterations = 1000;

	/** If it doesn't converge within this many iterations a new seed is created */
	public int maxConverge = 50;

	/** Change in distance criteria when testing for convergence */
	public double convergeTol = 1e-8;

	public void setTo( ConfigKMeans src ) {
		this.initializer = src.initializer;
		this.maxIterations = src.maxIterations;
		this.maxConverge = src.maxConverge;
		this.convergeTol = src.convergeTol;
	}

	public void checkValidity() {
		if (maxIterations < 0)
			throw new IllegalArgumentException("maxIterations can't be negative");
		if (maxConverge < 0)
			throw new IllegalArgumentException("maxConverge can't be negative");
		if (convergeTol < 0.0)
			throw new IllegalArgumentException("convergeTol can't be negative");
	}
}
