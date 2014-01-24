/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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
 * <p>
 * Implementation of {@link KdTreeSearchBestBinFirst} which searches for the single best nearest-neighbor.
 * </p>
 *
 * @author Peter Abeles
 */
public class KdTreeSearch1Bbf extends KdTreeSearchBestBinFirst implements KdTreeSearch1 {

	// the best node so far
	private KdTree.Node bestNode;

	/**
	 * Configures the search
	 *
	 * @param maxNodesSearched Maximum number of nodes it will search.  Used to limit CPU time.
	 */
	public KdTreeSearch1Bbf(int maxNodesSearched) {
		super(maxNodesSearched);
	}

	@Override
	public KdTree.Node findNeighbor(double[] target) {

		bestNode = null;

		_findClosest(target);

		return bestNode;
	}

	@Override
	public double getDistance() {
		return bestDistanceSq;
	}

	/**
	 * Checks to see if the current node's point is the closet point found so far
	 */
	@Override
	protected void checkBestDistance(KdTree.Node node, double[] target) {
		double distanceSq = KdTree.distanceSq(node,target,N);
		if( distanceSq <= bestDistanceSq ) {
			if( bestNode == null || distanceSq < bestDistanceSq ) {
				bestDistanceSq = distanceSq;
				bestNode = node;
			}
		}
	}

	@Override
	protected boolean canImprove(double distanceSq) {
		if( distanceSq <= bestDistanceSq ) {
			return bestNode == null || distanceSq < bestDistanceSq;
		}
		return false;
	}

}
