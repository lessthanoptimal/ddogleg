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
import org.ddogleg.struct.FastArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"NullAway"})
public class TestAssignKMeans {
	@Test void assign() {
		var clusters = new FastArray<>(double[].class);

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		var alg = new AssignKMeans<>(clusters, new EuclideanSqArrayF64(3));

		assertEquals(1,alg.assign(new double[]{0,0,9}));
		assertEquals(0, alg.assign(new double[]{12, 0, 0}));
	}

	@Test void assign_soft() {
		var clusters = new FastArray<>(double[].class);

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{5,0,0});

		var alg = new AssignKMeans<>(clusters, new EuclideanSqArrayF64(3));

		var histogram = new double[2];

		alg.assign(new double[]{10,0,0},histogram);
		assertEquals(1.0, histogram[0], 1e-8);
		assertEquals(0.0, histogram[1],1e-8);

		// see if much more weight is given to the second one
		alg.assign(new double[]{6, 0, 0}, histogram);
		assertTrue(histogram[0]*10 < histogram[1]);

		// this is actually a difficult case for using this type of distance metric
		// one cluster is much farther away and as a result the weight is equality split between the two closer points
		// which might not be desirable
		clusters.add( new double[]{5000,0,0});
		histogram = new double[3];
		alg.assign(new double[]{6,0,0},histogram);
		assertTrue(histogram[0]/30.0 > histogram[2]);
		assertEquals(histogram[0], histogram[1], 0.01);
	}
}
