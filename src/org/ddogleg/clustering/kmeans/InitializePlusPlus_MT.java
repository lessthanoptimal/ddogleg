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

import lombok.Getter;
import lombok.Setter;
import org.ddogleg.DDoglegConcurrency;
import org.ddogleg.struct.DogLambdas;
import org.ddogleg.struct.LArrayAccessor;
import pabeles.concurrency.GrowArray;

/**
 * <p>Concurrent implementation of {@link InitializePlusPlus}</p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class InitializePlusPlus_MT<P> extends InitializePlusPlus<P> {

	// Creates new points. Used as work space inside of threads
	DogLambdas.NewInstance<P> factoryPoint;

	// Stores the sum computed in each thread
	GrowArray<DistanceWork> threadsData;

	/**
	 * Minimum list size for it to use concurrent code. If a list is small it will run slower than the single
	 * thread version. By default this is zero since the optimal value is use case specific.
	 */
	@Getter @Setter int minimumConcurrent = 0;

	public InitializePlusPlus_MT( DogLambdas.NewInstance<P> factoryPoint ) {
		this.factoryPoint = factoryPoint;
		this.threadsData = new GrowArray<>(DistanceWork::new, DistanceWork::reset);
	}

	@Override public InitializeKMeans<P> newInstanceThread() {
		return new InitializePlusPlus_MT<>(factoryPoint);
	}

	/**
	 * A new seed has been added and the distance from the seeds needs to be updated
	 */
	@Override protected void updateDistanceWithNewSeed( LArrayAccessor<P> points, P seed ) {
		// see if it should run the single thread version instead
		if (points.size() < minimumConcurrent) {
			super.updateDistanceWithNewSeed(points, seed);
			return;
		}

		DDoglegConcurrency.loopBlocks(0, points.size(), threadsData, ( work, idx0, idx1 ) -> {
			final P point = work.point;
			double sum = 0.0;
			for (int pointIdx = idx0; pointIdx < idx1; pointIdx++) {
				points.getCopy(pointIdx, point);

				// Set the distance ot be the distance of th closest seed
				double d = computeDistance.distance(point, seed);
				double prevD = distances.data[pointIdx];
				if (d < prevD) {
					distances.data[pointIdx] = d;
					sum += d;
				} else {
					sum += prevD;
				}
			}
			work.sum = sum;
		});

		sumDistances = 0;
		for (int i = 0; i < threadsData.size(); i++) {
			sumDistances += threadsData.get(i).sum;
		}
	}

	private class DistanceWork {
		public double sum;
		public P point = factoryPoint.newInstance();

		public void reset() {
			sum = 0;
		}
	}
}
