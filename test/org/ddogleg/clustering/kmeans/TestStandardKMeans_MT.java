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

import org.ddogleg.clustering.ComputeMeanClusters;
import org.ddogleg.clustering.PointDistance;
import org.ddogleg.clustering.misc.EuclideanSqArrayF64;
import org.ddogleg.clustering.misc.ListAccessor;
import org.ddogleg.clustering.misc.MeanArrayF64;
import org.ddogleg.struct.DogArray;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
class TestStandardKMeans_MT {
	/**
	 * Create a simple random scenario and compare single to threaded results
	 */
	@Test void compare() {
		final int DOF = 13;
		List<double[]> points = TestStandardKMeans.createPoints(DOF, 500, true);
		ListAccessor<double[]> accessor = new ListAccessor<>(points,
				( src, dst ) -> System.arraycopy(src, 0, dst, 0, DOF), double[].class);

		StandardKMeans<double[]> single = createAlg(DOF, false);
		StandardKMeans<double[]> multi = createAlg(DOF, true);

		single.initialize(0xBEEF);
		multi.initialize(0xBEEF);

		for (int trial = 0; trial < 2; trial++) {
			single.process(accessor, 14);
			multi.process(accessor, 14);

			assertEquals(single.getBestClusterScore(), multi.getBestClusterScore(), UtilEjml.TEST_F64);

			DogArray<double[]> singleCluster = single.getBestClusters();
			DogArray<double[]> multiCluster = multi.getBestClusters();

			assertEquals(14, singleCluster.size);
			assertEquals(14, multiCluster.size);

			for (int seedIdx = 0; seedIdx < singleCluster.size; seedIdx++) {
				double[] e = singleCluster.get(seedIdx);
				double[] f = multiCluster.get(seedIdx);

				assertArrayEquals(e, f, UtilEjml.TEST_F64);
			}
		}
	}

	private StandardKMeans<double[]> createAlg( int DOF, boolean threaded ) {
		ComputeMeanClusters<double[]> updateMeans = new MeanArrayF64(DOF);
		InitializeKMeans<double[]> seedSelector = new InitializePlusPlus<>();
		PointDistance<double[]> distancer = new EuclideanSqArrayF64(DOF);
		StandardKMeans<double[]> alg =
				threaded ?
						new StandardKMeans_MT<>(updateMeans, seedSelector, distancer, () -> new double[DOF]) :
						new StandardKMeans<>(updateMeans, seedSelector, distancer, () -> new double[DOF]);
		alg.reseedAfterIterations = 100;
		alg.maxIterations = 100;
		alg.maxReSeed = 0;
		alg.convergeTol = 1e-8;

		alg.initialize(123);
		return alg;
	}
}