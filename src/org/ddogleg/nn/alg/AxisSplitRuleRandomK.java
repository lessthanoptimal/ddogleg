/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.nn.alg;

import org.ddogleg.sorting.QuickSelect;

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
	// number it will consider, but limited by the DOF
	int actualConsiderSplit;

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
		actualConsiderSplit = Math.min(numConsiderSplit,N);
	}

	@Override
	public int select(double[] variance) {

		// invert so that the largest variances will be at the bottom
		for( int i = 0; i < N; i++ )
			variance[i] = -variance[i];

		// find the largest ones
		QuickSelect.selectIndex(variance, actualConsiderSplit-1,N,indexes);

		// select on of the largests
		return indexes[ rand.nextInt(actualConsiderSplit) ];
	}
}
