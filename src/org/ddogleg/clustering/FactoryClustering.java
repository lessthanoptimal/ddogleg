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

import org.ddogleg.clustering.gmm.ExpectationMaximizationGmm_F64;
import org.ddogleg.clustering.gmm.SeedFromKMeans_F64;
import org.ddogleg.clustering.kmeans.*;
import org.ddogleg.clustering.misc.EuclideanSqArrayF64;
import org.ddogleg.clustering.misc.MeanArrayF64;
import org.ddogleg.struct.DogLambdas;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating clustering algorithms.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("unchecked") public class FactoryClustering {

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
			int maxIterations, int maxConverge, double convergeTol, int pointDimension ) {

		ConfigKMeans configKMeans = new ConfigKMeans();
		configKMeans.reseedAfterIterations = maxConverge;
		configKMeans.maxIterations = maxIterations;
		configKMeans.convergeTol = convergeTol;

		StandardKMeans<double[]> kmeans = kMeans(configKMeans, pointDimension, double[].class);
		SeedFromKMeans_F64 seeds = new SeedFromKMeans_F64(kmeans);

		return new ExpectationMaximizationGmm_F64(maxIterations, convergeTol, pointDimension, seeds);
	}

	/**
	 * K-Means using a primitive array, e.g. double[].
	 *
	 * @param pointDimension Length of the array
	 * @param dataType Specifies the data type, e.g. double[].class
	 */
	public static <T> StandardKMeans<T> kMeans( @Nullable ConfigKMeans config, int pointDimension, Class<T> dataType ) {
		if (dataType != double[].class)
			throw new IllegalArgumentException("Only double[] supported at this time.");

		return (StandardKMeans)kMeans(config,
				new MeanArrayF64(pointDimension),
				new EuclideanSqArrayF64(pointDimension),
				() -> new double[pointDimension]);
	}

	/**
	 *
	 * @param minimumForThreads The minimum number of points required for it to use concurrent code
	 */
	public static <T> StandardKMeans<T> kMeans_MT( @Nullable ConfigKMeans config, int pointDimension,
												   int minimumForThreads, Class<T> dataType ) {
		if (dataType != double[].class)
			throw new IllegalArgumentException("Only double[] supported at this time.");

		return (StandardKMeans)kMeans_MT(config,minimumForThreads,
				new MeanArrayF64(pointDimension),
				new EuclideanSqArrayF64(pointDimension),
				() -> new double[pointDimension]);
	}

	/**
	 * High level interface for creating k-means cluster.  If more flexibility is needed (e.g. custom seeds)
	 * then create and instance of {@link StandardKMeans} directly
	 *
	 * @param config Configuration for tuning parameters
	 * @param updateMeans Used to compute the means given point assignments
	 * @param factory Creates a new instance of a point
	 * @return StandardKMeans_F64
	 */
	public static <P> StandardKMeans<P> kMeans( @Nullable ConfigKMeans config,
												ComputeMeanClusters<P> updateMeans,
												PointDistance<P> pointDistance,
												DogLambdas.NewInstance<P> factory ) {
		if (config == null)
			config = new ConfigKMeans();

		InitializeKMeans<P> seed;

		switch (config.initializer) {
			case PLUS_PLUS:
				seed = new InitializePlusPlus<>();
				break;

			case STANDARD:
				seed = new InitializeStandard<>();
				break;

			default:
				throw new RuntimeException("Unknown initializer " + config.initializer);
		}

		StandardKMeans<P> alg = new StandardKMeans<>(updateMeans, seed, pointDistance, factory);
		alg.convergeTol = config.convergeTol;
		alg.maxIterations = config.maxIterations;
		alg.reseedAfterIterations = config.reseedAfterIterations;
		alg.maxReSeed = config.maxReSeed;

		return alg;
	}

	/**
	 *
	 * @param minimumForThreads The minimum number of points required for it to use concurrent code
	 */
	public static <P> StandardKMeans<P> kMeans_MT( @Nullable ConfigKMeans config,
												   int minimumForThreads,
												   ComputeMeanClusters<P> updateMeans,
												   PointDistance<P> pointDistance,
												   DogLambdas.NewInstance<P> factory ) {
		if (config == null)
			config = new ConfigKMeans();

		InitializeKMeans<P> seed;

		switch (config.initializer) {
			case PLUS_PLUS: {
				seed = new InitializePlusPlus_MT<>(factory);
				((InitializePlusPlus_MT)seed).setMinimumConcurrent(minimumForThreads);
			} break;

			case STANDARD:
				seed = new InitializeStandard<>(); // TODO make concurrent
				break;

			default:
				throw new RuntimeException("Unknown initializer " + config.initializer);
		}

		var alg = new StandardKMeans_MT<>(updateMeans, seed, pointDistance, factory);
		alg.convergeTol = config.convergeTol;
		alg.maxIterations = config.maxIterations;
		alg.reseedAfterIterations = config.reseedAfterIterations;
		alg.maxReSeed = config.maxReSeed;
		alg.setMinimumForConcurrent(minimumForThreads);

		return alg;
	}
}
