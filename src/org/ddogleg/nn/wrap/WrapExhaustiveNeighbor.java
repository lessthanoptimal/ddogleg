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

package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.ExhaustiveNeighbor;
import org.ddogleg.nn.alg.KdTreeDistance;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.DogArray_I32;

import java.util.List;

/**
 * Wrapper around {@link org.ddogleg.nn.alg.ExhaustiveNeighbor} for {@link NearestNeighbor}
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class WrapExhaustiveNeighbor<P> implements NearestNeighbor<P> {

	KdTreeDistance<P> distance;
	List<P> points;

	public WrapExhaustiveNeighbor(KdTreeDistance<P> distance ) {
		this.distance = distance;
	}

	@Override
	public void setPoints(List<P> points, boolean trackIndicies) {
		this.points = points;
	}

	@Override
	public Search<P> createSearch() {
		return new InternalSearch(distance);
	}

	private class InternalSearch implements Search<P> {
		ExhaustiveNeighbor<P> alg;
		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		InternalSearch(KdTreeDistance<P> distance) {
			alg = new ExhaustiveNeighbor<>(distance);
			alg.setPoints(points);
		}

		@Override
		public boolean findNearest(P point, double maxDistance, NnData<P> result) {
			if (maxDistance < 0)
				maxDistance = Double.MAX_VALUE;

			alg.setPoints(points);

			int index = alg.findClosest(point, maxDistance);
			if (index >= 0) {
				result.point = points.get(index);
				result.distance = alg.getBestDistance();
				result.index = index;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void findNearest(P point, double maxDistance, int numNeighbors, DogArray<NnData<P>> results) {
			results.reset();

			if (maxDistance < 0)
				maxDistance = Double.MAX_VALUE;

			alg.setPoints(points);

			outputIndex.reset();
			outputDistance.reset();
			alg.findClosestN(point, maxDistance, numNeighbors, outputIndex, outputDistance);

			for (int i = 0; i < outputIndex.size; i++) {
				int index = outputIndex.get(i);
				NnData<P> r = results.grow();
				r.distance = outputDistance.get(i);
				r.point = points.get(index);
				r.index = index;
			}
		}
	}
}
