package org.ddogleg.nn.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * <p>
 * Approximate search for {@link KdTree K-D Trees} using the best-bin-first method [1] that supports multiple trees.
 * A priority queue is created where nodes that are more likely to contain points close to the target are given
 * higher priority. It is approximate since only predetermined number of nodes are considered.
 * </p>
 *
 * <p>
 * Searches are initialized by performing of each tree at least once down to a leaf.  As these searches are
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
// TODO finish commenting
public class KdTreeSearchBbf implements KdTreeSearch {

	// the maximum number of nodes it will search
	int maxNodesSearched;

	// dimension of point
	int N;

	// The maximum distance a point is allowed to be from the target
	double maxDistance = Double.MAX_VALUE;

	// List of graph nodes that still need to be explored
	PriorityQueue<Helper> queue = new PriorityQueue<Helper>();

	// Forest of trees to search
	KdTree trees[];

	// The number of nodes that have been processed
	int numNodesSearched;
	// distance of the best node squared
	double bestDistanceSq;
	// the best node so far
	KdTree.Node bestNode;

	// used for recycling data structures
	List<Helper> unused = new ArrayList<Helper>();

	public KdTreeSearchBbf(int maxNodesSearched) {
		this.maxNodesSearched = maxNodesSearched;
	}

	@Override
	public void setTree(KdTree tree) {
		this.trees = new KdTree[]{tree};
		this.N = tree.N;
	}

	public void setTrees(KdTree []trees ) {
		this.trees = trees;
		this.N = trees[0].N;
	}

	@Override
	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	@Override
	public KdTree.Node findClosest(double[] target) {

		numNodesSearched = 0;
		bestDistanceSq = maxDistance*maxDistance;
		bestNode = null;

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
			if( h.closestPossibleSq >= bestDistanceSq )
				continue;

			searchNode(target,n);
		}
//		System.out.println("numNodesSearched "+numNodesSearched+"  max = "+maxNodes+" "+"  queue "+queue.size());

		// recycle data
		unused.addAll(queue);
		queue.clear();

		return bestNode;
	}

	/**
	 * Traverse a node down to a leaf.  Unexplored branches are added to the priority queue.
	 */
	protected void searchNode(double[] target, KdTree.Node n) {
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

	/**
	 * Adds a node to the priority queue.
	 *
	 * @param closestDistanceSq The closest distance that a point in the region could possibly be target
	 */
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

	/**
	 * Checks to see if the current node's point is the closet point found so far
	 */
	private void checkBestDistance(KdTree.Node node, double[] target) {
		double distanceSq = KdTree.distanceSq(node,target,N);
		if( distanceSq < bestDistanceSq ) {
			bestDistanceSq = distanceSq;
			bestNode = node;
		}
	}

	/**
	 * Recycles data for future use
	 */
	private void recycle( Helper h ) {
		unused.add(h);
	}

	/**
	 * Contains information on a node
	 */
	private static class Helper implements Comparable<Helper> {

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
