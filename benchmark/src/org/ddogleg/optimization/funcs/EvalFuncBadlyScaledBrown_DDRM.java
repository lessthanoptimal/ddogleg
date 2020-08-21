/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrixRMaj;

/**
 *
 * <p>
 * [1] J. More, B. Garbow, K. Hillstrom, "Testing Unconstrained Optimization Software"
 * 1981 ACM Transactions on Mathematical Software, Vol 7, No. 1, Match 1981, pages 17-41
 * </p>
 *
 * @author Peter Abeles
 */
public class EvalFuncBadlyScaledBrown_DDRM implements EvalFuncLeastSquares<DMatrixRMaj> {

	@Override
	public FunctionNtoM getFunction() {
		return new Func();
	}

	@Override
	public FunctionNtoMxN<DMatrixRMaj> getJacobian() {
		return new Deriv();
	}

	@Override
	public double[] getInitial() {
		return new double[]{1,1};
	}

	@Override
	public double[] getOptimal() {
		return new double[]{1e6,2e-6};
	}

	public static class Func implements FunctionNtoM
	{
		@Override
		public int getNumOfInputsN() {return 2;}

		@Override
		public int getNumOfOutputsM() {return 3;}

		@Override
		public void process(double[] input, double[] output) {
			double x1 = input[0];
			double x2 = input[1];

			output[0] = x1-1e6;
			output[1] = x2-2e-6;
			output[2] = x1*x2-2;
		}
	}

	public static class Deriv implements FunctionNtoMxN<DMatrixRMaj>
	{
		@Override
		public int getNumOfInputsN() {return 2;}

		@Override
		public int getNumOfOutputsM() {return 3;}

		@Override
		public void process(double[] input, DMatrixRMaj output) {
			double x1 = input[0];
			double x2 = input[1];

			output.data[0] = 1;
			output.data[1] = 0;
			output.data[2] = 0;
			output.data[3] = 1;
			output.data[4] = x2;
			output.data[5] = x1;
		}

		@Override
		public DMatrixRMaj declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(),getNumOfInputsN());
		}
	}
}
