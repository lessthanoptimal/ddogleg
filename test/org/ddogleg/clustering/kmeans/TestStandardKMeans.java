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

import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.ComputeMeanClusters;
import org.ddogleg.clustering.GenericClusterChecks_F64;
import org.ddogleg.clustering.PointDistance;
import org.ddogleg.clustering.misc.EuclideanSqArrayF64;
import org.ddogleg.clustering.misc.ListAccessor;
import org.ddogleg.clustering.misc.MeanArrayF64;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.LArrayAccessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Can;t use generic checks because he seeds might suck and the sets will be poorly chosen.
 *
 * @author Peter Abeles
 */
public class TestStandardKMeans extends GenericClusterChecks_F64 {
	int DOF = 4;

	@Test void matchPointsToClusters() {
		StandardKMeans<double[]> alg = createAlg(DOF);

		alg.clusters.resize(3);
		alg.memberCount.resize(3);

		alg.clusters.data[0] = new double[]{20, 0, 0, 0};
		alg.clusters.data[1] = new double[]{0, 20, 0, 0};
		alg.clusters.data[2] = new double[]{0, 0, 20, 0};

		List<double[]> points = new ArrayList<>();
		points.add(new double[]{20, 5, 0, 0});
		points.add(new double[]{25, -4, 0, 0});
		points.add(new double[]{0, 0, 22, 0});

		ListAccessor<double[]> accessor = new ListAccessor<>(points,
				( src, dst ) -> System.arraycopy(src, 0, dst, 0, DOF), double[].class);

		alg.matchPointsToClusters(accessor);

		assertEquals(2, alg.memberCount.get(0));
		assertEquals(0, alg.memberCount.get(1));
		assertEquals(1, alg.memberCount.get(2));

		assertEquals(0, alg.assignments.get(0));
		assertEquals(0, alg.assignments.get(1));
		assertEquals(2, alg.assignments.get(2));
	}

	private StandardKMeans<double[]> createAlg( int DOF ) {
		ComputeMeanClusters<double[]> updateMeans = new MeanArrayF64(DOF);
		InitializeKMeans<double[]> seedSelector = new InitializeStandard<>();
		PointDistance<double[]> distancer = new EuclideanSqArrayF64(DOF);
		StandardKMeans<double[]> alg = new StandardKMeans<>(updateMeans, seedSelector, distancer,
				() -> new double[DOF]);
		alg.maxConverge = 100;
		alg.maxIterations = 100;
		alg.convergeTol = 1e-8;

		alg.initialize(123);
		return alg;
	}

	public static List<double[]> createPoints( int DOF, int total, boolean fillRandom ) {
		List<double[]> ret = new ArrayList<>();

		var random = new Random(23432 + DOF + total);

		for (int i = 0; i < total; i++) {
			double[] a = new double[DOF];
			if (fillRandom) {
				for (int j = 0; j < a.length; j++) {
					a[j] = random.nextGaussian();
				}
			}

			ret.add(a);
		}

		return ret;
	}

	@Override
	public ComputeClusters<double[]> createClustersAlg( boolean seedHint, int dof ) {
		return createAlg(dof);
	}

	public static class FixedSeeds implements InitializeKMeans<double[]> {
		@Override
		public void initialize( PointDistance<double[]> distance, long randomSeed ) {}

		@Override
		public void selectSeeds( LArrayAccessor<double[]> points, int totalSeeds, DogArray<double[]> selectedSeeds ) {
			selectedSeeds.reset();
			for (int i = 0; i < totalSeeds; i++) {
				points.getCopy(i, selectedSeeds.grow());
			}
		}

		@Override public InitializeKMeans<double[]> newInstanceThread() {
			return this;
		}
	}
}
