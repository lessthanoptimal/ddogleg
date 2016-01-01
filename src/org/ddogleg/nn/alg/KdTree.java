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

package org.ddogleg.nn.alg;

/**
 * <p>
 * K-D Tree is short for k-dimensional tree and is a binary tree data structure used for quickly finding the
 * nearest-neighbor of a k-dimensional point in a set.  Each point can optionally have data associated with it.
 * The tree is structured such that at each node has a point and two children.  All points in the set with
 * values {@code <=} to the node's point in the specified dimension/axis are on the the left and {@code >=} to the right.  A leaf
 * will have no children.
 * </p>
 *
 * <p>NOTE: If multiple points have identical values then there will be a node for each point.</p>
 * <p>NOTE: If there is more than one point with an identical value to the node's point, then the identical points
 * can go in either the left or right branches.</p>
 *
 * @author Peter Abeles
 */
public class KdTree {

	// Number of elements/dimension in each point
	int N;
	// tree data structure
	Node root;

	/**
	 * Specifies the type of points it can process.
	 *
	 * @param N Number of elements in a point
	 */
	public KdTree(int N) {
		this.N = N;
	}

	public KdTree() {
	}

	/**
	 * Euclidean distance squared between the node's point and a point.
	 *
	 * @param a Node in the graph
	 * @param point A point
	 * @return Euclidean distance squared.
	 */
	public static double distanceSq( Node a , double[] point , int N) {
		double ret = 0;

		for( int i = 0; i < N; i++ ) {
			double d = a.point[i] - point[i];
			ret += d*d;
		}
		return ret;
	}

	/**
	 * Data type for each node in the binary tree.  A branch will have two non-null left and right children
	 * and the value for split will be {@code >= 0}.  If any of those conditions are not meet then it is a leaf.
	 */
	public static class Node {

		/** The node's point.  For branches this is used to split the data. NOTE: This is a reference to the
		 * original input data.
		 **/
		public double[] point;
		/** Optional associated data to the point */
		public Object data;
		/** axis used to split the data. -1 for leafs */
		public int split = -1;
		/** Branch &le; point[split] */
		public Node left;
		/** Branch &ge; point[split] */
		public Node right;

		public Node( double[] point , Object data ) {
			this.point = point;
			this.data = data;
		}

		public Node() {
		}

		public boolean isLeaf() {
			return split == -1;
		}
	}

}
