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
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.LArrayAccessor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Seeds are selects by randomly picking points.  This is the standard way to initialize k-means
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class InitializeStandard<P> implements InitializeKMeans<P> {

	protected Random rand;

	// List of unselected indexes
	protected DogArray_I32 unused = new DogArray_I32();
	// Set indicating which indexes have been used
	protected Set<Integer> used = new HashSet<>();

	@Override
	public void initialize(PointDistance<P> distance, long randomSeed) {
		rand = new Random(randomSeed);
	}

	@Override
	public void selectSeeds( LArrayAccessor<P> points, int totalSeeds, DogArray<P> selectedSeeds) {

		if( totalSeeds > points.size() )
			throw new IllegalArgumentException("More seeds requested than points!");

		selectedSeeds.resize(totalSeeds);

		if( totalSeeds*2 > points.size() ) {
			// The number of seeds is small relative to points, then keep then do an opt-out strategy
			unused.resize(points.size());
			for (int i = 0; i < points.size(); i++) {
				unused.set(i,i);
			}

			for (int i = 0; i < totalSeeds; i++) {
				int index = unused.removeSwap(rand.nextInt(unused.size));
				points.getCopy(index, selectedSeeds.get(i));
			}
		} else {
			// clear the used list since nothing is used yet
			used.clear();

			// randomly select indexes until the desired number have been selected
			while (used.size() < totalSeeds) {
				int index = rand.nextInt(points.size());
				if (used.contains(index))
					continue;
				// copy the point into the output set
				points.getCopy(index, selectedSeeds.get(used.size()));

				// mark this index as being used
				used.add(index);
			}
		}
	}

	@Override public InitializeKMeans<P> newInstanceThread() {
		return new InitializeStandard<>();
	}
}
