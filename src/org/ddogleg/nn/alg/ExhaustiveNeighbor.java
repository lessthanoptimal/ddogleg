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

package org.ddogleg.nn.alg;

import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.DogArray_I32;

import java.util.List;

/**
 * Exhaustively finds the nearest-neighbor to a n-dimensional point by considering every possibility.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class ExhaustiveNeighbor<P> {

	// List of points
	List<P> points;

	// the distance to the closest node found so far
	double bestDistance;

	final DogArray_F64 distances = new DogArray_F64();
	final DogArray_I32 indexes = new DogArray_I32();
	final DogArray_I32 indexesSort = new DogArray_I32();

	KdTreeDistance<P> distance;

	public ExhaustiveNeighbor( KdTreeDistance<P> distance) {
		this.distance = distance;
	}

	/**
	 * The input list which the nearest-neighbor is to be found inside of
	 *
	 * @param points List od points
	 */
	public void setPoints( List<P> points ) {
		this.points = points;
	}

	/**
	 * Finds the index of the point which has the smallest Euclidean distance to 'p' and is {@code <} maxDistance
	 * away.
	 *
	 * @param p A point.
	 * @param maxDistance The maximum distance (Euclidean squared) the neighbor can be.
	 * @return Index of the closest point.
	 */
	public int findClosest( P p , double maxDistance ) {
		int best = -1;
		bestDistance = maxDistance;

		for( int i = 0; i < points.size(); i++ ) {
			P c = points.get(i);

			double distanceC = distance.distance(p,c);

			if( distanceC <= bestDistance ) {
				bestDistance = distanceC;
				best = i;
			}
		}

		return best;
	}

	/**
	 * Finds the index of the point which has the smallest Euclidean distance to 'p' and is {@code <} maxDistance
	 * away.
	 *
	 * @param p A point.
	 * @param maxDistance The maximum distance (Euclidean squared) the neighbor can be.
	 * @param numNeighbors the requested number of nearest neighbors it should search for
	 * @param outputIndex Storage for the index of the closest elements
	 * @param outputDistance Storage for the distance of the closest elements
	 */
	public void findClosestN( P p , double maxDistance , int numNeighbors ,
							  DogArray_I32 outputIndex ,
							  DogArray_F64 outputDistance ) {

		// Compute the distance of each point and save the ones within range
		distances.reset();
		indexes.reset();

		for( int i = 0; i < points.size(); i++ ) {
			P c = points.get(i);

			double distanceC = distance.distance(p,c);

			if( distanceC <= maxDistance ) {
				distances.add(distanceC);
				indexes.add(i);
			}
		}

		// find the N closest elements
		numNeighbors = Math.min(distances.size,numNeighbors);
		if( numNeighbors == 0 )
			return;

		indexesSort.resize(distances.size);
		QuickSelect.selectIndex(distances.data,numNeighbors-1,distances.size,indexesSort.data);

		for( int i = 0; i < numNeighbors; i++ ) {
			int index = indexes.get(indexesSort.get(i));
			outputIndex.add( index );
			outputDistance.add( distances.get( indexesSort.get(i) ) );
		}
	}

	public double getBestDistance() {
		return bestDistance;
	}
}
