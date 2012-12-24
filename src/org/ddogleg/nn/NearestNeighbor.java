package org.ddogleg.nn;

import java.util.List;

/**
 * Abstract interface for finding the nearest neighbor of a set of points in K-dimensional space.   Solution can
 * be exact or approximate, depending on the implementation.
 *
 * WARNING: Do not modify the input lists until after the NN search is no longer needed.  If the input lists do need
 * to be modified, then pass ina copy instead.  This reduced memory overhead significantly.
 *
 * @author Peter Abeles
 */
public interface NearestNeighbor<D> {

	/**
	 * Initializes data structures.
	 *
	 * @param pointDimension Dimension of input data
	 */
	public void init( int pointDimension );

	/**
	 * Specifies the set of points which are to be searched.
	 *
	 * @param points
	 * @param data
	 */
	public void setPoints( List<double[]> points , List<D> data );

	/**
	 * Searches for the closest neighbor to point.  The neighbor must be within maxDistance.
	 *
	 * @param point A point being searched for.
	 * @param maxDistance Maximum distance the neighbor can be from point.
	 * @param result Storage for the result.
	 * @return true if a match within the max distance was found.
	 */
	public boolean findNearest( double[] point , double maxDistance , NnData<D> result );
}
