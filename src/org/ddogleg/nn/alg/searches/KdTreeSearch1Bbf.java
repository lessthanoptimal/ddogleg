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

package org.ddogleg.nn.alg.searches;

import org.ddogleg.nn.alg.KdTree;
import org.ddogleg.nn.alg.KdTreeDistance;
import org.ddogleg.nn.alg.KdTreeSearch1;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Implementation of {@link KdTreeSearchBestBinFirst} which searches for the single best nearest-neighbor.
 * </p>
 *
 * @author Peter Abeles
 */
public class KdTreeSearch1Bbf<P> extends KdTreeSearchBestBinFirst<P> implements KdTreeSearch1<P> {

	// the best node so far
	private @Nullable KdTree.Node bestNode;

	/**
	 * Configures the search
	 *
	 * @param maxNodesSearched Maximum number of nodes it will search.  Used to limit CPU time.
	 */
	public KdTreeSearch1Bbf(KdTreeDistance<P> distance, int maxNodesSearched) {
		super(distance,maxNodesSearched);
	}

	@Override
	public void setTree(Object tree) {
		if( tree instanceof KdTree ) {
			setTree((KdTree)tree);
		} else {
			setTrees((KdTree[])tree);
		}
	}

	@Override
	public @Nullable KdTree.Node findNeighbor(P target) {

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
	protected void checkBestDistance(KdTree.Node node, P target) {
		double distanceSq = distance.distance((P)node.point,target);
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

	@Override
	public KdTreeSearch1<P> copy() {
		return new KdTreeSearch1Bbf<>(distance,maxNodesSearched);
	}
}
