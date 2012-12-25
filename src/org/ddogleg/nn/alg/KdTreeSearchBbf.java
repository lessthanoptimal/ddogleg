package org.ddogleg.nn.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * <p>
 * Approximate search for {@link KdTree K-D Trees} using the best-bin-first method [1].  A priority queue is created
 * where nodes which have more likely to contain a good points are given higher priority. It is approximate since only
 * a specified number of nodes are considered.
 * </p>
 *
 * <p>
 * [1] Beis, Jeffrey S. and Lowe, David G, "Shape Indexing Using Approximate Nearest-Neighbour Search in
 * High-Dimensional Spaces" CVPR 1997
 * </p>
 *
 * @author Peter Abeles
 */
public class KdTreeSearchBbf implements KdTreeSearch {

	// the maximum number of nodes it will search
	int maxNodes;

	double maxDistance = Double.MAX_VALUE;

	PriorityQueue<Helper> queue = new PriorityQueue<Helper>();

	KdTree tree;

	int numNodesSearched;
	double bestDistanceSq;
	KdTree.Node bestNode;

	List<Helper> unused = new ArrayList<Helper>();

	public KdTreeSearchBbf(int maxNodes) {
		this.maxNodes = maxNodes;
	}

	@Override
	public void setTree(KdTree tree) {
		this.tree = tree;
	}

	@Override
	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	@Override
	public KdTree.Node findClosest(double[] target) {

		if( tree.root == null )
			return null;

		numNodesSearched = 0;
		bestDistanceSq = maxDistance*maxDistance;
		bestNode = null;

		// start the search from the root node
		addToQueue(0,tree.root,target);

		// TODO Change so that it searches down to a leaf
		// TODO queue based on dx*dx

		// iterate until it exhausts all options or the maximum number of nodes has been exceeded
		while( !queue.isEmpty() && numNodesSearched++ < maxNodes ) {
			Helper h = queue.remove();
			KdTree.Node n = h.node;
			recycle(h);

			// use new information to prune nodes
			if( h.closestPossibleSq >= bestDistanceSq )
				continue;

			while( true) {
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
				if( dx*dx < bestDistanceSq ) {
					addToQueue(dx*dx, further, target );
				}

				n = nearer;
			}
		}
//		System.out.println("numNodesSearched "+numNodesSearched+"  max = "+maxNodes+" "+"  queue "+queue.size());

		// recycle data
		unused.addAll(queue);
		queue.clear();

		return bestNode;
	}

	private void addToQueue( double closestDistanceSq , KdTree.Node node , double []target ) {

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

	private void checkBestDistance(KdTree.Node node, double[] target) {
		double distanceSq = tree.distanceSq(node,target);
		if( distanceSq < bestDistanceSq ) {
			bestDistanceSq = distanceSq;
			bestNode = node;
		}
	}

	private void recycle( Helper h ) {
		unused.add(h);
	}

	/**
	 * Contains information on a node
	 */
	private static class Helper implements Comparable<Helper> {

		double closestPossibleSq;
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
