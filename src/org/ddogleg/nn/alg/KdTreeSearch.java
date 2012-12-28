package org.ddogleg.nn.alg;

/**
 * Interface for searching a single tree for the nearest-neighbor
 *
 * @author Peter Abeles
 */
public interface KdTreeSearch {

	/**
	 * Specifies the tree which is to be searched
	 */
	public void setTree( KdTree tree );

	/**
	 * Specifies the maximum distance a closest-point needs to be to be considered
	 *
	 * @param maxDistance maximum distance from target
	 */
	public void setMaxDistance(double maxDistance );

	/**
	 * Searches for the closest point to target.  If no point is found that is less than maxDistance
	 * then return null.
	 *
	 * @param target Point whose nearest neighbor is being searched for
	 * @return The closest point or null if there is none.
	 */
	public KdTree.Node findClosest( double[] target );
}
