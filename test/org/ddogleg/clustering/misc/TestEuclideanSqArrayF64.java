/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.clustering.misc;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
class TestEuclideanSqArrayF64 {
	@Test void distance() {
		var alg = new EuclideanSqArrayF64(5);

		var a = new double[]{1, 2, 3, 4, 5};
		var b = new double[]{1, 2, 3, 4, 5};

		assertEquals(0.0, alg.distance(a, b), UtilEjml.TEST_F64);
		b[4] = 10;
		assertEquals(25.0, alg.distance(a, b), UtilEjml.TEST_F64);
	}
}