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

package org.ddogleg.stats;

import org.ddogleg.struct.GrowQueue_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestUtilStatisticsQueue {
	@Test
	public void mean_F64() {
		GrowQueue_F64 l = new GrowQueue_F64();
		l.add(0);
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);

		double found = UtilStatisticsQueue.mean(l);
		assertEquals(2,found,1e-8);
	}

	@Test
	public void variance_F64() {
		GrowQueue_F64 l = new GrowQueue_F64();
		l.add(0);
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);

		double found = UtilStatisticsQueue.variance(l,2);
		assertEquals(2.5,found,1e-8);
	}

	@Test
	public void fraction_F64() {

		GrowQueue_F64 l = new GrowQueue_F64();
		for (int i = 0; i < 100; i++) {
			l.add(i);
		}

		assertEquals(50,UtilStatisticsQueue.fraction(l,0.5),1e-8);
		assertEquals(0,UtilStatisticsQueue.fraction(l,0),1e-8);
		assertEquals(99,UtilStatisticsQueue.fraction(l,1.0),1e-8);
	}
}
