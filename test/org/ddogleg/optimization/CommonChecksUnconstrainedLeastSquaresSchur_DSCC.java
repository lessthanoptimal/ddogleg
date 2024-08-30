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

import org.ddogleg.optimization.funcs.EvalFunctionBundle2D;
import org.ddogleg.optimization.funcs.EvalFunctionBundle2D_DSCC;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class CommonChecksUnconstrainedLeastSquaresSchur_DSCC extends UnconstrainedLeastSquaresSchurEvaluator_DSCC {

	protected boolean checkFastConvergence = true;

	protected CommonChecksUnconstrainedLeastSquaresSchur_DSCC() {
		super(false, false);
	}

	@Nested class CommonChecks extends ChecksUnconstrainedLeastSquaresSchur<DMatrixSparseCSC> {
		@Override protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch( double minimumValue ) {
			return CommonChecksUnconstrainedLeastSquaresSchur_DSCC.this.createSearch(minimumValue);
		}

		@Override protected EvalFunctionBundle2D<DMatrixSparseCSC> createEvalFunction() {
			return new EvalFunctionBundle2D_DSCC();
		}
	}

	@Test
	public void checkBundle2D() {
		NonlinearResults results = bundle2D();

		// no algorithm to compare it against, just do some sanity checks for changes
		if( checkFastConvergence ) {
			assertTrue(results.numFunction < 50);
			assertTrue(results.numGradient < 50);
		}

		assertEquals(0, results.f, 1e-4);
	}

}
