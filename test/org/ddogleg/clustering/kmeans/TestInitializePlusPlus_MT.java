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

import org.ddogleg.clustering.misc.EuclideanSqArrayF64;
import org.ddogleg.clustering.misc.ListAccessor;
import org.ddogleg.struct.DogArray;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
class TestInitializePlusPlus_MT {
	/**
	 * Create a random scenario and compare toe single threaded results
	 */
	@Test void compare() {
		final int DOF = 9;
		var single = new InitializePlusPlus<double[]>();
		var threaded = new InitializePlusPlus_MT<>(() -> new double[DOF]);

		List<double[]> points = TestStandardKMeans.createPoints(DOF, 500, true);

		var seedsSingle = new DogArray<>(() -> new double[DOF]);
		var seedsThreaded = new DogArray<>(() -> new double[DOF]);

		for (int trial = 0; trial < 2; trial++) {
			performClustering(single, DOF, 15, points, seedsSingle);
			performClustering(threaded, DOF, 15, points, seedsThreaded);

			assertEquals(seedsSingle.size, seedsThreaded.size);
			for (int seedIdx = 0; seedIdx < seedsThreaded.size; seedIdx++) {
				double[] e = seedsSingle.get(seedIdx);
				double[] f = seedsThreaded.get(seedIdx);

				assertArrayEquals(e, f, UtilEjml.TEST_F64);
			}
		}
	}

	protected void performClustering( InitializePlusPlus<double[]> alg,
									  int DOF, int NUM_SEEDS, List<double[]> points, DogArray<double[]> seeds ) {
		var accessor = new ListAccessor<>(points,
				( src, dst ) -> System.arraycopy(src, 0, dst, 0, DOF), double[].class);
		EuclideanSqArrayF64 distance = new EuclideanSqArrayF64(DOF);
		alg.initialize(distance, 0xBEEF);
		alg.selectSeeds(accessor, NUM_SEEDS, seeds);
	}
}