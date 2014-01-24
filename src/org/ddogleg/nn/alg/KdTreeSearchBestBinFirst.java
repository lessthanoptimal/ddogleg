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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * <p>
 * Approximate search for {@link org.ddogleg.nn.alg.KdTree K-D Trees} using the best-bin-first method [1] that supports
 * multiple trees. A priority queue is created where nodes that are more likely to contain points close to the target are given
 * higher priority. It is approximate since only predetermined number of nodes are considered.
 * </p>
 *
 * <p>
 * Searches are initialized by searching each tree at least once down to a leaf.  As these searches are
 * performed, unexplored regions are added to the priority queue. Searching multiple trees is in response to [2],
 * which proposes using a set of random trees to improve search performance and take better advantage of structure
 * found in the data.
 * </p>
 *
 * <p>
 * [1] Beis, Jeffrey S. and Lowe, David G, "Shape Indexing Using Approximate Nearest-Neighbour Search in
 * High-Dimensional Spaces" CVPR 1997<br>
 * [2] Silpa-Anan, C. and Hartley, R. "Optimised KD-trees for fast image descriptor matching" CVPR 2008
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class KdTreeSearchBestBinFirst {

	// the maximum number of nodes it will search
	private int maxNodesSearched;

	// dimension of point
	protected int N;

	// The maximum distance a point is allowed to be from the target
	private double maxDistance = Double.MAX_VALUE;

	// List of graph nodes that still need to be explored
	private PriorityQueue<Helper> queue = new PriorityQueue<Helper>();

	// Forest of trees to search
	private KdTree trees[];

	// distance of the best node squared
	protected double bestDistanceSq;

	// used for recycling data structures
	private List<Helper> unused = new ArrayList<Helper>();

	// number of nodes which have been searched
	protected int numNodesSearched = 0;

	/**
	 * Configures the search
	 *
	 * @param maxNodesSearched Maximum number of nodes it will search.  Used to limit CPU time.
	 */
	public KdTreeSearchBestBinFirst(int maxNodesSearched) {
		this.maxNodesSearched = maxNodesSearched;
	}

	public void setTree(KdTree tree) {
		this.trees = new KdTree[]{tree};
		this.N = tree.N;
	}

	public void setTrees(KdTree []trees ) {
		this.trees = trees;
		this.N = trees[0].N;
	}

	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public void _findClosest(double[] target) {

		numNodesSearched = 0;
		bestDistanceSq = maxDistance;

		// start the search from the root node
		for( int i = 0; i < trees.length; i++ ) {
			KdTree.Node root = trees[i].root;
			if( root != null )
				searchNode(target,root);
		}

		// iterate until it exhausts all options or the maximum number of nodes has been exceeded
		while( !queue.isEmpty() && numNodesSearched++ < maxNodesSearched) {
			Helper h = queue.remove();
			KdTree.Node n = h.node;
			recycle(h);

			// use new information to prune nodes
			if( !canImprove(h.closestPossibleSq) )
				continue;

			searchNode(target,n);
		}
//		System.out.println("numNodesSearched "+numNodesSearched+"  max = "+maxNodes+" "+"  queue "+queue.size());

		// recycle data
		unused.addAll(queue);
		queue.clear();
	}

	/**
	 * Traverse a node down to a leaf.  Unexplored branches are added to the priority queue.
	 */
	protected void searchNode(double[] target, KdTree.Node n) {
		while( n != null) {
			checkBestDistance(n, target);

			if( n.isLeaf() )
				break;

			// select the most promising branch to investigate first
			KdTree.Node nearer,further;

			double splitValue = n.point[ n.split ];

			if( target[n.split ] <= splitValue ) {
				nearer = n.left;
				further = n.right;
			} else {
				nearer = n.right;
				further = n.left;
			}

			// See if it is possible for 'further' to contain a better node
			double dx = splitValue - target[ n.split ];
			if( further != null && canImprove(dx*dx) ) {
				addToQueue(dx*dx, further, target );
			}

			n = nearer;
		}
	}

	/**
	 * Adds a node to the priority queue.
	 *
	 * @param closestDistanceSq The closest distance that a point in the region could possibly be target
	 */
	protected void addToQueue( double closestDistanceSq , KdTree.Node node , double []target ) {

		if( !node.isLeaf() ) {
			Helper h;
			if( unused.isEmpty() ) {
				h = new Helper();
			} else {
				h = unused.remove( unused.size()-1 );
			}

			h.closestPossibleSq = closestDistanceSq;
			h.node = node;

			queue.add(h);
		} else {
			checkBestDistance(node, target);
		}
	}

	/**
	 * Checks to see if the current node's point is the closet point found so far
	 */
	protected abstract void checkBestDistance(KdTree.Node node, double[] target);

	/**
	 * Checks to see if it is possible for this distance to improve upon the current best
	 * @param distanceSq The distance being considered
	 * @return true if it can be better or false if not
	 */
	protected abstract boolean canImprove( double distanceSq );

	/**
	 * Recycles data for future use
	 */
	private void recycle( Helper h ) {
		unused.add(h);
	}

	/**
	 * Contains information on a node
	 */
	protected static class Helper implements Comparable<Helper> {

		// the closest the region can be to the target
		double closestPossibleSq;
		// node in the graph
		KdTree.Node node;

		@Override
		public int compareTo(Helper o) {
			if( closestPossibleSq < o.closestPossibleSq)
				return -1;
			else if( closestPossibleSq > o.closestPossibleSq)
				return 1;
			else
				return 0;
		}
	}
}
