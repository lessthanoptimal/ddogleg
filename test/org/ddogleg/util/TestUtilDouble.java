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

package org.ddogleg.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestUtilDouble {

	@Test
	public void sum_F64() {
		double total[] = new double[10];
		double expected = 0;
		for( int i = 0; i < total.length; i++ ) {
			expected += i+1;
			total[i] = i+1;
		}

		double found = UtilDouble.sum(total);

		assertEquals(expected,found,1e-8);
	}

	@Test
	public void sum_F32() {
		float total[] = new float[10];
		float expected = 0;
		for( int i = 0; i < total.length; i++ ) {
			expected += i+1;
			total[i] = i+1;
		}

		float found = UtilDouble.sum(total);

		assertEquals(expected,found,1e-8);
	}

}
