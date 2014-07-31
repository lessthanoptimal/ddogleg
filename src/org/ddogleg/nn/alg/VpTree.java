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

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * <p>
 * Vantage point tree implementation for nearest neighbor search. The implementation is based on the paper [1] and
 * the C++ implementation from Steve Hanov [2]. This implementation avoids recursion when searching to avoid a
 * possible stack overflow for pathological cases.
 * </p>
 *
 * <p>
 * The vp-tree is usually 2-3x slower than a kd-tree for a random set of points but it excels in
 * datasets that the kd-tree is weak in - for example points lying on a circle, line or plane.
 * The vp-tree is up to an order of magnitude faster than a kd-tree for these cases.
 * Use this data structure if you hit a pathological case for a kd-tree.
 * </p>
 *
 * <p>
 * [1] Peter N. Yianilo "Data Structures and Algorithms for Nearest Neighbor Search in General Metric Spaces"<br>
 *     http://aidblab.cse.iitm.ac.in/cs625/vptree.pdf<br>
 * [2] Steve Hanov.  see http://stevehanov.ca/blog/index.php?id=130<br>
 * </p>
 *
 * @author Karel Petránek
 *
 * @param <PointData> Type of user data attached to each point
 */
public class VpTree<PointData> implements NearestNeighbor<PointData> {
	private PointData[] itemData;
	private double[][] items;
	private Node root;
	private Random random;

	/**
	 * Constructor
	 *
	 * @param randSeed Random seed
	 */
   public VpTree( long randSeed ) {
		random = new Random(randSeed);
   }

   @Override
	public void findNearest(double[] target, double maxDistance, int numNeighbors, FastQueue<NnData<PointData>> results) {
		PriorityQueue<HeapItem> heap = search(target, maxDistance < 0 ? Double.POSITIVE_INFINITY : Math.sqrt(maxDistance), numNeighbors);

		while (!heap.isEmpty()) {
			final HeapItem heapItem = heap.poll();
			NnData<PointData> objects = new NnData<PointData>();
			objects.data = itemData[heapItem.index];
			objects.point = items[heapItem.index];
			objects.distance = heapItem.dist * heapItem.dist; // squared distance is expected
			results.add(objects);
		}

		results.reverse();
	}

	/**
	 * Builds the tree from a set of points by recursively partitioning
	 * them according to a random pivot.
	 * @param lower start of range
	 * @param upper end of range (exclusive)
	 * @return root of the tree or null if lower == upper
	 */
	private Node buildFromPoints(int lower, int upper) {
		if (upper == lower) {
			return null;
		}

		final Node node = new Node();
		node.index = lower;

		if (upper - lower > 1) {

			// choose an arbitrary vantage point and move it to the start
			int i = random.nextInt(upper - lower - 1) + lower;
			listSwap(items, lower, i);
			listSwap(itemData, lower, i);

			int median = (upper + lower + 1) / 2;

			// partition around the median distance
			// TODO: use the QuickSelect class?
			nthElement(lower + 1, upper, median, items[lower]);

			// what was the median?
			node.threshold = distance(items[lower], items[median]);

			node.index = lower;
			node.left = buildFromPoints(lower + 1, median);
			node.right = buildFromPoints(median, upper);
		}

		return node;
	}

	/**
	 * Ensures that the n-th element is in a correct position in the list based on
	 * the distance from origin.
	 * @param left start of range
	 * @param right end of range (exclusive)
	 * @param n element to put in the right position
	 * @param origin origin to compute the distance to
	 */
	private void nthElement(int left, int right, int n, double[] origin) {
		int npos = partitionItems(left, right, n, origin);
		if (npos < n)
			nthElement(npos + 1, right, n, origin);
		if (npos > n)
			nthElement(left, npos, n, origin);
	}

	/**
	 * Partition the points based on their distance to origin around the selected pivot.
	 * @param left range start
	 * @param right range end (exclusive)
	 * @param pivot pivot for the partition
	 * @param origin origin to compute the distance to
	 * @return index of the pivot
	 */
	private int partitionItems(int left, int right, int pivot, double[] origin) {
		double pivotDistance = distance(origin, items[pivot]);
		listSwap(items, pivot, right - 1);
		listSwap(itemData, pivot, right - 1);
		int storeIndex = left;
		for (int i = left; i < right - 1; i++) {
			if (distance(origin, items[i]) <= pivotDistance) {
				listSwap(items, i, storeIndex);
				listSwap(itemData, i, storeIndex);
				storeIndex++;
			}
		}
		listSwap(items, storeIndex, right - 1);
		listSwap(itemData, storeIndex, right - 1);
		return storeIndex;
	}

