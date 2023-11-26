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

import org.ddogleg.optimization.trustregion.ConfigTrustRegion;

/**
 * @author Peter Abeles
 */
public class EvaluateTrustRegionBFGS extends UnconstrainedMinimizationEvaluator {

	public EvaluateTrustRegionBFGS( boolean verbose, boolean printScore ) {
		super(verbose, printScore);
	}

	@Override protected UnconstrainedMinimization createSearch() {
		var config = new ConfigTrustRegion();
//		config.regionInitial = 1;
//		config.scalingMinimum = 1e-6;
//		config.scalingMaximum = 1e4;

		UnconstrainedMinimization tr;
		tr = FactoryOptimization.doglegBFGS(config);
		return tr;
	}

	public static void main( String[] args ) {
		var eval = new EvaluateTrustRegionBFGS(false, true);

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
//		System.out.println("dodcfg              ----------------");
//		eval.dodcfg();
		System.out.println("variably            ----------------");
		eval.variably();
		System.out.println("trigonometric       ----------------");
		eval.trigonometric();
		System.out.println("Badly Scaled Brown  ----------------");
		eval.badlyScaledBrown();
		System.out.println("Badly Scaled Powell ----------------");
		eval.badlyScalledPowell();
	}
}
