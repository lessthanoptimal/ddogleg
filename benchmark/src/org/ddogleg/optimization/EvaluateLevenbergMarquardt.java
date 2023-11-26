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

import org.ddogleg.optimization.lm.ConfigLevenbergMarquardt;

/**
 * @author Peter Abeles
 */
public class EvaluateLevenbergMarquardt extends UnconstrainedLeastSquaresEvaluator_DDRM {

	public EvaluateLevenbergMarquardt( boolean verbose ) {
		super(verbose, true);
	}

	@Override protected UnconstrainedLeastSquares createSearch( double minimumValue ) {

		var config = new ConfigLevenbergMarquardt();
		config.dampeningInitial = 1e-8;
		config.hessianScaling = true;

		boolean robust = false;

		return FactoryOptimization.levenbergMarquardt(config, robust);
	}

	public static void main( String[] args ) {
		var eval = new EvaluateLevenbergMarquardt(false);

		System.out.println("Powell              ----------------");
		eval.powell();
		System.out.println("Powell Singular     ----------------");
		eval.powellSingular();
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
		System.out.println("Badly Scaled Powell ----------------");
		eval.badlyScalledPowell();
		System.out.println("Bundle 2D           ----------------");
		eval.bundle2D();
	}
}
