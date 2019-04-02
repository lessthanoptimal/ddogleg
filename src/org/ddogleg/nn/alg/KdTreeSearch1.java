/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

/**
 * Interface for searching a single tree for the nearest-neighbor
 *
 * @author Peter Abeles
 */
public interface KdTreeSearch1<P> {

	/**
	 * Specifies the maximum distance a closest-point needs to be to be considered
	 *
	 * @param maxDistance maximum distance from target
	 */
	void setMaxDistance(double maxDistance );

	/**
	 * Searches for the nearest neighbor to the target.  If no point is found that is less than maxDistance
	 * then return null.
	 *
	 * @param target Point whose nearest neighbor is being searched for
	 * @return The closest point or null if there is none.
	 */
	KdTree.Node findNeighbor(P target);

	/**
	 * Returns the distance of the closest node.
	 *
	 * @return distance to closest node.
	 */
	double getDistance();

	/**
	 * Creates a copy of this search with the same configuration. workspace isn't copied
	 */
	KdTreeSearch1<P> copy();
}
