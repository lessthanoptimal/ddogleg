package org.ddogleg.nn.alg;

import org.ddogleg.sorting.QuickSelectArray;

import java.util.Random;

/**
 * Randomly selects the larger variances.  The list is sorted so that the K largest variances are known.  It then
 * selects one of those randomly
 *
 * @author Peter Abeles
 */
public class AxisSplitRuleRandomK implements AxisSplitRule {

	// Random number generator
	Random rand;

	// number of elements in a point
	int N;
	// number of elements it will consider when randomly selecting split index
	int numConsiderSplit;

	// stores the original indexes of the 'numConsider' largest elements
	int indexes[];

	/**
	 *
	 * @param rand
	 * @param numConsiderSplit
	 */
	public AxisSplitRuleRandomK(Random rand, int numConsiderSplit) {
		this.rand = rand;
		this.numConsiderSplit = numConsiderSplit;
	}

	@Override
	public void setDimension(int N) {
		this.N = N;
		indexes = new int[N];
	}

	@Override
	public int select(double[] variance) {

		// invert so that the largest variances will be at the bottom
		for( int i = 0; i < N; i++ )
			variance[i] = -variance[i];

		// find the largest ones
		QuickSelectArray.selectIndex(variance, numConsiderSplit,N,indexes);

		// select on of the largests
		return indexes[ rand.nextInt(numConsiderSplit) ];
	}
}
