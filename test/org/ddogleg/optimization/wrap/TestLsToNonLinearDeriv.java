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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.derivative.NumericalGradientForward;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLsToNonLinearDeriv {

	@Test
	public void compareToNumeric() {
		FuncLS funcLS = new FuncLS();
		DerivLS derivLS = new DerivLS();
		FunctionNtoS func = new LsToNonLinear(funcLS);

		FunctionNtoN deriv = new LsToNonLinearDeriv(funcLS,derivLS);
		FunctionNtoN derivNumeric = new NumericalGradientForward(func);
		
		double point[] = new double[]{1,2};
		double expected[] = new double[2];
		double found[] = new double[2];
		
		deriv.process(point,found);
		derivNumeric.process(point,expected);
		
		for( int i = 0; i < expected.length; i++ ) {
			assertEquals(expected[i], found[i], 1e-4);
		}
	}

	public static class FuncLS implements FunctionNtoM
	{
		@Override
		public int getNumOfInputsN() {
			return 2;
		}

		@Override
		public int getNumOfOutputsM() {
			return 3;
		}

		@Override
		public void process(double[] input, double[] output) {
			double x1 = input[0];
			double x2 = input[1];

			output[0] = x1 + 10*x2*x2;
			output[1] = x2;
			output[2] = 2*x1+x2;
		}
	}

	public static class DerivLS implements FunctionNtoMxN<DMatrixRMaj>
	{
		@Override
		public int getNumOfInputsN() {
			return 2;
		}

		@Override
		public int getNumOfOutputsM() {
			return 3;
		}

		@Override
		public void process(double[] input, DMatrixRMaj output) {
			double x1 = input[0];
			double x2 = input[1];

			output.data[0] = 1;
			output.data[1] = 20*x2;
			output.data[2] = 0;
			output.data[3] = 1;
			output.data[4] = 2;
			output.data[5] = 1;
		}

		@Override
		public DMatrixRMaj declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(),getNumOfInputsN());
		}
	}
}
