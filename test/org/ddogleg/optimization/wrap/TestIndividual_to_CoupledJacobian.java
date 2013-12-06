/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestIndividual_to_CoupledJacobian {

	@Test
	public void trivial() {
		DummyFunction f = new DummyFunction();
		DummyJacobian g = new DummyJacobian();
		Individual_to_CoupledJacobian alg = new Individual_to_CoupledJacobian(f,g);

		double[] input = new double[5];
		alg.setInput(input);
		alg.computeFunctions(null);
		alg.computeJacobian(null);

		assertTrue(f.input == input);
		assertTrue(g.input == input);
	}

	protected class DummyFunction implements FunctionNtoM {
		double []input;

		@Override
		public int getInputsN() {
			return 5;
		}

		@Override
		public int getOutputsM() {
			return 4;
		}

		@Override
		public void process(double[] input, double[] output) {
			this.input = input;
		}
	}

	protected class DummyJacobian implements FunctionNtoMxN {

		double []input;

		@Override
		public int getInputsN() {
			return 5;
		}

		@Override
		public int getOutputsM() {
			return 4;
		}

		@Override
		public void process(double[] input, double[] output) {
			this.input = input;
		}
	}
}
