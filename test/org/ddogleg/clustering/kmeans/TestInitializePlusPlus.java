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
import org.ddogleg.struct.DogArray_F64;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestInitializePlusPlus extends StandardInitializeKMeansChecks {
	/**
	 * In this situation there are not enough unique points which can act as unique seeds.
	 * <p>
	 * This is a stricter version of generic test
	 */
	@Test void notEnoughUniquePoints_strict() {
		int DOF = 18;
		int NUM_SEEDS = 20;

		List<double[]> points = TestStandardKMeans.createPoints(DOF, 30, true);

		for (int i = 1; i < points.size(); i += 2) {
			System.arraycopy(points.get(i - 1), 0, points.get(i), 0, DOF);
		}
		var seeds = new DogArray<>(() -> new double[DOF]);

		performClustering(DOF, NUM_SEEDS, points, seeds);
		assertEquals(NUM_SEEDS, seeds.size);

		var hits = new int[15];
		for (double[] a : seeds.toList()) {
			int match = findMatch(a, points)/2;
			hits[match]++;
		}

		// make sure each one was selected at least once
		for (int i = 0; i < hits.length; i++) {
			assertTrue(hits[i] > 0);
		}
	}

	/**
	 * Test seed selection by seeing if it has the expected distribution.
	 */
	@Test void selectPointForNextSeed() {
		final int DOF = 1;
		var alg = new InitializePlusPlus<double[]>();
		alg.initialize(new EuclideanSqArrayF64(DOF), 123);

		alg.distances = DogArray_F64.array(3, 6, 1);
		alg.sumDistances = 10.0;

		var histogram = new double[3];

		for (int i = 0; i < 1000; i++) {
			histogram[alg.selectPointForNextSeed(rand.nextDouble())]++;
		}
		assertEquals(0.3, histogram[0]/1000.0, 0.02);
		assertEquals(0.6, histogram[1]/1000.0, 0.02);
		assertEquals(0.1, histogram[2]/1000.0, 0.02);
	}

	@Test void updateDistanceWithNewSeed() {
		final int DOF = 1;
		var alg = new InitializePlusPlus<double[]>();
		alg.initialize(new EuclideanSqArrayF64(DOF), 123);

		alg.distances = DogArray_F64.array(3, 6, 1);
		alg.sumDistances = Double.NaN; // if not reset this will mess it up
		var points = new ArrayList<double[]>();
		for (int i = 0; i < 3; i++) {
			points.add(new double[]{i*i});
		}
		var accessor = new ListAccessor<>(points, ( src, dst ) -> System.arraycopy(src, 0, dst, 0, DOF));

		alg.updateDistanceWithNewSeed(accessor, new double[]{-1});
		assertEquals(1, alg.distances.get(0), 1e-8);
		assertEquals(4, alg.distances.get(1), 1e-8);
		assertEquals(1, alg.distances.get(2), 1e-8);
		assertEquals(6, alg.sumDistances, 1e-8);
	}

	@Override
	public InitializeKMeans<double[]> createAlg( int DOF ) {
		return new InitializePlusPlus<>();
	}
}