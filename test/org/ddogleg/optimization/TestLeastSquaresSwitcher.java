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

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.junit.jupiter.api.Test;

public class TestLeastSquaresSwitcher {
	/**
	 * Constructs different solvers and sees if it blows up
	 */
	@Test void blowsUp() {
		var config = new ConfigNonLinearLeastSquares();

		var function = new DummyFunction();
		var alg = new LeastSquaresSwitcher<>();
		for (var type : ConfigNonLinearLeastSquares.Type.values()) {
			config.type = type;

			// Construct all permutations of this type
			for (var sparse : new boolean[]{false, true}) {
				for (var schur : new boolean[]{false, true}) {
					alg.setSolver(config, sparse, schur);
					alg.setFunction(function, function);
					alg.getSolver().initialize(new double[10], 1e-7, 1e-7);
					alg.getSolver().iterate();
				}
			}
		}
	}

	/** Does nothing */
	private static class DummyFunction implements FunctionNtoM, LeastSquaresSwitcher.ProcessJacobianOut {
		@Override public void process( double[] input ) {}

		@Override public int getSchurSplit() {return 7;}

		@Override public void setJacobianOut( LeastSquaresSwitcher.JacobianOut out ) {}

		@Override public void process( double[] input, double[] output ) {}

		@Override public int getNumOfInputsN() {return 10;}

		@Override public int getNumOfOutputsM() {return 10;}
	}
}
