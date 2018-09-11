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

package org.ddogleg.optimization.derivative;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestNumericalJacobianForward_DSCC {
	double tol = 1e-6;

	@Test
	public void simple() {
		// give it a function where one variable does not effect the output
		// to make the test more interesting
		SimpleFunction f = new SimpleFunction();
		NumericalJacobianForward_DSCC alg = new NumericalJacobianForward_DSCC(f);

		DMatrixSparseCSC output = alg.declareMatrixMxN();
		alg.process(new double[]{2,3,7},output);

		// one element should be zero
		assertEquals(5, output.nz_length);

		assertEquals(3, output.get(0,0), tol);
		assertEquals(-36, output.get(0,1), tol);
		assertEquals(0, output.get(0,2), tol);

		assertEquals(3, output.get(1,0), tol);
		assertEquals(2, output.get(1,1), tol);
		assertEquals(1, output.get(1,2), tol);
	}

	private static class SimpleFunction implements FunctionNtoM
	{
		@Override
		public int getNumOfInputsN() {
			return 3;
		}

		@Override
		public int getNumOfOutputsM() {
			return 2;
		}

		@Override
		public void process(double[] input, double output[]) {
			double x1 = input[0];
			double x2 = input[1];
			double x3 = input[2];

			output[0] = 3*x1 - 6*x2*x2;
			output[1] = x1*x2+x3;
		}
	}
}