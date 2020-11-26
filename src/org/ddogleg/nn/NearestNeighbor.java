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

package org.ddogleg.nn;

import org.ddogleg.struct.DogArray;

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
public interface NearestNeighbor<P> {

	/**
	 * Specifies the set of points which are to be searched.
	 *
	 * @param points Set of points.
	 * @param trackIndices If true it will keep track of the index. Making it easy to associate data.
	 */
	void setPoints( List<P> points , boolean trackIndices );

	/**
	 * Creates a new search for this data structure. This is intended to enabled concurrent searches. After {@link
	 * #setPoints} has been called and returned, each searched can be called independently in separate threads. Do
	 * not call {@link #setPoints} which a search is being performed.
	 *
	 * @return A new search object for this instance.
	 */
	Search<P> createSearch();

	/**
	 * An independent search instance.
	 */
	interface Search<P> {

		/**
		 * Searches for the nearest neighbor to the specified point.  The neighbor must be within maxDistance.
		 *
		 * <p>
		 * NOTE: How distance is measured is not specified here. See the implementation's documentation.  Euclidean
		 * distance squared is common.
		 * </p>
		 *
		 * @param point (Input) A point being searched for.
		 * @param maxDistance (Input) Maximum distance (inclusive, e.g. d &le; maxDistance) a neighbor can be from point.
		 *                    Values {@code <} 0 will be set to the maximum distance.
		 * @param result (Output) Storage for the result.
		 * @return true if a match within the max distance was found.
		 */
		boolean findNearest( P point , double maxDistance , NnData<P> result );

		/**
		 * Searches for the N nearest neighbor to the specified point.  The neighbors must be within maxDistance.
		 *
		 * <p>
		 * NOTE: How distance is measured is not specified here. See the implementation's documentation.  Euclidean
		 * distance squared is common.
		 * </p>
		 *
		 * @param point (Input) A point being searched for.
		 * @param maxDistance (Input) Maximum distance (inclusive, e.g. d &le; maxDistance) the neighbor can be from point.
		 *                    Values {@code <} 0 will be set to the maximum distance.
		 * @param numNeighbors (Input) The number of neighbors it will search for.
		 * @param results (Output) Storage for the result. Reset() is called.
		 */
		void findNearest( P point , double maxDistance , int numNeighbors , DogArray<NnData<P>> results );
	}
}
