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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.NonlinearResults;
import org.ddogleg.optimization.UnconstrainedLeastSquaresEvaluator_DSCC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class CommonChecksUnconstrainedLeastSquares_DSCC extends UnconstrainedLeastSquaresEvaluator_DSCC {

	protected CommonChecksUnconstrainedLeastSquares_DSCC() {
		super(false, false);
	}

	@Test
	public void checkHelicalValley() {
		NonlinearResults results = helicalValley();

		// no algorithm to compare it against, just do some sanity checks for changes
		assertTrue(results.numFunction < 100);
		assertTrue(results.numGradient < 100);
		assertEquals(1, results.x[0], 1e-4);
		assertEquals(0, results.x[1], 1e-4);
		assertEquals(0, results.x[2], 1e-4);
		assertEquals(0, results.f, 1e-4);
	}

	@Test
	public void checkRosenbrock() {
		NonlinearResults results = rosenbrock();

		// no algorithm to compare it against, just do some sanity checks for changes
		assertTrue(results.numFunction < 100);
		assertTrue(results.numGradient < 100);
		assertEquals(1, results.x[0], 1e-4);
		assertEquals(1, results.x[1], 1e-4);
		assertEquals(0, results.f, 1e-4);
	}

}
