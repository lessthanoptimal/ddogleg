/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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
	 * Finds the index of the point which has the smallest Euclidean distance to 'p' and is < maxDistance
	 * away.
	 *
	 * @param p A point.
	 * @param maxDistance The maximum distance the neighbor can be.
	 * @return Index of the closest point.
	 */
	public int findClosest( double[] p , double maxDistance ) {
		int best = -1;
		bestDistance = maxDistance*maxDistance;

		for( int i = 0; i < points.size(); i++ ) {
			double[] c = points.get(i);

			double distanceC = 0;
			for( int j = 0; j < N; j++ ) {
				double d = p[j] - c[j];
				distanceC += d*d;
			}

			if( distanceC < bestDistance ) {
				bestDistance = distanceC;
				best = i;
			}
		}

		return best;
	}

	public double getBestDistance() {
		return bestDistance;
	}
}
