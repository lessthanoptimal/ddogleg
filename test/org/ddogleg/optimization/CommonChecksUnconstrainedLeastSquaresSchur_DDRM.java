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
import org.ddogleg.optimization.funcs.EvalFunctionBundle2D_DDRM;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class CommonChecksUnconstrainedLeastSquaresSchur_DDRM extends UnconstrainedLeastSquaresSchurEvaluator_DDRM {

	protected boolean checkFastConvergence = true;
	protected int maxIterationsFast = 50;

	protected CommonChecksUnconstrainedLeastSquaresSchur_DDRM() {
		super(false, false);
	}

	@Nested class CommonChecks extends ChecksUnconstrainedLeastSquaresSchur<DMatrixRMaj> {
		@Override protected UnconstrainedLeastSquaresSchur<DMatrixRMaj> createSearch( double minimumValue ) {
			return CommonChecksUnconstrainedLeastSquaresSchur_DDRM.this.createSearch(minimumValue);
		}

		@Override protected EvalFunctionBundle2D<DMatrixRMaj> createEvalFunction() {
			return new EvalFunctionBundle2D_DDRM();
		}
	}

	@Test
	public void checkBundle2D() {
		NonlinearResults results = bundle2D();

		// no algorithm to compare it against, just do some sanity checks for changes
		if( checkFastConvergence ) {
			assertTrue(results.numFunction < maxIterationsFast);
			assertTrue(results.numGradient < maxIterationsFast);
		}

		assertEquals(0, results.f, 1e-4);
	}

}
