package org.ddogleg.nn.alg;

/**
 * Selects which axis the data should be split along when given a list of variances.
 *
 * @author Peter Abeles
 */
public interface AxisSplitRule {

	public void setDimension( int N );

	public int select( double []variance );
}
