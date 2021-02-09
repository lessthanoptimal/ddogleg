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

import org.ddogleg.clustering.PointDistance;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.LArrayAccessor;

import java.util.Random;

/**
 * <p>
 * Implementation of the seeding strategy described in [1]. A point is randomly selected from the list as the first
 * seed.  The remaining seeds are selected randomly based on the distance of each seed from their closest
 * cluster.
 * </p>
 *
 * <p>
 * [1] David Arthur and Sergei Vassilvitskii. 2007. k-means++: the advantages of careful seeding.
 * In Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms (SODA '07).
 * Society for Industrial and Applied Mathematics, Philadelphia, PA, USA, 1027-1035.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class InitializePlusPlus<P> implements InitializeKMeans<P> {
	Random rand;

	PointDistance<P> computeDistance;

	DogArray_F64 distances = new DogArray_F64();

	double sumDistances;

	@Override
	public void initialize( PointDistance<P> distance, long randomSeed ) {
		this.computeDistance = distance;
		rand = new Random(randomSeed);
	}

	@Override
	public void selectSeeds( LArrayAccessor<P> points, int totalSeeds, DogArray<P> selectedSeeds ) {
		if (totalSeeds > points.size())
			throw new IllegalArgumentException("More seeds requested than points!");

		// Pre-allocate memory and reset the output
		selectedSeeds.reserve(totalSeeds);
		selectedSeeds.reset();

		// the first seed is randomly selected from the list of points
		points.getCopy(rand.nextInt(points.size()), selectedSeeds.grow());

		// Initialize the sum of distance from seeds to 0
		distances.resize(points.size(), Double.MAX_VALUE);

		// Update with information from the first seed
		updateDistanceWithNewSeed(points, selectedSeeds.get(0));

		// Select the remaining seeds probabilistically based on distance from prior seeds
		for (int seedIdx = 1; seedIdx < totalSeeds; seedIdx++) {
			int selected = selectPointForNextSeed(rand.nextDouble());
			P seed = selectedSeeds.grow();
			points.getCopy(selected, seed);
			updateDistanceWithNewSeed(points, seed);
		}
	}

	@Override public InitializeKMeans<P> newInstanceThread() {
		return new InitializePlusPlus<>();
	}

	/**
	 * A new seed has been added and the distance from the seeds needs to be updated
	 */
	protected void updateDistanceWithNewSeed( LArrayAccessor<P> points, P seed ) {
		sumDistances = 0;
		for (int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
			P point = points.getTemp(pointIdx);

			// Set the distance ot be the distance of th closest seed
			double d = computeDistance.distance(point, seed);
			double prevD = distances.data[pointIdx];
			if (d < prevD) {
				distances.data[pointIdx] = d;
				sumDistances += d;
			} else {
				sumDistances += prevD;
			}
		}
	}

	/**
	 * Randomly selects the next seed.  The chance of a seed is based upon its distance
	 * from the closest cluster.  Larger distances mean more likely.
	 *
	 * @param targetFraction Number from 0 to 1, inclusive
	 * @return Index of the selected seed
	 */
	protected int selectPointForNextSeed( double targetFraction ) {
		// this won't select previously selected points because the distance will be zero
		// If the distance is zero it will simply skip over it
		double sum = 0;
		double targetValue = sumDistances*targetFraction;
		for (int pointIdx = 0; pointIdx < distances.size(); pointIdx++) {
			double d = distances.get(pointIdx);
			sum += d;
			if (sum >= targetValue && d != 0.0)
				return pointIdx;
		}

		// If every single point has already been matched to a seed then they will all have zero distance
		// In this situation we will just select a random point.
		return rand.nextInt(distances.size);
	}
}
