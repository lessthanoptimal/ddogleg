/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.List;

/**
 * Exhaustively finds the nearest-neighbor to a n-dimensional point by considering every possibility.
 *
 * @author Peter Abeles
 */
public class ExhaustiveNeighbor {

	// Number of elements in each point
	int N;
	// List of points
	List<double[]> points;

	// the distance to the closest node found so far
	double bestDistance;

	GrowQueue_F64 distances = new GrowQueue_F64();
	GrowQueue_I32 indexes = new GrowQueue_I32();
	GrowQueue_I32 indexesSort = new GrowQueue_I32();

	public ExhaustiveNeighbor(int n) {
		N = n;
	}

	public ExhaustiveNeighbor() {
	}

	/**
	 * Specifies the point's dimension
	 *
	 * @param n dimension
	 */
	public void setN(int n) {
		N = n;
	}

	/**
	 * The input list which the nearest-neighbor is to be found inside of
	 *
	 * @param points List od points
	 */
	public void setPoints( List<double[]> points ) {
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
	public int findClosest( double[] p , double maxDistance ) {
		int best = -1;
		bestDistance = maxDistance;

		for( int i = 0; i < points.size(); i++ ) {
			double[] c = points.get(i);

			double distanceC = 0;
			for( int j = 0; j < N; j++ ) {
				double d = p[j] - c[j];
				distanceC += d*d;
			}

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
	public void findClosestN( double[] p , double maxDistance , int numNeighbors ,
							  GrowQueue_I32 outputIndex ,
							  GrowQueue_F64 outputDistance ) {

		// Compute the distance of each point and save the ones within range
		distances.reset();
		indexes.reset();

		for( int i = 0; i < points.size(); i++ ) {
			double[] c = points.get(i);

			double distanceC = 0;
			for( int j = 0; j < N; j++ ) {
				double d = p[j] - c[j];
				distanceC += d*d;
			}

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
