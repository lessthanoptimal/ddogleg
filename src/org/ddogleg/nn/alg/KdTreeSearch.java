package org.ddogleg.nn.alg;

/**
 * TODO comment
 *
 * @author Peter Abeles
 */
public interface KdTreeSearch {

	public void setTree( KdTree tree );

	public void setMaxDistance(double maxDistance );

	public KdTree.Node findClosest( double[] target );
}
