package org.ddogleg.nn.alg;

/**
 * Various operations for manipulating and searching {@link KdTree}.
 *
 * @author Peter Abeles
 */
// TODO Add insert
// TODO Add N-nearest
public class KdTreeOperations {

	// the targeted tree
	KdTree tree;

	// point being searched for
	double[] target;

	// the maximum distance a neighbor is allowed to be
	double maxDistanceSq = Double.MAX_VALUE;
	// the closest neighbor which has yet to be found
	double bestDistanceSq;

	KdTree.Node closest;

	public void setTree( KdTree tree ) {
		this.tree = tree;
	}

	/**
	 * Specifies the greatest distance it will search
	 *
	 * @param maxDistance Maximum distance a closest point can be
	 */
	public void setMaxDistance(double maxDistance ) {
		this.maxDistanceSq = maxDistance*maxDistance ;
	}

	/**
	 * Finds the node which is closest to 'target'
	 *
	 * @param target A point
	 * @return Closest node or null if none is within the minimum distance.
	 */
	public KdTree.Node findClosest( double[] target ) {
		if( tree.root == null )
			return null;

		this.target = target;
		this.closest = null;
		this.bestDistanceSq = maxDistanceSq;

		stepClosest(tree.root);

		return closest;
	}

	/**
	 * Recursive step for finding the closest point
	 */
	private void stepClosest(KdTree.Node node) {

		double d = tree.distanceSq(node,target);
		if( d*d < bestDistanceSq ) {
			closest = node;
			bestDistanceSq = d*d;
		}

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

		stepClosest(nearer);

		// See if it is possible for 'further' to contain a better node
		double dx = splitValue - target[ node.split ];
		if( dx*dx < bestDistanceSq ) {
			stepClosest(further);
		}
	}

}
