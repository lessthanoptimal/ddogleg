/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestDerivativeChecker {
	@Test
	public void gradient() {

		SimpleS f = new SimpleS();
		SimpleGradient g = new SimpleGradient();

		assertTrue(DerivativeChecker.gradient(f,g,new double[]{2,4.5},1e-5));
	}

	private static class SimpleS implements FunctionNtoS {

		@Override
		public int getNumOfInputsN() {
			return 2;
		}

		@Override
		public double process(double[] input) {
			double x = input[0];
			double y = input[1];

			return 2*x*x + 0.5*y*y + 6.7*x*y;
		}
	}

	private static class SimpleGradient implements FunctionNtoN {

		@Override
		public int getN() {
			return 2;
		}

		@Override
		public void process(double[] input, double[] output ) {
			double x = input[0];
			double y = input[1];

			output[0] = 4*x + 6.7*y;
			output[1] = y + 6.7*x;
		}
	}
}
