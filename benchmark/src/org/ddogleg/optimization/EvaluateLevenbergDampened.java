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

package org.ddogleg.optimization;

import org.ddogleg.optimization.impl.LevenbergDampened_DDRM;
import org.ddogleg.optimization.wrap.LevenbergDampened_to_UnconstrainedLeastSquares;

/**
 * @author Peter Abeles
 */
public class EvaluateLevenbergDampened extends UnconstrainedLeastSquaresEvaluator_DDRM {

	double dampInit = 1e-3;

	public EvaluateLevenbergDampened(boolean verbose) {
		super(verbose, true);
	}

	@Override
	protected UnconstrainedLeastSquares createSearch(double minimumValue) {

		LevenbergDampened_DDRM alg = new LevenbergDampened_DDRM(dampInit);
		return new LevenbergDampened_to_UnconstrainedLeastSquares(alg);
	}

	public static void main( String args[] ) {
		EvaluateLevenbergDampened eval = new EvaluateLevenbergDampened(false);

		System.out.println("Powell              ----------------");
		eval.powell();
		System.out.println("Helical Valley      ----------------");
		eval.helicalValley();
		System.out.println("Rosenbrock          ----------------");
		eval.rosenbrock();
		System.out.println("Rosenbrock Mod      ----------------");
		eval.rosenbrockMod(Math.sqrt(2*1e6));
		System.out.println("variably            ----------------");
		eval.variably();
		System.out.println("trigonometric       ----------------");
		eval.trigonometric();
		System.out.println("Badly Scaled Brown  ----------------");
		eval.badlyScaledBrown();
		System.out.println("Bundle 2D           ----------------");
		eval.bundle2D();
	}
}
