package org.ddogleg.nn.alg;

/**
 * Selects which axis the data should be split along when given a list of variances.
 *
 * @author Peter Abeles
 */
public interface AxisSplitRule {

	/**
	 * Specifies the point's dimension
	 *
	 * @param N dimension
	 */
	public void setDimension( int N );

	/**
	 * Selects the index for splitting using the provided variances.  The input list can be modified.
	 * @param variance List of variances for each dimension in the point
	 * @return The selected split axis
	 */
	public int select( double []variance );
}
