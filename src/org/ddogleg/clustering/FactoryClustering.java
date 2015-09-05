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

import org.ddogleg.clustering.gmm.ExpectationMaximizationGmm_F64;
import org.ddogleg.clustering.gmm.SeedFromKMeans_F64;
import org.ddogleg.clustering.kmeans.InitializeKMeans_F64;
import org.ddogleg.clustering.kmeans.InitializePlusPlus;
import org.ddogleg.clustering.kmeans.InitializeStandard_F64;
import org.ddogleg.clustering.kmeans.StandardKMeans_F64;

/**
 * Factory for creating clustering algorithms.
 *
 * @author Peter Abeles
 */
public class FactoryClustering {

	/**
	 * <p>
	 * High level interface for creating GMM cluster.  If more flexibility is needed (e.g. custom seeds)
	 * then create and instance of {@link ExpectationMaximizationGmm_F64} directly
	 * </p>
	 *
	 * <p>WARNING: DEVELOPMENTAL AND IS LIKELY TO FAIL HORRIBLY</p>
	 *
	 * @param maxIterations Maximum number of iterations it will perform.
	 * @param maxConverge Maximum iterations allowed before convergence.  Re-seeded if it doesn't converge.
	 * @param convergeTol Distance based convergence tolerance.  Try 1e-8
	 * @return ExpectationMaximizationGmm_F64
	 */
	public static ExpectationMaximizationGmm_F64 gaussianMixtureModelEM_F64(
			int maxIterations, int maxConverge , double convergeTol) {

		StandardKMeans_F64 kmeans = kMeans_F64(null,maxIterations,maxConverge,convergeTol);
		SeedFromKMeans_F64 seeds = new SeedFromKMeans_F64(kmeans);

		return new ExpectationMaximizationGmm_F64(maxIterations,convergeTol,seeds);
	}

	/**
	 * High level interface for creating k-means cluster.  If more flexibility is needed (e.g. custom seeds)
	 * then create and instance of {@link org.ddogleg.clustering.kmeans.StandardKMeans_F64} directly
	 *
	 * @param initializer Specify which method should be used to select the initial seeds for the clusters.  null means default.
	 * @param maxIterations Maximum number of iterations it will perform.
	 * @param maxConverge Maximum iterations allowed before convergence.  Re-seeded if it doesn't converge.
	 * @param convergeTol Distance based convergence tolerance.  Try 1e-8
	 * @return StandardKMeans_F64
	 */
	public static StandardKMeans_F64 kMeans_F64( KMeansInitializers initializer,
												 int maxIterations, int maxConverge , double convergeTol) {
		InitializeKMeans_F64 seed;

		if( initializer == null ) {
			seed = new InitializePlusPlus();
		} else {
			switch (initializer) {
				case PLUS_PLUS:
					seed = new InitializePlusPlus();
					break;

				case STANDARD:
					seed = new InitializeStandard_F64();
					break;

				default:
					throw new RuntimeException("Unknown initializer " + initializer);
			}
		}
		return new StandardKMeans_F64(maxIterations,maxConverge,convergeTol,seed);
	}
}
