package org.ddogleg.nn.alg;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class VpTree<PointData> implements NearestNeighbor<PointData> {
	private List<PointData> itemData;
	private List<double[]> items;
	private double tau;
	private Node root;
	private Random random = new Random();

	@Override
	public void findNearest(double[] target, double maxDistance, int numNeighbors, FastQueue<NnData<PointData>> results) {
		PriorityQueue<HeapItem> heap = new PriorityQueue<HeapItem>();

		tau = maxDistance < 0 ? Double.POSITIVE_INFINITY : Math.sqrt(maxDistance);
		search(root, target, numNeighbors, heap);

		while (!heap.isEmpty()) {
			final HeapItem heapItem = heap.poll();
			NnData<PointData> objects = new NnData<PointData>();
			objects.data = itemData == null ? null : itemData.get(heapItem.index);
			objects.point = items.get(heapItem.index);
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

		Node node = new Node();
		node.index = lower;

		if (upper - lower > 1) {

			// choose an arbitrary point and move it to the start
			int i = random.nextInt(upper - lower - 1) + lower;
			listSwap(items, lower, i);
			listSwap(itemData, lower, i);

			int median = (upper + lower + 1) / 2;

			// partition around the median distance
			// TODO: use the QuickSelect class?
			nthElement(lower + 1, upper, median, items.get(lower));

			// what was the median?
			node.threshold = distance(items.get(lower), items.get(median));

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
		double pivotDistance = distance(origin, items.get(pivot));
		listSwap(items, pivot, right - 1);
		listSwap(itemData, pivot, right - 1);
		int storeIndex = left;
		for (int i = left; i < right - 1; i++) {
			if (distance(origin, items.get(i)) <= pivotDistance) {
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
	private <E> void listSwap(List<E> list, int a, int b) {
		if (list == null)
			return;

		E tmp = list.get(a);
		list.set(a, list.get(b));
		list.set(b, tmp);
	}

	/**
	 * Compute the Euclidean distance between p1 and p2.
	 * @param p1 first point
	 * @param p2 second point
	 * @return Euclidean distance
	 */
	private double distance(double[] p1, double[] p2) {
		double d = 0;
		for (int i = 0; i < p1.length; i++) {
			d += (p1[i] - p2[i]) * (p1[i] - p2[i]);
		}
		return Math.sqrt(d);
	}

	/**
	 * Recursively search for the k nearest neighbors to target.
	 * @param node tree to examine
	 * @param target target point
	 * @param k number of neighbors to find
	 * @param heap storage for the k nearest neighbors
	 */
	private void search(Node node, double[] target, int k, PriorityQueue<HeapItem> heap) {
		if (node == null)
			return;

		double dist = distance(items.get(node.index), target);

		if (dist <= tau) {
			if (heap.size() == k)
				heap.poll();
			heap.add(new HeapItem(node.index, dist));
			if (heap.size() == k)
				tau = heap.element().dist;
		}

		if (node.left == null && node.right == null) {
			return;
		}

		if (dist < node.threshold) {
			if (dist - tau <= node.threshold) {
				search(node.left, target, k, heap);
			}

			if (dist + tau >= node.threshold) {
				search(node.right, target, k, heap);
			}

		} else {
			if (dist + tau >= node.threshold) {
				search(node.right, target, k, heap);
			}

			if (dist - tau <= node.threshold) {
				search(node.left, target, k, heap);
			}
		}
	}

	@Override
	public void init(int pointDimension) {

	}

	@Override
	public void setPoints(List<double[]> points, List<PointData> data) {
		// Make a copy because we mutate the lists
		this.items = new ArrayList<double[]>(points);
		this.itemData = data == null ? null : new ArrayList<PointData>(data);
		this.root = buildFromPoints(0, items.size());
	}

	@Override
	public boolean findNearest(double[] point, double maxDistance, NnData<PointData> result) {
		FastQueue<NnData<PointData>> q = new FastQueue<NnData<PointData>>((Class) NnData.class, true);
		findNearest(point, maxDistance, 1, q);
		if (q.size() == 0)
			return false;
		result.data = q.get(0).data;
		result.distance = q.get(0).distance;
		result.point = q.get(0).point;
		return true;
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