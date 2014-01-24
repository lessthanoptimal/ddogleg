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

import org.ddogleg.struct.FastQueue;

/**
 * Standard algorithm for searching a {@link org.ddogleg.nn.alg.KdTree} for the nearest-neighbor of a search.
 * This is an adaptation of {@link KdTreeSearch1Standard} for N-nearest-neighbors.
 *
 * @author Peter Abeles
 */
public class KdTreeSearchNStandard implements KdTreeSearchN {

	// the targeted tree
	private KdTree tree;

	// point being searched for
	private double[] target;

	// the maximum distance a neighbor is allowed to be
	private double maxDistanceSq = Double.MAX_VALUE;
	// distance of the farthest neighbor
	private double mostDistantNeighborSq;
	// index of most distant neighbor
	private int mostDistantNeighborIndex;

	// then number of nearest-neighbors it's searching for
	private int searchN;

	@Override
	public void setTree( KdTree tree ) {
		this.tree = tree;
	}

	/**
	 * Specifies the greatest distance it will search
	 *
	 * @param maxDistance Maximum distance (Euclidean squared) a closest point can be
	 */
	@Override
	public void setMaxDistance(double maxDistance ) {
		this.maxDistanceSq = maxDistance;
	}

	/**
	 * Finds the nodes which are closest to 'target' and within range of the maximum distance.
	 *
	 * @param target A point
	 * @param searchN Number of nearest-neighbors it will search for
	 * @param results Storage for the found neighbors
	 */
	@Override
	public void findNeighbor(double[] target, int searchN, FastQueue<KdTreeResult> results) {
		if( searchN <= 0 )
			throw new IllegalArgumentException("I'm sorry, but I refuse to search for less than or equal to 0 neighbors.");

		if( tree.root == null )
			return;

		this.searchN = searchN;
		this.target = target;
		this.mostDistantNeighborSq = maxDistanceSq;

		stepClosest(tree.root,results);
	}

	/**
	 * Recursive step for finding the closest point
	 */
	private void stepClosest(KdTree.Node node , FastQueue<KdTreeResult> neighbors ) {

		if( node == null )
			return;

		checkBestDistance(node, neighbors);

		if( node.isLeaf() ) {
			return;
		}

		// select the most promising branch to investigate first
		KdTree.Node nearer,further;

		double splitValue = node.point[ node.split ];

		if( target[node.split ] <= splitValue ) {
			nearer = node.left;
			further = node.right;
		} else {
			nearer = node.right;
			further = node.left;
		}

		stepClosest(nearer,neighbors);

		// See if it is possible for 'further' to contain a better node
		// Or if N matches have yet to be find, if it is possible to meet the maximum distance requirement
		double dx = splitValue - target[ node.split ];
		if( dx*dx <= mostDistantNeighborSq) {
			if( neighbors.size() < searchN || dx*dx < mostDistantNeighborSq) {
				stepClosest(further,neighbors);
			}
		}
	}

	/**
	 * See if the node being considered is a new nearest-neighbor
	 */
	private void checkBestDistance(KdTree.Node node, FastQueue<KdTreeResult> neighbors) {
		double distSq = KdTree.distanceSq(node,target,tree.N);
		// <= because multiple nodes could be at the bestDistanceSq
		if( distSq <= mostDistantNeighborSq) {
			if( neighbors.size() < searchN ) {
				// the list of nearest neighbors isn't full yet so it doesn't know what the distance will be
				// so just keep on adding them to the list until it is full
				KdTreeResult r = neighbors.grow();
				r.distance = distSq;
				r.node = node;
				if( neighbors.size() == searchN ) {
					// find the most distant
					mostDistantNeighborSq = -1;
					for( int i = 0; i < searchN; i++ ) {
						r = neighbors.get(i);
						if( r.distance > mostDistantNeighborSq ) {
							mostDistantNeighborSq = r.distance;
							mostDistantNeighborIndex = i;
						}
					}
				}
			} else {
				for( int i = 0; i < searchN; i++ ) {
					KdTreeResult r = neighbors.get(i);
					if( r.distance > mostDistantNeighborSq ) {
						throw new RuntimeException("Most distant isn't the most distant");
					}
				}

				// Write over the most distant neighbor since we known this node must be closer
				// and update the maximum distance
				KdTreeResult r = neighbors.get(mostDistantNeighborIndex);
				r.node = node;
				r.distance = distSq;

				// If there are multiple points then there can be more than one point with the value of
				// 'bestDistanceSq', which is why two searches are required
				mostDistantNeighborSq = -1;
				for( int i = 0; i < searchN; i++ ) {
					r = neighbors.get(i);
					if( r.distance > mostDistantNeighborSq ) {
						mostDistantNeighborSq = r.distance;
						mostDistantNeighborIndex = i;
					}
				}
			}
		}
	}

}
