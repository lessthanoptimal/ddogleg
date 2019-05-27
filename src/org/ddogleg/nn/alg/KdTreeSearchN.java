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

import org.ddogleg.struct.FastQueue;

/**
 * Interface for searching a single tree for the N nearest-neighbors.
 *
 * @author Peter Abeles
 */
public interface KdTreeSearchN<P> {

	/**
	 * Specifies the tree which will be searched. The type of object is implementation specific
	 *
	 * @param tree search tree
	 */
	void setTree( Object tree );

	/**
	 * Specifies the maximum distance a closest-point needs to be to be considered
	 *
	 * @param maxDistance maximum distance from target
	 */
	void setMaxDistance(double maxDistance);

	/**
	 * Searches for the N nearest-neighbors to the target.  The results are added to the 'results' list.
	 *
	 * @param target Point whose nearest neighbor is being searched for
	 * @param searchN Number of closest points it will find.  Must be {@code >=} 1
	 * @param results Storage for the found neighbors.
	 */
	void findNeighbor(P target, int searchN, FastQueue<KdTreeResult> results);

	/**
	 * Creates a copy of this search with the same configuration. workspace isn't copied
	 */
	KdTreeSearchN<P> copy();

}

