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

package org.ddogleg.nn;

import org.ddogleg.struct.FastQueue;

import java.util.List;

/**
 * <p>
 * Abstract interface for finding the nearest neighbor to a user specified point inside of a set of points in
 * K-dimensional space.   Solution can be exact or approximate, depending on the implementation. The distance
 * metric is intentionally left undefined and is implementation dependent.
 * </p>
 *
 * <p>
 * WARNING: Do not modify the input lists until after the NN search is no longer needed.  If the input lists do need
 * to be modified, then pass in a copy instead.  This restriction reduced memory overhead significantly.
 * </p>
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
	 * @param points Set of points.
	 * @param data (Optional) Associated data.  Can be null.
	 */
	public void setPoints( List<double[]> points , List<D> data );

	/**
	 * Searches for the nearest neighbor to the specified point.  The neighbor must be within maxDistance.
	 *
	 * <p>
	 * NOTE: How distance is measured is not specified here. See the implementation's documentation.  Euclidean
	 * distance squared is common.
	 * </p>
	 *
	 * @param point A point being searched for.
	 * @param maxDistance Maximum distance (inclusive, e.g. d &le; maxDistance) a neighbor can be from point.
	 *                    Values {@code <} 0 will be set to the maximum distance.
	 * @param result Storage for the result.
	 * @return true if a match within the max distance was found.
	 */
	public boolean findNearest( double[] point , double maxDistance , NnData<D> result );

	/**
	 * Searches for the N nearest neighbor to the specified point.  The neighbors must be within maxDistance.
	 *
	 * <p>
	 * NOTE: How distance is measured is not specified here. See the implementation's documentation.  Euclidean
	 * distance squared is common.
	 * </p>
	 *
	 * @param point A point being searched for.
	 * @param maxDistance Maximum distance (inclusive, e.g. d &le; maxDistance) the neighbor can be from point.
	 *                    Values {@code <} 0 will be set to the maximum distance.
	 * @param numNeighbors The number of neighbors it will search for.
	 * @param result Storage for the result. Must be empty before calling. Must support grow() function.
	 */
	public void findNearest( double[] point , double maxDistance , int numNeighbors , FastQueue<NnData<D>> result );
}
