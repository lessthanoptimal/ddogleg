package org.ddogleg.nn;

/**
 * Results from a Nearest-Neighbor search.
 *
 * @author Peter Abeles
 */
public class NnData<D> {
	// tuple data
	public double[] point;
	// data associated with point
	public D data;
}
