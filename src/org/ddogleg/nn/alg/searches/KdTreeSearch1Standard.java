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

package org.ddogleg.nn.alg.searches;

import org.ddogleg.nn.alg.KdTree;
import org.ddogleg.nn.alg.KdTreeDistance;
import org.ddogleg.nn.alg.KdTreeSearch1;

/**
 * Standard algorithm for searching a {@link KdTree} for the nearest-neighbor of a search.  This is the algorithm
 * which is typically described in books.  At each node it examines distance of the two children and investigates
 * the closer child.  After it reaches a leaf it steps back in the search and sees if the other child could produce
 * a better solution, if it can it is also investigated.  The search stops when no more nodes can produce a better
 * result.
 *
 * @author Peter Abeles
 */
public class KdTreeSearch1Standard<P> implements KdTreeSearch1<P> {

	// the targeted tree
	private KdTree tree;

	// point being searched for
	private P target;

	// the maximum distance a neighbor is allowed to be
	private double maxDistanceSq = Double.MAX_VALUE;
	// the closest neighbor which has yet to be found
	private double bestDistanceSq;

	// the node which has been found to be the closest so far
	private KdTree.Node closest;

	KdTreeDistance<P> distance;

	public KdTreeSearch1Standard(KdTreeDistance<P> distance) {
		this.distance = distance;
	}

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
		this.maxDistanceSq = maxDistance ;
	}

	/**
	 * Finds the node which is closest to 'target'
	 *
	 * @param target A point
	 * @return Closest node or null if none is within the minimum distance.
	 */
	@Override
	public KdTree.Node findNeighbor(P target) {
		if( tree.root == null )
			return null;

		this.target = target;
		this.closest = null;
		this.bestDistanceSq = maxDistanceSq;

		stepClosest(tree.root);

		return closest;
	}

	@Override
	public double getDistance() {
		return bestDistanceSq;
	}

	/**
	 * Recursive step for finding the closest point
	 */
	private void stepClosest(KdTree.Node node) {

		if( node == null )
			return;

		if( node.isLeaf() ) {
			// a leaf can be empty.
			if( node.point != null ) {
				double distSq = distance.distance((P)node.point,target);
				if( distSq <= bestDistanceSq ) {
					if( closest == null || distSq < bestDistanceSq ) {
						closest = node;
						bestDistanceSq = distSq;
					}
				}
			}
			return;
		} else {
			double distSq = distance.distance((P)node.point,target);
			if( distSq <= bestDistanceSq ) {
				if( closest == null || distSq < bestDistanceSq ) {
					closest = node;
					bestDistanceSq = distSq;
				}
			}
		}

		// select the most promising branch to investigate first
		KdTree.Node nearer,further;

		double splitValue = distance.valueAt((P)node.point,node.split);

		double targetAtSplit = distance.valueAt(target,node.split);
		if( targetAtSplit	 <= splitValue ) {
			nearer = node.left;
			further = node.right;
		} else {
			nearer = node.right;
			further = node.left;
		}

		stepClosest(nearer);

		// See if it is possible for 'further' to contain a better node
		double dx = splitValue - targetAtSplit;
		double dx2 = dx*dx;
		if( dx2 <= bestDistanceSq ) {
			if( closest == null || dx2 < bestDistanceSq )
				stepClosest(further);
		}
	}

}
