/*
 * Copyright (c) 2012-2024, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization;

import org.ddogleg.optimization.funcs.EvalFuncHelicalValley;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Common checks for all least squares implementations */
public abstract class ChecksUnconstrainedLeastSquares<M extends DMatrix> {
	protected abstract UnconstrainedLeastSquares<M> createSearch( double minimumValue );

	/** Provide a post update adjustment and see if it has been called */
	@Test void postUpdateAdjuster() {
		var adjuster = new AdjustArray_F64() {
			public int count = 0;
			@Override public void process( double[] array, int offset, int length ) {
				count++;
			}
		};

		UnconstrainedLeastSquares<M> alg = createSearch(0.0);

		var func = new EvalFuncHelicalValley<DMatrixRMaj>();

		alg.setPostUpdate(adjuster);
		alg.setFunction(func.getFunction(), null);
		alg.initialize(func.getInitial(), 1e-8, 1e-8);

		int updateCount = 0;
		for (int i = 0; i < 20 && updateCount < 3; i++) {
			if (alg.iterate())
				break;
			if (alg.isUpdated())
				updateCount++;
		}

		// Make sure the adjuster must have been called
		assertTrue(updateCount > 0);

		// Make sure it was called the appropriate number of times. If an update is rejected
		// the adjustment would be called be it not updated
		assertTrue(adjuster.count >= updateCount);
	}
}