	/**
	 * Swaps two items in the given list.
	 * @param list list to swap the items in
	 * @param a index of the first item
	 * @param b index of the second item
	 * @param <E> list type
	 */
	private <E> void listSwap(E[] list, int a, int b) {
		final E tmp = list[a];
		list[a] = list[b];
		list[b] = tmp;
	}

	/**
	 * Compute the Euclidean distance between p1 and p2.
	 * @param p1 first point
	 * @param p2 second point
	 * @return Euclidean distance
	 */
	private static double distance(double[] p1, double[] p2) {
		switch (p1.length) {
			case 2: return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
			case 3: return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]) + (p1[2] - p2[2]) * (p1[2] - p2[2]));
			default: {
				double dist = 0;
				for (int i = p1.length - 1; i >= 0; i--) {
					final double d = (p1[i] - p2[i]);
					dist += d * d;
				}
				return Math.sqrt(dist);
			}
		}
	}

	/**
	 * Recursively search for the k nearest neighbors to target.
	 * @param target target point
	 * @param maxDistance maximum distance
	 * @param k number of neighbors to find
	 */
	private PriorityQueue<HeapItem> search(final double[] target, double maxDistance, final int k) {
		PriorityQueue<HeapItem> heap = new PriorityQueue<HeapItem>();
		if (root == null) {
			return heap;
		}

		double tau = maxDistance;
		final FastQueue<Node> nodes = new FastQueue<Node>(20, Node.class, false);
		nodes.add(root);

		while (nodes.size() > 0) {
			final Node node = nodes.removeTail();
			final double dist = distance(items[node.index], target);

			if (dist <= tau) {
				if (heap.size() == k) {
					heap.poll();
				}
				heap.add(new HeapItem(node.index, dist));
				if (heap.size() == k) {
					tau = heap.element().dist;
				}
			}

			if (node.left != null && dist - tau <= node.threshold) {
				nodes.add(node.left);
			}

			if (node.right != null && dist + tau >= node.threshold) {
				nodes.add(node.right);
			}
		}

		return heap;
	}

	/**
	 * Equivalent to the above search method to find one nearest neighbor.
	 * It is faster as it does not need to allocate and use the heap data structure.
	 * @param target target point
	 * @param maxDistance maximum distance
	 * @param result information about the nearest point (output parameter)
	 * @return true if a nearest point was found within maxDistance
	 */
	private boolean searchNearest(final double[] target, double maxDistance, NnData<PointData> result) {
		if (root == null) {
			return false;
		}

		double tau = maxDistance;
		final FastQueue<Node> nodes = new FastQueue<Node>(20, Node.class, false);
		nodes.add(root);
		result.distance = Double.POSITIVE_INFINITY;
		boolean found = false;

		while (nodes.size() > 0) {
			final Node node = nodes.getTail();
			nodes.removeTail();
			final double dist = distance(items[node.index], target);

			if (dist <= tau && dist < result.distance) {
				result.distance = dist;
				result.data = itemData[node.index];
				result.point = items[node.index];
				tau = dist;
				found = true;
			}

			if (node.left != null && dist - tau <= node.threshold) {
				nodes.add(node.left);
			}

			if (node.right != null && dist + tau >= node.threshold) {
				nodes.add(node.right);
			}
		}

		return found;
	}

	@Override
	public void init(int pointDimension) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPoints(List<double[]> points, List<PointData> data) {
		// Make a copy because we mutate the lists
		this.items = points.toArray(new double[0][]);
		this.itemData = data == null ? (PointData[])new Object[points.size()] : (PointData[])data.toArray();  // todo remove pointless list if null?
		this.root = buildFromPoints(0, items.length);
	}

	@Override
	public boolean findNearest(double[] point, double maxDistance, NnData<PointData> result) {
		boolean r = searchNearest(point, maxDistance < 0 ? Double.POSITIVE_INFINITY : Math.sqrt(maxDistance), result);
		result.distance *= result.distance; // Callee expects squared distance
		return r;
	}

	/**
	 * Separates the points to "closer than the threshold" (left) and "farther than the threshold" (right).
	 */
	private static class Node {
		int index;
		double threshold;
		Node left;
		Node right;
	}

	/**
	 * Holds possible candidates for nearest neighbors during the search.
	 */
	private static class HeapItem implements Comparable<HeapItem> {
		int index;
		double dist;

		HeapItem(int index, double dist) {
			this.index = index;
			this.dist = dist;
		}

		@Override
		public int compareTo(HeapItem o) {
			return (int) Math.signum(o.dist - dist);
		}
	}
}