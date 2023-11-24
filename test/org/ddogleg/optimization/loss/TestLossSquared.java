/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.loss;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ddogleg.optimization.loss.CommonChecksLossJacobian.randomArray;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TestLossSquared {
	protected Random rand = new Random(243);

	@Test void compareToManual() {
		double[] residuals = randomArray(20, 5, rand);

		double expected = 0.0;
		for (double d : residuals) {
			expected += d*d;
		}
		expected *= 0.5;

		var alg = new LossSquared();
		alg.setNumberOfFunctions(residuals.length);

		assertEquals(expected, alg.process(residuals), UtilEjml.TEST_F64);
	}
}