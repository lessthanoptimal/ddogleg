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

package org.ddogleg.nn.alg.distance;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestKdTreeEuclideanSq_F64 {
	@Test
	public void distance() {
		double a[] = {1,2,3,4};
		double b[] = {1,2,3,7};

		KdTreeEuclideanSq_F64 alg = new KdTreeEuclideanSq_F64(4);
		assertEquals( 9,alg.distance(a,b) , UtilEjml.TEST_F64);
	}

	@Test
	public void valueAt() {
		KdTreeEuclideanSq_F64 alg = new KdTreeEuclideanSq_F64(4);

		double a[] = {1,2,3,4};
		for (int i = 0; i < 4; i++) {
			assertEquals( i+1,alg.valueAt(a,i) , UtilEjml.TEST_F64);
		}
	}
}