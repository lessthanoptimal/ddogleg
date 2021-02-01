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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public abstract class StandardInitializeKMeansChecks {
	protected Random rand = new Random(234);

	public abstract InitializeKMeans<double[]> createAlg(int dof);

	/**
	 * In this situation there are not enough unique points which can act as unique seeds
	 */
	@Test void notEnoughUniquePoints() {
		int DOF = 20;
		int NUM_SEEDS = 20;

		List<double[]> points = TestStandardKMeans.createPoints(DOF, 30, true);
		for (int i = 1; i < points.size(); i += 2) {
			System.arraycopy(points.get(i - 1), 0, points.get(i), 0, DOF);
		}

		var seeds = new DogArray<>(() -> new double[DOF]);

		performClustering(DOF, NUM_SEEDS, points, seeds);

		// just make sure it found a match in the input set
		for (double[] a : seeds.toList()) {
			findMatch(a, points);
		}
	}

	public static int findMatch(double[] a, List<double[]> list) {
		for (int i = 0; i < list.size(); i++) {
			double[] b = list.get(i);
			boolean match = true;
			for (int j = 0; j < a.length; j++) {
				if (a[j] != b[j]) {
					match = false;
					break;
				}
			}
			if (match)
				return i;
		}
		throw new RuntimeException("Egads.  bug?");
	}

	@Test void selectSeeds() {
		int DOF = 20;
		int NUM_SEEDS = 20;

		List<double[]> points = TestStandardKMeans.createPoints(DOF, 100, true);
		var seeds = new DogArray<>(() -> new double[DOF]);

		performClustering(DOF, NUM_SEEDS, points, seeds);

		// make sure nothing was added to the list
		assertEquals(20, seeds.size());

		for (int i = 0; i < seeds.size(); i++) {
			double[] s = seeds.get(i);

			// make sure the seed was written to
			for (int j = 0; j < DOF; j++) {
				assertTrue(s[j] != 0);
			}

			// make sure it wasn't swapped with one of the points
			for (int j = 0; j < points.size(); j++) {
				assertTrue(points.get(j) != s);
			}
		}
	}

	/**
	 * Request more seeds than there are points.  This is impossible to do and ensure the seeds are
	 * unique.
	 */
	@Test void impossible() {
		try {
			int DOF = 20;
			int NUM_SEEDS = 4;

			// fewer points than seeds
			List<double[]> points = TestStandardKMeans.createPoints(DOF, NUM_SEEDS - 1, true);
			var seeds = new DogArray<>(() -> new double[DOF]);

			performClustering(DOF, NUM_SEEDS, points, seeds);

			fail("Should have thrown an exception!");
		} catch (Exception ignored) {
		}
	}

	/**
	 * Makes sure the seeds that it selects are unique
	 */
	@Test void uniqueSeeds() {
		// extremely unlikely that it will select each one uniquely if random
		uniqueSeeds(4, 4);
		// The standard algorithm switches technique when the ratio of points to seeds become
		// more extreme.  This might not have a collision
		uniqueSeeds(10, 4);
	}

	public void uniqueSeeds(int numPoints, int numSeeds) {
		int DOF = 20;

		InitializeKMeans<double[]> alg = createAlg(DOF);
		var distance = new EuclideanSqArrayF64(DOF);

		alg.initialize(distance, 0xBEEF);

		// 4 points and 4 seeds.  Each point must be a seed
		List<double[]> points = TestStandardKMeans.createPoints(DOF, numPoints, true);
		var seeds = new DogArray<>(() -> new double[DOF]);
		var accessor = new ListAccessor<>(points, ( src, dst ) -> System.arraycopy(src, 0, dst, 0, DOF));

		for (int i = 0; i < 30; i++) {
			alg.selectSeeds(accessor, numSeeds, seeds);

			// quick test to see if it modified the inputs by adding/removing
			assertEquals(numPoints, points.size());
			assertEquals(numSeeds, seeds.size());

			// Make sure each seed is only selected once
			for (int j = 0; j < seeds.size(); j++) {
				double[] a = seeds.get(j);

				for (int k = j + 1; k < seeds.size(); k++) {
					double[] b = seeds.get(k);

					// see if they are identical
					boolean identical = true;
					for (int l = 0; l < a.length; l++) {
						if (a[l] != b[l]) {
							identical = false;
							break;
						}
					}
					if (identical) {
						fail("Seed is not unique");
					}
				}
			}
		}
	}

	protected void performClustering(int DOF, int NUM_SEEDS, List<double[]> points, DogArray<double[]> seeds) {
		var accessor = new ListAccessor<>(points, (src, dst) -> System.arraycopy(src, 0, dst, 0, DOF));
		InitializeKMeans<double[]> alg = createAlg(DOF);
		EuclideanSqArrayF64 distance = new EuclideanSqArrayF64(DOF);
		alg.initialize(distance, 0xBEEF);
		alg.selectSeeds(accessor, NUM_SEEDS, seeds);
	}
}
