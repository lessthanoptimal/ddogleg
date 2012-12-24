package org.ddogleg.nn.alg;

import java.util.List;

/**
 * Exhaustively finds the nearest-neighbor to a point by considering every possibility.
 *
 * @author Peter Abeles
 */
public class ExhaustiveNeighbor {

	// Number of elements in each point
	int N;
	// List of points
	List<double[]> points;

	public ExhaustiveNeighbor(int n) {
		N = n;
	}

	public ExhaustiveNeighbor() {
	}

	public void setN(int n) {
		N = n;
	}

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
		double bestDistance = maxDistance*maxDistance;

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
}
