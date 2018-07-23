/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestAxisSplitRuleRandomK {

	@Test
	public void basic() {

		AxisSplitRuleRandomK alg = new AxisSplitRuleRandomK(new Random(234),3);

		// results are random.  Test to see if only the expected numbers are returned
		int num10 = 0;
		int num11 = 0;
		int num12 = 0;

		for( int i = 0; i < 20; i++ ) {
			double[] var = new double[]{1,2,3,10,4,5,5,6,11,12};
			alg.setDimension(var.length);
			int found = alg.select(var);
			switch( found ) {
				case 3:
					num10++;
					break;
				case 8:
					num11++;
					break;

				case 9:
					num12++;
					break;

				default:
					fail("Unexpected value");
			}

		}

		assertTrue(num10 > 2);
		assertTrue(num11 > 2);
		assertTrue(num12 > 2);
	}

	/**
	 * It's told to consider more options than the dimension of the data point.  If this isn't handled an out
	 * of bounds exception will be thrown.
	 */
	@Test
	public void splitExceedsDimension() {
		AxisSplitRuleRandomK alg = new AxisSplitRuleRandomK(new Random(234),10);

		alg.setDimension(3);

		alg.select(new double[3]);
	}

}
