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
import org.ddogleg.nn.alg.KdTreeResult;
import org.ddogleg.nn.alg.KdTreeSearchN;
import org.ddogleg.struct.DogArray;

/**
 * <p>
 * Implementation of {@link KdTreeSearchBestBinFirst} which searches for the N nearest-neighbors.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class KdTreeSearchNBbf<P> extends KdTreeSearchBestBinFirst<P> implements KdTreeSearchN<P> {


	// number of nearest-neighbors it will find
	private int searchN;

	// storage for found results nodes
	private DogArray<KdTreeResult> neighbors;

	/**
	 * Configures the search
	 *
	 * @param maxNodesSearched Maximum number of nodes it will search.  Used to limit CPU time.
	 */
	public KdTreeSearchNBbf(KdTreeDistance<P> distance, int maxNodesSearched) {
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
	public void findNeighbor(P target, int searchN, DogArray<KdTreeResult> results) {

		this.searchN = searchN;
		this.neighbors = results;

		_findClosest(target);
	}

	@Override
	public KdTreeSearchN<P> copy() {
		return new KdTreeSearchNBbf<>(distance,maxNodesSearched);
	}

	/**
	 * Checks to see if the current node's point is the closet point found so far
	 */
	@Override
	protected void checkBestDistance(KdTree.Node node, P target) {

		double distanceSq = distance.distance((P)node.point,target);
		// <= because multiple nodes could be at the bestDistanceSq
		if( distanceSq <= bestDistanceSq ) {

			// see if the node is already in the list.  This is possible because there can be multiple trees
			for( int i = 0; i < neighbors.size(); i++ ) {
				KdTreeResult r = neighbors.get(i);
				if( r.node.point == node.point )
					return;
			}

			if( neighbors.size() < searchN ) {
				// the list of nearest neighbors isn't full yet so it doesn't know what the distance will be
				// so just keep on adding them to the list until it is full
				KdTreeResult r = neighbors.grow();
				r.distance = distanceSq;
				r.node = node;
				if( neighbors.size() == searchN ) {

					// find the most distant node
					bestDistanceSq = 0;
					for( int i = 0; i < searchN; i++ ) {
						r = neighbors.get(i);

						if( r.distance > bestDistanceSq ) {
							bestDistanceSq = r.distance;
						}
					}
				}
			} else {
				// find the most distant neighbor and write over it since we known this node must be closer
				// and update the maximum distance
				for( int i = 0; i < searchN; i++ ) {
					KdTreeResult r = neighbors.get(i);
					if( r.distance == bestDistanceSq ) {
						r.node = node;
						r.distance = distanceSq;
						break;
					}
				}

				// If there are multiple points then there can be more than one point with the value of
				// 'bestDistanceSq', which is why two searches are required
				bestDistanceSq = 0;
				for( int i = 0; i < searchN; i++ ) {
					KdTreeResult r = neighbors.get(i);
					if( r.distance > bestDistanceSq ) {
						bestDistanceSq = r.distance;
					}
				}
			}
		}
	}

	@Override
	protected boolean canImprove(double distanceSq) {
		if( distanceSq <= bestDistanceSq ) {
			return neighbors.size() < searchN || distanceSq < bestDistanceSq;
		}
		return false;
	}
}
