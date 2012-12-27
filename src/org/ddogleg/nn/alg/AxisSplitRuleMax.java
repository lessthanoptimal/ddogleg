package org.ddogleg.nn.alg;

/**
 * Selects the axis with the largest variance to split.
 *
 * @author Peter Abeles
 */
public class AxisSplitRuleMax implements AxisSplitRule {

	int N;

	@Override
	public void setDimension(int N) {
		this.N = N;
	}

	@Override
	public int select(double[] var) {
		int split = -1;
		double bestVar = 0;
		for( int i = 0; i < N; i++ ) {
			if( var[i] > bestVar ) {
				split = i;
				bestVar = var[i];
			}
		}

		return split;
	}
}
