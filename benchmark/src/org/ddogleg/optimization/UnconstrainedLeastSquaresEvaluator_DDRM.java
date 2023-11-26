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

package org.ddogleg.optimization;

import org.ddogleg.optimization.funcs.*;
import org.ejml.data.DMatrixRMaj;

public abstract class UnconstrainedLeastSquaresEvaluator_DDRM extends
		UnconstrainedLeastSquaresEvaluator<DMatrixRMaj> {

	protected UnconstrainedLeastSquaresEvaluator_DDRM( boolean verbose, boolean printSummary ) {
		super(verbose, printSummary, true);
	}

	public NonlinearResults helicalValley() {
		return performTest(new EvalFuncHelicalValley<>());
	}

	public NonlinearResults rosenbrock() {
		return performTest(new EvalFuncRosenbrock_DDRM());
	}

	public NonlinearResults rosenbrockMod( double lambda ) {
		return performTest(new EvalFuncRosenbrockMod_DDRM(lambda));
	}

	public NonlinearResults variably() {
		return performTest(new EvalFuncVariablyDimensioned<>(10));
	}

	public NonlinearResults trigonometric() {
		return performTest(new EvalFuncTrigonometric<>(10));
	}

	public NonlinearResults badlyScaledBrown() {
		return performTest(new EvalFuncBadlyScaledBrown_DDRM());
	}

	public NonlinearResults powell() {
		return performTest(new EvalFuncPowell_DDRM());
	}

	public NonlinearResults powellSingular() {
		return performTest(new EvalFuncPowellSingular_DDRM());
	}

	public NonlinearResults badlyScalledPowell() {
		return performTest(new EvalFuncBadlyScaledPowell_DDRM());
	}

	public NonlinearResults bundle2D() {
		return performTest(new EvalFunctionBundle2D_DDRM());
	}

	public NonlinearResults lineOutlier() {
		return performTest(new EvalFuncLineWithOutliers<>());
	}
}
